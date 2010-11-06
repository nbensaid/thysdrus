/*
 * Copyright 2010 Nabil Ben Said.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.greencoding.thysdrus.circuitbreaker.aspect;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerMethodRegistry {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private ReentrantLock exclusiveHalfOpenLock = new ReentrantLock();

	// needed for checking the CLOSED cycle
	private ThreadLocal<Map<String, Long>> threadLocalMap = new ThreadLocal<Map<String, Long>>();

	// map with global state of CircuitBreaker entries
	private final Map<String, CircuitBreakerRegistryEntry> globalMap = new ConcurrentHashMap<String, CircuitBreakerRegistryEntry>();

	// this method is call before every CircuitBreaker annotated method is
	// called.
	public void registerMehtodIfnecessary(String method, MonitoredByCircuitBreaker annotation) {
		Map<String, Long> localMap = threadLocalMap.get();
		if (localMap == null) {
			localMap = new HashMap<String, Long>();
			threadLocalMap.set(localMap);
		}
		// this shall be null - recursion is not supported
		assert threadLocalMap.get().get(method) == null;

		CircuitBreakerRegistryEntry entry = globalMap.get(method);
		if (entry == null) {
			entry = createCircuitBreakerRegistryEntry(method, annotation);
			globalMap.put(method, entry);
		}
		localMap.put(method, Long.valueOf(entry.getClosedCycleCounter()));

	}

	/**
	 * method changes the status from HALF_OPEN to CLOSED then unlock the
	 * exclusiveHalfOpenLock. the caller must make sure that he has the half
	 * open exclusive lock before invoking this method.
	 * 
	 * @see getStatusWithHalfOpenExclusiveLockTry()
	 * @param method
	 */
	public void closeAndUnlock(String method) {
		assert exclusiveHalfOpenLock.isHeldByCurrentThread();
		CircuitBreakerRegistryEntry entry = globalMap.get(method);
		assert entry.getStatus().equals(CircuitBreakerStatus.HALF_OPEN);
		entry.getFailures().clear();
		entry.setStatus(CircuitBreakerStatus.CLOSED);
		entry.increaseClosedCycleCounter();

		exclusiveHalfOpenLock.unlock();
	}

	/**
	 * method changes the status from HALF_OPEN to OPEN then unlock the
	 * exclusiveHalfOpenLock. the caller must make sure that he has the half
	 * open exclusive lock before invoking this method.
	 * 
	 * @see getStatusWithHalfOpenExclusiveLockTry()
	 * @param method
	 */

	public void keepOpenAndUnlock(String method) {
		assert exclusiveHalfOpenLock.isHeldByCurrentThread();
		CircuitBreakerRegistryEntry entry = globalMap.get(method);
		assert entry.getStatus().equals(CircuitBreakerStatus.HALF_OPEN);
		entry.getFailures().clear();
		entry.setStatus(CircuitBreakerStatus.OPEN);
		entry.setLastOpenedTime(System.currentTimeMillis());

		exclusiveHalfOpenLock.unlock();
	}

	/**
	 * method returns true if closed cycle at the beginning of the method
	 * invocation is the same as at the end of the invocation, otherwise returns
	 * false.
	 * 
	 * @param method
	 * @return
	 */
	public boolean sameClosedCycleInLocalAndGlobaleContext(String method) {
		if (threadLocalMap.get().get(method) == globalMap.get(method).getClosedCycleCounter()) {
			return true;
		}
		return false;
	}

	/**
	 * method returns the {@link CircuitBreakerStatus} and tries to get the
	 * exclusive lock for the {@link CircuitBreakerStatus}.HALF_OPEN status in
	 * case the condition for the half open status is statisfied. If the lock
	 * succeed it saves HALF_OPEN state into the registry and returns
	 * HALF_OPEN_EXCLUSIVE to signal to the caller, that the current thread got
	 * the exclusive lock. It returns {@link CircuitBreakerStatus}.
	 * 
	 * Method may returns also one of the status CLOSED, OPEN or HALF_OPEN.
	 * HALF_OPEN is returned only if one of the concurrent threads has already
	 * the exclusive lock.
	 * 
	 * 
	 * @param method
	 * @return
	 */
	public CircuitBreakerStatus getStatusWithHalfOpenExclusiveLockTry(String method) {
		CircuitBreakerRegistryEntry entry = globalMap.get(method);
		if (entry.getStatus().equals(CircuitBreakerStatus.OPEN) && System.currentTimeMillis() - entry.getLastOpenedTime() >= entry.getRetryAfterMs()) {
			// condition for half open is statisfied, try to get the halfopene
			// exclusive lock.
			if (exclusiveHalfOpenLock.tryLock()) {
				// the lock will be release in the cleanUp method.
				entry.setStatus(CircuitBreakerStatus.HALF_OPEN);
				return CircuitBreakerStatus.HALF_OPEN_EXCLUSIVE;
			}
		}
		return entry.getStatus();
	}

	private CircuitBreakerRegistryEntry createCircuitBreakerRegistryEntry(String method, MonitoredByCircuitBreaker circuitBreaker) {
		int failureThreshold = circuitBreaker.failureThreshold();
		long failureThresholdTimeFrameMs = circuitBreaker.failureThresholdTimeFrameMs();
		long retryAfterMs = circuitBreaker.retryAfterMs();
		List<Class<? extends Throwable>> faultIndications = null;
		if (circuitBreaker.failureIndications() != null && circuitBreaker.failureIndications().length > 0) {
			faultIndications = Arrays.asList(circuitBreaker.failureIndications());
		} else {
			faultIndications = Collections.emptyList();
		}

		return new CircuitBreakerRegistryEntry(method, failureThreshold, failureThresholdTimeFrameMs, retryAfterMs, faultIndications);
	}

	/**
	 * method adds failure for the given method name. Checks if failure
	 * threshold is achieved, in case of set status to OPEN and return true,
	 * otherwise return false;
	 * 
	 * @param method
	 * @return true if status is changed from CLOSED to OPEN otherwise false.
	 */
	public synchronized boolean addFailureAndOpenCircuitIfThresholdAchived(String method) {
		CircuitBreakerRegistryEntry entry = globalMap.get(method);
		if (entry.getFailures().size() == entry.getFailureThreshold()) {
			entry.getFailures().remove(0);
		}
		long now = System.currentTimeMillis();
		entry.getFailures().add(Long.valueOf(now));
		if (!entry.getStatus().equals(CircuitBreakerStatus.OPEN) && entry.getFailures().size() == entry.getFailureThreshold()
				&& now - entry.getFailures().get(0) <= entry.getFailureThresholdTimeFrameMs()) {
			// open condition is full filled
			entry.setStatus(CircuitBreakerStatus.OPEN);
			entry.setLastOpenedTime(now);
			entry.getFailures().clear();
			return true;
		}
		return false;
	}

	public void cleanUp(String method) {
		// a Lock can be hold multiple times in a thread that's why we use an while loop here.
		while (exclusiveHalfOpenLock.isHeldByCurrentThread()){
			exclusiveHalfOpenLock.unlock();
			logger.error("exclusiveHalfOpenLock was not properly unlocked");
		}
		threadLocalMap.get().remove(method);
	}

	public List<Class<? extends Throwable>> getfailureIndications(String method) {
		return globalMap.get(method).getFailureIndications();
	}

	// needed only for unit tests
	CircuitBreakerRegistryEntry getEntry(String method) {
		return globalMap.get(method);
	}
}

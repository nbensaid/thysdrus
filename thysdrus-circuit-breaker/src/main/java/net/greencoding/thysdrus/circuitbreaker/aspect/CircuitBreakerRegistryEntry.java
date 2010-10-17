/*
 * Copyright 2010 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@net-m.de)
 *
 */
public class CircuitBreakerRegistryEntry {

	private final String name;

	private final int failureThreshold;

	private final long failureThresholdTimeFrameMs;

	private final long retryAfterMs;
	
	private final List<Class<? extends Throwable>> failureIndications;

	private CircuitBreakerStatus status;

	private ArrayList<Long> failures;

	private long lastOpenedTime;

	// counter for the closed cycles. needed to detect if the failure corresponds to the current closed cycle or this failure is a result of 
	// a long running method started in previous closed cycle.
	private long closedCycleCounter = 0;

	public CircuitBreakerRegistryEntry(String name, int failureThreshold, long failureThresholdTimeFrameMs, long retryAfterMs, List<Class<? extends Throwable>> failureIndications) {
		this.name = name;
		this.failureThreshold = failureThreshold;
		this.failureThresholdTimeFrameMs = failureThresholdTimeFrameMs;
		this.retryAfterMs = retryAfterMs;
		this.failureIndications = failureIndications;
		this.failures = new  ArrayList<Long>();
		this.status = CircuitBreakerStatus.CLOSED;
	}

	public CircuitBreakerStatus getStatus() {
		return status;
	}

	public void setStatus(CircuitBreakerStatus status) {
		this.status = status;
	}

	public ArrayList<Long> getFailures() {
		return failures;
	}

	public void setFailures(ArrayList<Long> failures) {
		this.failures = failures;
	}

	public String getName() {
		return name;
	}

	public int getFailureThreshold() {
		return failureThreshold;
	}

	public long getFailureThresholdTimeFrameMs() {
		return failureThresholdTimeFrameMs;
	}

	public long getRetryAfterMs() {
		return retryAfterMs;
	}

	public List<Class<? extends Throwable>> getFailureIndications() {
		return failureIndications;
	}

	public void increaseClosedCycleCounter() {
		this.closedCycleCounter = closedCycleCounter + 1;
	}

	public long getClosedCycleCounter() {
		return closedCycleCounter;
	}

	public long getLastOpenedTime() {
		return lastOpenedTime;
	}

	public void setLastOpenedTime(long lastOpenedTime) {
		this.lastOpenedTime = lastOpenedTime;
	}

}

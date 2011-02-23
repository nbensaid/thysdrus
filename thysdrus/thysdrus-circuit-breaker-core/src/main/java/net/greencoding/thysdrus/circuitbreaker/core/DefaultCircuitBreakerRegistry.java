package net.greencoding.thysdrus.circuitbreaker.core;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;

import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerEntry;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerRegisterResult;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Circuit Breaker Registry. It manages all circuit breakers. 
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 * 
 */
public final class DefaultCircuitBreakerRegistry implements CircuitBreakerRegistry {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private final String name;
	
	private Map<String, CircuitBreakerEntry> registry = new HashMap<String, CircuitBreakerEntry>();

	protected DefaultCircuitBreakerRegistry(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#halfOpenConditionSatisfied(java.lang.String)
	 */
	@Override
	public boolean halfOpenConditionSatisfied(String circuitBreakerKey){
		CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
		
		if (entry.getStatus().equals(CircuitBreakerStatus.OPEN) && 
				System.currentTimeMillis() - entry.getLastOpenedTimestamp() >= entry.getCircuitBreaker().getRetryTimeoutMs()) {
			logger.info("condition for HALF-OPEN state is satisfied. CB-key: {}", circuitBreakerKey);
			// condition for half open is satisfied
			return true;
		}

		return false;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#isRegistered(java.lang.String)
	 */
	@Override
	public boolean isRegistered(String circuitBreakerKey){
		return registry.containsKey(circuitBreakerKey);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#
	 * registerCircuitBreaker
	 * (net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker)
	 */
	@Override
	public CircuitBreakerRegisterResult registerCircuitBreaker(CircuitBreaker circuitBreaker) {
		assert circuitBreaker != null;
		logger.info("register CB: {}", circuitBreaker);

		CircuitBreakerEntry entry = null;

		synchronized (this) {	
			entry = registry.get(circuitBreaker.getCircuitBreakerKey());
			if (entry != null) {
				logger.warn("you are trying to register CB twice. {}", circuitBreaker);
				return new CircuitBreakerRegisterResult(false, "CB is already registered");
			}

			entry = new CircuitBreakerEntry(circuitBreaker);
			registry.put(circuitBreaker.getCircuitBreakerKey(), entry);
		}

		logger.info("successfully registered CB under CB-Entry {}", entry);
		return new CircuitBreakerRegisterResult(true, "CB successfully registered");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#
	 * getCircuitBreakerStatus(java.lang.String)
	 */
	@Override
	public CircuitBreakerStatus getCircuitBreakerStatus(String circuitBreakerKey) {
		CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
		if (entry == null) {
			throw new IllegalStateException("No CircuitBreakerEntry found for key: " + circuitBreakerKey);
		}
		CircuitBreakerStatus status = entry.getStatus();
		logger.info("Status from CB with key {} is {}", circuitBreakerKey, status);
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#
	 * tryHalfOpenLock(java.lang.String)
	 */
	@Override
	public boolean tryHalfOpenLock(String circuitBreakerKey) {
		CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
		Lock lock = entry.getHalfOpenLock();
		boolean successfully = lock.tryLock();
		if (successfully) {
			logger.info("successfully occured half-open lock by {}", circuitBreakerKey);
		} else {
			logger.info("CB {} coudn't occure half-open lock ", circuitBreakerKey);
		}
		return successfully;
	}
	
	/*
	 * (non-Javadoc)
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#releaseHalfOpenLock(java.lang.String, net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus)
	 */
	@Override
	public boolean releaseHalfOpenLock(String circuitBreakerKey, CircuitBreakerStatus status) {
		CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
		if (entry == null) {
			throw new IllegalStateException("No CB-Entry found with CB key " + circuitBreakerKey);
		}
		if (!entry.getStatus().equals(CircuitBreakerStatus.HALF_OPEN)) {
			logger.warn("CB with key {} is not in HALF-OPEN status currently.");
			return false;
		}
		synchronized (entry) {
			Lock lock = entry.getHalfOpenLock();
			lock.unlock();
			entry.setStatus(status);
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#
	 * resetCircuitBreaker(java.lang.String)
	 */
	@Override
	public void resetCircuitBreaker(String circuitBreakerKey) {
		CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
		if (entry == null) {
			logger.warn("no CB Entry found for key {}", circuitBreakerKey);
		}
		entry.setStatus(CircuitBreakerStatus.CLOSED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#
	 * unregisterCircuitBreaker(java.lang.String)
	 */
	@Override
	public void unregisterCircuitBreaker(String circuitBreakerKey) {
		CircuitBreakerEntry entry = registry.remove(circuitBreakerKey);
		if (entry == null) {
			logger.warn("no CB Entry found for key {}", circuitBreakerKey);
		} else {
			logger.info("removed CB Entry with key {} from registry.", circuitBreakerKey);
		}
	}

	/*
	 * (non-Javadoc)
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#handleMethodInvocationException(java.lang.String, java.lang.Throwable)
	 */
	@Override
	public boolean handleMethodInvocationException(final String circuitBreakerKey, final Throwable cause, final boolean inHalfOpenStatus) {
		boolean isFailure = false;
		synchronized (this) {
			CircuitBreakerEntry entry = registry.get(circuitBreakerKey);
			isFailure = isFailureIndicator(entry.getCircuitBreaker().getFailureIndications(), cause);
			if (isFailure) {
				long now = System.currentTimeMillis();
				entry.addFailureTimestamp(now);
				if (inHalfOpenStatus){
					releaseHalfOpenLock(circuitBreakerKey, CircuitBreakerStatus.OPEN);
					entry.setLastOpenedTimestamp(now);
				} else if (openConditionSatisfied(entry, now)) { 
					entry.setStatus(CircuitBreakerStatus.OPEN);
					entry.setLastOpenedTimestamp(now);
					entry.getLastFailureTimestamps().clear();
				}
				// TODO fire stats events
			}
		}
		return isFailure;
	}

	private boolean openConditionSatisfied(CircuitBreakerEntry entry, long now) {
		if (!entry.getStatus().equals(CircuitBreakerStatus.OPEN) && 
				entry.getLastFailureTimestamps().size() == entry.getCircuitBreaker().getFailureThreshold() && 
				((now - entry.getLastFailureTimestamps().get(0)) <= entry.getCircuitBreaker().getFailureThresholdTimeFrameMs()) ) {
			// open condition is satisfied
			return true;
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry#destroy
	 * ()
	 */
	@Override
	public void destroy() {
		registry.clear();
	}

	public String getName() {
		return name;
	}

	private boolean isFailureIndicator(Class<? extends Throwable>[] failureIndications, Throwable cause) {
		for (Class<? extends Throwable> clazz : failureIndications){
			if (clazz.isAssignableFrom(cause.getClass())) {
				return true;
			}
		}
		return false;
	}
}

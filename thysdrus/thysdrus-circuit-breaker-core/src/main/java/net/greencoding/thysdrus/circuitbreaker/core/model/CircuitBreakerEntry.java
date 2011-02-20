package net.greencoding.thysdrus.circuitbreaker.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Entry class holds circuit breaker status and all needed information for
 * status transition.
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 * 
 */
public class CircuitBreakerEntry {

	private CircuitBreaker circuitBreaker;

	private CircuitBreakerStatus status = CircuitBreakerStatus.CLOSED;

	private Lock halfOpenLock = new ReentrantLock();;

	private List<Long> lastFailureTimestamps = new ArrayList<Long>();

	private long lastOpenedTimestamp;

	public CircuitBreakerEntry(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
	}

	public CircuitBreaker getCircuitBreaker() {
		return circuitBreaker;
	}

	public CircuitBreakerStatus getStatus() {
		return status;
	}

	public Lock getHalfOpenLock() {
		return halfOpenLock;
	}

	public void setCircuitBreaker(CircuitBreaker circuitBreaker) {
		this.circuitBreaker = circuitBreaker;
	}

	public void setStatus(CircuitBreakerStatus status) {
		this.status = status;
	}

	public void setHalfOpenLock(Lock halfOpenLock) {
		this.halfOpenLock = halfOpenLock;
	}

	public List<Long> getLastFailureTimestamps() {
		return lastFailureTimestamps;
	}

	public synchronized void addFailureTimestamp(long timestamp) {
		if (lastFailureTimestamps.size() == circuitBreaker.getFailureThreshold()) {
			lastFailureTimestamps.remove(0);
		}
		lastFailureTimestamps.add(timestamp);
	}

	public long getLastOpenedTimestamp() {
		return lastOpenedTimestamp;
	}

	public void setLastOpenedTimestamp(long lastOpenedTimestamp) {
		this.lastOpenedTimestamp = lastOpenedTimestamp;
	}

}

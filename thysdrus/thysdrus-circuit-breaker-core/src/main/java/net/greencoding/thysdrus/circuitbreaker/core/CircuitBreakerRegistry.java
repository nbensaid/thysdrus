package net.greencoding.thysdrus.circuitbreaker.core;

import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerRegisterResult;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 *
 */
public interface CircuitBreakerRegistry {
	
	public boolean isRegistered(String circuitBreakerKey);
	
	public CircuitBreakerRegisterResult registerCircuitBreaker(CircuitBreaker circuitBreaker);
	
	public CircuitBreakerStatus getCircuitBreakerStatus(String circuitBreakerKey);
	
	public boolean halfOpenConditionSatisfied(String circuitBreakerKey);
	
	/**
	 * tries to get the HalfOpenLock for the CirucuitBreaker with the given CircuitBreakerkey
	 * 
	 * @param CircuitBreakerKey  key of circuit breaker to lock
	 * @return
	 */
	public boolean tryHalfOpenLock(String circuitBreakerKey);
	
	/**
	 * releases the HalfOpenLock if any exits. 
	 * @param circuitBreakerKey
	 * @return true if lock is release successfully
	 */
	public boolean releaseHalfOpenLock(String circuitBreakerKey, CircuitBreakerStatus status);
	
	/**
	 * resets the circuit breaker status to Closed.
	 * @param CircuitBreakerKey key of the circuit breaker to reset.
	 */
	public void resetCircuitBreaker(String circuitBreakerKey);
	
	public void unregisterCircuitBreaker(String circuitBreakerKey);
	
	public void destroy();

	/**
	 * returns true if cause is registered as failure indicator for circuit breaker with the given key.
	 * @param circuitBreakerKey
	 * @param cause
	 * @return
	 */
	public boolean handleMethodInvocationException(String circuitBreakerKey, Throwable cause, boolean inHalfOpenStatus);

	
}

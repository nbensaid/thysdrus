package net.greencoding.thysdrus.circuitbreaker.core.handler;

import net.greencoding.thysdrus.circuitbreaker.core.handler.exception.CircuitBreakerMethodExecutionException;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;

import org.aspectj.lang.ProceedingJoinPoint;

/**
 * 
 * CircuitBreakerHandler handles the flow and the CB status transitions.
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 *
 */
public interface CircuitBreakerHandler {
	
	/**
	 * handles the method invocation passed in the ProceedingJoinPoint with the surrounding Circuit Breaker for this method.
	 * @param pjp PrceedingJoinPoint
	 * @param circuitBreaker the circuitBreaker surrounding this method invocation.
	 * @return
	 * @throws CircuitBreakerMethodExecutionException which includes the real exception while method invocation as cause.
	 */
	public MethodInvocationResult handleMethodInvocation(ProceedingJoinPoint pjp, CircuitBreaker circuitBreaker);
	
}

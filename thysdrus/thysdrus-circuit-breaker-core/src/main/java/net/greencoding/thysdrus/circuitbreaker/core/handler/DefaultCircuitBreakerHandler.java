package net.greencoding.thysdrus.circuitbreaker.core.handler;

import net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry;
import net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistryFactory;
import net.greencoding.thysdrus.circuitbreaker.core.handler.exception.CircuitBreakerMethodExecutionException;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus;

import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 *
 */
public class DefaultCircuitBreakerHandler implements CircuitBreakerHandler {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CircuitBreakerRegistry registry = CircuitBreakerRegistryFactory.getSingelton();

	private static CircuitBreakerHandler singelton = null; 
		
	public static CircuitBreakerHandler getSingleton() {
		if (singelton == null){
			singelton = new DefaultCircuitBreakerHandler();
		}
		return singelton;
	}
	
	
	/*
	 * (non-Javadoc)
	 * @see net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerHandler#handleMethodInvocation(org.aspectj.lang.ProceedingJoinPoint, net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker)
	 */
	@Override
	public MethodInvocationResult handleMethodInvocation(final ProceedingJoinPoint pjp, final CircuitBreaker circuitBreaker) {
		if (circuitBreaker == null) {
			throw new IllegalStateException("CB is null");
		}
		String circuitBreakerKey = circuitBreaker.getCircuitBreakerKey();
		MethodInvocationResult methodInvocationResult = new MethodInvocationResult();
		
		if (! registry.isRegistered(circuitBreakerKey)) {
			registry.registerCircuitBreaker(circuitBreaker);
		}
		
		final CircuitBreakerStatus status = registry.getCircuitBreakerStatus(circuitBreakerKey);
		
		if (CircuitBreakerStatus.CLOSED.equals(status)){
			logger.info("CB {} is in CLOSED state - execute method and track the result", circuitBreakerKey);
			proceed(pjp, circuitBreakerKey, methodInvocationResult, false);
		} 
		
		if (CircuitBreakerStatus.HALF_OPEN.equals(status)) {
			logger.info("CB {} is in HALF-OPEN State - skip method execution", circuitBreakerKey);
			methodInvocationResult.setSuccessfullyTerminated(false);
			methodInvocationResult.setBlockedByCircuitBreaker(true);
			methodInvocationResult.setReturnObject(null);
			methodInvocationResult.setCircuitBreakerStatus(status);
		} 

		if (CircuitBreakerStatus.OPEN.equals(status)) {
			// 1. check half-open condition 
			// 2. try get lock 
			// 3. decide if you can proceed with method execution or return
			boolean inHalfOpen = false;
			if (registry.halfOpenConditionSatisfied(circuitBreakerKey)) {
				inHalfOpen = registry.tryHalfOpenLock(circuitBreakerKey); 
			}
			if (inHalfOpen) {
				proceed(pjp, circuitBreakerKey, methodInvocationResult, false);
			} else {
				methodInvocationResult.setSuccessfullyTerminated(false);
				methodInvocationResult.setBlockedByCircuitBreaker(true);
				methodInvocationResult.setReturnObject(null);
				methodInvocationResult.setCircuitBreakerStatus(status);
				logger.info("CB {} is OPEN. Method invocation blocked.", circuitBreakerKey);
			}
		}
		
		return methodInvocationResult;
	}

	private void proceed(final ProceedingJoinPoint pjp, final String circuitBreakerKey, final MethodInvocationResult methodInvocationResult, boolean inHalfOpen) {
		try {
			methodInvocationResult.setReturnObject(proceed(pjp));
			methodInvocationResult.setSuccessfullyTerminated(true);
			if (inHalfOpen) {
				registry.releaseHalfOpenLock(circuitBreakerKey, CircuitBreakerStatus.CLOSED);
			}
			logger.info("succesfully executed CB-key: {}, MethodInvocationResult: {}", circuitBreakerKey, methodInvocationResult);
		} catch (CircuitBreakerMethodExecutionException e) {
			methodInvocationResult.setCause(e.getCause());
			boolean isFailure = registry.handleMethodInvocationException(circuitBreakerKey, e.getCause(), inHalfOpen);
			if (isFailure) {
				methodInvocationResult.setFailureWhileExecution(true);
			} else {
				methodInvocationResult.setSuccessfullyTerminated(true);
			}
			logger.info("failure while execution: CB-key: {}, MethodInvocationResult: {}", circuitBreakerKey, methodInvocationResult);
		}
	}
	
	private Object proceed(final ProceedingJoinPoint pjp) throws CircuitBreakerMethodExecutionException {
		try {
			return pjp.proceed();
		} catch (Throwable t) {
			logger.warn("Exception while method execution: {}", pjp.getSignature().toLongString());
			throw new CircuitBreakerMethodExecutionException(t);
		}
	}
}

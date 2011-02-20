package net.greencoding.thysdrus.circuitbreaker.core.handler;

import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 *
 */
public class MethodInvocationResult {

	private long executionTime;
	
	private boolean successfullyTerminated = false;
	
	private boolean blockedByCircuitBreaker = false;
	
	private CircuitBreakerStatus circuitBreakerStatus;
	
	private Object returnObject;
	
	/**
	 * if failureWhileExecution is set then check the cause property. It must be also set.
	 * cause property can also be set, even if failureWhileExecution is false. 
	 */
	private boolean failureWhileExecution = false;

	/**
	 * not catched exception thrown while method execution 
	 */
	private Throwable cause;

	public long getExecutionTime() {
		return executionTime;
	}

	public void setExecutionTime(long executionTime) {
		this.executionTime = executionTime;
	}

	public boolean isSuccessfullyTerminated() {
		return successfullyTerminated;
	}

	public void setSuccessfullyTerminated(boolean successfullyTerminated) {
		this.successfullyTerminated = successfullyTerminated;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	public void setReturnObject(Object returnObject) {
		this.returnObject = returnObject;
	}

	public boolean isFailureWhileExecution() {
		return failureWhileExecution;
	}

	public void setFailureWhileExecution(boolean failureWhileExecution) {
		this.failureWhileExecution = failureWhileExecution;
	}

	public Throwable getCause() {
		return cause;
	}

	public void setCause(Throwable cause) {
		this.cause = cause;
	}

	public boolean isBlockedByCircuitBreaker() {
		return blockedByCircuitBreaker;
	}

	public void setBlockedByCircuitBreaker(boolean blockedByCircuitBreaker) {
		this.blockedByCircuitBreaker = blockedByCircuitBreaker;
	}

	public CircuitBreakerStatus getCircuitBreakerStatus() {
		return circuitBreakerStatus;
	}

	public void setCircuitBreakerStatus(CircuitBreakerStatus circuitBreakerStatus) {
		this.circuitBreakerStatus = circuitBreakerStatus;
	}

	@Override
	public String toString() {
		return "MethodInvocationResult [executionTime=" + executionTime + ", successfullyTerminated=" + successfullyTerminated + ", blockedByCircuitBreaker="
				+ blockedByCircuitBreaker + ", circuitBreakerStatus=" + circuitBreakerStatus + ", returnObject=" + returnObject + ", failureWhileExecution="
				+ failureWhileExecution + ", cause=" + cause + "]";
	}
	
}

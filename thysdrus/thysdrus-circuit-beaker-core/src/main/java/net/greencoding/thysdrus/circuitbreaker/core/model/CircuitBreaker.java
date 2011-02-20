package net.greencoding.thysdrus.circuitbreaker.core.model;

import java.lang.reflect.Method;
import java.util.List;

/**
 * CircuitBreaker model class
 * TODO implement a Builder for this class. This class must be immutable.
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 *
 */
public class CircuitBreaker {

	/**
	 * CB key. typically the method signature. If CB is defined in a CB Group then the group name will be used as circuit breaker key.
	 */
	private String circuitBreakerKey;
	
	private String circuitBreakerGroup;
	
	private List<Method> registeredMethods;
	
	private int failureThreshold; 
	
	private long failureThresholdTimeFrameMs;
	
	private long retryTimeoutMs;
	
	private Class<? extends Throwable> [] failureIndications;

	public String getCircuitBreakerKey() {
		return circuitBreakerKey;
	}

	public void setCircuitBreakerKey(String circuitBreakerKey) {
		this.circuitBreakerKey = circuitBreakerKey;
	}

	public String getCircuitBreakerGroup() {
		return circuitBreakerGroup;
	}

	public void setCircuitBreakerGroup(String circuitBreakerGroup) {
		this.circuitBreakerGroup = circuitBreakerGroup;
	}

	public List<Method> getRegisteredMethods() {
		return registeredMethods;
	}

	public void setRegisteredMethods(List<Method> registeredMethods) {
		this.registeredMethods = registeredMethods;
	}

	public int getFailureThreshold() {
		return failureThreshold;
	}

	public void setFailureThreshold(int failureThreshold) {
		this.failureThreshold = failureThreshold;
	}

	public long getFailureThresholdTimeFrameMs() {
		return failureThresholdTimeFrameMs;
	}

	public void setFailureThresholdTimeFrameMs(long failureThresholdTimeFrameMs) {
		this.failureThresholdTimeFrameMs = failureThresholdTimeFrameMs;
	}

	public long getRetryTimeoutMs() {
		return retryTimeoutMs;
	}

	public void setRetryTimeoutMs(long retryTimeoutMs) {
		this.retryTimeoutMs = retryTimeoutMs;
	}

	public Class<? extends Throwable>[] getFailureIndications() {
		return failureIndications;
	}

	public void setFailureIndications(Class<? extends Throwable>[] failureIndications) {
		this.failureIndications = failureIndications;
	}
	
}

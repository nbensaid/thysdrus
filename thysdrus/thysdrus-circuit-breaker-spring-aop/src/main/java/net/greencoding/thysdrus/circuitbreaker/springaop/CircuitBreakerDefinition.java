package net.greencoding.thysdrus.circuitbreaker.springaop;


/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
public class CircuitBreakerDefinition {

	private String circuitBreakerKey;
	
	private int failureThreshold = 5; 
	
	private long failureThresholdTimeFrameMs =  60000L;
	
	private long retryTimeoutMs = 10000L;
	
	@SuppressWarnings("unchecked")
	private Class<? extends Throwable> [] failureIndications = new Class[]{Exception.class};

	public void setCircuitBreakerKey(String circuitBreakerKey) {
		this.circuitBreakerKey = circuitBreakerKey;
	}

	public void setFailureThreshold(int failureThreshold) {
		this.failureThreshold = failureThreshold;
	}

	public void setFailureThresholdTimeFrameMs(long failureThresholdTimeFrameMs) {
		this.failureThresholdTimeFrameMs = failureThresholdTimeFrameMs;
	}

	public void setRetryTimeoutMs(long retryTimeoutMs) {
		this.retryTimeoutMs = retryTimeoutMs;
	}

	public void setFailureIndications(Class<? extends Throwable>[] failureIndications) {
		this.failureIndications = failureIndications;
	}

	public String getCircuitBreakerKey() {
		return circuitBreakerKey;
	}

	public int getFailureThreshold() {
		return failureThreshold;
	}

	public long getFailureThresholdTimeFrameMs() {
		return failureThresholdTimeFrameMs;
	}

	public long getRetryTimeoutMs() {
		return retryTimeoutMs;
	}

	public Class<? extends Throwable>[] getFailureIndications() {
		return failureIndications;
	}
	
}

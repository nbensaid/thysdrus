package net.greencoding.thysdrus.circuitbreaker.core.model;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@gmail.com)
 * 
 */
public class CircuitBreakerRegisterResult {

	private boolean successfull = false;

	private String message;

	public CircuitBreakerRegisterResult(boolean successullyRegistered, String string) {
		this.successfull = successullyRegistered;
	}

	public boolean isSuccessfull() {
		return successfull;
	}

	public void setSuccessfull(boolean successfull) {
		this.successfull = successfull;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	@Override
	public String toString() {
		return "CircuitBreakerRegisterResult [successfull=" + successfull + ", message=" + message + "]";
	}

}

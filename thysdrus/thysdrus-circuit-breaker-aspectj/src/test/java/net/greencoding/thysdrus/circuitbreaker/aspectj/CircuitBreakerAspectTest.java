/*
 * Copyright 2010 Nabil Ben Said.
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

package net.greencoding.thysdrus.circuitbreaker.aspectj;

//import static org.junit.Assert.*;
import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerAspectTest {

	@Before
	public void setUp() {

	}

	@Test
	public void testSimpleCircuitBreakerBlocking() {

		Exception exception = null;
		try {
			callExternalResource(true);
		} catch (MyException ignore) {
			exception = ignore;
		}
		Assert.assertEquals(MyException.class, exception.getClass());
		exception = null;
		
		try {
			callExternalResource(true);
		} catch (MyException ignore) {
			exception = ignore;
		}
		Assert.assertEquals(MyException.class, exception.getClass());
		exception = null;

		// next call shall be blocked by CB.
		try {
			callExternalResource(true);
		} catch (MyException e) {
			Assert.fail("MyException not expected here");
		}
	}

	@Test
	public void testSimpleCircuitBreakerGroupBlocking() {

		Exception exception = null;
		try {
			callExternalResource1GroupA(true);
		} catch (Exception ignore) {
			exception = ignore;
		}
		Assert.assertEquals(MyException.class, exception.getClass());
		
		exception = null;

		try {
			callExternalResource2GroupA(true);
		} catch (Exception ignore) {
			exception = ignore;
		}
		Assert.assertEquals(MyException.class, exception.getClass());

		// next call shall be blocked by CB.
		try {
			callExternalResource1GroupA(true);
		} catch (MyException e) {
			Assert.fail("MyException not expected here");
		}
	}

	@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryTimeoutMs = 3000, failureIndications = { MyException.class }, isSilientMode = false)
	private void callExternalResource(boolean fail) {
		if (fail) {
			throw new MyException();
		}
	}

	@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryTimeoutMs = 3000, failureIndications = { MyException.class }, isSilientMode = false, groupId="A")
	private void callExternalResource1GroupA(boolean fail) {
		if (fail) {
			throw new MyException();
		}
	}
	
	@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryTimeoutMs = 3000, failureIndications = { MyException.class }, isSilientMode = false, groupId="A")
	private void callExternalResource2GroupA(boolean fail) {
		if (fail) {
			throw new MyException();
		}
	}

	private class MyException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

}

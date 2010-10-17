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

package net.greencoding.thysdrus.circuitbreaker.aspect;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerAspect;
import net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerMethodRegistry;
import net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerRegistryEntry;
import net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerStatus;
import net.greencoding.thysdrus.circuitbreaker.exception.OpenCircuitException;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerTest {

	final String methodName = "private void net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerTest.callExternalResource(boolean)";

	CircuitBreakerMethodRegistry registry;

	@Before
	public void setUp() {
		registry = new CircuitBreakerMethodRegistry();
		CircuitBreakerAspect.setRegistry(registry);
	}

	@Test(expected = OpenCircuitException.class)
	public void testOpenCircuitException() {
		try {
			callExternalResource(true);
		} catch (MyException ignore) {}
		try {
			callExternalResource(true);
		} catch (MyException ignore) {}
		// next call shall be fail with OpenCircuitException
		callExternalResource(true);
	}

	@Test
	public void testCircuitBreakerReadAnnotationParameter() {
		callExternalResource(false);
		CircuitBreakerRegistryEntry entry = registry.getEntry(methodName);
		assertEquals(methodName, entry.getName());
		assertTrue(entry.getFailureIndications().contains(MyException.class));
		assertEquals(2, entry.getFailureThreshold());
		assertEquals(2000, entry.getFailureThresholdTimeFrameMs());
		assertEquals(3000, entry.getRetryAfterMs());
		assertEquals(CircuitBreakerStatus.CLOSED, entry.getStatus());
		assertEquals(0, entry.getFailures().size());
		assertEquals(0, entry.getLastOpenedTime());
		assertEquals(0, entry.getClosedCycleCounter());
	}

	@Test
	public void testCircuitBreakerSingleThread() throws InterruptedException {
		externalResource(true);
		Thread.sleep(1000);
		externalResource(true);
		// next call must be blocked
		externalResource(false);
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodName).getStatus());
		assertEquals(0, registry.getEntry(methodName).getFailures().size());
		long openedTime = registry.getEntry(methodName).getLastOpenedTime();
		Thread.sleep(3000);
		// half open call
		externalResource(true);
		// status back to OPEN
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodName).getStatus());
		// lastOpenedTime updated?
		assertFalse(openedTime == registry.getEntry(methodName).getLastOpenedTime());
		Thread.sleep(3000);
		externalResource(false);
		assertEquals(CircuitBreakerStatus.CLOSED, registry.getEntry(methodName).getStatus());
		assertEquals(1, registry.getEntry(methodName).getClosedCycleCounter());
	}

	@Test
	public void testCircuitBreakerMultiThreaded() throws InterruptedException {
		String methodSignature = "private void net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerTest.MyThread.externalCall(boolean, long)";
		MyThread thread1 = new MyThread(true, 1000l);
		MyThread thread2 = new MyThread(true, 2000l);
		MyThread thread3 = new MyThread(true, 4000l);
		// MyThread thread4 = new MyThread(false, 1000l);

		thread1.start();
		thread2.start();
		thread3.start();
		Thread.sleep(4500);
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodSignature).getStatus());
		// thread4.start();
	}

	private void externalResource(boolean fail) {
		try {
			callExternalResource(fail);
		} catch (MyException ignore) {
			// ignore
		} catch (OpenCircuitException ignore) {
			// ignore
		}
	}

	@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryAfterMs = 3000, failureIndications = { MyException.class })
	private void callExternalResource(boolean fail) {
		if (fail) {
			throw new MyException();
		}
	}

	private class MyException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private class MyThread extends Thread {

		private boolean fail = false;

		private long exectionDuration = 0l;

		public MyThread(boolean fail, long executionDuration) {
			this.fail = fail;
			this.exectionDuration = executionDuration;
		}

		@Override
		public void run() {
			super.run();
			try {
				externalCall(fail, exectionDuration);
			} catch (MyException ignore) {
				// ignore
			} catch (OpenCircuitException ignore) {
				// ignore
			}
		}

		@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryAfterMs = 3000, failureIndications = { MyException.class })
		private void externalCall(boolean fail, long executionDuration) {
			try {
				Thread.sleep(executionDuration);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (fail) {
				throw new MyException();
			}
		}
	}

	@Test
	public void testObjectReturnValueOfCalledMethod() {
		try {
			externalCallWithReturnObject(true);
		} catch (MyException e) {}

		assertEquals(null, externalCallWithReturnObject(true));
		
	}

	@MonitoredByCircuitBreaker(failureThreshold = 1)
	private Object externalCallWithReturnObject(boolean fail) {
		if (fail) {
			throw new MyException();
		}
		return new Object();
	}
}

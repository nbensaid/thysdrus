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

	final String methodName = "private void net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerTest.callExternalResourceNonSilient(boolean)";

	CircuitBreakerMethodRegistry registry;

	@Before
	public void setUp() {
		registry = new CircuitBreakerMethodRegistry();
		CircuitBreakerAspect.setRegistry(registry);
	}

	@Test(expected = OpenCircuitException.class)
	public void testOpenCircuitException() {
		try {
			callExternalResourceNonSilient(true);
		} catch (MyException ignore) {}
		try {
			callExternalResourceNonSilient(true);
		} catch (MyException ignore) {}
		// next call shall be fail with OpenCircuitException
		callExternalResourceNonSilient(true);
	}

	@Test
	public void testCircuitBreakerReadAnnotationParameter() {
		callExternalResourceNonSilient(false);
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
		externalResourceNonSilient(true);
		Thread.sleep(1000);
		externalResourceNonSilient(true);
		// next call must be blocked
		externalResourceNonSilient(false);
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodName).getStatus());
		assertEquals(0, registry.getEntry(methodName).getFailures().size());
		long openedTime = registry.getEntry(methodName).getLastOpenedTime();
		Thread.sleep(3000);
		// half open call
		externalResourceNonSilient(true);
		// status back to OPEN
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodName).getStatus());
		// lastOpenedTime updated?
		assertFalse(openedTime == registry.getEntry(methodName).getLastOpenedTime());
		Thread.sleep(3000);
		externalResourceNonSilient(false);
		assertEquals(CircuitBreakerStatus.CLOSED, registry.getEntry(methodName).getStatus());
		assertEquals(1, registry.getEntry(methodName).getClosedCycleCounter());
	}

	@Test
	public void testCircuitBreakerMultiThreadedNonSilient() throws InterruptedException {
		String methodSignatureNonSilient = "private void net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerTest.MyRunnable.externalCallNonSilient(boolean, long)";
		boolean isSilientMode = false;
		Thread thread1 = new Thread(new MyRunnable(true, 1000l, isSilientMode));
		Thread thread2 = new Thread(new MyRunnable(true, 2000l, isSilientMode));
		Thread thread3 = new Thread(new MyRunnable(true, 4000l, isSilientMode));
		// MyThread thread4 = new MyThread(false, 1000l);

		thread1.start();
		thread2.start();
		thread3.start();
		Thread.sleep(4500);
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodSignatureNonSilient).getStatus());
		// thread4.start();
	}
	
	@Test
	public void testCircuitBreakerMultiThreadedSilient() throws InterruptedException {
		String methodSignatureNonSilient = "private void net.greencoding.thysdrus.circuitbreaker.aspect.CircuitBreakerTest.MyRunnable.externalCallSilient(boolean, long)";
		boolean isSilientMode = true;
		Thread thread1 = new Thread(new MyRunnable(true, 1000l, isSilientMode));
		Thread thread2 = new Thread(new MyRunnable(true, 2000l, isSilientMode));
		Thread thread3 = new Thread(new MyRunnable(true, 4000l, isSilientMode));
		// MyThread thread4 = new MyThread(false, 1000l);
		
		thread1.start();
		thread2.start();
		thread3.start();
		Thread.sleep(4500);
		assertEquals(CircuitBreakerStatus.OPEN, registry.getEntry(methodSignatureNonSilient).getStatus());
		// thread4.start();
	}

	private void externalResourceNonSilient(boolean fail) {
		try {
			callExternalResourceNonSilient(fail);
		} catch (MyException ignore) {
			// ignore
		} catch (OpenCircuitException ignore) {
			// ignore
		}
	}

	@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryAfterMs = 3000, failureIndications = { MyException.class }, isSilientMode = false)
	private void callExternalResourceNonSilient(boolean fail) {
		if (fail) {
			throw new MyException();
		}
	}

	private class MyException extends RuntimeException {
		private static final long serialVersionUID = 1L;
	}

	private class MyRunnable implements Runnable {

		private boolean fail = false;

		private long executionDuration = 0l;

		private boolean silient = true;

		public MyRunnable(boolean fail, long executionDuration, boolean silient) {
			this.fail = fail;
			this.executionDuration = executionDuration;
			this.silient = silient;
		}
		
		public void run(){
			if (silient) {
				externalCallSilient(fail, executionDuration);
			} else {
				try {
					externalCallNonSilient(fail, executionDuration);
				} catch (MyException ignore) {
					// ignore
				} catch (OpenCircuitException ignore) {
					// ignore
				}
			}
		}

		@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryAfterMs = 3000, failureIndications = { MyException.class }, isSilientMode = false)
		private void externalCallNonSilient(boolean fail, long executionDuration) {
			try {
				Thread.sleep(executionDuration);
			} catch (InterruptedException e) {
				throw new RuntimeException(e);
			}
			if (fail) {
				throw new MyException();
			}
		}

		@MonitoredByCircuitBreaker(failureThreshold = 2, failureThresholdTimeFrameMs = 2000, retryAfterMs = 3000, failureIndications = { MyException.class }, isSilientMode = true)
		private void externalCallSilient(boolean fail, long executionDuration) {
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

	@MonitoredByCircuitBreaker(failureThreshold = 1, isSilientMode = true)
	private Object externalCallWithReturnObject(boolean fail) {
		if (fail) {
			throw new MyException();
		}
		return null;
	}
}

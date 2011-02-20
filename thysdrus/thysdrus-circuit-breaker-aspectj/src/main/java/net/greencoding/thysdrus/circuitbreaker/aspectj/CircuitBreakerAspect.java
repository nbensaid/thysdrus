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

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.exception.CircuitBreakerMethodExecutionException;
import net.greencoding.thysdrus.circuitbreaker.exception.OpenCircuitException;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
@Aspect
public class CircuitBreakerAspect {
	// TODO implement statistics

	private static final Logger logger = LoggerFactory.getLogger(CircuitBreakerAspect.class);

	private static CircuitBreakerMethodRegistry registry = new CircuitBreakerMethodRegistry();

	@Around("execution(@net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker * * (..))")
	public Object breakCircuit(final ProceedingJoinPoint pjp) throws Throwable {
		Object returnObject = null;
		final String method = pjp.getSignature().toLongString();
		CircuitBreakerStatus status = null;
		try {
			final MethodSignature sig = (MethodSignature) pjp.getStaticPart().getSignature();
			final MonitoredByCircuitBreaker cbAnnotation = sig.getMethod().getAnnotation(MonitoredByCircuitBreaker.class);
			registry.registerMehtodIfnecessary(method, cbAnnotation);
			status = registry.getStatusWithHalfOpenExclusiveLockTry(method);
			if (status.equals(CircuitBreakerStatus.OPEN)) {
				logger.info("CIRCUIT STATUS: OPEN. Method {} can not be executed. try later!", method);
				if (cbAnnotation.isSilientMode()) {
					return null;
				} else {
					throw new OpenCircuitException();
				}
			} else if (status.equals(CircuitBreakerStatus.HALF_OPEN)) {
				logger.info("CIRCUIT STATUS: HALF_OPEN. Another thread has the exclusive lock for half open. Method {} can not be executed.", method);
				if (cbAnnotation.isSilientMode()) {
					return null;
				} else {
					throw new OpenCircuitException();
				}
			} else if (status.equals(CircuitBreakerStatus.CLOSED)) {
				logger.info("CIRCUIT STATUS: CLOSED. execute method {}", method);
				returnObject = proceed(pjp);
			} else if (status.equals(CircuitBreakerStatus.HALF_OPEN_EXCLUSIVE)) {
				logger.info("CIRCUIT STATUS: HALF_OPEN_EXCLUSIVE. This thread win the exclusive lock for the half open call. execute method: {}", method);
				returnObject = proceed(pjp);
				logger.info("CIRCUIT STATUS: HALF_OPEN_EXCLUSIVE. method execution was successfull. now close circuit for method {}", method);
				registry.closeAndUnlock(method);
			}

		} catch (CircuitBreakerMethodExecutionException e) {
			Throwable throwable = e.getCause();
			for (Class<? extends Throwable> clazz : registry.getfailureIndications(method)){
				if (clazz.isAssignableFrom(throwable.getClass())) {
					// detected a failure 
					logger.info("dectected failure. failure indication: {} \nException:", clazz.getCanonicalName(), throwable);
					if (status.equals(CircuitBreakerStatus.CLOSED) && registry.sameClosedCycleInLocalAndGlobaleContext(method)) {
						logger.info("Valid failure: method call and failure are in the same CLOSED cycle.");
						registry.addFailureAndOpenCircuitIfThresholdAchived(method);
					} else if (status.equals(CircuitBreakerStatus.HALF_OPEN_EXCLUSIVE)) {
						registry.keepOpenAndUnlock(method);
					} 
					throw throwable;
				}
			}
			// thrown exception is not a failureIndication
			if (status.equals(CircuitBreakerStatus.HALF_OPEN_EXCLUSIVE)) {
				logger.info("CIRCUIT STATUS: HALF_OPEN_EXCLUSIVE. method execution was successfull. now close circuit for method {}", method);
				registry.closeAndUnlock(method);
			}
			// throw the original method execution exception upper to the method invoker
			throw throwable;
		} finally {
			registry.cleanUp(method);
		}
		return returnObject;
	}

	private Object proceed(ProceedingJoinPoint pjp) throws CircuitBreakerMethodExecutionException {
		try {
			return pjp.proceed();
		} catch (Throwable t) {
			logger.debug("Exception while method execution: {}", pjp.getSignature().toLongString());
			throw new CircuitBreakerMethodExecutionException(t);
		}
	}

	protected static void setRegistry(CircuitBreakerMethodRegistry registry) {
		CircuitBreakerAspect.registry = registry;
	}
	
}

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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.core.handler.CircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.DefaultCircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.MethodInvocationResult;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;

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

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	//private final static String ANNOTATION_SIGNATURE =  MonitoredByCircuitBreaker.class.getName();
	private final static String ANNOTATION_SIGNATURE =  "net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker";
	private final static String POINTCUT = "execution(@"+ ANNOTATION_SIGNATURE + " * * (..))"; 
	
	private CircuitBreakerAnnotationDiscoverer annotationDiscoverer = new CircuitBreakerAnnotationDiscoverer();

	public CircuitBreakerAspect() {
		// TODO discoverer doesn't work currently. It just logs the methods
		// annotated with MonitoredByCircuitBreaker.
		// TODO think about reflection and OSGi support.
		annotationDiscoverer.discover();
	}

	private CircuitBreakerHandler cbHandler = DefaultCircuitBreakerHandler.getSingleton() ;

	@Around(POINTCUT)
	public Object breakCircuit(final ProceedingJoinPoint pjp) throws Throwable {
		
		//TODO in further step the extraction of the CircuitBreaker object shall be made in the CBAnnotationDiscoverer.
		// and at this point on the circuitBreakerKey shall be extracted from the ProceedingJoinPoint.
		// The CB Handler shall be improved to accept also the pjp + circuitBreakerKey instead of the whole 
		// circuitBreaker object.
		CircuitBreaker circuitBreaker = extractCircuitBreakerFromProceedingJoinPoint(pjp);
		logger.info("CB: {}", circuitBreaker);
		MethodInvocationResult methodInvocationResult = cbHandler.handleMethodInvocation(pjp, circuitBreaker);
		if (methodInvocationResult.getReturnObject() != null) {
			return methodInvocationResult.getReturnObject();
		} else {
			return null;
		}
	}

	private CircuitBreaker extractCircuitBreakerFromProceedingJoinPoint(final ProceedingJoinPoint pjp) {
		CircuitBreaker ciruitBreaker = new CircuitBreaker();
		String circuitBreakerKey = null;
		final MethodSignature sig = (MethodSignature) pjp.getStaticPart().getSignature();
		final MonitoredByCircuitBreaker cbAnnotation = sig.getMethod().getAnnotation(MonitoredByCircuitBreaker.class);

		String circuitBreakerGroup = cbAnnotation.groupId().isEmpty() ? null : cbAnnotation.groupId();
		if (circuitBreakerGroup != null) {
			circuitBreakerKey = circuitBreakerGroup;
			ciruitBreaker.setCircuitBreakerGroup(circuitBreakerGroup);
		} else {
			circuitBreakerKey = sig.toLongString();
		}

		ciruitBreaker.setCircuitBreakerKey(circuitBreakerKey);
		ciruitBreaker.setFailureIndications(cbAnnotation.failureIndications());
		ciruitBreaker.setFailureThreshold(cbAnnotation.failureThreshold());
		ciruitBreaker.setFailureThresholdTimeFrameMs(cbAnnotation.failureThresholdTimeFrameMs());
		ciruitBreaker.setRetryTimeoutMs(cbAnnotation.retryTimeoutMs());
		List<Method> registeredMethods = new ArrayList<Method>();
		registeredMethods.add(sig.getMethod());
		ciruitBreaker.setRegisteredMethods(registeredMethods);
		
		return ciruitBreaker;
	}

}

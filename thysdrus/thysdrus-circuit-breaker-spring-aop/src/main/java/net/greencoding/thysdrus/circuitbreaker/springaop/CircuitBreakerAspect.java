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

package net.greencoding.thysdrus.circuitbreaker.springaop;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreakerBean;
import net.greencoding.thysdrus.circuitbreaker.core.handler.CircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.DefaultCircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.MethodInvocationResult;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
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
	private final static String ANNOTATION_SIGNATURE =  "net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreakerBean";
	private final static String POINTCUT = "execution(@"+ ANNOTATION_SIGNATURE + " * * (..)) && @annotation(annotation)"; 
	
	
	private CircuitBreakerHandler cbHandler = DefaultCircuitBreakerHandler.getSingleton() ;


	@Around(POINTCUT)
//	@Around("execution(@net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreakerBean * * (..)) &&" +
//			" @annotation(annotation)")
	public Object breakCircuit(final ProceedingJoinPoint pjp, MonitoredByCircuitBreakerBean annotation) throws Throwable {
		logger.info("entered Around aspect");
		String circuitBreakerKey = annotation.value();
		logger.info("CB: {}", circuitBreakerKey);
		MethodInvocationResult methodInvocationResult = cbHandler.handleMethodInvocation(pjp, circuitBreakerKey);
		if (methodInvocationResult.getReturnObject() != null) {
			return methodInvocationResult.getReturnObject();
		} else {
			return null;
		}
	}

}

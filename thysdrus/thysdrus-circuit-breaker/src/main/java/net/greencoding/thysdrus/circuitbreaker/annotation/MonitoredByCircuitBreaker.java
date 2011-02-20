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

package net.greencoding.thysdrus.circuitbreaker.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
@Target(value=ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface MonitoredByCircuitBreaker {

	// open circuit breaker if failure threshold reached 
	int failureThreshold() default 5;
	// open ciruict breaker if failure threshold reached in 60sec
	long failureThresholdTimeFrameMs() default 60000l;
	
	// transition to halfopen
	long retryAfterMs() default 10000l;
	
	// Exception which indicates failures 
	Class<? extends Throwable> [] failureIndications() default {Exception.class}; 
	
	boolean isSilientMode() default true;
	
	// TODO exclude exception as failure
}

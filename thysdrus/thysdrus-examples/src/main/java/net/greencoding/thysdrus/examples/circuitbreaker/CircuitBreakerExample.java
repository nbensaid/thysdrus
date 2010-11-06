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

package net.greencoding.thysdrus.examples.circuitbreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerExample {

	private static Logger logger = LoggerFactory.getLogger(CircuitBreakerExample.class);

	public static void main(String[] args) throws InterruptedException {
		while (true) {
			Thread.sleep(50l);
			try {
				externalCall();
			} catch (Exception e) {
				logger.info("exception: ", e);
			}
		}
	}

	@MonitoredByCircuitBreaker(failureIndications = { Exception.class }, failureThreshold = 2, isSilientMode = true, failureThresholdTimeFrameMs = 200, retryAfterMs = 1000)
	public static void externalCall() {
		// request some resource from another subsystem
		subsystemCall();
	}

	public static void subsystemCall() {
		throw new RuntimeException("something went wrong in subsystem");
	}

}

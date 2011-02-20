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

package net.greencoding.thysdrus.circuitbreaker.core.handler.exception;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@net-m.de)
 *
 */
public class CircuitBreakerMethodExecutionException extends Exception {

	private static final long serialVersionUID = 469923151407502871L;

	public CircuitBreakerMethodExecutionException() {
		super();
	}

	public CircuitBreakerMethodExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

	public CircuitBreakerMethodExecutionException(String message) {
		super(message);
	}

	public CircuitBreakerMethodExecutionException(Throwable cause) {
		super(cause);
	}
}

package net.greencoding.thysdrus.circuitbreaker.aspectj;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;

import org.junit.Test;


public class CircuitBreakerAnnotationDiscovereTest {

	@Test
	public void testDiscover(){
		CircuitBreakerAnnotationDiscoverer classUnderTest = new CircuitBreakerAnnotationDiscoverer();
		classUnderTest.discover();
		// TODO improve the Discoverer to distinguish between overloaded methods.
	}
	
	@SuppressWarnings("unused")
	@MonitoredByCircuitBreaker
	private void testClass(){
		
	}
	
	@MonitoredByCircuitBreaker
	@SuppressWarnings("unused")
	private boolean  testClass(int a){
		return true;
	}
}

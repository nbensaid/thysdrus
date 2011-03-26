package net.greencoding.thysdrus.circuitbreaker.springaop;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreakerBean;
import net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistry;
import net.greencoding.thysdrus.circuitbreaker.core.CircuitBreakerRegistryFactory;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreakerStatus;

import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"file:src/test/resources/test-context.xml"})
public class AspectJCBTest {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CircuitBreakerRegistry cbr = CircuitBreakerRegistryFactory.getSingelton();
	
	/**
	 * this test pass only with a valid aspectj-maven-plugin configuration in the pom.xml
	 * 
	 * @throws Exception
	 */
	@Test
	public void test() throws Exception{
		Assert.assertEquals(CircuitBreakerStatus.CLOSED, cbr.getCircuitBreakerStatus("testCircuitBreaker"));
		try {
			monitoredMethod(true);
		} catch(Exception e) {}
		try {
			monitoredMethod(true);
		} catch(Exception e) {}
		try {
			monitoredMethod(true);
		} catch(Exception e) {}
		Assert.assertEquals(CircuitBreakerStatus.OPEN, cbr.getCircuitBreakerStatus("testCircuitBreaker"));
	}
	
	
	@MonitoredByCircuitBreakerBean("testCircuitBreaker")
	private void monitoredMethod(boolean fail) throws Exception{
		logger.info("monitoredMethod");
		if (fail) {
			throw new Exception();
		}
	}
}


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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "file:src/test/resources/test-context.xml" })
public class AopAutoproxyTest {

	@Autowired
	private ApplicationContext context;

	private CircuitBreakerRegistry cbr = CircuitBreakerRegistryFactory.getSingelton();

	@Test
	public void testAutoproxy() {
		BeanInterface abean = (BeanInterface) context.getBean("fooBean");
		Assert.assertEquals(CircuitBreakerStatus.CLOSED, cbr.getCircuitBreakerStatus("testCircuitBreaker2"));
		try {
			abean.aMethod(true);
		} catch (Exception ignore) {}
		try {
			abean.aMethod(true);
		} catch (Exception ignore) {}
		Assert.assertEquals(CircuitBreakerStatus.OPEN, cbr.getCircuitBreakerStatus("testCircuitBreaker2"));
	}
}
interface BeanInterface {

	// CB annotations on interface are ignored
	public void aMethod(boolean fail) throws Exception;
}
class FooBean implements BeanInterface {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@MonitoredByCircuitBreakerBean("testCircuitBreaker2")
	public void aMethod(boolean fail) throws Exception{
		logger.info("aMethod is executed.");
		if (fail) {
			throw new Exception();
		}
	}
}
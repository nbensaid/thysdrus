package net.greencoding.thysdrus.circuitbreaker.aspectj;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;
import net.greencoding.thysdrus.circuitbreaker.core.handler.CircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.DefaultCircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerAnnotationDiscoverer implements AnnotationDiscoverer {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	private CircuitBreakerHandler cbHandler = DefaultCircuitBreakerHandler.getSingleton();
	
	private Map<String, CircuitBreaker> circuitBreakerToRegister = new HashMap<String, CircuitBreaker>();

	@Override
	public void discover() {
		Reflections reflections = new Reflections("",new MethodAnnotationsScanner());
        
		Set<Method> methods = reflections.getMethodsAnnotatedWith(MonitoredByCircuitBreaker.class);
		logger.info("methods: " + methods);
		if (methods == null) {
			return;
		}
		
		Iterator<Method> iterator = methods.iterator();
		while (iterator.hasNext()){
			Method method = iterator.next();
			checkCircuitBreakerAnnotation(method);
		}
		
		Iterator<String> keyIterator = circuitBreakerToRegister.keySet().iterator();
		while(keyIterator.hasNext()) {
			cbHandler.registerCircuitBreaker(circuitBreakerToRegister.get(keyIterator.next()));
		}
		
		circuitBreakerToRegister.clear();
	}
	
	private void checkCircuitBreakerAnnotation(final Method method) {
		CircuitBreaker ciruitBreaker = new CircuitBreaker();
		String circuitBreakerKey = null;
		MonitoredByCircuitBreaker annotation = method.getAnnotation(MonitoredByCircuitBreaker.class);

		String circuitBreakerGroup = annotation.groupId().isEmpty() ? null : annotation.groupId();
		if (circuitBreakerGroup != null) {
			circuitBreakerKey = circuitBreakerGroup;
			ciruitBreaker.setCircuitBreakerGroup(circuitBreakerGroup);
		} else {
			circuitBreakerKey = method.toString();
		}
		ciruitBreaker.setCircuitBreakerKey(circuitBreakerKey);
		List<Method> registeredMethods = null;

		CircuitBreaker groupCircuitBreaker = circuitBreakerToRegister.get(circuitBreakerKey);
		if (groupCircuitBreaker != null) {
			checkGroupCircuitBreaker(groupCircuitBreaker, method.toString(), annotation);
			ciruitBreaker = groupCircuitBreaker;
			registeredMethods = ciruitBreaker.getRegisteredMethods();
		} else {
			 registeredMethods = new ArrayList<Method>();
		}
		
		ciruitBreaker.setFailureIndications(annotation.failureIndications());
		ciruitBreaker.setFailureThreshold(annotation.failureThreshold());
		ciruitBreaker.setFailureThresholdTimeFrameMs(annotation.failureThresholdTimeFrameMs());
		ciruitBreaker.setRetryTimeoutMs(annotation.retryTimeoutMs());
		registeredMethods.add(method);
		ciruitBreaker.setRegisteredMethods(registeredMethods);
		
		circuitBreakerToRegister.put(circuitBreakerKey, ciruitBreaker);
	}

	private void checkGroupCircuitBreaker(CircuitBreaker groupCircuitBreaker, String methodSignature, MonitoredByCircuitBreaker annotation) {
		if (!Arrays.equals(groupCircuitBreaker.getFailureIndications(), annotation.failureIndications()) ||  
				groupCircuitBreaker.getFailureThreshold() != annotation.failureThreshold() ||
				groupCircuitBreaker.getFailureThresholdTimeFrameMs() != annotation.failureThresholdTimeFrameMs()||
				groupCircuitBreaker.getRetryTimeoutMs() != annotation.retryTimeoutMs()) {
		
			logger.error("two circuit breaker with the same groupId {} have different parameters method1:{}  method2:{}", new Object[]{groupCircuitBreaker.getCircuitBreakerGroup(), groupCircuitBreaker.getRegisteredMethods().get(0).toString(), methodSignature});
			throw new IllegalStateException("two circuit breaker with the same groupId " + groupCircuitBreaker.getCircuitBreakerGroup() +" have different parameters CB1:" + groupCircuitBreaker.getRegisteredMethods().get(0).toString() +" CB2:{}" + methodSignature);
		}
	}


	
}

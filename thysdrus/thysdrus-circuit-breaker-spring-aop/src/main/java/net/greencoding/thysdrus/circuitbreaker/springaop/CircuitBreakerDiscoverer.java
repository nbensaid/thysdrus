package net.greencoding.thysdrus.circuitbreaker.springaop;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreakerBean;
import net.greencoding.thysdrus.circuitbreaker.core.handler.CircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.handler.DefaultCircuitBreakerHandler;
import net.greencoding.thysdrus.circuitbreaker.core.model.CircuitBreaker;

import org.reflections.Reflections;
import org.reflections.scanners.MethodAnnotationsScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;

public class CircuitBreakerDiscoverer implements InitializingBean {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	ApplicationContext context;
	
	private CircuitBreakerHandler cbHandler = DefaultCircuitBreakerHandler.getSingleton();

	//@Override
	public void afterPropertiesSet() throws Exception {
		String beanNames [] = context.getBeanNamesForType(CircuitBreakerDefinition.class);
		if (beanNames != null && beanNames.length > 0){
			
			Reflections reflections = new Reflections("",new MethodAnnotationsScanner());
	        
			Set<Method> methods = reflections.getMethodsAnnotatedWith(MonitoredByCircuitBreakerBean.class);
			logger.info("methods: " + methods);
			if (methods == null) {
				return;
			}
			// scan the class path for annotation store them in a map <beanId, method>
			Map<String, List<Method>> annotatedMethods = new HashMap<String, List<Method>>();
			
			Iterator<Method> iterator = methods.iterator();
			while (iterator.hasNext()){
				Method method = iterator.next();
				MonitoredByCircuitBreakerBean annotation = method.getAnnotation(MonitoredByCircuitBreakerBean.class);
				if (annotation != null){
					List<Method> methodList = annotatedMethods.get(annotation.value());
					if (methodList == null) {
						methodList = new ArrayList<Method>();
					}
					methodList.add(method);
					annotatedMethods.put(annotation.value(), methodList);
				}
			}
			
			for (String beanName : beanNames) {
				CircuitBreakerDefinition cbDefinition = (CircuitBreakerDefinition) context.getBean(beanName);
				CircuitBreaker circuitBreaker = new CircuitBreaker();
				String key = cbDefinition.getCircuitBreakerKey() == null ? beanName : cbDefinition.getCircuitBreakerKey() ;
				circuitBreaker.setCircuitBreakerKey(key);
				circuitBreaker.setFailureIndications(cbDefinition.getFailureIndications());
				circuitBreaker.setFailureThreshold(cbDefinition.getFailureThreshold());
				circuitBreaker.setFailureThresholdTimeFrameMs(cbDefinition.getFailureThresholdTimeFrameMs());
				circuitBreaker.setRetryTimeoutMs(cbDefinition.getRetryTimeoutMs());
				circuitBreaker.setRegisteredMethods(annotatedMethods.get(beanName));
				cbHandler.registerCircuitBreaker(circuitBreaker);
			}
		}
	}

}

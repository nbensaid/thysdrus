package net.greencoding.thysdrus.circuitbreaker.aspectj;

import net.greencoding.thysdrus.circuitbreaker.annotation.MonitoredByCircuitBreaker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.impetus.annovention.ClasspathDiscoverer;
import com.impetus.annovention.Discoverer;
import com.impetus.annovention.listener.MethodAnnotationDiscoveryListener;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 * 
 */
public class CircuitBreakerAnnotationDiscoverer {

	private Discoverer discoverer = new ClasspathDiscoverer();

	public CircuitBreakerAnnotationDiscoverer() {
		discoverer.addAnnotationListener(new CircuitBreakerAnnotationDiscoveryListener());
	}

	public void discover() {
		discoverer.discover();
	}

	static class CircuitBreakerAnnotationDiscoveryListener implements MethodAnnotationDiscoveryListener {
		private static Logger log = LoggerFactory.getLogger(CircuitBreakerAnnotationDiscoverer.class);

		//@Override
		public void discovered(String clazz, String method, String annotation) {
			log.info("Discovered Method(" + clazz + "." + method + ") with Annotation(" + annotation + ")");
			// TODO register the method in the CircuitBreakerRegistry
			
		}

		//@Override
		public String[] supportedAnnotations() {
			return new String[] {MonitoredByCircuitBreaker.class.getName() };
		}
	}
}

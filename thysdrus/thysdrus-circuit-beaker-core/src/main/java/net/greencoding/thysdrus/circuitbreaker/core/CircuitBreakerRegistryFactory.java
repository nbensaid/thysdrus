package net.greencoding.thysdrus.circuitbreaker.core;

import java.util.HashMap;
import java.util.Map;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@net-m.de)
 * 
 */
public class CircuitBreakerRegistryFactory {

	private static String defaultCBRegistryName = "defaultCircuitBreakerRegistry";

	private static Map<String, CircuitBreakerRegistry> registries = new HashMap<String, CircuitBreakerRegistry>();

	/**
	 * returns the default CB Registry.
	 * 
	 * @return
	 */
	public static CircuitBreakerRegistry getSingelton() {
		CircuitBreakerRegistry registry = registries.get(defaultCBRegistryName);
		if (registry == null) {
			registry = new DefaultCircuitBreakerRegistry(defaultCBRegistryName);
			registries.put(defaultCBRegistryName, registry);
		}
		return registry;
	}

	/**
	 * returns a CB Registry with the given name. If no registy exists with the
	 * given name, a new one will be created.
	 * 
	 * @param name
	 * @return
	 */
	public static CircuitBreakerRegistry getInstance(String name) {
		CircuitBreakerRegistry registry = registries.get(name);
		if (registry == null) {
			registry = new DefaultCircuitBreakerRegistry(name);
			registries.put(name, registry);
		}
		return registry;
	}
}

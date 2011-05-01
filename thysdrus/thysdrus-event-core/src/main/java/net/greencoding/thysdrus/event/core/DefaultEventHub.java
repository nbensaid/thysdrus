package net.greencoding.thysdrus.event.core;

import java.util.List;

import net.greencoding.thysdrus.event.core.listener.EventListener;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
public class DefaultEventHub implements EventHub {

	private EventListenerRegistry listenerRegistry = DefaultEventListenerRegistry.getSingelton();
	
	private static EventHub singelton = new DefaultEventHub();

	public static EventHub getSingelton() {
		return singelton;
	}
	
	protected DefaultEventHub(){
	}
		
	@Override
	public void publishEvent(Event event) {
		// TODO improve this method to run asynchronous. use a queue and threads.
		List<EventListener<? extends Event>> listeners = listenerRegistry.getEventListernersByEventType(event.getClass());
		if (listeners == null){
			return;
		}

		for (EventListener<? extends Event> eventListener : listeners){
			eventListener.newEvent(event);
		}
	}

	protected void setListenerRegistry(EventListenerRegistry listenerRegistry) {
		this.listenerRegistry = listenerRegistry;
	}

}

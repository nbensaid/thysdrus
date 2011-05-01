package net.greencoding.thysdrus.event.core.listener;


import net.greencoding.thysdrus.event.core.Event;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
public interface EventListener<E extends Event> {

	public void newEvent(Event event);
	
	public Class<E> getEventType();
}

package net.greencoding.thysdrus.event.core;

import java.util.List;

import net.greencoding.thysdrus.event.core.listener.EventListener;

/**
 * 
 * @author Nabil Ben Said (nabil.ben.said@net-m.de)
 *
 */
public interface EventListenerRegistry {

	public void addListener(EventListener<? extends Event> listener);
	
	public void removeListener(EventListener<? extends Event> listener);

	public List<EventListener<? extends Event>> getEventListernersByEventType(Class<? extends Event> type);
}

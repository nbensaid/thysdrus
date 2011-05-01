package net.greencoding.thysdrus.event.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.greencoding.thysdrus.event.core.listener.EventListener;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
public class DefaultEventListenerRegistry implements EventListenerRegistry {

	private Map<Class<? extends Event>, List<EventListener<? extends Event>>> listenerMap = new HashMap<Class<? extends Event>, List<EventListener<? extends Event>>>();

	private static EventListenerRegistry singelton = new DefaultEventListenerRegistry();
	
	private DefaultEventListenerRegistry(){
	}
	
	public static EventListenerRegistry getSingelton(){
		return singelton; 
	}
	
	@Override
	public void addListener(EventListener<? extends Event> listener) {
		if (listener == null){
			return;
		}
		List<EventListener<? extends Event>> listenerList = listenerMap.get(listener.getEventType());
		if (listenerList == null) {
			listenerList = new ArrayList<EventListener<? extends Event>>();
			listenerMap.put(listener.getEventType(), listenerList);
		}
		if (! listenerList.contains(listener)) {
			listenerList.add(listener);
		}
	}

	@Override
	public void removeListener(EventListener<? extends Event> listener) {
		if (listener== null){
			return;
		}
		List<EventListener<? extends Event>> listenerList = listenerMap.get(listener.getEventType());
		if (listenerList != null && listenerList.contains(listener)) {
			listenerList.remove(listener);
		}
	}

	@Override
	public List<EventListener<? extends Event>> getEventListernersByEventType(Class<? extends Event> type) {
		return listenerMap.get(type);
	}

	
}

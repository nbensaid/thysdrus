package net.greencoding.thysdrus.event.core.listener;

import java.lang.reflect.ParameterizedType;

import net.greencoding.thysdrus.event.core.Event;

public abstract class BaseEventListener<E extends Event> implements EventListener<E> {

	@SuppressWarnings("unchecked")
	@Override
	public Class<E> getEventType() {
		return (Class<E>)((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
	}

}

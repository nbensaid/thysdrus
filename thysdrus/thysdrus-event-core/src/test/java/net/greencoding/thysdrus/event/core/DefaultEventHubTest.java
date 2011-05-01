package net.greencoding.thysdrus.event.core;

import junit.framework.Assert;
import net.greencoding.thysdrus.event.core.listener.BaseEventListener;

import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Nabil Ben Said (nabil.bensaid@gmail.com)
 *
 */
public class DefaultEventHubTest {

	private EventListenerRegistry listenerRegistry = DefaultEventListenerRegistry.getSingelton();
	
	private DefaultEventHub classUnderTest = new DefaultEventHub();
	
	@Before
	public void setup(){
		classUnderTest.setListenerRegistry(listenerRegistry);
	}
		
	@Test
	public void publishEvent() {
		// given 
		Event event = new MyEvent();
		MyEventListener myEventListener = new MyEventListener();
		MyEventListener myEventListener2 = new MyEventListener();

		listenerRegistry.addListener(myEventListener);
		listenerRegistry.addListener(myEventListener2);
		
		// when 
		classUnderTest.publishEvent(event);
		
		// then
		Assert.assertEquals(1, myEventListener.receivedEvents);
		Assert.assertEquals(1, myEventListener2.receivedEvents);
	}
	
	class MyEvent implements Event {
		
	}
	
	class MyEventListener extends BaseEventListener<MyEvent> {
		private int receivedEvents = 0;
		
		@Override
		public void newEvent(Event event) {
			receivedEvents++;
		}
	}
}

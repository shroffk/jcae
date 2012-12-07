/**
 * 
 * Copyright 2010 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This code is distributed in the hope that it will be useful,
 * but without any warranty; without even the implied warranty of
 * merchantability or fitness for a particular purpose. See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ch.psi.jcae.converter;

import gov.aps.jca.CAException;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import ch.psi.jcae.impl.ChannelBean;

/**
 * Abstract converter class to automatically convert channel values
 * 
 * @author ebner
 *
 */
public abstract class AbstractConverterBean<E,T> implements PropertyChangeListener {

	/**
	 * PropertyChangeSupport
	 */
	private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
	
	
	/**
	 * Key for property change support if value has changed
	 */
	public static final String PROPERTY_VALUE = "value";
	
	/**
	 * ChannelBean that gets wrapped by this converter
	 */
	private final ChannelBean<E> channelBean;
	
	public AbstractConverterBean(ChannelBean<E> channelBean){
		this.channelBean = channelBean;
		this.channelBean.addPropertyChangeListener(this);
	}
	
	
	/**
	 * Get converted value of channel
	 * @return Converted value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public T getValue() throws CAException, InterruptedException{
		E o = channelBean.getValue();
		return(convertForward(o));
	}
	
	/**
	 * Get converted channel value
	 * @param size	Size of the array/value to get and return
	 * @return Converted value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public T getValue(int size) throws CAException, InterruptedException{
		E o = channelBean.getValue(size);
		return(convertForward(o));
	}
	
	/**
	 * Get converted channel value
	 * @param force	Force the library to get the value over the network
	 * @return	Converted value
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public T getValue(boolean force) throws CAException, InterruptedException{
		E o = channelBean.getValue(force);
		return(convertForward(o));
	}
	
	/**
	 * Set converted value to channel
	 * @param value	Value to convert and to set on the channel
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setValue(T value) throws CAException, InterruptedException{
		E o = convertReverse(value);
		channelBean.setValue(o);
	}
	
	/**
	 * Set converted value to channel
	 * @param value		Value to convert and to set on the channel
	 * @param timeout 	Time to wait until set is done. If timeout <=0 wait forever. Timeout in milliseconds
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void setValue(T value, long timeout) throws CAException, InterruptedException{
		E o = convertReverse(value);
		channelBean.setValue(o, timeout);
	}
	
	/**
	 * Set converted value to channel without waiting the set to return
	 * @param value		Value to convert and to set to the channel
	 * @throws CAException
	 */
	public void setValueNoWait(T value) throws CAException{
		E o = convertReverse(value);
		channelBean.setValueNoWait(o);
	}
	
	/**
	 * Wait until channel has reached the specified value.
	 * @param value		Value to wait for
	 * @param timeout	Wait timeout in milliseconds. (if timeout=0 wait forever)
	 * @throws CAException
	 * @throws InterruptedException 
	 */
	public void waitForValue(T value, long timeout) throws CAException, InterruptedException{
		E o = convertReverse(value);
		channelBean.waitForValue(o, timeout);
	}
	
	/**
	 * Get wrapped ChannelBean object
	 * @return	Wrapped ChannelBean
	 */
	public ChannelBean<E> getChannelBean(){
		return channelBean;
	}
	
	/* (non-Javadoc)
	 * @see java.beans.PropertyChangeListener#propertyChange(java.beans.PropertyChangeEvent)
	 */
	@SuppressWarnings("unchecked")
	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		// Forward property change events that are value dependent
		
		E nv = (E) evt.getNewValue();
		E ov = (E) evt.getOldValue();
		String key = evt.getPropertyName();
		if(key.equals(ChannelBean.PROPERTY_VALUE)){
			changeSupport.firePropertyChange(PROPERTY_VALUE, convertForward(ov), convertForward(nv));
		}
	}
	
	/**
	 * Destroy this bean
	 */
	public void destroy(){
		// Unregister bean from channel bean
		channelBean.removePropertyChangeListener(this);
	}
	
	
	/**
	 * Add/register a property change listener for this object
	 * @param l		Listener object
	 */
	public void addPropertyChangeListener( PropertyChangeListener l ) {
		changeSupport.addPropertyChangeListener( l );
	} 

	/**
	 * Remove property change listener from this object
	 * @param l		Listener object
	 */
	public void removePropertyChangeListener( PropertyChangeListener l ) { 
		changeSupport.removePropertyChangeListener( l );
	}
	
	/**
	 * Function to convert a channel value into converter type
	 *  
	 * @param value	Value of the channel
	 * @return	Converted channel value
	 */
	protected abstract T convertForward(E value);

	/**
	 * Conversion function to convert value of the converter to the effective channel value.
	 * @param value	Value to convert to the channel value
	 * @return	Value to set on the channel
	 */
    protected abstract E convertReverse(T value);
}
/**
 * Copyright (c) 2012 Paul Scherrer Institute. All rights reserved.
 */

package ch.psi.jcae;

public class ChannelDescriptor<T> extends Descriptor<T> {

	private String name;
	
	
	public ChannelDescriptor(){
	}
	
	public ChannelDescriptor(Class<T> type, String name){
		this.name = name;
		this.type = type;
	}
	
	public ChannelDescriptor(Class<T> type, String name, Boolean monitored){
		this.name = name;
		this.type = type;
		this.monitored = monitored;
	}
	
	public ChannelDescriptor(Class<T> type, String name, Boolean monitored, Integer size){
		this.name = name;
		this.type = type;
		this.monitored = monitored;
		this.size = size;
	}
	
	/**
	 * @return the name
	 */
	public String getName() {
		return name;
	}
	/**
	 * @param name the name to set
	 */
	public void setName(String name) {
		this.name = name;
	}
	
}

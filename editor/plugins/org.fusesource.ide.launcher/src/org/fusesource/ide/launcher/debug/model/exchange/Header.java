/*******************************************************************************
 * Copyright (c) 2014 Red Hat, Inc.
 * Distributed under license by Red Hat, Inc. All rights reserved.
 * This program is made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution,
 * and is available at http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Red Hat, Inc. - initial API and implementation
 ******************************************************************************/
package org.fusesource.ide.launcher.debug.model.exchange;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlValue;

/**
 * @author lhein
 */
@XmlRootElement(name = "header")
public class Header {
	private String key;
	private String type;
	private String value;
	
	/**
	 * 
	 */
	public Header() {
		// TODO Auto-generated constructor stub
	}
	
	/**
	 * 
	 */
	public Header(String key, String value, String type) {
		this.key = key;
		this.value = value;
		this.type = type;
	}
	
	/**
	 * @return the key
	 */
	@XmlAttribute(name = "key")
	public String getKey() {
		return this.key;
	}
	
	/**
	 * @param key the key to set
	 */
	public void setKey(String key) {
		this.key = key;
	}
	
	/**
	 * @return the type
	 */
	@XmlAttribute(name = "type")
	public String getType() {
		return this.type;
	}
	
	/**
	 * @param type the type to set
	 */
	public void setType(String type) {
		this.type = type;
	}
	
	/**
	 * @return the value
	 */
	@XmlValue
	public String getValue() {
		return this.value;
	}
	
	/**
	 * @param value the value to set
	 */
	public void setValue(String value) {
		this.value = value;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return String.format("%s=%s", getKey(), getValue());
	}
}

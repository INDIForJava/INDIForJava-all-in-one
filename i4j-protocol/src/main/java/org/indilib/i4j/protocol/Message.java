package org.indilib.i4j.protocol;

/*
 * #%L
 * INDI Protocol implementation
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents an INDI XML protocol element.
 * 
 * @param <T>
 *            type for the builder
 * @author Richard van Nieuwenhoven
 */
@XStreamAlias("message")
public class Message<T> extends INDIProtocol<T> {

    /**
     * the device attribute of the element.
     */
    @XStreamAsAttribute
    private String device;

    /**
     * the message element attribute.
     */
    @XStreamAsAttribute
    private String message;

    /**
     * the timestamp attribute of the element.
     */
    @XStreamAsAttribute
    private String timestamp;

    /**
     * @return the device attribute of the element.
     */
    public String getDevice() {
        return device;
    }

    /**
     * @return the message element attribute.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the timestamp attribute of the element.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * set the device attribute of the element.
     * 
     * @param newDevice
     *            the new attibute device value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setDevice(String newDevice) {
        this.device = newDevice;
        return (T) this;
    }

    /**
     * set the max message atttribute.
     * 
     * @param newMessage
     *            the new message value
     * @return this for builder pattern.
     */

    @SuppressWarnings("unchecked")
    public T setMessage(String newMessage) {
        this.message = newMessage;
        return (T) this;
    }

    /**
     * set the timestamp attribute of the element.
     * 
     * @param newTimestamp
     *            the new attibute timestamp value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setTimestamp(String newTimestamp) {
        this.timestamp = newTimestamp;
        return (T) this;
    }
}

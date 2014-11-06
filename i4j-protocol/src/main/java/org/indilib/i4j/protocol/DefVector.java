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

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents an INDI XML protocol element.
 * 
 * @param <T>
 *            type for the builder
 * @author Richard van Nieuwenhoven
 */
public abstract class DefVector<T> extends INDIProtocol<T> {

    /**
     * the device element attribute.
     */
    @XStreamAsAttribute
    private String device;

    /**
     * the group element attribute.
     */
    @XStreamAsAttribute
    private String group;

    /**
     * the label element attribute.
     */
    @XStreamAsAttribute
    private String label;

    /**
     * the message element attribute.
     */
    @XStreamAsAttribute
    private String message;

    /**
     * the perm element attribute.
     */
    @XStreamAsAttribute
    private String perm;

    /**
     * the state element attribute.
     */
    @XStreamAsAttribute
    private String state;

    /**
     * the timeout element attribute.
     */
    @XStreamAsAttribute
    private String timeout;

    /**
     * the timestamp element attribute.
     */
    @XStreamAsAttribute
    private String timestamp;

    /**
     * @return the device element attribute.
     */
    public String getDevice() {
        return device;
    }

    /**
     * @return the group element attribute.
     */
    public String getGroup() {
        return group;
    }

    /**
     * @return the label element attribute.
     */
    public String getLabel() {
        return label;
    }

    /**
     * @return the message element attribute.
     */
    public String getMessage() {
        return message;
    }

    /**
     * @return the perm element attribute.
     */
    public String getPerm() {
        return perm;
    }

    /**
     * @return the state element attribute.
     */
    public String getState() {
        return state;
    }

    /**
     * @return the timeout element attribute.
     */
    public String getTimeout() {
        return timeout;
    }

    /**
     * @return the timestamp element attribute.
     */
    public String getTimestamp() {
        return timestamp;
    }

    /**
     * set the device element atttribute.
     * 
     * @param newDevice
     *            the new device value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setDevice(String newDevice) {
        this.device = newDevice;
        return (T) this;
    }

    /**
     * set the group element atttribute.
     * 
     * @param newGroup
     *            the new group value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setGroup(String newGroup) {
        this.group = newGroup;
        return (T) this;
    }

    /**
     * set the label element atttribute.
     * 
     * @param newLabel
     *            the new label value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setLabel(String newLabel) {
        this.label = newLabel;
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
     * set the perm element atttribute.
     * 
     * @param newPerm
     *            the new perm value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setPerm(String newPerm) {
        this.perm = newPerm;
        return (T) this;
    }

    /**
     * set the state element atttribute.
     * 
     * @param newState
     *            the new state value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setState(String newState) {
        this.state = newState;
        return (T) this;
    }

    /**
     * set the timeout element atttribute.
     * 
     * @param newTimeout
     *            the new timeout value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setTimeout(String newTimeout) {
        this.timeout = newTimeout;
        return (T) this;
    }

    /**
     * set the timestamp element atttribute.
     * 
     * @param newTimestamp
     *            the new timestamp value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setTimestamp(String newTimestamp) {
        this.timestamp = newTimestamp;
        return (T) this;
    }

    @Override
    public boolean isVector() {
        return true;
    }

    @Override
    public boolean isDef() {
        return true;
    }
}

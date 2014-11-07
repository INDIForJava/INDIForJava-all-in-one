package org.indilib.i4j.protocol;

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

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
@XStreamAlias("getProperties")
public class GetProperties<T> extends INDIProtocol<T> {

    /**
     * the device attribute of the element.
     */
    @XStreamAsAttribute
    private String device;

    /**
     * @return the device attribute of the element.
     */
    public String getDevice() {
        return device;
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
}

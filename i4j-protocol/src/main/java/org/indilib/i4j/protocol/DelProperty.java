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
@XStreamAlias("delProperty")
public class DelProperty<T> extends INDIProtocol<T> {

    /**
     * the device attribute of the element.
     */
    @XStreamAsAttribute
    private String device;

    /**
     * the version attribute of the element.
     */
    @XStreamAsAttribute
    private String version;

    /**
     * @return the device attribute of the element.
     */
    public String getDevice() {
        return device;
    }
    /**
     * @return the version attribute of the element.
     */
    public String getVersion() {
        return version;
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
     * set the version attribute of the element.
     * 
     * @param newVersion
     *            the new attibute version value
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setVersion(String newVersion) {
        this.version = newVersion;
        return (T) this;
    }
}

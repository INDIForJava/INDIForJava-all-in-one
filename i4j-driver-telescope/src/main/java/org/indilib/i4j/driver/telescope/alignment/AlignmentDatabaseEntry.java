package org.indilib.i4j.driver.telescope.alignment;

import java.io.Serializable;

public class AlignmentDatabaseEntry implements Cloneable, Serializable {

    private static final long serialVersionUID = 5136616882513466640L;

    protected double observationJulianDate;

    /**
     * Right ascension in decimal hours. N.B. libnova works in decimal degrees
     * so conversion is always needed!
     */
    protected double rightAscension;

    /** Declination in decimal degrees */
    protected double declination;

    /**
     * Normalised vector giving telescope pointing direction. This is referred
     * to elsewhere as the "apparent" direction.
     */
    protected TelescopeDirectionVector telescopeDirection;

    /** Private data associated with this sync point */
    private byte[] privateData;

    @Override
    public Object clone() throws CloneNotSupportedException {
        AlignmentDatabaseEntry clone = (AlignmentDatabaseEntry) super.clone();
        clone.telescopeDirection = (TelescopeDirectionVector) telescopeDirection.clone();
        clone.privateData = new byte[privateData.length];
        System.arraycopy(privateData, 0, clone.privateData, 0, privateData.length);
        return clone;
    }
}

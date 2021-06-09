package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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

import java.io.Serializable;

/**
 * entry to store in the alignment database.
 *
 * @author Richard van Nieuwenhoven
 */
public class AlignmentDatabaseEntry implements Cloneable, Serializable {

    /**
     * serial versionid.
     */
    private static final long serialVersionUID = 5136616882513466640L;

    /**
     * obeservation julian date.
     */
    protected double observationJulianDate;

    /**
     * Right ascension in decimal hours. N.B. libnova works in decimal degrees
     * so conversion is always needed!
     */
    protected double rightAscension;

    /**
     * Declination in decimal degrees.
     */
    protected double declination;

    /**
     * Normalised vector giving telescope pointing direction. This is referred
     * to elsewhere as the "apparent" direction.
     */
    protected TelescopeDirectionVector telescopeDirection;

    /**
     * Private data associated with this sync point.
     */
    private byte[] privateData;

    /**
     * constructor with all fields at once.
     *
     * @param rightAscension        the right ascention
     * @param declination           the declination
     * @param observationJulianDate the observation date
     * @param telescopeDirection    the telescope direction vector
     */
    public AlignmentDatabaseEntry(double rightAscension, double declination, double observationJulianDate, TelescopeDirectionVector telescopeDirection) {
        this.rightAscension = rightAscension;
        this.declination = declination;
        this.observationJulianDate = observationJulianDate;
        this.telescopeDirection = telescopeDirection;
    }

    /**
     * default constructor.
     */
    public AlignmentDatabaseEntry() {
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        AlignmentDatabaseEntry clone = (AlignmentDatabaseEntry) super.clone();
        clone.telescopeDirection = (TelescopeDirectionVector) telescopeDirection.clone();
        clone.privateData = new byte[privateData.length];
        System.arraycopy(privateData, 0, clone.privateData, 0, privateData.length);
        return clone;
    }
}

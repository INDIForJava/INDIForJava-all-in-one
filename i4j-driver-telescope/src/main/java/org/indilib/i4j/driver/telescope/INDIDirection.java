package org.indilib.i4j.driver.telescope;

import org.indilib.i4j.INDISexagesimalFormatter;

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

/**
 * Direction pointer coordinates for a telescope.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIDirection {

    /**
     * the right ascension of the direction pointer coordinates.
     */
    private double ra;

    /**
     * the declination of the direction pointer coordinates.
     */
    private double dec;

    /**
     * formatter for the toStrings.
     */
    private final INDISexagesimalFormatter formatter = new INDISexagesimalFormatter("%010.6m");

    /**
     * create a direction pointer coordinates for a telescope.
     * 
     * @param ra
     *            the right ascension of the point in space
     * @param dec
     *            the declination of the point in space
     */
    public INDIDirection(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }

    /**
     * create a direction pointer coordinates for a telescope.
     */
    public INDIDirection() {
    }

    /**
     * @return the right ascension of the direction pointer coordinates.
     */
    public double getRa() {
        return ra;
    }

    /**
     * @return the right ascension of the direction pointer coordinates as a
     *         string.
     */
    public String getRaString() {
        return formatter.format(ra);
    }

    /**
     * set the right ascension of the direction pointer coordinates.
     * 
     * @param ra
     *            the right ascension of the point in space
     */
    public void setRa(double ra) {
        this.ra = ra;
    }

    /**
     * @return the declination of the direction pointer coordinates.
     */
    public double getDec() {
        return dec;
    }

    /**
     * @return the declination of the direction pointer coordinates as a string.
     */
    public String getDecString() {
        return formatter.format(dec);
    }

    /**
     * set the declination of the direction pointer coordinates.
     * 
     * @param dec
     *            the declination of the point in space
     */
    public void setDec(double dec) {
        this.dec = dec;
    }

    /**
     * set the direction pointer coordinates.
     * 
     * @param newRa
     *            the right ascension of the point in space
     * @param newDec
     *            the declination of the point in space
     */
    public void set(double newRa, double newDec) {
        this.ra = newRa;
        this.dec = newDec;
    }

    /**
     * add the value to the right ascension of the direction pointer
     * coordinates.
     * 
     * @param addRa
     *            the addition to the right ascension of the point in space
     */
    public void addRa(double addRa) {
        this.ra += addRa;
    }

    /**
     * add the value to the declination of the direction pointer coordinates.
     * 
     * @param addDec
     *            the addition to the declination of the point in space
     */
    public void addDec(double addDec) {
        this.dec += addDec;
    }

}

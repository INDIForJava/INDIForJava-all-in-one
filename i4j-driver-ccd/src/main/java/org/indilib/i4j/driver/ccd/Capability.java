package org.indilib.i4j.driver.ccd;

/*
 * #%L
 * INDI for Java Abstract CCD Driver
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
 * This class collects the different capabilities of the current ccd driver. It
 * consists of a list of booleans that can be set with the "builder" paradigm.
 * 
 * @author Richard van Nieuwenhoven
 */
public class Capability {

    /**
     * Can this driver abort a running exposure?
     */
    private boolean canAbort = false;

    /**
     * Can this driver bin pixel together?
     */
    private boolean canBin = false;

    /**
     * can this driver do expose a subframe?
     */
    private boolean canSubFrame = false;

    /**
     * Does this driver have a cooler (and a temperature sensor)?
     */
    private boolean hasCooler = false;

    /**
     * Does this ccd have a second ccd chip as a guider?
     */
    private boolean hasGuideHead = false;

    /**
     * does this ccd have an shutter (that the driver can operate)?
     */
    private boolean hasShutter = false;

    /**
     * @return True if CCD can abort exposure. False otherwise.
     */
    public boolean canAbort() {
        return canAbort;
    }

    /**
     * set the can abort value.
     * 
     * @param canAbortValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability canAbort(boolean canAbortValue) {
        this.canAbort = canAbortValue;
        return this;
    }

    /**
     * @return True if CCD supports binning. False otherwise.
     */
    public boolean canBin() {
        return canBin;
    }

    /**
     * set the can bin value.
     * 
     * @param canBinValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability canBin(boolean canBinValue) {
        this.canBin = canBinValue;
        return this;
    }

    /**
     * @return True if CCD supports subframing. False otherwise.
     */
    public boolean canSubFrame() {
        return canSubFrame;
    }

    /**
     * set the can subframe value.
     * 
     * @param canSubFrameValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability canSubFrame(boolean canSubFrameValue) {
        this.canSubFrame = canSubFrameValue;
        return this;
    }

    /**
     * @return True if CCD has cooler and temperature can be controlled. False
     *         otherwise.
     */
    public boolean hasCooler() {
        return hasCooler;
    }

    /**
     * set the has cooler value.
     * 
     * @param hasCoolerValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability hasCooler(boolean hasCoolerValue) {
        this.hasCooler = hasCoolerValue;
        return this;
    }

    /**
     * @return True if CCD has guide head. False otherwise.
     */
    public boolean hasGuideHead() {
        return hasGuideHead;
    }

    /**
     * set the has guider head value.
     * 
     * @param hasGuideHeadValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability hasGuideHead(boolean hasGuideHeadValue) {
        this.hasGuideHead = hasGuideHeadValue;
        return this;
    }

    /**
     * @return True if CCD has mechanical or electronic shutter. False
     *         otherwise.
     */
    public boolean hasShutter() {
        return hasShutter;
    }

    /**
     * set the has shutter value.
     * 
     * @param hasShutterValue
     *            the new value
     * @return this to specify more capabilities
     */
    public Capability hasShutter(boolean hasShutterValue) {
        this.hasShutter = hasShutterValue;
        return this;
    }

}

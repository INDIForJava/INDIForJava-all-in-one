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

public class Capability {

    private boolean canAbort = false;

    private boolean canBin = false;

    private boolean canSubFrame = false;

    private boolean hasCooler = false;

    private boolean hasGuideHead = false;

    private boolean hasShutter = false;
    /**
     * @return True if CCD can abort exposure. False otherwise.
     */
    public boolean canAbort() {
        return canAbort;
    }

    public Capability canAbort(boolean canAbort) {
        this.canAbort = canAbort;
        return this;
    }

    /**
     * @return True if CCD supports binning. False otherwise.
     */
    public boolean canBin() {
        return canBin;
    }

    public Capability canBin(boolean canBin) {
        this.canBin = canBin;
        return this;
    }

    /**
     * @return True if CCD supports subframing. False otherwise.
     */
    public boolean canSubFrame() {
        return canSubFrame;
    }

    public Capability canSubFrame(boolean canSubFrame) {
        this.canSubFrame = canSubFrame;
        return this;
    }

    /**
     * @return True if CCD has cooler and temperature can be controlled. False
     *         otherwise.
     */
    public boolean hasCooler() {
        return hasCooler;
    }

    public Capability hasCooler(boolean hasCooler) {
        this.hasCooler = hasCooler;
        return this;
    }

    /**
     * @return True if CCD has guide head. False otherwise.
     */
    public boolean hasGuideHead() {
        return hasGuideHead;
    }

    public Capability hasGuideHead(boolean hasGuideHead) {
        this.hasGuideHead = hasGuideHead;
        return this;
    }

    /**
     * @return True if CCD has mechanical or electronic shutter. False
     *         otherwise.
     */
    public boolean hasShutter() {
        return hasShutter;
    }

    public Capability hasShutter(boolean hasShutter) {
        this.hasShutter = hasShutter;
        return this;
    }


}

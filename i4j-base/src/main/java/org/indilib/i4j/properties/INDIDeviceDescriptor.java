package org.indilib.i4j.properties;

/*
 * #%L
 * INDI for Java Base Library
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import static org.indilib.i4j.properties.INDIStandardProperty.ABS_DOME_POSITION;
import static org.indilib.i4j.properties.INDIStandardProperty.ABS_FOCUS_POSITION;
import static org.indilib.i4j.properties.INDIStandardProperty.ATMOSPHERE;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_ABORT_EXPOSURE;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_BINNING;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_CFA;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_COMPRESSION;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_COOLER;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_COOLER_POWER;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_EXPOSURE;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_FRAME;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_FRAME_RESET;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_FRAME_TYPE;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_INFO;
import static org.indilib.i4j.properties.INDIStandardProperty.CCDn_TEMPERATURE;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_ABORT_MOTION;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_AUTOSYNC;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_GOTO;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_MEASUREMENTS;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_MOTION;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_PARAMS;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_SHUTTER;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_SPEED;
import static org.indilib.i4j.properties.INDIStandardProperty.DOME_TIMER;
import static org.indilib.i4j.properties.INDIStandardProperty.EQUATORIAL_COORD;
import static org.indilib.i4j.properties.INDIStandardProperty.EQUATORIAL_EOD_COORD;
import static org.indilib.i4j.properties.INDIStandardProperty.FILTER_NAME;
import static org.indilib.i4j.properties.INDIStandardProperty.FILTER_SLOT;
import static org.indilib.i4j.properties.INDIStandardProperty.GEOGRAPHIC_COORD;
import static org.indilib.i4j.properties.INDIStandardProperty.HORIZONTAL_COORD;
import static org.indilib.i4j.properties.INDIStandardProperty.ON_COORD_SET;
import static org.indilib.i4j.properties.INDIStandardProperty.REL_DOME_POSITION;
import static org.indilib.i4j.properties.INDIStandardProperty.SWITCHn;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_ABORT_MOTION;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_INFO;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_MOTION_NS;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_MOTION_WE;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_PARK;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_SLEW_RATE;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_TIMED_GUIDE_NS;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_TIMED_GUIDE_WE;
import static org.indilib.i4j.properties.INDIStandardProperty.TELESCOPE_TRACK_RATE;
import static org.indilib.i4j.properties.INDIStandardProperty.TIME_UTC;

/**
 * This enumeration list allows the detectction what kind of device a device is
 * depending on the available properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum INDIDeviceDescriptor {
    /**
     * telescope device.
     */
    TELESCOPE(present(EQUATORIAL_EOD_COORD), present(ON_COORD_SET), present(TELESCOPE_MOTION_NS), present(TELESCOPE_MOTION_WE), present(TELESCOPE_TIMED_GUIDE_NS),
            present(TELESCOPE_TIMED_GUIDE_WE), present(TELESCOPE_SLEW_RATE), present(TELESCOPE_PARK), present(TELESCOPE_ABORT_MOTION), present(TELESCOPE_TRACK_RATE),
            present(TELESCOPE_INFO), present(EQUATORIAL_COORD), present(HORIZONTAL_COORD)),
    /**
     * ccd device.
     */
    CCD(present(CCDn_FRAME), present(CCDn_EXPOSURE), present(CCDn_ABORT_EXPOSURE), present(CCDn_FRAME), present(CCDn_TEMPERATURE), present(CCDn_COOLER),
            present(CCDn_COOLER_POWER), present(CCDn_FRAME_TYPE), present(CCDn_BINNING), present(CCDn_COMPRESSION), present(CCDn_FRAME_RESET), present(CCDn_INFO),
            present(CCDn_CFA), present(CCDn)),
    /**
     * filter device.
     */
    FILTER(present(FILTER_SLOT), present(FILTER_NAME), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),
    /**
     * focuser device.
     */
    FOCUSER(present(ABS_FOCUS_POSITION), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),
    /**
     * dome device.
     */
    DOME(present(DOME_SPEED), present(DOME_MOTION), present(DOME_TIMER), present(REL_DOME_POSITION), present(ABS_DOME_POSITION), present(DOME_ABORT_MOTION),
            present(DOME_SHUTTER), present(DOME_GOTO), present(DOME_PARAMS), present(DOME_AUTOSYNC), present(DOME_MEASUREMENTS)),
    /**
     * location device.
     */
    LOCATION(present(GEOGRAPHIC_COORD), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),
    /**
     * weather device.
     */
    WEATHER(present(ATMOSPHERE), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),
    /**
     * time device.
     */
    TIME(present(TIME_UTC), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),
    /**
     * switch device.
     */
    SWITCH(present(SWITCHn), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME));

    /**
     * Description of a property that should be availabe or missing in a device.
     */
    private static final class Description {

        /**
         * shoudl the property be there or not.
         */
        private final boolean present;

        /**
         * name of the property.
         */
        private final String name;

        /**
         * @return the name of the property.
         */
        private String name() {
            return name;
        }

        /**
         * constructor.
         * 
         * @param name
         *            the name of the property.
         * @param present
         *            should it be present or not present.
         */
        private Description(String name, boolean present) {
            this.name = name;
            this.present = present;
        }
    }

    /**
     * properties that describe a device.
     */
    private final Description[] propertyDescription;

    /**
     * construct a device description based on avaiable and not available
     * properties.
     * 
     * @param propertyDescription
     *            the properties that describe a device.
     */
    private INDIDeviceDescriptor(Description... propertyDescription) {
        this.propertyDescription = propertyDescription;
    }

    /**
     * the property that should be present in a device.
     * 
     * @param property
     *            the property
     * @return the description.
     */
    private static Description present(INDIStandardProperty property) {
        return new Description(property.name(), true);
    }

    /**
     * the property that should be missing in a device.
     * 
     * @param property
     *            the property
     * @return the description.
     */
    private static Description missing(INDIStandardProperty property) {
        return new Description(property.name(), false);
    }

}
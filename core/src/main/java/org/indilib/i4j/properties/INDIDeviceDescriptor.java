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

import java.util.*;

import static org.indilib.i4j.properties.INDIStandardProperty.*;

/**
 * This enumeration list allows the detection what kind of device a device is
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
    SWITCH(present(SWITCH_MODULE), missing(EQUATORIAL_EOD_COORD), missing(EQUATORIAL_COORD), missing(HORIZONTAL_COORD), missing(CCDn_FRAME)),

    /**
     * an unknown device, no standard properties.
     */
    UNKNOWN();

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
    INDIDeviceDescriptor(Description... propertyDescription) {
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

    /**
     * Analyze a list of properties and depending on the presence or not
     * Presence of properties try to detect the type of device something is.
     * 
     * @param properties
     *            the available list of properties.
     * @return the enumeration that describes the device type.
     */
    public static INDIDeviceDescriptor[] detectDeviceType(Collection<String> properties) {
        Set<String> indexedProperties = unfifyPropertyNames(properties);
        List<Match> matches = new LinkedList<>();
        for (INDIDeviceDescriptor device : values()) {
            int points = 0;
            for (Description property : device.getPropertyDescription()) {
                if (property.isPresent()) {
                    // the presence of a property counts as a point.
                    if (indexedProperties.contains(property.name)) {
                        points++;
                    }
                } else {
                    // the presence of a missing property is absolute (no
                    // match).
                    if (indexedProperties.contains(property.name)) {
                        points = Integer.MIN_VALUE;
                        break;
                    }
                }
            }
            if (points > 0) {
                matches.add(new Match(device, points));
            }
        }
        matches.sort(Comparator.comparingInt(o -> o.matchPoints));
        INDIDeviceDescriptor[] result = new INDIDeviceDescriptor[matches.size()];
        for (int index = 0; index < result.length; index++) {
            result[index] = matches.get(index).descriptor;
        }
        return result;
    }

    /**
     * take the list of strings and rename each to a naming that is compatible
     * with the device list. So all upper cases and all digits to a small 'n'.
     * 
     * @param properties
     *            the list of properties to convert
     * @return the new unified list.
     */
    private static Set<String> unfifyPropertyNames(Collection<String> properties) {
        Set<String> indexedProperties = new HashSet<>();
        for (String string : properties) {
            StringBuilder buffer = new StringBuilder();
            for (char character : string.toCharArray()) {
                if (Character.isDigit(character)) {
                    buffer.append('n');
                } else {
                    buffer.append(Character.toUpperCase(character));
                }
            }
            indexedProperties.add(buffer.toString());
        }
        return indexedProperties;
    }

    /**
     * @return properties that describe a device.
     */
    private Description[] getPropertyDescription() {
        return propertyDescription;
    }

    /**
     * Description of a property that should be availabe or missing in a device.
     */
    private static final class Description {

        /**
         * should the property be there or not.
         */
        private final boolean present;

        /**
         * name of the property.
         */
        private final String name;

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

        /**
         * @return if the property should be there or just not.
         */
        private boolean isPresent() {
            return present;
        }
    }

    /**
     * a match during the search for devices.
     */
    private static class Match {

        /**
         * the matching descriptor.
         */
        private final INDIDeviceDescriptor descriptor;

        /**
         * how good does the device match? the higher the better.
         */
        private final int matchPoints;

        /**
         * constructor for the Match.
         * 
         * @param descriptor
         *            the matching descriptor.
         * @param matchPoints
         *            how good does the device match? the higher the better.
         */
        public Match(INDIDeviceDescriptor descriptor, int matchPoints) {
            this.descriptor = descriptor;
            this.matchPoints = matchPoints;
        }
    }
}

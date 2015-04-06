package org.indilib.i4j.test;

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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import static org.indilib.i4j.properties.INDIDeviceDescriptor.CCD;
import static org.indilib.i4j.properties.INDIDeviceDescriptor.FILTER;
import static org.indilib.i4j.properties.INDIDeviceDescriptor.FOCUSER;
import static org.indilib.i4j.properties.INDIDeviceDescriptor.LOCATION;
import static org.indilib.i4j.properties.INDIDeviceDescriptor.TIME;
import static org.indilib.i4j.properties.INDIDeviceDescriptor.*;

import java.util.Arrays;
import java.util.List;

import org.indilib.i4j.properties.INDIDeviceDescriptor;
import org.junit.Assert;
import org.junit.Test;

public class TestDeviceDetector {

    @Test
    public void testCCD() throws Exception {
        String[] properties = {
            "CCD_TEMPERATURE",
            "UPLOAD_MODE",
            "UPLOAD_SETTINGS",
            "CCD_FRAME",
            "CCD_FRAME_TYPE",
            "CCD_EXPOSURE",
            "CCD_ABORT_EXPOSURE",
            "CCD_BINNING",
            "CCD_INFO",
            "CCD_COMPRESSION",
            "CCD1",
            "CCD_AUTO_LOOP"
        };
        Assert.assertEquals(CCD, INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties))[0]);
    }

    @Test
    public void testFilter() {

        String[] properties = {
            "filter_names",
            "FILTER_SLOT",
            "FILTER_NAME"
        };
        Assert.assertEquals(FILTER, INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties))[0]);
    }

    @Test
    public void testFocuser() {

        String[] properties = {
            "ABS_FOCUS_POSITION",
            "FOCUS_SPEED",
            "stop_focusing"
        };
        Assert.assertEquals(FOCUSER, INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties))[0]);
    }

    @Test
    public void testTelescope1() {
        String[] properties = {
            "EQUATORIAL_EOD_COORD",
            "TIME_UTC",
            "GEOGRAPHIC_COORD",
            "ON_COORD_SET",
            "CONFIG_PROCESS",
            "TELESCOPE_ABORT_MOTION",
            "TELESCOPE_INFO",
            "LEVEL_NORTH"
        };
        INDIDeviceDescriptor[] detectedDeviceType = INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties));
        Assert.assertEquals(TELESCOPE, detectedDeviceType[0]);
        Assert.assertEquals(1, detectedDeviceType.length);
    }

    @Test
    public void testMix1() {

        String[] properties = {
            "TIME_UTC",
            "GEOGRAPHIC_COORD",
            "ATMOSPHERE"
        };
        List<INDIDeviceDescriptor> detectedDeviceType = Arrays.asList(INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties)));
        Assert.assertTrue(detectedDeviceType.contains(LOCATION));
        Assert.assertTrue(detectedDeviceType.contains(WEATHER));
        Assert.assertTrue(detectedDeviceType.contains(TIME));
        Assert.assertEquals(3, detectedDeviceType.size());
    }

    @Test
    public void testFilter2() {

        String[] properties = {
            "filter_names",
            "FILTER_SLOT",
            "FILTER_NAME",
            "filter_positions",
            "factory_settings"
        };
        Assert.assertEquals(FILTER, INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties))[0]);
    }

    @Test
    public void testTelescope() {

        String[] properties = {
            "EQUATORIAL_EOD_COORD",
            "TIME_UTC",
            "GEOGRAPHIC_COORD",
            "ON_COORD_SET",
            "CONFIG_PROCESS",
            "TELESCOPE_ABORT_MOTION",
            "TELESCOPE_INFO"
        };
        INDIDeviceDescriptor[] detectedDeviceType = INDIDeviceDescriptor.detectDeviceType(Arrays.asList(properties));
        Assert.assertEquals(TELESCOPE, detectedDeviceType[0]);
        Assert.assertEquals(1, detectedDeviceType.length);
    }

}

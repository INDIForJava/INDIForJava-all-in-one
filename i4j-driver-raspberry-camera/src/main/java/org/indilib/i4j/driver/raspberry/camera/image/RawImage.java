package org.indilib.i4j.driver.raspberry.camera.image;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.sanselan.formats.tiff.TiffField;
import org.apache.sanselan.formats.tiff.TiffImageMetadata;

/**
 * This class represents a raw image of the raspberry pi camera together with
 * the recorded exif informations.
 * 
 * @author Richard van Nieuwenhoven
 */
public class RawImage {

    /**
     * date format for exif dates.
     */
    private final SimpleDateFormat exifDateFormat = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    /**
     * the instrument that took the image. ;-) cam of cource.
     */
    private String instrument = "";

    /**
     * date time when the picture was taken..
     */
    private double expTime = -1d;

    /**
     * the exposure time used to take the picure.
     */
    private double shutterSpeedValue = -1d;

    /**
     * the eperture used to take the picure.
     */
    private double apertureValue = -1d;

    /**
     * the brightness used to take the picure.
     */
    private double brightnessValue = -1d;

    /**
     * the max aperture used to take the picure.
     */
    private double maxApertureValue = -1d;

    /**
     * the iso used to take the picure.
     */
    private Integer iso = -1;

    /**
     * date time when the picture was taken..
     */
    private Date observeDat = new Date();

    /**
     * the raw image data.
     */
    private byte[] rawData;

    /**
     * @return the raw image data.
     */
    public byte[] getRawData() {
        return rawData;
    }

    /**
     * Constructor for the raw image. store the raw data and extract the exif
     * informations.
     * 
     * @param metadata
     *            the exif data
     * @param rawData
     *            the raw image data
     * @throws Exception
     *             if something goes worng , not expected.
     */
    protected RawImage(TiffImageMetadata metadata, byte[] rawData) throws Exception {
        this.rawData = rawData;
        @SuppressWarnings("unchecked")
        List<TiffField> exifFields = metadata.getAllFields();
        String make = "";
        String model = "";
        for (TiffField tiffField : exifFields) {
            if ("Make".equals(tiffField.getTagName())) {
                make = tiffField.getStringValue();
            } else if ("Model".equals(tiffField.getTagName())) {
                model = tiffField.getStringValue();
            } else if ("Exposure Time".equals(tiffField.getTagName())) {
                expTime = tiffField.getDoubleValue();
            } else if ("ISO".equals(tiffField.getTagName())) {
                iso = tiffField.getIntValue();
            } else if ("Create Date".equals(tiffField.getTagName())) {
                observeDat = exifDateFormat.parse(tiffField.getStringValue());
            } else if ("Shutter Speed Value".equals(tiffField.getTagName())) {
                shutterSpeedValue = tiffField.getDoubleValue();
            } else if ("Aperture Value".equals(tiffField.getTagName())) {
                apertureValue = tiffField.getDoubleValue();
            } else if ("Brightness Value".equals(tiffField.getTagName())) {
                brightnessValue = tiffField.getDoubleValue();
            } else if ("Max Aperture Value".equals(tiffField.getTagName())) {
                maxApertureValue = tiffField.getDoubleValue();
            }
            instrument = (make + " " + model).trim();
        }
    }

    /**
     * @return the exif data as fits header infos.
     */
    public Map<String, Object> getFitsAttributes() {
        Map<String, Object> result = new HashMap<>();
        result.put("PI-INSTR", instrument);
        result.put("PI-EXPTI", expTime);
        result.put("PI-D-OBS", observeDat);
        result.put("PI-ISO", iso);
        result.put("PI-SSV", shutterSpeedValue);
        result.put("PI-AV", apertureValue);
        result.put("PI-BV", brightnessValue);
        result.put("PI-MAV", maxApertureValue);
        return result;
    }
}

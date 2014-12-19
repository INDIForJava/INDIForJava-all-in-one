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
import org.indilib.i4j.fits.StandardFitsHeader;

public class RawImage {

    private final SimpleDateFormat EXIF_DATE_FORMAT = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public double getExpTime() {
        return expTime;
    }

    public void setExpTime(double expTime) {
        this.expTime = expTime;
    }

    public double getShutterSpeedValue() {
        return shutterSpeedValue;
    }

    public void setShutterSpeedValue(double shutterSpeedValue) {
        this.shutterSpeedValue = shutterSpeedValue;
    }

    public double getApertureValue() {
        return apertureValue;
    }

    public void setApertureValue(double apertureValue) {
        this.apertureValue = apertureValue;
    }

    public double getBrightnessValue() {
        return brightnessValue;
    }

    public void setBrightnessValue(double brightnessValue) {
        this.brightnessValue = brightnessValue;
    }

    public double getMaxApertureValue() {
        return maxApertureValue;
    }

    public void setMaxApertureValue(double maxApertureValue) {
        this.maxApertureValue = maxApertureValue;
    }

    public Integer getIso() {
        return iso;
    }

    public void setIso(Integer iso) {
        this.iso = iso;
    }

    public Date getObserveDat() {
        return observeDat;
    }

    public void setObserveDat(Date observeDat) {
        this.observeDat = observeDat;
    }

    public byte[] getRawData() {
        return rawData;
    }

    public void setRawData(byte[] rawData) {
        this.rawData = rawData;
    }

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
                observeDat = EXIF_DATE_FORMAT.parse(tiffField.getStringValue());
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

    private String instrument = "";

    private double expTime = -1d;

    private double shutterSpeedValue = -1d;

    private double apertureValue = -1d;

    private double brightnessValue = -1d;

    private double maxApertureValue = -1d;

    private Integer iso = -1;

    private Date observeDat = new Date();

    private byte[] rawData;

    public Map<String, Object> getFitsAttributes() {
        Map<String, Object> result = new HashMap<>();
        result.put("PI-INSTRUME", instrument);
        result.put("PI-EXPTIME", expTime);
        result.put("PI-DATE-OBS", observeDat);
        result.put("PI-ISO", iso);
        result.put("PI-SSV", shutterSpeedValue);
        result.put("PI-AV", apertureValue);
        result.put("PI-BV", brightnessValue);
        result.put("PI-MAV", maxApertureValue);
        result.put(StandardFitsHeader.BAYERPAT, "BGGR");
        return result;
    }
}

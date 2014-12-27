package org.indilib.i4j.driver.raspberry.camera;

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

/**
 * The possibple rapstill options.
 * 
 * @author Richard van Nieuwenhoven
 */
public enum CameraOption {
    /**
     * Set image width size.
     */
    width("-w", "--width", "Set image width <size>"),
    /**
     * Set image height size.
     */
    height("-h", "--height", "Set image height <size>"),
    /**
     * Set jpeg quality 0 to 100.
     */
    quality("-q", "--quality", "Set jpeg quality <0 to 100>"),
    /**
     * Add raw bayer data to jpeg metadata.
     */
    raw("-r", "--raw", "Add raw bayer data to jpeg metadata"),
    /**
     * Output filename filename (to write to stdout, use '-o -'). If not
     * specified, no file is saved.
     */
    output("-o", "--output", "Output filename <filename> (to write to stdout, use '-o -'). If not specified, no file is saved"),
    /**
     * Output verbose information during run.
     */
    verbose("-v", "--verbose", "Output verbose information during run"),
    /**
     * Time (in ms) before takes picture and shuts down (if not specified, set
     * to 5s).
     */
    timeout("-t", "--timeout", "Time (in ms) before takes picture and shuts down (if not specified, set to 5s)"),
    /**
     * Set thumbnail parameters (x:y:quality).
     */
    thumb("-th", "--thumb", "Set thumbnail parameters (x:y:quality)"),
    /**
     * Run a demo mode (cycle through range of camera options, no capture).
     */
    demo("-d", "--demo", "Run a demo mode (cycle through range of camera options, no capture)"),
    /**
     * Encoding to use for output file (jpg, bmp, gif, png).
     */
    encoding("-e", "--encoding", "Encoding to use for output file (jpg, bmp, gif, png)"),
    /**
     * EXIF tag to apply to captures (format as 'key=value').
     */
    exif("-x", "--exif", "EXIF tag to apply to captures (format as 'key=value')"),
    /**
     * Timelapse mode. Takes a picture every t ms.
     */
    timelapse("-tl", "--timelapse", "Timelapse mode. Takes a picture every <t>ms"),
    /**
     * Set image sharpness (-100 to 100).
     */
    sharpness("-sh", "--sharpness", "Set image sharpness (-100 to 100)"),
    /**
     * Set image contrast (-100 to 100).
     */
    contrast("-co", "--contrast", "Set image contrast (-100 to 100)"),
    /**
     * Set image brightness (0 to 100).
     */
    brightness("-br", "--brightness", "Set image brightness (0 to 100)"),
    /**
     * Set image saturation (-100 to 100).
     */
    saturation("-sa", "--saturation", "Set image saturation (-100 to 100)"),
    /**
     * Set capture ISO.
     */
    ISO("-ISO", "--ISO", "Set capture ISO"),
    /**
     * Turn on video stablisation.
     */
    vstab("-vs", "--vstab", "Turn on video stablisation"),
    /**
     * Set EV compensation.
     */
    ev("-ev", "--ev", "Set EV compensation"),
    /**
     * Set exposure mode (see Notes).
     */
    exposure("-ex", "--exposure", "Set exposure mode (see Notes)"),
    /**
     * Set AWB mode (see Notes).
     */
    awb("-awb", "--awb", "Set AWB mode (see Notes)"),
    /**
     * Set AWB gains - AWB mode must be off.
     */
    awbgains("-awbg", "--awbgains", "Set AWB gains - AWB mode must be off"),
    /**
     * Set image effect (see Notes).
     */
    imxfx("-ifx", "--imxfx", "Set image effect (see Notes)"),
    /**
     * Set colour effect (U:V).
     */
    colfx("-cfx", "--colfx", "Set colour effect (U:V)"),
    /**
     * Set metering mode (see Notes).
     */
    metering("-mm", "--metering", "Set metering mode (see Notes)"),
    /**
     * Set image rotation (0-359).
     */
    rotation("-rot", "--rotation", "Set image rotation (0-359)"),
    /**
     * Do not display a preview window.
     */
    nopreview("-n", "--nopreview", "Do not display a preview window"),
    /**
     * Enable 'burst capture mode'.
     */
    burst("-bm", "--burst", "Enable 'burst capture mode'"),
    /**
     * Set shutter speed in microseconds.
     */
    shutter("-ss", "--shutter", "Set shutter speed in microseconds"),
    /**
     * Set horizontal flip.
     */
    hflip("-hf", "--hflip", "Set horizontal flip"),
    /**
     * Set vertical flip.
     */
    vflip("-vf", "--vflip", "Set vertical flip");

    /**
     * the short argument version.
     */
    private final String shortArg;

    /**
     * the long argument version.
     */
    private final String longArg;

    /**
     * the argument description.
     */
    private final String description;

    /**
     * internal contructor for a camera option.
     * 
     * @param shortArg
     *            the short argument version.
     * @param longArg
     *            the long argument version.
     * @param description
     *            the argument description.
     */
    private CameraOption(String shortArg, String longArg, String description) {
        this.shortArg = shortArg;
        this.longArg = longArg;
        this.description = description;
    }

    /**
     * @return the short argument version.
     */
    protected String getShortArg() {
        return shortArg;
    }

    /**
     * @return the long argument version.
     */
    protected String getLongArg() {
        return longArg;
    }

    /**
     * @return the argument description.
     */
    protected String getDescription() {
        return description;
    }

}

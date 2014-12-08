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

public enum CameraOption {
    width("-w", "--width", "Set image width <size>"),
    height("-h", "--height", "Set image height <size>"),
    quality("-q", "--quality", "Set jpeg quality <0 to 100>"),
    raw("-r", "--raw", "Add raw bayer data to jpeg metadata"),
    output("-o", "--output", "Output filename <filename> (to write to stdout, use '-o -'). If not specified, no file is saved"),
    verbose("-v", "--verbose", "Output verbose information during run"),
    timeout("-t", "--timeout", "Time (in ms) before takes picture and shuts down (if not specified, set to 5s)"),
    thumb("-th", "--thumb", "Set thumbnail parameters (x:y:quality)"),
    demo("-d", "--demo", "Run a demo mode (cycle through range of camera options, no capture)"),
    encoding("-e", "--encoding", "Encoding to use for output file (jpg, bmp, gif, png)"),
    exif("-x", "--exif", "EXIF tag to apply to captures (format as 'key=value')"),
    timelapse("-tl", "--timelapse", "Timelapse mode. Takes a picture every <t>ms"),
    sharpness("-sh", "--sharpness", "Set image sharpness (-100 to 100)"),
    contrast("-co", "--contrast", "Set image contrast (-100 to 100)"),
    brightness("-br", "--brightness", "Set image brightness (0 to 100)"),
    saturation("-sa", "--saturation", "Set image saturation (-100 to 100)"),
    ISO("-ISO", "--ISO", "Set capture ISO"),
    vstab("-vs", "--vstab", "Turn on video stablisation"),
    ev("-ev", "--ev", "Set EV compensation"),
    exposure("-ex", "--exposure", "Set exposure mode (see Notes)"),
    awb("-awb", "--awb", "Set AWB mode (see Notes)"),
    awbgains("-awbg", "--awbgains", "Set AWB gains - AWB mode must be off"),
    imxfx("-ifx", "--imxfx", "Set image effect (see Notes)"),
    colfx("-cfx", "--colfx", "Set colour effect (U:V)"),
    metering("-mm", "--metering", "Set metering mode (see Notes)"),
    rotation("-rot", "--rotation", "Set image rotation (0-359)"),
    nopreview("-n", "--nopreview", "Do not display a preview window"),
    burst("-bm", "--burst", "Enable 'burst capture mode'"),
    shutter("-ss", "--shutter", "Set shutter speed in microseconds"),
    hflip("-hf", "--hflip", "Set horizontal flip"),
    vflip("-vf", "--vflip", "Set vertical flip");

    public final String shortArg;

    public final String longArg;

    public final String description;

    private CameraOption(String shortArg, String longArg, String description) {
        this.shortArg = shortArg;
        this.longArg = longArg;
        this.description = description;
    }

}

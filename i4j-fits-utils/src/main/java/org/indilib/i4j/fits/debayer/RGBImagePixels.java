package org.indilib.i4j.fits.debayer;

/*
 * #%L
 * INDI for Java Utilities for the fits image format
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

class RGBImagePixels {

    private ImagePixels red;

    public ImagePixels getRed() {
        return red;
    }

    public ImagePixels getBlue() {
        return blue;
    }

    public ImagePixels getGreen() {
        return green;
    }

    public void setRed(ImagePixels red) {
        this.red = red;
    }

    public void setBlue(ImagePixels blue) {
        this.blue = blue;
    }

    public void setGreen(ImagePixels green) {
        this.green = green;
    }

    private ImagePixels blue;

    private ImagePixels green;

}

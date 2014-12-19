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

import static org.indilib.i4j.fits.debayer.DebayerRowOrder.*;

/**
 * This class is a refactored class from a JImage plugin. originating:
 * http://www.umanitoba.ca/faculties/science/astronomy/jwest/plugins.html all
 * credit for the debayering algorithms go there.
 * 
 * @author Richard van Nieuwnehoven
 */
public class DebayerImage {

    ImagePixels ip;

    private int width;

    private int height;

    public void run(ImagePixels ip) {
        RGBImagePixels rgb;

        DebayerRowOrder row_order = DebayerRowOrder.BGBG;
        int algorithm = 0;

        if (algorithm == 0)
            rgb = new ReplicateDebayerAlgorithm().decode(row_order, ip);
        else if (algorithm == 1)
            rgb = new AverageDebayerAlgorithm().decode(row_order, ip);
        else if (algorithm == 2)
            rgb = new SmoothDebayerAlgorithm().decode(row_order, ip);
        else if (algorithm == 3)
            rgb = new AdaptiveDebayerAlgorithm().decode(row_order, ip);

    }
}

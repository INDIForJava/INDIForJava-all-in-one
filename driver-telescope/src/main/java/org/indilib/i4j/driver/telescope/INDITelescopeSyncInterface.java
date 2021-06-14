package org.indilib.i4j.driver.telescope;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

/**
 * If a telescope driver supports the sync meganism, it should implement this
 * interface.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDITelescopeSyncInterface {

    /**
     * sync the current coordinates.
     * 
     * @param ra
     *            the right ascension of the goto point in space
     * @param dec
     *            the declination of the point in space
     * @return true if successful.
     */
    boolean sync(double ra, double dec);

}

package org.indilib.i4j.driver.ccd;

/*
 * #%L
 * INDI for Java Guider extention - inactive
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

public interface INDIGuiderInterface {

    /**
     * Guide easward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideEast(double ms);

    /**
     * Guide northward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return True if successful, false otherwise.
     */
    boolean guideNorth(double ms);

    /**
     * Guide southward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideSouth(double ms);

    /**
     * Guide westward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideWest(double ms);

}

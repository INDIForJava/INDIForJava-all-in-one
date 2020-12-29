package org.indilib.i4j.driver.telescope.alignment;

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
 * The math plugin interface for the alignment. all implementors can calculate
 * conversions between the telescope vector and the celestrial coordinates.
 * (Both directions)
 * 
 * @author Richard van Nieuwenhoven
 */
public interface IMathPlugin {

    /**
     * create the new math plugin.
     */
    void create();

    /**
     * destroy the math plugin.
     */
    void destroy();

    /**
     * Get the approximate alignment of the mount.
     * 
     * @return the approximate alignment.
     */
    MountAlignment getApproximateAlignment();

    /**
     * @return the identification of this plugin.
     */
    String id();

    /**
     * Initialise or re-initialise the math plugin. Re-reading the in memory
     * database as necessary.
     * 
     * @param inMemoryDatabase
     *            the database to use
     * @return true if successful
     */
    boolean initialise(InMemoryDatabase inMemoryDatabase);

    /**
     * @return the human readable name of the plugin.
     */
    String name();

    /**
     * the approximate alignment of the mount.
     * 
     * @param approximateAlignment
     *            the approximate alignment
     */
    void setApproximateAlignment(MountAlignment approximateAlignment);

    /**
     * Get the alignment corrected telescope pointing direction for the supplied
     * celestial coordinates.
     * 
     * @param rightAscension
     *            Right Ascension (Decimal Hours).
     * @param declination
     *            Declination (Decimal Degrees).
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param apparentTelescopeDirectionVector
     *            Parameter to receive the corrected telescope direction
     * @return True if successful
     */
    boolean transformCelestialToTelescope(double rightAscension, double declination, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector);

    /**
     * Get the true celestial coordinates for the supplied telescope pointing
     * direction.
     * 
     * @param apparentTelescopeDirectionVector
     *            the telescope direction
     * @param julianOffset
     *            to be applied to the current julian date.
     * @param rightAscension
     *            Parameter to receive the Right Ascension (Decimal Hours).
     * @param declination
     *            Parameter to receive the Declination (Decimal Degrees).
     * @return True if successful
     */
    boolean transformTelescopeToCelestial(TelescopeDirectionVector apparentTelescopeDirectionVector, double julianOffset, DoubleRef rightAscension, DoubleRef declination);

}

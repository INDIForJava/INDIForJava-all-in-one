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

public interface IMathPlugin {

    void create();

    void destroy();

    // Public methods
    // / \brief Get the approximate alognment of the mount
    // / \return the approximate alignment
    MountAlignment getApproximateMountAlignment();

    String id();

    // / \brief Initialise or re-initialise the math plugin. Re-reading the in
    // memory database as necessary.
    // / \return True if successful
    boolean initialise(InMemoryDatabase inMemoryDatabase);

    String name();

    void setApproximateMountAlignment(MountAlignment approximateAlignment);

    // / \brief Get the alignment corrected telescope pointing direction for the
    // supplied celestial coordinates
    // / \param[in] RightAscension Right Ascension (Decimal Hours).
    // / \param[in] Declination Declination (Decimal Degrees).
    // / \param[in] JulianOffset to be applied to the current julian date.
    // / \param[out] ApparentTelescopeDirectionVector Parameter to receive the
    // corrected telescope direction
    // / \return True if successful
    boolean transformCelestialToTelescope(double rightAscension, double declination, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector);

    // / \brief Get the true celestial coordinates for the supplied telescope
    // pointing direction
    // / \param[in] ApparentTelescopeDirectionVector the telescope direction
    // / \param[out] RightAscension Parameter to receive the Right Ascension
    // (Decimal Hours).
    // / \param[out] Declination Parameter to receive the Declination (Decimal
    // Degrees).
    // / \return True if successful
    boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination);

}

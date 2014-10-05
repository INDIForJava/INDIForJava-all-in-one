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

    MountAlignment getApproximateMountAlignment();

    String id();

    boolean initialise(InMemoryDatabase inMemoryDatabase);

    String name();

    void setApproximateMountAlignment(InMemoryDatabase inMemoryDatabase);

    boolean transformCelestialToTelescope(double d, double e, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector);

    boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination);

}

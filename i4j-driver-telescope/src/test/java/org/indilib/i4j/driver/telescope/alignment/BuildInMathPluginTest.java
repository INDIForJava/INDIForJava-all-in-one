package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

import java.io.File;

import org.junit.Test;

public class BuildInMathPluginTest {

    @Test
    public void testMathPlungin1() {
        System.setProperty("user.home", new File("target").getAbsolutePath());
        IMathPlugin plugin = new BuiltInMathPlugin();
        plugin.create();
        plugin.initialise(new InMemoryDatabase());
        plugin.setApproximateAlignment(MountAlignment.NORTH_CELESTIAL_POLE);
        // plugin.transformCelestialToTelescope(rightAscension, declination,
        // julianOffset, apparentTelescopeDirectionVector)
    }
}

package org.indilib.i4j.driver.telescope;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionNS;
import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionWE;

/**
 * This interface must be provided by the telescope supporting movements.
 *
 * @author Richard van Nieuwenhoven
 */
public interface INDITelescopeMoveInterface {

    /**
     * North/South motion. implementors should move the scope accordingly.
     *
     * @param dir The direction to move to
     * @return true is successful
     */
    boolean moveNS(TelescopeMotionNS dir);

    /**
     * West/East motion. implementors should move the scope accordingly.
     *
     * @param dir The direction to move to
     * @return true is successful
     */
    boolean moveWE(TelescopeMotionWE dir);

    /**
     * update the movement will be called periodically.
     *
     * @param current the current direction, adapt this value to the new target
     *                position.
     * @param rate    the move rate in arcminutes.
     */
    void update(INDIDirection current, double rate);

}

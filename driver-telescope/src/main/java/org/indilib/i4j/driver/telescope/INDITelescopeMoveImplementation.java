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

import static org.indilib.i4j.Constants.PropertyStates.IDLE;

/**
 * This is the standard implementation of scope movements, if the scope supports
 * them.
 *
 * @author Richard van Nieuwenhoven
 */
public class INDITelescopeMoveImplementation implements INDITelescopeMoveInterface {

    /**
     * multiplier to create 360 degrees from 24 hours.
     */
    private static final double HOUR_TO_DEGREE = 15d;
    /**
     * the move extention to access the properties.
     */
    private final INDITelescopeMoveExtension moveExtension;
    /**
     * last move motion in the North/south axis.
     */
    private TelescopeMotionNS moveNSlastMotion = null;

    /**
     * last move motion in the west/east axis.
     */
    private TelescopeMotionWE moveWElastMotion = null;

    /**
     * Contructor of the standard move implementation.
     *
     * @param moveExtension the move extention to access the properties.
     */
    public INDITelescopeMoveImplementation(INDITelescopeMoveExtension moveExtension) {
        this.moveExtension = moveExtension;
    }

    @Override
    public boolean moveNS(TelescopeMotionNS dir) {

        switch (dir) {
            case MOTION_NORTH:
                if (moveNSlastMotion != TelescopeMotionNS.MOTION_NORTH) {
                    moveNSlastMotion = TelescopeMotionNS.MOTION_NORTH;
                } else {
                    moveNSlastMotion = null;
                    moveExtension.getMovementNSS().resetAllSwitches();
                    moveExtension.getMovementNSS().setState(IDLE);
                    moveExtension.updateProperty(moveExtension.getMovementNSS());
                }
                break;

            case MOTION_SOUTH:
                if (moveNSlastMotion != TelescopeMotionNS.MOTION_SOUTH) {
                    moveNSlastMotion = TelescopeMotionNS.MOTION_SOUTH;
                } else {
                    moveNSlastMotion = null;
                    moveExtension.getMovementNSS().resetAllSwitches();
                    moveExtension.getMovementNSS().setState(IDLE);
                    moveExtension.updateProperty(moveExtension.getMovementNSS());
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public boolean moveWE(TelescopeMotionWE dir) {
        switch (dir) {
            case MOTION_WEST:
                if (moveWElastMotion != TelescopeMotionWE.MOTION_WEST) {
                    moveWElastMotion = TelescopeMotionWE.MOTION_WEST;
                } else {
                    moveExtension.getMovementWES().resetAllSwitches();
                    moveExtension.getMovementWES().setState(IDLE);
                    moveExtension.updateProperty(moveExtension.getMovementWES());
                }
                break;

            case MOTION_EAST:
                if (moveWElastMotion != TelescopeMotionWE.MOTION_EAST) {
                    moveWElastMotion = TelescopeMotionWE.MOTION_EAST;
                } else {
                    moveExtension.getMovementWES().resetAllSwitches();
                    moveExtension.getMovementWES().setState(IDLE);
                    moveExtension.updateProperty(moveExtension.getMovementWES());
                }
                break;
            default:
                break;
        }

        return true;
    }

    @Override
    public void update(INDIDirection current, double rate) {
        if (moveExtension.isMoveNorth()) {
            current.addDec(rate);
        } else if (moveExtension.isMoveSouth()) {
            current.addDec(-rate);
        }
        if (moveExtension.isMoveWest()) {
            current.addRa(rate / HOUR_TO_DEGREE);
        } else if (moveExtension.isMoveEast()) {
            current.addRa(-rate / HOUR_TO_DEGREE);
        }
    }
}

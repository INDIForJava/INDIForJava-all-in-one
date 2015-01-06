package org.indilib.i4j.driver.telescope;

import static org.indilib.i4j.Constants.PropertyStates.IDLE;

import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionNS;
import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionWE;

/**
 * This is the standard implementation of scope movements, if the scope supports
 * them.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDITelescopeMoveImplementation implements INDITelescopeMoveInterface {

    /**
     * the move extention to access the properties.
     */
    private final INDITelescopeMoveExtension moveExtension;

    /**
     * multiplier to create 360 degrees from 24 hours.
     */
    private static final double HOUR_TO_DEGREE = 15d;

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
     * @param moveExtension
     *            the move extention to access the properties.
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

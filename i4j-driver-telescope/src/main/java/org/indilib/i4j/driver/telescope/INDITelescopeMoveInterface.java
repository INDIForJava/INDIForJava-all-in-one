package org.indilib.i4j.driver.telescope;

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
     * @param dir
     *            The direction to move to
     * @return true is successful
     */
    boolean moveNS(TelescopeMotionNS dir);

    /**
     * West/East motion. implementors should move the scope accordingly.
     * 
     * @param dir
     *            The direction to move to
     * @return true is successful
     */
    boolean moveWE(TelescopeMotionWE dir);

    /**
     * update the movement will be called periodically.
     * 
     * @param current
     *            the current direction, adapt this value to the new target
     *            position.
     * @param rate
     *            the move rate in arcminutes.
     */
    void update(INDIDirection current, double rate);

}

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

import static org.indilib.i4j.Constants.PropertyStates.BUSY;
import static org.indilib.i4j.Constants.PropertyStates.IDLE;
import static org.indilib.i4j.Constants.PropertyStates.OK;

import java.util.Date;

import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionNS;
import org.indilib.i4j.driver.telescope.INDITelescope.TelescopeMotionWE;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This extention handles the basic remote manual movement of the scope. it
 * receives directions over the rate and direction buttons and adapts the scope
 * acourdingly.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDITelescopeMoveExtension extends INDIDriverExtension<INDITelescope> {

    /**
     * Default movement implementation that does only report that movements are
     * not suppored.
     */
    private class DefaulMoveImplementation implements INDITelescopeMoveInterface {

        @Override
        public boolean moveNS(TelescopeMotionNS dir) {
            LOG.error("Mount does not support North/South motion.");
            getMovementNSS().resetAllSwitches();
            getMovementNSS().setState(IDLE);
            updateProperty(getMovementNSS(), "Mount does not support North/South motion.");
            return false;
        }

        @Override
        public boolean moveWE(TelescopeMotionWE dir) {
            LOG.error("Mount does not support West/East motion.");
            getMovementWES().resetAllSwitches();
            getMovementWES().setState(IDLE);
            updateProperty(getMovementWES(), "Mount does not support West/East motion.");
            return false;
        }

        @Override
        public void update(INDIDirection current, double rate) {
            // not supported so no movement
        }
    }

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDITelescopeMoveExtension.class);

    /**
     * The property tab for the motion controls.
     */
    protected static final String MOTION_TAB = "Motion";

    /**
     * Speed of the telescope motion in arc minutes / second.
     */
    @InjectProperty(name = "TELESCOPE_MOTION_RATE", label = "Motion rate", state = OK, group = MOTION_TAB)
    protected INDINumberProperty movementRateP;

    /**
     * Speed of the telescope motion in arc minutes / second.
     */
    @InjectElement(name = "MOTION_RATE", label = "Motion rate ", minimum = 0, maximum = 90d, numberFormat = "%010.6m", numberValue = 5d)
    protected INDINumberElement movementRate;

    /**
     * Telescope motion buttons, to move the pointing position over the
     * north/south axis.
     */
    @InjectProperty(name = "TELESCOPE_MOTION_NS", label = "North/South", group = MOTION_TAB)
    protected INDISwitchProperty movementNSS;

    /**
     * move the scope pointing position to the north .
     */
    @InjectElement(name = "MOTION_NORTH", label = "North")
    protected INDISwitchElement movementNSSNorth;

    /**
     * move the scope pointing position to the south.
     */
    @InjectElement(name = "MOTION_SOUTH", label = "South")
    protected INDISwitchElement movementNSSSouth;

    /**
     * Telescope motion buttons, to move the pointing position over the
     * west/east axis.
     */
    @InjectProperty(name = "TELESCOPE_MOTION_WE", label = "West/East", group = MOTION_TAB)
    protected INDISwitchProperty movementWES;

    /**
     * move the scope pointing position to the west.
     */
    @InjectElement(name = "MOTION_WEST", label = "West")
    protected INDISwitchElement movementWESWest;

    /**
     * move the scope pointing position to the east.
     */
    @InjectElement(name = "MOTION_EAST", label = "East")
    protected INDISwitchElement movementWESEast;

    /**
     * the real doing of the moveing is done here.
     */
    protected INDITelescopeMoveInterface moveImpl;

    /**
     * standard constructor for the move extention.
     * 
     * @param driver
     *            the telescope to with this extention belongs.
     */
    public INDITelescopeMoveExtension(INDITelescope driver) {
        super(driver);
        movementNSS.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newMovementNSSValue(elementsAndValues);
            }
        });
        movementWES.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newMovementWESValue(elementsAndValues);
            }
        });
        movementRateP.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                property.setState(OK);
                property.setValues(elementsAndValues);
                updateProperty(property);
            }
        });
        if (this.driver instanceof INDITelescopeMoveInterface) {
            moveImpl = (INDITelescopeMoveInterface) this.driver;
        } else {
            moveImpl = new DefaulMoveImplementation();
        }
    }

    /**
     * new values where send from the client for the move north/south property.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newMovementNSSValue(INDISwitchElementAndValue[] elementsAndValues) {
        movementNSS.setValues(elementsAndValues);
        movementNSS.setState(BUSY);
        if (movementNSSNorth.isOn()) {
            moveImpl.moveNS(TelescopeMotionNS.MOTION_NORTH);
        } else {
            moveImpl.moveNS(TelescopeMotionNS.MOTION_SOUTH);
        }
    }

    /**
     * new values where send from the client for the move west/east property.
     * 
     * @param elementsAndValues
     *            The new Elements and Values
     */
    private void newMovementWESValue(INDISwitchElementAndValue[] elementsAndValues) {
        movementWES.setValues(elementsAndValues);
        movementWES.setState(BUSY);
        if (movementWESWest.isOn()) {
            moveImpl.moveWE(TelescopeMotionWE.MOTION_WEST);
        } else {
            moveImpl.moveWE(TelescopeMotionWE.MOTION_EAST);

        }
    }

    @Override
    public void connect() {
        super.connect();
        addProperty(movementRateP);
        addProperty(movementNSS);
        addProperty(movementWES);
    }

    @Override
    public void disconnect() {
        super.disconnect();
        removeProperty(movementRateP);
        removeProperty(movementNSS);
        removeProperty(movementWES);
    }

    /**
     * abort all movements!
     */
    public void abort() {
        if (movementWES.getState() == BUSY) {
            movementWES.resetAllSwitches();
            movementWES.setState(IDLE);
            updateProperty(movementWES);
        }
        if (movementNSS.getState() == BUSY) {
            movementNSS.resetAllSwitches();
            movementNSS.setState(IDLE);
            updateProperty(movementNSS);
        }
    }

    /**
     * @return property for the Speed of the telescope motion in arc minutes /
     *         second.
     */
    public INDINumberProperty getMovementRateP() {
        return movementRateP;
    }

    /**
     * @return element for the Speed of the telescope motion in arc minutes /
     *         second.
     */
    public INDINumberElement getMovementRate() {
        return movementRate;
    }

    /**
     * @return Telescope motion buttons, to move the pointing position over the
     *         north/south axis.
     */
    public INDISwitchProperty getMovementNSS() {
        return movementNSS;
    }

    /**
     * @return Telescope motion buttons, to move the pointing position over the
     *         west/east axis.
     */
    public INDISwitchProperty getMovementWES() {
        return movementWES;
    }

    /**
     * set the move implementation the move extention should use.
     * 
     * @param moveImpl
     *            the implementation
     */
    public void setMoveImpl(INDITelescopeMoveInterface moveImpl) {
        if (moveImpl == null) {
            this.moveImpl = new DefaulMoveImplementation();
        }
        this.moveImpl = moveImpl;
    }

    /**
     * update the movement, telescopes should call this in the read interfall.
     * 
     * @param current
     *            the current direction, adapt this value to the new target
     *            position.
     */
    public void update(INDIDirection current) {
        moveImpl.update(current, movementRate.getValue());
    }

    /**
     * @return true if the scope should move south.
     */
    public boolean isMoveSouth() {
        return movementNSS.getState() == BUSY && movementNSSSouth.isOn();
    }

    /**
     * @return true if the scope should move north.
     */
    public boolean isMoveNorth() {
        return movementNSS.getState() == BUSY && movementNSSNorth.isOn();
    }

    /**
     * @return true if the scope should move west.
     */
    public boolean isMoveWest() {
        return movementWES.getState() == BUSY && movementWESWest.isOn();
    }

    /**
     * @return true if the scope should move east.
     */
    public boolean isMoveEast() {
        return movementWES.getState() == BUSY && movementWESEast.isOn();
    }

    /**
     * @return should the scope move in any direction?
     */
    public boolean hasMoveRequest() {
        return movementNSS.getState() == BUSY && movementNSS.getSelectedCount() > 0 || //
                movementWES.getState() == BUSY && movementWES.getSelectedCount() > 0;
    }
}

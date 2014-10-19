package org.indilib.i4j.driver.telescope;

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

import static org.indilib.i4j.Constants.PropertyStates.IDLE;

import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;

/**
 * This park extension should be enabled for every telescope that supports a
 * "park" position. The telescope-driver should implement the
 * {@link INDITelescopeParkInterface} if it is supported.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDITelescopeParkExtension extends INDIDriverExtension<INDITelescope> {

    /**
     * the parking property.
     */
    @InjectProperty(name = "TELESCOPE_PARK", label = "Park", group = INDIDriver.GROUP_MAIN_CONTROL)
    private INDISwitchProperty park;

    /**
     * the element to activate the park command.
     */
    @InjectElement(name = "PARK", label = "Park")
    private INDISwitchElement parkElement;

    /**
     * who to call if the telescope receives a request to park.
     */
    private INDITelescopeParkInterface parkInterface;

    /**
     * is the telescope parked at the moment?.
     */
    private boolean parked;

    /**
     * Constructor of the part extension, you should realy know what you are
     * doing if you call this yourself. Better to let it be used by the
     * injector.
     * 
     * @param telecopeDriver
     *            the telescope driver to attact this extention to.
     */
    public INDITelescopeParkExtension(INDITelescope telecopeDriver) {
        super(telecopeDriver);
        if (!isActive()) {
            return;
        }
        park.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                parked = true;
                parkInterface.park();
            }
        });
        parkInterface = (INDITelescopeParkInterface) telecopeDriver;
    }

    @Override
    public void connect() {
        if (!isActive()) {
            return;
        }
        addProperty(park);
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        removeProperty(park);
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDITelescopeParkInterface;
    }

    /**
     * @return are we busy parking?
     */
    public boolean isBusy() {
        if (!isActive()) {
            return false;
        }
        return park.getState() == PropertyStates.BUSY;
    }

    /**
     * any action is stopped returning to idle state.
     */
    public void setIdle() {
        if (!isActive()) {
            return;
        }
        this.park.setState(IDLE);
        park.resetAllSwitches();
        updateProperty(this.park);
    }

    /**
     * Reset the current state to not busy.
     */
    public void setNotBussy() {
        if (!isActive()) {
            return;
        }
        if (this.park.getState() == PropertyStates.BUSY) {
            this.park.setState(IDLE);
        }

    }

    /**
     * @return is the telescope parked at the moment?
     */
    public boolean isParked() {
        return parked;
    }

    /**
     * set the telescope as parked.
     * 
     * @param parked
     *            the new parked state.
     */
    public void setParked(boolean parked) {
        this.parked = parked;
    }
}

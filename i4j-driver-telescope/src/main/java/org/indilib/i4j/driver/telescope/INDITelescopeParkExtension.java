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
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;

public class INDITelescopeParkExtension extends INDIDriverExtension<INDITelescope> {

    @InjectProperty(name = "TELESCOPE_PARK", label = "Park", group = INDITelescope.MAIN_CONTROL_TAB)
    private INDISwitchProperty park;

    @InjectElement(name = "PARK", label = "Park")
    private INDISwitchElement parkElement;

    private INDITelescopeParkInterface parkInterface;

    public INDITelescopeParkExtension(INDITelescope telecopeDriver) {
        super(telecopeDriver);
        if (!isActive()) {
            return;
        }
        park.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
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
        driver.addProperty(park);
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        driver.removeProperty(park);
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDITelescopeParkInterface;
    }

    public boolean isBusy() {
        if (!isActive()) {
            return false;
        }
        return park.getState() == PropertyStates.BUSY;
    }

    public void setIdle() {
        if (!isActive()) {
            return;
        }
        this.park.setState(IDLE);
        park.resetAllSwitches();
        driver.updateProperty(this.park);
    }

    public void setNotBussy() {
        if (!isActive()) {
            return;
        }
        if (this.park.getState() == PropertyStates.BUSY) {
            this.park.setState(IDLE);
        }

    }

}

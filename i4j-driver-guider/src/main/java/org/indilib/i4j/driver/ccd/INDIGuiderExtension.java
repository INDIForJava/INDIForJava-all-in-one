package org.indilib.i4j.driver.ccd;

/*
 * #%L
 * INDI for Java Guider extention - inactive
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

import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.NumberEvent;

public class INDIGuiderExtension extends INDIDriverExtension<INDIDriver> {

    @InjectProperty(name = "TELESCOPE_TIMED_GUIDE_NS", label = "Guide North/South")
    private INDINumberProperty guideNS;

    @InjectElement(name = "TIMED_GUIDE_N", label = "North (msec)", maximum = 60000d, step = 10d)
    private INDINumberElement guideNorth;

    @InjectElement(name = "TIMED_GUIDE_S", label = "South (msec)", maximum = 60000d, step = 10d)
    private INDINumberElement guideSouth;

    @InjectProperty(name = "TELESCOPE_TIMED_GUIDE_WE", label = "Guide East/West")
    private INDINumberProperty guideWE;

    @InjectElement(name = "TIMED_GUIDE_E", label = "East (msec)", maximum = 60000d, step = 10d)
    private INDINumberElement guideWest;

    @InjectElement(name = "TIMED_GUIDE_W", label = "West (msec)", maximum = 60000d, step = 10d)
    private INDINumberElement guideEast;

    private INDIGuiderInterface guiderInterface;

    /**
     * Initilize guider properties. It is recommended to call this function
     * within initProperties() of your primary device
     * 
     * @param deviceName
     *            Name of the primary device
     * @param groupName
     *            Group or tab name to be used to define guider properties.
     */
    public INDIGuiderExtension(INDIDriver parentDriver) {
        super(parentDriver);
        guideNS.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {

                // We are being asked to send a guide pulse north/south on the
                // st4
                // port
                property.setValues(elementsAndValues);
                boolean rc = false;
                if (guideNorth.getValue() != 0d) {
                    guideSouth.setValue(0d);
                    rc = guiderInterface.guideNorth(guideNorth.getValue());
                } else if (guideSouth.getValue() != 0d) {
                    rc = guiderInterface.guideSouth(guideSouth.getValue());
                }

                guideNS.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
                driver.updateProperty(guideNS);
                return;

            }
        });
        guideWE.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                // We are being asked to send a guide pulse north/south on the
                // st4
                // port
                property.setValues(elementsAndValues);
                boolean rc = false;
                if (guideWest.getValue() != 0) {
                    guideEast.setValue(0);
                    rc = guiderInterface.guideEast(guideWest.getValue());
                } else if (guideEast.getValue() != 0)
                    rc = guiderInterface.guideWest(guideEast.getValue());

                guideWE.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
                driver.updateProperty(guideWE);
                return;
            }
        });
    }

    public void setGuiderInterface(INDIGuiderInterface guiderInterface) {
        this.guiderInterface = guiderInterface;
    }

}

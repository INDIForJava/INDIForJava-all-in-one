package org.indilib.i4j.driver.serialswitch;

/*
 * #%L
 * INDI for Java Driver for the cheap serial switches
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.driver.serial.INDISerialPortInterface;
import org.indilib.i4j.properties.INDIStandardElement;
import org.indilib.i4j.properties.INDIStandardProperty;
import org.indilib.i4j.protocol.api.INDIConnection;

public class SerialSwitchDriver extends INDIDriver implements INDIConnectionHandler, INDISerialPortInterface {

    /**
     * the serial connection extention to connect the device.
     */
    @InjectExtension
    private INDISerialPortExtension serialPortExtension;

    /**
     * the switch property representing the relais series.
     */
    @InjectProperty(std = INDIStandardProperty.SWITCH_MODULE, label = "Switches", switchRule = SwitchRules.ANY_OF_MANY)
    private INDISwitchProperty switches;

    /**
     * relais 1.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 1)
    private INDISwitchElement switch1;

    /**
     * relais 2.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 2)
    private INDISwitchElement switch2;

    /**
     * relais 3.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 3)
    private INDISwitchElement switch3;

    /**
     * relais 4.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 4)
    private INDISwitchElement switch4;

    /**
     * relais 5.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 5)
    private INDISwitchElement switch5;

    /**
     * relais 6.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 6)
    private INDISwitchElement switch6;

    /**
     * relais 7.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 7)
    private INDISwitchElement switch7;

    /**
     * relais 8.
     */
    @InjectElement(std = INDIStandardElement.SWITCHn, nIndex = 8)
    private INDISwitchElement switch8;

    /**
     * Constructor for the switch driver.
     * 
     * @param connection
     *            the indi connection to use
     */
    public SerialSwitchDriver(INDIConnection connection) {
        super(connection);
        serialPortExtension.setPortDetailesFixed(false);
        serialPortExtension.setBaudrate(9600);
        serialPortExtension.setDatabits(8);
        serialPortExtension.setStopbits(1);
        serialPortExtension.setParity(0);
        serialPortExtension.setMinimumMillisecondsBetweenCommands(50);
        switches.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                switches.setValues(elementsAndValues);
                switches.setState(PropertyStates.OK);
                sendState();
                updateProperty(switches);
            }
        });
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        addProperty(switches);
        serialPortExtension.open();
        // set to receive mode.
        serialPortExtension.sendByte(0x50, false);
        serialPortExtension.sendByte(0x51, false);
        sendState();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        serialPortExtension.close();
        removeProperty(switches);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * send the switch state to the module.
     */
    private void sendState() {
        int value = 0;
        value = value | (switch1.isOn() ? 1 : 0);
        value = value | (switch2.isOn() ? 2 : 0);
        value = value | (switch3.isOn() ? 4 : 0);
        value = value | (switch4.isOn() ? 8 : 0);
        value = value | (switch5.isOn() ? 16 : 0);
        value = value | (switch6.isOn() ? 32 : 0);
        value = value | (switch7.isOn() ? 64 : 0);
        value = value | (switch8.isOn() ? 128 : 0);
        serialPortExtension.sendByte((byte) value, false);
    }

}

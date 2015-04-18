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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.annotation.InjectExtension;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.driver.serial.INDISerialPortExtension;
import org.indilib.i4j.protocol.api.INDIConnection;

public class SerialSwitchDriver extends INDIDriver implements INDIConnectionHandler {

    @InjectExtension
    private INDISerialPortExtension serialPortExtension;

    protected SerialSwitchDriver(INDIConnection connection) {
        super(connection);
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {

    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }

}

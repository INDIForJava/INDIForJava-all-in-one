package org.indilib.i4j.driver.telescope.nexstar.gt;

/*
 * #%L
 * INDI for Java Driver for the NexStar GT Mount
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.indilib.i4j.driver.serial.INDISerialPortInterface;
import org.indilib.i4j.driver.telescope.INDITelescope;
import org.indilib.i4j.driver.telescope.INDITelescopeSyncInterface;

public class NexStarGt extends INDITelescope implements INDITelescopeSyncInterface, INDISerialPortInterface {

    public NexStarGt(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
    }

    @Override
    public boolean sync(double ra, double dec) {
        return false;
    }

    @Override
    protected boolean abort() {
        return false;
    }

    @Override
    protected void doGoto(double ra, double dec) {

    }

    @Override
    protected void readScopeStatus() {

    }

    @Override
    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
        return false;
    }

    @Override
    protected boolean updateTime(Date utc, double d) {
        return false;
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

}

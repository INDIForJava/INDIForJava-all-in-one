package org.indilib.i4j.driver.telescope.simulator;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

import org.indilib.i4j.driver.telescope.INDITelescope;

/**
 * An telescope simulator that has "real" encoders and can sync. So a template
 * vor all real horizontal based scopes.
 * 
 * @author Richard van Nieuwenhoven
 */
public class TelescopeAltAzSimulator extends INDITelescope {

    /**
     * constructor for the altaz simulator.
     * 
     * @param inputStream
     *            the input stream.
     * @param outputStream
     *            the output stream
     */
    public TelescopeAltAzSimulator(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
    }

    @Override
    protected boolean abort() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected void doGoto(double ra, double dec) {
        // TODO Auto-generated method stub

    }

    @Override
    protected void readScopeStatus() {
        // TODO Auto-generated method stub

    }

    @Override
    protected boolean updateLocation(double targetLat, double targetLong, double targetElev) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    protected boolean updateTime(Date utc, double d) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getName() {
        // TODO Auto-generated method stub
        return null;
    }
}

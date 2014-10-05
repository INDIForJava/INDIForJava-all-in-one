package org.indilib.i4j.driver;

/*
 * #%L
 * INDI for Java Driver Library
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

import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.util.INDIPropertyInjector;

public abstract class INDIDriverExtension<Driver extends INDIDriver> {

    protected final Driver driver;

    public INDIDriverExtension(Driver driver) {
        this.driver = driver;
        if (isActive()) {
            INDIPropertyInjector.initialize(this.driver, this);
        }
    }

    public boolean isActive() {
        return true;
    }

    public void connect() {

    }

    public void disconnect() {

    }

    protected void updateProperty(INDIProperty<?> property) throws INDIException {
        driver.updateProperty(property);
    }

    protected void updateProperty(INDIProperty<?> property, String message) throws INDIException {
        driver.updateProperty(property, message);
    }

    protected void updateProperty(INDIProperty<?> property, boolean updateminmax, String message) throws INDIException {
        driver.updateProperty(property, updateminmax, message);
    }

}

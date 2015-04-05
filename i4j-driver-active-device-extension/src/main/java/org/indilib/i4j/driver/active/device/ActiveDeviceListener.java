package org.indilib.i4j.driver.active.device;

/*
 * #%L
 * INDI for Java Acive Device Extension
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

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDIServerConnection;
import org.indilib.i4j.driver.INDITextElement;

public class ActiveDeviceListener implements INDIDeviceListener, INDIPropertyListener {

    private final INDITextElement element;

    private final INDIServerConnection serverConnection;

    public ActiveDeviceListener(INDITextElement element, INDIServerConnection serverConnection) {
        this.element = element;
        this.serverConnection = serverConnection;
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        property.addINDIPropertyListener(this);

    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        property.removeINDIPropertyListener(this);
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        // now copy the values to the apropitate target properties.

    }

}

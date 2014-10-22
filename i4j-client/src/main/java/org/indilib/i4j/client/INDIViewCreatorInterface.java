package org.indilib.i4j.client;

/*
 * #%L
 * INDI for Java Client Library
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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;

public interface INDIViewCreatorInterface {

    INDIDeviceListener createDeviceListener(INDIDevice indiDevice) throws INDIException;

    INDIElementListener createBlobElementView(INDIBLOBElement indiblobElement, PropertyPermissions permission) throws INDIException;

    INDIElementListener createLightElementView(INDILightElement indiLightElement, PropertyPermissions permission) throws INDIException;

    INDIElementListener createNumberElementView(INDINumberElement indiNumberElement, PropertyPermissions permission) throws INDIException;

    INDIElementListener createSwitchElementView(INDISwitchElement indiSwitchElement, PropertyPermissions permission) throws INDIException;

    INDIElementListener createTextElementView(INDITextElement indiTextElement, PropertyPermissions permission) throws INDIException;

    INDIPropertyListener createBlobPropertyView(INDIBLOBProperty indiProperty) throws INDIException;

    INDIPropertyListener createNumberPropertyView(INDINumberProperty indiProperty) throws INDIException;

    INDIPropertyListener createTextPropertyView(INDITextProperty indiProperty) throws INDIException;

    INDIPropertyListener createSwitchPropertyView(INDISwitchProperty indiProperty) throws INDIException;

    INDIPropertyListener createLightPropertyView(INDILightProperty indiProperty) throws INDIException;

}

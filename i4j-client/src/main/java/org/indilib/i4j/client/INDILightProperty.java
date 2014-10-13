package org.indilib.i4j.client;

/*
 * #%L
 * INDI for Java Client Library
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

import org.indilib.i4j.ClassInstantiator;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * A class representing a INDI Light Property.
 * <p>
 * It implements a listener mechanism to notify changes in its Elements.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 17, 2013
 */
public class INDILightProperty extends INDIProperty {

    /**
     * A UI component that can be used in graphical interfaces for this Light
     * Property.
     */
    private INDIPropertyListener uiComponent;

    /**
     * Constructs an instance of <code>INDILightProperty</code>.
     * <code>INDILightProperty</code>s are not usually directly instantiated.
     * Usually used by <code>INDIDevice</code>.
     * 
     * @param xml
     *            A XML Element <code>&lt;defLightVector&gt;</code> describing
     *            the Property.
     * @param device
     *            The <code>INDIDevice</code> to which this Property belongs.
     */
    protected INDILightProperty(Element xml, INDIDevice device) {
        super(xml, device);

        NodeList list = xml.getElementsByTagName("defLight");

        for (int i = 0; i < list.getLength(); i++) {
            Element child = (Element) list.item(i);

            String name = child.getAttribute("name");

            INDIElement iel = getElement(name);

            if (iel == null) { // Does not exist
                INDILightElement ite = new INDILightElement(child, this);
                addElement(ite);
            }
        }
    }

    @Override
    protected void update(Element el) {
        super.update(el, "oneLight");
    }

    /**
     * Always sets the permission to Read Only as lights may not change.
     * 
     * @param permission
     *            ignored.
     */
    @Override
    protected void setPermission(PropertyPermissions permission) {
        super.setPermission(PropertyPermissions.RO);
    }

    /**
     * Sets the timeout to 0 as lights may not change.
     * 
     * @param timeout
     *            ignored.
     */
    @Override
    protected void setTimeout(int timeout) {
        super.setTimeout(0);
    }

    /**
     * Gets an empty <code>String</code> as Light Properties cannot be changed
     * by clients.
     * 
     * @return "" a empty <code>String</code>
     */
    @Override
    protected String getXMLPropertyChangeInit() {
        return ""; // A light cannot change
    }

    /**
     * Gets an empty <code>String</code> as Light Properties cannot be changed
     * by clients.
     * 
     * @return "" a empty <code>String</code>
     */
    @Override
    protected String getXMLPropertyChangeEnd() {
        return ""; // A light cannot change
    }

    @Override
    public INDIPropertyListener getDefaultUIComponent() throws INDIException {
        if (uiComponent != null) {
            removeINDIPropertyListener(uiComponent);
        }

        Object[] arguments = new Object[]{
            this
        };
        String[] possibleUIClassNames = new String[]{
            "org.indilib.i4j.client.ui.INDIDefaultPropertyPanel",
            "org.indilib.i4j.androidui.INDIDefaultPropertyView"
        };

        try {
            uiComponent = (INDIPropertyListener) ClassInstantiator.instantiate(possibleUIClassNames, arguments);
        } catch (ClassCastException e) {
            throw new INDIException("The UI component is not a valid INDIPropertyListener. Probably a incorrect library in the classpath.");
        }

        addINDIPropertyListener(uiComponent);

        return uiComponent;
    }

    /**
     * Gets a particular Element of this Property by its name.
     * 
     * @param name
     *            The name of the Element to be returned
     * @return The Element of this Property with the given <code>name</code>.
     *         <code>null</code> if there is no Element with that
     *         <code>name</code>.
     */
    @Override
    public INDILightElement getElement(String name) {
        return (INDILightElement) super.getElement(name);
    }
}

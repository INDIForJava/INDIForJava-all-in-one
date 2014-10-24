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

import org.indilib.i4j.Constants;
import static org.indilib.i4j.INDIDateFormat.dateFormat;
import org.indilib.i4j.driver.util.INDIPropertyBuilder;

/**
 * A class representing a INDI Light Property.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.34, November 7, 2013
 */
public class INDILightProperty extends INDIProperty<INDILightElement> {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -3655076563473257286L;

    /**
     * Constructs an instance of a <code>INDILightProperty</code>. Called by its
     * sub-classes. useing the settings from the builder.
     * 
     * @param builder
     *            the builder with all the settings.
     */
    public INDILightProperty(INDIPropertyBuilder<?> builder) {
        super(builder);
    }

    @Override
    public INDILightElement getElement(String name) {
        return (INDILightElement) super.getElement(name);
    }

    @Override
    protected String getXMLPropertyDefinitionInit() {
        String xml =
                "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + dateFormat().getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionInit(String message) {
        String xml =
                "<defLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" label=\"" + getLabel() + "\" group=\"" + getGroup() + "\" state=\""
                        + Constants.getPropertyStateAsString(getState()) + "\" timestamp=\"" + dateFormat().getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertyDefinitionEnd() {
        String xml = "</defLightVector>";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit() {
        String xml =
                "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timestamp=\"" + dateFormat().getCurrentTimestamp() + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetInit(String message) {
        String xml =
                "<setLightVector device=\"" + getDriver().getName() + "\" name=\"" + getName() + "\" state=\"" + Constants.getPropertyStateAsString(getState())
                        + "\" timestamp=\"" + dateFormat().getCurrentTimestamp() + "\" message=\"" + message + "\">";

        return xml;
    }

    @Override
    protected String getXMLPropertySetEnd() {
        String xml = "</setLightVector>";

        return xml;
    }

    @Override
    protected Class<INDILightElement> elementClass() {
        return INDILightElement.class;
    }
}

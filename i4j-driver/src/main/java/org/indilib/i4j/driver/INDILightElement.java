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
import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.w3c.dom.Element;

/**
 * A class representing a INDI Light Element.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.11, March 26, 2012
 */
public class INDILightElement extends INDIElement {

    /**
     * Serialization id.
     */
    private static final long serialVersionUID = -4038203228454293238L;

    /**
     * Current State value for this Light Element.
     */
    private LightStates state;

    /**
     * Constructs an instance of a <code>INDILightElement</code> with the
     * settings from the <code>builder</code>.
     */
    public INDILightElement(INDIElementBuilder<INDILightElement> builder) {
        super(builder);
        this.state = builder.state();
    }

    @Override
    public INDILightProperty getProperty() {
        return (INDILightProperty) super.getProperty();
    }

    @Override
    public LightStates getValue() {
        return state;
    }

    @Override
    public void setValue(Object newValue) {
        LightStates ns = null;
        try {
            ns = (LightStates) newValue;
        } catch (ClassCastException e) {
            throw new IllegalArgumentException("Value for a Light Element must be a INDILightElement.LightStates");
        }

        this.state = ns;
    }

    @Override
    public String getXMLOneElement(boolean includeMinMaxStep) {
        String v = Constants.getLightStateAsString(state);

        String xml = "<oneLight name=\"" + this.getName() + "\">" + v + "</oneLight>";

        return xml;
    }

    @Override
    public String getNameAndValueAsString() {
        return getName() + " - " + getValue();
    }

    @Override
    protected String getXMLDefElement() {
        String v = Constants.getLightStateAsString(state);

        String xml = "<defLight name=\"" + this.getName() + "\" label=\"" + getLabel() + "\">" + v + "</defLight>";

        return xml;
    }

    @Override
    public Object parseOneValue(Element xml) {
        return Constants.parseLightState(xml.getTextContent().trim());
    }
}

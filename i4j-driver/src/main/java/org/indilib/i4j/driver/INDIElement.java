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

import java.io.Serializable;

import org.indilib.i4j.driver.util.INDIElementBuilder;
import org.w3c.dom.Element;

/**
 * A class representing a INDI Element. The subclasses
 * <code>INDIBLOBElement</code>, <code>INDILightElement</code>,
 * <code>INDINumberElement</code>, <code>INDISwitchElement</code> and
 * <code>INDITextElement</code> define the basic Elements that a INDI Property
 * may contain according to the INDI protocol.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.32, July 23, 2013
 */
public abstract class INDIElement implements Serializable {

    /**
     * The name of the Element
     */
    private String name;

    /**
     * The label of the Element
     */
    private String label;

    /**
     * The Property to which this Element belongs.
     */
    private INDIProperty property;

    /**
     * Constructs an instance of <code>INDIElement</code>. Called by its
     * sub-classes. If the <code>label</code> is null, the name is assigned to
     * the label.
     * 
     * @param property
     *            The Property to which this Element belongs.
     * @param name
     *            The name of the Element
     * @param label
     *            The label of the Element if the <code>name</code> * is
     *            <code>null</code>.
     */
    protected INDIElement(INDIProperty property, String name, String label) {
        this.property = property;

        if (name == null) {
            throw new IllegalArgumentException("No name for Element");
        }

        name = name.trim();

        if (name.length() == 0) {
            throw new IllegalArgumentException("No name for Element");
        }

        this.name = name;

        if (label == null) {
            this.label = name;
        } else {
            label = label.trim();

            if (label.length() == 0) {
                this.label = name;
            } else {
                this.label = label;
            }
        }

        property.addElement(this);
    }

    /**
     * Constructs an instance of <code>INDIElement</code> with properties from
     * the builder. Called by its sub-classes.
     */
    @SuppressWarnings("unchecked")
    protected INDIElement(INDIElementBuilder<? extends INDIElement> builder) {
        this.property = builder.indiProperty();
        name = builder.name();
        if (name.length() == 0) {
            throw new IllegalArgumentException("No name for Element");
        }
        this.label = builder.label();
        this.property.addElement(this);
    }

    /**
     * Constructs an instance of <code>INDIElement</code> with a label equal to
     * its name. Called by its sub-classes.
     * 
     * @param property
     *            The Property to which this Element belongs.
     * @param name
     *            The name of the Element. if the <code>name</code> * is
     *            <code>null</code>.
     */
    protected INDIElement(INDIProperty property, String name) {
        this.property = property;

        if (name == null) {
            throw new IllegalArgumentException("No name for Element");
        }

        name = name.trim();

        if (name.length() == 0) {
            throw new IllegalArgumentException("No name for Element");
        }

        this.name = name;

        this.label = name;

        property.addElement(this);
    }

    /**
     * Gets the Property to which this Element belongs.
     * 
     * @return The Property to which this Element belongs
     */
    public INDIProperty getProperty() {
        return property;
    }

    /**
     * Gets the label of the Element.
     * 
     * @return The label of the Element.
     */
    public String getLabel() {
        return label;
    }

    /**
     * Gets the name of the Element.
     * 
     * @return The name of the Element.
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the current value of the Element.
     * 
     * @return The current value of the Element.
     */
    public abstract Object getValue();

    /**
     * Parses a &lt;oneXXX&gt; XML message and gets the desired value in it.
     * 
     * @param xml
     *            The XML element to be parsed.
     * @return The value of the element described in the <code>XML</code>
     *         element.
     */
    public abstract Object parseOneValue(Element xml);

    /**
     * Sets the value of the Element to <code>newValue</code>.
     * 
     * @param newValue
     *            The new value for the Element. if the <code>newValue</code> is
     *            not a valid one for the type of the Element.
     */
    public abstract void setValue(Object newValue);

    /**
     * Gets a &lt;oneXXX&gt; XML string describing the current value of the
     * Element.
     * 
     * @return the &lt;oneXXX&gt; XML string describing the current value of the
     *         Element.
     */
    protected abstract String getXMLOneElement(boolean includeMinMaxStep);

    /**
     * Gets a &lt;defXXX&gt; XML string describing the current value and
     * properties of the Element.
     * 
     * @return The &lt;defXXX&gt; XML string describing the current value and
     *         properties of the Element.
     */
    protected abstract String getXMLDefElement();

    /**
     * Gets the name of the element and its current value
     * 
     * @return a String with the name of the Element and Its Value
     */
    public abstract String getNameAndValueAsString();
}

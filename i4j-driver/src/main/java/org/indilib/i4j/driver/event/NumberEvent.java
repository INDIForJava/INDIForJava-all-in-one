
package org.indilib.i4j.driver.event;

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

import java.util.Date;

import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;

/**
 * This is a convince class the reduce the number of genetics that must be
 * specified for event handler. In this case it handles it for the Number
 * properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class NumberEvent implements IEventHandler<INDINumberProperty, INDINumberElement, Double> {

    /**
     * the current property being changed. do not use it if nor really
     * Necessary, it's much better and readable to use direct references.
     */
    protected INDINumberProperty property;

    @Override
    public final void processNewValue(INDINumberProperty property, Date date, INDIElementAndValue<INDINumberElement, Double>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDINumberElementAndValue[]) elementsAndValues);
    }

    /**
     * the Simplified call without the property. TODO: should we also exclude
     * the date? it is almoust never used.
     * 
     * @param date
     *            the time it was set
     * @param elementsAndValues
     *            the new values for the elements of this property
     */
    public abstract void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues);
}

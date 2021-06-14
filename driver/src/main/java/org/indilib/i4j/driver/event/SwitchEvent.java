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

import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;

/**
 * This is a convince class the reduce the number of genetics that must be
 * specified for event handler. In this case it handles it for the Switch
 * properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public abstract class SwitchEvent implements IEventHandler<INDISwitchProperty, INDISwitchElement, SwitchStatus> {

    /**
     * the log to write messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SwitchEvent.class);

    /**
     * the current property being changed. do not use it if nor really
     * Necessary, it's much better and readable to use direct references.
     */
    protected INDISwitchProperty property;

    @Override
    public final void processNewValue(INDISwitchProperty valueProperty, Date date, INDIElementAndValue<INDISwitchElement, SwitchStatus>[] elementsAndValues) {
        property = valueProperty;
        if (elementsAndValues instanceof INDISwitchElementAndValue[]) {
            processNewValue(date, (INDISwitchElementAndValue[]) elementsAndValues);
        } else {
            LOG.error("illegal value for process new value");
        }
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

    public abstract void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues);
}

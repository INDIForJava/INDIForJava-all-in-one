package org.indilib.i4j.client.ui;

/*
 * #%L
 * INDI for Java Client UI Library
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

/**
 * A panel to represent a <code>INDIProperty</code>.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @see INDIProperty
 */
public abstract class INDIPropertyPanel extends javax.swing.JPanel implements INDIPropertyListener {

    /**
     * serial Version UID.
     */
    private static final long serialVersionUID = -8287205438543609020L;

    /**
     * the property behind the pannel.
     */
    private INDIProperty<?> property;

    /**
     * Creates new form INDIPropertyPanel.
     * 
     * @param property
     *            the property behind the pannel.
     */
    public INDIPropertyPanel(INDIProperty<?> property) {
        this.property = property;
    }

    /**
     * @return the property behind the pannel.
     */
    public INDIProperty<?> getProperty() {
        return property;
    }

    /**
     * check the set button of the pannel.
     */
    protected abstract void checkSetButton();
}

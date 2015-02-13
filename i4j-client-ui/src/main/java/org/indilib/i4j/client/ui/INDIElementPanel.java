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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;

/**
 * A panel to represent a <code>INDIElement</code>.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @see INDIElement
 */
public abstract class INDIElementPanel extends javax.swing.JPanel implements INDIElementListener {

    /**
     * serial Version UID.
     */
    private static final long serialVersionUID = -1306255501095007904L;

    /**
     * is the element changed?
     */
    private boolean changed;

    /**
     * is the element writeable?
     */
    private boolean writable;

    /**
     * the parrent property pannel.
     */
    private INDIPropertyPanel ipp;

    /**
     * Creates new form INDIElementPanel.
     * 
     * @param perm
     *            the permissions for this pannel.
     */
    protected INDIElementPanel(PropertyPermissions perm) {
        if (perm != PropertyPermissions.RO) {
            writable = true;
        } else {
            writable = false;
        }

        ipp = null;

        changed = false;
    }

    /**
     * set the property pannel.
     * 
     * @param parentPropertyPannel
     *            the pannel to set.
     */
    protected void setINDIPropertyPanel(INDIPropertyPanel parentPropertyPannel) {
        ipp = parentPropertyPannel;
    }

    /**
     * check the set button on the pannel.
     */
    protected void checkSetButton() {
        if (ipp != null) {
            ipp.checkSetButton();
        }
    }

    /**
     * set the changed state of the element.
     * 
     * @param changed
     *            the new changed state.
     */
    protected void setChanged(boolean changed) {
        this.changed = changed;
    }

    /**
     * @return true if the element value was changed.
     */
    protected boolean isChanged() {
        return changed;
    }

    /**
     * @return true if the element is writable.
     */
    protected boolean isWritable() {
        return writable;
    }

    /**
     * @return the disired value for the element.
     */
    protected abstract Object getDesiredValue();

    /**
     * @return the element behind this pannel.
     */
    protected abstract INDIElement getElement();

    /**
     * set the error state for this element.
     * 
     * @param erroneous
     *            true if error
     * @param errorMessage
     *            the message connected.
     */
    protected abstract void setError(boolean erroneous, String errorMessage);

    /**
     * @return true if the desired value is erroneous.
     */
    protected abstract boolean isDesiredValueErroneous();

    /**
     * clean the desired value.
     */
    protected abstract void cleanDesiredValue();

    /**
     * @return the size of the element name.
     */
    protected abstract int getNameSize();

    /**
     * set the size for the element name.
     * 
     * @param size
     *            the new size to use.
     */
    protected abstract void setNameSize(int size);
}

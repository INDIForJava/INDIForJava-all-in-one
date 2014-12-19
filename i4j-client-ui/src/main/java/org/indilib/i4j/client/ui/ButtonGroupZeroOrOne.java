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

import javax.swing.ButtonGroup;
import javax.swing.ButtonModel;

/**
 * A class to allow Radio Buttons to have zero elements selected.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class ButtonGroupZeroOrOne extends ButtonGroup {

    /**
     * serial version id.
     */
    private static final long serialVersionUID = 1048010122685624096L;

    @Override
    public void setSelected(ButtonModel m, boolean b) {
        if (b && m != null && m != getSelection()) {
            super.setSelected(m, b);
        } else if (!b && m == getSelection()) {
            clearSelection();
        }
    }
}

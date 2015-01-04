package org.indilib.i4j.client.fx;

/*
 * #%L
 * INDI for Java Client UI Library
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

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.Initializable;

/**
 * abstract class for all javafx controller.
 * 
 * @author Richard van Nieuwenhoven
 * @param <INDIClass>
 *            the indi device/property or element class this controller is used
 *            for.
 */
public abstract class INDIController<INDIClass> implements INDIFxAccess, Initializable {

    /**
     * the indi device/property or element this controller is used for.
     */
    protected INDIClass indi;

    /**
     * subclassed can intercep the moment the controller is connected to the
     * indi class.
     */
    protected void indiConnected() {
    }

    /**
     * the indi device/property or element this controller is used for.
     * 
     * @param indi
     *            indi device/property or element
     */
    public void setIndi(INDIClass indi) {
        this.indi = indi;
        indiConnected();
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }
}

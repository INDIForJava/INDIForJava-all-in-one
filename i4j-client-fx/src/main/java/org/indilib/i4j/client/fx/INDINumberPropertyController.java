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

import javafx.fxml.FXML;

import org.controlsfx.dialog.Dialogs;
import org.indilib.i4j.client.INDINumberProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * controller for number properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDINumberPropertyController extends INDIPropertyController<INDINumberProperty> {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDINumberPropertyController.class);

    /**
     * send the changed value to the indi server.
     */
    @FXML
    private void set() {
        try {
            indi.sendChangesToDriver();
        } catch (Exception e) {
            LOG.error("could not send new value to the server", e);
            Dialogs.create()//
                    .owner(label)//
                    .title("Set error")//
                    .masthead("Could not send the new value to the indi server.")//
                    .message(e.getMessage())//
                    .showError();
        }
    }
}

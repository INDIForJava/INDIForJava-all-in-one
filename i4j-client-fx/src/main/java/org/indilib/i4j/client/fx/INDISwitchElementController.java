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
import javafx.scene.control.ToggleButton;
import javafx.scene.image.ImageView;

import org.controlsfx.dialog.Dialogs;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDISwitchElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for a indi switch element.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDISwitchElementController extends INDIElementController<INDISwitchElement> {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDISwitchElementController.class);

    /**
     * the button representing the switch.
     */
    @FXML
    private ImageView buttonImage;

    @Override
    public void elementChanged(INDIElement element) {
        super.elementChanged(element);
        ((ToggleButton) this.element).setText(element.getLabel());
        String switchStatusToStyle = INDIFxFactory.switchStatusToStyle(((INDISwitchElement) element).getValue());
        if (!buttonImage.getStyleClass().contains(switchStatusToStyle)) {
            buttonImage.getStyleClass().removeAll(INDIFxFactory.STYLE_SWITCH_STATES);
            buttonImage.getStyleClass().add(switchStatusToStyle);
        }
    }

    /**
     * the switch element was clicked. change the disired value and send it to
     * the server.
     */
    @FXML
    public void click() {
        try {
            if (indi.getValue() == SwitchStatus.ON) {
                indi.setDesiredValue(SwitchStatus.OFF);
            } else {
                indi.setDesiredValue(SwitchStatus.ON);
            }
            indi.getProperty().sendChangesToDriver();
        } catch (Exception e) {
            LOG.error("could not send new value to the server", e);
            Dialogs.create()//
                    .owner(buttonImage)//
                    .title("Set error")//
                    .masthead("Could not send the new value to the indi server.")//
                    .message(e.getMessage())//
                    .showError();
        }

    }
}

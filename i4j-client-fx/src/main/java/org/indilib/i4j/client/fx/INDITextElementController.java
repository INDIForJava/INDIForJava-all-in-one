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

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import org.controlsfx.dialog.Dialogs;
import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDITextElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for an indi text element.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDITextElementController extends INDIElementController<INDITextElement> {

    /**
     * A logger for the errors.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDITextElementController.class);

    /**
     * the field holding the desired value.
     */
    @FXML
    private TextField setValue;

    /**
     * the field holding the current value.
     */
    @FXML
    private TextField value;

    /**
     * the current value field was clicked, copy the value to the desired value
     * field and shift the focus there.
     */
    @FXML
    public void clickOnRO() {
        setValue.setText(value.getText());
        setValue.requestFocus();
    }

    @Override
    public void elementChanged(INDIElement element) {
        super.elementChanged(element);
        value.setText(element.getValueAsString());
    }

    @Override
    protected void indiConnected() {
        if (indi.getProperty().getPermission() == PropertyPermissions.RO) {
            setValue.setVisible(false);
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setValue.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                try {
                    indi.setDesiredValue(newValue);
                } catch (Exception e) {
                    LOG.error("set the dired value as requested", e);
                    Dialogs.create()//
                            .owner(setValue)//
                            .title("Set error")//
                            .masthead("Could not set the desierd value.")//
                            .message("the value was probably not a legal value, the value is reset to the current value, see the log for more details")//
                            .showError();
                    setValue.setText(value.getText());
                }
            }
        });
    }
}

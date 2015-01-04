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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDINumberElement;

/**
 * Controller for number elements.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDINumberElementController extends INDIElementController<INDINumberElement> {

    /**
     * the value to set.
     */
    @FXML
    private TextField setValue;

    /**
     * the value provided by the server.
     */
    @FXML
    private TextField value;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        setValue.textProperty().addListener(new ChangeListener<String>() {

            @Override
            public void changed(final ObservableValue<? extends String> observable, final String oldValue, final String newValue) {
                try {
                    if (indi.checkCorrectValue(newValue)) {
                        indi.setDesiredValue(newValue);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    // TODO: set error
                }

            }
        });
    }

    @Override
    protected void indiConnected() {
        if (indi.getProperty().getPermission() == PropertyPermissions.RO) {
            setValue.setVisible(false);
        }
    }

    /**
     * when the server value was clicked, copy the value and focus on the local
     * value.
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
}

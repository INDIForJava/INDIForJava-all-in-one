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

import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIProperty;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class INDIGroupController extends INDIController<String> {

    @FXML
    private StackPane group;

    @FXML
    private Label label;

    @FXML
    private VBox box;

    @Override
    protected void indiConnected() {
        super.indiConnected();
        label.setText(indi);
    }

    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        try {
            box.getChildren().add(((INDIFxAccess) property.getDefaultUIComponent()).getGui(Node.class));
        } catch (INDIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public <T> T getGui(Class<T> clazz) {
        return clazz.cast(group);
    }
}

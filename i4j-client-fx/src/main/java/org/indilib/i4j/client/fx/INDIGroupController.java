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

import java.util.Iterator;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

/**
 * controller class for a group of properties.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIGroupController extends INDIController<String> implements INDIDeviceListener {

    /**
     * the stackpane for the group.
     */
    @FXML
    private StackPane group;

    /**
     * the label at the top of the group.
     */
    @FXML
    private Label label;

    /**
     * the box containing the children (properties).
     */
    @FXML
    private VBox box;

    @Override
    protected void indiConnected() {
        super.indiConnected();
        label.setText(indi);
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        try {
            INDIPropertyListener defaultUIComponent = property.getDefaultUIComponent();
            box.getChildren().add(((INDIFxAccess) defaultUIComponent).getGui(Node.class));
            defaultUIComponent.propertyChanged(property);
        } catch (INDIException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    public <T> T getGui(Class<T> clazz) {
        return clazz.cast(group);
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        Iterator<Node> children = box.getChildren().iterator();
        while (children.hasNext()) {
            INDIPropertyController<?> propertyController = INDIFxFactory.controller(children.next());
            if (propertyController.indi == property) {
                children.remove();
                break;
            }
        }
    }

    /**
     * @return is this group emty?
     */
    public boolean isEmpty() {
        return box.getChildren().isEmpty();
    }

    @Override
    public void messageChanged(INDIDevice device) {

    }
}

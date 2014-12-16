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

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

public abstract class INDIPropertyController<PROPERTYCLASS extends INDIProperty<?>> extends INDIController<PROPERTYCLASS> implements INDIPropertyListener {

    @FXML
    protected ImageView state;

    @FXML
    protected Label label;

    @FXML
    protected Pane elements;

    @FXML
    protected Pane property;

    @Override
    public <T> T getGui(Class<T> clazz) {
        return clazz.cast(property);
    }

    @Override
    protected void indiConnected() {
        super.indiConnected();
        for (INDIElement element : indi) {
            try {
                INDIElementListener defaultUIComponent = element.getDefaultUIComponent();
                addElement(((INDIFxAccess) defaultUIComponent).getGui(Node.class));
                defaultUIComponent.elementChanged(element);
            } catch (INDIException e) {
                e.printStackTrace();
            }
        }
    }

    protected void addElement(Node element) {
        elements.getChildren().add(element);
    }

    @Override
    public void propertyChanged(INDIProperty<?> property) {
        label.setText(property.getLabel());
        String stateToStyle = INDIFxFactory.stateToStyle(property.getState());
        if (!state.getStyleClass().contains(stateToStyle)) {
            state.getStyleClass().removeAll(INDIFxFactory.STYLE_STATES);
            state.getStyleClass().add(stateToStyle);
        }
    }

}

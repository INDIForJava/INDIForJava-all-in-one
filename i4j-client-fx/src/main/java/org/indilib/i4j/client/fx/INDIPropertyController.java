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
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;

import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;

/**
 * abstract property controller for all properties. handles all common tasks.
 * 
 * @author Richard van Nieuwenhoven
 * @param <PROPERTYCLASS>
 *            the type of the iniproperty.
 */
public abstract class INDIPropertyController<PROPERTYCLASS extends INDIProperty<?>> extends INDIController<PROPERTYCLASS> implements INDIPropertyListener {

    /**
     * image representing the state of the property.
     */
    @FXML
    protected ImageView state;

    /**
     * the label with the descriptio of the preoperty.
     */
    @FXML
    protected Label label;

    /**
     * the gui element in witch the elements will be kept.
     */
    @FXML
    protected Pane elements;

    /**
     * the pane for the property itself.
     */
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

    /**
     * add a gui element to the children.
     * 
     * @param element
     *            the element to add.
     */
    protected void addElement(Node element) {
        elements.getChildren().add(element);
    }

    @Override
    public void propertyChanged(INDIProperty<?> indiProperty) {
        label.setText(indiProperty.getLabel());
        String stateToStyle = INDIFxFactory.stateToStyle(indiProperty.getState());
        if (!state.getStyleClass().contains(stateToStyle)) {
            state.getStyleClass().removeAll(INDIFxFactory.STYLE_STATES);
            state.getStyleClass().add(stateToStyle);
        }
    }

}

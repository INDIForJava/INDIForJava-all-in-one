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
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;

import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.client.INDILightElement;

/**
 * Controller for a light element.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDILightElementController extends INDIElementController<INDILightElement> {

    /**
     * The label of the light element.
     */
    @FXML
    private Label label;

    /**
     * the image of the light element.
     */
    @FXML
    private ImageView value;

    @Override
    public void elementChanged(INDIElement element) {
        super.elementChanged(element);
        label.setText(element.getLabel());
        String lightStateToStyle = INDIFxFactory.lightStateToStyle(((INDILightElement) element).getValue());
        if (!value.getStyleClass().contains(lightStateToStyle)) {
            value.getStyleClass().removeAll(INDIFxFactory.LIGHT_STATES);
            value.getStyleClass().add(lightStateToStyle);
        }
    }
}

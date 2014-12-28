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

import java.util.HashMap;
import java.util.Map;

import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.layout.VBox;

import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.fx.INDIFxFactory.FxController;

public class INDIDeviceController extends INDIController<INDIDevice> implements INDIDeviceListener {

    @FXML
    private Tab device;

    @FXML
    private VBox groups;

    private Map<String, FxController<Parent, String, INDIGroupController>> groupMap = new HashMap<>();

    @Override
    protected void indiConnected() {
        device.setText(indi.getName());
        for (INDIProperty<?> property : indi.getPropertiesAsList()) {
            newProperty(indi, property);
        }
    }

    @Override
    public void newProperty(INDIDevice device, INDIProperty<?> property) {
        FxController<Parent, String, INDIGroupController> fxGroup = groupMap.get(property.getGroup());
        if (fxGroup == null) {
            fxGroup = INDIFxFactory.newINDIFxGroup();
            fxGroup.controller.setIndi(property.getGroup());
            groupMap.put(property.getGroup(), fxGroup);
            groups.getChildren().add(fxGroup.fx);
        }
        fxGroup.controller().newProperty(device, property);
    }

    @Override
    public void removeProperty(INDIDevice device, INDIProperty<?> property) {
        FxController<Parent, String, INDIGroupController> fxGroup = groupMap.get(property.getGroup());
        if (fxGroup != null) {
            fxGroup.controller().removeProperty(device, property);
            if (fxGroup.controller().isEmpty()) {
                groupMap.remove(fxGroup);
                groups.getChildren().remove(fxGroup.fx);
            }
        }
    }

    @Override
    public void messageChanged(INDIDevice device) {
        // TODO Auto-generated method stub

    }

    @Override
    public <T> T getGui(Class<T> clazz) {
        return clazz.cast(device);
    }

}

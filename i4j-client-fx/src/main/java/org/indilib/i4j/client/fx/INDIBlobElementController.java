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

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.CheckMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.ScrollEvent;

import javax.imageio.ImageIO;

import nom.tam.fits.Fits;

import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.fits.FitsImage;

public class INDIBlobElementController extends INDIElementController<INDIBLOBElement> {

    @FXML
    Label label;

    @FXML
    ImageView image;

    @FXML
    ScrollPane scrollPane;

    WritableImage fxImage;

    final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    @FXML
    private void show(ActionEvent event) {
        CheckMenuItem show = (CheckMenuItem) event.getSource();
        if (show.isSelected()) {
            image.setImage(fxImage);
            scrollPane.setVisible(true);
        } else {
            image.setImage(null);
            scrollPane.setVisible(false);
        }
    }

    @FXML
    private void save() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setMaxHeight(newValue.doubleValue() / 4d * 3d);
            }
        });
        zoomProperty.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable arg0) {
                image.setFitWidth(zoomProperty.get() * 4);
                // image.setFitHeight(zoomProperty.get() * 4);
            }
        });

        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / 1.1);
                }
                event.consume();
            }
        });
    }

    @Override
    public void elementChanged(INDIElement element) {
        super.elementChanged(element);
        INDIBLOBValue value = ((INDIBLOBElement) element).getValue();

        BufferedImage bufferedImage = null;
        if (value.getFormat().toLowerCase().endsWith("fits")) {
            Fits fitsImage = FitsImage.asImage(value.getBlobData());
            if (fitsImage != null) {
                bufferedImage = FitsImage.asJavaImage(fitsImage);
            }
        } else {
            try {
                bufferedImage = ImageIO.read(new ByteArrayInputStream(value.getBlobData()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (bufferedImage != null) {
            fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            image.setImage(fxImage);
        } else {
            image.setImage(null);
        }
    }
}

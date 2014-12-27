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

import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.GridPane;

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
    private void show() {
        image.setImage(null);

    }

    @FXML
    private void save() {

    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        image.fitWidthProperty().bind(((GridPane) element).widthProperty());
        image.setPreserveRatio(true);
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
            WritableImage fxImage = SwingFXUtils.toFXImage(bufferedImage, null);
            image.setImage(fxImage);
        } else {
            image.setImage(null);
        }
    }
}

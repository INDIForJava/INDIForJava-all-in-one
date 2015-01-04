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
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import javafx.stage.FileChooser;

import javax.imageio.ImageIO;

import nom.tam.fits.Fits;

import org.controlsfx.dialog.Dialogs;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIElement;
import org.indilib.i4j.fits.FitsImage;

/**
 * The gui controller for blob elements.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIBlobElementController extends INDIElementController<INDIBLOBElement> {

    /**
     * vertical multiplier for the image. (keep aspect ratio of 4:3 for viewing)
     */
    private static final double ZOOM_VERTICAL_MULTIPLIER = 3d;

    /**
     * horizontal multiplier for the image. (keep aspect ratio of 4:3 for
     * viewing)
     */
    private static final double ZOOM_HORIZONTAL_MULTIPLIER = 4d;

    /**
     * zoom factor to use when zooming.
     */
    private static final double ZOOM_FACTOR = 1.1;

    /**
     * the blob label.
     */
    @FXML
    private Label label;

    /**
     * the image viewer container.
     */
    @FXML
    private ImageView image;

    /**
     * the scroll pane for the image container, enables zoom and moving.
     */
    @FXML
    private ScrollPane scrollPane;

    /**
     * the from the blob converted to an fx image.
     */
    private WritableImage fxImage;

    /**
     * the file name to use for autosaving images.
     */
    private File autoSaveFileNameTemplate;

    /**
     * property for the zoom factor.
     */
    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    /**
     * the show menu was toggled. (disable or enable the imageviewing depending
     * of the state of the menu item.
     * 
     * @param event
     *            the triggerd event.
     */
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

    /**
     * the current blob must be saved, aske where.
     */
    @FXML
    private void save() {
        if (indi.getValue() != null && indi.getValue().getSize() > 0) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("image" + indi.getValue().getFormat());
            fileChooser.setTitle("Save Blob");
            File fileToStore = fileChooser.showSaveDialog(label.getScene().getWindow());
            byte[] blobData = indi.getValue().getBlobData();
            storeBlobData(fileToStore, blobData);
        } else {
            Dialogs.create()//
                    .owner(label)//
                    .title("No data")//
                    .masthead("No blob data to save.")//
                    .message("No blob received yet, nothing to store.")//
                    .showWarning();
        }
    }

    /**
     * store the blob data to a file.
     * 
     * @param fileToStore
     *            the file to save the data.
     * @param blobData
     *            the blob data to save.
     */
    private void storeBlobData(File fileToStore, byte[] blobData) {
        try {
            FileOutputStream out = new FileOutputStream(fileToStore);
            out.write(blobData);
            out.close();
        } catch (Exception e) {
            Dialogs.create()//
                    .owner(label)//
                    .title("Save Error")//
                    .masthead("Could not save the blob.")//
                    .message(e.getMessage())//
                    .showError();
        }
    }

    /**
     * toggle of the autosave menu and ask for the autosave file name template.
     * if there is a current image store it immediately.
     * 
     * @param event
     *            the event tiggering the menu.
     */
    @FXML
    private void autoSave(ActionEvent event) {
        CheckMenuItem autoSave = (CheckMenuItem) event.getSource();
        if (autoSave.isSelected()) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setInitialFileName("image" + indi.getValue().getFormat());
            fileChooser.setTitle("Auto save blobs");
            autoSaveFileNameTemplate = fileChooser.showSaveDialog(label.getScene().getWindow());
            if (indi.getValue() != null && indi.getValue().getSize() > 0) {
                byte[] blobData = indi.getValue().getBlobData();
                storeBlobData(autoSaveFileNameTemplate, blobData);
            }
        } else {
            autoSaveFileNameTemplate = null;
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        super.initialize(location, resources);
        scrollPane.widthProperty().addListener(new ChangeListener<Number>() {

            @Override
            public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
                scrollPane.setMaxHeight(newValue.doubleValue() / ZOOM_HORIZONTAL_MULTIPLIER * ZOOM_VERTICAL_MULTIPLIER);
            }
        });
        zoomProperty.addListener(new InvalidationListener() {

            @Override
            public void invalidated(Observable arg0) {
                image.setFitWidth(zoomProperty.get() * ZOOM_HORIZONTAL_MULTIPLIER);
            }
        });

        scrollPane.addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {

            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * ZOOM_FACTOR);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / ZOOM_FACTOR);
                }
                event.consume();
            }
        });
    }

    @Override
    public void elementChanged(INDIElement element) {
        super.elementChanged(element);
        INDIBLOBValue value = ((INDIBLOBElement) element).getValue();

        if (autoSaveFileNameTemplate != null && value != null && value.getSize() > 0) {
            int count = 0;
            File currentFileToStore;
            do {
                currentFileToStore = createAutoSaveFileName(value, count);
                count++;
            } while (currentFileToStore.exists());
            storeBlobData(currentFileToStore, value.getBlobData());
        }

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

    /**
     * construct a file name using the template the current date time and a
     * counter.
     * 
     * @param value
     *            the value to save (used for the extention)
     * @param count
     *            the counter to use.
     * @return the hopefully unqie file name
     */
    private File createAutoSaveFileName(INDIBLOBValue value, int count) {
        String name = autoSaveFileNameTemplate.getName();
        if (name.lastIndexOf('.') >= 0) {
            name = name.substring(0, name.lastIndexOf('.'));
        }
        name = name + "_" + new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS").format(new Date()) + "-" + count;
        if (value.getFormat().startsWith(".")) {
            name = name + value.getFormat();
        } else {
            name = name + "." + value.getFormat();
        }
        File currentFileToStore = new File(autoSaveFileNameTemplate.getParent(), name);
        return currentFileToStore;
    }
}

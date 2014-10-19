package org.indilib.i4j.driver.examples;

/*
 * #%L
 * INDI for Java Driver examples
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIBLOBValue;
import org.indilib.i4j.driver.INDIBLOBElement;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A small example Driver that uses the INDI for Java Driver library. It defines
 * two BLOB Properties, two Text Properties and a Switch One. The BLOB
 * Properties will have two images about the weather in Spain and Europe
 * (dinamically downloaded from http://eltiempo.es), and the Text ones will
 * contain the names of them. It will check for updated images every 15 minutes.
 * The Switch Property can be used to ask for the current images (for example
 * once the client connects to the driver).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.3, April 5, 2012
 */
public class INDIElTiempoDriver extends INDIDriver implements Runnable, INDIConnectionHandler {

    /**
     * number of milliseconds in 15 minutes.
     */
    private static final int MILLISECONDS_IN_15_MUNUTES = 15 * 60 * 1000;

    /**
     * buffer size for image reading.
     */
    private static final int BUFFER_SIZE = 65536;

    /**
     * property timeout to use.
     */
    private static final int TIMEOUT_IN_SECONDS = 3;

    /**
     * fixed string position 5.
     */
    private static final int FIXED_STRING_POSITION_5 = 5;

    /**
     * fixed string position 7.
     */
    private static final int FIXED_STRING_POSITION_7 = 7;

    /**
     * fixed string position 9.
     */
    private static final int FIXED_STRING_POSITION_9 = 9;

    /**
     * fixed string position 11.
     */
    private static final int FIXED_STRING_POSITION_11 = 11;

    /**
     * fixed string position 13.
     */
    private static final int FIXED_STRING_POSITION_13 = 13;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(INDIElTiempoDriver.class);

    /*
     * The properties
     */
    /**
     * an image element.
     */
    private INDISwitchElement sendImage;

    /**
     * an send property.
     */
    private INDISwitchProperty send;

    /**
     * an spain image property.
     */
    private INDIBLOBProperty spainImageProp;

    /**
     * an spain image element.
     */
    private INDIBLOBElement spainImageElem;

    /**
     * an spain image name property.
     */
    private INDITextProperty spainImageNameProp;

    /**
     * an spain image name element.
     */
    private INDITextElement spainImageNameElem;

    /**
     * europe image property.
     */
    private INDIBLOBProperty europeImageProp;

    /**
     * europe image element.
     */
    private INDIBLOBElement europeImageElem;

    /**
     * europe image name property.
     */
    private INDITextProperty europeImageNameProp;

    /**
     * europe image name element.
     */
    private INDITextElement europeImageNameElem;

    /**
     * The thread that continuously reads images and sends them back tothe
     * clients.
     */
    private Thread runningThread;

    /**
     * A signal to stop the thread.
     */
    private boolean stop;

    /**
     * Initializes the driver. It creates the Proerties and its Elements.
     * 
     * @param inputStream
     *            The input stream from which the Driver will read.
     * @param outputStream
     *            The output stream to which the Driver will write.
     */
    public INDIElTiempoDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        // We create the Switch Property with only one Switch Element
        send = newSwitchProperty().name("SEND").label("Send Image").group(INDIDriver.GROUP_MAIN_CONTROL)//
                .timeout(TIMEOUT_IN_SECONDS).switchRule(SwitchRules.AT_MOST_ONE).create();
        sendImage = send.newElement().create();

        addProperty(send);

        // We create the BLOB Property for the Spain satellite image
        spainImageProp = newBlobProperty().name("SPAIN_SATELLITE_IMAGE").label("Spain Image").group(INDIDriver.GROUP_MAIN_CONTROL)//
                .permission(PropertyPermissions.RO).timeout(0).create();
        spainImageElem = spainImageProp.newElement().create();

        addProperty(spainImageProp);

        // We create the Text Property for the Spain image name
        spainImageNameProp = newTextProperty().name("SPAIN_IMAGE_NAME").label("Spain Image Name").group(INDIDriver.GROUP_MAIN_CONTROL)//
                .timeout(TIMEOUT_IN_SECONDS).create();
        spainImageNameElem = spainImageNameProp.newElement().create();

        addProperty(spainImageNameProp);

        // We create the BLOB Property for the Europe satellite image
        europeImageProp = newBlobProperty().name("EUROPE_SATELLITE_IMAGE").label("Europe Image").group(INDIDriver.GROUP_MAIN_CONTROL)//
                .permission(PropertyPermissions.RO).timeout(0).create();
        europeImageElem = europeImageProp.newElement().create();

        addProperty(europeImageProp);

        // We create the Text Property for the Europe image name
        europeImageNameProp = newTextProperty().name("EUROPE_IMAGE_NAME").label("Europe Image Name").group(INDIDriver.GROUP_MAIN_CONTROL)//
                .timeout(TIMEOUT_IN_SECONDS).create();
        europeImageNameElem = europeImageNameProp.newElement().create();

        addProperty(europeImageNameProp);

        stop = true;
    }

    /**
     * @return Gets the name of the Driver.
     */
    @Override
    public String getName() {
        return "El Tiempo INDI Driver";
    }

    /**
     * If we receive the Switch Value ON of the property "SEND" we check for new
     * images in the web, download them and send them to the client.
     * 
     * @param property
     *            The Switch Property asked to change.
     * @param timestamp
     *            The timestamp of the received message
     * @param elementsAndValues
     *            An array of pairs of Switch Elements and its requested values
     *            to be parsed.
     */
    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {

        if (property == send) {

            if (elementsAndValues.length > 0) {
                SwitchStatus stat = elementsAndValues[0].getValue();

                if (stat == SwitchStatus.ON) {

                    property.setState(PropertyStates.OK);

                    updateProperty(property, "Checking images");

                    checksForSpainImage(true);

                    checksForEuropeImage(true);
                }
            }
        }
    }

    /**
     * Checks for the Spain Image and, if new, sends it to the clients.
     * 
     * @param alwaysSend
     *            if <code>true</code> the image is sended to the client. If
     *            not, it is only sended if it is new.
     */
    private void checksForSpainImage(boolean alwaysSend) {
        boolean newImage = checkForImage("http://www.eltiempo.es/satelite", "SPAIN");
        INDIBLOBValue v = spainImageElem.getValue();

        if (v.getSize() > 0) {
            if (newImage || alwaysSend) {
                spainImageProp.setState(PropertyStates.OK);

                spainImageNameProp.setState(PropertyStates.OK);

                updateProperty(spainImageProp);
                updateProperty(spainImageNameProp);
            }
        }
    }

    /**
     * get an image of europe from the internet.
     * 
     * @param alwaysSend
     *            if true the image is send in any case if false only if it is
     *            new.
     */
    private void checksForEuropeImage(boolean alwaysSend) {
        boolean newImage = checkForImage("http://www.eltiempo.es/europa/satelite/", "EUROPE");
        INDIBLOBValue v = europeImageElem.getValue();

        if (v.getSize() > 0) {
            if (newImage || alwaysSend) {
                europeImageProp.setState(PropertyStates.OK);

                europeImageNameProp.setState(PropertyStates.OK);

                updateProperty(europeImageProp);
                updateProperty(europeImageNameProp);

            }
        }
    }

    /**
     * Checks for a new image in the <code>url</code> and if it has changed,
     * saves it to the appropriate BLOB Property (data) and Text Property (name)
     * - according to the <code>imagePrefix</code>.
     * 
     * @param url
     *            url to get the image form.
     * @param imagePrefix
     *            prefix for the image.
     * @return true if successful.
     */
    private boolean checkForImage(String url, String imagePrefix) {
        File webpage = new File("web.html");
        String text;

        try {
            downloadAndSave(url, webpage);

            text = readFile(webpage);
        } catch (IOException e) {
            LOG.error("io exception", e);

            return false;
        }

        // We look for the URL of the image to be downloaded
        String searchString = "<img id=\"imgmap\" src=\"";
        int start = text.indexOf(searchString);

        if (start == -1) { // Not found
            return false;
        }

        start += searchString.length();

        int stringStrop = text.indexOf("\"", start + 1);

        if (stringStrop == -1) {
            return false;
        }

        String imgURL = text.substring(start, stringStrop);

        int lastBar = imgURL.lastIndexOf("/");

        String fileName = imgURL.substring(lastBar + 1);

        File image = new File(fileName);

        if (!image.exists()) { // Download the image
            try {
                downloadAndSave(imgURL, image);
            } catch (IOException e) {
                LOG.error("io exception", e);

                return false;
            }
        }

        byte[] imageBytes;

        try {
            imageBytes = readBinaryFile(image);
        } catch (IOException e) {
            LOG.error("io exception", e);

            return false;
        }

        // Define
        INDIBLOBProperty pim = (INDIBLOBProperty) getProperty(imagePrefix + "_SATELLITE_IMAGE");
        INDIBLOBElement eim = (INDIBLOBElement) pim.getElement(imagePrefix + "_SATELLITE_IMAGE");

        if (Arrays.equals(imageBytes, eim.getValue().getBLOBData())) {
            return false; // The same image as the one in the property
        }

        eim.setValue(new INDIBLOBValue(imageBytes, "jpg"));

        int pos1 = fileName.lastIndexOf("-");

        String name =
                fileName.substring(pos1, pos1 + FIXED_STRING_POSITION_5) + "/" + fileName.substring(pos1 + FIXED_STRING_POSITION_5, pos1 + FIXED_STRING_POSITION_7) + "/"
                        + fileName.substring(pos1 + FIXED_STRING_POSITION_7, pos1 + FIXED_STRING_POSITION_9) + " "
                        + fileName.substring(pos1 + FIXED_STRING_POSITION_9, pos1 + FIXED_STRING_POSITION_11) + ":"
                        + fileName.substring(pos1 + FIXED_STRING_POSITION_11, pos1 + FIXED_STRING_POSITION_13);

        INDITextProperty pn = (INDITextProperty) getProperty(imagePrefix + "_IMAGE_NAME");
        INDITextElement en = (INDITextElement) pn.getElement(imagePrefix + "_IMAGE_NAME");

        en.setValue(imagePrefix + " Satellite " + name + " UTC");

        return true;
    }

    /**
     * Reads a text file and returns its contents as a String.
     * 
     * @param file
     *            The file to read
     * @return The contents of the file
     * @throws IOException
     *             when the file could not be read.
     */
    private String readFile(File file) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;
        StringBuilder stringBuilder = new StringBuilder();
        String ls = System.getProperty("line.separator");

        while ((line = reader.readLine()) != null) {
            stringBuilder.append(line);
            stringBuilder.append(ls);
        }

        return stringBuilder.toString();
    }

    /**
     * Reads a binary file and returns its contents as a byte[].
     * 
     * @param file
     *            The file to be read
     * @return The contents of the file
     * @throws IOException
     *             when the file could not be read.
     */
    private byte[] readBinaryFile(File file) throws IOException {
        int fileSize = (int) file.length();
        FileInputStream reader = new FileInputStream(file);

        byte[] buffer = new byte[fileSize];

        int totalRead = 0;

        while (totalRead < fileSize) {
            int readed = reader.read(buffer, totalRead, fileSize - totalRead);

            if (readed == -1) {
                return null; // Unexpected end of file
            }

            totalRead += readed;
        }

        return buffer;
    }

    /**
     * Downloads a <code>url</code> and saves its contents to <code>file</code>.
     * 
     * @param url
     *            url to load the image from
     * @param file
     *            file to save th image to
     * @throws IOException
     *             if the file could not be written or the image could not be
     *             downloaded
     */
    private void downloadAndSave(String url, File file) throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];

        URL u = new URL(url);
        InputStream is = u.openStream(); // throws an IOException
        BufferedInputStream bis = new BufferedInputStream(is);
        FileOutputStream fos = new FileOutputStream(file);

        int readed = 0;

        while (readed != -1) {
            readed = bis.read(buffer);

            if (readed > 0) {
                fos.write(buffer, 0, readed);
            }
        }

        bis.close();
        fos.close();
    }

    /**
     * The thread that every 15 minutes checks for new images and, if they have
     * changed, downloads them and sends them back to the clients.
     */
    @Override
    public void run() {
        while (!stop) {
            try {
                Thread.sleep(MILLISECONDS_IN_15_MUNUTES);
            } catch (InterruptedException e) {
                LOG.error("sleep interrupted", e);
            }

            if (!stop) {
                checksForSpainImage(false);

                checksForEuropeImage(false);
            }
        }

        LOG.info("Thread Stopped");
    }

    /**
     * The method that will handle the connection.
     * 
     * @param timestamp
     *            when the connection message has been received.
     */
    @Override
    public void driverConnect(Date timestamp) {
        if (stop) {
            LOG.info("Starting El Tiempo Driver");
            stop = false;
            runningThread = new Thread(this);
            runningThread.start();
        }
    }

    /**
     * The method that will handle the disconnection.
     * 
     * @param timestamp
     *            when the disconnection message has been received.
     */
    @Override
    public void driverDisconnect(Date timestamp) {
        LOG.info("Stopping El Tiempo Driver");

        stop = true;
    }
}

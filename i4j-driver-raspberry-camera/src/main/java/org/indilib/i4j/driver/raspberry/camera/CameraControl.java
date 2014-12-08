package org.indilib.i4j.driver.raspberry.camera;

/*
 * #%L
 * INDI for Java Driver for the Raspberry pi camera
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.indilib.i4j.driver.raspberry.camera.image.JpegStreamScanner;
import org.indilib.i4j.driver.raspberry.camera.image.RawImage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class is an wrapper around the raspistill command, it converts the raw
 * image to the requested format and uses the burst mode to increase the image
 * thouput.
 * 
 * @author Richard van Nieuwenhoven
 */
public class CameraControl {

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CameraControl.class);

    private static final String OPTION_OFF = "_OPTION_IS_OFF_";

    private static final String OPTION_ON = "_OPTION_IS_ON_";

    private static final String COMMAND = "raspistill";

    private final Map<CameraOption, String> arguments = new HashMap<CameraOption, String>();

    private final Map<String, Object> imageAttributes = new HashMap<String, Object>();

    private boolean stoped = false;

    public void start() {
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (!stoped) {
                    // if there is an image to report and there was no new
                    // request.
                    if (currentImage != null) {
                        RawImage capturedImage = currentImage;
                        currentImage = null;
                        imageCaptured(capturedImage);
                    } else {
                        try {
                            waiting();
                            Thread.sleep(100L);
                        } catch (InterruptedException e) {
                            // ignore
                        }
                    }
                }
            }
        }, "Raspberry pi camera, image reporter").start();
    }

    /**
     * this method is called after a capture call in a separate thread, as soon
     * as the image is ready.
     * 
     * @param capturedImage
     */
    protected synchronized void imageCaptured(RawImage capturedImage) {

    }

    protected void waiting() {

    }

    public CameraControl addOption(CameraOption key) {
        addOption(key, OPTION_ON);
        return this;
    }

    public CameraControl removeOption(CameraOption key) {
        addOption(key, OPTION_OFF);
        return this;
    }

    public CameraControl removeDefaultOption(CameraOption key) {
        addDefaultOption(key, OPTION_OFF);
        return this;
    }

    public CameraControl addDefaultOption(CameraOption key) {
        addDefaultOption(key, OPTION_ON);
        return this;
    }

    public CameraControl addOption(CameraOption key, String value) {
        this.arguments.put(key, value);
        return this;
    }

    public CameraControl addDefaultOption(CameraOption key, String value) {
        if (!this.arguments.containsKey(key)) {
            this.arguments.put(key, value);
        }
        return this;
    }

    public synchronized void stop() {
        stoped = true;
        currentImage = null;
        stopCommandProcesses();
    }

    public synchronized void capture(double shutterSeconds) throws Exception {
        addDefaultOption(CameraOption.output, "-");
        addDefaultOption(CameraOption.width, "128");
        addDefaultOption(CameraOption.height, "128");
        addDefaultOption(CameraOption.raw);
        addDefaultOption(CameraOption.timeout, "180000");
        addDefaultOption(CameraOption.timelapse, "1000");
        addDefaultOption(CameraOption.nopreview);
        addDefaultOption(CameraOption.burst);
        addDefaultOption(CameraOption.awb, "off");
        addDefaultOption(CameraOption.awbgains, "1,1");
        addDefaultOption(CameraOption.ISO, "800");
        addDefaultOption(CameraOption.shutter, Integer.toString(Double.valueOf(shutterSeconds * 1000000d).intValue()));
        currentImage = null;
        if (converterThread == null || !converterThread.isAlive()) {
            startCommandProcesses();
        }
    }

    private Process process;

    private void stopCommandProcesses() {
        if (process != null) {
            int pid = getPidOfProcess();
            try {
                Runtime.getRuntime().exec("kill -s INT " + pid).waitFor();
            } catch (Exception e) {
                throw new RuntimeException("This should not happen", e);
            }
            // process.destroy();
            // wait for the threads to end nicely
            while ((errorThread != null && errorThread.isAlive()) || (converterThread != null && converterThread.isAlive())) {
                try {
                    Thread.sleep(100L);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
    }

    private int getPidOfProcess() {
        try {
            Field declaredField = process.getClass().getDeclaredField("pid");
            declaredField.setAccessible(true);
            return (int) declaredField.get(process);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not get processid", e);
        }
    }

    private Thread errorThread;

    private Thread converterThread;

    private void startCommandProcesses() throws IOException {
        List<String> command = new ArrayList<String>();
        StringBuffer commandString = new StringBuffer("Command used to capture image: ");
        command.add(CameraControl.COMMAND);
        commandString.append(CameraControl.COMMAND);
        for (Entry<CameraOption, String> argument : this.arguments.entrySet()) {
            if (argument.getValue() != OPTION_OFF) {
                command.add(argument.getKey().longArg);
                commandString.append(' ');
                commandString.append(argument.getKey().longArg);
                if (argument.getValue() != OPTION_ON) {
                    command.add(argument.getValue());
                    commandString.append(' ');
                    commandString.append(argument.getValue());
                }
            }
        }
        LOG.info("executing " + commandString);
        ProcessBuilder builder = new ProcessBuilder(command);
        process = builder.start();
        InputStream is = process.getInputStream();
        final InputStream es = process.getErrorStream();
        errorThread = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    String line;
                    BufferedReader errorReader = new BufferedReader(new InputStreamReader(es));
                    while ((line = errorReader.readLine()) != null) {
                        if (line.isEmpty()) {
                            try {
                                Thread.sleep(100L);
                                process.exitValue();
                                return;
                            } catch (Exception x) {
                                // ok not yet exited
                            }
                        } else {
                            LOG.error(line);
                        }
                    }
                } catch (IOException e) {
                    LOG.error("exception during syserror readding ", e);
                }

            }
        }, "Raspberry pi camera command error logger");
        errorThread.start();
        converterThread = new Thread(new JpegStreamScanner(new BufferedInputStream(is)) {

            @Override
            protected void rawImage(RawImage rawImage) throws Exception {
                currentImage = rawImage;
            }

            @Override
            protected boolean isInShutdown() {
                return stoped;
            }
        }, "Raspberry pi camera raw image extractor");
        converterThread.start();
    }

    private RawImage currentImage;

    public CameraControl setAttribute(String name, Object value) {
        this.imageAttributes.put(name, value);
        return this;
    }
}

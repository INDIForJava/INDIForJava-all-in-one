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
     * default sleep time between loops. To reduce buzzy waiting impact.
     */
    private static final long THREAD_SLEEP_TIME = 100L;

    /**
     * how many nano second are in a second.
     */
    private static final double SECOND_IN_NANO_SECONDS = 1000000d;

    /**
     * The logger to log the messages to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(CameraControl.class);

    /**
     * indicator value that an option is off.
     */
    private static final String OPTION_OFF = "_OPTION_IS_OFF_";

    /**
     * indicator value that an option is on.
     */

    private static final String OPTION_ON = "_OPTION_IS_ON_";

    /**
     * the raspberry command to take a picture.
     */
    private static final String COMMAND = "raspistill";

    /**
     * the arguments for the raspistill command.
     */
    private final Map<CameraOption, String> arguments = new HashMap<CameraOption, String>();

    /**
     * ist this command still running?
     */
    private boolean stoped = false;

    /**
     * the current captured image.
     */
    private RawImage currentImage;

    /**
     * the error protocolling thread.
     */
    private Thread errorThread;

    /**
     * the image processing thread.
     */
    private Thread converterThread;

    /**
     * the rapstill process started.
     */
    private Process process;

    /**
     * start the thread that reads an processes the captures of rapstill.
     * 
     * @return this (builder pattern)
     */
    public CameraControl start() {
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
                            Thread.sleep(THREAD_SLEEP_TIME);
                        } catch (InterruptedException e) {
                            LOG.warn("sleep interupted");
                        }
                    }
                }
            }
        }, "Raspberry pi camera, image reporter").start();
        return this;
    }

    /**
     * this method is called after a capture call in a separate thread, as soon
     * as the image is ready.
     * 
     * @param capturedImage
     *            the raw image that was captured.
     */
    protected synchronized void imageCaptured(RawImage capturedImage) {
    }

    /**
     * will be called just before the image reporter thread will go to sleep.
     */
    protected void waiting() {
    }

    /**
     * activate the option of rapstill.
     * 
     * @param key
     *            the option to activate
     * @return this (builder pattern)
     */
    public CameraControl addOption(CameraOption key) {
        addOption(key, OPTION_ON);
        return this;
    }

    /**
     * deactivate the option of rapstill.
     * 
     * @param key
     *            the option to deactivate
     * @return this (builder pattern)
     */
    public CameraControl removeOption(CameraOption key) {
        addOption(key, OPTION_OFF);
        return this;
    }

    /**
     * activate a default option of rapstill. (if a value was already given it
     * is not changed).
     * 
     * @param key
     *            the option to set
     * @return this (builder pattern)
     */
    public CameraControl addDefaultOption(CameraOption key) {
        addDefaultOption(key, OPTION_ON);
        return this;
    }

    /**
     * activate a option of rapstill.
     * 
     * @param key
     *            the option to set
     * @param value
     *            the parameter of the option
     * @return this (builder pattern)
     */
    public CameraControl addOption(CameraOption key, String value) {
        if (value != null && !value.trim().isEmpty()) {
            this.arguments.put(key, value);
        }
        return this;
    }

    /**
     * activate a default option of rapstill. (if a value was already given it
     * is not changed).
     * 
     * @param key
     *            the option to set
     * @param value
     *            the parameter of the option
     * @return this (builder pattern)
     */
    public CameraControl addDefaultOption(CameraOption key, String value) {
        if (!this.arguments.containsKey(key)) {
            this.arguments.put(key, value);
        }
        return this;
    }

    /**
     * stop the capturing and the connected processes.
     */
    public synchronized void stop() {
        stoped = true;
        currentImage = null;
        stopCommandProcesses();
    }

    /**
     * capture a image with the given exporsure. Do it as a loop because the
     * start up time for the raw imageing with long exposure times is very big.
     * 
     * @param shutterSeconds
     *            the exposure time in seconds.
     * @throws Exception
     *             if the command could not be executed.
     */
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
        addDefaultOption(CameraOption.shutter, Integer.toString(Double.valueOf(shutterSeconds * SECOND_IN_NANO_SECONDS).intValue()));
        currentImage = null;
        if (converterThread == null || !converterThread.isAlive()) {
            startCommandProcesses();
        }
    }

    /**
     * stop the running rapstill command nicely.
     */
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
                    Thread.sleep(THREAD_SLEEP_TIME);
                } catch (InterruptedException e) {
                    LOG.warn("sleep interupted");
                }
            }
        }
    }

    /**
     * @return the pid of the rapstill command.
     */
    private int getPidOfProcess() {
        try {
            Field declaredField = process.getClass().getDeclaredField("pid");
            declaredField.setAccessible(true);
            return (int) declaredField.get(process);
        } catch (Exception e) {
            throw new UnsupportedOperationException("Could not get processid", e);
        }
    }

    /**
     * start the rapstill command an the processing threads.
     * 
     * @throws IOException
     *             if something went wrong durring the start, not ecpected.
     */
    private void startCommandProcesses() throws IOException {
        List<String> command = new ArrayList<String>();
        StringBuffer commandString = new StringBuffer("Command used to capture image: ");
        command.add(CameraControl.COMMAND);
        commandString.append(CameraControl.COMMAND);
        for (Entry<CameraOption, String> argument : this.arguments.entrySet()) {
            if (argument.getValue() != OPTION_OFF) {
                command.add(argument.getKey().getLongArg());
                commandString.append(' ');
                commandString.append(argument.getKey().getLongArg());
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
                                Thread.sleep(THREAD_SLEEP_TIME);
                                process.exitValue();
                                return;
                            } catch (Exception x) {
                                LOG.warn("sleep interupted");
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
            protected void rawImage(RawImage rawImage) {
                currentImage = rawImage;
            }

            @Override
            protected boolean isInShutdown() {
                return stoped;
            }
        }, "Raspberry pi camera raw image extractor");
        converterThread.start();
    }

}

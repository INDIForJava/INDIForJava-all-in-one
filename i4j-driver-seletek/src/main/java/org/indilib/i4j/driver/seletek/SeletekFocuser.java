/*
 *  This file is part of INDI for Java Seletek Driver.
 * 
 *  INDI for Java Seletek Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Seletek Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Seletek Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.seletek;

/*
 * #%L
 * INDI for Java Driver for the Seletek
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

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDINotLoadableDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchOneOfManyProperty;
import org.indilib.i4j.driver.INDISwitchOneOrNoneProperty;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.indilib.i4j.driver.focuser.INDIFocuserDriver;

/**
 * A class that acts as a INDI for Java Focuser Driver for a focuser connected
 * to a Seletek.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public class SeletekFocuser extends INDIFocuserDriver implements INDINotLoadableDriver, Runnable {

    /**
     * initial absolute position.
     */
    private static final int INITIAL_ABSOLUTE_POSITION = 50000;

    /**
     * maximum absolute position.
     */
    private static final int MAXIMUM_ABSOLUTE_POSITION = 100000;

    /**
     * Maximum speed.
     */
    private static final int MAXIMUM_SPEED = 10;

    /**
     * The Seletek port number to which the focuser is attached.
     */
    private int seletekPort;

    /**
     * The Seletek Driver.
     */
    private I4JSeletekDriver driver;

    /**
     * To finish the thread that reads the positions of the focuser.
     */
    private boolean stopPositionReaderThread;

    /**
     * Marks if the reader thread should ask for the position of the focser
     * (only when it is moving).
     */
    private boolean updatePosition;

    /**
     * The Wire Mode Property.
     */
    private INDISwitchOneOfManyProperty wireModeP;

    /**
     * The Model Property.
     */
    private INDISwitchOneOfManyProperty modelP;

    /**
     * The Half Step Property.
     */
    private INDISwitchOneOrNoneProperty halfStepP;

    /**
     * The Move Power Property.
     */
    private INDINumberProperty powerSettingsP;

    /**
     * The Move Power Element.
     */
    private INDINumberElement movePowerE;

    /**
     * The Move Power Element.
     */
    private INDINumberElement stopPowerE;

    /**
     * Constructs an instance of a <code>SeletekFocuser</code> with a particular
     * <code>inputStream</code> from which to read the incoming messages (from
     * clients) and a <code>outputStream</code> to write the messages to the
     * clients.
     * 
     * @param inputStream
     *            The stream from which to read messages
     * @param outputStream
     *            The stream to which to write the messages
     * @param seletekPort
     *            0 - Main port, 1 - Exp port
     * @param driver
     *            The Seletek Driver
     */
    public SeletekFocuser(InputStream inputStream, OutputStream outputStream, int seletekPort, I4JSeletekDriver driver) {
        super(inputStream, outputStream);

        this.seletekPort = seletekPort;
        this.driver = driver;

        initializeStandardProperties();
        showSpeedProperty();
        speedHasBeenChanged(); // To set the possibly saved speed

        showStopFocusingProperty();

        driver.stopStepper(seletekPort);

        stopPositionReaderThread = false;
        updatePosition = false;

        wireModeP = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("wireMode").label("Wire Mode").group("Configuration").create();
        wireModeP.newElement().name("Lunático").create();
        wireModeP.newElement().name("Lunático Inverted").create();
        wireModeP.newElement().name("RF/Moonlite").create();
        wireModeP.newElement().name("RF/Moonlite Inverted").create();
        addProperty(wireModeP);

        int mode = wireModeP.getSelectedIndex();
        driver.setStepperWireMode(seletekPort, mode);

        modelP = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("model").label("Model").group("Configuration").create();
        modelP.newElement().name("Unipolar").create();
        modelP.newElement().name("Bipolar").create();
        modelP.newElement().name("DC").create();
        modelP.newElement().name("Step and Dir").create();
        addProperty(modelP);

        int model = modelP.getSelectedIndex();
        driver.setStepperModel(seletekPort, model);

        halfStepP = newProperty(INDISwitchOneOrNoneProperty.class).saveable(true).name("halfStep").label("Half Step").group("Configuration").create();
        halfStepP.newElement().name("Half Step").create();

        addProperty(halfStepP);
        boolean half = (halfStepP.getStatus() == SwitchStatus.ON);
        driver.setStepperHalfStep(seletekPort, half);

        powerSettingsP = newNumberProperty().saveable(true).name("stepper_pow").label("Power Settings").group("Configuration").create();
        movePowerE = powerSettingsP.newElement().name("move_power").label("Moving Power")//
                .maximum(I4JSeletekDriver.MAX_POWER_VALUE).numberValue(I4JSeletekDriver.MAX_POWER_VALUE).step(1).numberFormat("%1.0f").create();
        stopPowerE = powerSettingsP.newElement().name("stop_power").label("Stopped Power")//
                .maximum(I4JSeletekDriver.MAX_POWER_VALUE).step(1).numberFormat("%1.0f").create();
        addProperty(powerSettingsP);

        driver.setStepperMovePower(seletekPort, movePowerE.getValue().intValue());
        driver.setStepperStopPower(seletekPort, stopPowerE.getValue().intValue());

        Thread readerThread = new Thread(this);
        readerThread.start();
    }

    @Override
    public String getName() {
        return "Seletek Focuser (" + Utils.getSeletekPortName(seletekPort) + ")";
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        super.processNewSwitchValue(property, timestamp, elementsAndValues);

        if (property == wireModeP) {
            int mode = wireModeP.getSelectedIndex(elementsAndValues);
            driver.setStepperWireMode(seletekPort, mode);
            wireModeP.setSelectedIndex(elementsAndValues);
            wireModeP.setState(PropertyStates.OK);

            updateProperty(wireModeP);

        }

        if (property == modelP) {
            int model = modelP.getSelectedIndex(elementsAndValues);
            driver.setStepperModel(seletekPort, model);
            modelP.setSelectedIndex(elementsAndValues);
            modelP.setState(PropertyStates.OK);

            updateProperty(modelP);

        }

        if (property == halfStepP) {
            boolean half = (halfStepP.getStatus(elementsAndValues) == SwitchStatus.ON);
            driver.setStepperHalfStep(seletekPort, half);
            halfStepP.setStatus(elementsAndValues);
            halfStepP.setState(PropertyStates.OK);

            updateProperty(halfStepP);

        }
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        super.processNewNumberValue(property, timestamp, elementsAndValues);

        if (property == powerSettingsP) {
            powerSettingsP.setValues(elementsAndValues);

            driver.setStepperMovePower(seletekPort, movePowerE.getValue().intValue());
            driver.setStepperStopPower(seletekPort, stopPowerE.getValue().intValue());

            powerSettingsP.setState(PropertyStates.OK);

            updateProperty(powerSettingsP);

        }
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
        // throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMaximumSpeed() {
        return MAXIMUM_SPEED;
    }

    @Override
    public final void speedHasBeenChanged() {
        driver.setStepperSpeed(seletekPort, getCurrentSpeed());
    }

    /**
     * The Seletek signals that the speed has been set.
     */
    void setDesiredSpeed() {
        super.desiredSpeedSet();
    }

    @Override
    public final int getMaximumAbsPos() {
        return MAXIMUM_ABSOLUTE_POSITION;
    }

    @Override
    public final int getMinimumAbsPos() {
        return 0;
    }

    @Override
    public final int getInitialAbsPos() {
        return INITIAL_ABSOLUTE_POSITION;
    }

    @Override
    public void absolutePositionHasBeenChanged() {
        driver.stepperGotoAbs(seletekPort, getDesiredAbsPosition());
        updatePosition = true;
    }

    @Override
    public void stopHasBeenRequested() {
        driver.stopStepper(seletekPort);
    }

    /**
     * The Seletek informs that the focuser has stopped.
     */
    void stopFocuser() {
        stopped();

        finalPositionReached();
        updatePosition = false;
        driver.getStepperPos(seletekPort);
    }

    /**
     * The Seletek informs about the focuser position.
     * 
     * @param position
     *            The position of the focuser
     */
    void showFocusPosition(int position) {
        positionChanged(position);

        if (position == getDesiredAbsPosition()) {
            finalPositionReached();
            updatePosition = false;
        }
    }

    @Override
    public void isBeingDestroyed() {
        stopPositionReaderThread = true;

        Utils.sleep(I4JSeletekDriver.MILLISECONDS_TO_WAIT_BEFORE_CLOSE_PORTS);

        super.isBeingDestroyed();
    }

    /**
     * The thread will ask for the position of the focuser every 200 miliseconds
     * when it is moving.
     */
    @Override
    public void run() {
        while (!stopPositionReaderThread) {
            if (updatePosition) {
                driver.getStepperPos(seletekPort);
            }

            Utils.sleep(I4JSeletekDriver.MILLISECONDS_TO_WAIT_BEFORE_CLOSE_PORTS);
        }
    }
}

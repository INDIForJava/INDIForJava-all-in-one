/*
 *  This file is part of INDI for Java Raspberry PI GPIO Driver.
 *
 *  INDI for Java Raspberry PI GPIO Driver is free software: you can
 *  redistribute it and/or modify it under the terms of the GNU General Public
 *  License as published by the Free Software Foundation, either version 3 of
 *  the License, or (at your option) any later version.
 *
 *  INDI for Java Raspberry PI GPIO Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Raspberry PI GPIO Driver.  If not, see
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver.raspberrypi;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * INDI for Java Driver for the Raspberry PI
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

/**
 * A Thread that asks the Raspberry Pi GPIO Driver to update the sensor
 * Properties periodically.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class RaspberryPiSensorReaderThread extends Thread {

    /**
     * Ten seconds in milliseconds.
     */
    private static final int TEN_SECONDS_IN_MILLISECONDS = 10000;

    /**
     * logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RaspberryPiSensorReaderThread.class);

    /**
     * To stop the Thread.
     */
    private boolean stopReading;

    /**
     * The Raspberry Pi GPIO Driver.
     */
    private I4JRaspberryPiGPIODriver driver;

    /**
     * Constructs an instance of the Thread.
     * 
     * @param driver
     *            The Raspberry Pi GPIO Driver
     */
    protected RaspberryPiSensorReaderThread(I4JRaspberryPiGPIODriver driver) {
        stopReading = false;
        this.driver = driver;
    }

    /**
     * Asks the thread to stop.
     */
    protected void stopReading() {
        stopReading = true;
    }

    @Override
    public void run() {
        while (!stopReading) {
            driver.setSensors();
            sleep(TEN_SECONDS_IN_MILLISECONDS);
        }
    }

    /**
     * Sleeps for a certain amount of time.
     * 
     * @param milis
     *            The number of milliseconds to sleep
     */
    private void sleep(int milis) {
        try {
            Thread.sleep(milis);
        } catch (InterruptedException e) {
            LOG.error("sleep interupted", e);
        }
    }
}

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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that asks for the readings of the sensors of the Seletek.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public class SeletekSensorStatusRequesterThread extends Thread {

    private static Logger LOG = LoggerFactory.getLogger(SeletekSensorStatusRequesterThread.class);

    /**
     * The Seletek Driver.
     */
    private I4JSeletekDriver driver;

    /**
     * To finish the thread.
     */
    private boolean stopRequesting;

    /**
     * Number of temperature readings to do before obtaining a final one.
     */
    public static final int TEMPERATURE_READINGS = 10;

    /**
     * Constructs an instance of a
     * <code>SeletekSensorStatusRequesterThread</code>.
     * 
     * @param driver
     *            The Seletek Driver
     */
    public SeletekSensorStatusRequesterThread(I4JSeletekDriver driver) {
        this.driver = driver;

        stopRequesting = false;
    }

    /**
     * Tells the thread to stop requesting and finish.
     */
    public void stopRequesting() {
        stopRequesting = true;
    }

    @Override
    public void run() {
        while (!stopRequesting) {
            for (int i = 0; i < TEMPERATURE_READINGS; i++) {
                if (!stopRequesting) {
                    driver.askForInternalTemperature();
                    Utils.sleep(100);
                }

                if (!stopRequesting) {
                    driver.askForExternalTemperature();
                    Utils.sleep(100);
                }
            }

            if (!stopRequesting) {
                driver.askForPowerOk();
                Utils.sleep(100);
            }

            if (!stopRequesting) {
                Utils.sleep(60000);
            }
        }

        LOG.info("Seletek Sensor Status Reader Thread Ending");
    }
}

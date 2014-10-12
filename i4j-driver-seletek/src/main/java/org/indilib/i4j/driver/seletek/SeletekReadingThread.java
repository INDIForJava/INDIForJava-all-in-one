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

import java.io.FileInputStream;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread that reads messages from the Seletek.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.35, November 11, 2013
 */
public class SeletekReadingThread extends Thread {

    /**
     * Buffer size.
     */
    private static final int BUFFER_SIZE = 4096;

    /**
     * Logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SeletekReadingThread.class);

    /**
     * The Seletek Driver.
     */
    private I4JSeletekDriver driver;

    /**
     * The stream from which to read the messages.
     */
    private FileInputStream seletekInput;

    /**
     * To finish the thread.
     */
    private boolean stopReading;

    /**
     * The thread has ended.
     */
    private boolean ended;

    /**
     * Constructs an instance of a <code>SeletekReadingThread</code>.
     * 
     * @param driver
     *            The driver that will parse the messages
     * @param seletekInput
     *            The input stream from which the messages will be readed
     */
    public SeletekReadingThread(I4JSeletekDriver driver, FileInputStream seletekInput) {
        this.driver = driver;
        this.seletekInput = seletekInput;
    }

    /**
     * Tells the thread to stop reading and finish.
     */
    protected synchronized void stopReading() {
        stopReading = true;
    }

    /**
     * Gets if the thread has already ended.
     * 
     * @return <code>true</code> if the thread has already * * ended,
     *         <code>false</code> otherwise
     */
    protected boolean hasEnded() {
        return ended;
    }

    @Override
    public void run() {
        ended = false;
        String buffer = "";

        while (!stopReading) {
            try {
                if (seletekInput.available() > 0) {
                    byte[] readed = new byte[BUFFER_SIZE];

                    int br = seletekInput.read(readed);

                    if (br > 0) {
                        buffer += new String(readed, 0, br);

                        buffer = driver.parseSeletekMessages(buffer);
                    }
                }
            } catch (IOException e) {
                stopReading = true;
            }

            Utils.sleep(I4JSeletekDriver.MILLISECONDS_TO_WAIT_BEFORE_CLOSE_PORTS);
        }

        LOG.info("Seletek Reader Thread Ending");

        ended = true;
    }
}

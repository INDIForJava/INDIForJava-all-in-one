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

import java.io.InputStream;
import java.io.OutputStream;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An example class representing a very basic INDI Driver. It just defines a
 * read only Number Property that shows a pseudo random number that changes each
 * second.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.3, April 4, 2012
 */
public class RandomNumberGeneratorDriver extends INDIDriver implements Runnable {

    /**
     * interfall between number generations.
     */
    private static final int GENERATE_INTERFALL = 1000;

    /**
     * logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(RandomNumberGeneratorDriver.class);

    /**
     * The random number Property.
     */
    @InjectProperty(name = "random", label = "Random Number", permission = PropertyPermissions.RO)
    private INDINumberProperty randomP;

    /**
     * The random number Element.
     */
    @InjectElement(maximum = 1.0, numberFormat = "%f")
    private INDINumberElement randomE;

    /**
     * Initializes the driver. It creates the Proerties and its Elements.
     * 
     * @param inputStream
     *            The input stream from which the Driver will read.
     * @param outputStream
     *            The output stream to which the Driver will write.
     */
    public RandomNumberGeneratorDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        this.addProperty(randomP);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public String getName() {
        return "Random Number Generator";
    }

    /**
     * Main logic: iterate forever changing the number value.
     */
    @Override
    public void run() {
        while (true) {
            double aux = Math.random();

            // Update Element
            randomE.setValue(aux);

            // Set Property state to OK
            randomP.setState(PropertyStates.OK);

            // Send the changes to the Clients
            updateProperty(randomP);
            try {
                Thread.sleep(GENERATE_INTERFALL);
            } catch (InterruptedException e) {
                LOG.error("sleep interrupted", e);
            }
        }
    }
}

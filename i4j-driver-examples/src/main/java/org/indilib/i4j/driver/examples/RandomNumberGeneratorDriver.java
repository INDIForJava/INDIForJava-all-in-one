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
import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;

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
     * The random number Property
     */
    private INDINumberProperty randomP;

    /**
     * The random number Element
     */
    private INDINumberElement randomE;

    public RandomNumberGeneratorDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        // Define the Property REMEMBER TO SET IT TO RO
        randomP = new INDINumberProperty(this, "random", "Random Number", PropertyStates.IDLE, PropertyPermissions.RO);
        randomE = new INDINumberElement(randomP, "random", "Random Number", 0, 0, 1.0, 0, "%f");

        this.addProperty(randomP);

        Thread t = new Thread(this);
        t.start();
    }

    @Override
    public String getName() {
        return "Random Number Generator";
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    /**
     * Main logic: iterate forever changing the number value
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
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
        }
    }
}

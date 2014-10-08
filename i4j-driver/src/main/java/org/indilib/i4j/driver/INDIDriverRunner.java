package org.indilib.i4j.driver;

/*
 * #%L
 * INDI for Java Driver Library
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class to initialize and launch a <code>INDIDriver</code>. It just contain a
 * <code>main</code> method to initialize the appropriate Driver.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.10, March 19, 2012
 */
public class INDIDriverRunner {

    private static final Logger LOG = LoggerFactory.getLogger(INDIDriverRunner.class);

    /**
     * Initializes a <code>INDIDriver</code>.
     * 
     * @param args
     *            the command line arguments. The first argument must be the
     *            complete name of the class of the <code>INDIDriver</code>.
     *            That class must be in the class path in order to be loaded.
     * @see INDIDriver
     */
    public static void main(String[] args) {
        if (args.length != 1) {
            LOG.error("A INDIDriver class name must be supplied");
            System.exit(-1);
        }

        INDIDriver driver = null;

        try {
            Class theClass = Class.forName(args[0]);
            Constructor c = theClass.getConstructor(InputStream.class, OutputStream.class);
            driver = (INDIDriver) c.newInstance(System.in, System.out);
        } catch (ClassNotFoundException ex) {
            LOG.error(ex + " class must be in class path.", ex);
            System.exit(-1);
        } catch (InstantiationException ex) {
            LOG.error(ex + " class must be concrete.", ex);
            System.exit(-1);
        } catch (IllegalAccessException ex) {
            LOG.error(ex + " class must have a no-arg constructor.", ex);
            System.exit(-1);
        } catch (NoSuchMethodException ex) {
            LOG.error(ex + " class must have a InputStream, OutputStream constructor.", ex);
            System.exit(-1);
        } catch (InvocationTargetException ex) {
            LOG.error(ex + " invocation target exception.", ex);
            System.exit(-1);
        }

        driver.startListening();
    }
}

package org.indilib.i4j.driver.junit;

/*
 * #%L
 * INDI for Java Driver JUNIT Test Utilities
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import java.lang.reflect.Field;

import org.indilib.i4j.INDIProtocolReader;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.indilib.i4j.protocol.io.INDIPipedConnections;
import org.junit.runners.model.Statement;

/**
 * the statement that realy wrappes arount a sigle test.
 * 
 * @author Richard van Nieuwenhoven
 */
public final class INDIStatement extends Statement {

    /**
     * instance of the test class.
     */
    private Object target;

    /**
     * the original test.
     */
    private Statement base;

    /**
     * the connection between the driver and the client.
     */
    private INDIPipedConnections pipedCon;

    /**
     * the driver protokol reader of messages.
     */
    private INDIProtocolReader reader;

    /**
     * the test client to the driver.
     */
    private TestClient testClient;

    /**
     * the field with the driver instance.
     */
    private Field driverField = null;

    /**
     * the field with the test client instance.
     */
    private Field clientField = null;

    /**
     * Constructor for the statement.
     * 
     * @param base
     *            the original test.
     * @param target
     *            instance of the test class.
     */
    INDIStatement(Statement base, Object target) {
        this.target = target;
        this.base = base;
    }

    @Override
    public void evaluate() throws Throwable {
        try {
            // Execute handlers before statement execution
            before();
            // Execute statements
            base.evaluate();
        } finally {
            // Execute handlers after statement execution
            after();
        }
    }

    /**
     * initialize the driver and client before the test starts.
     */
    private void before() {

        try {
            for (Field field : target.getClass().getDeclaredFields()) {
                if (INDIDriver.class.isAssignableFrom(field.getType())) {
                    driverField = field;
                }
                if (TestClient.class.isAssignableFrom(field.getType())) {
                    clientField = field;
                }
            }

            pipedCon = new INDIPipedConnections();

            INDIDriver driver = (INDIDriver) driverField.getType().getConstructor(INDIConnection.class).newInstance(pipedCon.first());
            if (driverField != null) {
                driverField.setAccessible(true);
                driverField.set(target, driver);
            }
            driver.startListening();
            reader = new INDIProtocolReader(driver, "device reader");
            reader.start();

            testClient = new TestClient(pipedCon.second());
            if (clientField != null) {
                clientField.setAccessible(true);
                clientField.set(target, testClient);
            }
        } catch (Exception e) {
            throw new AssertionError("before test failed", e);
        }
    }

    /**
     * shutown the driver and test class after the test.
     */
    private void after() {
        try {
            if (clientField != null) {
                clientField.set(target, null);
            }
            if (driverField != null) {
                driverField.set(target, null);
            }
            if (reader != null) {
                reader.setStop(true);
            }
            if (testClient != null) {
                testClient.stop();
            }
            if (pipedCon != null) {
                pipedCon.first().close();
                pipedCon.second().close();
            }
        } catch (Exception e) {
            throw new AssertionError("after test failed", e);
        }
    }

}

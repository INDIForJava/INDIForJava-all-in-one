package org.indilib.i4j.driver.sqm;

/*
 * #%L
 * INDI for Java Driver for the Sky Quality Meter - LU
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

import static org.indilib.i4j.Constants.PropertyPermissions.RO;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.UnsupportedCommOperationException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.StringTokenizer;

import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIPortProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class that acts as a INDI for Java Driver for the Sky Quality Meter - LU.
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.37, January 11, 2014
 */
public class I4JSQMDriver extends INDIDriver implements INDIConnectionHandler {

    /**
     * index in the measurements for the sensor temperature.
     */
    private static final int SENSOR_TEMPERATURE_P_INDEX = 4;

    /**
     * index in the measurements for the sensor period sp.
     */
    private static final int SENSOR_PERIOD_SP_INDEX = 3;

    /**
     * index in the measurements for the sensor period cp.
     */
    private static final int SENSOR_PERIOD_CP_INDEX = 2;

    /**
     * index in the measurements for the sensor freq p.
     */
    private static final int SENSOR_FREQ_P_INDEX = 1;

    /**
     * index in the measurements for the sensor reading p.
     */
    private static final int SENSOR_READING_P_INDEX = 0;

    /**
     * the timeout to use for the serial port.
     */
    private static final int SERIAL_PORT_TIMEOUT = 2000;

    /**
     * baud rate of the serial device.
     */
    private static final int BAUD_RATE = 115200;

    /**
     * stepping for the sensor temperature.
     */
    private static final double SENSOR_TEMPERATURE_STEPPING = 0.1;

    /**
     * stepping for the sensor reading.
     */
    private static final double SENSOR_READING_STEPPING = 0.01;

    /**
     * stepping for the sensor period.
     */
    private static final double SENSOR_PERIOD_STEP = 0.001;

    /**
     * a very big number to be used as a maximum number.
     */
    private static final int BIG_MAXIMUM_VALUE = 1000000000;

    /**
     * the maximal value for the sensor reading property.
     */
    private static final int MAXIMUM_SENSOR_READING = 100000;

    /**
     * the minimal value for the sensor reading property.
     */
    private static final int MINIMUM_SENSOR_READING = -100000;

    /**
     * the logger for messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(I4JSQMDriver.class);

    /**
     * The serial and its reading and writting streams / readers.
     */
    private SerialPort serialPort;

    /**
     * the output stream to the serial port.
     */
    private OutputStream os;

    /**
     * the buffered reader over the serial port input stream.
     */
    private BufferedReader br;

    /**
     * The PORTS property.
     */
    private INDIPortProperty portP;

    /**
     * Protocol Number property.
     */
    private INDITextProperty protocolNumberP;

    /**
     * Model Number property.
     */
    private INDITextProperty modelNumberP;

    /**
     * Feature Number property.
     */
    private INDITextProperty featureNumberP;

    /**
     * Serial Number property.
     */
    private INDITextProperty serialNumberP;

    /**
     * Sensor Reading property.
     */
    private INDINumberProperty sensorReadingP;

    /**
     * Sensor Frequency property.
     */
    private INDINumberProperty sensorFreqP;

    /**
     * Sensor Period (Cycles) property.
     */
    private INDINumberProperty sensorPeriodCP;

    /**
     * Sensor Period (Seconds) property.
     */
    private INDINumberProperty sensorPeriodSP;

    /**
     * Sensor Temperature property.
     */
    private INDINumberProperty sensorTempP;

    /**
     * Do Readings Property.
     */
    private INDISwitchProperty doReadingP;

    /**
     * Constructs an instance of a <code>I4JSQMDriver</code> with a particular
     * <code>inputStream</code> from which to read the incoming messages (from
     * clients) and a <code>outputStream</code> to write the messages to the
     * clients.
     * 
     * @param inputStream
     *            The stream from which to read messages.
     * @param outputStream
     *            The stream to which to write the messages.
     */
    public I4JSQMDriver(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);

        serialPort = null;

        portP = INDIPortProperty.create(this, "/dev/ttyUSB0");

        protocolNumberP = newTextProperty().name("protocolNumber").label("Protocol Number").group("Device Info").permission(RO).create();
        protocolNumberP.newElement().textValue("-").create();

        modelNumberP = newTextProperty().name("modelNumber").label("Model Number").group("Device Info").permission(RO).create();
        modelNumberP.newElement().textValue("-").create();

        featureNumberP = newTextProperty().name("featureNumber").label("Feature Number").group("Device Info").permission(RO).create();
        featureNumberP.newElement().textValue("-").create();

        serialNumberP = newTextProperty().name("serialNumber").label("Serial Number").group("Device Info").permission(RO).create();
        serialNumberP.newElement().textValue("-").create();

        sensorReadingP = newNumberProperty().name("sensorReading").label("Sensor Reading (m/sas)").group("Readings").permission(RO).create();
        sensorReadingP.newElement().minimum(MINIMUM_SENSOR_READING).maximum(MAXIMUM_SENSOR_READING).step(SENSOR_READING_STEPPING).numberFormat("%5.2f").create();

        sensorFreqP = newNumberProperty().name("sensorFrequency").label("Sensor Frequency (Hz)").group("Readings").permission(RO).create();
        sensorFreqP.newElement().maximum(BIG_MAXIMUM_VALUE).step(1).numberFormat("%10.0f").create();

        sensorPeriodCP = newNumberProperty().name("sensorPeriodC").label("Sensor Period (counts)").group("Readings").permission(RO).create();
        sensorPeriodCP.newElement().maximum(BIG_MAXIMUM_VALUE).step(1).numberFormat("%10.0f").create();

        sensorPeriodSP = newNumberProperty().name("sensorPeriodS").label("Sensor Period (s)").group("Readings").permission(RO).create();
        sensorPeriodSP.newElement().maximum(BIG_MAXIMUM_VALUE).step(SENSOR_PERIOD_STEP).numberFormat("%10.3f").create();

        sensorTempP = newNumberProperty().name("sensorTemperature").label("Sensor Temperature (C)").group("Readings").permission(RO).create();
        sensorTempP.newElement().maximum(BIG_MAXIMUM_VALUE).step(SENSOR_TEMPERATURE_STEPPING).numberFormat("%4.1f").create();

        doReadingP = newSwitchProperty().name("doReading").label("Do Reading").group("Main Control").create();
        doReadingP.newElement().create();

        this.addProperty(portP);
    }

    @Override
    public String getName() {
        return "Sky Quality Meter - LU";
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        portP.processTextValue(property, elementsAndValues);
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        if (property == doReadingP) {
            doReading();
            doReadingP.setState(PropertyStates.OK);

            updateProperty(doReadingP);

        }
    }

    @Override
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {

        /*
         * if (property == nReadingsP) { int aux =
         * elementsAndValues[0].getValue().intValue(); if (aux < 1) { aux = 1; }
         * if (aux > 100) { aux = 100; } nReadingsP.setValue(aux + "");
         * nReadingsP.setState(PropertyStates.OK); updateProperty(nReadingsP); }
         */
    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        String portName = portP.getPort();

        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
            serialPort = (SerialPort) portIdentifier.open(this.getClass().getName(), SERIAL_PORT_TIMEOUT);
            serialPort.setSerialPortParams(BAUD_RATE, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
        } catch (NoSuchPortException | PortInUseException | UnsupportedCommOperationException e) {
            serialPort = null;
            throw new INDIException("Problem connecting to " + portName + ".\n" + e.toString());
        }

        try {
            os = serialPort.getOutputStream();
            br = new BufferedReader(new InputStreamReader(serialPort.getInputStream()));
        } catch (IOException e) {
            throw new INDIException("Problem connecting to " + portName + ".\n" + e.toString());
        }

        this.addProperty(protocolNumberP);
        this.addProperty(modelNumberP);
        this.addProperty(featureNumberP);
        this.addProperty(serialNumberP);

        this.addProperty(sensorReadingP);
        this.addProperty(sensorFreqP);
        this.addProperty(sensorPeriodCP);
        this.addProperty(sensorPeriodSP);
        this.addProperty(sensorTempP);

        // this.addProperty(nReadingsP);
        this.addProperty(doReadingP);

        getDeviceInfo();
    }

    @Override
    public void driverDisconnect(Date timestamp) throws INDIException {
        if (serialPort != null) {
            serialPort.close();

            try {
                os.close();
                br.close();
            } catch (IOException e) {
                LOG.error("could not close the serial port", e);
            }

            os = null;
            br = null;
        }

        this.removeProperty(protocolNumberP);
        this.removeProperty(modelNumberP);
        this.removeProperty(featureNumberP);
        this.removeProperty(serialNumberP);

        this.removeProperty(sensorReadingP);
        this.removeProperty(sensorFreqP);
        this.removeProperty(sensorPeriodCP);
        this.removeProperty(sensorPeriodSP);
        this.removeProperty(sensorTempP);

        // this.removeProperty(nReadingsP);
        this.removeProperty(doReadingP);
    }

    /**
     * Performs a reading of the sensor. This method populates the measurements
     * properties.
     */
    private void doReading() {
        // int nR = nReadingsP.getValue().intValue();

        /*
         * double[][] measurements = new double[nR][]; double initialAvg = 0.0;
         * for (int i = 0 ; i < nR ; i++) { measurements[i] = getMeasurement();
         * initialAvg += measurements[i][0]; } initialAvg /= nR; // Compute
         * standard deviation double sigma = 0.0; for (int i = 0 ; i < nR ; i++)
         * { sigma += (measurements[i][0] - initialAvg) * (measurements[i][0] -
         * initialAvg); } sigma /= nR; sigma = Math.sqrt(sigma); // Compute
         * sigma clipped average double[] sigmaClippedAverage = new double[5];
         * int addedData = 0; for (int i = 0 ; i < nR ; i++) { if
         * (Math.abs(measurements[i][0] - initialAvg) <= sigma) { addedData++;
         * sigmaClippedAverage[0] += measurements[i][0]; sigmaClippedAverage[1]
         * += measurements[i][1]; sigmaClippedAverage[2] += measurements[i][2];
         * sigmaClippedAverage[3] += measurements[i][3]; sigmaClippedAverage[4]
         * += measurements[i][4]; } } sigmaClippedAverage[0] /= addedData;
         * sigmaClippedAverage[1] /= addedData; sigmaClippedAverage[2] /=
         * addedData; sigmaClippedAverage[3] /= addedData;
         * sigmaClippedAverage[4] /= addedData;
         */

        double[] measurements = getMeasurement();
        sensorReadingP.firstElement().setValue(measurements[SENSOR_READING_P_INDEX]);
        sensorReadingP.setState(PropertyStates.OK);
        sensorFreqP.firstElement().setValue(measurements[SENSOR_FREQ_P_INDEX]);
        sensorFreqP.setState(PropertyStates.OK);
        sensorPeriodCP.firstElement().setValue(measurements[SENSOR_PERIOD_CP_INDEX]);
        sensorPeriodCP.setState(PropertyStates.OK);
        sensorPeriodSP.firstElement().setValue(measurements[SENSOR_PERIOD_SP_INDEX]);
        sensorPeriodSP.setState(PropertyStates.OK);
        sensorTempP.firstElement().setValue(measurements[SENSOR_TEMPERATURE_P_INDEX]);
        sensorTempP.setState(PropertyStates.OK);

        updateProperty(sensorReadingP);
        updateProperty(sensorFreqP);
        updateProperty(sensorPeriodCP);
        updateProperty(sensorPeriodSP);
        updateProperty(sensorTempP);

    }

    /**
     * Gets a measurement from the SQM.
     * 
     * @return An array of 5 elements: Magnitudes per square arc second,
     *         frequency of sensor, period of sensor in counts, period of sensor
     *         in seconds, light sensor temperature (ºC)
     */
    private double[] getMeasurement() {
        String answer;
        try {
            do {
                writeToSQM("rx");
                answer = readFromSQM();
                // answer =
                // "r, 06.70m,0000022921Hz,0000000020c,0000000.000s, 039.4C\n";

                answer = answer.trim();
            } while (!answer.startsWith("r,"));
        } catch (IOException e) {
            LOG.error("read error", e);
            return null;
        }

        double[] res = new double[SENSOR_TEMPERATURE_P_INDEX + 1];
        StringTokenizer st = new StringTokenizer(answer, ",", false);

        st.nextToken(); // Ignore "r"

        String aux = st.nextToken();
        aux = aux.substring(0, aux.length() - 1);
        res[SENSOR_READING_P_INDEX] = Double.parseDouble(aux);

        aux = st.nextToken();
        aux = aux.substring(0, aux.length() - 2);
        res[SENSOR_FREQ_P_INDEX] = Double.parseDouble(aux);

        aux = st.nextToken();
        aux = aux.substring(0, aux.length() - 1);
        res[SENSOR_PERIOD_CP_INDEX] = Double.parseDouble(aux);

        aux = st.nextToken();
        aux = aux.substring(0, aux.length() - 1);
        res[SENSOR_PERIOD_SP_INDEX] = Double.parseDouble(aux);

        aux = st.nextToken();
        aux = aux.substring(0, aux.length() - 1);
        res[SENSOR_TEMPERATURE_P_INDEX] = Double.parseDouble(aux);

        return res;
    }

    /**
     * Retrieves basic device info of the SQM and populates the corresponding
     * properties.
     */
    private void getDeviceInfo() {
        String answer;
        try {
            do {
                writeToSQM("ix");
                answer = readFromSQM();
                // answer = "i,00000002,00000003,00000001,00000413\n";

                answer = answer.trim();
            } while (!answer.startsWith("i,"));
        } catch (IOException e) {
            LOG.error("read error", e);
            return;
        }

        StringTokenizer st = new StringTokenizer(answer, ",", false);

        st.nextToken(); // Ignore "i"

        protocolNumberP.firstElement().setValue(st.nextToken());
        protocolNumberP.setState(PropertyStates.OK);
        modelNumberP.firstElement().setValue(st.nextToken());
        modelNumberP.setState(PropertyStates.OK);
        featureNumberP.firstElement().setValue(st.nextToken());
        featureNumberP.setState(PropertyStates.OK);
        serialNumberP.firstElement().setValue(st.nextToken());
        serialNumberP.setState(PropertyStates.OK);

        updateProperty(protocolNumberP);
        updateProperty(modelNumberP);
        updateProperty(featureNumberP);
        updateProperty(serialNumberP);

    }

    /**
     * Reads a line from the SQM.
     * 
     * @return The readed line
     * @throws IOException
     *             If there is any problem in the communication.
     */
    private String readFromSQM() throws IOException {
        if (br != null) {
            String aux = br.readLine();

            return aux;
        }

        return "";
    }

    /**
     * Writes a String to the SQM.
     * 
     * @param msg
     *            The String to be sent to the SQM.
     * @throws IOException
     *             If there is any problem in the communication.
     */
    private void writeToSQM(String msg) throws IOException {
        if (os != null) {
            os.write(msg.getBytes());
        }
    }
}

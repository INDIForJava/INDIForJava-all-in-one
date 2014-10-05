/*
 *  This file is part of INDI for Java Sky Quality Meter - LU Driver.
 * 
 *  INDI for Java QHY Sky Quality Meter - LU Driver is free software: you can 
 *  redistribute it and/or modify it under the terms of the GNU General Public 
 *  License as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Sky Quality Meter - LU Driver is distributed in the hope that it
 *  will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Sky Quality Meter - LU Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package lorg.indilib.i4j.driver.sqm;

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

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIBLOBElementAndValue;
import org.indilib.i4j.driver.INDIBLOBProperty;
import org.indilib.i4j.driver.INDIConnectionHandler;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDIOneElementNumberProperty;
import org.indilib.i4j.driver.INDIOneElementTextProperty;
import org.indilib.i4j.driver.INDIPortProperty;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchOneOrNoneProperty;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;

/**
 * A class that acts as a INDI for Java Driver for the Sky Quality Meter - LU.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.37, January 11, 2014
 */
public class I4JSQMDriver extends INDIDriver implements INDIConnectionHandler {

  /**
   * The serial and its reading and writting streams / readers.
   */
  private SerialPort serialPort;
  private OutputStream os;
  private BufferedReader br;
  /**
   * The PORTS property.
   */
  private INDIPortProperty portP;
  /**
   * Protocol Number property
   */
  private INDIOneElementTextProperty protocolNumberP;
  /**
   * Model Number property
   */
  private INDIOneElementTextProperty modelNumberP;
  /**
   * Feature Number property
   */
  private INDIOneElementTextProperty featureNumberP;
  /**
   * Serial Number property
   */
  private INDIOneElementTextProperty serialNumberP;
  /**
   * Sensor Reading property
   */
  private INDIOneElementNumberProperty sensorReadingP;
  /**
   * Sensor Frequency property
   */
  private INDIOneElementNumberProperty sensorFreqP;
  /**
   * Sensor Period (Cycles) property
   */
  private INDIOneElementNumberProperty sensorPeriodCP;
  /**
   * Sensor Period (Seconds) property
   */
  private INDIOneElementNumberProperty sensorPeriodSP;
  /**
   * Sensor Temperature property
   */
  private INDIOneElementNumberProperty sensorTempP;
  /**
   * Number of Readings property
   */
//  private INDIOneElementNumberProperty nReadingsP;
  /**
   * Do Readings Property
   */
  private INDISwitchOneOrNoneProperty doReadingP;

  /**
   * Constructs an instance of a
   * <code>I4JSQMDriver</code> with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages.
   * @param outputStream The stream to which to write the messages.
   */
  public I4JSQMDriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    serialPort = null;

    portP = INDIPortProperty.createSaveablePortProperty(this, "/dev/ttyUSB0");

    protocolNumberP = new INDIOneElementTextProperty(this, "protocolNumber", "Protocol Number", "Device Info", PropertyStates.IDLE, PropertyPermissions.RO, "-");
    modelNumberP = new INDIOneElementTextProperty(this, "modelNumber", "Model Number", "Device Info", PropertyStates.IDLE, PropertyPermissions.RO, "-");
    featureNumberP = new INDIOneElementTextProperty(this, "featureNumber", "Feature Number", "Device Info", PropertyStates.IDLE, PropertyPermissions.RO, "-");
    serialNumberP = new INDIOneElementTextProperty(this, "serialNumber", "Serial Number", "Device Info", PropertyStates.IDLE, PropertyPermissions.RO, "-");

    sensorReadingP = new INDIOneElementNumberProperty(this, "sensorReading", "Sensor Reading (m/sas)", "Readings", PropertyStates.IDLE, PropertyPermissions.RO, -100000, 100000, 0.01, "%5.2f", 0);
    sensorFreqP = new INDIOneElementNumberProperty(this, "sensorFrequency", "Sensor Frequency (Hz)", "Readings", PropertyStates.IDLE, PropertyPermissions.RO, 0, 1000000000, 1, "%10.0f", 0);
    sensorPeriodCP = new INDIOneElementNumberProperty(this, "sensorPeriodC", "Sensor Period (counts)", "Readings", PropertyStates.IDLE, PropertyPermissions.RO, 0, 1000000000, 1, "%10.0f", 0);
    sensorPeriodSP = new INDIOneElementNumberProperty(this, "sensorPeriodS", "Sensor Period (s)", "Readings", PropertyStates.IDLE, PropertyPermissions.RO, 0, 10000000, 0.001, "%10.3f", 0);
    sensorTempP = new INDIOneElementNumberProperty(this, "sensorTemperature", "Sensor Temperature (C)", "Readings", PropertyStates.IDLE, PropertyPermissions.RO, 0, 10000000, 0.1, "%4.1f", 0);

//    nReadingsP = INDIOneElementNumberProperty.createSaveableOneElementNumberProperty(this, "nReadings", "Number of Readings", "Settings", PropertyStates.IDLE, PropertyPermissions.RW, 1, 100, 1, "%1.0f", 50);

    doReadingP = new INDISwitchOneOrNoneProperty(this, "doReading", "Do Reading", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, "Do Reading", SwitchStatus.OFF);

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

    /*    if (property == nReadingsP) {
     int aux = elementsAndValues[0].getValue().intValue();
     if (aux < 1) {
     aux = 1;
     }

     if (aux > 100) {
     aux = 100;
     }

     nReadingsP.setValue(aux + "");
     nReadingsP.setState(PropertyStates.OK);
     try {
     updateProperty(nReadingsP);
     } catch (INDIException e) {
     e.printStackTrace();
     }
     }*/
  }

  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
  }

  @Override
  public void driverConnect(Date timestamp) throws INDIException {
    String portName = portP.getPort();

    try {
      CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
      serialPort = (SerialPort)portIdentifier.open(this.getClass().getName(), 2000);
      serialPort.setSerialPortParams(115200, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
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

//    this.addProperty(nReadingsP);
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

//    this.removeProperty(nReadingsP);
    this.removeProperty(doReadingP);
  }

  /**
   * Performs a reading of the sensor. This method populates the measurements
   * properties.
   */
  private void doReading() {
//    int nR = nReadingsP.getValue().intValue();

    /*    double[][] measurements = new double[nR][];

     double initialAvg = 0.0;

     for (int i = 0 ; i < nR ; i++) {
     measurements[i] = getMeasurement();

     initialAvg += measurements[i][0];
     }

     initialAvg /= nR;

     // Compute standard deviation
     double sigma = 0.0;
     for (int i = 0 ; i < nR ; i++) {
     sigma += (measurements[i][0] - initialAvg) * (measurements[i][0] - initialAvg);
     }

     sigma /= nR;
     sigma = Math.sqrt(sigma);

     // Compute sigma clipped average
     double[] sigmaClippedAverage = new double[5];
     int addedData = 0;

     for (int i = 0 ; i < nR ; i++) {
     if (Math.abs(measurements[i][0] - initialAvg) <= sigma) {
     addedData++;

     sigmaClippedAverage[0] += measurements[i][0];
     sigmaClippedAverage[1] += measurements[i][1];
     sigmaClippedAverage[2] += measurements[i][2];
     sigmaClippedAverage[3] += measurements[i][3];
     sigmaClippedAverage[4] += measurements[i][4];
     }
     }

     sigmaClippedAverage[0] /= addedData;
     sigmaClippedAverage[1] /= addedData;
     sigmaClippedAverage[2] /= addedData;
     sigmaClippedAverage[3] /= addedData;
     sigmaClippedAverage[4] /= addedData;

     */

    double[] measurements = getMeasurement();
    sensorReadingP.setValue(measurements[0]);
    sensorReadingP.setState(PropertyStates.OK);
    sensorFreqP.setValue(measurements[1]);
    sensorFreqP.setState(PropertyStates.OK);
    sensorPeriodCP.setValue(measurements[2]);
    sensorPeriodCP.setState(PropertyStates.OK);
    sensorPeriodSP.setValue(measurements[3]);
    sensorPeriodSP.setState(PropertyStates.OK);
    sensorTempP.setValue(measurements[4]);
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
   * @return An array of 5 elements: Magnitudes per square arc second, frequency
   * of sensor, period of sensor in counts, period of sensor in seconds, light
   * sensor temperature (ºC)
   */
  private double[] getMeasurement() {
    String answer;
    try {
      do {
        writeToSQM("rx");
        answer = readFromSQM();
       // answer = "r, 06.70m,0000022921Hz,0000000020c,0000000.000s, 039.4C\n";

        answer = answer.trim();
      } while (!answer.startsWith("r,"));
    } catch (IOException e) {
      e.printStackTrace();
      return null;
    }

    double[] res = new double[5];
    StringTokenizer st = new StringTokenizer(answer, ",", false);

    st.nextToken(); // Ignore "r"

    String aux = st.nextToken();
    aux = aux.substring(0, aux.length() - 1);
    res[0] = Double.parseDouble(aux);

    aux = st.nextToken();
    aux = aux.substring(0, aux.length() - 2);
    res[1] = Double.parseDouble(aux);

    aux = st.nextToken();
    aux = aux.substring(0, aux.length() - 1);
    res[2] = Double.parseDouble(aux);

    aux = st.nextToken();
    aux = aux.substring(0, aux.length() - 1);
    res[3] = Double.parseDouble(aux);

    aux = st.nextToken();
    aux = aux.substring(0, aux.length() - 1);
    res[4] = Double.parseDouble(aux);

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
      //  answer = "i,00000002,00000003,00000001,00000413\n";

        answer = answer.trim();
      } while (!answer.startsWith("i,"));
    } catch (IOException e) {
      e.printStackTrace();
      return;
    }

    StringTokenizer st = new StringTokenizer(answer, ",", false);

    st.nextToken(); // Ignore "i"

    protocolNumberP.setValue(st.nextToken());
    protocolNumberP.setState(PropertyStates.OK);
    modelNumberP.setValue(st.nextToken());
    modelNumberP.setState(PropertyStates.OK);
    featureNumberP.setValue(st.nextToken());
    featureNumberP.setState(PropertyStates.OK);
    serialNumberP.setValue(st.nextToken());
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
   * @throws IOException If there is any problem in the communication.
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
   * @param msg The String to be sent to the SQM.
   * @throws IOException If there is any problem in the communication.
   */
  private void writeToSQM(String msg) throws IOException {
    if (os != null) {
      os.write(msg.getBytes());
    }
  }
}

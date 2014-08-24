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
package laazotea.indi.raspberrypi;

import com.pi4j.io.gpio.GpioController;
import com.pi4j.io.gpio.GpioFactory;
import com.pi4j.io.gpio.GpioPin;
import com.pi4j.io.gpio.GpioPinDigitalInput;
import com.pi4j.io.gpio.GpioPinDigitalOutput;
import com.pi4j.io.gpio.GpioPinPwmOutput;
import com.pi4j.io.gpio.Pin;
import com.pi4j.io.gpio.PinPullResistance;
import com.pi4j.io.gpio.PinState;
import com.pi4j.io.gpio.RaspiPin;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.system.SystemInfo;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import laazotea.indi.Constants.LightStates;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIBLOBElementAndValue;
import laazotea.indi.driver.INDIBLOBProperty;
import laazotea.indi.driver.INDIConnectionHandler;
import laazotea.indi.driver.INDIDriver;
import laazotea.indi.driver.INDILightElement;
import laazotea.indi.driver.INDILightProperty;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDIOneElementTextProperty;
import laazotea.indi.driver.INDIProperty;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchOneOfManyProperty;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;

/**
 * A class that acts as a INDI for Java Driver for the Raspberry Pi GPIO port.
 * This driver makes use of the Pi4J library (http://pi4j.com/).
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.36, November 23, 2013
 */
public class I4JRaspberryPiGPIODriver extends INDIDriver implements INDIConnectionHandler, GpioPinListenerDigital {

  /**
   * GPIO Pins Config Property
   */
  private INDISwitchOneOfManyProperty[] pinsConfigP;
  /**
   * GPIO pins names Property
   */
  private INDITextProperty pinsNamesP;
  /**
   * GPIO pins names Elements
   */
  private INDITextElement[] pinsNamesE;
  /**
   * The number of GPIO Pins
   */
  private static final int NUMBER_OF_PINS = 21;
  /**
   * The GPIO Controller
   */
  private GpioController gpio;
  /**
   * The actual pins for the controller
   */
  private GpioPin[] pins;
  /**
   * Each of the controlling pins Properties (Switch for Output, Number for PWM
   * or Light for Input)
   */
  private INDIProperty[] pinsProperties;
  /**
   * An convenience array of the available pins
   */
  private static final Pin[] pinsArray = new Pin[]{RaspiPin.GPIO_00, RaspiPin.GPIO_01, RaspiPin.GPIO_02, RaspiPin.GPIO_03, RaspiPin.GPIO_04, RaspiPin.GPIO_05, RaspiPin.GPIO_06, RaspiPin.GPIO_07, RaspiPin.GPIO_08, RaspiPin.GPIO_09, RaspiPin.GPIO_10, RaspiPin.GPIO_11, RaspiPin.GPIO_12, RaspiPin.GPIO_13, RaspiPin.GPIO_14, RaspiPin.GPIO_15, RaspiPin.GPIO_16, RaspiPin.GPIO_17, RaspiPin.GPIO_18, RaspiPin.GPIO_19, RaspiPin.GPIO_20};
  /**
   * CPU Temperature Property
   */
  private INDINumberProperty cpuTemperatureP;
  /**
   * CPU Temperature Element
   */
  private INDINumberElement cpuTemperatureE;
  /**
   * Memory Property
   */
  private INDINumberProperty memoryP;
  /**
   * Used Memory Element
   */
  private INDINumberElement memoryUsedE;
  /**
   * Free Memory Element
   */
  private INDINumberElement memoryFreeE;
  /**
   * Buffers Element
   */
  private INDINumberElement memoryBuffersE;
  /**
   * Cached Memory Element
   */
  private INDINumberElement memoryCachedE;
  /**
   * Shared Memory Element
   */
  private INDINumberElement memorySharedE;
  /**
   * Uptime Property
   */
  private INDINumberProperty uptimeP;
  /**
   * Uptime Element
   */
  private INDINumberElement uptimeE;
  /**
   * Idle Uptime Element
   */
  private INDINumberElement uptimeIdleE;
  /**
   * Uptime (in Text format) Property
   */
  private INDITextProperty uptimeTextP;
  /**
   * Uptime (in Text format) Element
   */
  private INDITextElement uptimeTextE;
  /**
   * Idle Uptime (in Text format) Element
   */
  private INDITextElement uptimeIdleTextE;
  /**
   * A thread that reads Raspberry Pi sensors
   */
  private RaspberryPiSensorReaderThread readerThread;

  /**
   * Constructs an instance of a
   * <code>I4JRaspberryPiGPIODriver</code> with a particular
   * <code>inputStream<code> from which to read the incoming messages (from clients) and a
   * <code>outputStream</code> to write the messages to the clients.
   *
   * @param inputStream The stream from which to read messages.
   * @param outputStream The stream to which to write the messages.
   */
  public I4JRaspberryPiGPIODriver(InputStream inputStream, OutputStream outputStream) {
    super(inputStream, outputStream);

    pins = new GpioPin[NUMBER_OF_PINS];

    pinsProperties = new INDIProperty[NUMBER_OF_PINS];

    pinsNamesP = INDITextProperty.createSaveableTextProperty(this, "pin_names", "Pin Names", "Pin Names", PropertyStates.IDLE, PropertyPermissions.RW);
    pinsNamesE = new INDITextElement[NUMBER_OF_PINS];
    for (int i = 0 ; i < NUMBER_OF_PINS ; i++) {
      pinsNamesE[i] = pinsNamesP.getElement("pin_" + i + "name");

      if (pinsNamesE[i] == null) {
        pinsNamesE[i] = new INDITextElement(pinsNamesP, "pin_" + i + "_name", "Pin " + i + " Name", "" + i);
      }
    }

    pinsConfigP = new INDISwitchOneOfManyProperty[NUMBER_OF_PINS];

    for (int i = 0 ; i < pinsConfigP.length ; i++) {
      if (i == 1) {
        pinsConfigP[i] = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "pin_" + i + "_config", "GPIO " + i, "Configuration", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"Not Used", "Output", "Input", "Input - Pull Down", "Input - Pull Up", "Output - PWM"}, 0);
      } else {
        pinsConfigP[i] = INDISwitchOneOfManyProperty.createSaveableSwitchOneOfManyProperty(this, "pin_" + i + "_config", "GPIO " + i, "Configuration", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"Not Used", "Output", "Input", "Input - Pull Down", "Input - Pull Up"}, 0);
      }
    }

    try {
      INDITextProperty board = new INDITextProperty(this, "board", "Board", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDITextElement(board, "type", "Type", SystemInfo.getBoardType() + "");
      new INDITextElement(board, "revision", "Revision", SystemInfo.getRevision());
      new INDITextElement(board, "serial", "Serial Number", SystemInfo.getSerial());
      addProperty(board);

      INDIOneElementTextProperty bogoMIPS = new INDIOneElementTextProperty(this, "bogo_mips", "Bogo MIPS", "System Info", PropertyStates.OK, PropertyPermissions.RO, getBogoMIPS());
      addProperty(bogoMIPS);

      INDINumberProperty clockFrequencies = new INDINumberProperty(this, "clock_frequencies", "Clock Frequencies (MHz)", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDINumberElement(clockFrequencies, "arm", "Arm", (SystemInfo.getClockFrequencyArm() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "core", "Core", (SystemInfo.getClockFrequencyCore() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "dpi", "DPI", (SystemInfo.getClockFrequencyDPI() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "emmc", "EMMC", (SystemInfo.getClockFrequencyEMMC() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "h264", "H264", (SystemInfo.getClockFrequencyH264() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "hdmi", "HDMI", (SystemInfo.getClockFrequencyHDMI() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "isp", "ISP", (SystemInfo.getClockFrequencyISP() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "pixel", "Pixel", (SystemInfo.getClockFrequencyPixel() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "pwm", "PWM", (SystemInfo.getClockFrequencyPWM() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "arm", "Arm", (SystemInfo.getClockFrequencyArm() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "uart", "UART", (SystemInfo.getClockFrequencyUART() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "v3d", "V3D", (SystemInfo.getClockFrequencyV3D() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      new INDINumberElement(clockFrequencies, "vec", "VEC", (SystemInfo.getClockFrequencyVEC() / 1000000.0) + "", "0", "10000", "1", "%1.2f");
      addProperty(clockFrequencies);

      INDILightProperty codecs = new INDILightProperty(this, "codecs", "Codecs", "System Info", PropertyStates.OK);
      new INDILightElement(codecs, "h264", "H264", SystemInfo.getCodecH264Enabled() ? LightStates.OK : LightStates.IDLE);
      new INDILightElement(codecs, "mpg2", "MPG2", SystemInfo.getCodecMPG2Enabled() ? LightStates.OK : LightStates.IDLE);
      new INDILightElement(codecs, "wvc1", "WVC1", SystemInfo.getCodecWVC1Enabled() ? LightStates.OK : LightStates.IDLE);
      addProperty(codecs);

      INDITextProperty cpu = new INDITextProperty(this, "cpu", "CPU", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDITextElement(cpu, "processor", "Processor", getProcessor());
      new INDITextElement(cpu, "features", "Features", Arrays.toString(SystemInfo.getCpuFeatures()));
      new INDITextElement(cpu, "hardware", "Hardware", SystemInfo.getHardware());
      new INDITextElement(cpu, "architecture", "Architecture", SystemInfo.getCpuArchitecture());
      new INDITextElement(cpu, "implementer", "Implementer", SystemInfo.getCpuImplementer());
      new INDITextElement(cpu, "part", "Part", SystemInfo.getCpuPart());
      new INDITextElement(cpu, "revision", "Revision", SystemInfo.getCpuRevision());
      new INDITextElement(cpu, "variant", "Variant", SystemInfo.getCpuVariant());
      new INDITextElement(cpu, "voltage", "Voltage", SystemInfo.getCpuVoltage() + "");
      addProperty(cpu);

      INDINumberProperty memoryVoltage = new INDINumberProperty(this, "memory_voltages", "Memory Voltages", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDINumberElement(memoryVoltage, "voltage_sdram_c", "Voltage SDRAM C", SystemInfo.getMemoryVoltageSDRam_C() + "", "0", "100", "1", "%1.2f");
      new INDINumberElement(memoryVoltage, "voltage_sdram_i", "Voltage SDRAM I", SystemInfo.getMemoryVoltageSDRam_I() + "", "0", "100", "1", "%1.2f");
      new INDINumberElement(memoryVoltage, "voltage_sdram_p", "Voltage SDRAM P", SystemInfo.getMemoryVoltageSDRam_P() + "", "0", "100", "1", "%1.2f");
      addProperty(memoryVoltage);

      INDITextProperty os = new INDITextProperty(this, "os", "Operating System", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDITextElement(os, "name", "Name", SystemInfo.getOsName());
      new INDITextElement(os, "version", "Version", SystemInfo.getOsVersion());
      new INDITextElement(os, "architecture", "Architecture", SystemInfo.getOsArch());
      new INDITextElement(os, "firmware_build", "Firmware Build", SystemInfo.getOsFirmwareBuild());
      new INDITextElement(os, "firmware_date", "Firmware Date", SystemInfo.getOsFirmwareDate());
      new INDITextElement(os, "float_abi", "Float Abi", SystemInfo.isHardFloatAbi() ? "Hard" : "Soft");
      addProperty(os);

      INDITextProperty java = new INDITextProperty(this, "java", "Java", "System Info", PropertyStates.OK, PropertyPermissions.RO);
      new INDITextElement(java, "runtime", "Runtime", SystemInfo.getJavaRuntime());
      new INDITextElement(java, "version", "Version", SystemInfo.getJavaVersion());
      new INDITextElement(java, "virtual_machine", "Virtual Machine", SystemInfo.getJavaVirtualMachine());
      new INDITextElement(java, "vendor", "Vendor", SystemInfo.getJavaVendor());
      new INDITextElement(java, "vendor_url", "Vendor URL", SystemInfo.getJavaVendorUrl());
      addProperty(java);

      memoryP = new INDINumberProperty(this, "memory", "Memory (MB)", "Sensors", PropertyStates.OK, PropertyPermissions.RO);
      new INDINumberElement(memoryP, "total", "Total", (SystemInfo.getMemoryTotal() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");
      memoryUsedE = new INDINumberElement(memoryP, "used", "Used", (SystemInfo.getMemoryUsed() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");
      memoryFreeE = new INDINumberElement(memoryP, "free", "Free", (SystemInfo.getMemoryFree() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");
      memoryBuffersE = new INDINumberElement(memoryP, "buffers", "Buffers", (SystemInfo.getMemoryBuffers() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");
      memoryCachedE = new INDINumberElement(memoryP, "cached", "Cached", (SystemInfo.getMemoryCached() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");
      memorySharedE = new INDINumberElement(memoryP, "shared", "Shared", (SystemInfo.getMemoryShared() / (1024.0 * 1024.0)) + "", "0", "10000", "1", "%1.2f");

      cpuTemperatureP = new INDINumberProperty(this, "cpu_temperature", "CPU Temperature", "Sensors", PropertyStates.OK, PropertyPermissions.RO);
      cpuTemperatureE = new INDINumberElement(cpuTemperatureP, "cpu_temperature", "Temperature (°C)", SystemInfo.getCpuTemperature() + "", "0", "200", "1", "%1.2f");
    } catch (IOException | InterruptedException | ParseException e) {
    }

    uptimeP = new INDINumberProperty(this, "uptime", "System Uptime", "Sensors", PropertyStates.OK, PropertyPermissions.RO);
    uptimeE = new INDINumberElement(uptimeP, "uptime", "Uptime (secs)", "0", "0", "10000000000", "1", "%1.0f");
    uptimeIdleE = new INDINumberElement(uptimeP, "uptime_idle", "Idle Uptime (secs)", "0", "0", "10000000000", "1", "%1.0f");

    uptimeTextP = new INDITextProperty(this, "uptimet", "System Uptime (Text)", "Sensors", PropertyStates.OK, PropertyPermissions.RO);
    uptimeTextE = new INDITextElement(uptimeTextP, "uptimet", "Uptime", "0");
    uptimeIdleTextE = new INDITextElement(uptimeTextP, "uptime_idlet", "Idle Uptime", "0");
  }

  /**
   * Currently buggy in p4j 0.0.5 so disabled 
   * @return
   */
  private String getBogoMIPS() {
	 // return SystemInfo.getBogoMIPS();
	 return "n/a";
  }
  
  /**
   * Currently buggy in p4j 0.0.5 so disabled 
   * @return
   */
  private String getProcessor() {
	 // return SystemInfo.getProcessor();
	 return "n/a";
  }
  @Override
  public String getName() {
    return "Raspberry Pi GPIO";
  }

  @Override
  public void processNewNumberValue(INDINumberProperty property, Date Timestamp, INDINumberElementAndValue[] elementsAndValues) {
    int pin = -1;

    for (int i = 0 ; i < NUMBER_OF_PINS ; i++) {
      if (property == pinsProperties[i]) {
        pin = i;
      }
    }

    if (pin != -1) {  // The value is set to be updated
      INDINumberProperty p = (INDINumberProperty)pinsProperties[pin];

      int value = elementsAndValues[0].getValue().intValue();

      elementsAndValues[0].getElement().setValue("" + value);

      ((GpioPinPwmOutput)pins[pin]).setPwm(value);

      p.setState(PropertyStates.OK);

      try {
        updateProperty(p);
      } catch (INDIException e) {
      }
    }
  }

  @Override
  public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
    if (property == pinsNamesP) {
      property.setValues(elementsAndValues);
      pinsNamesP.setState(PropertyStates.OK);

      try {
        updateProperty(pinsNamesP);
      } catch (INDIException e) {
      }
    }
  }

  @Override
  public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
    // Process CONFIG OF PINS
    int pin = -1;

    for (int i = 0 ; i < NUMBER_OF_PINS ; i++) {
      if (property == pinsConfigP[i]) {
        pin = i;
      }
    }

    if (pin != -1) { // The property isd a pin Config
      if (pinsConfigP[pin].getSelectedIndex() != pinsConfigP[pin].getSelectedIndex(elementsAndValues)) { // The state changes
        pinsConfigP[pin].setSelectedIndex(elementsAndValues);

        if (pins[pin] != null) {
          removeProperty(pinsProperties[pin]);
          pinsProperties[pin] = null;
          gpio.unprovisionPin(pins[pin]);
          pins[pin] = null;
        }

        if (pinsConfigP[pin].getSelectedIndex() == 1) {
          createOutputPin(pin);
        } else if (pinsConfigP[pin].getSelectedIndex() == 2) {
          createInputPin(pin, PinPullResistance.OFF);
        } else if (pinsConfigP[pin].getSelectedIndex() == 3) {
          createInputPin(pin, PinPullResistance.PULL_DOWN);
        } else if (pinsConfigP[pin].getSelectedIndex() == 4) {
          createInputPin(pin, PinPullResistance.PULL_UP);
        } else if (pinsConfigP[pin].getSelectedIndex() == 5) {
          createPWMOutputPin(pin);
        }
      }

      pinsConfigP[pin].setState(PropertyStates.OK);

      try {
        updateProperty(pinsConfigP[pin]);
      } catch (INDIException e) {
      }
    }

    // Process Digital Output Pins
    pin = -1;

    for (int i = 0 ; i < NUMBER_OF_PINS ; i++) {
      if (property == pinsProperties[i]) {
        pin = i;
      }
    }

    if (pin != -1) {  // The value is set to be updated
      INDISwitchOneOfManyProperty p = (INDISwitchOneOfManyProperty)pinsProperties[pin];

      p.setSelectedIndex(elementsAndValues);

      int selected = p.getSelectedIndex();

      if (selected == 0) {
        ((GpioPinDigitalOutput)pins[pin]).high();
      } else {
        ((GpioPinDigitalOutput)pins[pin]).low();
      }

      p.setState(PropertyStates.OK);

      try {
        updateProperty(p);
      } catch (INDIException e) {
      }
    }
  }

  /**
   * Creates an Output Pin (Switch) Property.
   *
   * @param pin The pin that is going to be used as output
   */
  private void createOutputPin(int pin) {
    pins[pin] = gpio.provisionDigitalOutputPin(pinsArray[pin], PinState.LOW);
    pinsProperties[pin] = new INDISwitchOneOfManyProperty(this, "gpiopin_" + pin, "Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW, new String[]{"On", "Off"}, 1);
    addProperty(pinsProperties[pin]);
  }

  /**
   * Creates an PWM Pin (Number) Property.
   *
   * @param pin The pin that is going to be used as PWM
   */
  private void createPWMOutputPin(int pin) {
    pins[pin] = gpio.provisionPwmOutputPin(pinsArray[pin]);

    INDINumberProperty np = new INDINumberProperty(this, "gpiopin_" + pin, "Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")", "Main Control", PropertyStates.IDLE, PropertyPermissions.RW);
    pinsProperties[pin] = np;
    new INDINumberElement(np, "value", "PWM Value", 0.0, 0.0, 1000.0, 1.0, "%1.0f");
    addProperty(pinsProperties[pin]);
  }

  /**
   * Creates an Input Pin (Light) Property.
   *
   * @param pin The pin that is going to be used as input
   */
  private void createInputPin(int pin, PinPullResistance pull) {
    pins[pin] = gpio.provisionDigitalInputPin(pinsArray[pin], pull);
    ((GpioPinDigitalInput)pins[pin]).addListener(this);

    INDILightProperty lp = new INDILightProperty(this, "gpiopin_" + pin, "Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")", "Main Control", PropertyStates.IDLE);
    pinsProperties[pin] = lp;
    new INDILightElement(lp, "state", "State", LightStates.IDLE);
    addProperty(pinsProperties[pin]);
    updateInputPin(pin);
  }

  /**
   * Updates the Property for an Input pin.
   *
   * @param pin The pin to be updated.
   */
  private void updateInputPin(int pin) {
    PinState st = ((GpioPinDigitalInput)pins[pin]).getState();
    LightStates ls = LightStates.OK;

    if (st == PinState.LOW) {
      ls = LightStates.IDLE;
    }

    ((INDILightProperty)pinsProperties[pin]).getElement("state").setValue(ls);

    try {
      updateProperty(pinsProperties[pin]);
    } catch (INDIException e) {
    }
  }

  @Override
  public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
  }

  @Override
  public void driverConnect(Date timestamp) throws INDIException {
    gpio = GpioFactory.getInstance();

    for (int i = 0 ; i < pinsConfigP.length ; i++) {
      addProperty(pinsConfigP[i]);

      if (pinsConfigP[i].getSelectedIndex() == 1) {
        createOutputPin(i);
      } else if (pinsConfigP[i].getSelectedIndex() == 2) {
        createInputPin(i, PinPullResistance.OFF);
      } else if (pinsConfigP[i].getSelectedIndex() == 3) {
        createInputPin(i, PinPullResistance.PULL_DOWN);
      } else if (pinsConfigP[i].getSelectedIndex() == 4) {
        createInputPin(i, PinPullResistance.PULL_UP);
      } else if (pinsConfigP[i].getSelectedIndex() == 5) {
        createPWMOutputPin(i);
      }
    }

    addProperty(pinsNamesP);
    addProperty(cpuTemperatureP);
    addProperty(memoryP);
    addProperty(uptimeP);
    addProperty(uptimeTextP);

    readerThread = new RaspberryPiSensorReaderThread(this);
    readerThread.start();
  }

  @Override
  public void driverDisconnect(Date timestamp) throws INDIException {
    for (int i = 0 ; i < pinsConfigP.length ; i++) {
      removeProperty(pinsConfigP[i]);

      if (pinsProperties[i] != null) {
        removeProperty(pinsProperties[i]);
        pinsProperties[i] = null;
      }

      if (pins[i] != null) {
        gpio.unprovisionPin(pins[i]);
        pins[i] = null;
      }
    }

    removeProperty(pinsNamesP);
    removeProperty(cpuTemperatureP);
    removeProperty(memoryP);
    removeProperty(uptimeP);
    removeProperty(uptimeTextP);

    gpio.shutdown();

    readerThread.stopReading();
    readerThread = null;
  }

  @Override
  public void handleGpioPinDigitalStateChangeEvent(GpioPinDigitalStateChangeEvent event) {
    GpioPin p = event.getPin();

    for (int i = 0 ; i < NUMBER_OF_PINS ; i++) {
      if (p == pins[i]) {
        updateInputPin(i);
        break;
      }
    }
  }

  /**
   * Updates the sensor properties. Called periodically by
   * <code>RaspberryPiSensorReaderThread</code>.
   *
   * @see RaspberryPiSensorReaderThread
   */
  protected void setSensors() {
    try {
      memoryUsedE.setValue((SystemInfo.getMemoryUsed() / (1024.0 * 1024.0)) + "");
      memoryFreeE.setValue((SystemInfo.getMemoryFree() / (1024.0 * 1024.0)) + "");
      memoryBuffersE.setValue((SystemInfo.getMemoryBuffers() / (1024.0 * 1024.0)) + "");
      memoryCachedE.setValue((SystemInfo.getMemoryCached() / (1024.0 * 1024.0)) + "");
      memorySharedE.setValue((SystemInfo.getMemoryShared() / (1024.0 * 1024.0)) + "");

      cpuTemperatureE.setValue(SystemInfo.getCpuTemperature() + "");
    } catch (IOException | InterruptedException e) {
    }

    try {
      updateProperty(cpuTemperatureP);
      updateProperty(memoryP);
    } catch (INDIException e) {
    }

    try {
      Scanner sc = new Scanner(new FileInputStream("/proc/uptime"));
      String aux1 = sc.next();
      String aux2 = sc.next();
      sc.close();

      long ut = (Double.valueOf(aux1)).longValue();
      long uti = (Double.valueOf(aux2)).longValue();

      uptimeE.setValue(ut + "");
      uptimeIdleE.setValue(uti + "");

      uptimeTextE.setValue(secondsToWeeksDaysHoursMinutesSeconds(ut));
      uptimeIdleTextE.setValue(secondsToWeeksDaysHoursMinutesSeconds(uti));
    } catch (FileNotFoundException e) {
    }

    try {
      updateProperty(uptimeP);
      updateProperty(uptimeTextP);
    } catch (INDIException e) {
    }
  }

  /**
   * Converts a number of seconds into a string of the format <em>[X Weeks, ][X
   * Days, ][X Hours, ][X Minutes, ]X seconds</em>
   *
   * @param seconds The number of seconds to be converted.
   *
   * @return A <code>String</code> with the format <em>[X Weeks, ][X Days, ][X
   * Hours, ][X Minutes, ]X seconds</em>
   */
  private String secondsToWeeksDaysHoursMinutesSeconds(long seconds) {
    long accum = seconds;

    int weeks = (int)TimeUnit.SECONDS.toDays(accum) / 7;
    accum -= weeks * 7 * 24 * 60 * 60;

    int days = (int)TimeUnit.SECONDS.toDays(accum);
    accum -= days * 24 * 60 * 60;

    long hours = TimeUnit.SECONDS.toHours(accum);
    accum -= hours * 60 * 60;

    long minutes = TimeUnit.SECONDS.toMinutes(accum);
    accum -= minutes * 60;

    long secs = accum;

    String res = "";

    if (weeks > 0) {
      res += weeks;

      if (weeks == 1) {
        res += " Week, ";
      } else {
        res += " Weeks, ";
      }
    }

    if (days > 0) {
      res += days;

      if (days == 1) {
        res += " Day, ";
      } else {
        res += " Days, ";
      }
    }

    if (hours > 0) {
      res += hours;

      if (hours == 1) {
        res += " Hour, ";
      } else {
        res += " Hours, ";
      }
    }

    if (minutes > 0) {
      res += minutes;

      if (minutes == 1) {
        res += " Minute, ";
      } else {
        res += " Minutes, ";
      }
    }

    res += secs;

    if (seconds == 1) {
      res += " Second";
    } else {
      res += " Seconds";
    }

    return res;
  }
}

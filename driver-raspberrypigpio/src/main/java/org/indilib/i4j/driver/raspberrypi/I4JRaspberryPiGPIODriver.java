package org.indilib.i4j.driver.raspberrypi;

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

import com.pi4j.io.gpio.*;
import com.pi4j.io.gpio.event.GpioPinDigitalStateChangeEvent;
import com.pi4j.io.gpio.event.GpioPinListenerDigital;
import com.pi4j.system.SystemInfo;
import org.indilib.i4j.Constants.LightStates;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.*;
import org.indilib.i4j.driver.connection.INDIConnectionHandler;
import org.indilib.i4j.protocol.api.INDIConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

import static org.indilib.i4j.Constants.PropertyPermissions.RO;
import static org.indilib.i4j.Constants.PropertyStates.OK;
import static org.indilib.i4j.Constants.SwitchStatus.ON;

/**
 * A class that acts as a INDI for Java Driver for the Raspberry Pi GPIO port.
 * This driver makes use of the Pi4J library (http://pi4j.com/).
 * 
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class I4JRaspberryPiGPIODriver extends INDIDriver implements INDIConnectionHandler, GpioPinListenerDigital {

    /**
     * maximum uptime in seconds.
     */
    private static final double MAX_UPTIME_IN_SECONDS = 10000000000d;

    /**
     * maximum temperature in celcius.
     */
    private static final int MAX_CPU_TEMPERATURE = 200;

    /**
     * max voltage.
     */
    private static final int MAX_VOLTAGE = 100;

    /**
     * Number of seconds per minute.
     */
    private static final int SECONDS_PER_MINUTE = 60;

    /**
     * Number of minutes per hour.
     */
    private static final int MINUTES_PER_HOUR = 60;

    /**
     * Number of hours per day.
     */
    private static final int HOURS_PER_DAY = 24;

    /**
     * Number of days per week.
     */
    private static final int DAY_TO_WEEK_DIVIDER = 7;

    /**
     * Number of seconds per hour.
     */
    private static final int SECONDS_PER_HOUR = MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

    /**
     * Number of seconds per day.
     */
    private static final int SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR;

    /**
     * Number of seconds per week.
     */
    private static final int SECONDS_PER_WEEK = DAY_TO_WEEK_DIVIDER * SECONDS_PER_DAY;

    /**
     * option index for output pwm.
     */
    private static final int SWITCH_OUTPUT_PWM_INDEX = 5;

    /**
     * option index for input pull up.
     */
    private static final int SWITCH_INPUT_PULL_UP_INDEX = 4;

    /**
     * option index for input pull down.
     */
    private static final int SWITCH_INPUT_PULL_DOWN_INDEX = 3;

    /**
     * option index for input.
     */
    private static final int SWITCH_INPUT_INDEX = 2;

    /**
     * option index for output.
     */
    private static final int SWITCH_OUTPUT_INDEX = 1;

    /**
     * convert hz to megahez by dividing by.
     */
    private static final double HZ_TO_MEGAHERZ_DIVIDER = 1000000.0;

    /**
     * logger to use.
     */
    private static final Logger LOG = LoggerFactory.getLogger(I4JRaspberryPiGPIODriver.class);

    /**
     * max property value 10000.
     */
    private static final double MAX_VALUE_10000 = 10000;

    /**
     * the number of bytes in one megabyte.
     */
    private static final double ONE_MEGABYTE = 1024.0 * 1024.0;

    /**
     * maximum value for the puls wave modulator.
     */
    private static final double MAXIMUM_PWM_VALUE = 1000.0;

    /**
     * GPIO Pins Config Property.
     */
    private INDISwitchOneOfManyProperty[] pinsConfigP;

    /**
     * GPIO pins names Property.
     */
    private INDITextProperty pinsNamesP;

    /**
     * GPIO pins names Elements.
     */
    private INDITextElement[] pinsNamesE;

    /**
     * The number of GPIO Pins.
     */
    private static final int NUMBER_OF_PINS = 21;

    /**
     * The GPIO Controller.
     */
    private GpioController gpio;

    /**
     * The actual pins for the controller.
     */
    private GpioPin[] pins;

    /**
     * Each of the controlling pins Properties (Switch for Output, Number for
     * PWM or Light for Input).
     */
    private INDIProperty<?>[] pinsProperties;

    /**
     * An convenience array of the available pins.
     */
    private static final Pin[] PINS_ARRAY = new Pin[]{
        RaspiPin.GPIO_00,
        RaspiPin.GPIO_01,
        RaspiPin.GPIO_02,
        RaspiPin.GPIO_03,
        RaspiPin.GPIO_04,
        RaspiPin.GPIO_05,
        RaspiPin.GPIO_06,
        RaspiPin.GPIO_07,
        RaspiPin.GPIO_08,
        RaspiPin.GPIO_09,
        RaspiPin.GPIO_10,
        RaspiPin.GPIO_11,
        RaspiPin.GPIO_12,
        RaspiPin.GPIO_13,
        RaspiPin.GPIO_14,
        RaspiPin.GPIO_15,
        RaspiPin.GPIO_16,
        RaspiPin.GPIO_17,
        RaspiPin.GPIO_18,
        RaspiPin.GPIO_19,
        RaspiPin.GPIO_20
    };

    /**
     * CPU Temperature Property.
     */
    private INDINumberProperty cpuTemperatureP;

    /**
     * CPU Temperature Element.
     */
    private INDINumberElement cpuTemperatureE;

    /**
     * Memory Property.
     */
    private INDINumberProperty memoryP;

    /**
     * Used Memory Element.
     */
    private INDINumberElement memoryUsedE;

    /**
     * Free Memory Element.
     */
    private INDINumberElement memoryFreeE;

    /**
     * Buffers Element.
     */
    private INDINumberElement memoryBuffersE;

    /**
     * Cached Memory Element.
     */
    private INDINumberElement memoryCachedE;

    /**
     * Shared Memory Element.
     */
    private INDINumberElement memorySharedE;

    /**
     * Uptime Property.
     */
    private INDINumberProperty uptimeP;

    /**
     * Uptime Element.
     */
    private INDINumberElement uptimeE;

    /**
     * Idle Uptime Element.
     */
    private INDINumberElement uptimeIdleE;

    /**
     * Uptime (in Text format) Property.
     */
    private INDITextProperty uptimeTextP;

    /**
     * Uptime (in Text format) Element.
     */
    private INDITextElement uptimeTextE;

    /**
     * Idle Uptime (in Text format) Element.
     */
    private INDITextElement uptimeIdleTextE;

    /**
     * A thread that reads Raspberry Pi sensors.
     */
    private RaspberryPiSensorReaderThread readerThread;

    /**
     * Constructs an instance of a <code>I4JRaspberryPiGPIODriver</code> with a
     * particular <code>inputStream</code> from which to read the incoming
     * messages (from clients) and a <code>outputStream</code> to write the
     * messages to the clients.
     * 
     * @param connection
     *            the indi connection to the server.
     */
    public I4JRaspberryPiGPIODriver(INDIConnection connection) {
        super(connection);

        pins = new GpioPin[NUMBER_OF_PINS];

        pinsProperties = new INDIProperty[NUMBER_OF_PINS];

        pinsNamesP = newTextProperty().saveable(true).name("pin_names").label("Pin Names").group("Pin Names").create();
        pinsNamesE = new INDITextElement[NUMBER_OF_PINS];
        for (int i = 0; i < NUMBER_OF_PINS; i++) {
            pinsNamesE[i] = pinsNamesP.getElement("pin_" + i + "name");

            if (pinsNamesE[i] == null) {
                pinsNamesE[i] = pinsNamesP.newElement().name("pin_" + i + "_name").label("Pin " + i + " Name").textValue(Integer.toString(i)).create();
            }
        }

        pinsConfigP = new INDISwitchOneOfManyProperty[NUMBER_OF_PINS];

        for (int i = 0; i < pinsConfigP.length; i++) {
            pinsConfigP[i] = newProperty(INDISwitchOneOfManyProperty.class).saveable(true).name("pin_" + i + "_config").label("GPIO " + i).group("Configuration").create();
            pinsConfigP[i].newElement().name("Not Used").switchValue(ON).create();
            pinsConfigP[i].newElement().name("Output").create();
            pinsConfigP[i].newElement().name("Input").create();
            pinsConfigP[i].newElement().name("Input - Pull Down").create();
            pinsConfigP[i].newElement().name("Input - Pull Up").create();
            if (i == 1) {
                pinsConfigP[i].newElement().name("Output - PWM").create();
            }
        }

        try {
            INDITextProperty board = newTextProperty().name("board").label("Board").group("System Info").state(OK).permission(RO).create();
            board.newElement().name("type").label("Type").textValue("" + SystemInfo.getBoardType()).create();
            board.newElement().name("revision").label("Revision").textValue(SystemInfo.getRevision()).create();
            board.newElement().name("serial").label("Serial Number").textValue(SystemInfo.getSerial()).create();

            addProperty(board);

            INDITextProperty bogoMIPS = newTextProperty().name("bogo_mips").label("Bogo MIPS").group("System Info").state(OK).permission(RO).create();
            bogoMIPS.newElement().textValue(getBogoMIPS()).create();
            addProperty(bogoMIPS);

            INDINumberProperty clockFrequencies =
                    newNumberProperty().name("clock_frequencies").label("Clock Frequencies (MHz)").group("System Info").state(OK).permission(RO).create();
            clockFrequencies.newElement().name("arm").label("Arm").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyArm() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("core").label("Core").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyCore() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("dpi").label("DPI").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyDPI() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("emmc").label("EMMC").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyEMMC() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("h264").label("H264").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyH264() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("hdmi").label("HDMI").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyHDMI() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("isp").label("ISP").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyISP() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("pixel").label("Pixel").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyPixel() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("pwm").label("PWM").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyPWM() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("arm").label("Arm").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyArm() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("uart").label("UART").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyUART() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("v3d").label("V3D").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyV3D() / HZ_TO_MEGAHERZ_DIVIDER).create();
            clockFrequencies.newElement().name("vec").label("VEC").maximum(MAX_VALUE_10000).step(1d).numberFormat("%1.2f")
                    .numberValue(SystemInfo.getClockFrequencyVEC() / HZ_TO_MEGAHERZ_DIVIDER).create();

            addProperty(clockFrequencies);

            INDILightProperty codecs = newLightProperty().name("codecs").label("Codecs").group("System Info").state(OK).create();
            codecs.newElement().name("h264").label("H264").state(SystemInfo.getCodecH264Enabled() ? LightStates.OK : LightStates.IDLE).create();
            codecs.newElement().name("mpg2").label("MPG2").state(SystemInfo.getCodecMPG2Enabled() ? LightStates.OK : LightStates.IDLE).create();
            codecs.newElement().name("wvc1").label("WVC1").state(SystemInfo.getCodecWVC1Enabled() ? LightStates.OK : LightStates.IDLE).create();
            addProperty(codecs);

            INDITextProperty cpu = newTextProperty().name("cpu").label("CPU").group("System Info").state(OK).permission(RO).create();
            cpu.newElement().name("processor").label("Processor").textValue(getProcessor()).create();
            cpu.newElement().name("features").label("Features").textValue(Arrays.toString(SystemInfo.getCpuFeatures())).create();
            cpu.newElement().name("hardware").label("Hardware").textValue(SystemInfo.getHardware()).create();
            cpu.newElement().name("architecture").label("Architecture").textValue(SystemInfo.getCpuArchitecture()).create();
            cpu.newElement().name("implementer").label("Implementer").textValue(SystemInfo.getCpuImplementer()).create();
            cpu.newElement().name("part").label("Part").textValue(SystemInfo.getCpuPart()).create();
            cpu.newElement().name("revision").label("Revision").textValue(SystemInfo.getCpuRevision()).create();
            cpu.newElement().name("variant").label("Variant").textValue(SystemInfo.getCpuVariant()).create();
            cpu.newElement().name("voltage").label("Voltage").textValue(SystemInfo.getCpuVoltage() + "").create();
            addProperty(cpu);

            INDINumberProperty memoryVoltage = newNumberProperty().name("memory_voltages").label("Memory Voltages").group("System Info").state(OK).permission(RO).create();
            memoryVoltage.newElement().name("voltage_sdram_c").label("Voltage SDRAM C").numberValue(SystemInfo.getMemoryVoltageSDRam_C()).maximum(MAX_VOLTAGE).step(1)
                    .numberFormat("%1.2f").create();
            memoryVoltage.newElement().name("voltage_sdram_i").label("Voltage SDRAM I").numberValue(SystemInfo.getMemoryVoltageSDRam_I()).maximum(MAX_VOLTAGE).step(1)
                    .numberFormat("%1.2f").create();
            memoryVoltage.newElement().name("voltage_sdram_p").label("Voltage SDRAM P").numberValue(SystemInfo.getMemoryVoltageSDRam_P()).maximum(MAX_VOLTAGE).step(1)
                    .numberFormat("%1.2f").create();
            addProperty(memoryVoltage);

            INDITextProperty os = newTextProperty().name("os").label("Operating System").group("System Info").state(OK).permission(RO).create();
            os.newElement().name("name").label("Name").textValue(SystemInfo.getOsName()).create();
            os.newElement().name("version").label("Version").textValue(SystemInfo.getOsVersion()).create();
            os.newElement().name("architecture").label("Architecture").textValue(SystemInfo.getOsArch()).create();
            os.newElement().name("firmware_build").label("Firmware Build").textValue(SystemInfo.getOsFirmwareBuild()).create();
            os.newElement().name("firmware_date").label("Firmware Date").textValue(SystemInfo.getOsFirmwareDate()).create();
            os.newElement().name("float_abi").label("Float Abi").textValue(SystemInfo.isHardFloatAbi() ? "Hard" : "Soft").create();
            addProperty(os);

            INDITextProperty java = newTextProperty().name("java").label("Java").group("System Info").state(OK).permission(RO).create();
            java.newElement().name("runtime").label("Runtime").textValue(SystemInfo.getJavaRuntime()).create();
            java.newElement().name("version").label("Version").textValue(SystemInfo.getJavaVersion()).create();
            java.newElement().name("virtual_machine").label("Virtual Machine").textValue(SystemInfo.getJavaVirtualMachine()).create();
            java.newElement().name("vendor").label("Vendor").textValue(SystemInfo.getJavaVendor()).create();
            java.newElement().name("vendor_url").label("Vendor URL").textValue(SystemInfo.getJavaVendorUrl()).create();
            addProperty(java);

            memoryP = newNumberProperty().name("memory").label("Memory (MB)").group("Sensors").state(OK).permission(RO).create();
            memoryP.newElement().name("total").label("Total").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryTotal() / ONE_MEGABYTE).create();

            memoryUsedE = memoryP.newElement().name("used").label("Used").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryUsed() / ONE_MEGABYTE).create();
            memoryFreeE = memoryP.newElement().name("free").label("Free").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryFree() / ONE_MEGABYTE).create();
            memoryBuffersE = memoryP.newElement().name("buffers").label("Buffers").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryBuffers() / ONE_MEGABYTE).create();
            memoryCachedE = memoryP.newElement().name("cached").label("Cached").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryCached() / ONE_MEGABYTE).create();
            memorySharedE = memoryP.newElement().name("shared").label("Shared").step(1d).maximum(MAX_VALUE_10000).numberFormat("%1.2f")//
                    .numberValue(SystemInfo.getMemoryShared() / ONE_MEGABYTE).create();

            cpuTemperatureP = newNumberProperty().name("cpu_temperature").label("CPU Temperature").group("Sensors").state(OK).permission(RO).create();
            cpuTemperatureE = cpuTemperatureP.newElement().name("cpu_temperature").label("Temperature (°C)").step(1).numberFormat("%1.2f").maximum(MAX_CPU_TEMPERATURE)//
                    .numberValue(SystemInfo.getCpuTemperature()).create();
        } catch (IOException | InterruptedException | ParseException e) {
            LOG.error("exception during property creation", e);
        }

        uptimeP = newNumberProperty().name("uptime").label("System Uptime").group("Sensors").state(OK).permission(RO).create();
        uptimeE = uptimeP.newElement().name("uptime").label("Uptime (secs)").step(1).numberFormat("%1.0f").maximum(MAX_UPTIME_IN_SECONDS).create();
        uptimeIdleE = uptimeP.newElement().name("uptime_idle").label("Idle Uptime (secs)").step(1).numberFormat("%1.0f").maximum(MAX_UPTIME_IN_SECONDS).create();

        uptimeTextP = newTextProperty().name("uptimet").label("System Uptime (Text)").group("Sensors").state(OK).permission(RO).create();
        uptimeTextE = uptimeTextP.newElement().name("uptimet").label("Uptime").textValue("0").create();
        uptimeIdleTextE = uptimeTextP.newElement().name("uptime_idlet").label("Idle Uptime").textValue("0").create();
    }

    /**
     * Currently buggy in p4j 0.0.5 so disabled.
     * 
     * @return bogo mips
     */
    private String getBogoMIPS() {
        // return SystemInfo.getBogoMIPS();
        return "n/a";
    }

    /**
     * Currently buggy in p4j 0.0.5 so disabled.
     * 
     * @return processor description.
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
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) {
        int pin = -1;

        for (int i = 0; i < NUMBER_OF_PINS; i++) {
            if (property == pinsProperties[i]) {
                pin = i;
            }
        }

        if (pin != -1) { // The value is set to be updated
            INDINumberProperty p = (INDINumberProperty) pinsProperties[pin];

            int value = elementsAndValues[0].getValue().intValue();

            elementsAndValues[0].getElement().setValue("" + value);

            ((GpioPinPwmOutput) pins[pin]).setPwm(value);

            p.setState(PropertyStates.OK);

            updateProperty(p);

        }
    }

    @Override
    public void processNewTextValue(INDITextProperty property, Date timestamp, INDITextElementAndValue[] elementsAndValues) {
        if (property == pinsNamesP) {
            property.setValues(elementsAndValues);
            pinsNamesP.setState(PropertyStates.OK);

            updateProperty(pinsNamesP);

        }
    }

    @Override
    public void processNewSwitchValue(INDISwitchProperty property, Date timestamp, INDISwitchElementAndValue[] elementsAndValues) {
        // Process CONFIG OF PINS
        int pin = -1;

        for (int i = 0; i < NUMBER_OF_PINS; i++) {
            if (property == pinsConfigP[i]) {
                pin = i;
            }
        }

        if (pin != -1) { // The property isd a pin Config
            if (pinsConfigP[pin].getSelectedIndex() != pinsConfigP[pin].getSelectedIndex(elementsAndValues)) { // The
                                                                                                               // state
                                                                                                               // changes
                pinsConfigP[pin].setSelectedIndex(elementsAndValues);

                if (pins[pin] != null) {
                    removeProperty(pinsProperties[pin]);
                    pinsProperties[pin] = null;
                    gpio.unprovisionPin(pins[pin]);
                    pins[pin] = null;
                }

                if (pinsConfigP[pin].getSelectedIndex() == SWITCH_OUTPUT_INDEX) {
                    createOutputPin(pin);
                } else if (pinsConfigP[pin].getSelectedIndex() == SWITCH_INPUT_INDEX) {
                    createInputPin(pin, PinPullResistance.OFF);
                } else if (pinsConfigP[pin].getSelectedIndex() == SWITCH_INPUT_PULL_DOWN_INDEX) {
                    createInputPin(pin, PinPullResistance.PULL_DOWN);
                } else if (pinsConfigP[pin].getSelectedIndex() == SWITCH_INPUT_PULL_UP_INDEX) {
                    createInputPin(pin, PinPullResistance.PULL_UP);
                } else if (pinsConfigP[pin].getSelectedIndex() == SWITCH_OUTPUT_PWM_INDEX) {
                    createPWMOutputPin(pin);
                }
            }

            pinsConfigP[pin].setState(PropertyStates.OK);

            updateProperty(pinsConfigP[pin]);

        }

        // Process Digital Output Pins
        pin = -1;

        for (int i = 0; i < NUMBER_OF_PINS; i++) {
            if (property == pinsProperties[i]) {
                pin = i;
            }
        }

        if (pin != -1) { // The value is set to be updated
            INDISwitchOneOfManyProperty p = (INDISwitchOneOfManyProperty) pinsProperties[pin];

            p.setSelectedIndex(elementsAndValues);

            int selected = p.getSelectedIndex();

            if (selected == 0) {
                ((GpioPinDigitalOutput) pins[pin]).high();
            } else {
                ((GpioPinDigitalOutput) pins[pin]).low();
            }

            p.setState(PropertyStates.OK);

            updateProperty(p);

        }
    }

    /**
     * Creates an Output Pin (Switch) Property.
     * 
     * @param pin
     *            The pin that is going to be used as output
     */
    private void createOutputPin(int pin) {
        pins[pin] = gpio.provisionDigitalOutputPin(PINS_ARRAY[pin], PinState.LOW);
        pinsProperties[pin] = newProperty(INDISwitchOneOfManyProperty.class)//
                .name("gpiopin_" + pin).label("Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        pinsProperties[pin].newElement().name("On").switchValue(ON).create();
        pinsProperties[pin].newElement().name("Off").create();
        addProperty(pinsProperties[pin]);
    }

    /**
     * Creates an PWM Pin (Number) Property.
     * 
     * @param pin
     *            The pin that is going to be used as PWM
     */
    private void createPWMOutputPin(int pin) {
        pins[pin] = gpio.provisionPwmOutputPin(PINS_ARRAY[pin]);

        INDINumberProperty np = newNumberProperty()//
                .name("gpiopin_" + pin).label("Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")").group(INDIDriver.GROUP_MAIN_CONTROL).create();
        np.newElement().name("value").label("PWM Value").maximum(MAXIMUM_PWM_VALUE).step(1).numberFormat("%1.0f").create();
        pinsProperties[pin] = np;
        addProperty(pinsProperties[pin]);
    }

    /**
     * Creates an Input Pin (Light) Property.
     * 
     * @param pin
     *            The pin that is going to be used as input
     * @param pull
     *            Pin pull up/down resistance definition.
     */
    private void createInputPin(int pin, PinPullResistance pull) {
        pins[pin] = gpio.provisionDigitalInputPin(PINS_ARRAY[pin], pull);
        ((GpioPinDigitalInput) pins[pin]).addListener(this);

        INDILightProperty lp = newLightProperty()//
                .name("gpiopin_" + pin).label("Pin " + pin + " (" + pinsNamesE[pin].getValue() + ")").group(INDIDriver.GROUP_MAIN_CONTROL).create();

        pinsProperties[pin] = lp;
        lp.newElement().name("state").label("State").create();
        addProperty(pinsProperties[pin]);
        updateInputPin(pin);
    }

    /**
     * Updates the Property for an Input pin.
     * 
     * @param pin
     *            The pin to be updated.
     */
    private void updateInputPin(int pin) {
        PinState st = ((GpioPinDigitalInput) pins[pin]).getState();
        LightStates ls = LightStates.OK;

        if (st == PinState.LOW) {
            ls = LightStates.IDLE;
        }

        ((INDILightProperty) pinsProperties[pin]).getElement("state").setValue(ls);

        updateProperty(pinsProperties[pin]);

    }

    @Override
    public void processNewBLOBValue(INDIBLOBProperty property, Date timestamp, INDIBLOBElementAndValue[] elementsAndValues) {
    }

    @Override
    public void driverConnect(Date timestamp) throws INDIException {
        gpio = GpioFactory.getInstance();

        for (int i = 0; i < pinsConfigP.length; i++) {
            addProperty(pinsConfigP[i]);

            if (pinsConfigP[i].getSelectedIndex() == SWITCH_OUTPUT_INDEX) {
                createOutputPin(i);
            } else if (pinsConfigP[i].getSelectedIndex() == SWITCH_INPUT_INDEX) {
                createInputPin(i, PinPullResistance.OFF);
            } else if (pinsConfigP[i].getSelectedIndex() == SWITCH_INPUT_PULL_DOWN_INDEX) {
                createInputPin(i, PinPullResistance.PULL_DOWN);
            } else if (pinsConfigP[i].getSelectedIndex() == SWITCH_INPUT_PULL_UP_INDEX) {
                createInputPin(i, PinPullResistance.PULL_UP);
            } else if (pinsConfigP[i].getSelectedIndex() == SWITCH_OUTPUT_PWM_INDEX) {
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
        for (int i = 0; i < pinsConfigP.length; i++) {
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

        for (int i = 0; i < NUMBER_OF_PINS; i++) {
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
            memoryUsedE.setValue(SystemInfo.getMemoryUsed() / ONE_MEGABYTE + "");
            memoryFreeE.setValue(SystemInfo.getMemoryFree() / ONE_MEGABYTE + "");
            memoryBuffersE.setValue(SystemInfo.getMemoryBuffers() / ONE_MEGABYTE + "");
            memoryCachedE.setValue(SystemInfo.getMemoryCached() / ONE_MEGABYTE + "");
            memorySharedE.setValue(SystemInfo.getMemoryShared() / ONE_MEGABYTE + "");

            cpuTemperatureE.setValue(SystemInfo.getCpuTemperature() + "");
        } catch (IOException | InterruptedException e) {
            LOG.error("io exception", e);
        }

        updateProperty(cpuTemperatureP);
        updateProperty(memoryP);

        try {
            Scanner sc = new Scanner(new FileInputStream("/proc/uptime"));
            String aux1 = sc.next();
            String aux2 = sc.next();
            sc.close();

            long ut = Double.valueOf(aux1).longValue();
            long uti = Double.valueOf(aux2).longValue();

            uptimeE.setValue(ut + "");
            uptimeIdleE.setValue(uti + "");

            uptimeTextE.setValue(secondsToWeeksDaysHoursMinutesSeconds(ut));
            uptimeIdleTextE.setValue(secondsToWeeksDaysHoursMinutesSeconds(uti));
        } catch (FileNotFoundException e) {
            LOG.error("file not found", e);
        }

        updateProperty(uptimeP);
        updateProperty(uptimeTextP);

    }

    /**
     * Converts a number of seconds into a string of the format
     * <em>[X Weeks, ][X Days, ][X Hours, ][X Minutes, ]X seconds</em>.
     * 
     * @param seconds
     *            The number of seconds to be converted.
     * @return A <code>String</code> with the format <em>[X Weeks, ][X Days, ][X
     * Hours, ][X Minutes, ]X seconds</em>
     */
    private String secondsToWeeksDaysHoursMinutesSeconds(long seconds) {
        long accum = seconds;

        int weeks = (int) TimeUnit.SECONDS.toDays(accum) / DAY_TO_WEEK_DIVIDER;
        accum -= weeks * SECONDS_PER_WEEK;

        int days = (int) TimeUnit.SECONDS.toDays(accum);
        accum -= days * SECONDS_PER_DAY;

        long hours = TimeUnit.SECONDS.toHours(accum);
        accum -= hours * SECONDS_PER_HOUR;

        long minutes = TimeUnit.SECONDS.toMinutes(accum);
        accum -= minutes * SECONDS_PER_MINUTE;

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

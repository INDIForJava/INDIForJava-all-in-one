package laazotea.indi.driver.serial;

import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import jssc.SerialPort;
import jssc.SerialPortException;
import laazotea.indi.INDIException;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.driver.INDIDriver;
import laazotea.indi.driver.INDIDriverExtention;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;
import laazotea.indi.driver.annotation.InjectElement;
import laazotea.indi.driver.annotation.InjectProperty;
import laazotea.indi.driver.event.TextEvent;

public class INDISerialPortExtention extends INDIDriverExtention<INDIDriver> {

    private static Logger LOG = Logger.getLogger(INDISerialPortExtention.class.getName());

    protected static final String OPTIONS_TAB = "Options";

    @InjectProperty(name = "PORTS", label = "Ports", group = OPTIONS_TAB, saveable = true)
    protected INDITextProperty port;

    @InjectElement(name = "PORT", label = "Port", valueT = "/dev/ttyUSB0")
    protected INDITextElement portElement;

    private INDISerialPortInterface servialPortInterface;

    private SerialPort serialPort;

    private int baudrate = SerialPort.BAUDRATE_4800;

    private int databits = SerialPort.DATABITS_8;

    private int stopbits = SerialPort.STOPBITS_1;

    private int parity = SerialPort.PARITY_NONE;

    private Thread shutdownHook;

    public INDISerialPortExtention(INDIDriver driver) {
        super(driver);
        if (!isActive()) {
            return;
        }
        this.port.setEventHandler(new TextEvent() {

            @Override
            public void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues) {
                property.setValues(elementsAndValues);
                property.setState(PropertyStates.OK);
                try {
                    updateProperty(property);
                } catch (INDIException e) {
                }
            }
        });
        servialPortInterface = (INDISerialPortInterface) driver;
        this.shutdownHook = new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    if (serialPort != null) {
                        serialPort.closePort();
                    }
                } catch (Exception e) {
                }
            }
        });
        Runtime.getRuntime().addShutdownHook(this.shutdownHook);
    }

    private void handleSerialException(SerialPortException e) {
        try {
            updateProperty(port, "Serial port error " + e.getMessage());
        } catch (INDIException e1) {
        }
        LOG.log(Level.SEVERE, "Serial port error", e);
    }

    public synchronized boolean close() {
        try {
            if (this.serialPort != null) {
                this.serialPort.closePort();// Close serial port
            }
            this.serialPort = null;
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    @Override
    public void connect() {
        if (!isActive()) {
            return;
        }
        driver.addProperty(port);
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        close();
        driver.removeProperty(port);
    }

    public SerialPort getOpenSerialPort() {
        if (serialPort == null || !serialPort.isOpened()) {
            open();
        }
        return serialPort;
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDISerialPortInterface;
    }

    public synchronized boolean open() {
        if (this.serialPort != null) {
            close();
        }
        try {
            this.serialPort = new SerialPort(portElement.getValue());
            this.serialPort.openPort();// Open serial port
            this.serialPort.setParams(baudrate, databits, stopbits, parity);
            return true;
        } catch (SerialPortException e) {
            handleSerialException(e);
            return false;
        }
    }

    public INDISerialPortExtention setBaudrate(int baudrate) {
        this.baudrate = baudrate;
        return this;
    }

    public INDISerialPortExtention setDatabits(int databits) {
        this.databits = databits;
        return this;
    }

    public INDISerialPortExtention setParity(int parity) {
        this.parity = parity;
        return this;
    }

    public INDISerialPortExtention setStopbits(int stopbits) {
        this.stopbits = stopbits;
        return this;
    }
}

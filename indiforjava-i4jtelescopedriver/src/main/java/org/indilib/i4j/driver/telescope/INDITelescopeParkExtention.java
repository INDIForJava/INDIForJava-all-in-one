package org.indilib.i4j.driver.telescope;

import java.util.Date;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.event.SwitchEvent;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.INDIException;
import static org.indilib.i4j.Constants.PropertyStates.IDLE;

public class INDITelescopeParkExtention extends INDIDriverExtension<INDITelescope> {

    @InjectProperty(name = "TELESCOPE_PARK", label = "Park", group = INDITelescope.MAIN_CONTROL_TAB)
    private INDISwitchProperty park;

    @InjectElement(name = "PARK", label = "Park")
    private INDISwitchElement parkElement;

    private INDITelescopeParkInterface parkInterface;

    public INDITelescopeParkExtention(INDITelescope telecopeDriver) {
        super(telecopeDriver);
        if (!isActive()) {
            return;
        }
        park.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                parkInterface.park();
            }
        });
        parkInterface = (INDITelescopeParkInterface) telecopeDriver;
    }

    @Override
    public void connect() {
        if (!isActive()) {
            return;
        }
        driver.addProperty(park);
    }

    @Override
    public void disconnect() {
        if (!isActive()) {
            return;
        }
        driver.removeProperty(park);
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDITelescopeParkInterface;
    }

    public boolean isBusy() {
        if (!isActive()) {
            return false;
        }
        return park.getState() == PropertyStates.BUSY;
    }

    public void setIdle() {
        if (!isActive()) {
            return;
        }
        this.park.setState(IDLE);
        park.resetAllSwitches();
        try {
            driver.updateProperty(this.park);
        } catch (INDIException e) {
        }
    }

    public void setNotBussy() {
        if (!isActive()) {
            return;
        }
        if (this.park.getState() == PropertyStates.BUSY) {
            this.park.setState(IDLE);
        }

    }

}

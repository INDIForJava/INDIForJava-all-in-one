package org.indilib.i4j.driver.event;

import java.util.Date;

import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.Constants.SwitchStatus;

public abstract class SwitchEvent implements IEventHandler<INDISwitchProperty, INDISwitchElement, SwitchStatus> {

    protected INDISwitchProperty property;

    @Override
    public final void processNewValue(INDISwitchProperty property, Date date, INDIElementAndValue<INDISwitchElement, SwitchStatus>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDISwitchElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues);
}

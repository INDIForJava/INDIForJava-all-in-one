package org.indilib.i4j.driver.event;

import java.util.Date;

import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;

public abstract class NumberEvent implements IEventHandler<INDINumberProperty, INDINumberElement, Double> {

    protected INDINumberProperty property;

    @Override
    public final void processNewValue(INDINumberProperty property, Date date, INDIElementAndValue<INDINumberElement, Double>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDINumberElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues);
}

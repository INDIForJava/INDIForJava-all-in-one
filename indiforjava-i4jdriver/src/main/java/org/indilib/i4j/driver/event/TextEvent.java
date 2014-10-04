package org.indilib.i4j.driver.event;

import java.util.Date;

import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDITextElement;
import org.indilib.i4j.driver.INDITextElementAndValue;
import org.indilib.i4j.driver.INDITextProperty;

public abstract class TextEvent implements IEventHandler<INDITextProperty, INDITextElement, String> {

    protected INDITextProperty property;

    @Override
    public final void processNewValue(INDITextProperty property, Date date, INDIElementAndValue<INDITextElement, String>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDITextElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues);
}

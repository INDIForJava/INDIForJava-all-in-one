package laazotea.indi.driver.event;

import java.util.Date;

import laazotea.indi.driver.INDIElementAndValue;
import laazotea.indi.driver.INDITextElement;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;

public abstract class TextEvent implements IEventHandler<INDITextProperty, INDITextElement, String> {

    protected INDITextProperty property;

    @Override
    public final void processNewValue(INDITextProperty property, Date date, INDIElementAndValue<INDITextElement, String>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDITextElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDITextElementAndValue[] elementsAndValues);
}

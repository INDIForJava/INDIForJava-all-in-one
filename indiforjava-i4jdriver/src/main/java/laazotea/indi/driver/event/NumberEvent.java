package laazotea.indi.driver.event;

import java.util.Date;

import laazotea.indi.driver.INDIElementAndValue;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;

public abstract class NumberEvent implements IEventHandler<INDINumberProperty, INDINumberElement, Double> {

    protected INDINumberProperty property;

    @Override
    public final void processNewValue(INDINumberProperty property, Date date, INDIElementAndValue<INDINumberElement, Double>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDINumberElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues);
}

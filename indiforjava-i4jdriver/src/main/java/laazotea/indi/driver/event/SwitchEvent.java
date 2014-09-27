package laazotea.indi.driver.event;

import java.util.Date;

import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.driver.INDIElementAndValue;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;

public abstract class SwitchEvent implements IEventHandler<INDISwitchProperty, INDISwitchElement, SwitchStatus> {

    protected INDISwitchProperty property;

    @Override
    public final void processNewValue(INDISwitchProperty property, Date date, INDIElementAndValue<INDISwitchElement, SwitchStatus>[] elementsAndValues) {
        this.property = property;
        processNewValue(date, (INDISwitchElementAndValue[]) elementsAndValues);
    }

    public abstract void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues);
}

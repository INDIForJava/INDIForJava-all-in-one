package laazotea.indi.driver.event;

import java.util.Date;

import laazotea.indi.driver.INDIElement;
import laazotea.indi.driver.INDIElementAndValue;
import laazotea.indi.driver.INDIProperty;

public interface IEventHandler<Prop extends INDIProperty<Elem>, Elem extends INDIElement, Type> {

    void processNewValue(Prop property, Date date, INDIElementAndValue<Elem, Type>[] elementsAndValues);
}

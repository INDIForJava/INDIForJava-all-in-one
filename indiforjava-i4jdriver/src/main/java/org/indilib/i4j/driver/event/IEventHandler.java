package org.indilib.i4j.driver.event;

import java.util.Date;

import org.indilib.i4j.driver.INDIElement;
import org.indilib.i4j.driver.INDIElementAndValue;
import org.indilib.i4j.driver.INDIProperty;

public interface IEventHandler<Prop extends INDIProperty<Elem>, Elem extends INDIElement, Type> {

    void processNewValue(Prop property, Date date, INDIElementAndValue<Elem, Type>[] elementsAndValues);
}

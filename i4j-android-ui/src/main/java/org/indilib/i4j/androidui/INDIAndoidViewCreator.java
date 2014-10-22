package org.indilib.i4j.androidui;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.client.INDIBLOBElement;
import org.indilib.i4j.client.INDIBLOBProperty;
import org.indilib.i4j.client.INDIDevice;
import org.indilib.i4j.client.INDIDeviceListener;
import org.indilib.i4j.client.INDIElementListener;
import org.indilib.i4j.client.INDILightElement;
import org.indilib.i4j.client.INDILightProperty;
import org.indilib.i4j.client.INDINumberElement;
import org.indilib.i4j.client.INDINumberProperty;
import org.indilib.i4j.client.INDIProperty;
import org.indilib.i4j.client.INDIPropertyListener;
import org.indilib.i4j.client.INDISwitchElement;
import org.indilib.i4j.client.INDISwitchProperty;
import org.indilib.i4j.client.INDITextElement;
import org.indilib.i4j.client.INDITextProperty;
import org.indilib.i4j.client.INDIViewCreatorInterface;

public class INDIAndoidViewCreator implements INDIViewCreatorInterface {

    @Override
    public INDIDeviceListener createDeviceListener(INDIDevice indiDevice) throws INDIException {
        return new INDIDeviceView(indiDevice);
    }

    @Override
    public INDIElementListener createBlobElementView(INDIBLOBElement indiblobElement, PropertyPermissions permission) throws INDIException {
        return new INDIBLOBElementView(indiblobElement, permission);
    }

    @Override
    public INDIElementListener createLightElementView(INDILightElement indiLightElement, PropertyPermissions permission) throws INDIException {
        return new INDILightElementView(indiLightElement);
    }

    @Override
    public INDIElementListener createNumberElementView(INDINumberElement indiNumberElement, PropertyPermissions permission) throws INDIException {
        return new INDINumberElementView(indiNumberElement, permission);
    }

    @Override
    public INDIElementListener createSwitchElementView(INDISwitchElement indiSwitchElement, PropertyPermissions permission) throws INDIException {
        return new INDISwitchElementView(indiSwitchElement, permission);
    }

    @Override
    public INDIElementListener createTextElementView(INDITextElement indiTextElement, PropertyPermissions permission) throws INDIException {
        return new INDITextElementView(indiTextElement, permission);
    }

    public INDIPropertyListener createDefaultPropertyView(INDIProperty indiProperty) throws INDIException {
        return new INDIDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createBlobPropertyView(INDIBLOBProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createNumberPropertyView(INDINumberProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createTextPropertyView(INDITextProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createSwitchPropertyView(INDISwitchProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }

    @Override
    public INDIPropertyListener createLightPropertyView(INDILightProperty indiProperty) throws INDIException {
        return createDefaultPropertyView(indiProperty);
    }
}

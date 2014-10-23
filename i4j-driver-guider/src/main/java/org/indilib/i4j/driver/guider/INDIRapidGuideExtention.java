package org.indilib.i4j.driver.guider;

/*
 * #%L
 * INDI for Java Guider extention - inactive
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.util.Date;

import org.indilib.i4j.Constants.PropertyPermissions;
import org.indilib.i4j.Constants.PropertyStates;
import org.indilib.i4j.Constants.SwitchRules;
import org.indilib.i4j.Constants.SwitchStatus;
import org.indilib.i4j.INDIException;
import org.indilib.i4j.driver.INDIDriver;
import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDINumberElement;
import org.indilib.i4j.driver.INDINumberElementAndValue;
import org.indilib.i4j.driver.INDINumberProperty;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.INDISwitchElementAndValue;
import org.indilib.i4j.driver.INDISwitchProperty;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.driver.annotation.InjectProperty;
import org.indilib.i4j.driver.ccd.INDICCDDriver;
import org.indilib.i4j.driver.event.NumberEvent;
import org.indilib.i4j.driver.event.SwitchEvent;

public class INDIRapidGuideExtention extends INDIDriverExtension<INDIDriver> {

    protected static final String GUIDE_HEAD_TAB = "Guider Head";

    protected static final String GUIDE_CONTROL_TAB = "Guider Control";

    protected static final String RAPIDGUIDE_TAB = "Rapid Guide";

    @InjectProperty(name = "RAPID_GUIDE", label = "Rapid Guide", group = INDIDriver.GROUP_OPTIONS, timeout = 0)
    protected INDISwitchProperty rapidGuide;

    @InjectElement(name = "ENABLE", label = "Enable")
    protected INDISwitchElement rapidGuideEnable;

    @InjectElement(name = "DISABLE", label = "Disable", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideDisable;

    @InjectProperty(name = "RAPID_GUIDE_SETUP", label = "Rapid Guide Setup", group = RAPIDGUIDE_TAB, timeout = 0, switchRule = SwitchRules.ANY_OF_MANY)
    protected INDISwitchProperty rapidGuideSetup;

    @InjectElement(name = "AUTO_LOOP", label = "Auto loop", switchValue = SwitchStatus.ON)
    protected INDISwitchElement rapidGuideSetupAutoLoop;

    @InjectElement(name = "SEND_IMAGE", label = "Send image")
    protected INDISwitchElement rapidGuideSetupShowMarker;

    @InjectElement(name = "SHOW_MARKER", label = "Show marker")
    protected INDISwitchElement rapidGuideSetupSendImage;

    @InjectProperty(name = "RAPID_GUIDE_DATA", label = "Rapid Guide Data", group = RAPIDGUIDE_TAB, permission = PropertyPermissions.RO)
    protected INDINumberProperty rapidGuideData;

    @InjectElement(name = "GUIDESTAR_X", label = "Guide star position X", maximum = 1024, numberFormat = "%%5.2f")
    protected INDINumberElement rapidGuideDataX;

    @InjectElement(name = "GUIDESTAR_Y", label = "Guide star position Y", maximum = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataY;

    @InjectElement(name = "GUIDESTAR_FIT", label = "GUIDESTAR_FIT", maximum = 1024, numberFormat = "%5.2f")
    protected INDINumberElement rapidGuideDataFIT;

    private final INDIGuiderDataCalculator guiderDataCalculator;

    private boolean rapidGuideEnabled = false;

    private boolean autoLoop = false;

    private boolean sendImage = false;

    private boolean showMarker = false;

    public INDIRapidGuideExtention(INDIDriver driver) {
        super(driver);

        rapidGuide.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newRapidGuideValue(elementsAndValues);
            }

        });

        rapidGuideSetup.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newRapidGuideSetupValue(elementsAndValues);
            }
        });

        rapidGuideData.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newRapidGuideDataValue(elementsAndValues);
            }
        });
        guiderDataCalculator = new INDIGuiderDataCalculator(rapidGuideData, rapidGuideDataX, rapidGuideDataY, rapidGuideDataFIT);
    }

    private void newRapidGuideDataValue(INDINumberElementAndValue[] elementsAndValues) {
        rapidGuideData.setState(PropertyStates.OK);
        rapidGuideData.setValues(elementsAndValues);
        updateProperty(rapidGuideData);
    }

    private void newRapidGuideSetupValue(INDISwitchElementAndValue[] elementsAndValues) {
        rapidGuideSetup.setValues(elementsAndValues);
        rapidGuideSetup.setState(PropertyStates.OK);

        autoLoop = rapidGuideSetupAutoLoop.getValue() == SwitchStatus.ON;
        sendImage = rapidGuideSetupSendImage.getValue() == SwitchStatus.ON;
        showMarker = rapidGuideSetupShowMarker.getValue() == SwitchStatus.ON;

        updateProperty(rapidGuideSetup);
    }

    private void newRapidGuideValue(INDISwitchElementAndValue[] elementsAndValues) {
        rapidGuide.setValues(elementsAndValues);
        rapidGuide.setState(PropertyStates.OK);
        rapidGuideEnabled = (rapidGuideEnable.getValue() == SwitchStatus.ON);

        if (rapidGuideEnabled) {
            addProperty(rapidGuideSetup);
            addProperty(rapidGuideData);
        } else {
            removeProperty(rapidGuideSetup);
            removeProperty(rapidGuideData);
        }
        updateProperty(rapidGuide);
    }

    @Override
    public void connect() {
        addProperty(this.rapidGuide);
        if (rapidGuideEnabled) {
            addProperty(this.rapidGuideSetup);
            addProperty(this.rapidGuideData);
        }
    }

    @Override
    public void disconnect() {
        removeProperty(this.rapidGuide);
        if (rapidGuideEnabled) {
            removeProperty(this.rapidGuideSetup);
            removeProperty(this.rapidGuideData);
        }
    }

    /**
     * update the ccd image with the guider info
     * 
     * @param ccdImage
     * @param height
     * @param width
     */
    public boolean exposureComplete(boolean sendImage, INDICCDImage ccdImage, int width, int height) {
        boolean showMarker = false;
        boolean sendData = false;
        if (rapidGuideEnabled) {
            sendImage = this.sendImage;
            showMarker = this.showMarker;
            sendData = true;
        }
        if (sendData) {
            guiderDataCalculator.detectGuideData(width, height, ccdImage, showMarker);
            updateProperty(rapidGuideData);
            return true;
        }
        return false;
    }
}

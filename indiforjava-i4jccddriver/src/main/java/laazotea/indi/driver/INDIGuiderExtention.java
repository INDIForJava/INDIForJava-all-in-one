package laazotea.indi.driver;

import java.util.Date;

import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;
import laazotea.indi.driver.annotation.InjectElement;
import laazotea.indi.driver.annotation.InjectProperty;
import laazotea.indi.driver.event.NumberEvent;

public class INDIGuiderExtention extends INDIDriverExtention<INDIDriver> {

    @InjectProperty(name = "TELESCOPE_TIMED_GUIDE_NS", label = "Guide North/South")
    private INDINumberProperty guideNS;

    @InjectElement(name = "TIMED_GUIDE_N", label = "North (msec)", maximumD = 60000d, stepD = 10d)
    private INDINumberElement guideNorth;

    @InjectElement(name = "TIMED_GUIDE_S", label = "South (msec)", maximumD = 60000d, stepD = 10d)
    private INDINumberElement guideSouth;

    @InjectProperty(name = "TELESCOPE_TIMED_GUIDE_WE", label = "Guide East/West")
    private INDINumberProperty guideWE;

    @InjectElement(name = "TIMED_GUIDE_E", label = "East (msec)", maximumD = 60000d, stepD = 10d)
    private INDINumberElement guideWest;

    @InjectElement(name = "TIMED_GUIDE_W", label = "West (msec)", maximumD = 60000d, stepD = 10d)
    private INDINumberElement guideEast;

    private INDIGuiderInterface guiderInterface;

    
    public void setGuiderInterface(INDIGuiderInterface guiderInterface) {
        this.guiderInterface = guiderInterface;
    }

    /**
     * Initilize guider properties. It is recommended to call this function
     * within initProperties() of your primary device
     * 
     * @param deviceName
     *            Name of the primary device
     * @param groupName
     *            Group or tab name to be used to define guider properties.
     */
    public INDIGuiderExtention(INDIDriver parentDriver) {
        super(parentDriver);
        guideNS.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {

                // We are being asked to send a guide pulse north/south on the
                // st4
                // port
                property.setValues(elementsAndValues);
                boolean rc = false;
                if (guideNorth.getValue() != 0d) {
                    guideSouth.setValue(0d);
                    rc = guiderInterface.guideNorth(guideNorth.getValue());
                } else if (guideSouth.getValue() != 0d) {
                    rc = guiderInterface.guideSouth(guideSouth.getValue());
                }

                guideNS.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
                try {
                    driver.updateProperty(guideNS);
                } catch (INDIException e) {
                }
                return;

            }
        });
        guideWE.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                // We are being asked to send a guide pulse north/south on the
                // st4
                // port
                property.setValues(elementsAndValues);
                boolean rc = false;
                if (guideWest.getValue() != 0) {
                    guideEast.setValue(0);
                    rc = guiderInterface.guideEast(guideWest.getValue());
                } else if (guideEast.getValue() != 0)
                    rc = guiderInterface.guideWest(guideEast.getValue());

                guideWE.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
                try {
                    driver.updateProperty(guideWE);
                } catch (INDIException e) {
                }
                return;
            }
        });
    }

}

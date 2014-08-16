package laazotea.indi.driver;

import static laazotea.indi.Constants.PropertyPermissions.RW;
import static laazotea.indi.Constants.PropertyStates.IDLE;

import java.util.Date;

import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.INDIException;

public class INDIGuider {

    public INDINumberElement getGuideNorth() {
        return guideNorth;
    }

    public INDINumberElement getGuideSouth() {
        return guideSouth;
    }

    public INDINumberProperty getGuideNS() {
        return guideNS;
    }

    public INDINumberElement getGuideWest() {
        return guideWest;
    }

    public INDINumberElement getGuideEast() {
        return guideEast;
    }

    public INDINumberProperty getGuideWE() {
        return guideWE;
    }

    private final INDINumberElement guideNorth;

    private final INDINumberElement guideSouth;

    private final INDINumberProperty guideNS;

    private final INDINumberElement guideWest;

    private final INDINumberElement guideEast;

    private final INDINumberProperty guideWE;

    private final INDIGuiderInterface guiderInterface;

    /**
     * Initilize guider properties. It is recommended to call this function
     * within initProperties() of your primary device
     * 
     * @param deviceName
     *            Name of the primary device
     * @param groupName
     *            Group or tab name to be used to define guider properties.
     */
    public INDIGuider(INDIDriver dirver, INDIGuiderInterface guiderInterface, String groupName) {

        this.guideNS = new INDINumberProperty(dirver, "TELESCOPE_TIMED_GUIDE_NS", "Guide North/South", groupName, IDLE, RW, 60);
        this.guideNorth = new INDINumberElement(guideNS, "TIMED_GUIDE_N", "North (msec)", 0, 0, 60000, 10, "%g");
        this.guideSouth = new INDINumberElement(guideNS, "TIMED_GUIDE_S", "South (msec)", 0, 0, 60000, 10, "%g");

        this.guideWE = new INDINumberProperty(dirver, "TELESCOPE_TIMED_GUIDE_WE", "Guide East/West", groupName, IDLE, RW, 60);
        this.guideEast = new INDINumberElement(guideNS, "TIMED_GUIDE_E", "East (msec)", 0, 0, 60000, 10, "%g");
        this.guideWest = new INDINumberElement(guideNS, "TIMED_GUIDE_W", "West (msec)", 0, 0, 60000, 10, "%g");
        this.guiderInterface = guiderInterface;
    }

    /**
     * Call this function whenever client updates GuideNSNP or GuideWSP
     * properties in the primary device. This function then takes care of
     * issuing the corresponding GuideXXXX function accordingly.
     * 
     * @throws INDIException
     */
    public void processNewNumberValue(INDINumberProperty property, Date timestamp, INDINumberElementAndValue[] elementsAndValues) throws INDIException {
        if (property == guideNS) {
            // We are being asked to send a guide pulse north/south on the st4
            // port
            property.setValues(elementsAndValues);
            boolean rc = false;
            if (guideNorth.getValue() != 0d) {
                guideSouth.setValue(0d);
                ;
                rc = guiderInterface.guideNorth(guideNorth.getValue());
            } else if (guideSouth.getValue() != 0d) {
                rc = guiderInterface.guideSouth(guideSouth.getValue());
            }

            guideNS.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
            guideNS.getDriver().updateProperty(guideNS);
            return;

        } else if (property == guideWE) {
            // We are being asked to send a guide pulse north/south on the st4
            // port
            property.setValues(elementsAndValues);
            boolean rc = false;
            if (guideWest.getValue() != 0) {
                guideEast.setValue(0);
                rc = guiderInterface.guideEast(guideWest.getValue());
            } else if (guideEast.getValue() != 0)
                rc = guiderInterface.guideWest(guideEast.getValue());

            guideWE.setState(rc ? PropertyStates.OK : PropertyStates.ALERT);
            guideNS.getDriver().updateProperty(guideWE);
            return;
        }
    }
}

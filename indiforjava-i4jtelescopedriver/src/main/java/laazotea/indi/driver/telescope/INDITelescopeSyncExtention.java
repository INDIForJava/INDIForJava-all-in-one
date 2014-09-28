package laazotea.indi.driver.telescope;

import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.driver.INDIDriverExtention;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.annotation.INDIe;

public class INDITelescopeSyncExtention extends INDIDriverExtention<INDITelescope> {

    public INDITelescopeSyncExtention(INDITelescope driver) {
        super(driver);
    }

    @Override
    public boolean isActive() {
        return driver.canSync();
    }

    @INDIe(property = "ON_COORD_SET", name = "SYNC", label = "Sync")
    private INDISwitchElement coordSync;

    public boolean doSync(double ra, double dec) {
        if (isActive() && this.coordSync.getValue() == SwitchStatus.ON) {
            driver.sync(ra, dec);
            return true;
        }
        return false;
    }
}

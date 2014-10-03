package laazotea.indi.driver.telescope;

import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.driver.INDIDriverExtention;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.annotation.InjectElement;

public class INDITelescopeSyncExtention extends INDIDriverExtention<INDITelescope> {

    @InjectElement(property = "ON_COORD_SET", name = "SYNC", label = "Sync")
    private INDISwitchElement coordSync;

    public INDITelescopeSyncExtention(INDITelescope driver) {
        super(driver);
    }

    public boolean doSync(double ra, double dec) {
        if (isActive() && this.coordSync.getValue() == SwitchStatus.ON) {
            driver.sync(ra, dec);
            return true;
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return driver.canSync();
    }
}

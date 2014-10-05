package org.indilib.i4j.driver.telescope;

import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.annotation.InjectElement;
import org.indilib.i4j.Constants.SwitchStatus;

public class INDITelescopeSyncExtension extends INDIDriverExtension<INDITelescope> {

    @InjectElement(property = "ON_COORD_SET", name = "SYNC", label = "Sync")
    private INDISwitchElement coordSync;

    INDITelescopeSyncInterface syncInterface;

    public INDITelescopeSyncExtension(INDITelescope driver) {
        super(driver);
        if (!isActive()) {
            return;
        }
        syncInterface = (INDITelescopeSyncInterface) driver;
    }

    public boolean doSync(double ra, double dec) {
        if (isActive() && this.coordSync.getValue() == SwitchStatus.ON) {
            return syncInterface.sync(ra, dec);
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDITelescopeSyncInterface;
    }
}

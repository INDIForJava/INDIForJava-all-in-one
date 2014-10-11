package org.indilib.i4j.driver.telescope;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
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

import org.indilib.i4j.driver.INDIDriverExtension;
import org.indilib.i4j.driver.INDISwitchElement;
import org.indilib.i4j.driver.annotation.InjectElement;

/**
 * The sync extension handles the system that improves the calibration of the
 * telescope with every sync of a position. If a telescope supports this syncing
 * (with is highly recommenced) than this extension is used for it.
 */
public class INDITelescopeSyncExtension extends INDIDriverExtension<INDITelescope> {

    /**
     * an aditional element for the the on coordinates set property. That sync's
     * as soon as the scope reaches the destination.
     */
    @InjectElement(property = "ON_COORD_SET", name = "SYNC", label = "Sync")
    private INDISwitchElement coordSync;

    /**
     * The interface to indicate that the telescope supports syncing.
     */
    private INDITelescopeSyncInterface syncInterface;

    /**
     * Constructor of the extension, you should really know what you are doing
     * if you call this yourself. Better to let it be used by the injector.
     * 
     * @param telecopeDriver
     *            the telescope driver to attact this extention to.
     */
    public INDITelescopeSyncExtension(INDITelescope telecopeDriver) {
        super(telecopeDriver);
        if (!isActive()) {
            return;
        }
        syncInterface = (INDITelescopeSyncInterface) telecopeDriver;
    }

    /**
     * sync the current coordinates.
     * 
     * @param ra
     *            the right ascension of the goto point in space
     * @param dec
     *            the declination of the point in space
     * @return true if successful.
     */
    public boolean doSync(double ra, double dec) {
        if (isActive() && this.coordSync.isOn()) {
            return syncInterface.sync(ra, dec);
        }
        return false;
    }

    @Override
    public boolean isActive() {
        return driver instanceof INDITelescopeSyncInterface;
    }
}

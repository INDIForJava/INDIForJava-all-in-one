package laazotea.indi.driver;

import laazotea.indi.INDIException;
import laazotea.indi.driver.util.INDIPropertyInjector;

public abstract class INDIDriverExtention<Driver extends INDIDriver> {

    protected final Driver driver;

    public INDIDriverExtention(Driver driver) {
        this.driver = driver;
        if (isActive()) {
            INDIPropertyInjector.initialize(this.driver, this);
        }
    }

    public boolean isActive() {
        return true;
    }

    public void connect() {

    }

    public void disconnect() {

    }

    protected void updateProperty(INDIProperty<?> property) throws INDIException {
        driver.updateProperty(property);
    }

    protected void updateProperty(INDIProperty<?> property, String message) throws INDIException {
        driver.updateProperty(property, message);
    }

    protected void updateProperty(INDIProperty<?> property, boolean updateminmax, String message) throws INDIException {
        driver.updateProperty(property, updateminmax, message);
    }

}

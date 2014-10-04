package org.indilib.i4j.driver.telescope.alignment;

public class BuiltInMathPlugin implements IMathPlugin {

    @Override
    public String id() {
        return "INBUILT_MATH_PLUGIN";
    }

    @Override
    public String name() {
        return "Inbuilt Math Plugin";
    }

    @Override
    public void destroy() {
        // TODO Auto-generated method stub

    }

    @Override
    public void create() {
        // TODO Auto-generated method stub

    }

    @Override
    public MountAlignment getApproximateMountAlignment() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean initialise(InMemoryDatabase inMemoryDatabase) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void setApproximateMountAlignment(InMemoryDatabase inMemoryDatabase) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean transformCelestialToTelescope(double d, double e, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination) {
        // TODO Auto-generated method stub
        return false;
    }
}

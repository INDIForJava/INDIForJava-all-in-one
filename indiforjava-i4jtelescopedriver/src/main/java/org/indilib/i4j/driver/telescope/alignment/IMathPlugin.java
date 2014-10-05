package org.indilib.i4j.driver.telescope.alignment;

public interface IMathPlugin {

    void create();

    void destroy();

    MountAlignment getApproximateMountAlignment();

    String id();

    boolean initialise(InMemoryDatabase inMemoryDatabase);

    String name();

    void setApproximateMountAlignment(InMemoryDatabase inMemoryDatabase);

    boolean transformCelestialToTelescope(double d, double e, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector);

    boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination);

}

package laazotea.indi.alignment;


public interface IMathPlugin {

    String id();

    String name();

    void destroy();

    void create();

    MountAlignment getApproximateMountAlignment();

    boolean initialise(InMemoryDatabase inMemoryDatabase);

    void setApproximateMountAlignment(InMemoryDatabase inMemoryDatabase);

    boolean transformCelestialToTelescope(double d, double e, double julianOffset, TelescopeDirectionVector apparentTelescopeDirectionVector);

    boolean transformTelescopeToCelestial(TelescopeDirectionVector telescopeDirectionVector, DoubleRef rightAscension, DoubleRef declination);

}

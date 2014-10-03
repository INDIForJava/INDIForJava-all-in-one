package laazotea.indi.driver.ccd;

public enum CcdFrame {
    LIGHT_FRAME("Light"),
    BIAS_FRAME("Bias"),
    DARK_FRAME("Dark"),
    FLAT_FRAME("Flat Field");

    private final String fitsName;

    private CcdFrame(String fitsName) {
        this.fitsName = fitsName;
    }

    public String fitsValue() {
        return fitsName;
    }

}

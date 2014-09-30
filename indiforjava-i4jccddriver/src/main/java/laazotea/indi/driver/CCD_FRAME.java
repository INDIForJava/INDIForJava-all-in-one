package laazotea.indi.driver;

enum CCD_FRAME {
    LIGHT_FRAME("Light"),
    BIAS_FRAME("Bias"),
    DARK_FRAME("Dark"),
    FLAT_FRAME("Flat Field");

    private CCD_FRAME(String fitsName) {
        this.fitsName = fitsName;
    }

    private final String fitsName;

    public String fitsValue() {
        return fitsName;
    }

}
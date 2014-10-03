package laazotea.indi.driver.ccd;

public class Capability {

    private boolean canAbort = false;

    private boolean canBin = false;

    private boolean canSubFrame = false;

    private boolean hasCooler = false;

    private boolean hasGuideHead = false;

    private boolean hasShutter = false;

    private boolean hasST4Port = false;

    /**
     * @return True if CCD can abort exposure. False otherwise.
     */
    public boolean canAbort() {
        return canAbort;
    }

    public Capability canAbort(boolean canAbort) {
        this.canAbort = canAbort;
        return this;
    }

    /**
     * @return True if CCD supports binning. False otherwise.
     */
    public boolean canBin() {
        return canBin;
    }

    public Capability canBin(boolean canBin) {
        this.canBin = canBin;
        return this;
    }

    /**
     * @return True if CCD supports subframing. False otherwise.
     */
    public boolean canSubFrame() {
        return canSubFrame;
    }

    public Capability canSubFrame(boolean canSubFrame) {
        this.canSubFrame = canSubFrame;
        return this;
    }

    /**
     * @return True if CCD has cooler and temperature can be controlled. False
     *         otherwise.
     */
    public boolean hasCooler() {
        return hasCooler;
    }

    public Capability hasCooler(boolean hasCooler) {
        this.hasCooler = hasCooler;
        return this;
    }

    /**
     * @return True if CCD has guide head. False otherwise.
     */
    public boolean hasGuideHead() {
        return hasGuideHead;
    }

    public Capability hasGuideHead(boolean hasGuideHead) {
        this.hasGuideHead = hasGuideHead;
        return this;
    }

    /**
     * @return True if CCD has mechanical or electronic shutter. False
     *         otherwise.
     */
    public boolean hasShutter() {
        return hasShutter;
    }

    public Capability hasShutter(boolean hasShutter) {
        this.hasShutter = hasShutter;
        return this;
    }

    /**
     * @return True if CCD has ST4 port for guiding. False otherwise.
     */
    public boolean hasST4Port() {
        return hasST4Port;
    }

    public Capability hasST4Port(boolean hasST4Port) {
        this.hasST4Port = hasST4Port;
        return this;
    }

}

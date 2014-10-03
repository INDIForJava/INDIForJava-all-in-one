package laazotea.indi.driver;

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

    /**
     * @return True if CCD supports binning. False otherwise.
     */
    public boolean canBin() {
        return canBin;
    }

    /**
     * @return True if CCD supports subframing. False otherwise.
     */
    public boolean canSubFrame() {
        return canSubFrame;
    }

    /**
     * @return True if CCD has cooler and temperature can be controlled. False
     *         otherwise.
     */
    public boolean hasCooler() {
        return hasCooler;
    }

    /**
     * @return True if CCD has guide head. False otherwise.
     */
    public boolean hasGuideHead() {
        return hasGuideHead;
    }

    /**
     * @return True if CCD has mechanical or electronic shutter. False
     *         otherwise.
     */
    public boolean hasShutter() {
        return hasShutter;
    }

    /**
     * @return True if CCD has ST4 port for guiding. False otherwise.
     */
    public boolean hasST4Port() {
        return hasST4Port;
    }

    public Capability canAbort(boolean canAbort) {
        this.canAbort = canAbort;
        return this;
    }

    public Capability canBin(boolean canBin) {
        this.canBin = canBin;
        return this;
    }

    public Capability canSubFrame(boolean canSubFrame) {
        this.canSubFrame = canSubFrame;
        return this;
    }

    public Capability hasCooler(boolean hasCooler) {
        this.hasCooler = hasCooler;
        return this;
    }

    public Capability hasGuideHead(boolean hasGuideHead) {
        this.hasGuideHead = hasGuideHead;
        return this;
    }

    public Capability hasShutter(boolean hasShutter) {
        this.hasShutter = hasShutter;
        return this;
    }

    public Capability hasST4Port(boolean hasST4Port) {
        this.hasST4Port = hasST4Port;
        return this;
    }

}

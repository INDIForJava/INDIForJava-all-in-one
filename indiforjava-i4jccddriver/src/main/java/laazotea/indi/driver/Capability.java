package laazotea.indi.driver;

class Capability {

    public boolean isHasGuideHead() {
        return hasGuideHead;
    }

    public void setHasGuideHead(boolean hasGuideHead) {
        this.hasGuideHead = hasGuideHead;
    }

    public boolean isHasST4Port() {
        return hasST4Port;
    }

    public void setHasST4Port(boolean hasST4Port) {
        this.hasST4Port = hasST4Port;
    }

    public boolean isHasShutter() {
        return hasShutter;
    }

    public void setHasShutter(boolean hasShutter) {
        this.hasShutter = hasShutter;
    }

    public boolean isHasCooler() {
        return hasCooler;
    }

    public void setHasCooler(boolean hasCooler) {
        this.hasCooler = hasCooler;
    }

    public boolean isCanBin() {
        return canBin;
    }

    public void setCanBin(boolean canBin) {
        this.canBin = canBin;
    }

    public boolean isCanSubFrame() {
        return canSubFrame;
    }

    public void setCanSubFrame(boolean canSubFrame) {
        this.canSubFrame = canSubFrame;
    }

    public boolean isCanAbort() {
        return canAbort;
    }

    public void setCanAbort(boolean canAbort) {
        this.canAbort = canAbort;
    }

    boolean hasGuideHead = false;

    boolean hasST4Port = false;

    boolean hasShutter = false;

    boolean hasCooler = false;

    boolean canBin = false;

    boolean canSubFrame = false;

    boolean canAbort = false;
}
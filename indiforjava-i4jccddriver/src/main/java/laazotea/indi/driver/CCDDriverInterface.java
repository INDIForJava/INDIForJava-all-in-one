package laazotea.indi.driver;

public interface CCDDriverInterface {

    boolean abortExposure();

    boolean startExposure(double duration);

    boolean updateCCDBin(int binX, int binY);

    boolean updateCCDFrame(int x, int y, int w, int h);

    boolean updateCCDFrameType(CCD_FRAME frameType);

}

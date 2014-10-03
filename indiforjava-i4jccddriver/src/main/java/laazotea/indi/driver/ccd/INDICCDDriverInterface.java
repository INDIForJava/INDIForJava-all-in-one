package laazotea.indi.driver.ccd;

public interface INDICCDDriverInterface {

    boolean abortExposure();

    boolean startExposure(double duration);

    boolean updateCCDBin(int binX, int binY);

    boolean updateCCDFrame(int x, int y, int w, int h);

    boolean updateCCDFrameType(CcdFrame frameType);

}

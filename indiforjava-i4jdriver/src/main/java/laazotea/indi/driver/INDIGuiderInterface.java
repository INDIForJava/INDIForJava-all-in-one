package laazotea.indi.driver;


public interface INDIGuiderInterface {

    /**
     * Guide northward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return True if successful, false otherwise.
     */
    boolean guideNorth(double ms);

    /**
     * Guide southward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideSouth(double ms);

    /**
     * Guide easward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideEast(double ms);

    /**
     * Guide westward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideWest(double ms);

}

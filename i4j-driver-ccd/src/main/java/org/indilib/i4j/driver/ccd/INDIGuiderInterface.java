package org.indilib.i4j.driver.ccd;

public interface INDIGuiderInterface {

    /**
     * Guide easward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideEast(double ms);

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
     * Guide westward for ms milliseconds
     * 
     * @param ms
     *            Duration in milliseconds.
     * @return 0 if successful, -1 otherwise.
     */
    boolean guideWest(double ms);

}

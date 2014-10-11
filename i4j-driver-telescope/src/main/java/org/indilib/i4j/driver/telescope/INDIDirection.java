package org.indilib.i4j.driver.telescope;

/**
 * Direction pointer coordinates for a telescope.
 * 
 * @author Richard van Nieuwenhoven
 */
public class INDIDirection {

    /**
     * how many seconds in an hour.
     */
    private static final int SECONDS_PER_HOUR = 3600;

    /**
     * how many minutes in an hour.
     */
    private static final int MINUTES_PER_HOUR = 60;

    /**
     * how many seconds in a minute.
     */
    private static final int SECONDS_PER_MINUTES = 60;

    /**
     * the right ascension of the direction pointer coordinates.
     */
    private double ra;

    /**
     * the declination of the direction pointer coordinates.
     */
    private double dec;

    /**
     * create a direction pointer coordinates for a telescope.
     * 
     * @param ra
     *            the right ascension of the point in space
     * @param dec
     *            the declination of the point in space
     */
    public INDIDirection(double ra, double dec) {
        this.ra = ra;
        this.dec = dec;
    }

    /**
     * create a direction pointer coordinates for a telescope.
     */
    public INDIDirection() {
    }

    /**
     * @return the right ascension of the direction pointer coordinates.
     */
    public double getRa() {
        return ra;
    }

    /**
     * @return the right ascension of the direction pointer coordinates as a
     *         string.
     */
    public String getRaString() {
        return formatStringSexa(ra);
    }

    /**
     * set the right ascension of the direction pointer coordinates.
     * 
     * @param ra
     *            the right ascension of the point in space
     */
    public void setRa(double ra) {
        this.ra = ra;
    }

    /**
     * @return the declination of the direction pointer coordinates.
     */
    public double getDec() {
        return dec;
    }

    /**
     * @return the declination of the direction pointer coordinates as a string.
     */
    public String getDecString() {
        return formatStringSexa(dec);
    }

    /**
     * set the declination of the direction pointer coordinates.
     * 
     * @param dec
     *            the declination of the point in space
     */
    public void setDec(double dec) {
        this.dec = dec;
    }

    /**
     * set the direction pointer coordinates.
     * 
     * @param newRa
     *            the right ascension of the point in space
     * @param newDec
     *            the declination of the point in space
     */
    public void set(double newRa, double newDec) {
        this.ra = newRa;
        this.dec = newDec;
    }

    /**
     * add the value to the right ascension of the direction pointer
     * coordinates.
     * 
     * @param addRa
     *            the addition to the right ascension of the point in space
     */
    public void addRa(double addRa) {
        this.ra += addRa;
    }

    /**
     * add the value to the declination of the direction pointer coordinates.
     * 
     * @param addDec
     *            the addition to the declination of the point in space
     */
    public void addDec(double addDec) {
        this.dec += addDec;
    }

    /**
     * This must be replaced by {@link org.indilib.i4j.INDISexagesimalFormatter}
     * .
     * 
     * @param value
     *            value to format
     * @return the formatted string
     */
    private String formatStringSexa(double value) {
        String out = "";
        long n;
        int d;
        int f;
        int m;
        int s;

        /* save whether it's negative but do all the rest with a positive */
        boolean isneg = value < 0;
        if (isneg) {
            value = -value;
        }

        /* convert to an integral number of whole portions */
        n = Math.round(value * SECONDS_PER_HOUR);
        d = (int) (n / SECONDS_PER_HOUR);
        f = (int) (n % SECONDS_PER_HOUR);

        /* form the whole part; "negative 0" is a special case */
        if (isneg && d == 0) {
            out += String.format("%s%s-0", 0, "");
        } else if (isneg) {
            out += String.format("%d%d", 2, -d);
        } else {
            out += String.format("%d%d", 2, d);
        }
        /* dd:mm:ss */
        m = f / (SECONDS_PER_HOUR / MINUTES_PER_HOUR);
        s = f % (SECONDS_PER_HOUR / SECONDS_PER_MINUTES);
        out += String.format(":%02d:%02d", m, s);
        return out;
    }
}

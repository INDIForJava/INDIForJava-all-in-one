package laazotea.indi.driver.telescope;

import static laazotea.indi.Constants.PropertyStates.IDLE;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import laazotea.indi.Constants;
import laazotea.indi.Constants.PropertyPermissions;
import laazotea.indi.Constants.PropertyStates;
import laazotea.indi.Constants.SwitchStatus;
import laazotea.indi.INDIException;
import laazotea.indi.driver.INDIBLOBElementAndValue;
import laazotea.indi.driver.INDIBLOBProperty;
import laazotea.indi.driver.INDINumberElement;
import laazotea.indi.driver.INDINumberElementAndValue;
import laazotea.indi.driver.INDINumberProperty;
import laazotea.indi.driver.INDISwitchElement;
import laazotea.indi.driver.INDISwitchElementAndValue;
import laazotea.indi.driver.INDISwitchProperty;
import laazotea.indi.driver.INDITextElementAndValue;
import laazotea.indi.driver.INDITextProperty;
import laazotea.indi.driver.annotation.InjectElement;
import laazotea.indi.driver.annotation.InjectExtention;
import laazotea.indi.driver.annotation.InjectProperty;
import laazotea.indi.driver.ccd.INDIGuiderExtention;
import laazotea.indi.driver.ccd.INDIGuiderInterface;
import laazotea.indi.driver.event.NumberEvent;
import laazotea.indi.driver.event.SwitchEvent;

public class TelescopeSimulator extends INDITelescope implements INDIGuiderInterface {

    private static Logger LOG = Logger.getLogger(TelescopeSimulator.class.getName());

    /**
     * slew rate, degrees/s
     */
    static final double GOTO_RATE = 2d;

    /** slew rate, degrees/s */
    static final double SLEW_RATE = 0.5d;

    /**
     * slew rate, degrees/s
     */
    static final double FINE_SLEW_RATE = 0.1d;

    /**
     * sidereal rate, degrees/s
     */
    static final double SID_RATE = 0.004178d;

    /**
     * Move at GOTO_RATE until distance from target is GOTO_LIMIT degrees
     */
    static final double GOTO_LIMIT = 5d;

    /**
     * Move at SLEW_LIMIT until distance from target is SLEW_LIMIT degrees
     */
    static final double SLEW_LIMIT = 2d;

    /**
     * Move at FINE_SLEW_RATE until distance from target is FINE_SLEW_LIMIT
     * degrees
     */
    static final double FINE_SLEW_LIMIT = 0.5d;

    /**
     * poll period, ms
     */
    static final long POLLMS = 250;

    static final long RA_AXIS = 0;

    static final long DEC_AXIS = 1;

    static final int GUIDE_NORTH = 0;

    static final int GUIDE_SOUTH = 1;

    static final int GUIDE_WEST = 0;

    static final int GUIDE_EAST = 1;

    private double currentRA;

    private double currentDEC;

    private double targetRA;

    private double targetDEC;

    private boolean Parked;

    /**
     * Simulated periodic error in RA, DEC
     */
    @InjectProperty(name = "EQUATORIAL_PE", label = "Periodic Error", group = Constants.MOTION_TAB, permission = PropertyPermissions.RO)
    private INDINumberProperty eqPen;

    @InjectElement(name = "RA_PE", label = "RA (hh:mm:ss)", valueD = 0.15d, maximumD = 24d, numberFormat = "%010.6m")
    private INDINumberElement eqPenRa;

    @InjectElement(name = "DEC_PE", label = "DEC (dd:mm:ss)", valueD = 0.15d, minimumD = -90d, maximumD = 90d, numberFormat = "%010.6m")
    private INDINumberElement eqPenDec;

    /**
     * Enable client to manually add periodic error northward or southward for
     * simulation purposes
     */
    @InjectProperty(name = "PE_NS", label = "PE N/S", group = Constants.MOTION_TAB)
    private INDISwitchProperty periodicErrorNS;

    @InjectElement(name = "PE_N", label = "North")
    private INDISwitchElement periodicErrorNSNorth;

    @InjectElement(name = "PE_S", label = "South")
    private INDISwitchElement periodicErrorNSSouth;

    /**
     * Enable client to manually add periodic error westward or easthward for
     * simulation purposes
     */
    @InjectProperty(name = "PE_WE", label = "PE W/E", group = Constants.MOTION_TAB)
    private INDISwitchProperty periodicErrorWE;

    @InjectElement(name = "PE_W", label = "West")
    private INDISwitchElement periodicErrorWEWest;

    @InjectElement(name = "PE_E", label = "East")
    private INDISwitchElement periodicErrorWEEast;

    /**
     * How fast do we guide compared to sidereal rate
     */
    @InjectProperty(name = "GUIDE_RATE", label = "Guiding Rate", group = Constants.MOTION_TAB, timeout = 0)
    private INDINumberProperty guideRate;

    @InjectElement(name = "GUIDE_RATE_WE", label = "W/E Rate", valueD = 0.3d, maximumD = 1d, stepD = 0.1d, numberFormat = "%g")
    private INDINumberElement guideRateWE;

    @InjectElement(name = "GUIDE_RATE_NS", label = "N/S Rate", valueD = 0.3d, maximumD = 1d, stepD = 0.1d, numberFormat = "%g")
    private INDINumberElement guideRateNS;

    @InjectExtention(group = Constants.MOTION_TAB)
    private INDIGuiderExtention guider;

    private double[] guiderNSTarget = new double[2];

    private double[] guiderEWTarget = new double[2];

    private long lastSystime = -1;

    private double last_dx = 0, last_dy = 0;

    TelescopeMotionNS moveNSlast_motion = null;

    TelescopeMotionWE moveWElast_motion = null;

    public TelescopeSimulator(InputStream inputStream, OutputStream outputStream) {
        super(inputStream, outputStream);
        currentRA = 0;
        currentDEC = 90;
        Parked = false;

        // Let's simulate it to be an F/10 8" telescope
        scopeParametersAperture.setValue(203d);
        scopeParametersFocalLength.setValue(2000d);
        scopeParametersGuiderAperture.setValue(203d);
        scopeParametersGuiderFocalLength.setValue(2000d);

        trackState = TelescopeStatus.SCOPE_IDLE;
        this.guideRate.setEventHandler(new NumberEvent() {

            @Override
            public void processNewValue(Date date, INDINumberElementAndValue[] elementsAndValues) {
                newGuideRateValue(elementsAndValues);
            }
        });
        periodicErrorNS.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newPErrNSValue(elementsAndValues);
            }
        });
        periodicErrorWE.setEventHandler(new SwitchEvent() {

            @Override
            public void processNewValue(Date date, INDISwitchElementAndValue[] elementsAndValues) {
                newPErrWEValue(elementsAndValues);
            }
        });
        this.guider.setGuiderInterface(this);
    }

    /**
     * This must be replaced by a real java impl
     */
    private String fs_sexa(double a, int w, int fracbase) {
        String out = "";
        long n;
        int d;
        int f;
        int m;
        int s;
        boolean isneg;

        /* save whether it's negative but do all the rest with a positive */
        isneg = (a < 0);
        if (isneg)
            a = -a;

        /* convert to an integral number of whole portions */
        n = (long) (a * fracbase + 0.5);
        d = (int) (n / fracbase);
        f = (int) (n % fracbase);

        /* form the whole part; "negative 0" is a special case */
        if (isneg && d == 0)
            out += String.format("%s%s-0", w - 2, "");
        else
            out += String.format("%d%d", w, isneg ? -d : d);

        /* do the rest */
        switch (fracbase) {
            case 60: /* dd:mm */
                m = f / (fracbase / 60);
                out += String.format(":%02d", m);
                break;
            case 600: /* dd:mm.m */
                out += String.format(":%02d.%1d", f / 10, f % 10);
                break;
            case 3600: /* dd:mm:ss */
                m = f / (fracbase / 60);
                s = f % (fracbase / 60);
                out += String.format(":%02d:%02d", m, s);
                break;
            case 36000: /* dd:mm:ss.s */
                m = f / (fracbase / 60);
                s = f % (fracbase / 60);
                out += String.format(":%02d:%02d.%1d", m, s / 10, s % 10);
                break;
            case 360000: /* dd:mm:ss.ss */
                m = f / (fracbase / 60);
                s = f % (fracbase / 60);
                out += String.format(":%02d:%02d.%02d", m, s / 100, s % 100);
                break;
            default:
                throw new IllegalArgumentException("unkown franctionbase");
        }

        return out;
    }

    private void newGuideRateValue(INDINumberElementAndValue[] elementsAndValues) {
        this.guideRate.setValues(elementsAndValues);
        guideRate.setState(PropertyStates.OK);
        try {
            updateProperty(this.guideRate);
        } catch (INDIException e) {
        }
    };

    private void newPErrNSValue(INDISwitchElementAndValue[] elementsAndValues) {
        periodicErrorNS.setValues(elementsAndValues);
        periodicErrorNS.setState(PropertyStates.OK);

        if (periodicErrorNSNorth.getValue() == SwitchStatus.ON) {
            eqPenDec.setValue(eqPenDec.getValue() + (SID_RATE * guideRateNS.getValue()));
            LOG.log(Level.INFO, String.format("Simulating PE in NORTH direction for value of %g", SID_RATE));
        } else {
            eqPenDec.setValue(eqPenDec.getValue() - (SID_RATE * guideRateNS.getValue()));
            LOG.log(Level.INFO, String.format("Simulating PE in SOUTH direction for value of %g", SID_RATE));
        }
        periodicErrorNS.resetAllSwitches();
        try {
            updateProperty(periodicErrorNS);
            updateProperty(this.eqPen);
        } catch (INDIException e) {
        }
    }

    private void newPErrWEValue(INDISwitchElementAndValue[] elementsAndValues) {
        periodicErrorWE.setValues(elementsAndValues);
        periodicErrorWE.setState(PropertyStates.OK);

        if (periodicErrorWEWest.getValue() == SwitchStatus.ON) {
            eqPenRa.setValue(eqPenRa.getValue() - (SID_RATE / 15d * guideRateWE.getValue()));
            LOG.log(Level.INFO, String.format("Simulator PE in WEST direction for value of %g", SID_RATE));
        } else {
            eqPenRa.setValue(eqPenRa.getValue() + (SID_RATE / 15d * guideRateWE.getValue()));
            LOG.log(Level.INFO, String.format("Simulator PE in EAST direction for value of %g", SID_RATE));
        }

        periodicErrorWE.resetAllSwitches();
        try {
            updateProperty(periodicErrorWE);
            updateProperty(this.eqPen);
        } catch (INDIException e) {
        }
    }

    @Override
    protected boolean abort() {
        if (movementNSS.getState() == PropertyStates.BUSY) {
            movementNSS.resetAllSwitches();
            movementNSS.setState(IDLE);
            try {
                updateProperty(movementNSS);
            } catch (INDIException e) {
            }
        }
        if (movementWES.getState() == PropertyStates.BUSY) {
            movementWES.resetAllSwitches();
            movementWES.setState(IDLE);
            try {
                updateProperty(movementWES);
            } catch (INDIException e) {
            }
        }
        if (parkExtention.isBusy()) {
            parkExtention.setIdle();
        }
        if (eqn.getState() == PropertyStates.BUSY) {
            eqn.setState(IDLE);
            try {
                updateProperty(eqn);
            } catch (INDIException e) {
            }
        }
        trackState = TelescopeStatus.SCOPE_IDLE;
        abort.setState(PropertyStates.OK);
        abort.resetAllSwitches();
        try {
            updateProperty(abort);
        } catch (INDIException e) {
        }
        LOG.log(Level.INFO, "Telescope aborted.");

        return true;
    }

    @Override
    protected boolean canPark() {
        return true;
    }

    @Override
    protected boolean canSync() {
        return true;
    }

    @Override
    protected void doGoto(double r, double d) {
        // IDLog("ScopeSim Goto\n");
        targetRA = r;
        targetDEC = d;
        String RAStr, DecStr;

        RAStr = fs_sexa(targetRA, 2, 3600);
        DecStr = fs_sexa(targetDEC, 2, 3600);

        Parked = false;
        trackState = TelescopeStatus.SCOPE_SLEWING;

        eqn.setState(PropertyStates.BUSY);

        LOG.log(Level.INFO, String.format("Slewing to RA: %s - DEC: %s", RAStr, DecStr));
    }

    @Override
    protected boolean moveNS(TelescopeMotionNS dir) {

        switch (dir) {
            case MOTION_NORTH:
                if (moveNSlast_motion != TelescopeMotionNS.MOTION_NORTH)
                    moveNSlast_motion = TelescopeMotionNS.MOTION_NORTH;
                else {
                    movementNSS.resetAllSwitches();
                    movementNSS.setState(IDLE);
                    try {
                        updateProperty(movementNSS);
                    } catch (INDIException e) {
                    }
                }
                break;

            case MOTION_SOUTH:
                if (moveNSlast_motion != TelescopeMotionNS.MOTION_SOUTH)
                    moveNSlast_motion = TelescopeMotionNS.MOTION_SOUTH;
                else {
                    movementNSS.resetAllSwitches();
                    movementNSS.setState(IDLE);
                    try {
                        updateProperty(movementNSS);
                    } catch (INDIException e) {
                    }
                    ;
                }
                break;
        }

        return true;
    }

    @Override
    protected boolean moveWE(TelescopeMotionWE dir) {

        switch (dir) {
            case MOTION_WEST:
                if (moveWElast_motion != TelescopeMotionWE.MOTION_WEST)
                    moveWElast_motion = TelescopeMotionWE.MOTION_WEST;
                else {
                    movementWES.resetAllSwitches();
                    movementWES.setState(IDLE);
                    try {
                        updateProperty(movementWES);
                    } catch (INDIException e) {
                    }
                    ;
                }
                break;

            case MOTION_EAST:
                if (moveWElast_motion != TelescopeMotionWE.MOTION_EAST)
                    moveWElast_motion = TelescopeMotionWE.MOTION_EAST;
                else {
                    movementWES.resetAllSwitches();
                    movementWES.setState(IDLE);
                    try {
                        updateProperty(movementWES);
                    } catch (INDIException e) {
                    }
                    ;
                }
                break;
        }

        return true;
    }

    @Override
    protected void park() {
        targetRA = 0;
        targetDEC = 90;
        Parked = true;
        trackState = TelescopeStatus.SCOPE_PARKING;
        LOG.log(Level.INFO, "Parking telescope in progress...");
    }

    @Override
    protected void readScopeStatus() {
        double dt = 0d, da_ra = 0d, da_dec = 0d, dx = 0d, dy = 0d, ra_guide_dt = 0d, dec_guide_dt = 0d;
        int nlocked, ns_guide_dir = -1, we_guide_dir = -1;
        String RA_DISP, DEC_DISP, RA_GUIDE, DEC_GUIDE, RA_PE, DEC_PE, RA_TARGET, DEC_TARGET;

        /* update elapsed time since last poll, don't presume exactly POLLMS */
        long now = System.currentTimeMillis();
        if (lastSystime == -1) {
            lastSystime = now;
        }
        dt = ((double) (now - lastSystime)) / 1000d;
        lastSystime = now;

        if ((Math.abs(targetRA - currentRA) * 15d) >= GOTO_LIMIT) {
            da_ra = GOTO_RATE * dt;
        } else if ((Math.abs(targetRA - currentRA) * 15d) >= SLEW_LIMIT) {
            da_ra = SLEW_RATE * dt;
        } else {
            da_ra = FINE_SLEW_RATE * dt;
        }
        if (Math.abs(targetDEC - currentDEC) >= GOTO_LIMIT) {
            da_dec = GOTO_RATE * dt;
        } else if (Math.abs(targetDEC - currentDEC) >= SLEW_LIMIT) {
            da_dec = SLEW_RATE * dt;
        } else {
            da_dec = FINE_SLEW_RATE * dt;
        }
        if (this.movementNSS.getState() == PropertyStates.BUSY) {
            if (this.movementNSSNorth.getValue() == SwitchStatus.ON)
                currentDEC += da_dec;
            else if (this.movementNSSSouth.getValue() == SwitchStatus.ON)
                currentDEC -= da_dec;

            newRaDec(currentRA, currentDEC);
            return;
        }

        if (this.movementWES.getState() == PropertyStates.BUSY) {
            if (this.movementWESWest.getValue() == SwitchStatus.ON)
                currentRA += da_ra / 15d;
            else if (this.movementWESEast.getValue() == SwitchStatus.ON)
                currentRA -= da_ra / 15d;

            newRaDec(currentRA, currentDEC);
            return;

        }

        /*
         * Process per current state. We check the state of
         * EQUATORIAL_EOD_COORDS_REQUEST and act acoordingly
         */
        switch (trackState) {
        /*
         * case SCOPE_IDLE: EqNP.s = IPS_IDLE; break;
         */
            case SCOPE_SLEWING:
            case SCOPE_PARKING:
                /* slewing - nail it when both within one pulse @ SLEWRATE */
                nlocked = 0;

                dx = targetRA - currentRA;

                if ((Math.abs(dx) * 15d) <= da_ra) {
                    currentRA = targetRA;
                    nlocked++;
                } else if (dx > 0)
                    currentRA += da_ra / 15.;
                else
                    currentRA -= da_ra / 15.;

                dy = targetDEC - currentDEC;
                if (Math.abs(dy) <= da_dec) {
                    currentDEC = targetDEC;
                    nlocked++;
                } else if (dy > 0)
                    currentDEC += da_dec;
                else
                    currentDEC -= da_dec;

                eqn.setState(PropertyStates.BUSY);

                if (nlocked == 2) {
                    if (trackState == TelescopeStatus.SCOPE_SLEWING) {

                        // Initially no PE in both axis.
                        eqnRa.setValue(currentRA);
                        eqnDec.setValue(currentDEC);

                        try {
                            updateProperty(this.eqn);
                        } catch (INDIException e) {
                        }

                        trackState = TelescopeStatus.SCOPE_TRACKING;

                        eqn.setState(PropertyStates.OK);
                        LOG.log(Level.INFO, "Telescope slew is complete. Tracking...");
                    } else {
                        trackState = TelescopeStatus.SCOPE_PARKED;
                        eqn.setState(PropertyStates.IDLE);
                        LOG.log(Level.INFO, "Telescope parked successfully.");
                    }
                }

                break;

            case SCOPE_IDLE:
            case SCOPE_TRACKING:
                /* tracking */

                dt *= 1000;

                if (guiderNSTarget[GUIDE_NORTH] > 0) {
                    LOG.log(Level.INFO, "Commanded to GUIDE NORTH for " + guiderNSTarget[GUIDE_NORTH] + " ms");
                    ns_guide_dir = GUIDE_NORTH;
                } else if (guiderNSTarget[GUIDE_SOUTH] > 0) {
                    LOG.log(Level.INFO, "Commanded to GUIDE SOUTH for " + guiderNSTarget[GUIDE_SOUTH] + " ms");
                    ns_guide_dir = GUIDE_SOUTH;
                }

                // WE Guide Selection
                if (guiderEWTarget[GUIDE_WEST] > 0) {
                    we_guide_dir = GUIDE_WEST;
                    LOG.log(Level.INFO, "Commanded to GUIDE WEST for " + guiderEWTarget[GUIDE_WEST] + " ms");
                } else if (guiderEWTarget[GUIDE_EAST] > 0) {
                    we_guide_dir = GUIDE_EAST;
                    LOG.log(Level.INFO, "Commanded to GUIDE EAST for " + guiderEWTarget[GUIDE_EAST] + " ms");
                }

                if (ns_guide_dir != -1) {

                    dec_guide_dt = SID_RATE * guideRateNS.getValue() * guiderNSTarget[ns_guide_dir] / 1000.0 * (ns_guide_dir == GUIDE_NORTH ? 1 : -1);

                    // If time remaining is more that dt, then decrement and
                    if (guiderNSTarget[ns_guide_dir] >= dt)
                        guiderNSTarget[ns_guide_dir] -= dt;
                    else
                        guiderNSTarget[ns_guide_dir] = 0;

                    eqPenDec.setValue(eqPenDec.getValue() + dec_guide_dt);

                }

                if (we_guide_dir != -1) {

                    ra_guide_dt = SID_RATE / 15.0 * guideRateWE.getValue() * guiderEWTarget[we_guide_dir] / 1000.0 * (we_guide_dir == GUIDE_WEST ? -1 : 1);

                    if (guiderEWTarget[we_guide_dir] >= dt)
                        guiderEWTarget[we_guide_dir] -= dt;
                    else
                        guiderEWTarget[we_guide_dir] = 0;

                    eqPenRa.setValue(eqPenRa.getValue() + ra_guide_dt);

                }

                // Mention the followng:
                // Current RA displacemet and direction
                // Current DEC displacement and direction
                // Amount of RA GUIDING correction and direction
                // Amount of DEC GUIDING correction and direction

                dx = eqPenRa.getValue() - targetRA;
                dy = eqPenDec.getValue() - targetDEC;
                RA_DISP = fs_sexa(Math.abs(dx), 2, 3600);
                DEC_DISP = fs_sexa(Math.abs(dy), 2, 3600);

                RA_GUIDE = fs_sexa(Math.abs(ra_guide_dt), 2, 3600);
                DEC_GUIDE = fs_sexa(Math.abs(dec_guide_dt), 2, 3600);

                RA_PE = fs_sexa(eqPenRa.getValue(), 2, 3600);
                DEC_PE = fs_sexa(eqPenDec.getValue(), 2, 3600);

                RA_TARGET = fs_sexa(targetRA, 2, 3600);
                DEC_TARGET = fs_sexa(targetDEC, 2, 3600);

                if ((dx != last_dx || dy != last_dy || ra_guide_dt != 0 || dec_guide_dt != 0)) {
                    last_dx = dx;
                    last_dy = dy;
                    LOG.log(Level.INFO, String.format("dt is %g\n", dt));
                    LOG.log(Level.INFO, String.format("RA Displacement (%c%s) %s -- %s of target RA %s\n", dx >= 0 ? '+' : '-', RA_DISP, RA_PE,
                            (eqPenRa.getValue() - targetRA) > 0 ? "East" : "West", RA_TARGET));
                    LOG.log(Level.INFO, String.format("DEC Displacement (%c%s) %s -- %s of target RA %s\n", dy >= 0 ? '+' : '-', DEC_DISP, DEC_PE,
                            (eqPenDec.getValue() - targetDEC) > 0 ? "North" : "South", DEC_TARGET));
                    LOG.log(Level.INFO, String.format("RA Guide Correction (%g) %s -- Direction %s\n", ra_guide_dt, RA_GUIDE, ra_guide_dt > 0 ? "East" : "West"));
                    LOG.log(Level.INFO, String.format("DEC Guide Correction (%g) %s -- Direction %s\n", dec_guide_dt, DEC_GUIDE, dec_guide_dt > 0 ? "North" : "South"));
                }

                if (ns_guide_dir != -1 || we_guide_dir != -1) {
                    try {
                        updateProperty(this.eqPen);
                    } catch (INDIException e) {
                    }
                }
                break;

            default:
                break;
        }

        String RAStr, DecStr;

        RAStr = fs_sexa(currentRA, 2, 3600);
        DecStr = fs_sexa(currentDEC, 2, 3600);

        LOG.log(Level.INFO, String.format("Current RA: %s Current DEC: %s", RAStr, DecStr));

        newRaDec(currentRA, currentDEC);
    }

    @Override
    protected boolean sync(double ra, double dec) {
        currentRA = ra;
        currentDEC = dec;

        eqPenRa.setValue(ra);
        eqPenDec.setValue(dec);
        try {
            updateProperty(this.eqPen);
        } catch (INDIException e) {
        }

        LOG.log(Level.INFO, "Sync is successful.");

        trackState = TelescopeStatus.SCOPE_IDLE;
        eqn.setState(PropertyStates.OK);

        newRaDec(currentRA, currentDEC);

        return true;
    }

    @Override
    protected long updateInterfall() {
        return 250L;
    }

    public void driverConnect(Date timestamp) throws INDIException {
        super.driverConnect(timestamp);
        LOG.log(Level.INFO, "Telescope simulator is online.");
        guider.connect();
        addProperty(guideRate);
        addProperty(eqPen);
        addProperty(periodicErrorNS);
        addProperty(periodicErrorWE);
    }

    public void driverDisconnect(Date timestamp) throws INDIException {
        super.driverDisconnect(timestamp);
        LOG.log(Level.INFO, "Telescope simulator is offline.");
        guider.disconnect();
        removeProperty(guideRate);
        removeProperty(eqPen);
        removeProperty(periodicErrorNS);
        removeProperty(periodicErrorWE);
    }

    public String getDefaultName() {
        return "Telescope Simulator";
    }

    @Override
    public String getName() {
        return getDefaultName();
    }

    @Override
    public boolean guideEast(double ms) {
        guiderEWTarget[GUIDE_EAST] = ms;
        guiderEWTarget[GUIDE_WEST] = 0;
        return true;
    }

    @Override
    public boolean guideNorth(double ms) {
        guiderNSTarget[GUIDE_NORTH] = ms;
        guiderNSTarget[GUIDE_SOUTH] = 0;
        return true;
    }

    @Override
    public boolean guideSouth(double ms) {
        guiderNSTarget[GUIDE_SOUTH] = ms;
        guiderNSTarget[GUIDE_NORTH] = 0;
        return true;
    }

    @Override
    public boolean guideWest(double ms) {
        guiderEWTarget[GUIDE_WEST] = ms;
        guiderEWTarget[GUIDE_EAST] = 0;
        return true;

    }

}

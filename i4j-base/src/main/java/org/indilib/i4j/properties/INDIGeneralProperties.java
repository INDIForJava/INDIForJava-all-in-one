package org.indilib.i4j.properties;

/**
 * The following tables describe standard properties pertaining to generic
 * devices and class-specific devices like telescope, CCDs...etc. The name of a
 * standard property and its members must be strictly reserved in all drivers.
 * However, it is permissible to change the label element of properties. You can
 * find numerous uses of the standard properties in the INDI library driver
 * repository.
 * 
 * @see http://indilib.org/develop/developer-manual/101-standard-properties.html
 * @author Richard van Nieuwenhoven
 */
public final class INDIGeneralProperties {

    /**
     * the switch property to connect the driver to the device.
     */
    public static final String CONNECTION = "CONNECTION";

    /**
     * switch element to establish connection to device.
     */
    public static final String CONNECT = "CONNECT";

    /**
     * switch element to establish Disconnect the device.
     */
    public static final String DISCONNECT = "DISCONNECT";

    /**
     * the device text property of the connection port of to the device.
     */
    public static final String DEVICE_PORT = "DEVICE_PORT";

    /**
     * text element of the connection port of to the device.
     */
    public static final String PORT = "PORT";

    /**
     * number property of the Local sidereal time HH:MM:SS.
     */
    public static final String TIME_LST = "TIME_LST";

    /**
     * number element of the Local sidereal time HH:MM:SS.
     */
    public static final String LST = "LST";

    /**
     * text property of the UTC Time & Offset.
     */
    public static final String TIME_UTC = "TIME_UTC";

    /**
     * text element of the UTC time in ISO 8601 format.
     */
    public static final String UTC = "UTC";

    /**
     * text element of the UTC offset, in hours +E.
     */
    public static final String OFFSET = "OFFSET";

    /**
     * number property of the Earth geodetic coordinate.
     */
    public static final String GEOGRAPHIC_COORD = "GEOGRAPHIC_COORD";

    /**
     * number element of the Site latitude (-90 to +90), degrees +N.
     */
    public static final String LAT = "LAT";

    /**
     * number element of the Site longitude (0 to 360), degrees +E.
     */
    public static final String LONG = "LONG";

    /**
     * number element of the Site elevation, meters.
     */
    public static final String ELEV = "ELEV";

    /**
     * number property of the Weather conditions.
     */
    public static final String ATMOSPHERE = "ATMOSPHERE";

    /**
     * number element of the temperature in Kelvin.
     */
    public static final String TEMPERATURE = "TEMPERATURE";

    /**
     * number element of the pressure in hPa.
     */
    public static final String PRESSURE = "PRESSURE";

    /**
     * number element of the humidity Percentage %.
     */
    public static final String HUMIDITY = "HUMIDITY";

    /**
     * switch property of the upload mode of blobs.
     */
    public static final String UPLOAD_MODE = "UPLOAD_MODE";

    /**
     * switch element of the Send BLOB to client.
     */
    public static final String UPLOAD_CLIENT = "UPLOAD_CLIENT";

    /**
     * switch element of the Save BLOB locally.
     */
    public static final String UPLOAD_LOCAL = "UPLOAD_LOCAL";

    /**
     * switch element of the Send blob to client and save it locally as well.
     */
    public static final String UPLOAD_BOTH = "UPLOAD_BOTH";

    /**
     * text property of the upload settings of blobs.
     */
    public static final String UPLOAD_SETTINGS = "UPLOAD_SETTINGS";

    /**
     * text element of the Upload directory if the BLOB is saved locally.
     */
    public static final String UPLOAD_DIR = "UPLOAD_DIR";

    /**
     * text element of the Prefix used when saving filename.
     */
    public static final String UPLOAD_PREFIX = "UPLOAD_PREFIX";

    /**
     * text property of the Name of active devices. If defined, at least one
     * member below must be defined in the vector.ACTIVE_DEVICES is used to aid
     * clients in automatically providing the users with a list of active
     * devices (i.e. CONNECTION is ON) whenever needed. For example, a CCD
     * driver may define ACTIVE_DEVICES property with one member:
     * ACTIVE_TELESCOPE. Suppose that the client is also running LX200 Basic
     * driver to control the telescope. If the telescope is connected, the
     * client may automatically fill the ACTIVE_TELESCOPE field or provide a
     * drop-down list of active telescopes to select from. Once set, the CCD
     * driver may record, for example, the telescope name, RA, DEC, among other
     * metadata once it captures an image. Therefore, ACTIVE_DEVICES is
     * primarily used to link together different classes of devices to exchange
     * information if required.
     */
    public static final String ACTIVE_DEVICES = "ACTIVE_DEVICES";

    /**
     * text element of the active telescope.
     */
    public static final String ACTIVE_TELESCOPE = "ACTIVE_TELESCOPE";

    /**
     * text element of the active ccd.
     */
    public static final String ACTIVE_CCD = "ACTIVE_CCD";

    /**
     * text element of the active filter.
     */
    public static final String ACTIVE_FILTER = "ACTIVE_FILTER";

    /**
     * text element of the active focuser.
     */
    public static final String ACTIVE_FOCUSER = "ACTIVE_FOCUSER";

    /**
     * text element of the active dome.
     */
    public static final String ACTIVE_DOME = "ACTIVE_DOME";

    /**
     * utility class do not instanciate it.
     */
    private INDIGeneralProperties() {
    }
}

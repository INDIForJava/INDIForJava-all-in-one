package laazotea.indi.alignment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import laazotea.indi.INDIException;
import net.sourceforge.novaforjava.api.LnLnlatPosn;

public class InMemoryDatabase {

    private static Logger LOG = Logger.getLogger(InMemoryDatabase.class.getName());

    private static String DATABASE_FILE = ".i4j/alignment.db";

    private List<LoadDatabaseCallback> callbacks = new ArrayList<InMemoryDatabase.LoadDatabaseCallback>();

    public static interface LoadDatabaseCallback {

        void loadDatabaseCallback();
    }

    private List<AlignmentDatabaseEntry> mySyncPoints = new ArrayList<>();

    /**
     * Check if a entry already exists in the database
     * 
     * @param CandidateEntry
     *            The candidate entry to check
     * @param Tolerance
     *            The % tolerance used in the checking process (default 0.1%)
     * @return True if an entry already exists within the required tolerance
     */
    public boolean CheckForDuplicateSyncPoint(AlignmentDatabaseEntry CandidateEntry, double Tolerance) {
        return false;
    }

    public boolean CheckForDuplicateSyncPoint(AlignmentDatabaseEntry CandidateEntry) {
        return CheckForDuplicateSyncPoint(CandidateEntry, 0.1);
    }

    /**
     * Get a reference to the in memory database.
     * 
     * @return A reference to the in memory database.
     */
    List<AlignmentDatabaseEntry> GetAlignmentDatabase() {
        return mySyncPoints;
    }

    /**
     * Get the database reference position
     * 
     * @param Position
     *            A pointer to a ln_lnlat_posn object to retunr the current
     *            position in
     * @return True if successful
     */
    boolean GetDatabaseReferencePosition(LnLnlatPosn Position) {
        return false;
    }

    /**
     * Load the database from persistent storage
     * 
     * @param DeviceName
     *            The name of the current device.
     * @return True if successful
     */
    boolean LoadDatabase(String DeviceName) {
        File db = getDataBaseFile(DeviceName);
        db.getParentFile().mkdirs();
        try (ObjectInputStream out = new ObjectInputStream(new FileInputStream(db))) {
            mySyncPoints = (List<AlignmentDatabaseEntry>) out.readObject();
            return true;
        } catch (IOException | ClassNotFoundException e) {
            LOG.log(Level.SEVERE, "could not load sync points to alignment database", e);
            return false;
        } finally {
            for (LoadDatabaseCallback loadDatabaseCallback : callbacks) {
                loadDatabaseCallback.loadDatabaseCallback();
            }
        }
    }

    /**
     * Save the database to persistent storage
     * 
     * @param DeviceName
     *            The name of the current device.
     * @return True if successful
     */
    boolean SaveDatabase(String DeviceName) {
        File db = getDataBaseFile(DeviceName);
        db.getParentFile().mkdirs();
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(db))) {
            out.writeObject(mySyncPoints);
        } catch (IOException e) {
            LOG.log(Level.SEVERE, "could not save sync points to alignment database", e);
            return false;
        }
        return true;
    }

    /**
     * construct a file name for the point database for this device.
     * 
     * @param deviceName
     * @return the file to use.
     */
    private File getDataBaseFile(String deviceName) {
        File base = new File(System.getProperty("user.home"), DATABASE_FILE);
        StringBuffer deviceNameFile = new StringBuffer(deviceName);
        deviceNameFile.append('-');
        deviceNameFile.append(base.getName());
        for (int index = 0; index < deviceNameFile.length(); index++) {
            char character = deviceNameFile.charAt(index);
            if (!Character.isLetterOrDigit(character) && character != '.') {
                deviceNameFile.setCharAt(index, '-');
            }
        }
        File current = new File(base.getParentFile(), deviceNameFile.toString());
        return current;
    }

    /**
     * Set the database reference position
     * 
     * @param Latitude
     * @param Longitude
     */
    void SetDatabaseReferencePosition(double Latitude, double Longitude) {
    }

    /**
     * Set the function to be called when the database is loaded or reloaded
     * 
     * @param CallbackPointer
     *            A pointer to the class function to call
     * @param ThisPointer
     *            A pointer to the class object of the callback function
     */
    void SetLoadDatabaseCallback(LoadDatabaseCallback callback) {
        callbacks.add(callback);
    }

}

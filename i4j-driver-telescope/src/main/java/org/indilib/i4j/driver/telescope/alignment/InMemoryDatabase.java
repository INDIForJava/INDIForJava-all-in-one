package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
 * %%
 * Copyright (C) 2013 - 2014 indiforjava
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.novaforjava.api.LnLnlatPosn;

import org.indilib.i4j.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InMemoryDatabase {

    public static interface LoadDatabaseCallback {

        void loadDatabaseCallback();
    }

    private static Logger LOG = LoggerFactory.getLogger(InMemoryDatabase.class);

    private static String DATABASE_FILE = "alignment.db";

    private List<LoadDatabaseCallback> callbacks = new ArrayList<InMemoryDatabase.LoadDatabaseCallback>();

    private List<AlignmentDatabaseEntry> mySyncPoints = new ArrayList<>();

    /**
     * construct a file name for the point database for this device.
     * 
     * @param deviceName
     * @return the file to use.
     */
    private File getDataBaseFile(String deviceName) {
        File base = new File(FileUtils.getI4JBaseDirectory(), DATABASE_FILE);
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
     *            A pointer to a ln_lnlat_posn object to return the current
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
            LOG.error("could not load sync points to alignment database", e);
            return false;
        } finally {
            for (LoadDatabaseCallback loadDatabaseCallback : callbacks) {
                try {
                    loadDatabaseCallback.loadDatabaseCallback();
                } catch (Exception e) {
                    LOG.error("Callback has thrown a exception", e);
                }
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
            LOG.error("could not save sync points to alignment database", e);
            return false;
        }
        return true;
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

    public boolean CheckForDuplicateSyncPoint(AlignmentDatabaseEntry CandidateEntry) {
        return CheckForDuplicateSyncPoint(CandidateEntry, 0.1);
    }

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

}

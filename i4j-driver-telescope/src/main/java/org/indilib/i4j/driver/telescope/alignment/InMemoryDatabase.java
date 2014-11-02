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

/**
 * This class provides the driver side API to the in memory alignment database.
 * 
 * @author Richard van Nieuwenhoven
 */
public class InMemoryDatabase {

    /**
     * the default maximum tolerance between sync points.
     */
    private static final double MAXIMUM_TOLERANCE_BETWEEN_SYNC_POINTS = 0.1;

    /**
     * callback when the database is loaded.
     */
    public interface LoadDatabaseCallback {

        /**
         * the database was loaded.
         */
        void loadDatabaseCallback();
    }

    /**
     * the logger to log to.
     */
    private static final Logger LOG = LoggerFactory.getLogger(InMemoryDatabase.class);

    /**
     * default name of the database file.
     */
    private static final String DATABASE_FILE = "alignment.db";

    /**
     * list of callbacks, to call when the database was loaded.
     */
    private List<LoadDatabaseCallback> callbacks = new ArrayList<InMemoryDatabase.LoadDatabaseCallback>();

    /**
     * the complete list of sync points. (this is the database contens).
     */
    private List<AlignmentDatabaseEntry> mySyncPoints = new ArrayList<>();

    /**
     * the latitude of the reference position.
     */
    private double latitude;

    /**
     * the longitude of the reference position.
     */
    private double longitude;

    /**
     * construct a file name for the point database for this device.
     * 
     * @param deviceName
     *            the name of the device
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
    protected List<AlignmentDatabaseEntry> getAlignmentDatabase() {
        return mySyncPoints;
    }

    /**
     * Get the database reference position.
     * 
     * @param position
     *            A pointer to a ln_lnlat_posn object to return the current
     *            position in
     * @return True if successful
     */
    protected boolean getDatabaseReferencePosition(LnLnlatPosn position) {
        position.lat = latitude;
        position.lng = longitude;
        return true;
    }

    /**
     * Load the database from persistent storage.
     * 
     * @param deviceName
     *            The name of the current device.
     * @return True if successful
     */
    protected boolean loadDatabase(String deviceName) {
        File db = getDataBaseFile(deviceName);
        if (db.getParentFile().mkdirs()) {
            LOG.error("sync point db directory created");
        }
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
     * Save the database to persistent storage.
     * 
     * @param deviceName
     *            The name of the current device.
     * @return True if successful
     */
    protected boolean saveDatabase(String deviceName) {
        File db = getDataBaseFile(deviceName);
        if (db.getParentFile().mkdirs()) {
            LOG.error("sync point db directory created");
        }
        try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(db))) {
            out.writeObject(mySyncPoints);
        } catch (IOException e) {
            LOG.error("could not save sync points to alignment database", e);
            return false;
        }
        return true;
    }

    /**
     * Set the database reference position.
     * 
     * @param newLatitude
     *            the latitude of the reference position
     * @param newLongitude
     *            the longitude of the reference position
     */
    protected void setDatabaseReferencePosition(double newLatitude, double newLongitude) {
        latitude = newLatitude;
        longitude = newLongitude;
    }

    /**
     * Set the function to be called when the database is loaded or reloaded.
     * 
     * @param callback
     *            the callback to call.
     */
    protected void setLoadDatabaseCallback(LoadDatabaseCallback callback) {
        callbacks.add(callback);
    }

    /**
     * Check the database for duplicate sync points.
     * 
     * @param candidateEntry
     *            the entry to compaire
     * @return true if there was a duplicate.
     */
    public boolean checkForDuplicateSyncPoint(AlignmentDatabaseEntry candidateEntry) {
        return checkForDuplicateSyncPoint(candidateEntry, MAXIMUM_TOLERANCE_BETWEEN_SYNC_POINTS);
    }

    /**
     * Check if a entry already exists in the database.
     * 
     * @param candidateEntry
     *            The candidate entry to check
     * @param tolerance
     *            The % tolerance used in the checking process (default 0.1%)
     * @return True if an entry already exists within the required tolerance
     */
    public boolean checkForDuplicateSyncPoint(AlignmentDatabaseEntry candidateEntry, double tolerance) {
        return false;
    }

}

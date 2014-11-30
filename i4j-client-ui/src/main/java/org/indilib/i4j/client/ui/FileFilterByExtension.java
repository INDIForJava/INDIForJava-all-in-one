/*
 *  This file is part of INDI for Java Client UI.
 * 
 *  INDI for Java Client UI is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Client UI is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Client UI.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.client.ui;

/*
 * #%L
 * INDI for Java Client UI Library
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
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.swing.filechooser.FileFilter;

import org.indilib.i4j.FileUtils;

/**
 * A convenience implementation of FileFilter that filters out all files except
 * for those type extensions that it knows about. Extensions are of the type
 * ".foo", which is typically found on Windows and Unix boxes, but not on
 * Macinthosh. Case is ignored. Example - create a new filter that filerts out
 * all files but gif and jpg image files: JFileChooser chooser = new
 * JFileChooser(); FileFilterByExtension filter = new FileFilterByExtension( new
 * String{"gif", "jpg"}, "JPEG & GIF Images")
 * chooser.addChoosableFileFilter(filter); chooser.showOpenDialog(this);
 * 
 * @version 1.9, March 19, 2012
 * @author Jeff Dinkins
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 */
public class FileFilterByExtension extends FileFilter {

    /**
     * String for unknown file types.
     */
    private static final String TYPE_UNKNOWN = "Type Unknown";

    /**
     * String for hidden files.
     */
    private static final String HIDDEN_FILE = "Hidden File";

    /**
     * A hashmap for every filter.
     */
    private LinkedHashMap filters = null;

    /**
     * The human readable description of the file filter.
     */
    private String description = null;

    /**
     * A more complete description for the file filter.
     */
    private String fullDescription = null;

    /**
     * To show the extensions in the description of the filters.
     */
    private boolean useExtensionsListInDescription = true;

    /**
     * Creates a file filter. If no filters are added, then all files are
     * accepted.
     * 
     * @see #addExtension
     */
    public FileFilterByExtension() {
        this.filters = new LinkedHashMap();
    }

    /**
     * Creates a file filter that accepts files with the given extension.
     * Example: new FileFilterByExtension("jpg");
     * 
     * @param extension
     *            The extension to be accepted by the filter.
     * @see #addExtension
     */
    public FileFilterByExtension(String extension) {
        this(extension, null);
    }

    /**
     * Creates a file filter that accepts the given file type. Example: new
     * FileFilterByExtension("jpg", "JPEG Image Images"); Note that the "."
     * before the extension is not needed. If provided, it will be ignored.
     * 
     * @param extension
     *            The extension to be accepted by the filter.
     * @param description
     *            The description of the kind of files that is accepted by the
     *            filter.
     * @see #addExtension
     */
    public FileFilterByExtension(String extension, String description) {
        this();
        if (extension != null) {
            addExtension(extension);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Creates a file filter from the given string array. Example: new
     * FileFilterByExtension(String {"gif", "jpg"}); Note that the "." before
     * the extension is not needed and will be ignored. There will be no
     * description for the extensions.
     * 
     * @param filters
     *            The array of extensions that the filter will accept.
     * @see #addExtension
     */
    public FileFilterByExtension(String[] filters) {
        this(filters, null);
    }

    /**
     * Creates a file filter from the given string array and description.
     * Example: new FileFilterByExtension(String {"gif", "jpg"},
     * "Gif and JPG Images"); Note that the "." before the extension is not
     * needed and will be ignored.
     * 
     * @param filters
     *            The array of extension s that the filer will accept.
     * @param description
     *            The description of the kind of files that is accepted by the
     *            filter.
     * @see #addExtension
     */
    public FileFilterByExtension(String[] filters, String description) {
        this();
        for (int i = 0; i < filters.length; i++) {
            // add filters one by one
            addExtension(filters[i]);
        }
        if (description != null) {
            setDescription(description);
        }
    }

    /**
     * Return true if this file should be shown in the directory panel, false if
     * it shouldn't. Files that begin with "." are ignored.
     * 
     * @param file
     *            The file to be tested by the filter.
     * @return <code>true</code> if the file should be shown. <code>false</code>
     *         otherwise.
     * @see #getExtension
     * @see FileFilter#accept
     */
    public boolean accept(File file) {
        if (file != null) {
            if (file.isDirectory()) {
                return true;
            }
            String extension = FileUtils.getExtensionOfFile(file).toLowerCase();
            if (extension != null && filters.get(extension) != null) {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds a filetype "dot" extension to filter against. For example: the
     * following code will create a filter that filters out all files except
     * those that end in ".jpg" and ".tif": FileFilterByExtension filter = new
     * FileFilterByExtension(); filter.addExtension("jpg");
     * filter.addExtension("tif"); Note that the "." before the extension is not
     * needed and will be ignored.
     * 
     * @param extension
     *            The new extension to be added to the filter.
     */
    public void addExtension(String extension) {
        if (filters == null) {
            filters = new LinkedHashMap();
        }
        filters.put(extension.toLowerCase(), this);
        fullDescription = null;
    }

    /**
     * Returns the human readable description of this filter. For example: "JPEG
     * and GIF Image Files (*.jpg, *.gif)"
     * 
     * @return The human readable description of the filter.
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     * @see FileFilter#getDescription
     */
    public String getDescription() {
        if (fullDescription == null) {
            if (description == null || isExtensionListInDescription()) {
                fullDescription = description == null ? "(" : description + " (";
                // build the description from the extension list
                Iterator extensions = filters.keySet().iterator();
                if (extensions != null) {
                    fullDescription += "." + (String) extensions.next();
                    while (extensions.hasNext()) {
                        fullDescription += ", " + (String) extensions.next();
                    }
                }
                fullDescription += ")";
            } else {
                fullDescription = description;
            }
        }
        return fullDescription;
    }

    /**
     * Sets the human readable description of this filter. For example:
     * filter.setDescription("Gif and JPG Images");
     * 
     * @param description
     *            The human readable description of the filter.
     * @see #setDescription
     * @see #setExtensionListInDescription
     * @see #isExtensionListInDescription
     */
    public void setDescription(String description) {
        this.description = description;
        fullDescription = null;
    }

    /**
     * Determines whether the extension list (.jpg, .gif, etc) should show up in
     * the human readable description. Only relevent if a description was
     * provided in the constructor or using setDescription();
     * 
     * @param useExtensionListInDescription
     *            if the extensions list should be shown
     * @see #getDescription
     * @see #setDescription
     * @see #isExtensionListInDescription
     */
    public void setExtensionListInDescription(boolean useExtensionListInDescription) {
        this.useExtensionsListInDescription = useExtensionListInDescription;
        fullDescription = null;
    }

    /**
     * Returns whether the extension list (.jpg, .gif, etc) should show up in
     * the human readable description. Only relevent if a description was
     * provided in the constructor or using setDescription();
     * 
     * @return if the extension list should be shown or not
     * @see #getDescription
     * @see #setDescription
     * @see #setExtensionListInDescription
     */
    public boolean isExtensionListInDescription() {
        return useExtensionsListInDescription;
    }
}

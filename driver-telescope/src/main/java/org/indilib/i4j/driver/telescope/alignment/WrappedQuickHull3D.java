package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L
 * INDI for Java Abstract Telescope Driver
 * %%
 * Copyright (C) 2012 - 2014 indiforjava
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

import java.util.ArrayList;
import java.util.List;

import org.gnu.savannah.gsl.GslMatrix;

import com.github.quickhull3d.Point3d;
import com.github.quickhull3d.QuickHull3D;

/**
 * Wrapper around the QuickHull3D class with some helper functions to make the
 * use in alignment system easier.
 * 
 * @author Richard van Nieuwenhoven
 */
public class WrappedQuickHull3D {

    /**
     * 3 dimentions.
     */
    private static final int DIM_3D = 3;

    /**
     * a calculated face of the ConvexHull.
     */
    public final class Face {

        /**
         * the indexes of the verdex points of the face.
         */
        private final int[] verdexIndexes;

        /**
         * transformation matrix.
         */
        protected GslMatrix matrix = new GslMatrix(DIM_3D, DIM_3D);

        /**
         * constructor with the verdex indexes.
         * 
         * @param verdexIndexes
         *            the verdex indexes.
         */
        private Face(int[] verdexIndexes) {
            this.verdexIndexes = verdexIndexes;
        }

        /**
         * get the index of the face corner in the original point list.
         * 
         * @param index
         *            corner index between 0 and 2
         * @return the number in the original point list.
         */
        public int vnum(int index) {
            return vnums[verdexIndexes[index]];
        }

    }

    /**
     * the wrapped convex hull.
     */
    private QuickHull3D quickHull3D = new QuickHull3D();

    /**
     * the points to build the hull from.
     */
    private List<Point3d> points = new ArrayList<>();

    /**
     * the calculated faces.
     */
    private Face[] faces;

    /**
     * the indexes of the original points.
     */
    private int[] vnums;

    /**
     * add one point to the convex hull.
     * 
     * @param x
     *            the x coordinate.
     * @param y
     *            the y coordinate.
     * @param z
     *            the z coordinate.
     */
    public void add(double x, double y, double z) {
        if (faces != null) {
            throw new IllegalAccessError("add may not be called anymore after the build of the convex hull");
        }
        points.add(new Point3d(x, y, z));
    }

    /**
     * build the hull and calculate the faces.
     */
    public void build() {
        quickHull3D.build(points.toArray(new Point3d[points.size()]));
        vnums = quickHull3D.getVertexPointIndices();
        int[][] faceIndexs = quickHull3D.getFaces();
        faces = new Face[faceIndexs.length];
        for (int index = 0; index < faceIndexs.length; index++) {
            faces[index] = new Face(faceIndexs[index]);
        }
    }

    /**
     * clear everything for a new calculation.
     */
    public void clear() {
        quickHull3D = new QuickHull3D();
        points = new ArrayList<>();
        faces = null;
        vnums = null;
    }

    /**
     * @return the calculated faces.
     */
    public Face[] getFaces() {
        if (faces == null) {
            throw new IllegalAccessError("faces not yet calculated, invoke build first!");
        }
        return faces;
    }

}

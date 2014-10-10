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

import org.gnu.savannah.gsl.GslMatrix;

public class ConvexHull {

    public static class Edge {

        Face[] adjface = {
            new Face(),
            new Face()
        };

        Vertex[] endpts = {
            new Vertex(),
            new Vertex()
        };

        /**
         * pointer to incident cone face.
         */
        protected Face newface;

        /**
         * True iff edge should be delete.
         */
        protected boolean delete_it;

        protected Edge next, prev;
    }

    public static class Face {

        protected Edge[] edge = {
            new Edge(),
            new Edge(),
            new Edge()
        };

        protected Vertex[] vertex = {
            new Vertex(),
            new Vertex(),
            new Vertex()
        };

        /**
         * True iff face visible from new point.
         */
        protected boolean visible;

        protected Face next, prev;

        protected GslMatrix pMatrix = new GslMatrix(3, 3);
    }

    public static class Vertex {

        protected int[] v = new int[3];

        protected int vnum;

        /**
         * pointer to incident cone edge (or NULL)
         */
        protected Edge duplicate;

        /**
         * True iff point on hull.
         */
        protected boolean onhull;

        /**
         * True iff point already processed.
         */
        protected boolean mark;

        protected Vertex next, prev;
    };

    public Face faces;

    public void constructHull() {
        // TODO to be implemented
    }

    public void doubleTriangle() {
        // TODO to be implemented
    }

    public void edgeOrderOnFaces() {
        // TODO to be implemented
    }

    public void makeNewVertex(double d, double e, double f, int i) {
        // TODO to be implemented
    }

    public void reset() {
        // TODO to be implemented
    }

}

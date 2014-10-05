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

    public class Edge {

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
        Face newface;

        /**
         * True iff edge should be delete.
         */
        boolean delete_it;

        Edge next, prev;
    }

    public class Face {

        Edge[] edge = {
            new Edge(),
            new Edge(),
            new Edge()
        };

        Vertex[] vertex = {
            new Vertex(),
            new Vertex(),
            new Vertex()
        };

        /**
         * True iff face visible from new point.
         */
        boolean visible;

        Face next, prev;

        GslMatrix pMatrix = new GslMatrix(3, 3);
    }

    public class Vertex {

        int[] v = new int[3];

        int vnum;

        /**
         * pointer to incident cone edge (or NULL)
         */
        Edge duplicate;

        /**
         * True iff point on hull.
         */
        boolean onhull;

        /**
         * True iff point already processed.
         */
        boolean mark;

        Vertex next, prev;
    };

    public Face faces;;

    public void constructHull() {
        // TODO Auto-generated method stub

    }

    public void doubleTriangle() {
        // TODO Auto-generated method stub

    }

    public void edgeOrderOnFaces() {
        // TODO Auto-generated method stub

    }

    public void makeNewVertex(double d, double e, double f, int i) {
        // TODO Auto-generated method stub

    }

    public void reset() {
        // TODO Auto-generated method stub

    }

}

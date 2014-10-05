package org.indilib.i4j.driver.telescope.alignment;

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

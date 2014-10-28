package org.indilib.i4j.driver.telescope.alignment;

/*
 * #%L INDI for Java Abstract Telescope Driver %% Copyright (C) 2013 - 2014
 * indiforjava %% This program is free software: you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the License,
 * or (at your option) any later version. This program is distributed in the
 * hope that it will be useful, but WITHOUT ANY WARRANTY; without even the
 * implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See
 * the GNU General Lesser Public License for more details. You should have
 * received a copy of the GNU General Lesser Public License along with this
 * program. If not, see <http://www.gnu.org/licenses/lgpl-3.0.html>. #L%
 */

import java.util.ArrayList;
import java.util.List;

import org.gnu.savannah.gsl.GslMatrix;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This code is described in "Computational Geometry in C" (Second Edition),
 * Chapter 4. It is not written to be comprehensible without the explanation in
 * that book. Input: 3n integer coordinates for the points. Output: the 3D
 * convex hull, in postscript with embedded comments showing the vertices and
 * faces. Written by Joseph O'Rourke, with contributions by Kristy Anderson,
 * John Kutcher, Catherine Schevon, Susan Weller. Last modified: May 2000
 * Questions to orourke@cs.smith.edu.</br>
 * -------------------------------------------------------------------- <br/>
 * This code is Copyright 2000 by Joseph O'Rourke. It may be freely
 * redistributed in its entirety provided that this copyright notice is not
 * removed.<br/>
 * --------------------------------------------------------------------<br/>
 * This class computes the convex hull of a set of 3d points.
 * 
 * @author Richard van Nieuwenhoven
 */
public class ConvexHull {

    private static class CircularArray<E> extends ArrayList<E> {

        @Override
        public E get(int index) {
            return super.get(index % size());
        }

        @Override
        public boolean add(E e) {
            super.add(0, e);
            return true;
        }

        /**
         * make sure that the first element in the circular list is e.
         * 
         * @param e
         *            the existing first element.
         */
        public void setAsFirst(E e) {
            while (get(0) != e) {
                E first = remove(0);
                super.add(first);
            }
        }
    }

    /**
     * The logger for any messages.
     */
    private static final Logger LOG = LoggerFactory.getLogger(ConvexHull.class);

    private static final int X = 0;

    private static final int Y = 1;

    private static final int Z = 2;

    /**
     * Define flags
     */
    private static final boolean ONHULL = true;

    private static final boolean REMOVED = true;

    private static final boolean VISIBLE = true;

    private static final boolean PROCESSED = true;

    /**
     * Range of safe coord values.
     */
    private static final int SAFE = 1000000;

    private boolean check = false;

    private int scaleFactor = SAFE - 1; // Scale factor to be used when
                                        // converting from

    // floating

    // point to integers and vice versa

    private CircularArray<Vertex> vertices = new CircularArray<ConvexHull.Vertex>();

    private List<Edge> edges = new CircularArray<ConvexHull.Edge>();

    public static class Edge {

        Face[] adjface = new Face[2];

        Vertex[] endpts = new Vertex[2];

        /**
         * pointer to incident cone face.
         */
        protected Face newface;

        /**
         * True iff edge should be delete.
         */
        protected boolean delete_it;

        public void copy(Edge duplicate) {
            for (int index = 0; index < adjface.length; index++) {
                adjface[index] = duplicate.adjface[index];
            }
            for (int index = 0; index < endpts.length; index++) {
                endpts[index] = duplicate.endpts[index];
            }
            // TODO do no know if i should copy the object or de ref?
            newface = duplicate.newface;

        }

        public boolean isNull() {
            return false;
        }

        public Edge copy() {
            Edge result = new Edge();
            result.copy(this);
            return result;
        }

    }

    public static class Face {

        protected Edge[] edge = new Edge[3];

        protected Vertex[] vertex = new Vertex[3];

        /**
         * True iff face visible from new point.
         */
        protected boolean visible;

        protected GslMatrix matrix = new GslMatrix(3, 3);

    }

    public static class Vertex {

        protected int[] v = new int[3];

        protected int vnum;

        /**
         * pointer to incident cone edge (or NULL).
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

    };

    public List<Face> faces = new CircularArray<ConvexHull.Face>();

    public int Volumei(Face f, Vertex p) {
        int vol;
        int ax, ay, az, bx, by, bz, cx, cy, cz;

        ax = f.vertex[0].v[X] - p.v[X];
        ay = f.vertex[0].v[Y] - p.v[Y];
        az = f.vertex[0].v[Z] - p.v[Z];
        bx = f.vertex[1].v[X] - p.v[X];
        by = f.vertex[1].v[Y] - p.v[Y];
        bz = f.vertex[1].v[Z] - p.v[Z];
        cx = f.vertex[2].v[X] - p.v[X];
        cy = f.vertex[2].v[Y] - p.v[Y];
        cz = f.vertex[2].v[Z] - p.v[Z];

        vol = (ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az * (bx * cy - by * cx));

        return vol;
    }

    /**
     * VolumeSign returns the sign of the volume of the tetrahedron determined
     * by f and p. VolumeSign is +1 iff p is on the negative side of f, where
     * the positive side is determined by the rh-rule. So the volume is positive
     * if the ccw normal to f points outside the tetrahedron. The final
     * fewer-multiplications form is due to Bob Williamson.
     */
    public int volumeSign(Face f, Vertex p) {
        double vol;
        int voli;
        double ax, ay, az, bx, by, bz, cx, cy, cz;

        ax = f.vertex[0].v[X] - p.v[X];
        ay = f.vertex[0].v[Y] - p.v[Y];
        az = f.vertex[0].v[Z] - p.v[Z];
        bx = f.vertex[1].v[X] - p.v[X];
        by = f.vertex[1].v[Y] - p.v[Y];
        bz = f.vertex[1].v[Z] - p.v[Z];
        cx = f.vertex[2].v[X] - p.v[X];
        cy = f.vertex[2].v[Y] - p.v[Y];
        cz = f.vertex[2].v[Z] - p.v[Z];

        vol = ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az * (bx * cy - by * cx);

        if (LOG.isDebugEnabled()) {
            /* Compute the volume using integers for comparison. */
            voli = Volumei(f, p);
            LOG.debug("Face=" + f + "; Vertex=" + p.vnum + ": vol(int) = " + voli + ", vol(double) = " + vol);
        }

        /* The volume should be an integer. */
        if (vol > 0.5)
            return 1;
        else if (vol < -0.5)
            return -1;
        else
            return 0;
    }

    /**
     * AddOne is passed a vertex. It first determines all faces visible from
     * that point. If none are visible then the point is marked as not onhull.
     * Next is a loop over edges. If both faces adjacent to an edge are visible,
     * then the edge is marked for deletion. If just one of the adjacent faces
     * is visible then a new face is constructed.
     */
    public boolean addOne(Vertex p) {

        Edge e, temp;
        int vol;
        boolean vis = false;

        if (LOG.isDebugEnabled()) {
            LOG.debug("AddOne: starting to add v" + p.vnum + ".");
        }

        /* Mark faces visible from p. */
        for (Face f : faces) {
            vol = volumeSign(f, p);
            if (LOG.isDebugEnabled())
                LOG.debug("faddr: " + f + "   paddr: " + p + "   Vol = " + vol);
            if (vol < 0) {
                f.visible = VISIBLE;
                vis = true;
            }
        }

        /* If no faces are visible from p, then p is inside the hull. */
        if (!vis) {
            p.onhull = !ONHULL;
            return false;
        }

        /*
         * Mark edges in interior of visible region for deletion. Erect a
         * newface based on each border edge.
         */
        int index = 0;
        e = edges.get(index++);
        do {
            temp = edges.get(index++);
            if (e.adjface[0].visible && (e.adjface[1] != null && e.adjface[1].visible))
                /* e interior: mark for deletion. */
                e.delete_it = REMOVED;
            else if (e.adjface[0].visible || (e.adjface[1] != null && e.adjface[1].visible))
                /* e border: make a new face. */
                e.newface = MakeConeFace(e, p);
            e = temp;
        } while (index < edges.size());
        return true;
    }

    /**
     * MakeConeFace makes a new face and two new edges between the edge and the
     * point that are passed to it. It returns a pointer to the new face.
     */
    public Face MakeConeFace(Edge e, Vertex p) {

        Edge[] new_edge = new Edge[2];
        Face new_face;
        int i, j;

        /* Make two new edges (if don't already exist). */
        for (i = 0; i < 2; ++i) {
            /* If the edge exists, copy it into new_edge. */
            new_edge[i] = copy(e.endpts[i].duplicate);
            if (new_edge[i] == null) {
                /* Otherwise (duplicate is NULL), MakeNullEdge. */
                new_edge[i] = MakeNullEdge();
                new_edge[i].endpts[0] = e.endpts[i];
                new_edge[i].endpts[1] = p;
                e.endpts[i].duplicate = new_edge[i];
            }
        }

        /* Make the new face. */
        new_face = MakeNullFace();
        new_face.edge[0] = e;
        new_face.edge[1] = new_edge[0];
        new_face.edge[2] = new_edge[1];
        MakeCcw(new_face, e, p);

        /* Set the adjacent face pointers. */
        for (i = 0; i < 2; ++i)
            for (j = 0; j < 2; ++j)
                /* Only one NULL link should be set to new_face. */
                if (new_edge[i].adjface[j] == null) {
                    new_edge[i].adjface[j] = new_face;
                    break;
                }

        return new_face;
    }

    /**
     * \brief MakeCcw puts the vertices in the face structure in counterclock
     * wise order. We want to store the vertices in the same order as in the
     * visible face. The third vertex is always p. Although no specific ordering
     * of the edges of a face are used by the code, the following condition is
     * maintained for each face f: one of the two endpoints of f.edge[i] matches
     * f.vertex[i]. But note that this does not imply that f.edge[i] is between
     * f.vertex[i] and f.vertex[(i+1)%3]. (Thanks to Bob Williamson.)
     */
    public void MakeCcw(Face f, Edge e, Vertex p) {

        Face fv; /* The visible face adjacent to e */
        int i; /* Index of e.endpoint[0] in fv. */

        if (e.adjface[0].visible)
            fv = e.adjface[0];
        else
            fv = e.adjface[1];

        /*
         * Set vertex[0] & [1] of f to have the same orientation as do the
         * corresponding vertices of fv.
         */
        for (i = 0; fv.vertex[i] != e.endpts[0]; ++i)
            ;
        /* Orient f the same as fv. */
        if (fv.vertex[(i + 1) % 3] != e.endpts[1]) {
            f.vertex[0] = e.endpts[1];
            f.vertex[1] = e.endpts[0];
        } else {
            f.vertex[0] = e.endpts[0];
            f.vertex[1] = e.endpts[1];
            Edge s = f.edge[1]; /* Temporary, for swapping */
            f.edge[1] = f.edge[2];
            f.edge[2] = s;
        }
        /*
         * This swap is tricky. e is edge[0]. edge[1] is based on endpt[0],
         * edge[2] on endpt[1]. So if e is oriented "forwards," we need to move
         * edge[1] to follow [0], because it precedes.
         */

        f.vertex[2] = p;

    }

    private Edge copy(Edge duplicate) {
        if (duplicate == null) {
            return null;
        } else {
            return duplicate.copy();
        }
    }

    /**
     * MakeNullEdge creates a new cell and initializes all pointers to NULL and
     * sets all flags to off. It returns a pointer to the empty cell.
     */
    public Edge MakeNullEdge() {
        Edge e;

        e = new Edge();
        e.adjface[0] = e.adjface[1] = e.newface = null;
        e.endpts[0] = e.endpts[1] = null;
        e.delete_it = !REMOVED;
        edges.add(e);
        return e;
    }

    /**
     * MakeNullFace creates a new face structure and initializes all of its
     * flags to NULL and sets all the flags to off. It returns a pointer to the
     * empty cell.
     */
    public Face MakeNullFace() {
        Face f;
        int i;

        f = new Face();
        for (i = 0; i < 3; ++i) {
            f.edge[i] = null;
            f.vertex[i] = null;
        }
        f.visible = !VISIBLE;
        faces.add(f);
        return f;
    }

    /**
     * ConstructHull adds the vertices to the hull one at a time. The hull
     * vertices are those in the list marked as onhull.
     */
    public void constructHull() {

        int index = 0;
        Vertex v = vertices.get(index++);
        do {
            Vertex vnext = vertices.get(index++);
            if (!v.mark) {
                v.mark = PROCESSED;
                addOne(v);
                CleanUp(vnext); /* Pass down vnext in case it gets deleted. */

                if (check) {
                    LOG.info("ConstructHull: After Add of " + v.vnum + " & Cleanup:");
                    Checks();
                }
                // if ( debug )
                // PrintOut( v );
            }
            v = vnext;
        } while (index < vertices.size());
    }

    /**
     * Checks the consistency of the hull and prints the results to the standard
     * error output.
     */
    public void Checks() {

        Consistency();
        Convexity();
        int V = 0;
        for (Vertex v : vertices) {
            if (v.mark) {
                V++;
            }
        }
        int E = edges.size();

        int F = faces.size();

        CheckEuler(V, E, F);
        CheckEndpts();

    }

    /**
     * Checks that, for each face, for each i={0,1,2}, the [i]th vertex of that
     * face is either the [0]th or [1]st endpoint of the [ith] edge of the face.
     */
    public void CheckEndpts() {
        boolean error = false;
        for (Face face : faces) {
            for (int i = 0; i < 3; ++i) {
                Vertex v = face.vertex[i];
                Edge e = face.edge[i];
                if (v != e.endpts[0] && v != e.endpts[1]) {
                    error = true;
                    LOG.info("CheckEndpts: Error!\n  addr: " + faces + ":  edges:(" + e.endpts[0].vnum + "," + e.endpts[1].vnum + ")");
                }
            }
        }

        if (error)
            LOG.error("Checks: ERROR found and reported above.");
        else
            LOG.info("Checks: All endpts of all edges of all faces check.");
    }

    /**
     * CheckEuler checks Euler's relation, as well as its implications when all
     * faces are known to be triangles. Only prints positive information when
     * debug is true, but always prints negative information.
     */
    public void CheckEuler(int V, int E, int F) {
        if (check)
            LOG.info("Checks: V, E, F = " + V + ' ' + E + ' ' + F + ":\t");

        if ((V - E + F) != 2)
            LOG.info("Checks: V-E+F != 2");
        else if (check)
            LOG.info("V-E+F = 2\t");

        if (F != (2 * V - 4))
            LOG.info("Checks: F=" + F + " != 2V-4=" + (2 * V - 4) + "; V=" + V);
        else if (check)
            LOG.info("F = 2V-4\t");

        if ((2 * E) != (3 * F))
            LOG.info("Checks: 2E=" + (2 * E) + " != 3F=" + (3 * F) + "; E=" + E + ", F=" + F);
        else if (check)
            LOG.info("2E = 3F");
    }

    /**
     * Consistency runs through the edge list and checks that all adjacent faces
     * have their endpoints in opposite order. This verifies that the vertices
     * are in counterclockwise order.
     */
    public boolean Collinear(Vertex a, Vertex b, Vertex c) {
        return (c.v[Z] - a.v[Z]) * (b.v[Y] - a.v[Y]) - (b.v[Z] - a.v[Z]) * (c.v[Y] - a.v[Y]) == 0
                && (b.v[Z] - a.v[Z]) * (c.v[X] - a.v[X]) - (b.v[X] - a.v[X]) * (c.v[Z] - a.v[Z]) == 0
                && (b.v[X] - a.v[X]) * (c.v[Y] - a.v[Y]) - (b.v[Y] - a.v[Y]) * (c.v[X] - a.v[X]) == 0;
    }

    /**
     * Consistency runs through the edge list and checks that all adjacent faces
     * have their endpoints in opposite order. This verifies that the vertices
     * are in counterclockwise order.
     */
    public void Consistency() {
        int i, j;
        boolean error = false;
        for (Edge e : edges) {
            /* find index of endpoint[0] in adjacent face[0] */
            for (i = 0; e.adjface[0].vertex[i] != e.endpts[0]; ++i)
                ;

            /* find index of endpoint[0] in adjacent face[1] */
            for (j = 0; e.adjface[1].vertex[j] != e.endpts[0]; ++j)
                ;

            /* check if the endpoints occur in opposite order */
            if (!(e.adjface[0].vertex[(i + 1) % 3] == e.adjface[1].vertex[(j + 2) % 3] || e.adjface[0].vertex[(i + 2) % 3] == e.adjface[1].vertex[(j + 1) % 3])) {
                error = true;
                break;
            }
        }

        if (error)
            LOG.error("Checks: edges are NOT consistent.");
        else
            LOG.info("Checks: edges consistent.");
    }

    /**
     * Convexity checks that the volume between every face and every point is
     * negative. This shows that each point is inside every face and therefore
     * the hull is convex.
     */
    public void Convexity() {

        int vol;
        boolean error = false;

        for (Face f : faces) {
            for (Vertex v : vertices) {
                if (v.mark) {
                    vol = VolumeSign(f, v);
                    if (vol < 0) {
                        error = true;
                        break;
                    }
                }

            }
        }

        if (error)
            LOG.error("Checks: NOT convex.");
        else
            LOG.info("Checks: convex.");
    }

    /**
     * \brief VolumeSign returns the sign of the volume of the tetrahedron
     * determined by f and p. VolumeSign is +1 iff p is on the negative side of
     * f, where the positive side is determined by the rh-rule. So the volume is
     * positive if the ccw normal to f points outside the tetrahedron. The final
     * fewer-multiplications form is due to Bob Williamson.
     */
    private int VolumeSign(Face f, Vertex p) {
        double vol;
        int voli;
        double ax, ay, az, bx, by, bz, cx, cy, cz;

        ax = f.vertex[0].v[X] - p.v[X];
        ay = f.vertex[0].v[Y] - p.v[Y];
        az = f.vertex[0].v[Z] - p.v[Z];
        bx = f.vertex[1].v[X] - p.v[X];
        by = f.vertex[1].v[Y] - p.v[Y];
        bz = f.vertex[1].v[Z] - p.v[Z];
        cx = f.vertex[2].v[X] - p.v[X];
        cy = f.vertex[2].v[Y] - p.v[Y];
        cz = f.vertex[2].v[Z] - p.v[Z];

        vol = ax * (by * cz - bz * cy) + ay * (bz * cx - bx * cz) + az * (bx * cy - by * cx);

        if (LOG.isDebugEnabled()) {
            /* Compute the volume using integers for comparison. */
            voli = Volumei(f, p);
            LOG.info("Face=" + f + "; Vertex=" + p.vnum + ": vol(int) = " + voli + ", vol(double) = " + vol);
        }

        /* The volume should be an integer. */
        if (vol > 0.5)
            return 1;
        else if (vol < -0.5)
            return -1;
        else
            return 0;
    }

    /**
     * DoubleTriangle builds the initial double triangle. It first finds 3
     * noncollinear points and makes two faces out of them, in opposite order.
     * It then finds a fourth point that is not coplanar with that face. The
     * vertices are stored in the face structure in counterclockwise order so
     * that the volume between the face and the point is negative. Lastly, the 3
     * newfaces to the fourth point are constructed and the data structures are
     * cleaned up.
     */
    public void doubleTriangle() {

        Vertex v0, v1, v2, v3;
        Face f0, f1 = null;
        int vol;

        /* Find 3 noncollinear points. */
        int index = 0;
        v0 = vertices.get(index++);
        while (Collinear(v0, vertices.get(index + 1), vertices.get(index + 2))) {
            v0 = vertices.get(index++);
            if (v0 == vertices.get(0)) {
                throw new IllegalArgumentException("DoubleTriangle:  All points are Collinear!");
            }
        }
        v1 = vertices.get(index++);
        v2 = vertices.get(index++);

        /* Mark the vertices as processed. */
        v0.mark = PROCESSED;
        v1.mark = PROCESSED;
        v2.mark = PROCESSED;

        /* Create the two "twin" faces. */
        f0 = MakeFace(v0, v1, v2, f1);
        f1 = MakeFace(v2, v1, v0, f0);

        /* Link adjacent face fields. */
        f0.edge[0].adjface[1] = f1;
        f0.edge[1].adjface[1] = f1;
        f0.edge[2].adjface[1] = f1;
        f1.edge[0].adjface[1] = f0;
        f1.edge[1].adjface[1] = f0;
        f1.edge[2].adjface[1] = f0;

        /* Find a fourth, noncoplanar point to form tetrahedron. */
        v3 = vertices.get(index++);
        vol = VolumeSign(f0, v3);
        while (vol == 0) {
            v3 = vertices.get(index++);
            if (v3 == v0) {
                throw new IllegalArgumentException("DoubleTriangle:  All points are coplanar!");
            }
            vol = VolumeSign(f0, v3);
        }

        /* Insure that v3 will be the first added. */
        vertices.setAsFirst(v3);
        LOG.debug("DoubleTriangle: finished. Head repositioned at v3.");
    }

    /**
     * MakeFace creates a new face structure from three vertices (in ccw order).
     * It returns a pointer to the face.
     */
    public Face MakeFace(Vertex v0, Vertex v1, Vertex v2, Face fold) {

        Face f;
        Edge e0, e1, e2;

        /* Create edges of the initial triangle. */
        if (fold == null) {
            e0 = MakeNullEdge();
            e1 = MakeNullEdge();
            e2 = MakeNullEdge();
        } else { /* Copy from fold, in reverse order. */
            e0 = fold.edge[2];
            e1 = fold.edge[1];
            e2 = fold.edge[0];
        }
        e0.endpts[0] = v0;
        e0.endpts[1] = v1;
        e1.endpts[0] = v1;
        e1.endpts[1] = v2;
        e2.endpts[0] = v2;
        e2.endpts[1] = v0;

        /* Create face for triangle. */
        f = MakeNullFace();
        f.edge[0] = e0;
        f.edge[1] = e1;
        f.edge[2] = e2;
        f.vertex[0] = v0;
        f.vertex[1] = v1;
        f.vertex[2] = v2;

        /* Link edges to face. */
        e0.adjface[0] = e1.adjface[0] = e2.adjface[0] = f;

        return f;
    }

    /**
     * EdgeOrderOnFaces: puts e0 between v0 and v1, e1 between v1 and v2, e2
     * between v2 and v0 on each face. This should be unnecessary, alas. Not
     * used in code, but useful for other purposes.
     */
    public void edgeOrderOnFaces() {
        Edge newEdge;
        int i, j;
        for (Face f : faces) {
            for (i = 0; i < 3; i++) {
                if (!(((f.edge[i].endpts[0] == f.vertex[i]) && (f.edge[i].endpts[1] == f.vertex[(i + 1) % 3])) || ((f.edge[i].endpts[1] == f.vertex[i]) && (f.edge[i].endpts[0] == f.vertex[(i + 1) % 3])))) {
                    /* Change the order of the edges on the face: */
                    for (j = 0; j < 3; j++) {
                        /* find the edge that should be there */
                        if (((f.edge[j].endpts[0] == f.vertex[i]) && (f.edge[j].endpts[1] == f.vertex[(i + 1) % 3]))
                                || ((f.edge[j].endpts[1] == f.vertex[i]) && (f.edge[j].endpts[0] == f.vertex[(i + 1) % 3]))) {
                            /*
                             * Swap it with the one erroneously put into its
                             * place:
                             */
                            if (LOG.isDebugEnabled())
                                LOG.debug("Making a swap in EdgeOrderOnFaces: F(" + f.vertex[0].vnum + ',' + f.vertex[1].vnum + ',' + f.vertex[2].vnum + "): e[" + i
                                        + "] and e[" + j + "]");
                            newEdge = f.edge[i];
                            f.edge[i] = f.edge[j];
                            f.edge[j] = newEdge;
                        }
                    }
                }
            }
        }
    }

    /**
     * Makes a vertex from the supplied information and adds it to the vertices
     * list.
     */
    public void makeNewVertex(double x, double y, double z, int VertexId) {
        Vertex v = MakeNullVertex();
        v.v[X] = (int) (x * scaleFactor);
        v.v[Y] = (int) (y * scaleFactor);
        v.v[Z] = (int) (z * scaleFactor);
        v.vnum = VertexId;
        if ((Math.abs(x) > SAFE) || (Math.abs(y) > SAFE) || (Math.abs(z) > SAFE)) {
            LOG.debug("Coordinate of vertex below might be too large: run with -d flag");
            PrintPoint(v);
        }
    }

    /**
     * \brief Prints a single vertex to the standard output.
     */
    void PrintPoint(Vertex p) {
        int i;
        String log = "";

        for (i = 0; i < 3; i++)
            log = log + '\t' + p.v[i];
        LOG.info(log);
    }

    /**
     * Set the floating point to integer scaling factor
     */
    public int setScaleFactor() {
        return scaleFactor;
    }

    /**
     * Set the floating point to integer scaling factor. If you want to tweak
     * this a good value to start from may well be a little bit more than the
     * resolution of the mounts encoders. Whatever is used must not exceed the
     * default value which is set to the constant SAFE.
     */
    public void SetScaleFactor(int newScaleFactor) {
        scaleFactor = newScaleFactor;
    }

    /**
     * \brief MakeNullVertex: Makes a vertex, nulls out fields.
     */
    Vertex MakeNullVertex() {

        Vertex v = new Vertex();
        v.duplicate = null;
        v.onhull = !ONHULL;
        v.mark = !PROCESSED;
        vertices.add(v);
        return v;
    }

    /**
     * brief Frees the vertices edges and faces lists and resets the debug and
     * check flags.
     */
    public void reset() {
        vertices.clear();
        edges.clear();
        faces.clear();
        check = false;
    }

    /**
     * \brief CleanEdges runs through the edge list and cleans up the structure.
     * If there is a newface then it will put that face in place of the visible
     * face and NULL out newface. It also deletes so marked edges.
     */
    void CleanEdges() {
        /* Integrate the newface's into the data structure. */
        /* Check every edge. */
        for (Edge e : edges) {

            if (e.newface != null) {
                if (e.adjface[0].visible)
                    e.adjface[0] = e.newface;
                else
                    e.adjface[1] = e.newface;
                e.newface = null;
            }
        }

        /* Delete any edges marked for deletion. */
        for (Edge e : new ArrayList<Edge>(edges)) {
            if (e.delete_it) {
                edges.remove(e);
            }
        }
    }

    /**
     * \brief CleanFaces runs through the face list and deletes any face marked
     * visible.
     */
    void CleanFaces() {
        for (Face f : new ArrayList<Face>(faces)) {
            if (f.visible) {
                faces.remove(f);
            }
        }
    }

    /**
     * \brief CleanUp goes through each data structure list and clears all flags
     * and NULLs out some pointers. The order of processing (edges, faces,
     * vertices) is important.
     */
    void CleanUp(Vertex pvnext) {
        CleanEdges();
        CleanFaces();
        CleanVertices(pvnext);
    }

    /**
     * \brief CleanVertices runs through the vertex list and deletes the
     * vertices that are marked as processed but are not incident to any
     * undeleted edges. The pointer to vnext, pvnext, is used to alter vnext in
     * ConstructHull() if we are about to delete vnext.
     */
    void CleanVertices(Vertex pvnext) {
        for (Vertex v : new ArrayList<Vertex>(vertices)) {
            if (v.mark && !v.onhull) {
                vertices.remove(v);
            } else {
                /* Reset flags. */
                v.duplicate = null;
                v.onhull = !ONHULL;
            }
        }
    }
}

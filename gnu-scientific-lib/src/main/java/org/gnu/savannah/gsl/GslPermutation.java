package org.gnu.savannah.gsl;

/*
 * #%L
 * GNU Scientific Library port
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

import org.gnu.savannah.gsl.util.DoubleArray;

public class GslPermutation {

    public int size;

    public int[] data;

    public GslPermutation(int n) {
        if (n == 0) {
            throw new IllegalStateException("permutation length n must be positive integer");
        }
        this.data = new int[n];
        this.size = n;
    }

    public void init() {
        int n = this.size;

        /* initialize permutation to identity */

        for (int i = 0; i < n; i++) {
            this.data[i] = i;
        }
    }

    public GslErrno swap(int i, int j) {
        if (i >= size) {
            throw new IllegalStateException("first index is out of range");
        }
        if (j >= size) {
            throw new IllegalStateException("second index is out of range");
        }
        if (i != j) {
            int tmp = this.data[i];
            this.data[i] = this.data[j];
            this.data[j] = tmp;
        }
        return GslErrno.GSL_SUCCESS;
    }

    public GslErrno permuteVector(GslVector v) {
        if (v.size != this.size) {
            throw new IllegalStateException("vector and permutation must be the same length");
        }

        permute(this.data, v.data, v.stride, v.size);

        return GslErrno.GSL_SUCCESS;
    }

    /*
     * In-place Permutations permute: OUT[i] = IN[perm[i]] i = 0 .. N-1
     * invpermute: OUT[perm[i]] = IN[i] i = 0 .. N-1 PERM is an index map, i.e.
     * a vector which contains a permutation of the integers 0 .. N-1. From
     * Knuth "Sorting and Searching", Volume 3 (3rd ed), Section 5.2 Exercise 10
     * (answers), p 617 FIXME: these have not been fully tested.
     */

    public GslErrno permute(int[] p, DoubleArray data, int stride, int n) {
        int i, k, pk;

        for (i = 0; i < n; i++) {
            k = p[i];

            while (k > i)
                k = p[k];

            if (k < i)
                continue;

            /* Now have k == i, i.e the least in its cycle */

            pk = p[k];

            if (pk == i)
                continue;

            /* shuffle the elements of the cycle */

            {
                int a;

                double[] t = new double[1];

                for (a = 0; a < 1; a++)
                    t[a] = data.get(i * stride + a);

                while (pk != i) {
                    for (a = 0; a < 1; a++) {
                        double r1 = data.get(pk * stride + a);
                        data.set(k * stride + a, r1);
                    }
                    k = pk;
                    pk = p[k];
                }

                for (a = 0; a < 1; a++)
                    data.set(k * stride + a, t[a]);
            }
        }

        return GslErrno.GSL_SUCCESS;
    }
}

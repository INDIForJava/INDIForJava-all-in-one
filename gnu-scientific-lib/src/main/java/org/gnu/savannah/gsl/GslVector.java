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

public class GslVector {

    public GslVector(int n) {
        if (n == 0) {
            throw new IllegalStateException("vector length n must be positive integer");
        }
        this.block = new GslBlock(n);
        this.data = block.data;
        this.size = n;
        this.stride = 1;
        this.owner = 1;
    }

    public int size;

    public int stride;

    public DoubleArray data;

    public GslBlock block;

    public int owner;

    public double get(int i) {
        return data.get(i * stride);
    }

    public void set(int i, double x) {
        data.set(i * stride, x);
    }

    public void setZero() {
        for (int index = 0; index < size; index++) {
            data.set(index * stride, 0d);
        }
    }

    public GslVectorView subvector(int offset, int n) {
        GslVectorView view = new GslVectorView();

        if (n == 0) {
            throw new IllegalStateException("vector length n must be positive integer");
        }

        if (offset + (n - 1) >= this.size) {
            throw new IllegalStateException("view would extend past end of vector");
        }

        {
            GslVector s = new GslVector(n);

            s.data = new DoubleArray(this.data, this.stride * offset);
            s.size = n;
            s.stride = this.stride;
            s.block = this.block;
            s.owner = 0;

            view.vector = s;
            return view;
        }
    }

    public GslVectorView subvectorWithStride(int offset, int stride, int n) {
        GslVectorView view = new GslVectorView();
        if (n == 0) {
            throw new IllegalStateException("vector length n must be positive integer");
        }

        if (stride == 0) {
            throw new IllegalStateException("stride must be positive integer");
        }

        if (offset + (n - 1) * stride >= this.size) {
            throw new IllegalStateException("view would extend past end of vector");
        }

        {
            GslVector s = new GslVector(1);

            s.data = new DoubleArray(this.data, this.stride * offset);
            s.size = n;
            s.stride = this.stride * stride;
            s.block = this.block;
            s.owner = 0;

            view.vector = s;
            return view;
        }
    }

    public GslErrno swapElements(int i, int j) {

        if (i >= size) {
            throw new IllegalStateException("first index is out of range");
        }

        if (j >= size) {
            throw new IllegalStateException("second index is out of range");
        }

        if (i != j) {
            int s = stride;
            int k;

            for (k = 0; k < 1; k++) {
                double tmp = data.get(j * s + k);
                data.set(j * s + k, data.get(i * s + k));
                data.set(i * s + k, tmp);
            }
        }

        return GslErrno.GSL_SUCCESS;
    }

    public boolean isNull() {
        int n = this.size;
        int stride = this.stride;

        int j;

        for (j = 0; j < n; j++) {
            int k;

            for (k = 0; k < 1; k++) {
                if (this.data.get(stride * j + k) != 0.0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isPos() {
        int n = this.size;
        int stride = this.stride;

        int j;

        for (j = 0; j < n; j++) {
            int k;

            for (k = 0; k < 1; k++) {
                if (this.data.get(stride * j + k) <= 0.0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isNeg() {
        int n = this.size;
        int stride = this.stride;

        int j;

        for (j = 0; j < n; j++) {
            int k;

            for (k = 0; k < 1; k++) {
                if (this.data.get(stride * j + k) >= 0.0) {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isNonNeg() {
        int n = this.size;
        int stride = this.stride;

        int j;

        for (j = 0; j < n; j++) {
            int k;

            for (k = 0; k < 1; k++) {
                if (this.data.get(stride * j + k) < 0.0) {
                    return false;
                }
            }
        }

        return true;
    }

    public DoubleArray ptr(int i) {
        return new DoubleArray(data, i * stride);
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder("gsl_vector(");
        for (int i = 0; i < size; i++) {
            if (i != 0) {
                result.append(',');
            }
            result.append(get(i));
        }
        result.append(')');
        return result.toString();
    }
}

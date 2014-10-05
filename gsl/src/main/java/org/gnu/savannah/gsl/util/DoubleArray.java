package org.gnu.savannah.gsl.util;

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

public class DoubleArray {
	private final double[] data;
	private final int offset;

	public DoubleArray(double[] data, int offset) {
		this.data = data;
		this.offset = offset;
	}

	public DoubleArray(DoubleArray data, int offset) {
		this.data = data.data;
		this.offset = data.offset + offset;
	}

	public DoubleArray(int size) {
		this.data = new double[size];
		this.offset = 0;
	}

	public DoubleArray(double... values) {
		this.data = values;
		this.offset = 0;
	}

	public void set(int index, double value) {
		data[offset + index] = value;
	}

	public double get(int index) {
		return data[offset + index];
	}

	public static void arraycopy(DoubleArray data1, int i, DoubleArray data2,
			int j, int k) {
		System.arraycopy(data1.data, data1.offset + i, data2.data, data2.offset
				+ j, k);

	}

	@Override
	public int hashCode() {
		return data.hashCode() + offset;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof DoubleArray) {
			DoubleArray other = (DoubleArray) obj;
			return data == other.data && offset == other.offset;
		}
		return false;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer("[");
		for (int i = offset; i < data.length; i++) {
			if (i != 0) {
				result.append(',');
			}
			result.append(data[i]);
		}
		result.append(']');
		return result.toString();
	}

	public double[] get() {
		return data;
	}
}

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

public enum CBLAS_DIAG {
    CblasNonUnit(131),
    CblasUnit(132);

    final int nr;

    private CBLAS_DIAG(int nr) {
        this.nr = nr;
    }

    public static CBLAS_DIAG valueOf(int i) {
        for (CBLAS_DIAG order : values()) {
            if (order.nr == i) {
                return order;
            }
        }
        return null;
    }
}

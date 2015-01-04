package org.indilib.i4j.client.fx;

/*
 * #%L
 * INDI for Java Client UI Library
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
/**
 * generic interface to get the gui from a controller.
 * 
 * @author Richard van Nieuwenhoven
 */
public interface INDIFxAccess {

    /**
     * get the gui element of the controller and cast it to the requested type.
     * (And throw a case exception if it does not match)
     * 
     * @param clazz
     *            the requested type
     * @return the gui elemnet of the controller.
     * @param <T>
     *            the the requested type
     */
    <T> T getGui(Class<T> clazz);
}

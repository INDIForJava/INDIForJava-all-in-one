package org.indilib.i4j.protocol;

/*
 * #%L
 * INDI Protocol implementation
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
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

/**
 * This class represents an INDI XML protocol element.
 * 
 * @param <T>
 *            type for the builder
 * @author Richard van Nieuwenhoven
 */
public abstract class INDIProtocol<T> {

    /**
     * the name element attribute.
     */
    @XStreamAsAttribute
    private String name;

    /**
     * @return the name element attribute.
     */
    public String getName() {
        return name;
    }

    /**
     * set the name element attribute.
     * 
     * @param newName
     *            the new name of the attribute.
     * @return this for builder pattern.
     */
    @SuppressWarnings("unchecked")
    public T setName(String newName) {
        this.name = newName;
        return (T) this;
    }
}

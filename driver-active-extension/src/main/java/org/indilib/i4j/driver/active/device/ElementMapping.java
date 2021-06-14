package org.indilib.i4j.driver.active.device;

/*
 * #%L
 * INDI for Java Acive Device Extension
 * %%
 * Copyright (C) 2012 - 2015 indiforjava
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

import org.indilib.i4j.properties.INDIStandardElement;

public class ElementMapping {

    private final String sourceProperty;

    private final String sourceElement;

    private final String targetProperty;

    private final String targetElement;

    public ElementMapping(String sourceProperty, String sourceElement, String targetProperty, String targetElement) {
        this.sourceProperty = sourceProperty;
        this.sourceElement = sourceElement;
        this.targetProperty = targetProperty;
        this.targetElement = targetElement;
    }

    public ElementMapping(String property, String element) {
        this.sourceProperty = property;
        this.sourceElement = element;
        this.targetProperty = property;
        this.targetElement = element;
    }

    public ElementMapping(INDIStandardElement element, String targetProperty, String targetElement) {
        this.sourceProperty = null;
        this.sourceElement = element.name();
        this.targetProperty = targetProperty;
        this.targetElement = targetElement;
    }

    public ElementMapping(INDIStandardElement element) {
        this.sourceProperty = null;
        this.sourceElement = element.name();
        this.targetProperty = null;
        this.targetElement = element.name();
    }
}

/*
 *  This file is part of INDI for Java Driver.
 * 
 *  INDI for Java Driver is free software: you can redistribute it
 *  and/or modify it under the terms of the GNU General Public License 
 *  as published by the Free Software Foundation, either version 3 of 
 *  the License, or (at your option) any later version.
 * 
 *  INDI for Java Driver is distributed in the hope that it will be
 *  useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 *  of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 * 
 *  You should have received a copy of the GNU General Public License
 *  along with INDI for Java Driver.  If not, see 
 *  <http://www.gnu.org/licenses/>.
 */
package org.indilib.i4j.driver;

/**
 * A interface representing a pair of a INDIElement and a value for the Element.
 *
 * @author S. Alonso (Zerjillo) [zerjioi at ugr.es]
 * @version 1.10, March 19, 2012
 */
public class INDIElementAndValue<Element extends INDIElement, Type> {

    /**
     * The Number element
     */
    private final Element element;

    /**
     * The Number value
     */
    private final Type value;

    /**
     * Constructs an instance of a <code>INDINumberElementAndValue</code>. This
     * class should not usually be instantiated by specific Drivers.
     *
     * @param element
     *            The Number Element
     * @param value
     *            The number
     */
    public INDIElementAndValue(Element element, Type value) {
        this.element = element;
        this.value = value;
    }

    public Element getElement() {
        return element;
    }

    public Type getValue() {
        return value;
    }
}

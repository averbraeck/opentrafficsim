/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2005, by Object Refinery Limited and Contributors.
 * 
 * Project Info:  http://www.jfree.org/jcommon/index.html
 *
 * This library is free software; you can redistribute it and/or modify it 
 * under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 2.1 of the License, or 
 * (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful, but 
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public 
 * License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, 
 * USA.  
 *
 * [Java is a trademark or registered trademark of Sun Microsystems, Inc. 
 * in the United States and other countries.]
 * 
 * ----------------------------
 * IntegerAttributeHandler.java
 * ----------------------------
 * (C)opyright 2003, 2004, by Thomas Morgner and Contributors.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: IntegerAttributeHandler.java,v 1.2 2005/10/18 13:30:16 mungady Exp $
 *
 * Changes
 * -------
 * 24.09.2003 : Initial version
 *
 */

package org.jfree.xml.attributehandlers;

import org.jfree.util.Log;

/**
 * A class that handles the conversion of {@link Integer} attributes to and from an appropriate
 * {@link String} representation.
 */
public class IntegerAttributeHandler implements AttributeHandler {

    /**
     * Creates a new attribute handler.
     */
    public IntegerAttributeHandler() {
        super();
    }

    /**
     * Converts the attribute to a string.
     *
     * @param o  the attribute ({@link Integer} expected).
     * 
     * @return A string representing the integer value.
     */
    public String toAttributeValue(final Object o) {
        try {
            final Integer in = (Integer) o;
            return in.toString();
        }
        catch (ClassCastException cce) {
            if (o != null) {
                Log.debug("ClassCastException: Expected Integer, found " + o.getClass());
            }
            throw cce;
        }
    }

    /**
     * Converts a string to a {@link Integer}.
     *
     * @param s  the string.
     *
     * @return a {@link Integer}.
     */
    public Object toPropertyValue(final String s) {
        return new Integer(s);
    }
}

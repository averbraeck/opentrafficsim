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
 * -----------------------
 * MemoryUsageMessage.java
 * -----------------------
 * (C) Copyright 2004, by Object Refinery Limited.
 *
 * Original Author:  Thomas Morgner;
 * Contributor(s):   David Gilbert (for Object Refinery Limited);
 *
 * $Id: MemoryUsageMessage.java,v 1.3 2005/10/18 13:14:33 mungady Exp $
 *
 * Changes
 * -------
 * 15-Jul-2004 : Added JCommon header (DG);
 *
 */

package org.jfree.base.log;

/**
 * A helper class to print memory usage message if needed.
 *
 * @author Thomas Morgner
 */
public class MemoryUsageMessage {

    /** The message. */
    private final String message;

    /**
     * Creates a new message.
     *
     * @param message  the message.
     */
    public MemoryUsageMessage(final String message) {
        this.message = message;
    }

    /**
     * Returns a string representation of the message (useful for debugging).
     *
     * @return the string.
     */
    public String toString() {
        return this.message + "Free: " + Runtime.getRuntime().freeMemory() + "; "
            + "Total: " + Runtime.getRuntime().totalMemory();
    }
    
}

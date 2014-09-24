/* ========================================================================
 * JCommon : a free general purpose class library for the Java(tm) platform
 * ========================================================================
 *
 * (C) Copyright 2000-2011, by Object Refinery Limited and Contributors.
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
 * ---------------------
 * JCommonResources.java
 * ---------------------
 * (C) Copyright 2002-2011, by Object Refinery Limited.
 *
 * Original Author:  David Gilbert (for Object Refinery Limited);
 * Contributor(s):   -;
 *
 * $Id: JCommonResources.java,v 1.23 2011/10/17 21:26:05 mungady Exp $
 *
 */

package org.jfree.resources;

import java.util.ListResourceBundle;

/**
 * Localised resources for the JCommon Class Library.
 */
public class JCommonResources extends ListResourceBundle {

    /**
     * Default constructor.
     */
    public JCommonResources() {
        // nothing to do
    }

    /**
     * Returns the array of strings in the resource bundle.
     *
     * @return The resources.
     */
    public Object[][] getContents() {
        return CONTENTS;
    }

    /** The resources to be localised. */
    private static final Object[][] CONTENTS = {

        {"project.name",      "JCommon"},
        {"project.version",   "1.0.17"},
        {"project.info",      "http://www.jfree.org/jcommon/"},
        {"project.copyright",
            "(C)opyright 2000-2011, by Object Refinery Limited and Contributors"}

    };

}

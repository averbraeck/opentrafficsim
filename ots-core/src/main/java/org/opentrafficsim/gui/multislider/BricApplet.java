/*
 * @(#)BricApplet.java
 *
 * $Date: 2014-03-13 03:15:48 -0500 (Thu, 13 Mar 2014) $
 *
 * Copyright (c) 2013 by Jeremy Wood.
 * All rights reserved.
 *
 * The copyright of this software is owned by Jeremy Wood. 
 * You may not use, copy or modify this software, except in  
 * accordance with the license agreement you entered into with  
 * Jeremy Wood. For details see accompanying license terms.
 * 
 * This software is probably, but not necessarily, discussed here:
 * https://javagraphics.java.net/
 * 
 * That site should also contain the most recent official version
 * of this software.  (See the SVN repository for more details.)
 */
package org.opentrafficsim.gui.multislider;

import javax.swing.JApplet;

/**
 * A JApplet with bug fixes/improvements built-in. Currently the only improvement is: this activates the
 * <code>AppletPopupFactory</code>.
 */
public class BricApplet extends JApplet
{
    private static final long serialVersionUID = 1L;

    public BricApplet()
    {
        AppletPopupFactory.initialize();
    }
}

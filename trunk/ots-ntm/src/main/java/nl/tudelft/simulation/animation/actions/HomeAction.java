/*
 * @(#) PanUpAction.java Oct 29, 2003 Copyright (c) 2002-2005 Delft University
 * of Technology Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved. This software is proprietary information of Delft University of
 * Technology 
 */
package nl.tudelft.simulation.animation.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;

import nl.tudelft.simulation.animation.GridPanel;
import nl.tudelft.simulation.language.io.URLResource;

/**
 * @author peter
 */
public class HomeAction extends AbstractAction
{
    /** */
    private static final long serialVersionUID = 20140909L;
    
    /** target of the gridpanel */
    private GridPanel target = null;

    /**
     * constructs a new ZoomIn
     * 
     * @param target the target
     */
    public HomeAction(final GridPanel target)
    {
        super("Home");
        this.target = target;
        this.putValue(Action.SMALL_ICON, new ImageIcon(URLResource.getResource("/toolbarButtonGraphics/navigation/Home16.gif")));
        this.setEnabled(true);
    }

    /**
     * @see java.awt.event.ActionListener #actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent actionEvent)
    {
        this.target.home();
        this.target.requestFocus();
    }
}
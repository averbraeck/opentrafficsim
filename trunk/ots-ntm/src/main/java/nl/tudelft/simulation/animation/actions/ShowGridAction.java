/*
 * @(#) ShowGridAction.java Oct 29, 2003 Copyright (c) 2002-2005 Delft
 * University of Technology Jaffalaan 5, 2628 BX Delft, the Netherlands. All
 * rights reserved. This software is proprietary information of Delft University
 * of Technology 
 */
package nl.tudelft.simulation.animation.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import nl.tudelft.simulation.animation.GridPanel;

/**
 * @author peter
 */
public class ShowGridAction extends AbstractAction
{
    /** */
    private static final long serialVersionUID = 20140909L;
    
    /** target of the gridpanel */
    private GridPanel target = null;

    /**
     * constructs a new AddRowAction
     * 
     * @param target the target
     */
    public ShowGridAction(final GridPanel target)
    {
        super("ShowGrid");
        this.target = target;
        this.setEnabled(true);
    }

    /**
     * @see java.awt.event.ActionListener #actionPerformed(java.awt.event.ActionEvent)
     */
    public void actionPerformed(final ActionEvent actionEvent)
    {
        this.target.showGrid(!this.target.isShowGrid());
        this.target.requestFocus();
    }
}
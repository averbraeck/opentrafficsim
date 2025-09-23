package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Listener for selecting choice options.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChoiceListener implements ActionListener
{

    /** Option. */
    private XsdTreeNode option;

    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param option possibly selected option.
     * @param editor editor.
     */
    public ChoiceListener(final XsdTreeNode option, final OtsEditor editor)
    {
        this.option = option;
        this.editor = editor;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        this.editor.getNodeActions().setOption(this.option);
    }

}

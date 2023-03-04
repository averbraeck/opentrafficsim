package org.opentrafficsim.swing.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

import org.opentrafficsim.draw.graphs.AbstractSpaceTimePlot;

/**
 * Embed a SpaceTimePlot in a Swing JPanel.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SwingSpaceTimePlot extends SwingPlot
{

    /**  */
    private static final long serialVersionUID = 20190823L;

    /**
     * Construct a new Swing container for SpaceTimePlot.
     * @param plot SpaceTimePlot; the plot to embed
     */
    public SwingSpaceTimePlot(final AbstractSpaceTimePlot plot)
    {
        super(plot);
    }

    /** {@inheritDoc} */
    @Override
    protected void addPopUpMenuItems(final JPopupMenu popupMenu)
    {
        JCheckBoxMenuItem fixedDomainCheckBox = new JCheckBoxMenuItem("Fixed time range", false);
        fixedDomainCheckBox.addActionListener(new ActionListener()
        {
            /** {@inheritDoc} */
            @Override
            public void actionPerformed(final ActionEvent e)
            {
                boolean fixed = ((JCheckBoxMenuItem) e.getSource()).isSelected();
                getPlot().updateFixedDomainRange(fixed);
            }
        });
        popupMenu.insert(fixedDomainCheckBox, 0);
        popupMenu.insert(new JPopupMenu.Separator(), 1);
    }

    /** {@inheritDoc} */
    @Override
    public AbstractSpaceTimePlot getPlot()
    {
        return (AbstractSpaceTimePlot) super.getPlot();
    }

}

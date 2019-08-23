package org.opentrafficsim.draw.graphs;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JCheckBoxMenuItem;
import javax.swing.JPopupMenu;

public class SwingSpaceTimePlot extends SwingPlot
{

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
            @SuppressWarnings("synthetic-access")
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

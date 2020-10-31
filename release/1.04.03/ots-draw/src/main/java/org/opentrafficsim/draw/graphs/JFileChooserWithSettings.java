package org.opentrafficsim.draw.graphs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;

import javax.swing.JFileChooser;
import javax.swing.JPanel;

/**
 * Small helper class that adds some components to a JFileChooser, the contents of which can be used to retrieve user settings
 * for output.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 14 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class JFileChooserWithSettings extends JFileChooser
{
    /** */
    private static final long serialVersionUID = 20181014L;

    /**
     * Constructor that adds components to the left of the 'Save' and 'Cancel' button.
     * @param components Component...; Components... components to add
     */
    public JFileChooserWithSettings(final Component... components)
    {
        JPanel saveCancelPanel = (JPanel) ((JPanel) this.getComponent(3)).getComponent(3);
        // insert leftVsRight panel in original place of saveCancelPanel
        JPanel leftVsRightPanel = new JPanel();
        leftVsRightPanel.setLayout(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        saveCancelPanel.getParent().add(leftVsRightPanel);
        // saveCancelPanel goes in there to the right
        leftVsRightPanel.add(saveCancelPanel);
        // to the left a panel that places it's contents to the south
        JPanel allignSouthPanel = new JPanel();
        allignSouthPanel.setPreferredSize(new Dimension(300, 50));
        leftVsRightPanel.add(allignSouthPanel, 0);
        // in that left area to the south, a panel for the setting components right to left
        JPanel settingsPanel = new JPanel();
        allignSouthPanel.setLayout(new BorderLayout());
        allignSouthPanel.add(settingsPanel, BorderLayout.SOUTH);
        settingsPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        // add components in that final panel
        int index = 0;
        for (Component component : components)
        {
            settingsPanel.add(component, index);
            index++;
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "JFileChooserWithSettings []";
    }

}

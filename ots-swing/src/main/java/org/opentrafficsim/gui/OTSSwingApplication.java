package org.opentrafficsim.gui;

import java.awt.Frame;
import java.util.Properties;

import javax.swing.JFrame;

import org.opentrafficsim.core.dsol.OTSModelInterface;

import nl.tudelft.simulation.dsol.swing.gui.DSOLPanel;

/**
 * OTSSwingApplication.java. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public abstract class OTSSwingApplication extends JFrame
{
    /** */
    private static final long serialVersionUID = 1L;

    /** Properties for the frame appearance (not simulation related). */
    private Properties frameProperties;

    /** Use EXIT_ON_CLOSE when true, DISPOSE_ON_CLOSE when false on closing of the window. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private boolean exitOnClose;

    /** The tabbed panel so other tabs can be added by the classes that extend this class. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    private OTSAnimationPanel panel;

    /** The model. */
    private OTSModelInterface model;

    /** Current appearance. */
    private Appearance appearance = Appearance.GRAY;

    /**
     * Constructor for DSOLApplication.
     * @param title String; the title in the top bar
     * @param panel DSOLPanel; the panel to put in the frame
     */
    public OTSSwingApplication(final String title, final OTSAnimationPanel panel)
    {
        super(title);
        this.panel = panel;
        this.setContentPane(panel);
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.pack();
        this.setExtendedState(Frame.MAXIMIZED_BOTH);
        this.setVisible(true);
    }

}


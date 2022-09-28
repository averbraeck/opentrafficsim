package org.opentrafficsim.swing.gui;

import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;

/**
 * Extension of a swing application with standard preparation.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 jan. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> model type
 */
public class OTSSimulationApplication<T extends OTSModelInterface> extends OTSSwingApplication<T>
{

    /** */
    private static final long serialVersionUID = 20190118L;

    /** Animation panel. */
    private final OTSAnimationPanel animationPanel;

    /**
     * @param model T; model
     * @param panel OTSAnimationPanel; animation panel
     * @throws OTSDrawingException on animation error
     */
    public OTSSimulationApplication(final T model, final OTSAnimationPanel panel) throws OTSDrawingException
    {
        super(model, panel);
        this.animationPanel = panel;
        animateNetwork();
        setAnimationToggles();
        addTabs();
        setAppearance(getAppearance()); // update appearance of added objects
    }

    /**
     * Creates the animation objects. This method is overridable. The default uses {@code DefaultAnimationFactory}.
     * @throws OTSDrawingException on animation error
     */
    protected void animateNetwork() throws OTSDrawingException
    {
        DefaultAnimationFactory.animateNetwork(getModel().getNetwork(), getModel().getNetwork().getSimulator(),
                getAnimationPanel().getGTUColorer());
    }

    /**
     * Set animation toggles. This method is overridable. The default sets standard text toggles.
     */
    protected void setAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(getAnimationPanel());
    }

    /**
     * Adds tabs. This method is overridable. The default does nothing.
     */
    protected void addTabs()
    {
        //
    }

    /**
     * Returns the animation panel.
     * @return OTSAnimationPanel; animation panel
     */
    public OTSAnimationPanel getAnimationPanel()
    {
        return this.animationPanel;
    }

}

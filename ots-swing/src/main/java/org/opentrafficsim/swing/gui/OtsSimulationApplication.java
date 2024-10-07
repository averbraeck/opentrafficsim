package org.opentrafficsim.swing.gui;

import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.draw.OtsDrawingException;

/**
 * Extension of a swing application with standard preparation.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> model type
 */
public class OtsSimulationApplication<T extends OtsModelInterface> extends OtsSwingApplication<T>
{

    /** */
    private static final long serialVersionUID = 20190118L;

    /** Animation panel. */
    private final OtsAnimationPanel animationPanel;

    /**
     * @param model model
     * @param panel animation panel
     * @throws OtsDrawingException on animation error
     */
    public OtsSimulationApplication(final T model, final OtsAnimationPanel panel) throws OtsDrawingException
    {
        super(model, panel);
        this.animationPanel = panel;
        setAnimationToggles();
        animateNetwork();
        addTabs();
        setAppearance(getAppearance()); // update appearance of added objects
    }

    /**
     * Creates the animation objects. This method is overridable. The default uses {@code DefaultAnimationFactory}.
     * @throws OtsDrawingException on animation error
     */
    private void animateNetwork() throws OtsDrawingException
    {
        DefaultAnimationFactory.animateNetwork(getModel().getNetwork(), getModel().getNetwork().getSimulator(),
                getAnimationPanel().getGtuColorer());
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
     * @return animation panel
     */
    public OtsAnimationPanel getAnimationPanel()
    {
        return this.animationPanel;
    }

}

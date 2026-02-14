package org.opentrafficsim.swing.gui;

import java.util.Map;

import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;

/**
 * Extension of a swing application with standard preparation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** GTU type markers. */
    private final Map<GtuType, GtuMarker> markers;

    /**
     * Constructor.
     * @param model model
     * @param panel animation panel
     * @param markers GTU type markers
     */
    public OtsSimulationApplication(final T model, final OtsAnimationPanel panel, final Map<GtuType, GtuMarker> markers)
    {
        super(model, panel);
        this.animationPanel = panel;
        this.markers = markers;
        setAnimationToggles();
        animateNetwork();
        addTabs();
        setAppearance(getAppearance()); // update appearance of added objects
    }

    /**
     * Creates the animation objects. This method is overridable. The default uses {@code DefaultAnimationFactory}.
     */
    private void animateNetwork()
    {
        DefaultAnimationFactory.animateNetwork(getModel().getNetwork(), getModel().getNetwork().getSimulator(),
                this.animationPanel.getColorControlPanel().getGtuColorerManager(), this.markers);
    }

    /**
     * Returns the GTU type markers.
     * @return GTU type markers
     */
    protected Map<GtuType, GtuMarker> getMarkers()
    {
        return this.markers;
    }

    /**
     * Set animation toggles. This method is overridable. The default sets standard text toggles.
     */
    protected void setAnimationToggles()
    {
        AnimationToggles.setIconAnimationTogglesStandard(getAnimationPanel());
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

package org.opentrafficsim.swing.gui;

import java.util.Map;

import org.opentrafficsim.animation.DefaultAnimationFactory;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData.GtuMarker;

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

    /** The switchableGtuColorer used to color the GTUs. */
    private final GtuColorer gtuColorer;

    /** GTU type markers. */
    private final Map<GtuType, GtuMarker> markers;

    /**
     * @param model model
     * @param panel animation panel
     * @param gtuColorer GTU colorer
     * @param markers GTU type markers
     */
    public OtsSimulationApplication(final T model, final OtsAnimationPanel panel, final GtuColorer gtuColorer,
            final Map<GtuType, GtuMarker> markers)
    {
        super(model, panel);
        this.animationPanel = panel;
        this.gtuColorer = gtuColorer;
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
        DefaultAnimationFactory.animateNetwork(getModel().getNetwork(), getModel().getNetwork().getSimulator(), this.gtuColorer,
                this.markers);
    }

    /**
     * Returns the GTU colorer.
     * @return GTU colorer
     */
    protected GtuColorer getGtuColorer()
    {
        return this.gtuColorer;
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

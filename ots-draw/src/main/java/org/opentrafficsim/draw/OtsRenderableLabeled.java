package org.opentrafficsim.draw;

import javax.naming.OperationNotSupportedException;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.geometry.OtsShape;

import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Renderable with label.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @param <L> source type
 * @param <T> text renderable type
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class OtsRenderableLabeled<L extends OtsShape, T extends RenderableTextSource<L, T>> extends OtsRenderable<L>
{

    /** Text object to destroy when the animation is destroyed. */
    private T text;

    /**
     * Constructor. This constructor uses no label prefix.
     * @param source the source
     * @param contextualized the object that can provide the context to store the animation objects
     */
    public OtsRenderableLabeled(final L source, final Contextualized contextualized)
    {
        this(source, contextualized, "");
    }

    /**
     * Constructor.
     * @param source the source
     * @param contextualized the object that can provide the context to store the animation objects
     * @param prefix prefix for the label
     */
    public OtsRenderableLabeled(final L source, final Contextualized contextualized, final String prefix)
    {
        super(source, contextualized);
        this.text = createText(source, contextualized, prefix);
    }

    /**
     * Sets the animation as dynamic, obtaining geometry at each draw.
     * @param dynamic whether the animation is dynamic, {@code false} by default
     * @return this animation for method chaining
     */
    @SuppressWarnings("unchecked")
    @Override
    public OtsRenderableLabeled<L, T> setDynamic(final boolean dynamic)
    {
        if (this.text != null)
        {
            this.text.setDynamic(dynamic);
        }
        return (OtsRenderableLabeled<L, T>) super.setDynamic(dynamic);
    }

    @Override
    public void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        if (this.text != null)
        {
            this.text.destroy(contextProvider);
        }
    }

    /**
     * Returns a text object that is co-animated with the object itself. Sub-classes of {@link OtsRenderableLabeled} typically
     * define a {@code .Text} class that extends {@link RenderableTextSource} such that a separate class exists that can be
     * toggled on/off. In this way labels of renderables can be show separately from the object itself. The {@code prefix} may
     * be ignore depending on the sub-class knowing it provided no prefix to this class, its super class.
     * @param source the object for which the text is displayed
     * @param contextualized context provider
     * @param prefix prefix for the label
     * @return text renderable, may be {@code null} to omit a label
     */
    protected abstract T createText(L source, Contextualized contextualized, String prefix);

    /**
     * Convenience class to use when the renderable wants to omit having a label. The {@link #createText} should return
     * {@code null}.
     * @param <L> source type
     */
    public static class NoText<L extends OtsShape> extends RenderableTextSource<L, NoText<L>>
    {

        /**
         * Constructor.
         * @throws OperationNotSupportedException whenever this constructor is called
         */
        NoText() throws OperationNotSupportedException
        {
            super(Throw.when(null, true, OperationNotSupportedException.class,
                    "Method createText should return null when NoText is used."), null, 0.0f, 0.0f, null, null, null, null);
        }

    }

}

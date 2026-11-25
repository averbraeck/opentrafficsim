package org.opentrafficsim.editor.extensions.map;

import java.util.function.Supplier;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generic implementation to listen to any changes under a node, including its own attributes and activation status. The node
 * represents some logical data in which any change requires an update. For example, this can be used to listen to any change in
 * a road layout, causing all links that use the layout to be redrawn.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <T> type of data that listeners require from what the main node represents.
 */
public abstract class DataListener<T> extends ChangeListener
{

    /** Expression evaluator supplier. */
    private final Supplier<Eval> eval;

    /** Flag when change occurred and offsets need to be recalculated. */
    private boolean dataIsDirty = true;

    /** Cached data. */
    private T data;

    /**
     * Constructor.
     * @param node node of the layout, either in definitions or under a link.
     * @param eval supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public DataListener(final XsdTreeNode node, final Supplier<Eval> eval)
    {
        super(node);
        this.eval = eval;
    }

    /**
     * Remove all listeners.
     */
    @Override
    public void destroy()
    {
        super.destroy();
        destroyData();
    }

    /**
     * Destroys the data. Can be overridden by subclasses. By default does nothing.
     */
    protected void destroyData()
    {
        //
    }

    @Override
    public void notify(final Event event)
    {
        if (listen(event))
        {
            this.dataIsDirty = true;
            fireEvent(CHANGE_EVENT, getNode());
        }
    }

    /**
     * Returns a current valid evaluator.
     * @return evaluator.
     */
    public Eval getEval()
    {
        return this.eval.get();
    }

    /**
     * Returns the data that the node of this listener represents.
     * @return data that the node of this listener represents.
     */
    public final T getData()
    {
        if (this.dataIsDirty)
        {
            this.data = calculateData();
            this.dataIsDirty = false;
        }
        return this.data;
    }

    /**
     * Calculates the data. This is called when data is required by a listener and the there have been changes.
     * @return calculated data based on content of the main node.
     */
    abstract T calculateData();

    @Override
    public void evalChanged()
    {
        this.dataIsDirty = true;
        super.evalChanged();
    }

}

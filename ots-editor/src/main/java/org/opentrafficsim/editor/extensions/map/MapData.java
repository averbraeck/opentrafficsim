package org.opentrafficsim.editor.extensions.map;

import java.util.function.Consumer;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.metadata.MetaData;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;

/**
 * Part of the map data structure.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public abstract class MapData extends LocalEventProducer implements EvalListener, OtsShape
{

    /** Event for a change in data that entails a change in visualization. */
    public static final EventType MAP_DATA_CHANGED =
            new EventType("MAP_DATA_CHANGED", new MetaData("MAP_DATA_CHANGED", "Data was changed that affects visualization."));

    /** Map showing all the elements. */
    private final EditorMap map;

    /** Tree node. */
    private final XsdTreeNode node;

    /** Editor. */
    private final OtsEditor editor;

    /** Whether this data is valid. */
    private boolean valid;

    /**
     * Constructor.
     * @param map map.
     * @param node tree node.
     * @param editor editor.
     */
    public MapData(final EditorMap map, final XsdTreeNode node, final OtsEditor editor)
    {
        this.map = map;
        this.node = node;
        this.editor = editor;
        this.editor.addEvalListener(this);
    }

    /**
     * Returns the tree node.
     * @return tree node.
     */
    public XsdTreeNode getNode()
    {
        return this.node;
    }

    /**
     * Returns the evaluator for expressions.
     * @return evaluator for expressions.
     */
    public Eval getEval()
    {
        return this.editor.getEval();
    }

    /**
     * Returns the editor.
     * @return editor
     */
    public OtsEditor getEditor()
    {
        return this.editor;
    }

    /**
     * Returns the undo.
     * @return undo
     */
    public Undo getUndo()
    {
        return this.editor.getUndo();
    }

    /**
     * Updates the animation as underlying data that might be relevant has changed.
     */
    abstract void update();

    @Override
    public final void evalChanged()
    {
        update();
    }

    /**
     * Destroy this data object, e.g. remove self as listener. Override and call super if subclasses remove their own listeners.
     * Using weak references for listeners is another option to deal with obsolete listening.
     */
    public void destroy()
    {
        this.editor.removeEvalListener(this);
    }

    /**
     * Notify the map that the element can be drawn.
     */
    void setValid()
    {
        this.valid = true;
        this.map.setValid(this);
        fireEvent(new Event(MAP_DATA_CHANGED, null));
    }

    /**
     * Notify the map that the element cannot be drawn.
     */
    void setInvalid()
    {
        this.valid = false;
        this.map.setInvalid(this);
        fireEvent(new Event(MAP_DATA_CHANGED, null));
    }

    /**
     * Returns whether this data is valid.
     * @return whether this data is valid
     */
    public boolean isValid()
    {
        return this.valid;
    }

    /**
     * Returns the map.
     * @return map.
     */
    // If public, this will remove the map from the tabbed pane when the inspector wants a renderer for this element, which is
    // the panel itself. It gets 'reparented' towards the cell in the inspection window.
    protected EditorMap getMap()
    {
        return this.map;
    }

    /**
     * Generic method to execute based on a (changed) attribute. If the value is {@code null}, the execution is invoked with a
     * {@code null} value. If the value is an invalid expression, or the value cannot be unmarshalled by the adapter, the
     * execution is not invoked. Typical usage is:
     *
     * <pre>
     * executeForAttribute((v) -> this.offsetStart = v, node, "OffsetStart", Adapters.get(Length.class));
     * </pre>
     *
     * @param <T> type of the value to set
     * @param execution setter that receives a successfully derived value to set locally
     * @param treeNode node that has the attribute or value, will often be {@link #getNode}
     * @param attribute name of the attribute, or {@code null} to read node value
     * @param adapter adapter
     */
    protected <T> void executeForAttribute(final Consumer<T> execution, final XsdTreeNode treeNode, final String attribute,
            final ExpressionAdapter<T, ?> adapter)
    {
        execute(execution, treeNode, attribute, true, adapter);
    }

    /**
     * Generic method to execute based on a (changed) attribute. If the value is {@code null}, the value is an invalid
     * expression, or the value cannot be unmarshalled by the adapter, the execution is not invoked. Typical usage is:
     *
     * <pre>
     * executeForNonNullAttribute((v) -> offsets.add(v), node, "OffsetStart", Adapters.get(Length.class));
     * </pre>
     *
     * @param <T> type of the value to set
     * @param execution setter that receives a successfully derived value to set locally
     * @param treeNode node that has the attribute or value, will often be {@link #getNode}
     * @param attribute name of the attribute, or {@code null} to read node value
     * @param adapter adapter
     */
    protected <T> void executeForNonNullAttribute(final Consumer<T> execution, final XsdTreeNode treeNode,
            final String attribute, final ExpressionAdapter<T, ?> adapter)
    {
        execute(execution, treeNode, attribute, false, adapter);
    }

    /**
     * Generic method to execute based on a (changed) node value. If the value is {@code null}, the execution is invoked with a
     * {@code null} value. If the value is an invalid expression, or the value cannot be unmarshalled by the adapter, the
     * execution is not invoked. Typical usage is:
     *
     * <pre>
     * executeValue((v) -> this.width = v, node, Adapters.get(Length.class));
     * </pre>
     *
     * @param <T> type of the value to set
     * @param execution setter that receives a successfully derived value to set locally
     * @param treeNode node that has the attribute or value, will often be {@link #getNode}
     * @param adapter adapter
     */
    protected <T> void executeForValue(final Consumer<T> execution, final XsdTreeNode treeNode,
            final ExpressionAdapter<T, ?> adapter)
    {
        execute(execution, treeNode, null, true, adapter);
    }

    /**
     * Generic method to execute based on a (changed) node value. If the value is {@code null}, the execution is invoked with a
     * {@code null} value. If the value is an invalid expression, or the value cannot be unmarshalled by the adapter, the
     * execution is not invoked. Typical usage is:
     *
     * <pre>
     * executeForNonNullValue((v) -> width.add(v), node, Adapters.get(Length.class));
     * </pre>
     *
     * @param <T> type of the value to set
     * @param execution setter that receives a successfully derived value to set locally
     * @param treeNode node that has the attribute or value, will often be {@link #getNode}
     * @param adapter adapter
     */
    protected <T> void executeForNonNullValue(final Consumer<T> execution, final XsdTreeNode treeNode,
            final ExpressionAdapter<T, ?> adapter)
    {
        execute(execution, treeNode, null, false, adapter);
    }

    /**
     * Generic method to execute based on a (changed) attribute or value. If the value is {@code null}, the execution is invoked
     * with a {@code null} value if {@code null} is allowed. If the value is an invalid expression, or the value cannot be
     * unmarshalled by the adapter, the execution is not invoked.
     * @param <T> type of the value to set
     * @param execution setter that receives a successfully derived value to set locally
     * @param treeNode node that has the attribute or value, will often be {@link #getNode}
     * @param attribute name of the attribute, or {@code null} to read node value
     * @param acceptNull whether to accept null
     * @param adapter adapter
     */
    private <T> void execute(final Consumer<T> execution, final XsdTreeNode treeNode, final String attribute,
            final boolean acceptNull, final ExpressionAdapter<T, ?> adapter)
    {
        try
        {
            String stringValue =
                    attribute == null ? treeNode.getValueOrDefault() : treeNode.getAttributeValueOrDefault(attribute);
            if (stringValue == null)
            {
                if (acceptNull)
                {
                    execution.accept(null);
                }
                return;
            }
            T value = adapter.unmarshal(stringValue).get(getEval());
            execution.accept(value);
        }
        catch (IllegalArgumentException ex)
        {
            setInvalid();
            // IllegalArgumentException: invalid value, keep old value for now
        }
        catch (Exception ex)
        {
            throw new OtsRuntimeException("Unexpected exception", ex);
        }
    }

}

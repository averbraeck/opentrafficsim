package org.opentrafficsim.editor.extensions.map;

import java.util.LinkedHashMap;
import java.util.function.Consumer;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djutils.draw.point.Point2d;
import org.djutils.eval.Eval;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.xml.bindings.ArcDirectionAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.DirectionAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.ExpressionAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.LinearDensityAdapter;
import org.opentrafficsim.xml.bindings.Point2dAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.StripeTypeAdapter;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;
import org.opentrafficsim.xml.bindings.types.ExpressionType;

/**
 * Part of the map data structure.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public abstract class MapData implements EvalListener
{

    /** Map showing all the elements. */
    private final Map map;

    /** Tree node. */
    private final XsdTreeNode node;

    /** Editor. */
    private final OtsEditor editor;

    /** Map of adapters per output type. */
    private final static java.util.Map<Class<?>, ExpressionAdapter<?, ?>> ADAPTERS = new LinkedHashMap<>();

    static
    {
        ADAPTERS.put(String.class, new StringAdapter());
        ADAPTERS.put(Point2d.class, new Point2dAdapter());
        ADAPTERS.put(Direction.class, new DirectionAdapter());
        ADAPTERS.put(Length.class, new LengthAdapter());
        ADAPTERS.put(LinearDensity.class, new LinearDensityAdapter());
        ADAPTERS.put(ArcDirection.class, new ArcDirectionAdapter());
        ADAPTERS.put(Double.class, new DoubleAdapter());
        ADAPTERS.put(Boolean.class, new BooleanAdapter());
        ADAPTERS.put(Stripe.Type.class, new StripeTypeAdapter());
    }

    /**
     * Constructor.
     * @param map Map; map.
     * @param node XsdTreeNode; tree node.
     * @param editor OtsEditor; editor.
     */
    public MapData(final Map map, final XsdTreeNode node, final OtsEditor editor)
    {
        this.map = map;
        this.node = node;
        this.editor = editor;
        this.editor.addEvalListener(this);
    }

    /**
     * Returns the tree node.
     * @return XsdTreeNode; tree node.
     */
    public XsdTreeNode getNode()
    {
        return this.node;
    }

    /**
     * Returns the evaluator for expressions.
     * @return Eval; evaluator for expressions.
     */
    public Eval getEval()
    {
        return this.editor.getEval();
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
        this.map.setValid(this);
    }

    /**
     * Notify the map that the element cannot be drawn.
     */
    void setInvalid()
    {
        this.map.setInvalid(this);
    }

    /**
     * Returns the map.
     * @return Map; map.
     */
    public Map getMap()
    {
        return this.map;
    }

    /**
     * Returns an adapter for the given class. Adapters are only provided for known classes. This is to limit the number of
     * adapters in memory, as these are stateless.
     * @param <T> output type of the adapter.
     * @param <E> expression type of the adapter.
     * @param clazz Class&lt;T&gt;; class of the output type of the adapter.
     * @return ExpressionAdapter&lt;T, ?&gt;; adapter.
     */
    @SuppressWarnings("unchecked")
    protected <T, E extends ExpressionType<T>> ExpressionAdapter<T, E> getAdapter(final Class<T> clazz)
    {
        Throw.when(!ADAPTERS.containsKey(clazz), RuntimeException.class,
                "No adapter for class %s available. Add it in the static code block of MapData or create one directly.");
        return (ExpressionAdapter<T, E>) ADAPTERS.get(clazz);
    }

    /**
     * Generic method to set a value based on a changed attribute. If the new value is {@code null}, the setter is invoked to
     * set a {@code null} value. If the value is an invalid expression, or the value cannot be unmarshalled by the adapter, no
     * change is made.
     * @param <T> type of the value to set.
     * @param setter Consumer&lt;T&gt;; setter that receives a successfully derived value.
     * @param adapter ExpressionAdapter&lt;T, ?&gt;; adapter.
     * @param node XsdTreeNode; node that has the attribute, will often be {@code getNode()}.
     * @param attribute String; name of the attribute.
     */
    protected <T> void setValue(final Consumer<T> setter, final ExpressionAdapter<T, ?> adapter, final XsdTreeNode node,
            final String attribute)
    {
        try
        {
            String stringValue = node.getAttributeValue(attribute);
            if (stringValue == null)
            {
                setter.accept(null);
                return;
            }
            T value = adapter.unmarshal(stringValue).get(getEval());
            setter.accept(value);
        }
        catch (RuntimeException ex)
        {
            setInvalid();
            // RuntimeException: expression not valid => we should add expression validators baked in to XsdTreeNode
            // IllegalArgumentException: invalid coordinate value, keep old coordinate
        }
        catch (Exception ex)
        {
            throw new RuntimeException("Unexpected exception", ex);
        }
    }

}

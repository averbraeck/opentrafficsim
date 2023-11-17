package org.opentrafficsim.editor.extensions.map;

import java.util.function.Supplier;

import org.djutils.eval.Eval;
import org.opentrafficsim.core.geometry.Flattener;
import org.opentrafficsim.core.geometry.Flattener.MaxAngle;
import org.opentrafficsim.core.geometry.Flattener.MaxDeviation;
import org.opentrafficsim.core.geometry.Flattener.MaxDeviationAndAngle;
import org.opentrafficsim.core.geometry.Flattener.NumSegments;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.road.network.factory.xml.parser.ScenarioParser;
import org.opentrafficsim.xml.bindings.AngleAdapter;
import org.opentrafficsim.xml.bindings.IntegerAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;

/**
 * Listener for flattener nodes, either under the network or at a link (shape). This class can also calculate a flattener
 * representing the information under a flattener node.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FlattenerListener extends ChangeListener<Flattener>
{

    /** */
    private static final long serialVersionUID = 20231114L;

    /** Adapter for integer values. */
    private static final IntegerAdapter INTEGER_ADAPTER = new IntegerAdapter();

    /** Adapter for length values. */
    private static final LengthAdapter LENGTH_ADAPTER = new LengthAdapter();

    /** Adapter for angle values. */
    private static final AngleAdapter ANGLE_ADAPTER = new AngleAdapter();

    /**
     * Constructor.
     * @param flattenerNode XsdTreeNode; node of the flattener, either at the network or at a link.
     * @param eval Supplier&lt;Eval&gt;; supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public FlattenerListener(final XsdTreeNode flattenerNode, final Supplier<Eval> eval)
    {
        super(flattenerNode, eval);
    }

    /** {@inheritDoc} */
    @Override
    Flattener calculateData()
    {
        try
        {
            if (!getNode().isValid())
            {
                return null;
            }
            if (getNode().getChild(0).getNodeName().equals("NumSegments"))
            {
                return new NumSegments(INTEGER_ADAPTER.unmarshal(getNode().getChild(0).getValue()).get(getEval()));
            }
            if (getNode().getChild(0).getChild(0).isActive())
            {
                if (getNode().getChild(0).getChild(1).isActive())
                {
                    return new MaxDeviationAndAngle(getDeviation(getNode().getChild(0).getChild(0)),
                            getAngle(getNode().getChild(0).getChild(1)));
                }
                return new MaxDeviation(getDeviation(getNode().getChild(0).getChild(0)));
            }
            if (getNode().getChild(0).getChild(1).isActive())
            {
                return new MaxAngle(getAngle(getNode().getChild(0).getChild(1)));
            }
        }
        catch (RuntimeException rte)
        {
            if (rte.getMessage().contains("is neither a DoubleScalar nor a Boolean")
                    && ScenarioParser.lastLookedUp instanceof Integer)
            {
                // TODO: dirty trick to obtain a value that was given to Eval, which not yet supports non Boolean/DoubleScalar.
                return new NumSegments((int) ScenarioParser.lastLookedUp);
            }
        }
        catch (Exception ex)
        {
            // during loading, return null
        }
        return null; // no max deviation and no max angle defined, i.e. invalid
    }

    /**
     * Returns a safe deviation value (>=0.001).
     * @param node XsdTreeNode; deviation node.
     * @return double; safe deviation value.
     */
    private double getDeviation(final XsdTreeNode node)
    {
        double deviation = LENGTH_ADAPTER.unmarshal(node.getValue()).get(getEval()).si;
        return deviation < 0.001 ? 0.001 : deviation;
    }

    /**
     * Returns a safe angle value (>=0.01).
     * @param node XsdTreeNode; angle node.
     * @return double; safe angle value.
     */
    private double getAngle(final XsdTreeNode node)
    {
        double deviation = ANGLE_ADAPTER.unmarshal(node.getValue()).get(getEval()).si;
        return deviation < 0.001 ? 0.001 : deviation;
    }

}

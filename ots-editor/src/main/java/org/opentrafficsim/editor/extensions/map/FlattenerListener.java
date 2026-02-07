package org.opentrafficsim.editor.extensions.map;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.eval.Eval;
import org.opentrafficsim.core.geometry.CurveFlattener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.Adapters;

/**
 * Listener for flattener nodes, either under the network or at a link (shape). This class can also calculate a flattener
 * representing the information under a flattener node.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FlattenerListener extends ChangeListener<CurveFlattener>
{

    /**
     * Constructor.
     * @param flattenerNode node of the flattener, either at the network or at a link.
     * @param eval supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public FlattenerListener(final XsdTreeNode flattenerNode, final Supplier<Eval> eval)
    {
        super(flattenerNode, eval);
    }

    @Override
    CurveFlattener calculateData()
    {
        try
        {
            if (!getNode().isValid())
            {
                return null;
            }
            if (getNode().getChild(0).getNodeName().equals("NumSegments"))
            {
                return new CurveFlattener(Integer.valueOf(getNode().getChild(0).getValue()));
            }
            if (getNode().getChild(0).getChild(0).isActive())
            {
                if (getNode().getChild(0).getChild(1).isActive())
                {
                    return new CurveFlattener(getDeviation(getNode().getChild(0).getChild(0)),
                            getAngle(getNode().getChild(0).getChild(1)));
                }
                return new CurveFlattener(getDeviation(getNode().getChild(0).getChild(0)));
            }
            if (getNode().getChild(0).getChild(1).isActive())
            {
                return new CurveFlattener(Angle.ofSI(getAngle(getNode().getChild(0).getChild(1))));
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
     * @param node deviation node.
     * @return safe deviation value.
     */
    private double getDeviation(final XsdTreeNode node)
    {
        double deviation = Adapters.get(Length.class).unmarshal(node.getValue()).get(getEval()).si;
        return deviation < 0.001 ? 0.001 : deviation;
    }

    /**
     * Returns a safe angle value (>=0.01).
     * @param node angle node.
     * @return safe angle value.
     */
    private double getAngle(final XsdTreeNode node)
    {
        double deviation = Adapters.get(Angle.class).unmarshal(node.getValue()).get(getEval()).si;
        return deviation < 0.001 ? 0.001 : deviation;
    }

}

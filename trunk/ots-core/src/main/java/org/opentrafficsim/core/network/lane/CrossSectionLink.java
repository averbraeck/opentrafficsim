package org.opentrafficsim.core.network.lane;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <LINKID> the ID type of the Link, e.g., String or Integer.
 * @param <NODEID> the ID type of the Node, e.g., String or Integer.
 */
public class CrossSectionLink<LINKID, NODEID> extends OTSLink<LINKID, NODEID>
{
    /** list of cross-section elements. */
    private final List<CrossSectionElement<LINKID, NODEID>> crossSectionElementList = new ArrayList<>();

    /** */
    private static final long serialVersionUID = 20141015L;

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param designLine the OTSLine3D design line of the Link
     * @param capacity link capacity in vehicles per hour.
     */
    public CrossSectionLink(final LINKID id, final Node<NODEID> startNode, final Node<NODEID> endNode,
        final OTSLine3D designLine, final DoubleScalar.Abs<FrequencyUnit> capacity)
    {
        super(id, startNode, endNode, designLine, capacity);
    }

    /**
     * Construction of a link.
     * @param id the link id.
     * @param startNode start node (directional).
     * @param endNode end node (directional).
     * @param designLine the OTSLine3D design line of the Link
     */
    public CrossSectionLink(final LINKID id, final Node<NODEID> startNode, final Node<NODEID> endNode,
        final OTSLine3D designLine)
    {
        super(id, startNode, endNode, designLine);
    }

    /**
     * Add a cross section element at the end of the list. <br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement<LINKID, NODEID> cse)
    {
        this.crossSectionElementList.add(cse);
    }

    /**
     * Add a cross section element at specified index in the list.<br>
     * <b>Note:</b> LEFT is seen as a positive lateral direction, RIGHT as a negative lateral direction.
     * @param index the location to insert the element.
     * @param cse the cross section element to add.
     */
    protected final void addCrossSectionElement(final CrossSectionElement<LINKID, NODEID> cse, final int index)
    {
        this.crossSectionElementList.add(index, cse);
    }

    /**
     * @return crossSectionElementList.
     */
    public final List<CrossSectionElement<LINKID, NODEID>> getCrossSectionElementList()
    {
        return this.crossSectionElementList;
    }

    /**
     * String ID implementation of the Point link.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version an 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class STR extends CrossSectionLink<String, String>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * Construct a new link.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param designLine the OTSLine3D design line of the Link
         * @param capacity link capacity in GTUs per hour
         */
        public STR(final String id, final OTSNode.STR startNode, final OTSNode.STR endNode, final OTSLine3D designLine,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
        {
            super(id, startNode, endNode, designLine, capacity);
        }

        /**
         * Construct a new link with infinite capacity.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param designLine the OTSLine3D design line of the Link
         */
        public STR(final String id, final OTSNode.STR startNode, final OTSNode.STR endNode, final OTSLine3D designLine)
        {
            super(id, startNode, endNode, designLine);
        }
    }

    /**
     * Integer ID implementation of the Point link.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version an 4, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class INT extends CrossSectionLink<Integer, Integer>
    {
        /** */
        private static final long serialVersionUID = 20150104L;

        /**
         * Construct a new link.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param designLine the OTSLine3D design line of the Link
         * @param capacity link capacity in GTUs per hour
         */
        public INT(final int id, final OTSNode.INT startNode, final OTSNode.INT endNode, final OTSLine3D designLine,
            final DoubleScalar.Abs<FrequencyUnit> capacity)
        {
            super(id, startNode, endNode, designLine, capacity);
        }

        /**
         * Construct a new link with infinite capacity.
         * @param id the link id
         * @param startNode start node (directional)
         * @param endNode end node (directional)
         * @param designLine the OTSLine3D design line of the Link
         */
        public INT(final int id, final OTSNode.INT startNode, final OTSNode.INT endNode, final OTSLine3D designLine)
        {
            super(id, startNode, endNode, designLine);
        }
    }

}

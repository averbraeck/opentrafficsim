package org.opentrafficsim.road.network.factory.nwb;

import java.util.LinkedHashMap;
import java.util.Map;

import org.opengis.feature.Feature;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.network.LinkType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.lane.changing.LaneKeepingPolicy;

/**
 * Augmented cross section link object.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 15 may 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ExtendedCrossSectionLink extends CrossSectionLink
{
    /** ... */
    private static final long serialVersionUID = 20190515L;

    /** Features on which this extended cross section link is based. */
    private final Map<String, Feature> features = new LinkedHashMap<>();

    /**
     * Construction of a cross section link with some extra data.
     * @param network RoadNetwork; the network
     * @param id String; the link id.
     * @param startNode OTSRoadNode; the start node (directional).
     * @param endNode OTSRoadNode; the end node (directional).
     * @param linkType LinkType; the link type
     * @param designLine OTSLine3D; the design line of the Link
     * @param simulator OTSSimulatorInterface; the simulator on which events can be scheduled
     * @param laneKeepingPolicy LaneKeepingPolicy; the policy to generally keep left, keep right, or keep lane
     * @throws NetworkException if link already exists in the network, if name of the link is not unique, or if the start node
     *             or the end node of the link are not registered in the network.
     */
    public ExtendedCrossSectionLink(final RoadNetwork network, final String id, final OTSRoadNode startNode, final OTSRoadNode endNode,
            final LinkType linkType, final OTSLine3D designLine, final OTSSimulatorInterface simulator,
            final LaneKeepingPolicy laneKeepingPolicy) throws NetworkException
    {
        super(network, id, startNode, endNode, linkType, designLine, simulator, laneKeepingPolicy);
    }

    /**
     * Add a feature.
     * @param key String; identifies the database / shape file from which this feature was retrieved
     * @param feature Feature; the feature
     */
    public void addFeature(final String key, final Feature feature)
    {
        this.features.put(key, feature);
    }
    
    /**
     * Retrieve the feature map.
     * @return List&lt;Feature&gt;; the feature map
     */
    public Map<String, Feature> getFeatures()
    {
        return this.features;
    }

}

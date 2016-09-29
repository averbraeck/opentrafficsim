package org.opentrafficsim.road.network.sampling;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.road.network.lane.DirectedLanePosition;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class MetaDataCrossSections extends MetaDataType<CrossSection>
{

    /**
     * @param id id
     */
    public MetaDataCrossSections(final String id)
    {
        super(id);
    }

    /**
     * Accepts all trajectory's or rejects all trajectory's depending on whether all cross sections have been crossed.
     */
    @Override
    protected final boolean[] accept(final List<Trajectory> trajectoryList, final List<Trajectories> trajectoriesList,
        final Set<CrossSection> querySet)
    {
        Set<CrossSection> crossedCrossSections = new HashSet<>();
        // Loop over trajectoryList/trajectoriesList combo
        for (int i = 0; i < trajectoryList.size(); i++)
        {
            Trajectories trajectories = trajectoriesList.get(i);
            // Loop over cross sections
            Iterator<CrossSection> crossSectionIterator = querySet.iterator();
            while (crossSectionIterator.hasNext())
            {
                CrossSection crossSection = crossSectionIterator.next();
                // Loop over lanes in cross section
                Iterator<DirectedLanePosition> directedLanePositionIterator = crossSection.getIterator();
                while (directedLanePositionIterator.hasNext())
                {
                    DirectedLanePosition directedLanePosition = directedLanePositionIterator.next();
                    // If Trajectories is of same lane and direction, check position
                    if (trajectories.getLaneDirection().getLane().equals(directedLanePosition.getLane())
                        && trajectories.getLaneDirection().getDirection().equals(directedLanePosition.getGtuDirection()))
                    {
                        double position = directedLanePosition.getPosition().si;
                        float[] x = trajectoryList.get(i).getX();
                        double xStart = x[0];
                        double xEnd = x[x.length - 1];
                        if ((xStart < position && position < xEnd) || (xEnd < position && position < xStart))
                        {
                            // Trajectory was up- and downstream of the location, so the location was crossed
                            crossedCrossSections.add(crossSection);
                        }
                    }
                }
            }
        }
        boolean[] out = new boolean[trajectoryList.size()];
        if (querySet.equals(crossedCrossSections))
        {
            Arrays.fill(out, true);
        }
        return out;
    }

}

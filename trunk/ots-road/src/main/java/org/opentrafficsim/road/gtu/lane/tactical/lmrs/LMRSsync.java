package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.LinkedHashSet;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). This is extended by:
 * <ul>
 * <li><i>Tag-along behavior</i> at low speed during synchronization, which prevents the driver from being overtaken as it waits
 * for an acceptable gap. This occurs as the follower in the target lane starts accelerating before the potential lane changer
 * does. As a result, the potential lane changer is overtaken. This process may occur sequentially letting the potential lane
 * changer make small jumps and reaching standstill for every new leader. This significantly disturbs the acceleration process
 * and thus queue discharge.</li>
 * <li><i>Active gap selection</i>, taking care of not stopping for synchronization upstream of the location where one can
 * actually merge, and accounting for speed differences with the target lane. The result is synchronization by following a
 * sensible leader in the target lane, rather than simply the direct leader in the target lane. A driver may also decide to
 * reduce acceleration to get behind the follower in the target lane. If synchronization is determined to be impossible, the
 * driver decelerates. Furthermore, speeds may be reduced to allow time for further lane changes.
 * <li><i>Courtesy lane changes</i>, where the level of lane change desire of drivers in adjacent lanes towards the current
 * lane, results in an additional lane change incentive towards the other adjacent lane. A factor <i>p</i> is applied to the
 * lane change desire of the adjacent leader. Further leaders are considered less. The opposite is also applied. Leaders on the
 * second lane to either direction that have desire to change to the first lane in that direction, result in a negative courtesy
 * desire. I.e. leave adjacent lane open for a leader on the second lane.</li>
 * <li><i>Gap-creation</i> is changed by letting drivers reduce speed for any adjacent leader within a given distance, rather
 * than only the direct leader. Furthermore, gaps are also created during lane changes, also for the adjacent lane of the target
 * lane.</li>
 * </ul>
 * See Schakel, W.J., Knoop, V.L., and Van Arem, B. (2012), <a
 * href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model with Relaxation and
 * Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, No. 2316, pp. 47-57. Note
 * in the official versions of TRB and TRR some errors appeared due to the typesetting of the papers (not in the preprint
 * provided here). A list of errata for the official versions is found <a
 * href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 13, 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LMRSsync extends AbstractLMRS
{

    /** Serialization id. */
    private static final long serialVersionUID = 20160803L;

    /**
     * Constructor setting the car-following model.
     * @param carFollowingModel Car-following model.
     */
    public LMRSsync(final CarFollowingModel carFollowingModel)
    {
        super(carFollowingModel);
    }

    /** {@inheritDoc} */
    @Override
    public final LinkedHashSet<MandatoryIncentive> getDefaultMandatoryIncentives()
    {
        LinkedHashSet<MandatoryIncentive> set = new LinkedHashSet<>();
        set.add(new IncentiveRoute());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final LinkedHashSet<VoluntaryIncentive> getDefaultVoluntaryIncentives()
    {
        LinkedHashSet<VoluntaryIncentive> set = new LinkedHashSet<>();
        set.add(new IncentiveSpeed());
        set.add(new IncentiveKeep());
        set.add(new IncentiveHierarchal());
        set.add(new IncentiveCourtesy());
        return set;
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final GTU gtu, final Time startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, GTUException, NetworkException,
        ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        String mandatory;
        try
        {
            mandatory = "mandatoryIncentives=" + getMandatoryIncentives() + ", ";
        }
        catch (GTUException ope)
        {
            // thrown if no mandatory incentives
            mandatory = "mandatoryIncentives=[]";
        }
        String voluntary;
        if (!getVoluntaryIncentives().isEmpty())
        {
            voluntary = "voluntaryIncentives=" + getVoluntaryIncentives();
        }
        else
        {
            voluntary = "voluntaryIncentives=[]";
        }
        return "LMRSsync [" + mandatory + voluntary + "]";
    }

}

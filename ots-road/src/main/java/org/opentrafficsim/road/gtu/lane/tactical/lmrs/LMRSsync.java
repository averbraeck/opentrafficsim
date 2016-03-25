package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

/**
 * Implementation of the LMRS (Lane change Model with Relaxation and Synchronization). This is extended by:
 * <li><i>Tag-along behavior</i> at low speed during synchronization, which prevents the driver from being overtaken as
 * it waits for an acceptable gap. This occurs as the follower in the target lane starts accelerating before the 
 * potential lane changer does. As a result, the potential lane changer is overtaken. This process may occur 
 * sequentially letting the potential lane changer make small jumps and reaching standstill for every new leader. This
 * significantly disturbs the acceleration process and thus queue discharge.
 * <li><i>Active gap selection</i>, taking care of not stopping for synchronization upstream of the location where one 
 * can actually merge, and accounting for speed differences with the target lane. The result is synchronization by
 * following a sensible leader in the target lane, rather than simply the direct leader in the target lane. A driver may
 * also decide to reduce acceleration to get behind the follower in the target lane. If synchronization is determined to
 * be impossible, the driver decelerates. Furthermore, speeds may be reduced to allow time for further lane changes.
 * <li><i>Courtesy lane changes</i>, where the level of lane change desire of drivers in adjacent lanes towards the 
 * current lane, results in an additional lane change incentive towards the other adjacent lane. A factor <i>p</i> is 
 * applied to the lane change desire of the adjacent leader. Further leaders are considered less. The opposite is also 
 * applied. Leaders on the second lane to either direction that have desire to change to the first lane in that 
 * direction, result in a negative courtesy desire. I.e. leave adjacent lane open for a leader on the second lane.
 * <li><i>Gap-creation</i> is changed by letting drivers reduce speed for any adjacent leader within a given distance, 
 * rather than only the direct leader. Furthermore, gaps are also created during lane changes, also for the adjacent 
 * lane of the target lane.
 * 
 * @see Schakel, W.J., Knoop, V.L., and Van Arem, B. (2012), 
 * <a href="http://victorknoop.eu/research/papers/TRB2012_LMRS_reviewed.pdf">LMRS: Integrated Lane Change Model with 
 * Relaxation and Synchronization</a>, Transportation Research Records: Journal of the Transportation Research Board, 
 * No. 2316, pp. 47-57. Note in the official versions of TRB and TRR some errors appeared due to the typesetting of the 
 * papers (not in the preprint provided here). A list of errata for the official versions is found 
 * <a href="http://victorknoop.eu/research/papers/Erratum_LMRS.pdf">here</a>.
 * @author Wouter Schakel
 */
public class LMRSsync extends LMRS {

	/** Serialization id. */
	private static final long serialVersionUID = 20160803L;

	/**
	 * Sets the default lane change incentives. These are the mandatory route incentive, and the voluntary speed, keep, 
	 * hierarchal and and courtesy incentives. Any existing incentives are removed.
	 */
	@Override
	public void setDefaultIncentives() {
		this.mandatoryIncentives.clear();
		this.voluntaryIncentives.clear();
		this.mandatoryIncentives.add(new IncentiveRoute());
		this.voluntaryIncentives.add(new IncentiveSpeed());
		this.voluntaryIncentives.add(new IncentiveKeep());
		this.voluntaryIncentives.add(new IncentiveHierarchal());
		this.voluntaryIncentives.add(new IncentiveCourtesy());
	}
	
	
	
}

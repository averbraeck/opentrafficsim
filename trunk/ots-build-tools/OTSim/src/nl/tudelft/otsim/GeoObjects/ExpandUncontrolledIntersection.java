package nl.tudelft.otsim.GeoObjects;

import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import nl.tudelft.otsim.GeoObjects.Node.DirectionalLink;
import nl.tudelft.otsim.GeoObjects.PriorityConflict.conflictType;
import nl.tudelft.otsim.SpatialTools.Circle;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * Expand a {@link Node} into a simple intersection with no traffic lights and all conflicts resolved along Dutch traffic law
 * priority rules.
 * 
 * @author Peter Knoppers & Guus F Tamminga
 */
public class ExpandUncontrolledIntersection implements NodeExpander {
	final Node node;
	final String hints;

	/**
	 * Create a NodeExpander that creates a simple intersection with no traffic lights and conflicts resolved using Dutch traffic
	 * law priority rules.
	 * @param node {@link Node}; the Node that uses this NodeExpander
	 * @param hints {@link String}; hint that direct the way this NodeExpander operates
	 */
	public ExpandUncontrolledIntersection(Node node, String hints) {
		this.node = node;
		this.hints = hints;
	}

	private enum Turn {
		RIGHT_UTURN, RIGHT_SHARP, RIGHT, STRAIGHT, LEFT, LEFT_SHARP, LEFT_UTURN
	};

	/** Reset the fromNodeExpand c.q. toNodeExpand that link into the current sub-Network */
	private void unlinkSubNetwork() {
		for (DirectionalLink dl : node.getLinksFromJunction(null))
			if (dl.incoming) {
				if (dl.link.getToNodeExpand() != node)
					dl.link.setToNodeExpand(node);
			} else {
				if (dl.link.getFromNodeExpand() != node)
					dl.link.setFromNodeExpand(node);
			}
	}

	private boolean expansionNeeded() {
		int entranceCount = node.incomingCount();
		int exitCount = node.leavingCount();
		if ((entranceCount == 0) || (exitCount == 0) || (entranceCount + exitCount <= 2)) {
			System.out.println("NOT creating sub-network for node " + node.getName_r());
			return false; // No node expansion needed for this degenerated node
		}

		if ((entranceCount == 2) && (exitCount == 2)) { // Check if this is node is really a form point in a two-way road
			boolean couldBeFormPoint = true;
			// Try to find an anti-parallel incoming link for every outgoing link
			for (DirectionalLink out : node.getLinksFromJunction(false)) {
				boolean foundReverse = false;
				for (DirectionalLink in : node.getLinksFromJunction(true)) {
					if (out.link.getToNode_r() == in.link.getFromNode_r()) {
						foundReverse = true;
						break;
					}
				}
				if (!foundReverse) {
					// for this outgoing link there is no anti-parallel counterpart
					couldBeFormPoint = false; // This node is not merely a form point
					break; // no need to look further
				}
			}
			if (couldBeFormPoint) {
				System.out.println("NOT creating sub-network for node " + node.getName_r());
				return false; // No node expansion needed for this degenerated node
			}
		}
		return true;
	}

	@Override
	public Network expandNode() {
		unlinkSubNetwork();
		if (!expansionNeeded())
			return null;

		// Expand a junction by creating a sub-Network.
		//
		// Create a Node for every entering link and every exiting link.
		// These new Nodes replace the Node that the incoming or outgoing link
		// was connected to at this Node.
		// The new Nodes get the coordinates of the center-most driving lane
		// Connecting Links are created between the new nodes
		// These links receive a new crossSection, and one crossSection element: the road with road markings and
		// finally driving lanes

		System.out.println("Creating sub-Network for node " + node.getName_r() + " links:");
		ArrayList<DirectionalLink> dlList = node.getLinksFromJunction(null);
		for (DirectionalLink dl : dlList)
			System.out.println("  " + dl.toString());
		int entranceCount = node.incomingCount();
		Network result = new Network(node);
		final boolean uTurnAllowed = false;
		int nextNodeID = 0;
		ArrayList<Node> allNodes = node.getParentNetwork_r().getAllNodeList(false);
		if (allNodes.size() > 0)
			nextNodeID = allNodes.get(allNodes.size() - 1).getNodeID() + 1;
		int initialNodeID = nextNodeID;
		// Create one Node in the sub-Network for each DirectionalLink and set
		// it as fromNodeExpand c.q. toNodeExpand in that DirectionalLink
		int incomingLaneCount = 0;
		int leavingLaneCount = 0;
		for (DirectionalLink dl : dlList) {
			ArrayList<Vertex> vertices = dl.link.getVertices();
			Vertex location = vertices.get(dl.incoming ? vertices.size() - 2 : 1);
			if (dl.incoming) {
				dl.link.setToNodeExpand(result.addNode(dl.link.getName_r() + "_i", nextNodeID++, location.x, location.y, node.z));
				incomingLaneCount += dl.link.getCrossSectionAtNode(true).collectLanes().size();
			} else {
				dl.link.setFromNodeExpand(result.addNode(dl.link.getName_r() + "_o", nextNodeID++, location.x, location.y, node.z));
				leavingLaneCount += dl.link.getCrossSectionAtNode(false).collectLanes().size();
			}
		}
		System.out.println("Created " + (nextNodeID - initialNodeID) + " sub-Nodes in the sub-Network of Node " + node.getName_r());

		// for every incoming arm
		for (DirectionalLink incomingLink : node.getLinksFromJunction(true)) {
			System.out.println("Creating connecting lanes for incoming traffic on internal Node "
					+ incomingLink.link.getToNodeExpand().getNodeID() + ", " + incomingLink.link.getToNodeExpand().getName_r());
			// Step A: build a list of turning movements
			ArrayList<Lane> incomingLanes = incomingLink.link.getCrossSectionAtNode(true).collectLanesReversed();
			// step through the outgoing links and collect information for all leaving lanes
			// An OutLinkInfo object is created to store that information
			ArrayList<Exit> exits = new ArrayList<Exit>();
			System.out.println("Creating fresh ArrayList neededConnectingLanes");
			ArrayList<NeededConnectingLane> neededConnectingLanes = new ArrayList<NeededConnectingLane>();

			//
			// STEP 1: Collect all exiting arms sequentially starting with the right-most as seen from incomingLink
			//
			int armIndex = dlList.indexOf(incomingLink);
			int totalExitLanes = 0;
			for (int arm = 1; arm < dlList.size(); arm++) {
				armIndex++;
				// Examine all links in circular order starting right after the current incoming link and stopping just before it
				if (armIndex >= dlList.size())
					armIndex = 0; // wrap around
				System.out.println(" arm is " + arm + ", armIndex is " + armIndex);
				DirectionalLink exitLink = dlList.get(armIndex);
				if (exitLink.incoming)
					continue; // Not an exit Link
				// compute the angle in radians between the incoming and leaving link
				double headingChange = Planar.normalizeAngle(exitLink.angle - incomingLink.angle);
				CrossSection leavingCS = exitLink.link.getCrossSections_r().get(0);
				// collect information on the index of the leaving link, its lanes and
				// the angle with the entering lane
				Exit exit = new Exit(leavingCS.collectLanesReversed(), headingChange, exits.size(), exitLink.link);
				if (exit.isUTurn() && (!uTurnAllowed))
					continue;
				exits.add(exit);
				totalExitLanes = totalExitLanes + leavingCS.collectLanesReversed().size();
			}

			boolean useTurnArrows = true;
			// Only use the turn arrows if ALL lanes have them.
			for (Lane l : incomingLanes)
				if (null == l.getTurnArrow().getOutLinkNumbers()) {
					useTurnArrows = false;
					break;
				}
			// STEP 2: determine the turning movements
			if ((exits.size() > 0) && ((entranceCount + exits.size()) > 2)) {
				if (incomingLaneCount == leavingLaneCount) {
					// Special (easy) case: simple connect each incoming lane to the next available leaving lane.
					// There should be no conflicts; turnArrows are ignored.
					// This happens particularly at on-ramps and off-ramps of highways.
					for (Lane incomingLane : incomingLanes) {
						//System.out.println("Connecting incoming lane " + incomingLane.toString());
						int skip = incomingLanes.size() - 1 - incomingLanes.indexOf(incomingLane);
						for (int arm = dlList.indexOf(incomingLink) - 1; skip >= 0; arm--) {
							if (arm < 0)
								arm += dlList.size();
							DirectionalLink dl = dlList.get(arm);
							if (dl.incoming)
								skip += dl.link.getCrossSectionAtNode(true).collectLanes().size();
							else {
								for (Lane leavingLane : dl.link.getCrossSectionAtNode(false).collectLanes()) {
									if (0 == skip--) {
										int exitIndex = -1;
										for (int i = exits.size(); --i >= 0; )
											if (exits.get(i).link == dl.link)
												exitIndex = i;
										if (exitIndex < 0)
											throw new Error("Cannot find exitIndex");
										System.out.println("creating NeededConnectingLane (-1) from " + incomingLane.toString() + " to " + leavingLane.toString());
										neededConnectingLanes.add(new NeededConnectingLane(incomingLane, leavingLane, exitIndex));
									}
								}
							}
						}
					}
				} else if (useTurnArrows) {
					// STEP 2A
					// The turning movements are defined by TurnArrows, the new
					// turning lanes must be constructed accordingly.
					// The OutlinkNumber defines the arm(s) (in anti-clockwise
					// order) that this lane connects to could be "1" (first) or
					// "12" (could be shared right/straight or right/right2 or
					// right/left)

					// loop through the lanes of the entering link
					int turnArrowIndex = 0; // the index of the incoming lane
					// Handle lanes from outermost to center-most
					for (int incomingLaneRank = 0; incomingLaneRank < incomingLanes.size(); incomingLaneRank++) {
						// We start with the first (anti-clockwise; probably right) turn
						Lane currentInLane = incomingLanes.get(incomingLaneRank);
						// retrieve the rank of the link to turn to from this inlane (from 0 to maxOutlinks - 1)
						int arrowIndicatedExit = currentInLane.getTurnArrow().getOutLinkNumbers()[turnArrowIndex];
						// we look for the connecting outLane by looping through all outlanes at this node
						if ((arrowIndicatedExit < 0) || (arrowIndicatedExit >= exits.size()))
							throw new Error("arrow indicated exit out of range");
						Exit exit = exits.get(arrowIndicatedExit);
						int inLanesToConnect = 1;
						// look if there are more turn lanes with the same turning direction
						while (incomingLaneRank + inLanesToConnect < incomingLanes.size())
							if (incomingLanes.get(incomingLaneRank + inLanesToConnect).getTurnArrow().getOutLinkNumbers()[0] == arrowIndicatedExit)
								inLanesToConnect++;
							else
								break;
						for (int i = 0; i < inLanesToConnect; i++) {
							if (i >= exit.getOutLanes().size())
								System.out.println("error inlanes to connect higher than outlanes");
							Lane outLane = exit.getOutLanes().get(i);
							// if a lane has got no stopLine (no priority defined) an "empty" stopLine is added
							if (null == currentInLane.getStopLine())
								currentInLane
										.setStopLine(new StopLine(currentInLane.getCse(), StopLine.NOSTOPLINE, -4.0, 2.0, 0.0));
							currentInLane = incomingLanes.get(incomingLaneRank);
							System.out.print("creating NeededConnectingLane (0): ");
							neededConnectingLanes.add(new NeededConnectingLane(currentInLane, outLane, exit.getLinkRank()));
							if (inLanesToConnect - i > 1)
								incomingLaneRank++;
						}
						// if more than one turning movement from an incoming lane?
						// keep this lane at the next loop (until all turns are dealt with)
						turnArrowIndex++;
						if (turnArrowIndex < incomingLanes.get(incomingLaneRank).getTurnArrow().getOutLinkNumbers().length)
							incomingLaneRank--;
						else
							// go to the next inLane and reset turnCount to zero
							turnArrowIndex = 0;
					}
				} else {
					// STEP 2B
					// No turning movements are defined for this link/lane,
					// construct the movements using heuristics

					int[] exitLanesAssigned = new int[exits.size()];
					int totalWantedLanes = 0;
					for (int exitIndex = 0; exitIndex < exits.size(); exitIndex++) {
						final Exit exit = exits.get(exitIndex);
						final double shareOfLanes = ((double) incomingLanes.size() * exit.numberOfLanes()) / totalExitLanes;
						exitLanesAssigned[exitIndex] = Math.min(Math.max((int) Math.ceil(shareOfLanes), 1), exit.numberOfLanes());
						totalWantedLanes += exitLanesAssigned[exitIndex];
					}
					int rightMost = 0;
					int leftMost = exits.size() - 1;
					int entranceLaneShortage = totalWantedLanes - incomingLanes.size();
					int[] fanOut = new int[exits.size()]; // guaranteed all initialized to 0
					if (totalWantedLanes > incomingLanes.size()) {
						// There are fewer incoming lanes than exiting lanes; share incoming lanes or don't connect to all lanes of
						// a multiple-lane exits
						int[] shareingPriority = new int[exits.size()];
						for (int exitIndex = 0; exitIndex < exits.size(); exitIndex++) {
							switch (exits.get(exitIndex).turnType()) {
							case LEFT_UTURN:
								shareingPriority[exitIndex] = 0;
								break;
							case RIGHT_UTURN:
								shareingPriority[exitIndex] = 0;
								break;
							case RIGHT_SHARP:
								shareingPriority[exitIndex] = 1;
								break;
							case LEFT_SHARP:
								shareingPriority[exitIndex] = 2;
								break;
							case RIGHT:
								shareingPriority[exitIndex] = 3;
								break;
							case LEFT:
								shareingPriority[exitIndex] = 4;
								break;
							case STRAIGHT:
								shareingPriority[exitIndex] = 5;
								break;
							}
						}
						while ((rightMost < leftMost) && (entranceLaneShortage > 0)) {
							// Assign one entrance lane to two exit lanes
							if (shareingPriority[rightMost] <= shareingPriority[rightMost + 1]) {
								if (shareingPriority[rightMost] <= shareingPriority[leftMost]) {
									if (0 == fanOut[rightMost])
										fanOut[rightMost]++;
									else
										exitLanesAssigned[rightMost]--;
									rightMost++;
								} else {
									if (0 == fanOut[leftMost])
										fanOut[leftMost]++;
									else
										exitLanesAssigned[leftMost]--;
									leftMost--;
								}
							} else {
								if (shareingPriority[rightMost + 1] < shareingPriority[leftMost]) {
									if (0 == fanOut[rightMost + 1])
										fanOut[rightMost + 1]++;
									else if (exitLanesAssigned[rightMost + 1] > 1)
										exitLanesAssigned[rightMost + 1]--;
									else
										fanOut[rightMost]++;
									rightMost += 2;
								} else {
									if (0 == fanOut[leftMost])
										fanOut[leftMost - 1]++;
									else if (exitLanesAssigned[leftMost] > 1)
										exitLanesAssigned[leftMost]--;
									else
										fanOut[leftMost - 1]++;
									leftMost--;
								}
							}
							entranceLaneShortage--;
						}
					}
					final int excessLanes = Math.max(0, incomingLanes.size() - totalWantedLanes);

					Lane currentInLane = null;
					int incomingLaneIndex = 0;
					for (int exitIndex = 0; exitIndex < exits.size(); exitIndex++) {
						Exit exit = exits.get(exitIndex);
						ArrayList<Lane> outLanes = exit.getOutLanes();
						// Connect the incoming and exiting lanes
						for (int i = 0; i < exitLanesAssigned[exitIndex]; i++) {
							currentInLane = incomingLanes.get(incomingLaneIndex);
							if (null == currentInLane.getStopLine())
								currentInLane.setStopLine(new StopLine(currentInLane.getCse(), StopLine.NOSTOPLINE, -4.0, 2.0, 0.0));
							Lane exitLane = exit.isRightOrStraight() ? outLanes.get(i) 
									: outLanes.get(exit.numberOfLanes() - exitLanesAssigned[exitIndex] + i);
							System.out.print("creating NeededConnectingLane (1): ");
							neededConnectingLanes.add(new NeededConnectingLane(currentInLane, exitLane, exitIndex));
							// if there are more incoming lanes than leaving lanes, connect all extra incoming lanes to
							// currentOutLane
							if ((excessLanes > 0) && (incomingLaneIndex == exitLanesAssigned[exitIndex] - 1)) {
								incomingLaneIndex++;
								currentInLane = incomingLanes.get(incomingLaneIndex);
								if (null == currentInLane.getStopLine())
									currentInLane.setStopLine(new StopLine(currentInLane.getCse(), StopLine.NOSTOPLINE, -4.0, 2.0,
											0.0));
								System.out.print("creating NeededConnectingLane (2): ");
								neededConnectingLanes.add(new NeededConnectingLane(currentInLane, exitLane, exitIndex));
							}
							incomingLaneIndex++;
						}
						// in case of any remaining exit lanes that are not yet connected
						// i.e. if there are 2 right turning lanes and 3 exit lanes,
						// the second right lane (counting from outer to inner) here gets
						// connected to the third exit lane)
						incomingLaneIndex -= fanOut[exitIndex];
					}
				} // FINISHED STEP 2B
			}
			// STEP 3: Create the connecting links from the current entrance
			for (Exit exit : exits) {
				// Build a list of all needed lanes for this exit
				ArrayList<NeededConnectingLane> connectingLanes = new ArrayList<NeededConnectingLane>();
				for (NeededConnectingLane ncl : neededConnectingLanes)
					if (ncl.outLane.getCse().getCrossSection().getLink() == exit.link)
						connectingLanes.add(ncl);
				createJunctionLink(result, connectingLanes);
			}
		}
		/*
		 * Discussion: As we are creating a sub-Networks we must create internal Links between the entrance Nodes and exit Nodes.
		 * These internal Links start with a CrossSection containing exactly one CrossSectionElement which is drivable and contains
		 * one or more Lanes. To connect smoothly, the Lanes on internal Links must begin at the exact lateral offset that the
		 * feeding Lanes that they connect onto arrives at. At the exit Node the Lanes on the connecting Link must end at the exact
		 * lateral offset as the Lanes on the exiting Link that these Lanes must connect to. The required offset at this point
		 * (usually) differs from the offset required at the entrance Node. To accommodate this, the internal Link needs two
		 * CrossSections. The Lane(s) in the internal Link will automatically get left and right vertices that are offset to the
		 * design line of the Link. The offset from the design line will increase or decrease in proportion with the position
		 * between the first and last CrossSection. This means that the starting and ending direction of the Lanes is hard to
		 * control.
		 */
		result.rebuild(); // Creates all the Lanes
		for (Lane lane : node.getParentNetwork_r().getLanes())
			if (lane.getDestination() == node.getNodeID())
				lane.setDestination(0);
		/*
		 * Consecutive Lanes within each Link were connected in rebuild. 
		 * Now connect to the incoming and leaving Lanes to the internal Lanes.
		 */
		for (DirectionalLink dl : node.getLinksFromJunction(false)) {
			Link toLink = dl.link;
			Node n = toLink.getFromNodeExpand();
			for (DirectionalLink fromLink : n.getLinksFromJunction(true))
				linkLanes(fromLink.link, toLink);
		}
		for (DirectionalLink dl : node.getLinksFromJunction(true)) {
			Link fromLink = dl.link;
			Node n = fromLink.getToNodeExpand();
			for (DirectionalLink toLink : n.getLinksFromJunction(false))
				linkLanes(fromLink, toLink.link);
		}
		for (Lane l : result.getLanes()) {
			System.out.println(String.format(Locale.US, "Lane %s on link %s from %s to %s up: %s, down %s", 
					l.toString(), l.crossSectionElement.getCrossSection().getLink().toString(),
					l.crossSectionElement.getCrossSection().getLink().getExpandedFromNode_r().toString(),
					l.crossSectionElement.getCrossSection().getLink().getExpandedToNode_r().toString(),
					null == l.getUp() ? "NULL" : l.getUp().toString(), 
					null == l.getDown() ? "NULL" : l.getDown().toString()));
		}
		// Block B Create the conflicts. Check all conflicts and figure out who yields to whom on each conflict
		// revisit all new Turning Links at this node (junction) to investigate conflicting lanes (merge, split, cross)
		for (Link link : result.getLinks()) {
			System.out.println("Checking link " + link);
			for (CrossSectionObject csoA : link.getCrossSections_r().get(link.getCrossSections_r().size() - 1)
					.getCrossSectionElementList_r().get(0).getCrossSectionObjects(Lane.class))
				System.out.println("  lane " + ((Lane) csoA).getID());
			for (Link compareToLink : result.getLinks()) {
				// only different links
				if (compareToLink == link)
					continue;
				// visit pairs of links only once
				if (compareToLink.getName_r().compareTo(link.getName_r()) <= 0)
					continue;
				// After determining the type of conflict, the priority rules are applied (traffic from the right has priority)
				for (CrossSectionObject csoA : link.getCrossSections_r().get(link.getCrossSections_r().size() - 1)
						.getCrossSectionElementList_r().get(0).getCrossSectionObjects(Lane.class)) {
					Lane laneA = (Lane) csoA;
					for (CrossSectionObject csoB : compareToLink.getCrossSections_r()
							.get(compareToLink.getCrossSections_r().size() - 1).getCrossSectionElementList_r().get(0)
							.getCrossSectionObjects(Lane.class)) {
						Lane laneB = (Lane) csoB;
						System.out.println("Checking conflict between lanes " + laneA.getID() + " and " + laneB.getID() + " at expanding node " + node.getNodeID());

						conflictType cType = null;
						Lane priorityLane;
						Lane yieldLane;

						if (null == laneA.getUp())
							System.err.println("null up on A: " + laneA.toString());
						if (null == laneB.getUp())
							System.err.println("null up on B: " + laneB.toString());
						if (null == laneA.getDown())
							System.err.println("null down on A: " + laneA.toString());
						if (null == laneB.getDown())
							System.err.println("null down on B: " + laneA.toString());
						Lane upA = laneA.getUp().get(0);
						Lane upB = laneB.getUp().get(0);
						Lane downA = laneA.getDown().get(0);
						Lane downB = laneB.getDown().get(0);
						Link linkUpA = upA.getCse().getCrossSection().getLink();
						Link linkUpB = upB.getCse().getCrossSection().getLink();
						Link linkDownA = downA.getCse().getCrossSection().getLink();
						Link linkDownB = downB.getCse().getCrossSection().getLink();
						boolean yieldA = true;
						boolean yieldB = true;

						if ((upA.getStopLine() != null) && (upA.getStopLine().getType() == StopLine.PRIORITYSTOPLINE))
							yieldA = false;
						if ((upB.getStopLine() != null) && (upB.getStopLine().getType() == StopLine.PRIORITYSTOPLINE))
							yieldB = false;
						if (yieldA && (!yieldB)) {
							yieldLane = laneA;
							priorityLane = laneB;
						} else if ((!yieldA) && yieldB) {
							yieldLane = laneB;
							priorityLane = laneA;
						} else { // Stop lines did not resolve this conflict.
							// Apply traffic law priority rules
							// could be on a junction with no rules or two opposing roads (both priority or yield)
							double angleIncomingA = Double.NaN;
							double angleIncomingB = Double.NaN;
							double angleLeavingA = Double.NaN;
							double angleLeavingB = Double.NaN;
							for (DirectionalLink dl : dlList) {
								if (dl.incoming) {
									if (dl.link.equals(linkUpA))
										angleIncomingA = dl.angle;
									if (dl.link.equals(linkUpB))
										angleIncomingB = dl.angle;
								} else {
									if (dl.link.equals(linkDownA))
										angleLeavingA = dl.angle;
									if (dl.link.equals(linkDownB))
										angleLeavingB = dl.angle;
								}
							}
							double angleDif;
							if (angleIncomingA > angleIncomingB)
								angleDif = 2 * Math.PI - angleIncomingA + angleIncomingB;
							else
								angleDif = angleIncomingA - angleIncomingB;
							if (angleDif < 0.75 * Math.PI) { // lane B comes from right
								yieldLane = laneA;
								priorityLane = laneB;
							} else if (angleDif > 1.25 * Math.PI) { // lane B comes from left
								priorityLane = laneA;
								yieldLane = laneB;
							} else { // Opposing flows: turning movement determines priority rules
								double turnAngleA;
								double turnAngleB;
								if (angleIncomingA > angleLeavingA)
									turnAngleA = 2 * Math.PI - angleIncomingA + angleLeavingA;
								else
									turnAngleA = angleIncomingA - angleLeavingA;
								if (angleIncomingB > angleLeavingB)
									turnAngleB = 2 * Math.PI - angleIncomingB + angleLeavingB;
								else
									turnAngleB = angleIncomingB - angleLeavingB;
								// turn with smallest angle has priority
								if (turnAngleA < turnAngleB) {
									priorityLane = laneA;
									yieldLane = laneB;
								} else {
									yieldLane = laneA;
									priorityLane = laneB;
								}
							}
						}
						StopLine stopLine = yieldLane.getUp().get(0).getStopLine();
						Polygon cArea = new Polygon();
						// Determine location at the lanes at the start of the conflict Area:
						Point2D.Double pInIn = getConflictIntersectionPoint(laneA.getLaneVerticesInner(),
								laneB.getLaneVerticesInner());
						Point2D.Double pInOut = getConflictIntersectionPoint(laneA.getLaneVerticesInner(),
								laneB.getLaneVerticesInner());
						Point2D.Double pOutIn = getConflictIntersectionPoint(laneA.getLaneVerticesInner(),
								laneB.getLaneVerticesInner());
						Point2D.Double pOutOut = getConflictIntersectionPoint(laneA.getLaneVerticesInner(),
								laneB.getLaneVerticesInner());
						// determine the stopLines of the incoming Link
						if (laneA.getUp().get(0).equals(laneB.getUp().get(0)))
							cType = conflictType.SPLIT;
						else if (laneA.getDown().get(0).equals(laneB.getDown().get(0)))
							cType = conflictType.MERGE;
						else if ((pInIn != null) || (pInOut != null) || (pOutIn != null) || (pOutOut != null))
							cType = conflictType.CROSSING;

						// FIXME: this does not look like the correct ordering of the points (expected inin, inout, outout, outin)
						if (pInIn != null)
							cArea.addPoint((int) pInIn.getX(), (int) pInIn.getY());
						if (pInOut != null)
							cArea.addPoint((int) pInOut.getX(), (int) pInOut.getY());
						if (pOutIn != null)
							cArea.addPoint((int) pOutIn.getX(), (int) pOutIn.getY());
						if (pOutOut != null)
							cArea.addPoint((int) pOutOut.getX(), (int) pOutOut.getY());

						if ((conflictType.MERGE == cType) || (conflictType.CROSSING == cType)) {
							if (cType.equals(conflictType.MERGE))
								yieldLane.addMergingYieldToLaneList(priorityLane);
							else if (cType.equals(conflictType.CROSSING))
								yieldLane.addCrossingYieldToLaneList(priorityLane);
							PriorityConflict priorityConflict = new PriorityConflict(priorityLane, yieldLane, cType, cArea);
							// add conflict to the relevant stopLine
							if (stopLine == null)
								System.err.println("no Stopline created or found");
							else
								stopLine.addConflicts(priorityConflict);
						} else
							System.err.println(String.format("Conflict of y%s and p%s at node %d has no intersection...",
									yieldLane.toString(), priorityLane.toString(), node.getNodeID()));
					}
				}
			}
		}
		System.out.format("Created %d links for sub-network for node %s\n", result.getLinks().size(), node.getName_r());
		return result;
	}

	private static void linkLanes(Link fromLink, Link toLink) {
		if (fromLink.getToNodeExpand() != toLink.getFromNodeExpand())
			throw new Error("fromLink does not end at start of toLink");
		CrossSection fromCS = fromLink.getCrossSectionAtNode(true);
		CrossSection toCS = toLink.getCrossSectionAtNode(false);
		for (CrossSectionElement fromCSE : fromCS.getCrossSectionElementList_r()) {
			for (CrossSectionObject fromCSO : fromCSE.getCrossSectionObjects(Lane.class)) {
				Lane fromLane = (Lane) fromCSO;
				ArrayList<Vertex> fromVertices = fromLane.getLaneVerticesCenter();
				Point2D.Double fromPosition = fromVertices.get(fromVertices.size() - 1).getPoint();
				for (CrossSectionElement toCSE : toCS.getCrossSectionElementList_r()) {
					for (CrossSectionObject toCSO : toCSE.getCrossSectionObjects(Lane.class)) {
						Lane toLane = (Lane) toCSO;
						Vertex inner = toLane.getLaneVerticesInner().get(0);
						Vertex outer = toLane.getLaneVerticesOuter().get(0);
						Point2D.Double center = toLane.getLaneVerticesCenter().get(0).getPoint();
						Line2D.Double centerLine = new Line2D.Double(toLane.getLaneVerticesCenter().get(0).getPoint(), toLane.getLaneVerticesCenter().get(1).getPoint());
						double width = inner.distance(outer);
						double distance = center.distance(fromPosition);
						distance = Planar.distanceLineSegmentToPoint(centerLine, fromPosition);
						centerLine = new Line2D.Double(fromVertices.get(fromVertices.size() - 1).getPoint(), fromVertices.get(fromVertices.size() - 2).getPoint());
						double otherDistance = Planar.distanceLineSegmentToPoint(centerLine, center);
						if (otherDistance < distance)
							distance = otherDistance;
						if (distance < width * 0.55) {
							System.out.println("Linking " + fromLane.getID() + " to " + toLane.getID());
							fromLane.addDownLane(toLane);
							toLane.addUpLane(fromLane);
							System.out.println(String.format(Locale.US, "  fromLane %4d has lateralPosition %.3fm", 
									fromLane.getID(), fromLane.lateralPosition));
							System.out.println(String.format(Locale.US, "    toLane %4d has lateralPosition %.3fm", 
									toLane.getID(), toLane.lateralPosition));
							break;
						}
					}
				}
			}
		}
	}

	/*
	 * Next problem... Currently, the only way to prescribe how lanes shall connect is to do it "yourself" (like in the old node
	 * expansion code). The Nodes in the generated sub-Network have either one entering, or one exiting Link. At these Nodes the
	 * total number of incoming Lanes may not be equal to the total number of exiting Lanes. If there is a difference, we need a way
	 * to prescribe which incoming Lanes shall connect to which outgoing Lanes in a way that is not messed up when this sub-Node is
	 * expanded in turn. One way it to make sure that sub-Nodes are never expanded. Sub-Nodes may be recognized by the fact that
	 * they have either one incoming Link, or one outgoing Link and that all incoming and outgoing Links have (nearly) the same
	 * direction. This is not "robust"; it would fail if all Links have nearly the same direction by accident. An alternative way is
	 * to create these Lanes in the node expansion and not apply Node expansion to Nodes that have connected Lanes in all their
	 * Links.
	 */

	private static CrossSection buildCrossSection(double leftEdge, double rightEdge, ArrayList<NeededConnectingLane> connectingLanes,
			double longitudinalPosition) {
		double startOffset = leftEdge;
		ArrayList<CrossSectionElement> cseList = new ArrayList<CrossSectionElement>();
		CrossSection cs = new CrossSection(longitudinalPosition, startOffset, cseList);
		ArrayList<RoadMarkerAlong> rmaList = new ArrayList<RoadMarkerAlong>();
		double averageLaneWidth = (rightEdge - leftEdge) / connectingLanes.size();
		System.out.println("average lane width " + averageLaneWidth);
		double lowestSpeedLimit = Double.MAX_VALUE;
		double lateralPosition = 0;
		for (NeededConnectingLane ncl : connectingLanes) {
			rmaList.add(new RoadMarkerAlong(connectingLanes.indexOf(ncl) == 0 ? RoadMarkerAlongTemplate.ALONG_CONTINUOUS
					: RoadMarkerAlongTemplate.ALONG_STRIPED, lateralPosition));
			lateralPosition += averageLaneWidth;
			double limit = ncl.inLane.crossSectionElement.getSpeedLimit();
			if (limit < lowestSpeedLimit)
				lowestSpeedLimit = limit;
			limit = ncl.outLane.crossSectionElement.getSpeedLimit();
			if (limit < lowestSpeedLimit)
				lowestSpeedLimit = limit;
		}
		rmaList.add(new RoadMarkerAlong(RoadMarkerAlongTemplate.ALONG_CONTINUOUS, lateralPosition));
		CrossSectionElement cse = new CrossSectionElement(cs, "road", lateralPosition, rmaList, null);
		cse.setSpeedLimit_w(lowestSpeedLimit * 3.6);
		cseList.add(cse);
		return cs;
	}

	private Link createJunctionLink(Network network, ArrayList<NeededConnectingLane> connectingLanes) {
		if (0 == connectingLanes.size())
			throw new Error("Cannot make a connecting Link for 0 Lanes");
		Collections.sort(connectingLanes, new Comparator<NeededConnectingLane>() {
			@Override
			public int compare(NeededConnectingLane a, NeededConnectingLane b) {
				double difference = a.inLane.lateralPosition - b.inLane.lateralPosition;
				if (difference > 0)
					return 1;
				else if (difference < 0)
					return -1;
				return 0;
			}
		});
		Link incomingLink = connectingLanes.get(0).inLane.getCse().getCrossSection().getLink();
		Node fromNode = incomingLink.getToNodeExpand();
		Link outgoingLink = connectingLanes.get(0).outLane.getCse().getCrossSection().getLink();
		Node toNode = outgoingLink.getFromNodeExpand();
		if (fromNode.distance(toNode) < 0.001)
			System.err.println("Nodes are too close");
		ArrayList<Vertex> incomingVertices = incomingLink.getVertices();
		incomingVertices.remove(incomingVertices.size() - 1); // Truncate at Node boundary
		ArrayList<Vertex> outgoingVertices = outgoingLink.getVertices();
		outgoingVertices.remove(0); // Truncate at Node boundary
		ArrayList<Point2D.Double> designLine = Planar.createSmoothCurve(Arrays.asList(Planar.getAlignment(incomingVertices)),
				Arrays.asList(Planar.getAlignment(outgoingVertices)), 0.1);
		designLine.remove(designLine.size() - 1); // Remove tail
		designLine.remove(0); // and head
		// Create the vertices for the design line (using the z of the node)
		ArrayList<Vertex> designVertices = new ArrayList<Vertex>(designLine.size());
		for (Point2D.Double p : designLine)
			designVertices.add(new Vertex(p, node.z));
		ArrayList<CrossSection> csList = new ArrayList<CrossSection>(2);
		//if ((10107 == fromNode.getNodeID()) && (10104 == toNode.getNodeID()))
		//	System.out.println("Creating link from " + fromNode.getNodeID() + " to " + toNode.getNodeID());
		csList.add(buildCrossSection(connectingLanes.get(0).inLane.lateralPosition, 
				connectingLanes.get(connectingLanes.size() - 1).inLane.lateralPosition
				+ connectingLanes.get(connectingLanes.size() - 1).inLane.lateralWidth,
				connectingLanes, 0));
		// Generate a unique sensible name for the new link
		String linkName = "from_" + fromNode.getNodeID() + "_to_" + toNode.getNodeID();
		Link newLink = network.addLink(linkName, fromNode.getNodeID(), toNode.getNodeID(), 0, false, csList, designVertices);
		newLink.calculateLength();
		try {
			csList.get(0).setEndLateralOffset_w(connectingLanes.get(0).outLane.lateralPosition);
		} catch (Exception e) {
			throw new Error("Cannot happen");
		}
		System.out.println("Vertices of autogenerated link: [" + Planar.verticesToString(newLink.getVertices()) + "]");
		for (CrossSection cs : csList)
			cs.setLink(newLink);
		newLink.setFromNodeExpand(fromNode);
		newLink.setToNodeExpand(toNode);
		newLink.rebuildLanes();
		return newLink;
	}

	private static Point2D.Double getConflictIntersectionPoint(ArrayList<Vertex> verticesA, ArrayList<Vertex> verticesB) {
		// TODO: rewrite using Planar.polyLineIntersectsPolyLine
		Vertex prevA = null;
		for (Vertex vA : verticesA) {
			if (null != prevA) {
				Line2D.Double lineA = new Line2D.Double(prevA.getX(), prevA.getY(), vA.getX(), vA.getY());
				Vertex prevB = null;
				for (Vertex vB : verticesB) {
					if (null != prevB) {
						Line2D.Double lineB = new Line2D.Double(prevB.getX(), prevB.getY(), vB.getX(), vB.getY());
						if (Planar.lineSegmentIntersectsLineSegment(lineA, lineB))
							return Planar.intersection(lineA, lineB);
					}
					prevB = vB;
				}
			}
			prevA = vA;
		}
		return null;
	}

	@Override
	public String description() {
		return "Uncontrolled intersection, all roads have equal priority";
	}

	@Override
	public SimplePolygon requiredSpace() {
		System.out.println("Determine node boundary of " + node);
		ArrayList<Point2D.Double> pointCloud = new ArrayList<Point2D.Double>();
		if (!expansionNeeded()) {
			pointCloud.add(node.getPoint());
			return new SimplePolygon(pointCloud, node.z);
		}
		// Add all end points of (drive-able) crossSectionElements to the pointCloud
		for (DirectionalLink dl : node.getDirectionalLinks()) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			for (int index = 2 * cs.getCrossSectionElementList_r().size(); --index >= 0;) {
				Line2D.Double dlLine = cs.vectorAtNode(dl.incoming, true, index, false);
				if (null == dlLine)
					continue;
				pointCloud.add((Point2D.Double) dlLine.getP1());
				for (DirectionalLink otherDL : node.getDirectionalLinks()) {
					if (otherDL.angle >= dl.angle) // only search up to dl (and NEVER include dl itself)
						break; // this way we'll find each intersection only ONCE
					CrossSection otherCS = otherDL.link.getCrossSectionAtNode(otherDL.incoming);
					for (int otherIndex = 2 * otherCS.getCrossSectionElementList_r().size(); --otherIndex >= 0;) {
						Line2D.Double otherDLLine = otherCS.vectorAtNode(otherDL.incoming, true, otherIndex, false);
						if (null == otherDLLine)
							continue;
						if (Planar.lineSegmentIntersectsLineSegment(dlLine, otherDLLine))
							pointCloud.add(Planar.intersection(dlLine, otherDLLine));
					}
				}
			}
		}

		// System.out.println("pointCloud of " + name + " contains these points: " + pointCloud.toString());
		if (0 == pointCloud.size())
			pointCloud.add(node.getPoint()); // add design point of this node
		Circle circle = Planar.circleCoveringPoints(pointCloud);
		// System.out.format("covering circle is %s\r\n", circle.toString());
		// Create the convex hull consisting of the points where the drive-able parts of the links enter the covering circle
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (DirectionalLink dl : node.getDirectionalLinks()) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			for (int index = 2 * cs.getCrossSectionElementList_r().size(); --index >= 0;)
				if (cs.elementFromNode(dl.incoming, true, index).getCrossSectionElementTypology().getDrivable()) {
					Line2D.Double line = cs.vectorAtNode(dl.incoming, true, index, false);
					if (null == line)
						continue;
					Point2D.Double intersections[] = Planar.intersectLineSegmentAndCircle(line, circle);
					// System.out.format(Main.Locale, "line %s intersects circle %s at %d point(s)\r\n",
					// GeometryTools.Line2DToString(line), circle.toString(), intersections.length);
					if (intersections.length > 1) {
						System.err.println("Peter thinks this never happens...");
						// use the one that is closest to the far end of the line
						if (intersections[0].distance(line.getP2()) < intersections[1].distance(line.getP2()))
							points.add(intersections[0]);
						else
							points.add(intersections[1]);
					} else if (intersections.length > 0)
						points.add(intersections[0]);
					else {
						System.out.println("line " + Planar.Line2DToString(line) + " does not intersect the circle "
								+ circle.toString());
						// probably a very near miss
						double ratio = circle.radius() / line.getP1().distance(circle.center());
						if ((ratio > 0.99) && (ratio < 1.01))
							points.add(Planar.log("adding almost intersection", (Point2D.Double) (line.getP1())));
						else
							System.err.println("Total miss: ratio is " + ratio + " (" + circle.radius()
									/ line.getP2().distance(circle.center()) + ")");
					}
				}
		}
		// Check if the point cloud is degenerate
		Circle c = Planar.circleCoveringPoints(points);
		if (c.radius() < 0.1) {
			points.clear();
			points.add(node.getPoint());
		}
		return new SimplePolygon(points, node.z);
		// System.out.println("convex hull is " + conflictArea.toString());
	}

	private class Exit {
		private final ArrayList<Lane> outLanes;
		private final int linkRank;
		private final Link link;
		private final Turn turnType;
		private final boolean rightOrStraight;
		private final boolean uTurn;

		private Exit(ArrayList<Lane> outLanes, Double headingChange, int linkRank, Link link) {
			this.outLanes = outLanes;
			this.linkRank = linkRank;
			this.link = link;
			final double uTurnRight = 0.10 * Math.PI;
			final double sharpRight = 0.25 * Math.PI;
			final double right = 0.75 * Math.PI;
			final double straight = 2 * Math.PI - right;
			final double left = 2 * Math.PI - sharpRight;
			final double sharpLeft = 2 * Math.PI - uTurnRight;

			if ((headingChange < uTurnRight) && (linkRank == 0))
				turnType = Turn.RIGHT_UTURN;
			else if ((headingChange >= uTurnRight) && (headingChange <= sharpRight))
				turnType = Turn.RIGHT_SHARP;
			else if ((headingChange >= sharpRight) && (headingChange <= right))
				turnType = Turn.RIGHT;
			else if ((headingChange >= right) && (headingChange <= straight))
				turnType = Turn.STRAIGHT;
			else if ((headingChange >= straight) && (headingChange <= left))
				turnType = Turn.LEFT;
			else if ((headingChange >= left) && (headingChange <= sharpLeft))
				turnType = Turn.LEFT_SHARP;
			// only the last arm can be a U-turn
			else if (((headingChange > sharpLeft) || (headingChange < uTurnRight)) && (linkRank > 0))
				turnType = Turn.LEFT_UTURN;
			else
				throw new Error("Cannot identify turn type");
			uTurn = (turnType == Turn.RIGHT_UTURN) || (turnType == Turn.LEFT_UTURN);
			rightOrStraight = headingChange <= straight;
		}

		public int numberOfLanes() {
			return outLanes.size();
		}

		public Turn turnType() {
			return turnType;
		}

		public ArrayList<Lane> getOutLanes() {
			return outLanes;
		}

		public int getLinkRank() {
			return linkRank;
		}

		public Link getLink() {
			return link;
		}

		public boolean isRightOrStraight() {
			return rightOrStraight;
		}

		public boolean isUTurn() {
			return uTurn;
		}

	}

	private class NeededConnectingLane {
		private final Lane inLane;
		private final Lane outLane;
		private final int outputLinkIndex;

		private NeededConnectingLane(Lane inlane, Lane outLane, int outputLinkIndex) {
			this.inLane = inlane;
			this.outLane = outLane;
			this.outputLinkIndex = outputLinkIndex;
			System.out.println("Created " + toString());
		}

		@Override
		public String toString() {
			return String.format("NeededConnectingLane from Lane %d (at %s) to Lane %d (at %s) linkRank %d", inLane.getID(),
					inLane.crossSectionElement.getCrossSection().getLink().getToNodeExpand(), outLane.getID(),
					outLane.crossSectionElement.getCrossSection().getLink().getFromNodeExpand(), outputLinkIndex);
		}

	}

}

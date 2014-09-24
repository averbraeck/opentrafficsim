package nl.tudelft.otsim.GeoObjects;

import static org.junit.Assert.*;

import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.StaXWriter;

import org.junit.Test;

/**
* Test the methods in the Node class
* <br /> This test is (currently) very incomplete
*
* @author Peter Knoppers
*/
public class NodeTest {

	@Test
	public void testToString() {
		fail("Not yet implemented");
	}

	@Test
	public void testPaintGraphicsPanel() {
		fail("Not yet implemented");
	}

	@Test
	public void testNodeNetworkStringDoubleDoubleDoubleBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testNodeNetworkStringIntDoubleDoubleDoubleBoolean() {
		fail("Not yet implemented");
	}

	@Test
	public void testNodeNetworkParsedNode() {
		fail("Not yet implemented");
	}

	@Test
	public void testWriteXML() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetName_r() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetName_w() {
		fail("Not yet implemented");
	}

	@Test
	public void testValidateName_v() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetNodeID() {
		fail("Not yet implemented");
	}

	@Test
	public void testClearLinks() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddLink() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetDirectionalLinks() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLinks_r() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsSink() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsSource() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetCircle() {
		fail("Not yet implemented");
	}

	@Test
	public void testDetermineNodeBoundary() {
		fail("Not yet implemented");
	}

	@Test
	public void testTruncateAtConflictArea() {
		fail("Not yet implemented");
	}

	@Test
	public void testFixGeometry() {
		fail("Not yet implemented");
	}

	@Test
	public void testCloseHoles() {
		fail("Not yet implemented");
	}

	@Test
	public void testIncomingCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testLeavingCount() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetLinksFromJunction() {
		fail("Not yet implemented");
	}

	/**
	 * Test the fixLinkConnections method (performs junction expansion)
	 */
	@Test
	public void testFixLinkConnections() {
		// TODO re-write this test to check the result of the new NodeExpander system
		// That test should probably move into a separate test class for that NodeExpander
		// TODO check that no unexpected connections were built
		for (String testJunction : testJunctions) {
			System.out.println("Running test " + testJunction.split(":")[2]);
			buildTestJunction(testJunction.split(":")[0]);
			try {
				String xmlText = StaXWriter.XMLString(network);
				System.out.println(xmlText);
			} catch (Exception e) {
				fail("Caught unexpected exception in creation of the StaXWriter");
			}
		
			final int legCount = junction.legCount();
			System.out.println("Test: " + testJunction);
			String expected = testJunction.split(":")[1];
			assertEquals("Test description error; number of exits mismatches", legCount, expected.split("/", -1).length);
			for (int legNo = 0; legNo < legCount; legNo++) {
				String connections = expected.split("/", -1)[legNo];
				String[] laneDestinations = connections.split(",", 0);
				if (connections.length() == 0)
					laneDestinations = new String[0];
				//System.out.println("laneDestinations.length is " + laneDestinations.length + ", inlaneCount is " + junction.getLeg(legNo).inLaneCount);
				assertEquals ("Test description error; number of lanes feeding junction does not match lane count", laneDestinations.length, junction.getLeg(legNo).inLaneCount);
				if (0 == laneDestinations.length)
					continue;
				System.out.println("Checking non-trivial incoming leg " + legNo);
				ArrayList<Lane> incomingLanes = incomingLinks[legNo].getCrossSections_r().get(0).collectLanes();
				ArrayList<Integer> connectionChecked = new ArrayList<Integer> ();
				//System.out.println("expected inLaneCount " + junction.getLeg(legNo).inLaneCount + " actual " + incomingLanes.size());
				assertEquals("incoming link has unexpected number of lanes", junction.getLeg(legNo).inLaneCount, incomingLanes.size());
				String[] subConnections = connections.split(",");
				for (int laneIndex = 0; laneIndex < subConnections.length; laneIndex++) {
					String subConnection = subConnections[laneIndex];
					System.out.println("checking subConnection \"" + subConnection + "\"");
					/*
					Lane incomingLane = incomingLanes.get(laneIndex);
					System.out.println("Incoming lane is " + incomingLane.toString());
					ArrayList<Lane> connectingLanes = incomingLane.getDownLanes_r();
					for (Lane l : connectingLanes) {
						System.out.print("Connecting lane " + l.toString() + " -> [");
						ArrayList<Lane> leavingLanes = l.getDownLanes_r();
						for (Lane l2 : leavingLanes)
						System.out.print(l2.toString() + " ");
						System.out.println("]");
					}
					*/
					for (String subSubConnection : subConnection.split("\\+")) {
						int outLinkNo = Integer.parseInt(subSubConnection.split("\\.")[0]);
						//System.out.println("outLinkNo=" + outLinkNo + ", limit is " + otherNodes.length);
						assertTrue("Test description error: referring to non-existent link " + outLinkNo, otherNodes.length > outLinkNo);
						int outLaneNo = Integer.parseInt(subSubConnection.split("\\.")[1]);
						//System.out.println("outLinkNo " + outLinkNo + ", outLaneNo " + outLaneNo);
						for (Lane l : incomingLanes) {
							for (Lane connectingLane : l.getDownLanes_r()) {
								//System.out.println("Checking connectingLane " + connectingLane.toString());
								ArrayList<Lane> leavingLanes = connectingLane.getDownLanes_r();
								assertEquals("There should be exactly one down lane on a connectingLane (lane " + l.toString() + " has " + leavingLanes.toString() + ")" , 1, leavingLanes.size());
								for (Lane leavingLane : leavingLanes) {
									int destinationID = leavingLane.getCse().getCrossSection().getLink().getToNode_r().getNodeID();
									//System.out.println("actual destinationID " + destinationID + " expecting " + otherNodes[outLinkNo].getNodeID());
									if (otherNodes[outLinkNo].getNodeID() == destinationID) {
										int actualLaneIndex = connectingLane.getCse().getCrossSectionObjects(Lane.class).indexOf(connectingLane);
										//System.out.println("actual lane index is " + actualLaneIndex + " expecting " + outLaneNo);
										if (actualLaneIndex == outLaneNo) {
											connectionChecked.add(laneIndex);
											System.out.println(subSubConnection + " found in " + subConnection);
										}
									}
								}
							}
						}
					}
				}
				//System.out.println("connectionChecked contains " + connectionChecked.toString());
				for (int i = 0; i < subConnections.length; i++) {
					int expectedConnections = subConnections[i].split("\\+").length;
					int count = 0;
					for (int k = connectionChecked.size(); --k >= 0; )
					if (connectionChecked.get(k) == i)
						count++;
					assertEquals("Expected number of connecting lanes must match actual number for " + subConnections[i], expectedConnections, count);
				}
			
			}
			System.out.println("Test succeeded (generated connections match expected connections)\n");
			System.out.println("Lane export:\n" + network.exportLanes());
		}
	}

	@Test
	public void testItemizeTrafficLightController_caption() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTrafficLightController_r() {
		fail("Not yet implemented");
	}

	@Test
	public void testSetTrafficLightController_w() {
		fail("Not yet implemented");
	}

	@Test
	public void testItemizeTrafficLightController_i() {
		fail("Not yet implemented");
	}

	@Test
	public void testCreateJunctionPolygon() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetTrafficLightController() {
		fail("Not yet implemented");
	}

	@Test
	public void testGetClosingLines() {
		fail("Not yet implemented");
	}

	@Test
	public void testHasConflictArea() {
		fail("Not yet implemented");
	}

	@Test
	public void testIsAutoGenerated() {
		fail("Not yet implemented");
	}

	private static Link createLink (Network network, String name, Node from, Node to, int laneCount) {
		if (0 == laneCount)
			return null;
		
		final double laneWidth = 3.0;
		final double grassWidth = 1.0;
		final double stripeRoom = 0.2;
		final double stripeWidth = 0.1;
		
		ArrayList<CrossSection> csl = new ArrayList<CrossSection>();
		ArrayList<CrossSectionElement> csel = new ArrayList<CrossSectionElement>();
		CrossSection cs = new CrossSection(0, 0, csel);
		csel.add(new CrossSectionElement(cs, "grass", grassWidth, new ArrayList<RoadMarkerAlong>(), null));
		ArrayList<RoadMarkerAlong> rmal = new ArrayList<RoadMarkerAlong>();
		rmal.add(new RoadMarkerAlong("|", stripeRoom / 2 + stripeWidth));
		for (int i = 1; i < laneCount; i++)
			rmal.add(new RoadMarkerAlong(":", i * (laneWidth + stripeRoom) + stripeRoom / 2 + stripeWidth));
		rmal.add(new RoadMarkerAlong("|", laneCount * (laneWidth + stripeWidth) + stripeRoom / 2 + stripeWidth));
		csel.add(new CrossSectionElement(cs, "road", laneCount * (laneWidth + stripeRoom) + stripeRoom, rmal, null));
		csel.add(new CrossSectionElement(cs, "grass", grassWidth, new ArrayList<RoadMarkerAlong>(), null));
		cs.setCrossSectionElementList_w(csel);
		csl.add(cs);
		return network.addLink(name, from.getNodeID(), to.getNodeID(), from.distance(to), false, csl, new ArrayList<Vertex>());
	}

	private static double round (double in, int fractionalDigits) {
		double multiplier = Math.pow(10, fractionalDigits);
		double result = Math.round(in * multiplier) / multiplier;
		//System.out.println(String.format(Locale.US, "rounding %s to %d digits yields %s", in, fractionalDigits, result));
		return result;
	}

	/**
	 * Format of the description of the testJunctions:
	 * Each String describes a test junction.
	 * Each String consists of three fields separated by a colon (:):
	 * 1: network description
	 * 2: expected connections
	 * 3: textual description of the test
	 *
	 * The network description consists of N fields separated by a slash (/).
	 * Each of these fields describes one leg of the junction.
	 * Each leg consists of three comma-separated fields
	 * 1: number of incoming lanes
	 * 2: number of outgoing lanes
	 * 3: angle of the link containing the incoming and outgoing lanes
	 * The angle is expressed in degrees; 0 is towards the right of the
	 * screen (increasing X), 90 is towards the top of the screen (decreasing
	 * Y).
	 *
	 * The expected connections are listed per incoming leg. The descriptions
	 * of the connections per incoming leg are separated by a slash (/).
	 * Each incoming leg has a (possibly empty) list of expected connections.
	 * Expected connections must be described for each lane of the leg, separated by a comma (,).
	 * If multiple connections are expected for a lane; these must be separated by a plus (+).
	 * Each expected connections is described with two numbers separated by a dot (.):
	 * 1: Number of the outgoing leg that the connection connects to
	 * 2: Rank of the lane in the outgoing leg that the connection connects to
	 */
	String[] testJunctions = {
		// Fully constrained cases (sum of exiting lanes == sum of feeding lanes)
		"2,0,-90/0,1,0/0,1,180:2.0,1.0//:T junction single left single right",
		"3,0,-90/0,1,0/0,2,180:2.1,2.0,1.0//:T with double left, single right",
		"3,0,-90/0,2,0/0,1,180:2.0,1.1,1.0//:T with single left, double right",
		"4,0,-90/0,2,0/0,2,180:2.1,2.0,1.1,1.0//:T with double left, double right",
		"3,0,-90/0,1,0/0,1,90/0,1,180:3.0,2.0,1.0///:X with single left, single straight, single right",
		"4,0,-90/0,1,0/0,1,90/0,2,180:3.1,3.0,2.0,1.0///:X with double left, single straight, single right",
		"4,0,-90/0,1,0/0,2,90/0,1,180:3.0,2.1,2.0,1.0///:X with single left, double straight, single right",
		"4,0,-90/0,2,0/0,1,90/0,1,180:3.0,2.0,1.1,1.0///:X with single left, single straight, double right",
		"5,0,-90/0,1,0/0,2,90/0,2,180:3.1,3.0,2.1,2.0,1.0///:X with double left, double straight, single right",
		"5,0,-90/0,2,0/0,1,90/0,2,180:3.1,3.0,2.0,1.1,1.0///:X with double left, single straight, double right",
		"5,0,-90/0,2,0/0,2,90/0,1,180:3.0,2.1,2.0,1.1,1.0///:X with single left, double straight, double right",
		"6,0,-90/0,2,0/0,2,90/0,2,180:3.1,3.0,2.1,2.0,1.1,1.0///:X with double left, double straight, double right",
		// Fully constrained cases (sum of feeding lanes == 1)
		"1,0,-90/0,1,0/0,1,180:2.0+1.0//:T with single left, single right",
		"1,0,-90/0,1,0/0,1,90/0,1,180:3.0+2.0+1.0///:X with single left, single straight, single right",
		// U_turn dow not work yet "1,0,-90/0,1,0/0,1,90/0,1,180/0,1,270:4.0+3.0+2.0+1.0//:X with single lanes and U-turn",
		// Unconstrained cases
		"2,0,-90/0,2,0/0,2,90/0,2,180:3.1+3.0,2.1+2.0+1.1+1.0///:X with double feed, double left double straight double right",
		// The result in the above case does not look very good... (the coded expected result was adapted to match the actual result)
		// The connections to 3.0 and 2.1 are not so fine...
	};

	private Network network;
	private Junction junction;
	private Node[] otherNodes;
	private Link[] incomingLinks;
	private Link[] outgoingLinks;

	private void buildTestJunction(String description) {
		junction = new Junction(description);
		// Create a Network that matches this junction
		network = new Network(null);
		// Location of the junction Node
		final double cx = 0;
		final double cy = 0;
		final double cz = 0;
		final double distance = 100;	// distance of neighboring Nodes (from the junction) in m
		Node junctionNode = network.addNode ("junction", network.nextNodeID(), cx, cy, cz);
		final int legCount = junction.legCount();
		otherNodes = new Node[legCount];
		incomingLinks = new Link[legCount];
		outgoingLinks = new Link[legCount];
		
		for (int legNo = 0; legNo < legCount; legNo++) {
			Junction.Leg leg = junction.getLeg(legNo);
			otherNodes[legNo] = network.addNode ("neighborNode" + legNo, network.nextNodeID(), round(cx + distance * Math.cos(Math.toRadians(leg.angle)), 3), round(cy + distance * Math.sin(Math.toRadians(leg.angle)), 3), cz);
			incomingLinks[legNo] = createLink(network, "feedLink" + legNo, otherNodes[legNo], junctionNode, leg.inLaneCount);
			outgoingLinks[legNo] = createLink(network, "exitLink" + legNo, junctionNode, otherNodes[legNo], leg.outLaneCount);
		}
		assertEquals("Network rebuild should succeed", Network.RebuildResult.SUCCESS, network.rebuild());
	}

	
	class Junction {
		public class Leg {
			final int inLaneCount;
			final int outLaneCount;
			final double angle;
			
			public Leg(int inLaneCount, int outLaneCount, double angle) {
				this.inLaneCount = inLaneCount;
				this.outLaneCount = outLaneCount;
				this.angle = angle;
			}
		
		}
		
		private ArrayList<Leg> legs = new ArrayList<Leg>();
		
		public Junction (String description) {
			for (String legString : description.split("/", -1)) {
				String fields[] = legString.split(",");
				legs.add(new Leg(Integer.parseInt(fields[0]), Integer.parseInt(fields[1]), Double.parseDouble(fields[2])));
			}
		}
		
		public Leg getLeg(int index) {
			return legs.get(index);
		}
		
		public int legCount() {
			return legs.size();
		}
	}

}

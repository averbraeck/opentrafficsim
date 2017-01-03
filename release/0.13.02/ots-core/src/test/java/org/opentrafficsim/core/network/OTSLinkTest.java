package org.opentrafficsim.core.network;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.awt.Color;
import java.rmi.RemoteException;
import java.util.HashMap;
import java.util.Map;

import javax.media.j3d.Bounds;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.eventlists.EventListInterface;
import nl.tudelft.simulation.dsol.experiment.Replication;
import nl.tudelft.simulation.dsol.experiment.ReplicationMode;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.Executable;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.immutablecollections.ImmutableMap;
import nl.tudelft.simulation.immutablecollections.ImmutableSet;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.gtu.plan.tactical.TacticalPlanner;

/**
 * Test the OTSLink class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 3, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class OTSLinkTest implements EventListenerInterface
{
    /** Count GTU_ADD events. */
    private int gtuAddedCount = 0;

    /** Count GTU_REMOVE events. */
    private int gtuRemovedCount = 0;

    /** Count other events. */
    private int otherEventCount = 0;

    /**
     * Test the OTSLink class.
     * @throws NetworkException should not happen uncaught in this test
     * @throws OTSGeometryException should not happen uncaught in this test
     */
    @Test
    public final void testOTSLink() throws NetworkException, OTSGeometryException
    {
        Network network = new OTSNetwork("OTSLinkTestNetwork");
        Node startNode = new OTSNode(network, "start", new OTSPoint3D(10, 20, 0));
        Node endNode = new OTSNode(network, "end", new OTSPoint3D(1000, 2000, 10));
        LinkType linkType = LinkType.ALL;
        OTSLine3D designLine = new OTSLine3D(startNode.getPoint(), endNode.getPoint());
        Map<GTUType, LongitudinalDirectionality> directionalityMap = new HashMap<>();
        OTSLink link = new OTSLink(network, "link", startNode, endNode, linkType, designLine, directionalityMap);
        assertTrue("network contains the newly constructed link", network.containsLink(link));
        assertTrue("our directionality map is stored and returned", directionalityMap.equals(link.getDirectionalityMap()));
        // directionalityMap is currently empty
        assertEquals("directionality for GTUType.ALL is DIR_NONE", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(GTUType.ALL));
        GTUType carType = new GTUType("car", GTUType.VEHICLE);
        link.addDirectionality(carType, LongitudinalDirectionality.DIR_MINUS);
        assertEquals("directionality for carType is DIR_MINUS", LongitudinalDirectionality.DIR_MINUS,
                link.getDirectionality(carType));
        GTUType bicycle = new GTUType("bicycle", GTUType.BIKE);
        assertEquals("directionality for bicycle is DIR_NONE", LongitudinalDirectionality.DIR_NONE,
                link.getDirectionality(bicycle));
        link.addDirectionality(GTUType.ALL, LongitudinalDirectionality.DIR_PLUS);
        assertEquals("directionality for bicycle is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(bicycle));
        link.removeDirectionality(carType);
        assertEquals("directionality for car is now DIR_PLUS", LongitudinalDirectionality.DIR_PLUS,
                link.getDirectionality(carType));
        assertEquals("The link contains no GTUs", 0, link.getGTUCount());
        assertEquals("The link contains zero GTUs", 0, link.getGTUs().size());

        link.addListener(this, Link.GTU_ADD_EVENT);
        link.addListener(this, Link.GTU_REMOVE_EVENT);
        assertEquals("add counter is 0", 0, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        GTU gtu1 = new MyGTU();
        GTU gtu2 = new MyGTU();
        link.addGTU(gtu1);
        assertEquals("add counter is now 1", 1, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertEquals("The link contains one GTU", 1, link.getGTUCount());
        assertEquals("The link contains one GTU", 1, link.getGTUs().size());
        assertEquals("The link contains our GTU", gtu1, link.getGTUs().iterator().next());
        link.addGTU(gtu2);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertTrue("The link contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.addGTU(gtu1); // Add gtu again (should make no difference)
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 0", 0, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertTrue("The link contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu1);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 1", 1, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu1); // removing it again has no effect
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 1", 1, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertTrue("The link contains gtu2", link.getGTUs().contains(gtu2));
        link.removeGTU(gtu2);
        assertEquals("add counter is now 2", 2, this.gtuAddedCount);
        assertEquals("remove counter is 2", 2, this.gtuRemovedCount);
        assertEquals("other event counter is 0", 0, this.otherEventCount);
        assertFalse("The link no longer contains gtu1", link.getGTUs().contains(gtu1));
        assertFalse("The link no longer contains gtu2", link.getGTUs().contains(gtu2));
        assertEquals("Network is correctly returned", network, link.getNetwork());
        assertEquals("LinkType is correctly returned", linkType, link.getLinkType());
        DirectedPoint location = link.getLocation();
        DirectedPoint expectedLocation = designLine.getLocationFraction(0.5);
        assertEquals("location is at halfway point of design line (because design line contains only two points)", 0,
                expectedLocation.distance(location), 0.1);
        // RotZ of location is bogus; makes no sense to test that
        Bounds bounds = link.getBounds();
        assertNotNull("bounds should not be null", bounds);
        assertFalse("link is not equal to null", link.equals(null));
        assertFalse("link is not equal to some other object", link.equals("Hello World!"));
        // Make another link to test the rest of equals
        OTSLink otherLink = new OTSLink(network, "link2", startNode, endNode, linkType, designLine, directionalityMap);
        assertFalse("link is not equal to extremely similar link with different id", link.equals(otherLink));
        // make a link with the same name in another network
        Network otherNetwork = new OTSNetwork("other");
        otherLink =
                new OTSLink(otherNetwork, "link", new OTSNode(otherNetwork, "start", new OTSPoint3D(10, 20, 0)), new OTSNode(
                        otherNetwork, "end", new OTSPoint3D(1000, 2000, 10)), linkType, designLine, directionalityMap);
        assertTrue("link is equal to extremely similar link with same id but different network", link.equals(otherLink));
        otherNetwork.removeLink(otherLink);
        otherLink = link.clone(otherNetwork, new MySim(), false);
        assertTrue("link is equal to clone in different network", link.equals(otherLink));
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        EventType eventType = event.getType();
        if (eventType.equals(Link.GTU_ADD_EVENT))
        {
            this.gtuAddedCount++;
        }
        else if (eventType.equals(Link.GTU_REMOVE_EVENT))
        {
            this.gtuRemovedCount++;
        }
        else
        {
            System.err.println("unhandled event is " + event);
            this.otherEventCount++;
        }
    }

    /**
     * Really simple GTU.
     */
    class MyGTU implements GTU
    {

        /** */
        private static final long serialVersionUID = 1L;

        /** {@inheritDoc} */
        @Override
        public DirectedPoint getLocation() throws RemoteException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Bounds getBounds() throws RemoteException
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType) throws RemoteException
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final boolean weak)
                throws RemoteException
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final short position)
                throws RemoteException
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final short position,
                final boolean weak) throws RemoteException
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeListener(final EventListenerInterface listener, final EventType eventType) throws RemoteException
        {
            return false;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Length getWidth()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Speed getMaximumSpeed()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration getMaximumAcceleration()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration getMaximumDeceleration()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public GTUType getGTUType()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public OTSDEVSSimulatorInterface getSimulator()
        {
            return new MySim();
        }

        /** {@inheritDoc} */
        @Override
        public RelativePosition getReference()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public RelativePosition getFront()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public RelativePosition getRear()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public RelativePosition getCenter()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ImmutableSet<RelativePosition> getContourPoints()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public ImmutableMap<TYPE, RelativePosition> getRelativePositions()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Speed getSpeed()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration getAcceleration()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Length getOdometer()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public BehavioralCharacteristics getBehavioralCharacteristics()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public StrategicalPlanner getStrategicalPlanner()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public TacticalPlanner getTacticalPlanner()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public OperationalPlan getOperationalPlan()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public TurnIndicatorStatus getTurnIndicatorStatus()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void setTurnIndicatorStatus(final TurnIndicatorStatus turnIndicatorStatus) throws GTUException
        {
            // / do nothing
        }

        /** {@inheritDoc} */
        @Override
        public void destroy()
        {
            // do nothing
        }

        /** {@inheritDoc} */
        @Override
        public Color getBaseColor()
        {
            return null;
        }

    }

    /**
     * Dummy simulator.
     */
    class MySim implements OTSDEVSSimulatorInterface
    {

        /** */
        private static final long serialVersionUID = 1L;

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventRel(final Duration relativeDelay, final short priority,
                final Executable executable) throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventRel(final Duration relativeDelay, final Executable executable)
                throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final OTSSimTimeDouble absoluteTime, final short priority,
                final Executable executable) throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final Time absoluteTime, final Executable executable)
                throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final Time absoluteTime, final short priority,
                final Executable executable) throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final OTSSimTimeDouble absoluteTime,
                final Executable executable) throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventNow(final short priority, final Executable executable)
                throws RemoteException, SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventNow(final Executable executable) throws RemoteException,
                SimRuntimeException
        {
            return null;
        }

        @Override
        public void runUpToAndIncluding(final Time when) throws SimRuntimeException, RemoteException
        {
            // do nothing
        }

        @Override
        public boolean isPauseOnError()
        {
            return false;
        }

        @Override
        public void setPauseOnError(final boolean pauseOnError)
        {
            // do nothing
        }

        @Override
        public OTSSimTimeDouble getSimulatorTime()
        {
            return new OTSSimTimeDouble(Time.ZERO);
        }

        @Override
        public void initialize(final Replication<Time, Duration, OTSSimTimeDouble> replication,
                final ReplicationMode replicationMode)
        {
            // do nothing
        }

        @Override
        public boolean isRunning()
        {
            return false;
        }

        @Override
        public void start()
        {
            // do nothing
        }

        @Override
        public void start(final boolean fireStartEvent)
        {
            // do nothing
        }

        @Override
        public void step()
        {
            // do nothing
        }

        @Override
        public void step(final boolean fireStepEvent)
        {
            // do nothing
        }

        @Override
        public void stop()
        {
            // do nothing
        }

        @Override
        public void stop(final boolean fireStopEvent) throws RemoteException, SimRuntimeException
        {
            // do nothing
        }

        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType) throws RemoteException
        {
            return false;
        }

        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final boolean weak)
                throws RemoteException
        {
            return false;
        }

        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final short position)
                throws RemoteException
        {
            return false;
        }

        @Override
        public boolean addListener(final EventListenerInterface listener, final EventType eventType, final short position,
                final boolean weak) throws RemoteException
        {
            return false;
        }

        @Override
        public boolean removeListener(final EventListenerInterface listener, final EventType eventType) throws RemoteException
        {
            return false;
        }

        @Override
        public boolean cancelEvent(final SimEventInterface<OTSSimTimeDouble> event)
        {
            return false;
        }

        @Override
        public EventListInterface<OTSSimTimeDouble> getEventList()
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEvent(final SimEventInterface<OTSSimTimeDouble> event)
                throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventRel(final Duration relativeDelay, final short priority,
                final Object source, final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventRel(final Duration relativeDelay, final Object source,
                final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final OTSSimTimeDouble absoluteTime, final short priority,
                final Object source, final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final Time absoluteTime, final Object source,
                final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final Time absoluteTime, final short priority,
                final Object source, final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventAbs(final OTSSimTimeDouble absoluteTime, final Object source,
                final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventNow(final short priority, final Object source,
                final Object target, final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public SimEventInterface<OTSSimTimeDouble> scheduleEventNow(final Object source, final Object target,
                final String method, final Object[] args) throws SimRuntimeException
        {
            return null;
        }

        @Override
        public void setEventList(final EventListInterface<OTSSimTimeDouble> eventList) throws SimRuntimeException
        {
            // do nothing
        }

        @Override
        public Replication<Time, Duration, OTSSimTimeDouble> getReplication()
        {
            return null;
        }

        @Override
        public void runUpTo(final Time when) throws SimRuntimeException
        {
            // do nothing
        }
    };

}

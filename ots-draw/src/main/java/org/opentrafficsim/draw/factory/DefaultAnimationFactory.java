package org.opentrafficsim.draw.factory;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.rmi.RemoteException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.bounds.Bounds;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point;
import org.djutils.draw.point.Point2d;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.logger.CategoryLogger;
import org.opentrafficsim.core.animation.gtu.colorer.GtuColorer;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuGenerator.GtuGeneratorPosition;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.object.LocatedObject;
import org.opentrafficsim.core.object.NonLocatedObject;
import org.opentrafficsim.draw.core.OtsDrawingException;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.draw.network.LinkAnimation;
import org.opentrafficsim.draw.network.LinkAnimation.LinkData;
import org.opentrafficsim.draw.network.NodeAnimation;
import org.opentrafficsim.draw.network.NodeAnimation.NodeData;
import org.opentrafficsim.draw.road.AbstractLineAnimation.LaneBasedObjectData;
import org.opentrafficsim.draw.road.BusStopAnimation;
import org.opentrafficsim.draw.road.BusStopAnimation.BusStopData;
import org.opentrafficsim.draw.road.ConflictAnimation;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation;
import org.opentrafficsim.draw.road.CrossSectionElementAnimation.CrossSectionElementData;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation;
import org.opentrafficsim.draw.road.GtuGeneratorPositionAnimation.GtuGeneratorPositionData;
import org.opentrafficsim.draw.road.LaneAnimation;
import org.opentrafficsim.draw.road.LaneAnimation.LaneData;
import org.opentrafficsim.draw.road.LaneDetectorAnimation;
import org.opentrafficsim.draw.road.LaneDetectorAnimation.LaneDetectorData;
import org.opentrafficsim.draw.road.SpeedSignAnimation;
import org.opentrafficsim.draw.road.SpeedSignAnimation.SpeedSignData;
import org.opentrafficsim.draw.road.StripeAnimation;
import org.opentrafficsim.draw.road.StripeAnimation.StripeData;
import org.opentrafficsim.draw.road.TrafficLightAnimation;
import org.opentrafficsim.draw.road.TrafficLightAnimation.TrafficLightData;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation;
import org.opentrafficsim.draw.road.TrafficLightDetectorAnimation.TrafficLightDetectorData;
import org.opentrafficsim.road.gtu.generator.LaneBasedGtuGenerator;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.Stripe;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.BusStop;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;
import org.opentrafficsim.road.network.lane.object.SpeedSign;
import org.opentrafficsim.road.network.lane.object.detector.LaneDetector;
import org.opentrafficsim.road.network.lane.object.detector.SinkDetector;
import org.opentrafficsim.road.network.lane.object.detector.TrafficLightDetector;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.Locatable;
import nl.tudelft.simulation.dsol.animation.d2.Renderable2d;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * DefaultAnimationFactory.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DefaultAnimationFactory implements EventListener
{
    /** */
    private static final long serialVersionUID = 20230129L;

    /** The network. */
    private final Network network;

    /** The simulator. */
    private final OtsSimulatorInterface simulator;

    /** GTU colorer. */
    private final GtuColorer gtuColorer;

    /** Rendered gtus. */
    private Map<LaneBasedGtu, Renderable2d<GtuData>> animatedGTUs = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Rendered located objects. */
    private Map<Locatable, Renderable2d<?>> animatedLocatedObjects = Collections.synchronizedMap(new LinkedHashMap<>());

    /** Rendered non-located objects. */
    private Map<NonLocatedObject, Renderable2d<?>> animatedNonLocatedObjects =
            Collections.synchronizedMap(new LinkedHashMap<>());

    /**
     * Creates animations for nodes, links and lanes. The class will subscribe to the network and listen to changes, so the
     * adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @param animateNetwork boolean; whether to animate the current network objects
     * @throws OtsDrawingException on drawing error
     */
    protected DefaultAnimationFactory(final Network network, final GtuColorer gtuColorer, final boolean animateNetwork)
            throws OtsDrawingException
    {
        this.network = network;
        this.simulator = network.getSimulator();
        this.gtuColorer = gtuColorer;

        // subscribe to adding and removing events
        network.addListener(this, Network.GTU_ADD_EVENT);
        network.addListener(this, Network.GTU_REMOVE_EVENT);
        network.addListener(this, Network.OBJECT_ADD_EVENT);
        network.addListener(this, Network.OBJECT_REMOVE_EVENT);
        network.addListener(this, Network.NONLOCATED_OBJECT_ADD_EVENT);
        network.addListener(this, Network.NONLOCATED_OBJECT_REMOVE_EVENT);

        // model the current infrastructure
        try
        {
            if (animateNetwork)
            {
                for (Node node : network.getNodeMap().values())
                {
                    new NodeAnimation(new NodeData()
                    {
                        /** {@inheritDoc} */
                        @Override
                        public Bounds<?, ?, ?> getBounds() throws RemoteException
                        {
                            return node.getBounds();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public String getId()
                        {
                            return node.getId();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public OrientedPoint2d getLocation()
                        {
                            return node.getLocation();
                        }
                    }, this.simulator);
                }
                for (Link link : network.getLinkMap().values())
                {
                    new LinkAnimation(new LinkData()
                    {
                        /** {@inheritDoc} */
                        @Override
                        public Bounds<?, ?, ?> getBounds() throws RemoteException
                        {
                            return link.getBounds();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public String getId()
                        {
                            return link.getId();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public boolean isConnector()
                        {
                            return link.isConnector();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public PolyLine2d getDesignLine()
                        {
                            return link.getDesignLine().getLine2d();
                        }

                        /** {@inheritDoc} */
                        @Override
                        public Point<?> getLocation()
                        {
                            return link.getLocation();
                        }
                    }, this.simulator, 0.5f);
                    if (link instanceof CrossSectionLink)
                    {
                        for (CrossSectionElement element : ((CrossSectionLink) link).getCrossSectionElementList())
                        {
                            if (element instanceof Lane)
                            {
                                Lane lane = (Lane) element;
                                new LaneAnimation(new LaneData()
                                {
                                    /** Contour. */
                                    private List<Point2d> contour = null;

                                    /** {@inheritDoc} */
                                    @Override
                                    public Bounds<?, ?, ?> getBounds() throws RemoteException
                                    {
                                        return lane.getBounds();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public String getId()
                                    {
                                        return lane.getFullId();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public PolyLine2d getCenterLine()
                                    {
                                        return lane.getCenterLine().getLine2d();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public List<Point2d> getContour()
                                    {
                                        if (this.contour == null)
                                        {
                                            // this creates a new list every time, so we cache it
                                            this.contour = lane.getContour().getPointList();
                                        }
                                        return this.contour;
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public Point2d getLocation()
                                    {
                                        return lane.getLocation();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public double getZ() throws RemoteException
                                    {
                                        return -0.0003;
                                    }
                                }, this.simulator, Color.GRAY.brighter());
                            }
                            else if (element instanceof Stripe)
                            {
                                Stripe stripe = (Stripe) element;
                                new StripeAnimation(new StripeData()
                                {
                                    /** {@inheritDoc} */
                                    @Override
                                    public Bounds<?, ?, ?> getBounds() throws RemoteException
                                    {
                                        return stripe.getBounds();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public PolyLine2d getCenterLine()
                                    {
                                        return stripe.getCenterLine().getLine2d();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public Point2d getLocation()
                                    {
                                        return stripe.getLocation();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public Type getType()
                                    {
                                        return Type.valueOf(stripe.getType().name());
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public Length getWidth()
                                    {
                                        return stripe.getWidth(0.5);
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public PolyLine2d getContour()
                                    {
                                        return stripe.getContour();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public double getZ() throws RemoteException
                                    {
                                        return -0.0002;
                                    }
                                }, this.simulator);
                            }
                            else
                            {
                                // formerly the shoulder animation
                                new CrossSectionElementAnimation(new CrossSectionElementData()
                                {
                                    /** Contour. */
                                    private List<Point2d> contour = null;

                                    /** {@inheritDoc} */
                                    @Override
                                    public Bounds<?, ?, ?> getBounds() throws RemoteException
                                    {
                                        return element.getBounds();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public List<Point2d> getContour()
                                    {
                                        if (this.contour == null)
                                        {
                                            // this creates a new list every time, so we cache it
                                            this.contour = element.getContour().getPointList();
                                        }
                                        return this.contour;
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public Point2d getLocation()
                                    {
                                        return element.getLocation();
                                    }

                                    /** {@inheritDoc} */
                                    @Override
                                    public final double getZ()
                                    {
                                        return -0.0004;
                                    }
                                }, this.simulator, Color.DARK_GRAY);
                            }
                        }
                    }
                }
            }

            for (Gtu gtu : network.getGTUs())
            {
                GtuData gtuData = new AnimationGtuData((LaneBasedGtu) gtu);
                Renderable2d<GtuData> gtuAnimation = new DefaultCarAnimation(gtuData, this.simulator);
                this.animatedGTUs.put((LaneBasedGtu) gtu, gtuAnimation);
            }

            for (LocatedObject object : network.getObjectMap().values())
            {
                animateLocatedObject(object);
            }

            for (NonLocatedObject object : network.getNonLocatedObjectMap().values())
            {
                animateNonLocatedObject(object);
            }
        }
        catch (RemoteException | NamingException exception)
        {
            throw new OtsDrawingException("Exception while creating network animation.", exception);
        }

    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is not read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param contextualized Contextualized; context provider
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateNetwork(final Network network, final Contextualized contextualized,
            final GtuColorer gtuColorer) throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, true);
    }

    /**
     * Creates animations for nodes, links, lanes and GTUs. This can be used if the network is read from XML. The class will
     * subscribe to the network and listen to changes, so the adding and removing of GTUs and Objects is animated correctly.
     * @param network Network; the network
     * @param gtuColorer GtuColorer; GTU colorer
     * @return the DefaultAnimationFactory
     * @throws OtsDrawingException on drawing error
     */
    public static DefaultAnimationFactory animateXmlNetwork(final Network network, final GtuColorer gtuColorer)
            throws OtsDrawingException
    {
        return new DefaultAnimationFactory(network, gtuColorer, false);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        try
        {
            if (event.getType().equals(Network.GTU_ADD_EVENT))
            {
                // schedule the addition of the GTU to prevent it from not having an operational plan
                LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU((String) event.getContent());
                this.simulator.scheduleEventNow(this, "animateGTU", new Object[] {gtu});
            }
            else if (event.getType().equals(Network.GTU_REMOVE_EVENT))
            {
                LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU((String) event.getContent());
                if (this.animatedGTUs.containsKey(gtu))
                {
                    this.animatedGTUs.get(gtu).destroy(gtu.getSimulator());
                    this.animatedGTUs.remove(gtu);
                }
            }
            else if (event.getType().equals(Network.OBJECT_ADD_EVENT))
            {
                LocatedObject object = this.network.getObjectMap().get((String) event.getContent());
                animateLocatedObject(object);
            }
            else if (event.getType().equals(Network.OBJECT_REMOVE_EVENT))
            {
                LocatedObject object = this.network.getObjectMap().get((String) event.getContent());
                // TODO: this.animatedObjects.get(object).destroy(object.getSimulator());
                // XXX: this is now a memory leak; we don't expect static animation objects to be removed during the run
                this.animatedLocatedObjects.remove(object);
            }
            else if (event.getType().equals(Network.NONLOCATED_OBJECT_ADD_EVENT))
            {
                NonLocatedObject object = this.network.getNonLocatedObjectMap().get((String) event.getContent());
                animateNonLocatedObject(object);
            }
            else if (event.getType().equals(Network.NONLOCATED_OBJECT_REMOVE_EVENT))
            {
                NonLocatedObject object = this.network.getNonLocatedObjectMap().get((String) event.getContent());
                // TODO: this.animatedObjects.get(object).destroy(object.getSimulator());
                // XXX: this is now a memory leak; we don't expect static animation objects to be removed during the run
                this.animatedNonLocatedObjects.remove(object);
            }
        }
        catch (SimRuntimeException exception)
        {
            CategoryLogger.always().error(exception, "Exception while updating network animation.");
        }
    }

    /**
     * Draw the GTU (scheduled method).
     * @param gtu LaneBasedGtu; the GTU to draw
     */
    protected void animateGTU(final LaneBasedGtu gtu)
    {
        try
        {
            GtuData gtuData = new AnimationGtuData(gtu);
            Renderable2d<GtuData> gtuAnimation = new DefaultCarAnimation(gtuData, this.simulator);
            this.animatedGTUs.put(gtu, gtuAnimation);
        }
        catch (RemoteException | NamingException exception)
        {
            gtu.getSimulator().getLogger().always().error(exception, "Exception while drawing GTU.");
        }
    }

    /**
     * Draw the static object.
     * @param object ObjectInterface; the object to draw
     */
    protected void animateLocatedObject(final LocatedObject object)
    {
        try
        {
            if (object instanceof SinkDetector)
            {
                SinkDetector detector = (SinkDetector) object;
                // Renderable2d<SinkSensor> objectAnimation = new SinkAnimation(detector, this.simulator);
                Renderable2d<LaneDetectorData> objectAnimation =
                        new LaneDetectorAnimation(new AnimationLaneDetectorData(detector), this.simulator, Color.ORANGE);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLightDetector)
            {
                TrafficLightDetector trafficLigthDetector = (TrafficLightDetector) object;
                Renderable2d<TrafficLightDetectorData> objectAnimation =
                        new TrafficLightDetectorAnimation(new TrafficLightDetectorData()
                        {
                            /** {@inheritDoc} */
                            @Override
                            public OrientedPoint2d getLocation()
                            {
                                return trafficLigthDetector.getLocation();
                            }

                            /** {@inheritDoc} */
                            @Override
                            public Bounds<?, ?, ?> getBounds() throws RemoteException
                            {
                                return trafficLigthDetector.getBounds();
                            }

                            /** {@inheritDoc} */
                            @Override
                            public PolyLine2d getGeometry()
                            {
                                return trafficLigthDetector.getGeometry();
                            }

                            /** {@inheritDoc} */
                            @Override
                            public boolean getOccupancy()
                            {
                                return trafficLigthDetector.getOccupancy();
                            }

                            /** {@inheritDoc} */
                            @Override
                            public String getId()
                            {
                                return trafficLigthDetector.getId();
                            }
                        }, this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLightDetector.StartEndDetector)
            {
                // we do not draw these, as we draw the TrafficLightDetector
                return;
            }
            else if (object instanceof LaneDetector)
            {
                LaneDetector detector = (LaneDetector) object;
                Renderable2d<LaneDetectorData> objectAnimation =
                        new LaneDetectorAnimation(new AnimationLaneDetectorData(detector), this.simulator, Color.BLACK);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof Conflict)
            {
                Conflict conflict = (Conflict) object;
                Renderable2d<ConflictData> objectAnimation = new ConflictAnimation(new ConflictData()
                {
                    /** Contour. */
                    private List<Point2d> contour = null;

                    /** {@inheritDoc} */
                    @Override
                    public Length getLaneWidth()
                    {
                        return conflict.getLane().getWidth(conflict.getLongitudinalPosition());
                    }

                    /** {@inheritDoc} */
                    @Override
                    public OrientedPoint2d getLocation()
                    {
                        return conflict.getLocation();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Bounds<?, ?, ?> getBounds() throws RemoteException
                    {
                        return conflict.getBounds();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public String getId()
                    {
                        return conflict.getFullId();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Color getColor()
                    {
                        switch (conflict.conflictPriority())
                        {
                            case SPLIT:
                                return Color.BLUE;
                            case PRIORITY:
                                return Color.GREEN;
                            case YIELD:
                                return Color.ORANGE;
                            default:
                                return Color.RED;
                        }
                    }

                    /** {@inheritDoc} */
                    @Override
                    public List<Point2d> getContour()
                    {
                        if (this.contour == null)
                        {
                            // this creates a new list every time, so we cache it
                            this.contour = conflict.getGeometry().getPointList();
                        }
                        return this.contour;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public boolean isCrossing()
                    {
                        return conflict.getConflictType().isCrossing();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public boolean isPermitted()
                    {
                        return conflict.isPermitted();
                    }
                }, this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof TrafficLight)
            {
                TrafficLight trafficLight = (TrafficLight) object;
                Renderable2d<TrafficLightData> objectAnimation =
                        new TrafficLightAnimation(new AnimationTrafficLightData(trafficLight), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof SpeedSign)
            {
                SpeedSign speedSign = (SpeedSign) object;
                Renderable2d<SpeedSignData> objectAnimation = new SpeedSignAnimation(new SpeedSignData()
                {
                    /** {@inheritDoc} */
                    @Override
                    public Point<?> getLocation() throws RemoteException
                    {
                        return speedSign.getLocation();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Bounds<?, ?, ?> getBounds() throws RemoteException
                    {
                        return speedSign.getBounds();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Speed getSpeed()
                    {
                        return speedSign.getSpeed();
                    }
                }, this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
            else if (object instanceof BusStop)
            {
                BusStop busStop = (BusStop) object;
                Renderable2d<BusStopData> objectAnimation =
                        new BusStopAnimation(new AnimationBusStopData(busStop), this.simulator);
                this.animatedLocatedObjects.put(object, objectAnimation);
            }
        }
        catch (RemoteException | NamingException exception)
        {
            CategoryLogger.always().error(exception, "Exception while drawing Object of class LocatedObject.");
        }
    }

    /**
     * Draw non-located objects.
     * @param object NonLocatedObject; the object to draw.
     */
    protected void animateNonLocatedObject(final NonLocatedObject object)
    {
        try
        {
            if (object instanceof LaneBasedGtuGenerator)
            {
                LaneBasedGtuGenerator generator = (LaneBasedGtuGenerator) object;
                for (GtuGeneratorPosition position : generator.getPositions())
                {
                    Renderable2d<GtuGeneratorPositionData> objectAnimation =
                            new GtuGeneratorPositionAnimation(new GtuGeneratorPositionData()
                            {
                                /** {@inheritDoc} */
                                @Override
                                public Point<?> getLocation() throws RemoteException
                                {
                                    return position.getLocation();
                                }

                                /** {@inheritDoc} */
                                @Override
                                public Bounds<?, ?, ?> getBounds() throws RemoteException
                                {
                                    return position.getBounds();
                                }

                                /** {@inheritDoc} */
                                @Override
                                public int getQueueCount()
                                {
                                    return position.getQueueCount();
                                }
                            }, this.simulator);
                    this.animatedLocatedObjects.put(position, objectAnimation);
                }
            }
        }
        catch (RemoteException | NamingException exception)
        {
            CategoryLogger.always().error(exception, "Exception while drawing Object of class NonLocatedObject.");
        }
    }

    /**
     * Animation data of a LaneBasedGtu.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public class AnimationGtuData implements GtuData
    {
        /** Gtu. */
        private final LaneBasedGtu gtu;

        /**
         * Constructor.
         * @param gtu LaneBasedGtu; GTU.
         */
        public AnimationGtuData(final LaneBasedGtu gtu)
        {
            this.gtu = gtu;
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPoint2d getLocation()
        {
            return this.gtu.getLocation();
        }

        /** {@inheritDoc} */
        @Override
        public Bounds<?, ?, ?> getBounds() throws RemoteException
        {
            return this.gtu.getBounds();
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return this.gtu.getId();
        }

        /** {@inheritDoc} */
        @Override
        public Color getColor()
        {
            return DefaultAnimationFactory.this.gtuColorer.getColor(this.gtu);
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return this.gtu.getLength();
        }

        /** {@inheritDoc} */
        @Override
        public Length getWidth()
        {
            return this.gtu.getWidth();
        }

        /** {@inheritDoc} */
        @Override
        public Length getFront()
        {
            return this.gtu.getFront().getDx();
        }

        /** {@inheritDoc} */
        @Override
        public Length getRear()
        {
            return this.gtu.getRear().getDx();
        }

        /** {@inheritDoc} */
        @Override
        public boolean leftIndicatorOn()
        {
            return this.gtu.getTurnIndicatorStatus().isLeftOrBoth();
        }

        /** {@inheritDoc} */
        @Override
        public boolean rightIndicatorOn()
        {
            return this.gtu.getTurnIndicatorStatus().isRightOrBoth();
        }

        /** {@inheritDoc} */
        @Override
        public RectangularShape getMarker()
        {
            switch (this.gtu.getType().getMarker())
            {
                case CIRCLE:
                    return new Ellipse2D.Double(0, 0, 0, 0);
                case SQUARE:
                    return new Rectangle2D.Double(0, 0, 0, 0);
                default:
                    return new Ellipse2D.Double(0, 0, 0, 0);
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean isBrakingLightsOn()
        {
            return this.gtu.isBrakingLightsOn();
        }

        /**
         * Returns the GTU.
         * @return LaneBasedGtu; GTU.
         */
        public LaneBasedGtu getGtu()
        {
            return this.gtu;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LaneBasedGtu " + this.gtu.getId();
        }
    }

    /**
     * Animation data of a LaneBasedObject.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <T> lane based object type
     */
    private abstract class AnimationLaneBasedObjectData<T extends LaneBasedObject> implements LaneBasedObjectData
    {
        /** Lane based object. */
        private final T laneBasedObject;

        /**
         * Constructor.
         * @param laneBasedObject T; laneBasedObject.
         */
        public AnimationLaneBasedObjectData(final T laneBasedObject)
        {
            this.laneBasedObject = laneBasedObject;
        }

        /** {@inheritDoc} */
        @Override
        public Length getLaneWidth()
        {
            return this.laneBasedObject.getLane().getWidth(this.laneBasedObject.getLongitudinalPosition());
        }

        /** {@inheritDoc} */
        @Override
        public OrientedPoint2d getLocation()
        {
            return this.laneBasedObject.getLocation();
        }

        /** {@inheritDoc} */
        @Override
        public Bounds<?, ?, ?> getBounds() throws RemoteException
        {
            return this.laneBasedObject.getBounds();
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return this.laneBasedObject.getId();
        }

        /**
         * Returns the wrapped object.
         * @return T; wrapped object.
         */
        T getLaneBasedObject()
        {
            return this.laneBasedObject;
        }
    }

    /**
     * Animation data of a LaneDetector.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class AnimationLaneDetectorData extends AnimationLaneBasedObjectData<LaneDetector> implements LaneDetectorData
    {
        /**
         * Constructor.
         * @param laneDetector LaneDetector; lane detector.
         */
        public AnimationLaneDetectorData(final LaneDetector laneDetector)
        {
            super(laneDetector);
        }
    }

    /**
     * Animation data of a BusStop.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class AnimationBusStopData extends AnimationLaneBasedObjectData<BusStop> implements BusStopData
    {
        /**
         * Constructor.
         * @param busStop BusStop; bus stop.
         */
        public AnimationBusStopData(final BusStop busStop)
        {
            super(busStop);
        }
    }

    /**
     * Animation data of a TrafficLight.
     * <p>
     * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class AnimationTrafficLightData extends AnimationLaneBasedObjectData<TrafficLight> implements TrafficLightData
    {
        /**
         * Constructor.
         * @param trafficLight TrafficLight; traffic light.
         */
        public AnimationTrafficLightData(final TrafficLight trafficLight)
        {
            super(trafficLight);
        }

        /** {@inheritDoc} */
        @Override
        public Color getColor()
        {
            switch (getLaneBasedObject().getTrafficLightColor())
            {
                case RED:
                {
                    return Color.RED;
                }
                case GREEN:
                case PREGREEN:
                {
                    return Color.GREEN;
                }
                case YELLOW:
                {
                    return Color.YELLOW;
                }
                default:
                {
                    return Color.BLACK;
                }
            }
        }
    }

}

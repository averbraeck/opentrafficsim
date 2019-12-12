package org.opentrafficsim.demo.network.xml;

import java.awt.Dimension;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformWGS84toRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.demo.network.xml.TestXMLParserAmsterdam.TestXMLModelAmsterdam;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestXMLParserAmsterdam extends OTSSimulationApplication<TestXMLModelAmsterdam>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestXMLParserAmsterdam(final TestXMLModelAmsterdam model, final OTSAnimationPanel animationPanel)
            throws OTSDrawingException
    {
        super(model, animationPanel);
        System.out.println("ANIMATEMAP.SIZE = " + this.defaultAnimationFactory.animatedObjects.size());
    }

    /** */
    private DefaultAnimationFactory defaultAnimationFactory;

    /**
     * Creates the animation objects. This method is overridable. The default uses {@code DefaultAnimationFactory}.
     * @throws OTSDrawingException on animation error
     */
    @Override
    protected void animateNetwork() throws OTSDrawingException
    {
        this.defaultAnimationFactory = DefaultAnimationFactory.animateNetwork(getModel().getNetwork(),
                getModel().getSimulator(), getAnimationPanel().getGTUColorer());
    }
    
    /** {@inheritDoc} */
    @Override
    protected void setAnimationToggles()
    {
        super.setAnimationToggles();
        getAnimationPanel().addAllToggleGISButtonText(" GIS Layers:", getModel().getGisMap(), "Turn GIS map layer on or off");
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OTSAnimator simulator = new OTSAnimator();
                    TestXMLModelAmsterdam xmlModel = new TestXMLModelAmsterdam(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(xmlModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, xmlModel, DEFAULT_COLORER, xmlModel.getNetwork());
                    new TestXMLParserAmsterdam(xmlModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestXMLParserAmsterdam []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestXMLModelAmsterdam extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSRoadNetwork network;

        /** the GIS map. */
        private GisRenderable2D gisMap;

        /**
         * @param simulator the simulator
         */
        TestXMLModelAmsterdam(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            URL xmlURL = URLResource.getResource("/xml/Amsterdam.xml");
            this.network = new OTSRoadNetwork("Amsterdam network", true);
            try
            {
                XmlNetworkLaneParser.build(xmlURL, this.network, getSimulator());
                System.out.println("\nGENERATING CONFLICTS");
                ConflictBuilder.buildConflictsParallel(this.network, this.network.getGtuType(GTUType.DEFAULTS.VEHICLE),
                        this.simulator, new ConflictBuilder.FixedWidthGenerator(new Length(2.0, LengthUnit.SI)));

                System.out.println("OBJECTMAP.SIZE  = " + this.network.getObjectMap().size());

            }
            catch (NetworkException | ParserConfigurationException | SAXException | OTSGeometryException | JAXBException
                    | URISyntaxException | XmlParserException | GTUException exception)
            {
                exception.printStackTrace();
            }

            for (TrafficLight tl : this.network.getObjectMap(TrafficLight.class).values())
            {
                tl.setTrafficLightColor(TrafficLightColor.GREEN);
            }

            URL gisURL = URLResource.getResource("/xml/Amsterdam/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            CoordinateTransform rdto0 = new CoordinateTransformWGS84toRDNew(0, 0);
            this.gisMap = new GisRenderable2D(this.simulator, gisURL, rdto0);
        }

        /**
         * @return gisMap
         */
        public final GisRenderable2D getGisMap()
        {
            return this.gisMap;
        }

        /** {@inheritDoc} */
        @Override
        public OTSRoadNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModelAmsterdam [simulator=" + this.simulator + "]";
        }

    }
}

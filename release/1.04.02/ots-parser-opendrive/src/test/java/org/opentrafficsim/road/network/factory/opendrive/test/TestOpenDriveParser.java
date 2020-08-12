package org.opentrafficsim.road.network.factory.opendrive.test;

import java.awt.Dimension;
import java.io.Serializable;
import java.net.URISyntaxException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.bind.JAXBException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.opendrive.OpenDriveParserException;
import org.opentrafficsim.road.network.factory.opendrive.parser.OpenDriveNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.opentrafficsim.swing.gui.OTSSimulationApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DSOLException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestOpenDriveParser extends OTSSimulationApplication<OTSModelInterface>
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestOpenDriveParser(final OTSModelInterface model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
    }

    /**
     * Main program.
     * @param args String[]; the command line arguments (not used)
     */
    public static void main(final String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    OTSAnimator simulator = new OTSAnimator("TestOpenDriveParser");
                    TestOpenDriveModel openDriveModel = new TestOpenDriveModel(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600.0), openDriveModel);
                    OTSAnimationPanel animationPanel = new OTSAnimationPanel(openDriveModel.getNetwork().getExtent(),
                            new Dimension(800, 600), simulator, openDriveModel, DEFAULT_COLORER, openDriveModel.getNetwork());
                    new TestOpenDriveParser(openDriveModel, animationPanel);
                }
                catch (SimRuntimeException | NamingException | RemoteException | OTSDrawingException | DSOLException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "TestOpenDriveParser []";
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-08-05 15:55:21 +0200 (Wed, 05 Aug 2015) $, @version $Revision: 1199 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class TestOpenDriveModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20150811L;

        /** the network. */
        private OTSRoadNetwork network;

        /**
         * @param simulator the simulator
         */
        TestOpenDriveModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            URL url = URLResource.getResource("/testod.xodr");
            this.simulator.setPauseOnError(false);
            this.network = new OTSRoadNetwork(url.getPath(), true, getSimulator());
            try
            {
                OpenDriveNetworkLaneParser.build(url, this.network, this.simulator);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | GTUException | OTSGeometryException
                    | JAXBException | URISyntaxException | OpenDriveParserException exception)
            {
                exception.printStackTrace();
            }

            /*-
            URL gisURL = URLResource.getResource("/gis/map.xml");
            System.err.println("GIS-map file: " + gisURL.toString());
            
            double latCenter = nlp.getHeaderTag().getOriginLat().si, lonCenter = nlp.getHeaderTag().getOriginLong().si;
            
            CoordinateTransform latLonToXY = new CoordinateTransformLonLatToXY(lonCenter, latCenter);
            new GisRenderable2D(this.simulator, gisURL, latLonToXY);
            */

        }

        /** {@inheritDoc} */
        @Override
        public final OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "TestOpenDriveModel";
        }

        /** {@inheritDoc} */
        @Override
        public Serializable getSourceId()
        {
            return "TestOpenDriveModel";
        }
    }

}

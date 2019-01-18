package org.opentrafficsim.road.network.factory.xml.test;

import java.awt.Dimension;
import java.io.IOException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.DefaultSwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSAnimator;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.draw.core.OTSDrawingException;
import org.opentrafficsim.draw.factory.DefaultAnimationFactory;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.OTSSwingApplication;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.opentrafficsim.swing.gui.OTSAnimationPanel;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestLaneDirections extends OTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 1L;

    /**
     * @param model the model
     * @param animationPanel the animation panel
     * @throws OTSDrawingException on drawing error
     */
    public TestLaneDirections(final OTSModelInterface model, final OTSAnimationPanel animationPanel) throws OTSDrawingException
    {
        super(model, animationPanel);
        DefaultAnimationFactory.animateNetwork(model.getNetwork(), model.getSimulator());
        AnimationToggles.setTextAnimationTogglesStandard(animationPanel);
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
                    TestXMLModel xmlModel = new TestXMLModel(simulator);
                    simulator.initialize(Time.ZERO, Duration.ZERO, Duration.createSI(3600.0), xmlModel);
                    OTSAnimationPanel animationPanel =
                            new OTSAnimationPanel(xmlModel.getNetwork().getExtent(), new Dimension(800, 600), simulator,
                                    xmlModel, new DefaultSwitchableGTUColorer(), xmlModel.getNetwork());
                    new TestLaneDirections(xmlModel, animationPanel);
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
    public String toString()
    {
        return "TestLaneDirections []";
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
    static class TestXMLModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** the network. */
        private OTSNetwork network;

        /**
         * @param simulator the simulator
         */
        TestXMLModel(final OTSSimulatorInterface simulator)
        {
            super(simulator);
        }

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            URL url = URLResource.getResource("/lanedirection.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                this.network = nlp.build(url, true);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException | ValueException | ParameterException exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModel [simulator=" + this.simulator + "]";
        }

    }

}

package org.opentrafficsim.road.network.factory.xml.test;

import java.awt.geom.Rectangle2D;
import java.awt.geom.Rectangle2D.Double;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.naming.Binding;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.event.EventContext;
import javax.swing.SwingUtilities;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.URLResource;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gis.CoordinateTransformWGS84toRDNew;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.swing.gui.AbstractOTSSwingApplication;
import org.opentrafficsim.swing.gui.AnimationToggles;
import org.xml.sax.SAXException;

import nl.javel.gisbeans.io.esri.CoordinateTransform;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.animation.D2.GisRenderable2D;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.naming.context.ContextUtil;

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Oct 17, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class TestXMLParserClone extends AbstractOTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 1L;

    /** the network for cloning. */
    protected OTSNetwork network;

    /** the old simulator. */
    protected SimulatorInterface.TimeDoubleUnit oldSimulator;

    /** the new simulator. */
    protected SimulatorInterface.TimeDoubleUnit newSimulator;

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
                    TestXMLParserClone xmlParserClone = new TestXMLParserClone();
                    // 1 hour simulation run for testing
                    xmlParserClone.oldSimulator = xmlParserClone.buildAnimator(Time.ZERO, Duration.ZERO,
                            new Duration(60.0, DurationUnit.MINUTE), new ArrayList<InputParameter<?>>(), null, true);
                    System.out.println("Number of animation objects before for old sim : "
                            + countNumberAnimationObjects(xmlParserClone.oldSimulator));

                    xmlParserClone.newSimulator = xmlParserClone.buildAnimator(Time.ZERO, Duration.ZERO,
                            new Duration(60.0, DurationUnit.MINUTE), new ArrayList<InputParameter<?>>(), null, true);
                    System.out.println("Number of animation objects after for old sim  : "
                            + countNumberAnimationObjects(xmlParserClone.oldSimulator));
                    System.out.println("Number of animation objects after for new sim  : "
                            + countNumberAnimationObjects(xmlParserClone.newSimulator));
                }
                catch (SimRuntimeException | NamingException | OTSSimulationException | InputParameterException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return "TestXMLModelClone";
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return "TestXMLModelClone";
    }

    /** {@inheritDoc} */
    @Override
    public final void stopTimersThreads()
    {
        super.stopTimersThreads();
    }

    /** {@inheritDoc} */
    @Override
    protected void addAnimationToggles()
    {
        AnimationToggles.setTextAnimationTogglesStandard(this);
    }

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel()
    {
        if (this.network == null)
        {
            return new TestXMLModelParse();
        }
        else
        {
            return new TestXMLModelClone();
        }
    }

    /** {@inheritDoc} */
    @Override
    protected final Double makeAnimationRectangle()
    {
        return new Rectangle2D.Double(102500, 478350, (113100 - 102500), (483280 - 478350));
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "TestXMLParserClone []";
    }

    /**
     * @param simulator the simulator to get the animation objects for
     * @return number of animation objects for this.simulation
     */
    static int countNumberAnimationObjects(SimulatorInterface.TimeDoubleUnit simulator)
    {
        int numberAnimationObjects = 0;
        try
        {
            EventContext context = (EventContext) ContextUtil.lookup(simulator.getReplication().getContext(), "/animation/2D");
            NamingEnumeration<Binding> list = context.listBindings("");
            while (list.hasMore())
            {
                list.next();
                numberAnimationObjects++;
            }
        }
        catch (NamingException exception)
        {
            System.err.println("Error when counting animation objects");
        }
        return numberAnimationObjects;
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestXMLModelParse extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            long millis = System.currentTimeMillis();
            URL url = URLResource.getResource("/N201v8.xml");
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator);
            try
            {
                TestXMLParserClone.this.network = nlp.build(url, true);
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException | ValueException | ParameterException exception)
            {
                exception.printStackTrace();
            }
            System.out.println("parsing took : " + (System.currentTimeMillis() - millis) + " ms");
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return TestXMLParserClone.this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModelParse to clone N201 model [simulator=" + this.simulator + "]";
        }
    }

    /**
     * Model to test the XML parser.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim
     * License</a>.
     * <p>
     * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
     * initial version un 27, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class TestXMLModelClone extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20141121L;

        /** {@inheritDoc} */
        @Override
        public final void constructModel() throws SimRuntimeException
        {
            long millis = System.currentTimeMillis();

            try
            {
                OTSNetwork oldNetwork = TestXMLParserClone.this.network;
                int oldNodes = oldNetwork.getNodeMap().size();
                int oldLinks = oldNetwork.getLinkMap().size();
                OTSNetwork newNetwork =
                        oldNetwork.clone("new N201", TestXMLParserClone.this.oldSimulator, this.simulator, true);
                oldNetwork.destroy(TestXMLParserClone.this.oldSimulator);
                System.out.println("Nodes old = " + oldNodes + " - after delete: " + oldNetwork.getNodeMap().size()
                        + " - new = " + newNetwork.getNodeMap().size());
                System.out.println("Links old = " + oldLinks + " - after delete: " + oldNetwork.getLinkMap().size()
                        + " - new = " + newNetwork.getLinkMap().size());
            }
            catch (NetworkException exception)
            {
                exception.printStackTrace();
            }

            System.out.println("cloning took : " + (System.currentTimeMillis() - millis) + " ms");

            URL gisURL = URLResource.getResource("/N201/map.xml");
            CoordinateTransform rdto0 = new CoordinateTransformWGS84toRDNew(0, 0);
            new GisRenderable2D(this.simulator, gisURL, rdto0);
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return TestXMLParserClone.this.network;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "TestXMLModelClone to clone N201 model [simulator=" + this.simulator + "]";
        }
    }
}

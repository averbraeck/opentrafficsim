package loadfromxml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventProducer;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.animation.GTUColorer;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.simulationengine.AbstractWrappableAnimation;
import org.opentrafficsim.simulationengine.OTSSimulationException;

/**
 * Select a OTS-network XML file, load it and run it.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 21, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LoadXML extends AbstractWrappableAnimation
{

    /** */
    private static final long serialVersionUID = 20170421L;

    /** Name of the XML file. */
    private String fileName = null;

    /** The XML code. */
    private String xml = null;

    /**
     * Load a network from an XML file.
     * @param args String[]; the command line arguments (currently not used)
     * @throws IOException when the file could not be read
     * @throws PropertyException ...
     * @throws OTSSimulationException ...
     * @throws NamingException ...
     * @throws SimRuntimeException ...
     */
    public static void main(final String[] args) throws IOException, SimRuntimeException, NamingException,
            OTSSimulationException, PropertyException
    {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.addChoosableFileFilter(new FileFilter()
        {

            @Override
            public boolean accept(final File f)
            {
                String name = f.getName();
                int length = name.length();
                if (length < 5)
                {
                    return false;
                }
                String type = name.substring(length - 4);
                return type.equalsIgnoreCase(".xml");
            }

            @Override
            public String getDescription()
            {
                return "XML files";
            }
        });
        fileChooser.removeChoosableFileFilter(fileChooser.getAcceptAllFileFilter());
        if (JFileChooser.APPROVE_OPTION != fileChooser.showOpenDialog(null))
        {
            System.out.println("No file chosen; exiting");
            System.exit(0);
        }
        LoadXML loadXML = new LoadXML();
        loadXML.fileName = fileChooser.getSelectedFile().getAbsolutePath();
        loadXML.xml = new String(Files.readAllBytes(Paths.get(loadXML.fileName)));
        loadXML.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(3600, TimeUnit.SI), new ArrayList<Property<?>>(), null,
                true);
    }

    /** {@inheritDoc} */
    @Override
    public final String shortName()
    {
        return this.fileName;
    }

    /** {@inheritDoc} */
    @Override
    public final String description()
    {
        return this.fileName;
    }

    /** Currently active XML model. */
    private XMLModel model = null;

    /** {@inheritDoc} */
    @Override
    protected final OTSModelInterface makeModel(final GTUColorer colorer) throws OTSSimulationException
    {
        this.model = new XMLModel();
        return this.model;
    }

    /**
     * The network.
     */
    class XMLModel extends EventProducer implements OTSModelInterface, EventListenerInterface
    {

        /** */
        private static final long serialVersionUID = 20170421L;

        /** The network. */
        private OTSNetwork network;

        /** The simulator. */
        private SimulatorInterface<Time, Duration, OTSSimTimeDouble> simulator;

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel(final SimulatorInterface<Time, Duration, OTSSimTimeDouble> theSimulator)
                throws SimRuntimeException, RemoteException
        {
            try
            {
                this.simulator = theSimulator;
                XmlNetworkLaneParser nlp = new XmlNetworkLaneParser((OTSDEVSSimulatorInterface) theSimulator);
                this.network = nlp.build(new ByteArrayInputStream(LoadXML.this.xml.getBytes(StandardCharsets.UTF_8)));
            }
            catch (Exception exception)
            {
                exception.printStackTrace();
            }
        }

        /** {@inheritDoc} */
        @Override
        public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator() throws RemoteException
        {
            return this.simulator;
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final EventInterface event) throws RemoteException
        {
            // WIP
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }
}

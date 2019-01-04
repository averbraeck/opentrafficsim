package loadfromxml;

import java.awt.Color;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;

import javax.naming.NamingException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.animation.gtu.colorer.AccelerationGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.GTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.IDGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SpeedGTUColorer;
import org.opentrafficsim.core.animation.gtu.colorer.SwitchableGTUColorer;
import org.opentrafficsim.core.dsol.AbstractOTSModel;
import org.opentrafficsim.core.dsol.OTSSimulationException;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.gtu.colorer.BlockingColorer;
import org.opentrafficsim.road.gtu.colorer.DesiredSpeedColorer;
import org.opentrafficsim.road.gtu.colorer.FixedColor;
import org.opentrafficsim.road.gtu.colorer.GTUTypeColorer;
import org.opentrafficsim.road.gtu.colorer.SplitColorer;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.network.factory.xml.XmlNetworkLaneParser;
import org.opentrafficsim.road.network.lane.conflict.ConflictBuilder;
import org.opentrafficsim.swing.gui.AbstractOTSSwingApplication;
import org.xml.sax.SAXException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameter;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;

/**
 * Select a OTS-network XML file, load it and run it.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 21, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class LoadXML extends AbstractOTSSwingApplication
{
    /** */
    private static final long serialVersionUID = 20170421L;

    /** Name of the XML file. */
    private String fileName = null;

    /** The XML code. */
    private String xml = null;

    /**
     * Load a network from an XML file; program entry point.
     * @param args String[]; the command line arguments; optional name of file to load
     * @throws IOException when the file could not be read
     * @throws InputParameterException should never happen
     * @throws OTSSimulationException when an error occurs during simulation
     * @throws NamingException when a name collision is detected
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args)
            throws IOException, SimRuntimeException, NamingException, OTSSimulationException, InputParameterException
    {
        LaneOperationalPlanBuilder.INSTANT_LANE_CHANGES = true;
        LoadXML loadXML = new LoadXML();
        if (0 == args.length)
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
            loadXML.fileName = fileChooser.getSelectedFile().getAbsolutePath();
        }
        else
        {
            loadXML.fileName = args[0];
        }
        loadXML.xml = new String(Files.readAllBytes(Paths.get(loadXML.fileName)));
        try
        {
            loadXML.buildAnimator(Time.ZERO, Duration.ZERO, new Duration(3600, DurationUnit.SI),
                    new ArrayList<InputParameter<?>>(), null, true);
        }
        catch (SimRuntimeException sre)
        {
            JOptionPane.showMessageDialog(null, sre.getMessage(), "Exception occured", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /** Currently active XML model. */
    private XMLModel model = null;

    /** GTU colorer. */
    private GTUColorer colorer = SwitchableGTUColorer.builder().addActiveColorer(new FixedColor(Color.BLUE, "Blue"))
            .addColorer(GTUTypeColorer.DEFAULT).addColorer(new IDGTUColorer())
            .addColorer(new SpeedGTUColorer(new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new DesiredSpeedColorer(new Speed(50, SpeedUnit.KM_PER_HOUR), new Speed(150, SpeedUnit.KM_PER_HOUR)))
            .addColorer(new AccelerationGTUColorer(Acceleration.createSI(-6.0), Acceleration.createSI(2)))
            .addColorer(new SplitColorer()).addColorer(new BlockingColorer()).build();

    /**
     * The network.
     */
    class XMLModel extends AbstractOTSModel
    {
        /** */
        private static final long serialVersionUID = 20170421L;

        /** The network. */
        private OTSNetwork network;

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public void constructModel()
                throws SimRuntimeException
        {
            XmlNetworkLaneParser nlp = new XmlNetworkLaneParser(this.simulator, getColorer());
            try
            {
                this.network = nlp.build(new ByteArrayInputStream(LoadXML.this.xml.getBytes(StandardCharsets.UTF_8)), false);
                ConflictBuilder.buildConflicts(this.network, GTUType.VEHICLE, this.simulator,
                        new ConflictBuilder.FixedWidthGenerator(Length.createSI(2.0)));
            }
            catch (NetworkException | ParserConfigurationException | SAXException | IOException | NamingException | GTUException
                    | OTSGeometryException | ValueException | ParameterException exception)
            {
                exception.printStackTrace();
                // Abusing the SimRuntimeException to propagate the message to the main method (the problem could actually be a
                // SAXException)
                throw new SimRuntimeException(exception.getMessage());
            }
        }

        /** {@inheritDoc} */
        @Override
        public OTSNetwork getNetwork()
        {
            return this.network;
        }

    }

    /**
     * @return the GTU colorer
     */
    public GTUColorer getColorer()
    {
        return this.colorer;
    }

}

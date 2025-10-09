package org.opentrafficsim.demo.loadfromxml;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsModelInterface;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.demo.DefaultsFactory;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.xml.XmlParserException;
import org.opentrafficsim.road.network.factory.xml.parser.XmlParser;
import org.opentrafficsim.swing.gui.OtsAnimationPanel;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.trafficcontrol.TrafficControlException;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterException;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;
import nl.tudelft.simulation.language.DsolException;

/**
 * Select a OTS-network XML file, load it and run it.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LoadXml extends OtsSimulationApplication<OtsModelInterface>
{
    /** */
    private static final long serialVersionUID = 20170421L;

    /**
     * Constructor.
     * @param model the model
     * @param animationPanel the animation panel
     */
    public LoadXml(final OtsModelInterface model, final OtsAnimationPanel animationPanel)
    {
        // TODO: colorer and markers based on XML
        super(model, animationPanel, DefaultsFactory.GTU_TYPE_MARKERS.toMap());
    }

    /**
     * Load a network from an XML file; program entry point.
     * @param args the command line arguments; optional name of file to load
     * @throws IOException when the file could not be read
     * @throws InputParameterException should never happen
     * @throws NamingException when a name collision is detected
     * @throws SimRuntimeException should never happen
     * @throws DsolException when simulator does not implement AnimatorInterface
     */
    public static void main(final String[] args)
            throws IOException, SimRuntimeException, NamingException, InputParameterException, DsolException
    {
        String fileName;
        String xml;
        if (0 == args.length)
        {
            JFileChooser fileChooser = new JFileChooser();
            fileChooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
            fileChooser.addChoosableFileFilter(new FileFilter()
            {

                @Override
                public boolean accept(final File f)
                {
                    if (f.isDirectory())
                    {
                        return true;
                    }
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
            fileName = fileChooser.getSelectedFile().getAbsolutePath();
        }
        else
        {
            fileName = args[0];
        }
        xml = new String(Files.readAllBytes(Paths.get(fileName)));
        try
        {
            OtsAnimator simulator = new OtsAnimator("LoadXML");
            XmlModel xmlModel = new XmlModel(simulator, "XML model", "Model built from XML file " + fileName, xml);
            Map<String, StreamInterface> map = new LinkedHashMap<>();
            // TODO: This seed is Aimsun specific.
            map.put("generation", new MersenneTwister(6L));
            xmlModel.getStreams().putAll(map);
            simulator.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), xmlModel,
                    new HistoryManagerDevs(simulator, Duration.ofSI(5.0), Duration.ofSI(10.0)));
            OtsAnimationPanel animationPanel = new OtsAnimationPanel(xmlModel.getNetwork().getExtent(), simulator, xmlModel,
                    DEFAULT_GTU_COLORERS, xmlModel.getNetwork());
            animationPanel.enableSimulationControlButtons();
            LoadXml loadXml = new LoadXml(xmlModel, animationPanel);
            // TODO: permabilityType (CAR above) can probably not be null, but we will move stripe type to stripe later
            // (now StripeAnimation.TYPE is figured out from permebability)
        }
        catch (SimRuntimeException sre)
        {
            JOptionPane.showMessageDialog(null, sre.getMessage(), "Exception occured", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
        }
    }

    /**
     * The Model.
     */
    static class XmlModel extends AbstractOtsModel
    {
        /** */
        private static final long serialVersionUID = 20170421L;

        /** The network. */
        private RoadNetwork network;

        /** The XML. */
        private final String xml;

        /**
         * Constructor.
         * @param simulator the simulator
         * @param shortName name of the model
         * @param description description of the model
         * @param xml the XML string
         */
        XmlModel(final OtsSimulatorInterface simulator, final String shortName, final String description, final String xml)
        {
            super(simulator, shortName, description, AbstractOtsModel.defaultInitialStreams());
            this.xml = xml;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            this.network = new RoadNetwork(getShortName(), getSimulator());
            try
            {
                XmlParser xmlParser = new XmlParser(this.network)
                        .setStream(new ByteArrayInputStream(this.xml.getBytes(StandardCharsets.UTF_8)));
                xmlParser.build();
            }
            catch (NetworkException | JAXBException | URISyntaxException | XmlParserException | SAXException
                    | ParserConfigurationException | GtuException | IOException | TrafficControlException exception)
            {
                exception.printStackTrace();
                // Abusing the SimRuntimeException to propagate the message to the main method (the problem could actually be a
                // parsing problem)
                throw new SimRuntimeException(exception.getMessage());
            }
        }

        @Override
        public RoadNetwork getNetwork()
        {
            return this.network;
        }

    }

}

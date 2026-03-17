package org.opentrafficsim.opendrive.parser;

import java.io.IOException;
import java.io.InputStream;
import java.rmi.RemoteException;

import javax.naming.NamingException;
import javax.xml.parsers.ParserConfigurationException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.io.ResourceResolver;
import org.opentrafficsim.core.dsol.AbstractOtsModel;
import org.opentrafficsim.core.dsol.OtsAnimator;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.perception.HistoryManagerDevs;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.swing.gui.OtsSimulationApplication;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.xml.sax.SAXException;

import jakarta.xml.bind.JAXBException;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.DsolException;

/**
 * Loads test XODR file.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class OpenDriveParserTest
{

    /** Empty private constructor. */
    private OpenDriveParserTest()
    {
        //
    }

    /**
     * Tester.
     * @param args arguments
     * @throws SimRuntimeException when e.g., warmupPeriod is larger than runLength
     * @throws NamingException when the context for the replication cannot be created
     * @throws RemoteException when notification of the animation panel fails
     * @throws DsolException when simulator does not implement AnimatorInterface
     */
    public static void main(final String[] args) throws SimRuntimeException, NamingException, RemoteException, DsolException
    {
        OtsAnimator sim = new OtsAnimator("animator");
        OpenDriveModel model = new OpenDriveModel(sim, "/xodr/UC_Motorway-Exit-Entry.xodr");
        sim.initialize(Time.ZERO, Duration.ZERO, Duration.ofSI(3600.0), model,
                new HistoryManagerDevs(sim, Duration.ofSI(5.0), Duration.ofSI(10.0)));

        OtsSimulationPanel animationPanel = new OtsSimulationPanel(model.getNetwork().getExtent(), model.getNetwork());
        OtsSimulationApplication<OpenDriveModel> app = new OtsSimulationApplication<>(model, animationPanel);
        app.setExitOnClose(true);

        animationPanel.enableSimulationControlButtons();
    }

    /**
     * Model.
     */
    public static class OpenDriveModel extends AbstractOtsModel
    {

        /** File. */
        private final String file;

        /** Network. */
        private RoadNetwork network;

        /**
         * Constructor.
         * @param simulator simulator
         * @param file file
         */
        public OpenDriveModel(final OtsSimulatorInterface simulator, final String file)
        {
            super(simulator);
            this.file = file;
        }

        @Override
        public Network getNetwork()
        {
            return this.network;
        }

        @Override
        public void constructModel() throws SimRuntimeException
        {
            try
            {
                InputStream stream = ResourceResolver.resolve(this.file).openStream();
                OpenDriveParser parser = OpenDriveParser.parseStream(stream);
                this.network = new RoadNetwork("roadNetwork", getSimulator());
                parser.build(this.network);
                OpenDriveParser.buildConflicts(this.network);
            }
            catch (NetworkException | IOException | JAXBException | SAXException | ParserConfigurationException ex)
            {
                throw new SimRuntimeException(ex);
            }
        }

    }

}

package org.opentrafficsim.road.network.factory.rti.communication;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.List;

import javax.naming.NamingException;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.rti.data.OTSToRTIData;
import org.opentrafficsim.road.network.factory.rti.data.RTIToOTSData;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

/** */
public class ReceiverThread extends Thread
{

    /** */
    private DatagramSocket Socket;

    /** */
    private byte[] receiveData;

    /** */
    private SubjectiveCar car;

    /** */
    OTSSimulatorInterface simulator;

    /** */
    GTUType carType;

    /** */
    List<LaneBasedIndividualGTU> rtiCars;

    /** */
    OTSRoadNetwork network;

    /**
     * @param simulator OTSSimulatorInterface; the simulator
     * @param carType GTUType; the GTU type
     * @param rtiCars List&lt;LaneBasedIndividualGTU&gt;; the list of cars in the RTI software
     * @param network OTSRoadNetwork; the network
     * @throws SocketException when communication fails
     */
    @SuppressFBWarnings("IL_INFINITE_LOOP")
    public ReceiverThread(OTSSimulatorInterface simulator, GTUType carType, List<LaneBasedIndividualGTU> rtiCars,
            final OTSRoadNetwork network) throws SocketException
    {
        super();
        this.Socket = new DatagramSocket(8090);
        this.receiveData = new byte[1000000];
        this.simulator = simulator;
        this.carType = carType;
        this.rtiCars = rtiCars;
        this.network = network;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressFBWarnings("IL_INFINITE_LOOP")
    public void run()
    {
        while (this.receiveData != null)
        {
            DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);

            try
            {
                this.Socket.receive(receivePacket);
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }

            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));

            RTIToOTSData simData;
            try
            {
                simData = UnPackUDPData.unPack(inputStream);

                // System.out.println("yaw is " + simData.getEgoOri().getYaw() + ", pitch is " + simData.getEgoOri().getPitch()
                // + ", roll is " + simData.getEgoOri().getRoll());
                DirectedPoint position = new DirectedPoint(simData.getEgoPos().getY(), simData.getEgoPos().getX(), 1.0, 0.0,
                        0.0, (Math.PI / 2 - simData.getEgoOri().getYaw()));

                if (this.car == null)
                    this.car = new SubjectiveCar("nissan", this.carType, this.simulator, position, this.network);

                this.car.setPosition(position);

                InetAddress IPAddress = InetAddress.getLocalHost();
                int port = 8091;

                OTSToRTIData data = new OTSToRTIData(this.rtiCars);
                data.setTimeStamp(System.currentTimeMillis());

                byte[] sendData = PackUDPData.pack(data);

                DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, port);
                Socket.send(sendPacket);

                System.out.println(data.getTimeStamp() + " \n");

                data = null;
                sendData = null;
                sendPacket = null;

            }
            catch (IOException | SimRuntimeException | NamingException | GTUException exception)
            {
                exception.printStackTrace();
            }

            // System.out.println(simData);
            try
            {
                inputStream.close();
            }
            catch (IOException exception)
            {
                exception.printStackTrace();
            }

            simData = null;
            receivePacket = null;

            // new Thread(new Responder(socket, packet)).start();
        }
        System.err.println("RECEIVEDATA = NULL - ReceiverThread ended");
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "ReceiverThread [Socket=" + this.Socket + ", car=" + this.car + ", carType=" + this.carType + ", rtiCars.size="
                + this.rtiCars.size() + "]";
    }

}

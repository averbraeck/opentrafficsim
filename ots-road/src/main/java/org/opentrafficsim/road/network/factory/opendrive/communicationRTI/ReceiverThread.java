package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

import javax.naming.NamingException;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.opendrive.data.RTIToOTSData;

/**
 * <br />
 * Copyright (c) 2013-2014 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving. All rights reserved. <br />
 * Some parts of the software (c) 2011-2014 TU Delft, Faculty of TBM, Systems & Simulation <br />
 * This software is licensed without restrictions to Nederlandse Organisatie voor Toegepast Natuurwetenschappelijk
 * Onderzoek TNO (TNO), Erasmus University Rotterdam, Delft University of Technology, Panteia B.V., Stichting Projecten
 * Binnenvaart, Ab Ovo Nederland B.V., Modality Software Solutions B.V., and Rijkswaterstaat - Dienst Water, Verkeer en
 * Leefomgeving, including the right to sub-license sources and derived products to third parties. <br />
 * 
 * @version Mar 24, 2013 <br>
 * @author <a href="http://tudelft.nl/averbraeck">Alexander Verbraeck </a>
 * @version SVN $Revision: 31 $ $Author: averbraeck $
 * @date $Date: 2011-08-15 04:38:04 +0200 (Mon, 15 Aug 2011) $
 **/
public class ReceiverThread implements Runnable
{
    
    /** */
    private DatagramSocket Socket;
    
    /** */
    private byte[] receiveData;
    
    /** */
    private SubjectiveCar car;
    
    /** */
    OTSDEVSSimulatorInterface simulator;
    
    GTUType carType;
    
    /**
     * @param simulator 
     * @param carType 
     * @throws SocketException 
     * 
     */
    public ReceiverThread(OTSDEVSSimulatorInterface simulator, GTUType carType) throws SocketException
    {
        super();
        this.Socket = new DatagramSocket(8090);
        this.receiveData = new byte[1000000];
        this.simulator = simulator;
        this.carType = carType;
    }


    /**
     * @see java.lang.Runnable#run()
     */
    @Override
    public void run()
    {
        while (this.receiveData != null) {
            DatagramPacket receivePacket = new DatagramPacket(this.receiveData, this.receiveData.length);
            
            try
            {
                this.Socket.receive(receivePacket);
            } catch (IOException exception)
            {
                exception.printStackTrace();
            }
            
            DataInputStream inputStream = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));
            
            RTIToOTSData simData;
            try
            {
                simData = UnPackUDPData.unPack(inputStream);
                
                System.out.println("yaw is " + simData.getEgoOri().getYaw() + ", pitch is " + simData.getEgoOri().getPitch() + ", roll is " + simData.getEgoOri().getRoll());
                DirectedPoint position = new DirectedPoint(simData.getEgoPos().getY(), simData.getEgoPos().getX(), 1.0, 0.0,0.0, (Math.PI/2 - simData.getEgoOri().getYaw()));
                
                if(this.car == null)
                    this.car = new SubjectiveCar("nissan", this.carType, this.simulator, position);
                
                this.car.setPosition(position);
                
            } catch (IOException | SimRuntimeException | NetworkException | NamingException exception)
            {
                exception.printStackTrace();
            }                                        
            
            //System.out.println(simData);
            try
            {
                inputStream.close();
            } catch (IOException exception)
            {
                exception.printStackTrace();
            }
            
            simData = null;
            receivePacket = null;
            
            //new Thread(new Responder(socket, packet)).start();
        }
    }

}

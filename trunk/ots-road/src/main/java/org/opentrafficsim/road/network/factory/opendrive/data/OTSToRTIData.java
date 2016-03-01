package org.opentrafficsim.road.network.factory.opendrive.data;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;

/** */
public class OTSToRTIData
{
    /** */
    private long timeStamp;

    /** */
    private int NumCars = 52;

    /** */
    private int NumPedestrians = 12;

    /** */
    private int NumObjects = 11;

    // /trafficState
    /** */
    private List<Position> exoPos = new ArrayList<Position>();

    /** */
    private List<Orientation> exoOri = new ArrayList<Orientation>();

    /** */
    private List<Velocity> exoVel = new ArrayList<Velocity>();// global reference frame

    // /padestrianState
    /** */
    private List<Position> pedPos = new ArrayList<Position>();

    /** */
    private List<Orientation> pedOri = new ArrayList<Orientation>();

    /** */
    private List<Velocity> pedVel = new ArrayList<Velocity>();// global reference frame

    // /objectState
    /** */
    private List<Position> objPos = new ArrayList<Position>();

    /** */
    private List<Orientation> objOri = new ArrayList<Orientation>();

    /** */
    private List<Velocity> objVel = new ArrayList<Velocity>();// global reference frame

    /**
     * @param rtiCars cars to prepare for RTI software
     * @throws RemoteException on unability to retrieve the location of one of the cars
     */
    public OTSToRTIData(List<LaneBasedIndividualGTU> rtiCars) throws RemoteException
    {
        // this.setTimeStamp(System.currentTimeMillis());

        for (int i = 0; i < this.NumCars; i++)
        {
            Position position =
                new Position(rtiCars.get(i).getLocation().getY(), rtiCars.get(i).getLocation().getX(), 0.15);
            this.exoPos.add(position);

            Orientation orientation =
                new Orientation(rtiCars.get(i).getLocation().getRotX(), rtiCars.get(i).getLocation().getRotY(), Math.PI
                    / 2 - rtiCars.get(i).getLocation().getRotZ());
            this.exoOri.add(orientation);

            Velocity vel = new Velocity();
            this.exoVel.add(vel);
        }

        for (int i = 0; i < this.NumPedestrians; i++)
        {
            Position position = new Position();
            this.pedPos.add(position);

            Orientation orientation = new Orientation();
            this.pedOri.add(orientation);

            Velocity vel = new Velocity();
            this.pedVel.add(vel);
        }

        for (int i = 0; i < this.NumObjects; i++)
        {
            Position position = new Position();
            this.objPos.add(position);

            Orientation orientation = new Orientation();
            this.objOri.add(orientation);

            Velocity vel = new Velocity();
            this.objVel.add(vel);
        }
    }

    /**
     * 
     */
    public OTSToRTIData()
    {
    }

    /**
     * @return exoPos
     */
    public List<Position> getExoPos()
    {
        return this.exoPos;
    }

    /**
     * @return exoOri
     */
    public List<Orientation> getExoOri()
    {
        return this.exoOri;
    }

    /**
     * @return exoVel
     */
    public List<Velocity> getExoVel()
    {
        return this.exoVel;
    }

    /**
     * @return pedPos
     */
    public List<Position> getPedPos()
    {
        return this.pedPos;
    }

    /**
     * @return pedOri
     */
    public List<Orientation> getPedOri()
    {
        return this.pedOri;
    }

    /**
     * @return pedVel
     */
    public List<Velocity> getPedVel()
    {
        return this.pedVel;
    }

    /**
     * @return objPos
     */
    public List<Position> getObjPos()
    {
        return this.objPos;
    }

    /**
     * @return objOri
     */
    public List<Orientation> getObjOri()
    {
        return this.objOri;
    }

    /**
     * @return objVel
     */
    public List<Velocity> getObjVel()
    {
        return this.objVel;
    }

    /**
     * @return numCars
     */
    public int getNumCars()
    {
        return this.NumCars;
    }

    /**
     * @param numCars set numCars
     */
    public void setNumCars(int numCars)
    {
        this.NumCars = numCars;
    }

    /**
     * @return numPedestrians
     */
    public int getNumPedestrians()
    {
        return this.NumPedestrians;
    }

    /**
     * @param numPedestrians set numPedestrians
     */
    public void setNumPedestrians(int numPedestrians)
    {
        this.NumPedestrians = numPedestrians;
    }

    /**
     * @return numObjects
     */
    public int getNumObjects()
    {
        return this.NumObjects;
    }

    /**
     * @param numObjects set numObjects
     */
    public void setNumObjects(int numObjects)
    {
        this.NumObjects = numObjects;
    }

    /**
     * @return timeStamp
     */
    public long getTimeStamp()
    {
        return this.timeStamp;
    }

    /**
     * @param timeStamp set timeStamp
     */
    public void setTimeStamp(long timeStamp)
    {
        this.timeStamp = timeStamp;
    }

}

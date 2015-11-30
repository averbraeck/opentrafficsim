package org.opentrafficsim.road.network.factory.opendrive.data;

import java.util.ArrayList;
import java.util.List;

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
    

    ///trafficState
    /** */
    private List<Position> exoPos = new ArrayList<Position>();
    
    /** */
    private List<Orientation> exoOri = new ArrayList<Orientation>();
    /** */
    private List<Velocity> exoVel = new ArrayList<Velocity>();//global reference frame

    
    ///padestrianState
    /** */
    private List<Position> pedPos = new ArrayList<Position>();
    /** */
    private List<Orientation> pedOri = new ArrayList<Orientation>();
    /** */
    private List<Velocity> pedVel = new ArrayList<Velocity>();//global reference frame

    
    ///objectState
    /** */
    private List<Position> objPos = new ArrayList<Position>();
    /** */
    private List<Orientation> objOri = new ArrayList<Orientation>();
    /** */
    private List<Velocity> objVel = new ArrayList<Velocity>();//global reference frame    
    
            
    /**
     * @param timeStamp
     */
    public OTSToRTIData()
    {
        //this.setTimeStamp(System.currentTimeMillis());
        
        for(int i = 0; i<this.NumCars; i++)
        {
            Position position = new Position();
            this.exoPos.add(position);
            
            Orientation orientation = new Orientation();
            this.exoOri.add(orientation);
            
            Velocity vel = new Velocity();
            this.exoVel.add(vel);
        }
        
        for(int i = 0; i<this.NumPedestrians; i++)
        {
            Position position = new Position();
            this.pedPos.add(position);
            
            Orientation orientation = new Orientation();
            this.pedOri.add(orientation);
            
            Velocity vel = new Velocity();
            this.pedVel.add(vel);
        }
        
        for(int i = 0; i<this.NumObjects; i++)
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

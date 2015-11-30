package org.opentrafficsim.road.network.factory.opendrive.data;

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
public class RTIToOTSData
{    
    /** time stamp*/
    private float timeStamp;//int32_t64 Hans suggests
    
    /** */
    private float deltaT;
    
    //my vehicle state
    /** */
    private Position  egoPos;  // position[m]
    /** */
    private Orientation egoOri;
    /** */
    private Velocity  egoVel; // local reference frame
    /** */
    private AngularVel egoAngVel;

   
    /** */
    private int intersection_type;
    /** */
    private int intersection_phase;
    /** */
    private float intersection_distance;
    
    
    /**
     * 
     */
    public RTIToOTSData()
    {

        this.setEgoPos(new Position());  // position[m]

        this.setEgoOri(new Orientation());

        this.setEgoVel(new Velocity()); // local reference frame

        this.setEgoAngVel(new AngularVel());
    }

    /**
     * @see java.lang.Object#toString()
     */    
    @Override
    public String toString()
    {
        return "RTIToOTSData [timeStamp=" + this.getTimeStamp() + "\ndeltaT=" + this.getDeltaT() + "\negoPos=" + this.getEgoPos()
                + "\negoOri=" + this.getEgoOri() + "\negoVel=" + this.getEgoVel() + "\negoAngVel=" + this.getEgoAngVel()
                + "\nintersection_type=" + this.getIntersection_type() + "\nintersection_phase=" + this.getIntersection_phase()
                + "\nintersection_distance=" + this.getIntersection_distance() + "]";
    }
    
    /**
     * @return timeStamp
     */
    public float getTimeStamp()
    {
        return this.timeStamp;
    }
    
    /**
     * @param timeStamp set timeStamp
     */
    public void setTimeStamp(float timeStamp)
    {
        this.timeStamp = timeStamp;
    }
    
    /**
     * @return deltaT
     */
    public float getDeltaT()
    {
        return this.deltaT;
    }
    
    /**
     * @param deltaT set deltaT
     */
    public void setDeltaT(float deltaT)
    {
        this.deltaT = deltaT;
    }
    
    /**
     * @return intersection_type
     */
    public int getIntersection_type()
    {
        return this.intersection_type;
    }
    
    /**
     * @param intersection_type set intersection_type
     */
    public void setIntersection_type(int intersection_type)
    {
        this.intersection_type = intersection_type;
    }
    
    /**
     * @return intersection_phase
     */
    public int getIntersection_phase()
    {
        return this.intersection_phase;
    }
    
    /**
     * @param intersection_phase set intersection_phase
     */
    public void setIntersection_phase(int intersection_phase)
    {
        this.intersection_phase = intersection_phase;
    }
    
    /**
     * @return intersection_distance
     */
    public float getIntersection_distance()
    {
        return this.intersection_distance;
    }
    
    /**
     * @param intersection_distance set intersection_distance
     */
    public void setIntersection_distance(float intersection_distance)
    {
        this.intersection_distance = intersection_distance;
    }

    /**
     * @return egoPos
     */
    public Position getEgoPos()
    {
        return this.egoPos;
    }

    /**
     * @param egoPos set egoPos
     */
    public void setEgoPos(Position egoPos)
    {
        this.egoPos = egoPos;
    }

    /**
     * @return egoOri
     */
    public Orientation getEgoOri()
    {
        return this.egoOri;
    }

    /**
     * @param egoOri set egoOri
     */
    public void setEgoOri(Orientation egoOri)
    {
        this.egoOri = egoOri;
    }

    /**
     * @return egoVel
     */
    public Velocity getEgoVel()
    {
        return this.egoVel;
    }

    /**
     * @param egoVel set egoVel
     */
    public void setEgoVel(Velocity egoVel)
    {
        this.egoVel = egoVel;
    }

    /**
     * @return egoAngVel
     */
    public AngularVel getEgoAngVel()
    {
        return this.egoAngVel;
    }

    /**
     * @param egoAngVel set egoAngVel
     */
    public void setEgoAngVel(AngularVel egoAngVel)
    {
        this.egoAngVel = egoAngVel;
    }
        
}



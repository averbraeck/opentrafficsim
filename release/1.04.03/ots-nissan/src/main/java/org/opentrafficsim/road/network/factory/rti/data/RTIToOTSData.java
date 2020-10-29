package org.opentrafficsim.road.network.factory.rti.data;

import java.io.Serializable;

/** */
public class RTIToOTSData implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** Time stamp. */
    private float timeStamp;// int32_t64 Hans suggests

    /** */
    private float deltaT;

    // My vehicle state
    /** */
    private Position egoPos; // position[m]

    /** */
    private Orientation egoOri;

    /** */
    private Velocity egoVel; // local reference frame

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

        this.setEgoPos(new Position()); // position[m]

        this.setEgoOri(new Orientation());

        this.setEgoVel(new Velocity()); // local reference frame

        this.setEgoAngVel(new AngularVel());
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "RTIToOTSData [timeStamp=" + this.getTimeStamp() + "\ndeltaT=" + this.getDeltaT() + "\negoPos="
                + this.getEgoPos() + "\negoOri=" + this.getEgoOri() + "\negoVel=" + this.getEgoVel() + "\negoAngVel="
                + this.getEgoAngVel() + "\nintersection_type=" + this.getIntersection_type() + "\nintersection_phase="
                + this.getIntersection_phase() + "\nintersection_distance=" + this.getIntersection_distance() + "]";
    }

    /**
     * @return timeStamp
     */
    public float getTimeStamp()
    {
        return this.timeStamp;
    }

    /**
     * @param timeStamp float; set timeStamp
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
     * @param deltaT float; set deltaT
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
     * @param intersection_type int; set intersection_type
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
     * @param intersection_phase int; set intersection_phase
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
     * @param intersection_distance float; set intersection_distance
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
     * @param egoPos Position; set egoPos
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
     * @param egoOri Orientation; set egoOri
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
     * @param egoVel Velocity; set egoVel
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
     * @param egoAngVel AngularVel; set egoAngVel
     */
    public void setEgoAngVel(AngularVel egoAngVel)
    {
        this.egoAngVel = egoAngVel;
    }

}

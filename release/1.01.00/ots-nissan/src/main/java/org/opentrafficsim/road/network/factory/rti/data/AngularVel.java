package org.opentrafficsim.road.network.factory.rti.data;

import java.io.Serializable;

/** */
public class AngularVel implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** */
    private float yawRate; // position[m]

    /** */
    private float pitchRate; // position[m]

    /** */
    private float rollRate; // position[m]

    /** {@inheritDoc} */
    public String toString()
    {
        return "AngularVel [yawRate=" + this.getYawRate() + ", pitchRate=" + this.getPitchRate() + ", rollRate="
                + this.getRollRate() + "]";
    }

    /**
     * @return yawRate
     */
    public float getYawRate()
    {
        return this.yawRate;
    }

    /**
     * @param yawRate float; set yawRate
     */
    public void setYawRate(float yawRate)
    {
        this.yawRate = yawRate;
    }

    /**
     * @return pitchRate
     */
    public float getPitchRate()
    {
        return this.pitchRate;
    }

    /**
     * @param pitchRate float; set pitchRate
     */
    public void setPitchRate(float pitchRate)
    {
        this.pitchRate = pitchRate;
    }

    /**
     * @return rollRate
     */
    public float getRollRate()
    {
        return this.rollRate;
    }

    /**
     * @param rollRate float; set rollRate
     */
    public void setRollRate(float rollRate)
    {
        this.rollRate = rollRate;
    }

}

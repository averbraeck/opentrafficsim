package org.opentrafficsim.road.network.factory.opendrive.data;

/** */
public class AngularVel
{
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
     * @param yawRate set yawRate
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
     * @param pitchRate set pitchRate
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
     * @param rollRate set rollRate
     */
    public void setRollRate(float rollRate)
    {
        this.rollRate = rollRate;
    }

}

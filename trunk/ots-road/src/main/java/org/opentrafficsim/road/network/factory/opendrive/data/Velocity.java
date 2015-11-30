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
public class Velocity
{
    /** */
    private float  vx;  // position[m]
    
    /** */
    private float  vy;  // position[m]
    
    /** */
    private float  vz;  // position[m]

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Velocity [vx=" + this.getVx() + ", vy=" + this.getVy() + ", vz=" + this.getVz() + "]";
    }

    /**
     * @return vx
     */
    public float getVx()
    {
        return this.vx;
    }

    /**
     * @param vx set vx
     */
    public void setVx(float vx)
    {
        this.vx = vx;
    }

    /**
     * @return vy
     */
    public float getVy()
    {
        return this.vy;
    }

    /**
     * @param vy set vy
     */
    public void setVy(float vy)
    {
        this.vy = vy;
    }

    /**
     * @return vz
     */
    public float getVz()
    {
        return this.vz;
    }

    /**
     * @param vz set vz
     */
    public void setVz(float vz)
    {
        this.vz = vz;
    }
    
    
}

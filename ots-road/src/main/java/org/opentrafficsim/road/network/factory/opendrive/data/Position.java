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
public class Position
{
    /** */
    private float  x;  // position[m]
    
    /** */
    private float  y;  // position[m]
    
    /** */
    private float  z;  // position[m]

    /**
     * @param y2
     * @param x2
     * @param z2
     */
    public Position(double y2, double x2, double z2)
    {
        this.setX((float) y2);
        this.setY((float) x2);
        this.setZ((float) z2);
    }

    /**
     * 
     */
    public Position()
    {
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "Position [x=" + this.getX() + ", y=" + this.getY() + ", z=" + this.getZ() + "]";
    }

    /**
     * @return x
     */
    public float getX()
    {
        return this.x;
    }

    /**
     * @param x set x
     */
    public void setX(float x)
    {
        this.x = x;
    }

    /**
     * @return y
     */
    public float getY()
    {
        return this.y;
    }

    /**
     * @param y set y
     */
    public void setY(float y)
    {
        this.y = y;
    }

    /**
     * @return z
     */
    public float getZ()
    {
        return this.z;
    }

    /**
     * @param z set z
     */
    public void setZ(float z)
    {
        this.z = z;
    }
    
    
}

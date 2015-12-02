package org.opentrafficsim.road.network.factory.opendrive.communicationRTI;

import java.rmi.RemoteException;
import java.util.Map;

import javax.media.j3d.Bounds;
import javax.naming.NamingException;
import javax.vecmath.Point3d;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.language.d3.BoundingBox;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.DrivingCharacteristics;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.Perception;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.plan.strategical.StrategicalPlanner;
import org.opentrafficsim.core.network.NetworkException;

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
public class SubjectiveCar extends AbstractGTU
{

    /** */
    private static final long serialVersionUID = 1L;

    /** */
    private DirectedPoint position = null;
    
    private Length.Rel length;
    
    private Length.Rel width;

    /**
     * @param id
     * @param type
     * @param simulator
     * @param initialLocation
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws NamingException 
     * @throws RemoteException 
     */
    public SubjectiveCar(String id, GTUType type, OTSDEVSSimulatorInterface simulator, DirectedPoint initialLocation)
            throws SimRuntimeException, NetworkException, RemoteException, NamingException
    {
        super(id, type, simulator, null, null, null);
        this.position = initialLocation;
        System.out.println("Subjective car created at position " + this.position);
        
        this.length = new Length.Rel(4.0, LengthUnit.METER);
        this.width = new Length.Rel(2.0, LengthUnit.METER);
        
        new SubjectiveCarAnimation(this, simulator);
    }

    /**
     * @param id
     * @param gtuType
     * @param simulator
     * @param strategicalPlanner
     * @param perception
     * @param initialLocation
     * @throws SimRuntimeException
     * @throws NetworkException
     */
    public SubjectiveCar(String id, GTUType gtuType, OTSDEVSSimulatorInterface simulator,
            StrategicalPlanner strategicalPlanner, Perception perception, DirectedPoint initialLocation)
            throws SimRuntimeException, NetworkException
    {
        super(id, gtuType, simulator, strategicalPlanner, perception, initialLocation);
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getLength()
     */
    @Override
    public Rel getLength()
    {
        return this.length;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getWidth()
     */
    @Override
    public Rel getWidth()
    {
        return this.width;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getMaximumVelocity()
     */
    @Override
    public Speed getMaximumVelocity()
    {
        return null;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getFront()
     */
    @Override
    public RelativePosition getFront()
    {
        return null;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getRear()
     */
    @Override
    public RelativePosition getRear()
    {
        return null;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getRelativePositions()
     */
    @Override
    public Map<TYPE, RelativePosition> getRelativePositions()
    {
        return null;
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#destroy()
     */
    @Override
    public void destroy()
    {
    }

    /**
     * @see org.opentrafficsim.core.gtu.GTU#getDrivingCharacteristics()
     */
    @Override
    public DrivingCharacteristics getDrivingCharacteristics()
    {
        return null;
    }

    /**
     * @see nl.tudelft.simulation.dsol.animation.LocatableInterface#getBounds()
     */
    @Override
    public Bounds getBounds() throws RemoteException
    {
        double dx = 0.5 * getLength().doubleValue();
        double dy = 0.5 * getWidth().doubleValue();
        return new BoundingBox(new Point3d(-dx, -dy, 0.0), new Point3d(dx, dy, 0.0));
    }

    /**
     * @return position
     */
    public DirectedPoint getPosition()
    {
        return this.position;
    }

    /**
     * @param position set position
     */
    public void setPosition(DirectedPoint position)
    {
        this.position = position;
    }

    @Override
    public DirectedPoint getLocation() throws RemoteException
    {
        //System.out.println("Subjective car at position " + this.position);

        return this.getPosition();
    }

}

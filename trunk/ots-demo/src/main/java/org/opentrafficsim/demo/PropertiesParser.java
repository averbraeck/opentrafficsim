package org.opentrafficsim.demo;

import java.util.List;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.modelproperties.CompoundProperty;
import org.opentrafficsim.base.modelproperties.Property;
import org.opentrafficsim.base.modelproperties.PropertyException;
import org.opentrafficsim.base.modelproperties.SelectionProperty;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedGTUFollowingTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIDM;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lmrs.LMRSFactory;
import org.opentrafficsim.road.gtu.lane.tactical.toledo.ToledoFactory;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlannerFactory;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlannerFactory;
import org.opentrafficsim.road.modelproperties.IDMPropertySet;

/**
 * Utilities for demos, e.g. parsing.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 7, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class PropertiesParser
{
    /** */
    private PropertiesParser()
    {
        // private constructor; do not instantiate class
    }

    /**
     * Get the car following model for a GTU type, e.g. "Car" or "Truck" from the properties.
     * @param properties the properties to parse
     * @param gtuType the type of GTU, e.g. "Car" or "Truck"
     * @return GTUFollowingModelOld; the car following model
     * @throws PropertyException in case parsing fails
     */
    public static GTUFollowingModelOld parseGTUFollowingModelOld(final List<Property<?>> properties, final String gtuType)
            throws PropertyException
    {
        // Get car-following model name
        String carFollowingModelName = null;
        CompoundProperty propertyContainer = new CompoundProperty("", "", "", properties, false, 0);
        Property<?> cfmp = propertyContainer.findByKey("CarFollowingModel");
        if (null == cfmp)
        {
            throw new PropertyException("Cannot find \"Car following model\" property");
        }
        if (cfmp instanceof SelectionProperty)
        {
            carFollowingModelName = ((SelectionProperty) cfmp).getValue();
        }
        else
        {
            throw new Error("\"Car following model\" property has wrong type");
        }

        // Get car-following model parameter
        for (Property<?> ap : new CompoundProperty("", "", "", properties, false, 0))
        {
            if (ap instanceof CompoundProperty)
            {
                CompoundProperty cp = (CompoundProperty) ap;
                if (ap.getKey().contains("IDM"))
                {
                    Acceleration a = IDMPropertySet.getA(cp);
                    Acceleration b = IDMPropertySet.getB(cp);
                    Length s0 = IDMPropertySet.getS0(cp);
                    Duration tSafe = IDMPropertySet.getTSafe(cp);
                    GTUFollowingModelOld gtuFollowingModel = null;
                    if (carFollowingModelName.equals("IDM"))
                    {
                        gtuFollowingModel = new IDMOld(a, b, s0, tSafe, 1.0);
                    }
                    else if (carFollowingModelName.equals("IDM+"))
                    {
                        gtuFollowingModel = new IDMPlusOld(a, b, s0, tSafe, 1.0);
                    }
                    else
                    {
                        throw new PropertyException("Unknown gtu following model: " + carFollowingModelName);
                    }
                    if (ap.getKey().contains(gtuType))
                    {
                        return gtuFollowingModel;
                    }
                }
            }
        }
        throw new PropertyException("Cannot determine GTU following model for GTU type " + gtuType);
    }

    /**
     * Get the lane change model from the properties.
     * @param properties the properties to parse
     * @return LaneChangeModel; the lane change model
     * @throws PropertyException in case parsing fails
     */
    public static LaneChangeModel parseLaneChangeModel(final List<Property<?>> properties) throws PropertyException
    {
        CompoundProperty propertyContainer = new CompoundProperty("", "", "", properties, false, 0);
        Property<?> cfmp = propertyContainer.findByKey("LaneChanging");
        if (null == cfmp)
        {
            throw new PropertyException("Cannot find \"Lane changing\" property");
        }
        if (cfmp instanceof SelectionProperty)
        {
            String laneChangeModelName = ((SelectionProperty) cfmp).getValue();
            if ("Egoistic".equals(laneChangeModelName))
            {
                return new Egoistic();
            }
            else if ("Altruistic".equals(laneChangeModelName))
            {
                return new Altruistic();
            }
            throw new PropertyException("Lane changing " + laneChangeModelName + " not implemented");
        }
        throw new PropertyException("\"Lane changing\" property has wrong type");
    }

    /**
     * Get the strategical planner factory from the properties.
     * @param properties the properties to parse
     * @param gtuFollowingModel the car following model in case it is needed
     * @param laneChangeModel the lane change model in case it is needed
     * @return LaneBasedStrategicalPlannerFactory; the tactical planner factory
     * @throws PropertyException in case parsing fails
     * @throws GTUException in case LMRS Factory cannot be created
     */
    public static LaneBasedStrategicalPlannerFactory<LaneBasedStrategicalPlanner> parseStrategicalPlannerFactory(
            final List<Property<?>> properties, final GTUFollowingModelOld gtuFollowingModel,
            final LaneChangeModel laneChangeModel) throws PropertyException, GTUException
    {
        for (Property<?> ap : new CompoundProperty("", "", "", properties, false, 0))
        {
            if (ap instanceof SelectionProperty)
            {
                SelectionProperty sp = (SelectionProperty) ap;
                if ("TacticalPlanner".equals(sp.getKey()))
                {
                    String tacticalPlannerName = sp.getValue();
                    if ("MOBIL".equals(tacticalPlannerName))
                    {
                        return new LaneBasedStrategicalRoutePlannerFactory(
                                new LaneBasedGTUFollowingTacticalPlannerFactory(gtuFollowingModel));
                    }
                    else if ("MOBIL/LC".equals(tacticalPlannerName))
                    {
                        return new LaneBasedStrategicalRoutePlannerFactory(
                                new LaneBasedCFLCTacticalPlannerFactory(gtuFollowingModel, laneChangeModel));
                    }
                    else if ("LMRS".equals(tacticalPlannerName))
                    {
                        // provide default parameters with the car-following model
                        BehavioralCharacteristics defaultBehavioralCFCharacteristics = new BehavioralCharacteristics();
                        defaultBehavioralCFCharacteristics.setDefaultParameters(AbstractIDM.class);
                        return new LaneBasedStrategicalRoutePlannerFactory(
                                new LMRSFactory(new IDMPlusFactory(), defaultBehavioralCFCharacteristics));
                    }
                    else if ("Toledo".equals(tacticalPlannerName))
                    {
                        return new LaneBasedStrategicalRoutePlannerFactory(new ToledoFactory());
                    }
                    else
                    {
                        throw new PropertyException("Don't know how to create a " + tacticalPlannerName + " tactical planner");
                    }
                }
            }
        }
        throw new PropertyException("No TacticalPlanner key found in the properties");
    }

}

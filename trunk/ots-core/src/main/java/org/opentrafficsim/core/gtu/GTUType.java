package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ConstantGenerator;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.units.distributions.ContinuousDistSpeed;

import nl.tudelft.simulation.jstats.distributions.DistNormal;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * A GTU type identifies the type of a GTU. <br>
 * GTU types are used to check whether a particular GTU can travel over a particular part of infrastructure. E.g. a
 * (LaneBased)GTU with GTUType CAR can travel over lanes that have a LaneType that has the GTUType CAR in the compatibility set.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 31, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class GTUType extends HierarchicalType<GTUType> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** Default types with their name. */
    public enum DEFAULTS
    {
        /** Super type for all road users. */
        ROAD_USER("ROAD_USER"),

        /** Super type for all water way users. */
        WATERWAY_USER("WATERWAY_USER"),

        /** Super type for all rail users. */
        RAILWAY_USER("RAILWAY_USER"),

        /** Super type for pedestrians. */
        PEDESTRIAN("PEDESTRIAN", ROAD_USER),

        /** Super type for bicycle. */
        BICYCLE("BICYCLE", ROAD_USER),

        /** Super type for mopeds. */
        MOPED("MOPED", BICYCLE),

        /** Super type for vehicles. */
        VEHICLE("VEHICLE", ROAD_USER),

        /** Super type for emergency vehicles. */
        EMERGENCY_VEHICLE("EMERGENCY_VEHICLE", VEHICLE),

        /** Super type for ships. */
        SHIP("SHIP", WATERWAY_USER),

        /** Super type for trains. */
        TRAIN("TRAIN", RAILWAY_USER),

        /** Super type for cars. */
        CAR("CAR", VEHICLE),

        /** Super type for vans. */
        VAN("VAN", VEHICLE),

        /** Super type for busses. */
        BUS("BUS", VEHICLE),

        /** Super type for trucks. */
        TRUCK("TRUCK", VEHICLE),

        /** Super type for scheduled busses. */
        SCHEDULED_BUS("SCHEDULED_BUS", BUS);

        /** The name. */
        private final String id;

        /** The name. */
        private final DEFAULTS parent;

        /**
         * Construct the enum.
         * @param id String; the id
         */
        DEFAULTS(final String id)
        {
            this.id = id;
            this.parent = null;
        }

        /**
         * Construct the enum.
         * @param id String; the id
         * @param parent the parent
         */
        DEFAULTS(final String id, final DEFAULTS parent)
        {
            this.id = id;
            this.parent = parent;
        }

        /** @return the id */
        public String getId()
        {
            return this.id;
        }

        /** @return the parent */
        public DEFAULTS getParent()
        {
            return this.parent;
        }
    }

    /** the network to which the GTUType belongs. */
    private final Network network;

    /** Templates for GTU characteristics within a network. */
    private static final Map<Network, Map<GTUType, TemplateGTUType>> DEFAULT_TEMPLATES = new LinkedHashMap<>();

    /**
     * @param id String; The id of the GTUType to make it identifiable.
     * @param network Network; The network to which the GTUType belongs
     * @throws NullPointerException if the id is null
     */
    public GTUType(final String id, final Network network) throws NullPointerException
    {
        super(id);
        this.network = network;
        this.network.addGtuType(this);
    }

    /**
     * @param id String; The id of the GTUType to make it identifiable.
     * @param parent GTUType; parent GTU type
     * @throws NullPointerException if the id is null
     */
    public GTUType(final String id, final GTUType parent) throws NullPointerException
    {
        super(id, parent);
        this.network = parent.getNetwork();
        this.network.addGtuType(this);
    }

    /**
     * Whether this, or any of the parent types, equals the given type.
     * @param type DEFAULTS; type
     * @return whether this, or any of the parent types, equals the given type
     */
    public boolean isOfType(final DEFAULTS type)
    {
        if (this.getId().equals(type.getId()))
        {
            return true;
        }
        if (getParent() != null)
        {
            return getParent().isOfType(type);
        }
        return false;
    }

    /**
     * Whether this equals the given type.
     * @param type DEFAULTS; type
     * @return whether this equals the given type
     */
    public boolean isType(final DEFAULTS type)
    {
        return this.getId().equals(type.getId());
    }

    /**
     * Returns default characteristics for given GTUType.
     * @param gtuType GTUType; GTUType GTU type
     * @param network Network; the network to use as a key
     * @param randomStream StreamInterface; stream for random numbers
     * @return default characteristics for given GTUType
     * @throws GTUException if there are no default characteristics for the GTU type
     */
    public static GTUCharacteristics defaultCharacteristics(final GTUType gtuType, final Network network,
            final StreamInterface randomStream) throws GTUException
    {
        Map<GTUType, TemplateGTUType> map = DEFAULT_TEMPLATES.get(network);
        if (map == null)
        {
            map = new LinkedHashMap<>();
            DEFAULT_TEMPLATES.put(network, map);
        }
        TemplateGTUType template = null;
        GTUType type = gtuType;
        boolean store = false;
        while (template == null)
        {
            if (map.containsKey(type))
            {
                template = map.get(type);
            }
            else
            {
                store = true;
                if (type.equals(network.getGtuType(DEFAULTS.CAR)))
                {
                    // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
                    template = new TemplateGTUType(gtuType, new ConstantGenerator<>(Length.instantiateSI(4.19)),
                        new ConstantGenerator<>(Length.instantiateSI(1.7)), new ConstantGenerator<>(new Speed(180,
                            SpeedUnit.KM_PER_HOUR)));
                }
                else if (type.equals(network.getGtuType(DEFAULTS.TRUCK)))
                {
                    // from "Maatgevende normen in de Nederlandse richtlijnen voor wegontwerp", R-2014-38, SWOV
                    template = new TemplateGTUType(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                        new ConstantGenerator<>(Length.instantiateSI(2.55)), new ContinuousDistSpeed(new DistNormal(
                            randomStream, 85.0, 2.5), SpeedUnit.KM_PER_HOUR));
                }
                else if (type.equals(network.getGtuType(DEFAULTS.BUS)))
                {
                    template = new TemplateGTUType(gtuType, new ConstantGenerator<>(Length.instantiateSI(12.0)),
                        new ConstantGenerator<>(Length.instantiateSI(2.55)), new ConstantGenerator<>(new Speed(90,
                            SpeedUnit.KM_PER_HOUR)));
                }
                else if (type.equals(network.getGtuType(DEFAULTS.VAN)))
                {
                    template = new TemplateGTUType(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                        new ConstantGenerator<>(Length.instantiateSI(2.4)), new ConstantGenerator<>(new Speed(180,
                            SpeedUnit.KM_PER_HOUR)));
                }
                else if (type.equals(network.getGtuType(DEFAULTS.EMERGENCY_VEHICLE)))
                {
                    template = new TemplateGTUType(gtuType, new ConstantGenerator<>(Length.instantiateSI(5.0)),
                        new ConstantGenerator<>(Length.instantiateSI(2.55)), new ConstantGenerator<>(new Speed(180,
                            SpeedUnit.KM_PER_HOUR)));
                }
                else
                {
                    type = type.getParent();
                    Throw.whenNull(type, "GTUType %s is not of any types with default characteristics.", gtuType);
                }
            }
            if (store && template != null)
            {
                map.put(gtuType, template);
            }
        }
        try
        {
            return template.draw();
        }
        catch (ProbabilityException | ParameterException exception)
        {
            throw new GTUException("GTUType draw failed.", exception);
        }
    }

    /**
     * @return the network to which the GTUType belongs
     */
    public Network getNetwork()
    {
        return this.network;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GTUType: " + this.getId();
    }

}

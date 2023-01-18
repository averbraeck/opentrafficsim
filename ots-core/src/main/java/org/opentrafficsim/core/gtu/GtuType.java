package org.opentrafficsim.core.gtu;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.HierarchicalType;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.definitions.Defaults;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.network.Network;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * A GTU type is used to identify all sorts of properties and compatibilities for GTUs. For example, what lanes a GTU may drive
 * on, and what length of vehicle to get.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public final class GtuType extends HierarchicalType<GtuType, Gtu> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** Templates for GTU characteristics within a network. */
    private static final Map<Network, Map<GtuType, TemplateGtuType>> DEFAULT_TEMPLATES = new LinkedHashMap<>();

    /** Template suppliers. */
    private static final Map<GtuType, Defaults> TEMPLATE_SUPPLIERS = new LinkedHashMap<>();

    /**
     * Constructor for root-level GTU types. The parent will be {@code GtuType.ALL}.
     * @param id String; The id of the GtuType to make it identifiable.
     * @throws NullPointerException if the id is null
     */
    public GtuType(final String id) throws NullPointerException
    {
        super(id);
    }

    /**
     * @param id String; The id of the GtuType to make it identifiable.
     * @param parent GtuType; parent GTU type
     * @throws NullPointerException if the id is null
     */
    public GtuType(final String id, final GtuType parent) throws NullPointerException
    {
        super(id, parent);
    }

    /**
     * Register a supplier for default GTU types.
     * @param gtuType GtuType; default GTU type.
     * @param defaults Defaults; supplier of the template.
     */
    public static void registerTemplateSupplier(final GtuType gtuType, final Defaults defaults)
    {
        TEMPLATE_SUPPLIERS.put(gtuType, defaults);
    }

    /**
     * Returns default characteristics for given GtuType.
     * @param gtuType GtuType; GtuType GTU type
     * @param network Network; the network to use as a key
     * @param randomStream StreamInterface; stream for random numbers
     * @return default characteristics for given GtuType
     * @throws GtuException if there are no default characteristics for the GTU type
     */
    public static GtuCharacteristics defaultCharacteristics(final GtuType gtuType, final Network network,
            final StreamInterface randomStream) throws GtuException
    {
        Map<GtuType, TemplateGtuType> map = DEFAULT_TEMPLATES.get(network);
        if (map == null)
        {
            map = new LinkedHashMap<>();
            DEFAULT_TEMPLATES.put(network, map);
        }
        TemplateGtuType template = map.get(gtuType);
        GtuType type = gtuType;
        while (template == null)
        {
            // try to obtain from supplier
            if (TEMPLATE_SUPPLIERS.containsKey(type))
            {
                template = TEMPLATE_SUPPLIERS.get(type).getTemplate(type, randomStream);
            }

            // check parent type if that gave null
            if (template == null)
            {
                type = type.getParent();
                Throw.whenNull(type, "GtuType %s is not of any type with default characteristics. "
                        + "Register a template supplier with GtuType.registerTemplateSupplier().", gtuType);
                template = map.get(type);
            }

            // store if we got one
            if (template != null)
            {
                if (!template.getGtuType().equals(gtuType))
                {
                    template = template.copyForGtuType(gtuType);
                }
                map.put(gtuType, template);
            }
        }
        try
        {
            return template.draw();
        }
        catch (ProbabilityException | ParameterException exception)
        {
            throw new GtuException("GtuType draw failed.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "GtuType: " + this.getId();
    }

}

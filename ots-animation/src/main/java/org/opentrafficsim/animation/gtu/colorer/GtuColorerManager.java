package org.opentrafficsim.animation.gtu.colorer;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.Gtu;

/**
 * With a GTU color manager, advanced logic can be applied to determine what the color of a GTU should be. Predicates are
 * supplied with GTU colorers, and are applied in order. The first predicate that is true, determines the colorer that is used.
 * Predicates can apply any logic, for example based on location, zoom level, GTU properties, etc.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GtuColorerManager
{

    /** List of items. */
    private List<PredicatedColorer> colorItems = new ArrayList<>();

    /** Default color when no predicate is true. */
    private final Color defaultColor;

    /**
     * Constructor.
     * @param defaultColor default color when no predicate is true
     */
    public GtuColorerManager(final Color defaultColor)
    {
        Throw.whenNull(defaultColor, "defaultColor");
        this.defaultColor = defaultColor;
    }

    /**
     * Add colorer with predicate at end of priority list.
     * @param predicate predicate
     * @param gtuColorer GTU colorer
     */
    public void add(final Predicate<Gtu> predicate, final GtuColorer gtuColorer)
    {
        Throw.whenNull(predicate, "predicate");
        Throw.whenNull(gtuColorer, "gtuColorer");
        this.colorItems.add(new PredicatedColorer(predicate, gtuColorer));
    }

    /**
     * Add colorer with predicate at given index in priority list.
     * @param index index
     * @param predicate predicate
     * @param gtuColorer GTU colorer
     * @throws IndexOutOfBoundsException if the index is out of bounds
     */
    public void add(final int index, final Predicate<Gtu> predicate, final GtuColorer gtuColorer)
    {
        Throw.whenNull(predicate, "predicate");
        Throw.whenNull(gtuColorer, "gtuColorer");
        this.colorItems.add(index, new PredicatedColorer(predicate, gtuColorer));
    }

    /**
     * Returns the number of colorers, each with their predicate.
     * @return number of colorers, each with their predicate
     */
    public int size()
    {
        return this.colorItems.size();
    }

    /**
     * Returns the color based on the first GTU colorer for which the coupled predicate is true.
     * @param gtu GTU
     * @return color based on the first GTU colorer for which the coupled predicate is true
     */
    public Color getColor(final Gtu gtu)
    {
        for (final PredicatedColorer item : this.colorItems)
        {
            if (item.predicate().test(gtu))
            {
                return item.colorer().getColor(gtu);
            }
        }
        return this.defaultColor;
    }

    /**
     * Record of predicate and GTU colorer.
     * @param predicate predicate
     * @param colorer GTU colorer
     */
    public record PredicatedColorer(Predicate<Gtu> predicate, GtuColorer colorer)
    {
        @Override
        public String toString()
        {
            return colorer().getName();
        }
    };
}

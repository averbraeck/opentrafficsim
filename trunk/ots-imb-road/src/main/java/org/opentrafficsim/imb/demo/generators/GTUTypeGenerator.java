package org.opentrafficsim.imb.demo.generators;

import java.util.ArrayList;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 16 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class GTUTypeGenerator
{

    /** GTU lengths. */
    private final List<Length> lengths = new ArrayList<>();
    
    /** GTU widths. */
    private final List<Length> widths = new ArrayList<>();
    
    /** GTU types. */
    private final List<GTUType> gtuTypes = new ArrayList<>();
    
    /** Maximum GTU speeds. */
    private final List<Speed> maximumSpeeds = new ArrayList<>();
    
    /** GTU type probabilities. */
    private final List<Double> probabilities = new ArrayList<>();
    
    /** Probability sum. */
    private double probabilitySum = 0.0;
    
    /**
     * @param length
     * @param width
     * @param gtuType
     * @param maximumSpeed
     * @param probability
     */
    public void addType(Length length, Length width, GTUType gtuType, Speed maximumSpeed, double probability)
    {
        this.lengths.add(length);
        this.widths.add(width);
        this.gtuTypes.add(gtuType);
        this.maximumSpeeds.add(maximumSpeed);
        this.probabilities.add(probability);
        this.probabilitySum += probability;
    }
    
    /**
     * @return random GTU type info
     */
    public GTUTypeInfo draw()
    {
        double r = Math.random() * this.probabilitySum;
        int i = 0;
        double probCumSum = this.probabilities.get(0);
        while (r > probCumSum && i < this.probabilities.size() - 1)
        {
            i++;
            probCumSum += this.probabilities.get(i);
        }
        return new GTUTypeInfo(this.lengths.get(i), this.widths.get(i), this.gtuTypes.get(i), this.maximumSpeeds.get(i));
    }
    
    /**
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 nov. 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public class GTUTypeInfo
    {
        
        /** Length. */
        private final Length length;
        
        /** Width. */
        private final Length width;
        
        /** GTU type. */
        private final GTUType gtuType;
        
        /** Maximum speed. */
        private final Speed maximumSpeed;

        /**
         * @param length
         * @param width
         * @param gtuType
         * @param maximumSpeed
         */
        public GTUTypeInfo(Length length, Length width, GTUType gtuType, Speed maximumSpeed)
        {
            this.length = length;
            this.width = width;
            this.gtuType = gtuType;
            this.maximumSpeed = maximumSpeed;
        }

        /**
         * @return length.
         */
        public Length getLength()
        {
            return this.length;
        }

        /**
         * @return width.
         */
        public Length getWidth()
        {
            return this.width;
        }

        /**
         * @return gtuType.
         */
        public GTUType getGtuType()
        {
            return this.gtuType;
        }

        /**
         * @return maximumSpeed.
         */
        public Speed getMaximumSpeed()
        {
            return this.maximumSpeed;
        }
        
    }
    
}

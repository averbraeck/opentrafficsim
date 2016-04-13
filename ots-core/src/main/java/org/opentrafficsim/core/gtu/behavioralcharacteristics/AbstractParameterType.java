package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import java.io.Serializable;
import java.util.UUID;

import org.djunits.unit.Unit;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * Defines meta-information of a parameter, defining the parameter uniquely.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, 
 *          initial version Apr 13, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <U> Unit of the value.
 * @param <T> Class of the value.
 */
public abstract class AbstractParameterType<U extends Unit<U>, T extends DoubleScalar.Rel<U>> implements Serializable
{
    
    /** */
    private static final long serialVersionUID = 20160400L;

    /** Unique identifier. */
    private final UUID uniqueId;

    /** Short name of parameter. */
    private final String id;

    /** Parameter description or full name. */
    private final String description;

    /** Class of the value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final Class<T> valueClass;

    /** Default value. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    protected final T defaultValue;

    /** Default check. */
    private final Check check;
    
    /** List of default checks for ParameterTypes. */
    public enum Check
    {
        /** Checks for &gt;0. */
        POSITIVE, 
        
        /** Checks for &lt;0. */
        NEGATIVE, 
        
        /** Checks for &ge;0. */
        POSITIVEZERO, 
        
        /** Checks for &le;0. */
        NEGATIVEZERO,
        
        /** Checks for &ne;0. */
        NONZERO,
        
        /** Checks for range [0...1]. */
        UNITINTERVAL;
    }

    /**
     * Constructor without default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass)
    {
        this(id, description, valueClass, null, null);
    }
    
    /**
     * Constructor with default value, without check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue)
    {
        this(id, description, valueClass, defaultValue, null);
    }

    /**
     * Constructor without default value, with check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param check Check for parameter values.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final Check check)
    {
        this(id, description, valueClass, null, check);
    }
    
    /**
     * Constructor with default value and check.
     * @param id Short name of parameter.
     * @param description Parameter description or full name.
     * @param valueClass Class of the value.
     * @param defaultValue Default value.
     * @param check Check for parameter values.
     */
    public AbstractParameterType(final String id, final String description, final Class<T> valueClass, final T defaultValue, final Check check)
    {
        this.id = id;
        this.description = description;
        this.valueClass = valueClass;
        this.defaultValue = defaultValue;
        this.uniqueId = UUID.randomUUID();
        this.check = check;
        if (this.defaultValue != null) 
        {
            try
            {
                checkCheck(this.defaultValue);
            }
            catch (ParameterException pe)
            {
                throw new RuntimeException("Default value of parameter '" + this.id
                    + "' does not comply with default constraints.", pe);
            }
        }
    }

    /**
     * Returns the parameter id.
     * @return id Parameter id.
     */
    public final String getId()
    {
        return this.id;
    }

    /**
     * Returns the parameter description.
     * @return description
     */
    public final String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the default value.
     * @return defaultValue Default value.
     * @throws ParameterException If no default value was set.
     */
    public abstract Object getDefaultValue() throws ParameterException;

    /**
     * Checks the default checks given with the parameter type.
     * @param value The value to check.
     * @throws ParameterException If the value does not comply with constraints.
     */
    protected final void checkCheck(final T value) throws ParameterException 
    {
        if (this.check == null)
        {
            return;
        }
        switch (this.check)
        {
            case POSITIVE:
                ParameterException.throwIf(value.si <= 0.0, "Value of parameter '" + this.id + "' must be above zero.");
                break;
                
            case NEGATIVE:
                ParameterException.throwIf(value.si >= 0.0, "Value of parameter '" + this.id + "' must be below zero.");
                break;
                
            case POSITIVEZERO:
                ParameterException.throwIf(value.si < 0.0, "Value of parameter '" + this.id + "' may not be below zero.");
                break;
                
            case NEGATIVEZERO:
                ParameterException.throwIf(value.si > 0.0, "Value of parameter '" + this.id + "' may not be above zero.");
                break;
                
            case NONZERO:
                ParameterException.throwIf(value.si == 0.0, "Value of parameter '" + this.id + "' may not be zero.");
                break;
                
            case UNITINTERVAL:
                ParameterException.throwIf(value.si < 0.0 || value.si > 1.0, "Value of parameter '" + this.id 
                    + "' must be in range [0...1]");
                break;
            
            default:
                throw new ParameterException("Unknown parameter check value.");
                
        }
    }
    
    /**
     * Print the given value from the map in BehavioralCharachteristics in a presentable format.
     * @param behavioralCharacteristics Behavioral characteristics to get the value from.
     * @return Printable string of value.
     * @throws ParameterException If the parameter is not present.
     */
    public abstract String printValue(BehavioralCharacteristics behavioralCharacteristics) throws ParameterException;

    /**
     * {@inheritDoc}
     */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.uniqueId == null) ? 0 : this.uniqueId.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @SuppressWarnings({"checkstyle:designforextension", "checkstyle:needbraces"})
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AbstractParameterType<?, ?> other = (AbstractParameterType<?, ?>) obj;
        if (this.uniqueId == null)
        {
            if (other.uniqueId != null)
                return false;
        }
        else if (!this.uniqueId.equals(other.uniqueId))
            return false;
        return true;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return "AbstractParameterType [id=" + this.id + ", description=" + this.description + ", valueClass="
            + this.valueClass + "]";
    }

}

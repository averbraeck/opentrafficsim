package nl.tno.imb.mc;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.imb.IMBException;

import nl.tno.imb.TByteBuffer;

/**
 * IMB model control parameter.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class Parameter
{
    /** Name of this parameter. */
    private final String name;

    /** Type of this Parameter. */
    private final Object value;

    /** Type of this parameter. */
    private final ParameterType type;

    /** List of possible values. */
    private List<String> valueList = null;

    /**
     * Construct a new float parameter.
     * @param name String; name of the new float parameter
     * @param value double; value of the new float parameter
     */
    public Parameter(final String name, final double value)
    {
        this.name = name;
        this.type = ParameterType.FLOAT;
        this.value = value;
    }

    /**
     * Construct a Boolean parameter.
     * @param name String; the name of the new Boolean parameter
     * @param value Boolean; the value of the new Boolean parameter
     */
    public Parameter(final String name, final Boolean value)
    {
        this.name = name;
        this.type = ParameterType.BOOLEAN;
        this.value = value;
    }

    /**
     * Construct a new integer parameter.
     * @param name String; name of the new integer parameter
     * @param value int; value of the new integer parameter
     */
    public Parameter(final String name, final int value)
    {
        this.name = name;
        this.type = ParameterType.INTEGER;
        this.value = value;
    }

    /**
     * Construct a String parameter.
     * @param name String; the name of the new String parameter
     * @param value String; the value of the new String parameter
     */
    public Parameter(final String name, final String value)
    {
        this.name = name;
        this.type = ParameterType.STRING;
        this.value = value;
    }

    /**
     * Construct a new Parameter from the next object in a TByteBuffer.
     * @param payload TByteBuffer; the received IMB data
     * @throws IMBException when the received type cannot be converted to a Parameter sub-type
     */
    public Parameter(final TByteBuffer payload) throws IMBException
    {
        this.name = payload.readString();
        int valueCode = payload.readInt32();
        if (ParameterType.BOOLEAN.value == valueCode)
        {
            this.type = ParameterType.BOOLEAN;
            this.value = payload.readBoolean();
        }
        else if (ParameterType.FLOAT.value == valueCode)
        {
            this.type = ParameterType.FLOAT;
            this.value = payload.readDouble();
        }
        else if (ParameterType.INTEGER.value == valueCode)
        {
            this.type = ParameterType.INTEGER;
            this.value = payload.readInt32();
        }
        else if (ParameterType.STRING.value == valueCode)
        {
            this.type = ParameterType.STRING;
            this.value = payload.readString();
        }
        else
        {
            throw new IMBException("Inhandled type: " + valueCode);
        }
        int optionCount = payload.readInt32();
        if (optionCount > 0)
        {
            this.valueList = new ArrayList<String>(optionCount);
            for (int optionIndex = 0; optionIndex < optionCount; optionIndex++)
            {
                this.valueList.add(payload.readString());
            }
        }
    }

    /**
     * Prepare this parameter for transmission over IMB.
     * @param payload TByteBuffer; the transmission buffer
     */
    public void prepare(final TByteBuffer payload)
    {
        payload.prepare(this.name);
        payload.prepare(this.type.value);
        switch (this.type)
        {
            case BOOLEAN:
                payload.prepare((boolean) this.value);
                break;

            case FLOAT:
                payload.prepare((double) this.value);
                break;

            case INTEGER:
                payload.prepare((int) this.value);
                break;

            case STRING:
                payload.prepare((String) this.value);
                break;
        }
        if (null != this.valueList && this.valueList.size() > 0)
        {
            payload.prepare(this.valueList.size());
            for (String option : this.valueList)
            {
                payload.prepare(option);
            }
        }
        else
        {
            payload.prepare(0);
        }
    }

    /**
     * Prepare this parameter for transmission over IMB.
     * @param payload TByteBuffer; the transmission buffer
     */
    public void qWrite(final TByteBuffer payload)
    {
        payload.qWrite(this.name);
        payload.qWrite(this.type.value);
        switch (this.type)
        {
            case BOOLEAN:
                payload.qWrite((boolean) this.value);
                break;

            case FLOAT:
                payload.qWrite((double) this.value);
                break;

            case INTEGER:
                payload.qWrite((int) this.value);
                break;

            case STRING:
                payload.qWrite((String) this.value);
                break;
        }
        if (null != this.valueList && this.valueList.size() > 0)
        {
            payload.qWrite(this.valueList.size());
            for (String option : this.valueList)
            {
                payload.qWrite(option);
            }
        }
        else
        {
            payload.qWrite(0);
        }
    }

    /**
     * Set the list of pre-defined options that the user may choose from.
     * @param list List&lt;String&gt;; the list of options
     */
    public void setValueList(final List<String> list)
    {
        this.valueList = new ArrayList<String>(list);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Parameter [name=" + this.name + ", value=" + this.value + ", type=" + this.type + "]";
    }

    /**
     * @return name.
     */
    public String getName()
    {
        return this.name;
    }

    /**
     * @return value.
     */
    public Object getValue()
    {
        return this.value;
    }

    /**
     * @return type.
     */
    public ParameterType getType()
    {
        return this.type;
    }

    /**
     * @return valueList.
     */
    public List<String> getValueList()
    {
        return this.valueList;
    }

    /**
     * IMB integer numbers and our corresponding enum values.
     */
    public enum ParameterType
    {
        /** Float parameter. */
        FLOAT(0),
        /** Boolean parameter. */
        BOOLEAN(1),
        /** Integer parameter. */
        INTEGER(2),
        /** String parameter. */
        STRING(3);

        /** The IMB integer value used for this ParameterType. */
        public final int value;

        /**
         * Construct a new ParameterType.
         * @param value int; the IMB integer value for the new ParameterType
         */
        ParameterType(final int value)
        {
            this.value = value;
        }

    };
}

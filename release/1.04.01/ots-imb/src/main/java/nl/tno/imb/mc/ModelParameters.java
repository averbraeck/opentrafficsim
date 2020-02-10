package nl.tno.imb.mc;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.imb.IMBException;
import org.opentrafficsim.imb.SelfWrapper;

import nl.tno.imb.TByteBuffer;

/**
 * Container for a list of model parameters.<br>
 * For now this object is immutable.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 17, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class ModelParameters implements SelfWrapper
{

    /** The stored parameters. */
    private List<Parameter> parameters;

    /** Fast lookup by name. */
    private Map<String, Parameter> nameMap;

    /**
     * Construct ModelParameters from a IMB ByteBuffer
     * @param payload TByteBuffer;
     * @throws IMBException
     */
    public ModelParameters(TByteBuffer payload) throws IMBException
    {
        int size = payload.readInt32();
        this.parameters = new ArrayList<>(size);
        this.nameMap = new LinkedHashMap<>(size);
        for (int index = 0; index < size; index++)
        {
            Parameter parameter = new Parameter(payload);
            String parameterName = parameter.getName();
            this.parameters.add(parameter);
            this.nameMap.put(parameterName, parameter);
        }
    }

    /**
     * Retrieve the parameter with a specified name
     * @param name String; name of the parameter to look up
     * @return Parameter; the parameter with the specified name, or null if no parameter with the specified name exists in this
     *         ModelParameters object
     */
    public Parameter getParameterByName(final String name)
    {
        return this.nameMap.get(name);
    }

    /**
     * Retrieve the names of all stored parameters. The returned object is a deep copy and may be modified by the caller.
     * @return List&lt;String&gt;; the names of all stored parameters
     */
    public List<String> getParameterNames()
    {
        return new ArrayList<String>(this.nameMap.keySet());
    }

    /**
     * Report if this ModelParameters object contains a parameter with the specified name.
     * @param name String; name of the parameter
     * @return boolean; true if such a parameter exists; false if such a parameter does not exist
     */
    public boolean parameterExists(final String name)
    {
        return this.nameMap.containsKey(name);
    }

    /**
     * Look up a parameter by name and throw an IMBException if no such parameter can be found.
     * @param name String; name of the parameter
     * @return Parameter; the Parameter with the specified name
     * @throws IMBException when no Parameter with the specified name is stored in this ModelParameters object
     */
    private Parameter getParameterOrThrowException(final String name) throws IMBException
    {
        Parameter parameter = this.nameMap.get(name);
        if (null == parameter)
        {
            throw new IMBException("No parameter with name " + name + " stored in this ModelParameters object");
        }
        return parameter;
    }

    /**
     * Retrieve the type of a parameter.
     * @param name String; name of the parameter
     * @return Parameter.ParameterType; the type of the parameter
     * @throws IMBException when no parameter with the specified name exists in this ModelParameters object
     */
    public Parameter.ParameterType getParameterType(final String name) throws IMBException
    {
        return getParameterOrThrowException(name).getType();
    }

    /**
     * Retrieve the value of a parameter.
     * @param name String; name of the parameter
     * @return Object; the value of the parameter
     * @throws IMBException when no parameter with the specified name exists in this ModelParameters object
     */
    public Object getParameterValue(final String name) throws IMBException
    {
        return getParameterOrThrowException(name).getValue();
    }

    /**
     * Add a parameter. The name of the parameter must be unique.
     * @param parameter Parameter; Parameter
     * @return boolean; true if the parameter was added
     */
    public boolean addParameter(final Parameter parameter)
    {
        if (this.nameMap.containsKey(parameter.getName()))
        {
            return false;
        }
        this.parameters.add(parameter);
        this.nameMap.put(parameter.getName(), parameter);
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public void prepare(TByteBuffer payload)
    {
        payload.prepare(this.parameters.size());
        for (int index = 0; index < this.parameters.size(); index++)
        {
            this.parameters.get(index).prepare(payload);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void qWrite(TByteBuffer payload)
    {
        payload.qWrite(this.parameters.size());
        for (int index = 0; index < this.parameters.size(); index++)
        {
            this.parameters.get(index).qWrite(payload);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("ModelParameters [parameters=");
        String separator = "";
        for (Parameter parameter : this.parameters)
        {
            result.append(separator);
            result.append(parameter.getName() + ":" + parameter.getValue());
            separator = ", ";
        }
        result.append("]");
        return result.toString();
    }

}

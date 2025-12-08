
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ParameterTypeBoolean complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ParameterTypeBoolean">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ParameterType">
 *       <attribute name="Default" type="{http://www.opentrafficsim.org/ots}boolean" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterTypeBoolean")
@SuppressWarnings("all") public class ParameterTypeBoolean
    extends ParameterType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Default")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType _default;

    /**
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getDefault() {
        return _default;
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDefault(BooleanType value) {
        this._default = value;
    }

}

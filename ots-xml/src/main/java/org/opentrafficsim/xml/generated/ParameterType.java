
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StaticFieldNameAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.FieldType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for ParameterType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ParameterType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Field" use="required" type="{http://www.opentrafficsim.org/ots}StaticFieldNameType" />
 *       <attribute name="Description" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ParameterType")
@XmlSeeAlso({
    ParameterTypeDuration.class,
    ParameterTypeLength.class,
    ParameterTypeSpeed.class,
    ParameterTypeAcceleration.class,
    ParameterTypeLinearDensity.class,
    ParameterTypeFrequency.class,
    ParameterTypeDouble.class,
    ParameterTypeFraction.class,
    ParameterTypeInteger.class,
    ParameterTypeBoolean.class,
    ParameterTypeString.class,
    ParameterTypeClass.class,
    ParameterTypeFloat.class,
    ParameterTypeLong.class
})
@SuppressWarnings("all") public class ParameterType implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Field", required = true)
    @XmlJavaTypeAdapter(StaticFieldNameAdapter.class)
    protected FieldType field;
    @XmlAttribute(name = "Description")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType description;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the field property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public FieldType getField() {
        return field;
    }

    /**
     * Sets the value of the field property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setField(FieldType value) {
        this.field = value;
    }

    /**
     * Gets the value of the description property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDescription() {
        return description;
    }

    /**
     * Sets the value of the description property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDescription(StringType value) {
        this.description = value;
    }

}


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
 * <p>Java-Klasse für ParameterType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der id-Eigenschaft ab.
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
     * Legt den Wert der id-Eigenschaft fest.
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
     * Ruft den Wert der field-Eigenschaft ab.
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
     * Legt den Wert der field-Eigenschaft fest.
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
     * Ruft den Wert der description-Eigenschaft ab.
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
     * Legt den Wert der description-Eigenschaft fest.
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

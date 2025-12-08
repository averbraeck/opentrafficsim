
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for ModelIdReferralType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="ModelIdReferralType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="ModelId" type="{http://www.opentrafficsim.org/ots}string" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ModelIdReferralType")
@SuppressWarnings("all") public class ModelIdReferralType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id")
    protected String id;
    @XmlAttribute(name = "ModelId")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType modelId;

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
     * Gets the value of the modelId property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getModelId() {
        return modelId;
    }

    /**
     * Sets the value of the modelId property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setModelId(StringType value) {
        this.modelId = value;
    }

}

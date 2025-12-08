
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.PositiveFactorAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.DoubleType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CategoryType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="CategoryType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="Lane" type="{http://www.opentrafficsim.org/ots}LaneLinkType" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="GtuType" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Route" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Factor" type="{http://www.opentrafficsim.org/ots}PositiveFactor" default="1.0" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CategoryType", propOrder = {
    "lane"
})
@SuppressWarnings("all") public class CategoryType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Lane")
    protected LaneLinkType lane;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "GtuType")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;
    @XmlAttribute(name = "Route")
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType route;
    @XmlAttribute(name = "Factor")
    @XmlJavaTypeAdapter(PositiveFactorAdapter.class)
    protected DoubleType factor;

    /**
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link LaneLinkType }
     *     
     */
    public LaneLinkType getLane() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link LaneLinkType }
     *     
     */
    public void setLane(LaneLinkType value) {
        this.lane = value;
    }

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
     * Gets the value of the gtuType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getGtuType() {
        return gtuType;
    }

    /**
     * Sets the value of the gtuType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGtuType(StringType value) {
        this.gtuType = value;
    }

    /**
     * Gets the value of the route property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getRoute() {
        return route;
    }

    /**
     * Sets the value of the route property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRoute(StringType value) {
        this.route = value;
    }

    /**
     * Gets the value of the factor property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DoubleType getFactor() {
        if (factor == null) {
            return new PositiveFactorAdapter().unmarshal("1.0");
        } else {
            return factor;
        }
    }

    /**
     * Sets the value of the factor property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFactor(DoubleType value) {
        this.factor = value;
    }

}

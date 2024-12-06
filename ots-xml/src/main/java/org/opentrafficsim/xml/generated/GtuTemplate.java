
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element name="LengthDist" type="{http://www.opentrafficsim.org/ots}LengthDistType"/>
 *         <element name="WidthDist" type="{http://www.opentrafficsim.org/ots}LengthDistType"/>
 *         <element name="MaxSpeedDist" type="{http://www.opentrafficsim.org/ots}SpeedDistType"/>
 *         <element name="MaxAccelerationDist" type="{http://www.opentrafficsim.org/ots}AccelerationDistType" minOccurs="0"/>
 *         <element name="MaxDecelerationDist" type="{http://www.opentrafficsim.org/ots}AccelerationDistType" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Default" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "lengthDist",
    "widthDist",
    "maxSpeedDist",
    "maxAccelerationDist",
    "maxDecelerationDist"
})
@XmlRootElement(name = "GtuTemplate")
@SuppressWarnings("all") public class GtuTemplate
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "LengthDist", required = true)
    protected LengthDistType lengthDist;
    @XmlElement(name = "WidthDist", required = true)
    protected LengthDistType widthDist;
    @XmlElement(name = "MaxSpeedDist", required = true)
    protected SpeedDistType maxSpeedDist;
    @XmlElement(name = "MaxAccelerationDist")
    protected AccelerationDistType maxAccelerationDist;
    @XmlElement(name = "MaxDecelerationDist")
    protected AccelerationDistType maxDecelerationDist;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;
    @XmlAttribute(name = "Default")
    protected Boolean _default;

    /**
     * Gets the value of the lengthDist property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDistType }
     *     
     */
    public LengthDistType getLengthDist() {
        return lengthDist;
    }

    /**
     * Sets the value of the lengthDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDistType }
     *     
     */
    public void setLengthDist(LengthDistType value) {
        this.lengthDist = value;
    }

    /**
     * Gets the value of the widthDist property.
     * 
     * @return
     *     possible object is
     *     {@link LengthDistType }
     *     
     */
    public LengthDistType getWidthDist() {
        return widthDist;
    }

    /**
     * Sets the value of the widthDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDistType }
     *     
     */
    public void setWidthDist(LengthDistType value) {
        this.widthDist = value;
    }

    /**
     * Gets the value of the maxSpeedDist property.
     * 
     * @return
     *     possible object is
     *     {@link SpeedDistType }
     *     
     */
    public SpeedDistType getMaxSpeedDist() {
        return maxSpeedDist;
    }

    /**
     * Sets the value of the maxSpeedDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link SpeedDistType }
     *     
     */
    public void setMaxSpeedDist(SpeedDistType value) {
        this.maxSpeedDist = value;
    }

    /**
     * Gets the value of the maxAccelerationDist property.
     * 
     * @return
     *     possible object is
     *     {@link AccelerationDistType }
     *     
     */
    public AccelerationDistType getMaxAccelerationDist() {
        return maxAccelerationDist;
    }

    /**
     * Sets the value of the maxAccelerationDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccelerationDistType }
     *     
     */
    public void setMaxAccelerationDist(AccelerationDistType value) {
        this.maxAccelerationDist = value;
    }

    /**
     * Gets the value of the maxDecelerationDist property.
     * 
     * @return
     *     possible object is
     *     {@link AccelerationDistType }
     *     
     */
    public AccelerationDistType getMaxDecelerationDist() {
        return maxDecelerationDist;
    }

    /**
     * Sets the value of the maxDecelerationDist property.
     * 
     * @param value
     *     allowed object is
     *     {@link AccelerationDistType }
     *     
     */
    public void setMaxDecelerationDist(AccelerationDistType value) {
        this.maxDecelerationDist = value;
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
     * Gets the value of the default property.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    public boolean isDefault() {
        if (_default == null) {
            return false;
        } else {
            return _default;
        }
    }

    /**
     * Sets the value of the default property.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    public void setDefault(Boolean value) {
        this._default = value;
    }

}

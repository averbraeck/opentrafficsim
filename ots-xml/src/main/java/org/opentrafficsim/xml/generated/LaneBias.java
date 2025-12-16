
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DoubleUnitIntervalAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.IntegerType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
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
 *       <choice>
 *         <sequence>
 *           <element name="LeftSpeed" type="{http://www.opentrafficsim.org/ots}SpeedType"/>
 *           <element name="RightSpeed" type="{http://www.opentrafficsim.org/ots}SpeedType"/>
 *         </sequence>
 *         <element name="FromLeft" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/>
 *         <element name="FromRight" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/>
 *       </choice>
 *       <attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Bias" use="required" type="{http://www.opentrafficsim.org/ots}float" />
 *       <attribute name="StickyLanes" type="{http://www.opentrafficsim.org/ots}positiveInteger" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "leftSpeed",
    "rightSpeed",
    "fromLeft",
    "fromRight"
})
@XmlRootElement(name = "LaneBias")
@SuppressWarnings("all") public class LaneBias
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "LeftSpeed", type = String.class)
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    protected SpeedType leftSpeed;
    @XmlElement(name = "RightSpeed", type = String.class)
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    protected SpeedType rightSpeed;
    @XmlElement(name = "FromLeft", type = String.class)
    @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.DoubleType fromLeft;
    @XmlElement(name = "FromRight", type = String.class)
    @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.DoubleType fromRight;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType gtuType;
    @XmlAttribute(name = "Bias", required = true)
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.DoubleType bias;
    @XmlAttribute(name = "StickyLanes")
    @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
    protected IntegerType stickyLanes;

    /**
     * Gets the value of the leftSpeed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedType getLeftSpeed() {
        return leftSpeed;
    }

    /**
     * Sets the value of the leftSpeed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftSpeed(SpeedType value) {
        this.leftSpeed = value;
    }

    /**
     * Gets the value of the rightSpeed property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedType getRightSpeed() {
        return rightSpeed;
    }

    /**
     * Sets the value of the rightSpeed property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightSpeed(SpeedType value) {
        this.rightSpeed = value;
    }

    /**
     * Gets the value of the fromLeft property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.DoubleType getFromLeft() {
        return fromLeft;
    }

    /**
     * Sets the value of the fromLeft property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromLeft(org.opentrafficsim.xml.bindings.types.DoubleType value) {
        this.fromLeft = value;
    }

    /**
     * Gets the value of the fromRight property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.DoubleType getFromRight() {
        return fromRight;
    }

    /**
     * Sets the value of the fromRight property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFromRight(org.opentrafficsim.xml.bindings.types.DoubleType value) {
        this.fromRight = value;
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
     * Gets the value of the bias property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.DoubleType getBias() {
        return bias;
    }

    /**
     * Sets the value of the bias property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setBias(org.opentrafficsim.xml.bindings.types.DoubleType value) {
        this.bias = value;
    }

    /**
     * Gets the value of the stickyLanes property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public IntegerType getStickyLanes() {
        return stickyLanes;
    }

    /**
     * Sets the value of the stickyLanes property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setStickyLanes(IntegerType value) {
        this.stickyLanes = value;
    }

}

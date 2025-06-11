
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
 * <p>Java-Klasse für anonymous complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der leftSpeed-Eigenschaft ab.
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
     * Legt den Wert der leftSpeed-Eigenschaft fest.
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
     * Ruft den Wert der rightSpeed-Eigenschaft ab.
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
     * Legt den Wert der rightSpeed-Eigenschaft fest.
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
     * Ruft den Wert der fromLeft-Eigenschaft ab.
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
     * Legt den Wert der fromLeft-Eigenschaft fest.
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
     * Ruft den Wert der fromRight-Eigenschaft ab.
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
     * Legt den Wert der fromRight-Eigenschaft fest.
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
     * Ruft den Wert der gtuType-Eigenschaft ab.
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
     * Legt den Wert der gtuType-Eigenschaft fest.
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
     * Ruft den Wert der bias-Eigenschaft ab.
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
     * Legt den Wert der bias-Eigenschaft fest.
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
     * Ruft den Wert der stickyLanes-Eigenschaft ab.
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
     * Legt den Wert der stickyLanes-Eigenschaft fest.
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

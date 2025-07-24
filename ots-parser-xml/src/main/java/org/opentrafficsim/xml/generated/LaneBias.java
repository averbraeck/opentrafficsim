//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import javax.annotation.Generated;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.DoubleAdapter;
import org.opentrafficsim.xml.bindings.DoubleUnitIntervalAdapter;
import org.opentrafficsim.xml.bindings.PositiveIntegerAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.IntegerType;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice&gt;
 *         &lt;sequence&gt;
 *           &lt;element name="LeftSpeed" type="{http://www.opentrafficsim.org/ots}SpeedType"/&gt;
 *           &lt;element name="RightSpeed" type="{http://www.opentrafficsim.org/ots}SpeedType"/&gt;
 *         &lt;/sequence&gt;
 *         &lt;element name="FromLeft" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/&gt;
 *         &lt;element name="FromRight" type="{http://www.opentrafficsim.org/ots}DoubleUnitInterval"/&gt;
 *       &lt;/choice&gt;
 *       &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="Bias" use="required" type="{http://www.opentrafficsim.org/ots}float" /&gt;
 *       &lt;attribute name="StickyLanes" type="{http://www.opentrafficsim.org/ots}positiveInteger" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class LaneBias
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "LeftSpeed", type = String.class)
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected SpeedType leftSpeed;
    @XmlElement(name = "RightSpeed", type = String.class)
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected SpeedType rightSpeed;
    @XmlElement(name = "FromLeft", type = String.class)
    @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.DoubleType fromLeft;
    @XmlElement(name = "FromRight", type = String.class)
    @XmlJavaTypeAdapter(DoubleUnitIntervalAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.DoubleType fromRight;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType gtuType;
    @XmlAttribute(name = "Bias", required = true)
    @XmlJavaTypeAdapter(DoubleAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.DoubleType bias;
    @XmlAttribute(name = "StickyLanes")
    @XmlJavaTypeAdapter(PositiveIntegerAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected IntegerType stickyLanes;

    /**
     * Ruft den Wert der leftSpeed-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setStickyLanes(IntegerType value) {
        this.stickyLanes = value;
    }

}

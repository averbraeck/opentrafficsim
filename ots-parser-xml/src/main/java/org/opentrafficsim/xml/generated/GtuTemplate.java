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
import org.opentrafficsim.xml.bindings.StringAdapter;
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
 *       &lt;sequence&gt;
 *         &lt;element name="LengthDist" type="{http://www.opentrafficsim.org/ots}LengthDistType"/&gt;
 *         &lt;element name="WidthDist" type="{http://www.opentrafficsim.org/ots}LengthDistType"/&gt;
 *         &lt;element name="MaxSpeedDist" type="{http://www.opentrafficsim.org/ots}SpeedDistType"/&gt;
 *         &lt;element name="MaxAccelerationDist" type="{http://www.opentrafficsim.org/ots}AccelerationDistType" minOccurs="0"/&gt;
 *         &lt;element name="MaxDecelerationDist" type="{http://www.opentrafficsim.org/ots}AccelerationDistType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *       &lt;attribute name="GtuType" use="required" type="{http://www.opentrafficsim.org/ots}string" /&gt;
 *       &lt;attribute name="Default" type="{http://www.w3.org/2001/XMLSchema}boolean" default="false" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
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
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class GtuTemplate
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "LengthDist", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected LengthDistType lengthDist;
    @XmlElement(name = "WidthDist", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected LengthDistType widthDist;
    @XmlElement(name = "MaxSpeedDist", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected SpeedDistType maxSpeedDist;
    @XmlElement(name = "MaxAccelerationDist")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected AccelerationDistType maxAccelerationDist;
    @XmlElement(name = "MaxDecelerationDist")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected AccelerationDistType maxDecelerationDist;
    @XmlAttribute(name = "Id", required = true)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected String id;
    @XmlAttribute(name = "GtuType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StringType gtuType;
    @XmlAttribute(name = "Default")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected Boolean _default;

    /**
     * Ruft den Wert der lengthDist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LengthDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public LengthDistType getLengthDist() {
        return lengthDist;
    }

    /**
     * Legt den Wert der lengthDist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLengthDist(LengthDistType value) {
        this.lengthDist = value;
    }

    /**
     * Ruft den Wert der widthDist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link LengthDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public LengthDistType getWidthDist() {
        return widthDist;
    }

    /**
     * Legt den Wert der widthDist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link LengthDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWidthDist(LengthDistType value) {
        this.widthDist = value;
    }

    /**
     * Ruft den Wert der maxSpeedDist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link SpeedDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public SpeedDistType getMaxSpeedDist() {
        return maxSpeedDist;
    }

    /**
     * Legt den Wert der maxSpeedDist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link SpeedDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setMaxSpeedDist(SpeedDistType value) {
        this.maxSpeedDist = value;
    }

    /**
     * Ruft den Wert der maxAccelerationDist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AccelerationDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public AccelerationDistType getMaxAccelerationDist() {
        return maxAccelerationDist;
    }

    /**
     * Legt den Wert der maxAccelerationDist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AccelerationDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setMaxAccelerationDist(AccelerationDistType value) {
        this.maxAccelerationDist = value;
    }

    /**
     * Ruft den Wert der maxDecelerationDist-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link AccelerationDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public AccelerationDistType getMaxDecelerationDist() {
        return maxDecelerationDist;
    }

    /**
     * Legt den Wert der maxDecelerationDist-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link AccelerationDistType }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setMaxDecelerationDist(AccelerationDistType value) {
        this.maxDecelerationDist = value;
    }

    /**
     * Ruft den Wert der id-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
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
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setId(String value) {
        this.id = value;
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
     * Ruft den Wert der default-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link Boolean }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public boolean isDefault() {
        if (_default == null) {
            return false;
        } else {
            return _default;
        }
    }

    /**
     * Legt den Wert der default-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link Boolean }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setDefault(Boolean value) {
        this._default = value;
    }

}

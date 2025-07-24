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
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.StripeTypeAdapter;
import org.opentrafficsim.xml.bindings.types.StripeType;


/**
 * <p>Java-Klasse für CseStripe complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CseStripe"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *             &lt;element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;element name="DrawingWidth" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" minOccurs="0"/&gt;
 *       &lt;/sequence&gt;
 *       &lt;attribute name="Id" type="{http://www.opentrafficsim.org/ots}IdType" /&gt;
 *       &lt;attribute name="Type" use="required" type="{http://www.opentrafficsim.org/ots}StripeType" /&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CseStripe", propOrder = {
    "centerOffset",
    "centerOffsetStart",
    "centerOffsetEnd",
    "drawingWidth"
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class CseStripe implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "CenterOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffset;
    @XmlElement(name = "CenterOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetStart;
    @XmlElement(name = "CenterOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetEnd;
    @XmlElement(name = "DrawingWidth", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType drawingWidth;
    @XmlAttribute(name = "Id")
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected String id;
    @XmlAttribute(name = "Type", required = true)
    @XmlJavaTypeAdapter(StripeTypeAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected StripeType type;

    /**
     * Ruft den Wert der centerOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffset() {
        return centerOffset;
    }

    /**
     * Legt den Wert der centerOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCenterOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffset = value;
    }

    /**
     * Ruft den Wert der centerOffsetStart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffsetStart() {
        return centerOffsetStart;
    }

    /**
     * Legt den Wert der centerOffsetStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCenterOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffsetStart = value;
    }

    /**
     * Ruft den Wert der centerOffsetEnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffsetEnd() {
        return centerOffsetEnd;
    }

    /**
     * Legt den Wert der centerOffsetEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setCenterOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffsetEnd = value;
    }

    /**
     * Ruft den Wert der drawingWidth-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getDrawingWidth() {
        return drawingWidth;
    }

    /**
     * Legt den Wert der drawingWidth-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setDrawingWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.drawingWidth = value;
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
     * Ruft den Wert der type-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public StripeType getType() {
        return type;
    }

    /**
     * Legt den Wert der type-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setType(StripeType value) {
        this.type = value;
    }

}

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
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;


/**
 * <p>Java-Klasse für CrossSectionElement complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType name="CrossSectionElement"&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;sequence&gt;
 *         &lt;choice minOccurs="0"&gt;
 *           &lt;element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *           &lt;element name="LeftOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *           &lt;element name="RightOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *           &lt;sequence&gt;
 *             &lt;choice&gt;
 *               &lt;element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *               &lt;element name="LeftOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *               &lt;element name="RightOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *             &lt;/choice&gt;
 *             &lt;choice&gt;
 *               &lt;element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *               &lt;element name="LeftOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *               &lt;element name="RightOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/&gt;
 *             &lt;/choice&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *         &lt;choice&gt;
 *           &lt;element name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/&gt;
 *           &lt;sequence&gt;
 *             &lt;element name="WidthStart" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/&gt;
 *             &lt;element name="WidthEnd" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/&gt;
 *           &lt;/sequence&gt;
 *         &lt;/choice&gt;
 *       &lt;/sequence&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CrossSectionElement", propOrder = {
    "centerOffset",
    "leftOffset",
    "rightOffset",
    "centerOffsetStart",
    "leftOffsetStart",
    "rightOffsetStart",
    "centerOffsetEnd",
    "leftOffsetEnd",
    "rightOffsetEnd",
    "width",
    "widthStart",
    "widthEnd"
})
@XmlSeeAlso({
    CseLane.class,
    CseShoulder.class
})
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class CrossSectionElement implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElement(name = "CenterOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffset;
    @XmlElement(name = "LeftOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffset;
    @XmlElement(name = "RightOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffset;
    @XmlElement(name = "CenterOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetStart;
    @XmlElement(name = "LeftOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffsetStart;
    @XmlElement(name = "RightOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffsetStart;
    @XmlElement(name = "CenterOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetEnd;
    @XmlElement(name = "LeftOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffsetEnd;
    @XmlElement(name = "RightOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffsetEnd;
    @XmlElement(name = "Width", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType width;
    @XmlElement(name = "WidthStart", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType widthStart;
    @XmlElement(name = "WidthEnd", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected org.opentrafficsim.xml.bindings.types.LengthType widthEnd;

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
     * Ruft den Wert der leftOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffset() {
        return leftOffset;
    }

    /**
     * Legt den Wert der leftOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLeftOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffset = value;
    }

    /**
     * Ruft den Wert der rightOffset-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffset() {
        return rightOffset;
    }

    /**
     * Legt den Wert der rightOffset-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRightOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffset = value;
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
     * Ruft den Wert der leftOffsetStart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffsetStart() {
        return leftOffsetStart;
    }

    /**
     * Legt den Wert der leftOffsetStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLeftOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffsetStart = value;
    }

    /**
     * Ruft den Wert der rightOffsetStart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffsetStart() {
        return rightOffsetStart;
    }

    /**
     * Legt den Wert der rightOffsetStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRightOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffsetStart = value;
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
     * Ruft den Wert der leftOffsetEnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffsetEnd() {
        return leftOffsetEnd;
    }

    /**
     * Legt den Wert der leftOffsetEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setLeftOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffsetEnd = value;
    }

    /**
     * Ruft den Wert der rightOffsetEnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffsetEnd() {
        return rightOffsetEnd;
    }

    /**
     * Legt den Wert der rightOffsetEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setRightOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffsetEnd = value;
    }

    /**
     * Ruft den Wert der width-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getWidth() {
        return width;
    }

    /**
     * Legt den Wert der width-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.width = value;
    }

    /**
     * Ruft den Wert der widthStart-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getWidthStart() {
        return widthStart;
    }

    /**
     * Legt den Wert der widthStart-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWidthStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.widthStart = value;
    }

    /**
     * Ruft den Wert der widthEnd-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public org.opentrafficsim.xml.bindings.types.LengthType getWidthEnd() {
        return widthEnd;
    }

    /**
     * Legt den Wert der widthEnd-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public void setWidthEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.widthEnd = value;
    }

}

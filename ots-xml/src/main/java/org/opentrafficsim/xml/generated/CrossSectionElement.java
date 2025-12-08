
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.LengthAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CrossSectionElement complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="CrossSectionElement">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <choice minOccurs="0">
 *           <element name="CenterOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *           <element name="LeftOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *           <element name="RightOffset" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *           <sequence>
 *             <choice>
 *               <element name="CenterOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *               <element name="LeftOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *               <element name="RightOffsetStart" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *             </choice>
 *             <choice>
 *               <element name="CenterOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *               <element name="LeftOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *               <element name="RightOffsetEnd" type="{http://www.opentrafficsim.org/ots}LengthType"/>
 *             </choice>
 *           </sequence>
 *         </choice>
 *         <choice>
 *           <element name="Width" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/>
 *           <sequence>
 *             <element name="WidthStart" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/>
 *             <element name="WidthEnd" type="{http://www.opentrafficsim.org/ots}PositiveLengthType"/>
 *           </sequence>
 *         </choice>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
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
@SuppressWarnings("all") public class CrossSectionElement implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "CenterOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffset;
    @XmlElement(name = "LeftOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffset;
    @XmlElement(name = "RightOffset", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffset;
    @XmlElement(name = "CenterOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetStart;
    @XmlElement(name = "LeftOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffsetStart;
    @XmlElement(name = "RightOffsetStart", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffsetStart;
    @XmlElement(name = "CenterOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType centerOffsetEnd;
    @XmlElement(name = "LeftOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType leftOffsetEnd;
    @XmlElement(name = "RightOffsetEnd", type = String.class)
    @XmlJavaTypeAdapter(LengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType rightOffsetEnd;
    @XmlElement(name = "Width", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType width;
    @XmlElement(name = "WidthStart", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType widthStart;
    @XmlElement(name = "WidthEnd", type = String.class)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.LengthType widthEnd;

    /**
     * Gets the value of the centerOffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffset() {
        return centerOffset;
    }

    /**
     * Sets the value of the centerOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffset = value;
    }

    /**
     * Gets the value of the leftOffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffset() {
        return leftOffset;
    }

    /**
     * Sets the value of the leftOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffset = value;
    }

    /**
     * Gets the value of the rightOffset property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffset() {
        return rightOffset;
    }

    /**
     * Sets the value of the rightOffset property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightOffset(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffset = value;
    }

    /**
     * Gets the value of the centerOffsetStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffsetStart() {
        return centerOffsetStart;
    }

    /**
     * Sets the value of the centerOffsetStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffsetStart = value;
    }

    /**
     * Gets the value of the leftOffsetStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffsetStart() {
        return leftOffsetStart;
    }

    /**
     * Sets the value of the leftOffsetStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffsetStart = value;
    }

    /**
     * Gets the value of the rightOffsetStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffsetStart() {
        return rightOffsetStart;
    }

    /**
     * Sets the value of the rightOffsetStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightOffsetStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffsetStart = value;
    }

    /**
     * Gets the value of the centerOffsetEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getCenterOffsetEnd() {
        return centerOffsetEnd;
    }

    /**
     * Sets the value of the centerOffsetEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setCenterOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.centerOffsetEnd = value;
    }

    /**
     * Gets the value of the leftOffsetEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getLeftOffsetEnd() {
        return leftOffsetEnd;
    }

    /**
     * Sets the value of the leftOffsetEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLeftOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.leftOffsetEnd = value;
    }

    /**
     * Gets the value of the rightOffsetEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getRightOffsetEnd() {
        return rightOffsetEnd;
    }

    /**
     * Sets the value of the rightOffsetEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setRightOffsetEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.rightOffsetEnd = value;
    }

    /**
     * Gets the value of the width property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getWidth() {
        return width;
    }

    /**
     * Sets the value of the width property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidth(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.width = value;
    }

    /**
     * Gets the value of the widthStart property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getWidthStart() {
        return widthStart;
    }

    /**
     * Sets the value of the widthStart property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidthStart(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.widthStart = value;
    }

    /**
     * Gets the value of the widthEnd property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.LengthType getWidthEnd() {
        return widthEnd;
    }

    /**
     * Sets the value of the widthEnd property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setWidthEnd(org.opentrafficsim.xml.bindings.types.LengthType value) {
        this.widthEnd = value;
    }

}


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java-Klasse für CorrelationParameterType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="CorrelationParameterType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <choice>
 *         <element name="Duration" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Length" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Speed" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Acceleration" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="LinearDensity" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Frequency" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Double" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Fraction" type="{http://www.opentrafficsim.org/ots}string"/>
 *         <element name="Integer" type="{http://www.opentrafficsim.org/ots}string"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CorrelationParameterType", propOrder = {
    "duration",
    "length",
    "speed",
    "acceleration",
    "linearDensity",
    "frequency",
    "_double",
    "fraction",
    "integer"
})
@SuppressWarnings("all") public class CorrelationParameterType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "Duration", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType duration;
    @XmlElement(name = "Length", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType length;
    @XmlElement(name = "Speed", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType speed;
    @XmlElement(name = "Acceleration", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType acceleration;
    @XmlElement(name = "LinearDensity", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType linearDensity;
    @XmlElement(name = "Frequency", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType frequency;
    @XmlElement(name = "Double", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType _double;
    @XmlElement(name = "Fraction", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType fraction;
    @XmlElement(name = "Integer", type = String.class)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType integer;

    /**
     * Ruft den Wert der duration-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDuration() {
        return duration;
    }

    /**
     * Legt den Wert der duration-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDuration(StringType value) {
        this.duration = value;
    }

    /**
     * Ruft den Wert der length-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLength() {
        return length;
    }

    /**
     * Legt den Wert der length-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLength(StringType value) {
        this.length = value;
    }

    /**
     * Ruft den Wert der speed-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getSpeed() {
        return speed;
    }

    /**
     * Legt den Wert der speed-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpeed(StringType value) {
        this.speed = value;
    }

    /**
     * Ruft den Wert der acceleration-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getAcceleration() {
        return acceleration;
    }

    /**
     * Legt den Wert der acceleration-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAcceleration(StringType value) {
        this.acceleration = value;
    }

    /**
     * Ruft den Wert der linearDensity-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLinearDensity() {
        return linearDensity;
    }

    /**
     * Legt den Wert der linearDensity-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLinearDensity(StringType value) {
        this.linearDensity = value;
    }

    /**
     * Ruft den Wert der frequency-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getFrequency() {
        return frequency;
    }

    /**
     * Legt den Wert der frequency-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrequency(StringType value) {
        this.frequency = value;
    }

    /**
     * Ruft den Wert der double-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getDouble() {
        return _double;
    }

    /**
     * Legt den Wert der double-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDouble(StringType value) {
        this._double = value;
    }

    /**
     * Ruft den Wert der fraction-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getFraction() {
        return fraction;
    }

    /**
     * Legt den Wert der fraction-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFraction(StringType value) {
        this.fraction = value;
    }

    /**
     * Ruft den Wert der integer-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getInteger() {
        return integer;
    }

    /**
     * Legt den Wert der integer-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setInteger(StringType value) {
        this.integer = value;
    }

}


package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for CorrelationParameterType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
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
     * Gets the value of the duration property.
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
     * Sets the value of the duration property.
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
     * Gets the value of the length property.
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
     * Sets the value of the length property.
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
     * Gets the value of the speed property.
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
     * Sets the value of the speed property.
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
     * Gets the value of the acceleration property.
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
     * Sets the value of the acceleration property.
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
     * Gets the value of the linearDensity property.
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
     * Sets the value of the linearDensity property.
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
     * Gets the value of the frequency property.
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
     * Sets the value of the frequency property.
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
     * Gets the value of the double property.
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
     * Sets the value of the double property.
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
     * Gets the value of the fraction property.
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
     * Sets the value of the fraction property.
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
     * Gets the value of the integer property.
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
     * Sets the value of the integer property.
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


package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.ClassAdapter;
import org.opentrafficsim.xml.bindings.LengthBeginEndAdapter;
import org.opentrafficsim.xml.bindings.PositiveLengthAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.ClassType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthType;
import org.opentrafficsim.xml.bindings.types.StringType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for TrafficLightDetectorType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="TrafficLightDetectorType">
 *   <complexContent>
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="Lane" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *       <attribute name="Position" use="required" type="{http://www.opentrafficsim.org/ots}LengthBeginEndType" />
 *       <attribute name="Length" use="required" type="{http://www.opentrafficsim.org/ots}PositiveLengthType" />
 *       <attribute name="Class" use="required" type="{http://www.opentrafficsim.org/ots}ClassNameType" />
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrafficLightDetectorType")
@SuppressWarnings("all") public class TrafficLightDetectorType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "Lane", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType lane;
    @XmlAttribute(name = "Position", required = true)
    @XmlJavaTypeAdapter(LengthBeginEndAdapter.class)
    protected LengthBeginEndType position;
    @XmlAttribute(name = "Length", required = true)
    @XmlJavaTypeAdapter(PositiveLengthAdapter.class)
    protected LengthType length;
    /**
     * Fully specified classname including the package.
     * 
     */
    @XmlAttribute(name = "Class", required = true)
    @XmlJavaTypeAdapter(ClassAdapter.class)
    protected ClassType clazz;

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
     * Gets the value of the lane property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLane() {
        return lane;
    }

    /**
     * Sets the value of the lane property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLane(StringType value) {
        this.lane = value;
    }

    /**
     * Gets the value of the position property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthBeginEndType getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPosition(LengthBeginEndType value) {
        this.position = value;
    }

    /**
     * Gets the value of the length property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthType getLength() {
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
    public void setLength(LengthType value) {
        this.length = value;
    }

    /**
     * Fully specified classname including the package.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public ClassType getClazz() {
        return clazz;
    }

    /**
     * Sets the value of the clazz property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getClazz()
     */
    public void setClazz(ClassType value) {
        this.clazz = value;
    }

}

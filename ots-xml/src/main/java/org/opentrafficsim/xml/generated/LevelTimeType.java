
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.DurationAdapter;
import org.opentrafficsim.xml.bindings.types.DurationType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for LevelTimeType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LevelTimeType">
 *   <simpleContent>
 *     <extension base="<http://www.opentrafficsim.org/ots>LevelType">
 *       <attribute name="Time" type="{http://www.opentrafficsim.org/ots}DurationType" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LevelTimeType", propOrder = {
    "value"
})
@SuppressWarnings("all") public class LevelTimeType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlValue
    protected String value;
    @XmlAttribute(name = "Time")
    @XmlJavaTypeAdapter(DurationAdapter.class)
    protected DurationType time;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getValue() {
        return value;
    }

    /**
     * Sets the value of the value property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Gets the value of the time property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationType getTime() {
        return time;
    }

    /**
     * Sets the value of the time property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(DurationType value) {
        this.time = value;
    }

}

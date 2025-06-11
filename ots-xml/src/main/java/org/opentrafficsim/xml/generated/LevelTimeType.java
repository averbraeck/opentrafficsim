
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.TimeAdapter;
import org.opentrafficsim.xml.bindings.types.TimeType;


/**
 * <p>Java-Klasse für LevelTimeType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="LevelTimeType">
 *   <simpleContent>
 *     <extension base="<http://www.opentrafficsim.org/ots>LevelType">
 *       <attribute name="Time" type="{http://www.opentrafficsim.org/ots}TimeType" />
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
    @XmlJavaTypeAdapter(TimeAdapter.class)
    protected TimeType time;

    /**
     * Ruft den Wert der value-Eigenschaft ab.
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
     * Legt den Wert der value-Eigenschaft fest.
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
     * Ruft den Wert der time-Eigenschaft ab.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public TimeType getTime() {
        return time;
    }

    /**
     * Legt den Wert der time-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setTime(TimeType value) {
        this.time = value;
    }

}

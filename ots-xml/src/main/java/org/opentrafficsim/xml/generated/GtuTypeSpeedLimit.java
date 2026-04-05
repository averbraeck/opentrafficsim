
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.SpeedType;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * Legal speed limit that applies to a GTU type within a context. For example
 *           80km/h for trucks anywhere within a country, i.e. a network. (Unless lower local speed limits apply.)
 * 
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <simpleContent>
 *     <extension base="<http://www.opentrafficsim.org/ots>PositiveSpeedType">
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "value"
})
@XmlRootElement(name = "GtuTypeSpeedLimit")
@SuppressWarnings("all") public class GtuTypeSpeedLimit
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlValue
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    protected SpeedType value;
    @XmlAttribute(name = "Id", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType id;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedType getValue() {
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
    public void setValue(SpeedType value) {
        this.value = value;
    }

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getId() {
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
    public void setId(StringType value) {
        this.id = value;
    }

}

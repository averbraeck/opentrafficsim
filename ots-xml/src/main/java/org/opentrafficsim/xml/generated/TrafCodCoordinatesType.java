
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.SpaceAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;


/**
 * <p>Java class for TrafCodCoordinatesType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="TrafCodCoordinatesType">
 *   <simpleContent>
 *     <extension base="<http://www.opentrafficsim.org/ots>MultiLineString">
 *       <attribute name="Space" type="{http://www.opentrafficsim.org/ots}space" default="preserve" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TrafCodCoordinatesType", propOrder = {
    "value"
})
@SuppressWarnings("all") public class TrafCodCoordinatesType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlValue
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.StringType value;
    @XmlAttribute(name = "Space")
    @XmlJavaTypeAdapter(SpaceAdapter.class)
    protected org.opentrafficsim.xml.bindings.types.StringType space;

    /**
     * Gets the value of the value property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.StringType getValue() {
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
    public void setValue(org.opentrafficsim.xml.bindings.types.StringType value) {
        this.value = value;
    }

    /**
     * Gets the value of the space property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public org.opentrafficsim.xml.bindings.types.StringType getSpace() {
        if (space == null) {
            return new SpaceAdapter().unmarshal("preserve");
        } else {
            return space;
        }
    }

    /**
     * Sets the value of the space property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpace(org.opentrafficsim.xml.bindings.types.StringType value) {
        this.space = value;
    }

}

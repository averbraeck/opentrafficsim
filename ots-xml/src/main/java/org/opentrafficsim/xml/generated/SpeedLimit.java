
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.BooleanAdapter;
import org.opentrafficsim.xml.bindings.SpeedAdapter;
import org.opentrafficsim.xml.bindings.types.BooleanType;
import org.opentrafficsim.xml.bindings.types.SpeedType;


/**
 * <p>Java class for SpeedLimit complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="SpeedLimit">
 *   <simpleContent>
 *     <extension base="<http://www.opentrafficsim.org/ots>PositiveSpeedType">
 *       <attribute name="Enforced" type="{http://www.opentrafficsim.org/ots}boolean" default="false" />
 *       <attribute name="GtuTypeAware" type="{http://www.opentrafficsim.org/ots}boolean" default="false" />
 *     </extension>
 *   </simpleContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpeedLimit", propOrder = {
    "value"
})
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.TemporalSpeedLimit.SpeedLimit.class
})
@SuppressWarnings("all") public class SpeedLimit
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlValue
    @XmlJavaTypeAdapter(SpeedAdapter.class)
    protected SpeedType value;
    @XmlAttribute(name = "Enforced")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType enforced;
    @XmlAttribute(name = "GtuTypeAware")
    @XmlJavaTypeAdapter(BooleanAdapter.class)
    protected BooleanType gtuTypeAware;

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
     * Gets the value of the enforced property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getEnforced() {
        if (enforced == null) {
            return new BooleanAdapter().unmarshal("false");
        } else {
            return enforced;
        }
    }

    /**
     * Sets the value of the enforced property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setEnforced(BooleanType value) {
        this.enforced = value;
    }

    /**
     * Gets the value of the gtuTypeAware property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public BooleanType getGtuTypeAware() {
        if (gtuTypeAware == null) {
            return new BooleanAdapter().unmarshal("false");
        } else {
            return gtuTypeAware;
        }
    }

    /**
     * Sets the value of the gtuTypeAware property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setGtuTypeAware(BooleanType value) {
        this.gtuTypeAware = value;
    }

}

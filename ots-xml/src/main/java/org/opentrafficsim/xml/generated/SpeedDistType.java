
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.SpeedUnitAdapter;
import org.opentrafficsim.xml.bindings.types.SpeedUnitType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for SpeedDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="SpeedDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="SpeedUnit" use="required" type="{http://www.opentrafficsim.org/ots}SpeedUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpeedDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.SpeedDist.class
})
@SuppressWarnings("all") public class SpeedDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "SpeedUnit", required = true)
    @XmlJavaTypeAdapter(SpeedUnitAdapter.class)
    protected SpeedUnitType speedUnit;

    /**
     * Gets the value of the speedUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public SpeedUnitType getSpeedUnit() {
        return speedUnit;
    }

    /**
     * Sets the value of the speedUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setSpeedUnit(SpeedUnitType value) {
        this.speedUnit = value;
    }

}


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.AccelerationUnitAdapter;
import org.opentrafficsim.xml.bindings.types.AccelerationUnitType;


/**
 * <p>Java class for AccelerationDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="AccelerationDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="AccelerationUnit" use="required" type="{http://www.opentrafficsim.org/ots}AccelerationUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AccelerationDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.AccelerationDist.class
})
@SuppressWarnings("all") public class AccelerationDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "AccelerationUnit", required = true)
    @XmlJavaTypeAdapter(AccelerationUnitAdapter.class)
    protected AccelerationUnitType accelerationUnit;

    /**
     * Gets the value of the accelerationUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public AccelerationUnitType getAccelerationUnit() {
        return accelerationUnit;
    }

    /**
     * Sets the value of the accelerationUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setAccelerationUnit(AccelerationUnitType value) {
        this.accelerationUnit = value;
    }

}


package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.DurationUnitAdapter;
import org.opentrafficsim.xml.bindings.types.DurationUnitType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for DurationDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="DurationDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="DurationUnit" use="required" type="{http://www.opentrafficsim.org/ots}DurationUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DurationDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.DurationDist.class
})
@SuppressWarnings("all") public class DurationDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "DurationUnit", required = true)
    @XmlJavaTypeAdapter(DurationUnitAdapter.class)
    protected DurationUnitType durationUnit;

    /**
     * Gets the value of the durationUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public DurationUnitType getDurationUnit() {
        return durationUnit;
    }

    /**
     * Sets the value of the durationUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setDurationUnit(DurationUnitType value) {
        this.durationUnit = value;
    }

}

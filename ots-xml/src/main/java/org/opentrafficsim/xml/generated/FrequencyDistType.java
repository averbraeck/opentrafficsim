
package org.opentrafficsim.xml.generated;

import java.io.Serializable;

import org.opentrafficsim.xml.bindings.FrequencyUnitAdapter;
import org.opentrafficsim.xml.bindings.types.FrequencyUnitType;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * <p>Java class for FrequencyDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="FrequencyDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="FrequencyUnit" use="required" type="{http://www.opentrafficsim.org/ots}FrequencyUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FrequencyDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelType.ModelParameters.FrequencyDist.class
})
@SuppressWarnings("all") public class FrequencyDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "FrequencyUnit", required = true)
    @XmlJavaTypeAdapter(FrequencyUnitAdapter.class)
    protected FrequencyUnitType frequencyUnit;

    /**
     * Gets the value of the frequencyUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public FrequencyUnitType getFrequencyUnit() {
        return frequencyUnit;
    }

    /**
     * Sets the value of the frequencyUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setFrequencyUnit(FrequencyUnitType value) {
        this.frequencyUnit = value;
    }

}

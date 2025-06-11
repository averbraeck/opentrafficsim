
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.FrequencyUnitAdapter;
import org.opentrafficsim.xml.bindings.types.FrequencyUnitType;


/**
 * <p>Java-Klasse für FrequencyDistType complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
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
     * Ruft den Wert der frequencyUnit-Eigenschaft ab.
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
     * Legt den Wert der frequencyUnit-Eigenschaft fest.
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

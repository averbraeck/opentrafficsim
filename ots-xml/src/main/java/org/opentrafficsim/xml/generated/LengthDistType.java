
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.LengthUnitAdapter;
import org.opentrafficsim.xml.bindings.types.LengthUnitType;


/**
 * <p>Java class for LengthDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="LengthDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="LengthUnit" use="required" type="{http://www.opentrafficsim.org/ots}LengthUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LengthDistType")
@XmlSeeAlso({
    org.opentrafficsim.xml.generated.ModelParameters.LengthDist.class
})
@SuppressWarnings("all") public class LengthDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "LengthUnit", required = true)
    @XmlJavaTypeAdapter(LengthUnitAdapter.class)
    protected LengthUnitType lengthUnit;

    /**
     * Gets the value of the lengthUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public LengthUnitType getLengthUnit() {
        return lengthUnit;
    }

    /**
     * Sets the value of the lengthUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLengthUnit(LengthUnitType value) {
        this.lengthUnit = value;
    }

}

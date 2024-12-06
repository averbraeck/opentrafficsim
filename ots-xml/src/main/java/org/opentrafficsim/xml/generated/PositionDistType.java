
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for PositionDistType complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="PositionDistType">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}ConstantDistType">
 *       <attribute name="PositionUnit" use="required" type="{http://www.opentrafficsim.org/ots}PositionUnitType" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PositionDistType")
@SuppressWarnings("all") public class PositionDistType
    extends ConstantDistType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "PositionUnit", required = true)
    protected String positionUnit;

    /**
     * Gets the value of the positionUnit property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getPositionUnit() {
        return positionUnit;
    }

    /**
     * Sets the value of the positionUnit property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setPositionUnit(String value) {
        this.positionUnit = value;
    }

}

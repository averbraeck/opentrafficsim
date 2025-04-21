
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Defines the default maximum speed allowed in conjunction with the specified road type.
 * 
 * <p>Java class for t_road_type_speed complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_type_speed">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="max" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_maxSpeed" />
 *       <attribute name="unit" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_unitSpeed" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_type_speed")
@SuppressWarnings("all") public class TRoadTypeSpeed
    extends OpenDriveElement
{

    /**
     * Maximum allowed speed. Given as string (only "no limit" / "undefined") or numerical value in the respective unit (see attribute unit). If the attribute unit is not specified, m/s is used as default.
     * 
     */
    @XmlAttribute(name = "max", required = true)
    protected String max;
    /**
     * Unit of the attribute max. For values, see chapter “units”.
     * 
     */
    @XmlAttribute(name = "unit")
    protected EUnitSpeed unit;

    /**
     * Maximum allowed speed. Given as string (only "no limit" / "undefined") or numerical value in the respective unit (see attribute unit). If the attribute unit is not specified, m/s is used as default.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getMax() {
        return max;
    }

    /**
     * Sets the value of the max property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getMax()
     */
    public void setMax(String value) {
        this.max = value;
    }

    /**
     * Unit of the attribute max. For values, see chapter “units”.
     * 
     * @return
     *     possible object is
     *     {@link EUnitSpeed }
     *     
     */
    public EUnitSpeed getUnit() {
        return unit;
    }

    /**
     * Sets the value of the unit property.
     * 
     * @param value
     *     allowed object is
     *     {@link EUnitSpeed }
     *     
     * @see #getUnit()
     */
    public void setUnit(EUnitSpeed value) {
        this.unit = value;
    }

}

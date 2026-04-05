
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.opentrafficsim.xml.bindings.StringAdapter;
import org.opentrafficsim.xml.bindings.types.StringType;


/**
 * <p>Java class for CseLane complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="CseLane">
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}CrossSectionElement">
 *       <attribute name="Id" use="required" type="{http://www.opentrafficsim.org/ots}IdType" />
 *       <attribute name="LaneType" use="required" type="{http://www.opentrafficsim.org/ots}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "CseLane")
@SuppressWarnings("all") public class CseLane
    extends CrossSectionElement
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlAttribute(name = "Id", required = true)
    protected String id;
    @XmlAttribute(name = "LaneType", required = true)
    @XmlJavaTypeAdapter(StringAdapter.class)
    protected StringType laneType;

    /**
     * Gets the value of the id property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the value of the id property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Gets the value of the laneType property.
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public StringType getLaneType() {
        return laneType;
    }

    /**
     * Sets the value of the laneType property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     */
    public void setLaneType(StringType value) {
        this.laneType = value;
    }

}

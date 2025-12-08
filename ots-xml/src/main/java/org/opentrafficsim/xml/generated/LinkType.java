
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for anonymous complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType>
 *   <complexContent>
 *     <extension base="{http://www.opentrafficsim.org/ots}GtuCompatibleInfraType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}SpeedLimit" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "speedLimit"
})
@XmlRootElement(name = "LinkType")
@SuppressWarnings("all") public class LinkType
    extends GtuCompatibleInfraType
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElement(name = "SpeedLimit")
    protected List<SpeedLimit> speedLimit;

    /**
     * Gets the value of the speedLimit property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the speedLimit property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getSpeedLimit().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link SpeedLimit }
     * </p>
     * 
     * 
     * @return
     *     The value of the speedLimit property.
     */
    public List<SpeedLimit> getSpeedLimit() {
        if (speedLimit == null) {
            speedLimit = new ArrayList<>();
        }
        return this.speedLimit;
    }

}

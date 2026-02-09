
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
 *     <restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       <sequence>
 *         <element ref="{http://www.opentrafficsim.org/ots}LaneBias" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "laneBias"
})
@XmlRootElement(name = "LaneBiases")
@SuppressWarnings("all") public class LaneBiases implements Serializable
{

    private static final long serialVersionUID = 10102L;
    /**
     * Sets the preferred lateral position for vehicle generation for the given GTU
     *         type either based on speed, or a fixed lateral position (between 0 and 1 covering the width of the road, in terms of
     *         lanes).
     * 
     */
    @XmlElement(name = "LaneBias")
    protected List<LaneBias> laneBias;

    /**
     * Sets the preferred lateral position for vehicle generation for the given GTU
     *         type either based on speed, or a fixed lateral position (between 0 and 1 covering the width of the road, in terms of
     *         lanes).
     * 
     * Gets the value of the laneBias property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneBias property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneBias().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link LaneBias }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneBias property.
     */
    public List<LaneBias> getLaneBias() {
        if (laneBias == null) {
            laneBias = new ArrayList<>();
        }
        return this.laneBias;
    }

}

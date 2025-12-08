
package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.xml.bindings.types.ClassType;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlElementRefs;
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
 *       <choice maxOccurs="unbounded">
 *         <element name="Default">
 *           <simpleType>
 *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *               <simpleType>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                   <enumeration value="DEFAULT"/>
 *                   <enumeration value="ID"/>
 *                   <enumeration value="SPEED"/>
 *                   <enumeration value="ACCELERATION"/>
 *                   <enumeration value="BLUE"/>
 *                   <enumeration value="DESIRED_SPEED"/>
 *                   <enumeration value="SPLIT"/>
 *                   <enumeration value="SYNCHRONIZATION"/>
 *                   <enumeration value="DESIRED_HEADWAY"/>
 *                   <enumeration value="TOTAL_DESIRE"/>
 *                   <enumeration value="SOCIAL_PRESSURE"/>
 *                 </restriction>
 *               </simpleType>
 *             </union>
 *           </simpleType>
 *         </element>
 *         <element name="Incentive">
 *           <simpleType>
 *             <union memberTypes=" {http://www.opentrafficsim.org/ots}Expression">
 *               <simpleType>
 *                 <restriction base="{http://www.w3.org/2001/XMLSchema}string">
 *                   <enumeration value="ROUTE"/>
 *                   <enumeration value="SPEED_WITH_COURTESY"/>
 *                   <enumeration value="KEEP"/>
 *                   <enumeration value="COURTESY"/>
 *                   <enumeration value="SOCIO_SPEED"/>
 *                   <enumeration value="BUS_STOP"/>
 *                 </restriction>
 *               </simpleType>
 *             </union>
 *           </simpleType>
 *         </element>
 *         <element name="Class" type="{http://www.opentrafficsim.org/ots}ClassNameType"/>
 *       </choice>
 *     </restriction>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "defaultOrIncentiveOrClazz"
})
@XmlRootElement(name = "GtuColorers")
@SuppressWarnings("all") public class GtuColorers
    implements Serializable
{

    private static final long serialVersionUID = 10102L;
    @XmlElementRefs({
        @XmlElementRef(name = "Default", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Incentive", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Class", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false)
    })
    protected List<JAXBElement<?>> defaultOrIncentiveOrClazz;

    /**
     * Gets the value of the defaultOrIncentiveOrClazz property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultOrIncentiveOrClazz property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getDefaultOrIncentiveOrClazz().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link ClassType }{@code >}
     * </p>
     * 
     * 
     * @return
     *     The value of the defaultOrIncentiveOrClazz property.
     */
    public List<JAXBElement<?>> getDefaultOrIncentiveOrClazz() {
        if (defaultOrIncentiveOrClazz == null) {
            defaultOrIncentiveOrClazz = new ArrayList<>();
        }
        return this.defaultOrIncentiveOrClazz;
    }

}

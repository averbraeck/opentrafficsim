//
// Diese Datei wurde mit der Eclipse Implementation of JAXB, v2.3.7 generiert 
// Siehe https://eclipse-ee4j.github.io/jaxb-ri 
// Änderungen an dieser Datei gehen bei einer Neukompilierung des Quellschemas verloren. 
// Generiert: 2025.07.24 um 01:18:43 PM CEST 
//


package org.opentrafficsim.xml.generated;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElementRef;
import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import org.opentrafficsim.xml.bindings.types.ClassType;


/**
 * <p>Java-Klasse für anonymous complex type.
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.
 * 
 * <pre>
 * &lt;complexType&gt;
 *   &lt;complexContent&gt;
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType"&gt;
 *       &lt;choice maxOccurs="unbounded"&gt;
 *         &lt;element name="Default"&gt;
 *           &lt;simpleType&gt;
 *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *               &lt;simpleType&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                   &lt;enumeration value="DEFAULT"/&gt;
 *                   &lt;enumeration value="ID"/&gt;
 *                   &lt;enumeration value="SPEED"/&gt;
 *                   &lt;enumeration value="ACCELERATION"/&gt;
 *                   &lt;enumeration value="BLUE"/&gt;
 *                   &lt;enumeration value="DESIRED_SPEED"/&gt;
 *                   &lt;enumeration value="SPLIT"/&gt;
 *                   &lt;enumeration value="SYNCHRONIZATION"/&gt;
 *                   &lt;enumeration value="DESIRED_HEADWAY"/&gt;
 *                   &lt;enumeration value="TOTAL_DESIRE"/&gt;
 *                   &lt;enumeration value="SOCIAL_PRESSURE"/&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/simpleType&gt;
 *             &lt;/union&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Incentive"&gt;
 *           &lt;simpleType&gt;
 *             &lt;union memberTypes=" {http://www.opentrafficsim.org/ots}Expression"&gt;
 *               &lt;simpleType&gt;
 *                 &lt;restriction base="{http://www.w3.org/2001/XMLSchema}string"&gt;
 *                   &lt;enumeration value="ROUTE"/&gt;
 *                   &lt;enumeration value="SPEED_WITH_COURTESY"/&gt;
 *                   &lt;enumeration value="KEEP"/&gt;
 *                   &lt;enumeration value="COURTESY"/&gt;
 *                   &lt;enumeration value="SOCIO_SPEED"/&gt;
 *                   &lt;enumeration value="BUS_STOP"/&gt;
 *                 &lt;/restriction&gt;
 *               &lt;/simpleType&gt;
 *             &lt;/union&gt;
 *           &lt;/simpleType&gt;
 *         &lt;/element&gt;
 *         &lt;element name="Class" type="{http://www.opentrafficsim.org/ots}ClassNameType"/&gt;
 *       &lt;/choice&gt;
 *     &lt;/restriction&gt;
 *   &lt;/complexContent&gt;
 * &lt;/complexType&gt;
 * </pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
    "defaultOrIncentiveOrClazz"
})
@XmlRootElement(name = "GtuColorers")
@Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
public class GtuColorers
    implements Serializable
{

    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    private final static long serialVersionUID = 10102L;
    @XmlElementRefs({
        @XmlElementRef(name = "Default", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Incentive", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false),
        @XmlElementRef(name = "Class", namespace = "http://www.opentrafficsim.org/ots", type = JAXBElement.class, required = false)
    })
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    protected List<JAXBElement<?>> defaultOrIncentiveOrClazz;

    /**
     * Gets the value of the defaultOrIncentiveOrClazz property.
     * 
     * <p>
     * This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the defaultOrIncentiveOrClazz property.
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * <pre>
     *    getDefaultOrIncentiveOrClazz().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link String }{@code >}
     * {@link JAXBElement }{@code <}{@link ClassType }{@code >}
     * 
     * 
     */
    @Generated(value = "com.sun.tools.xjc.Driver", comments = "JAXB RI v2.3.7", date = "2025-07-24T13:18:43+02:00")
    public List<JAXBElement<?>> getDefaultOrIncentiveOrClazz() {
        if (defaultOrIncentiveOrClazz == null) {
            defaultOrIncentiveOrClazz = new ArrayList<JAXBElement<?>>();
        }
        return this.defaultOrIncentiveOrClazz;
    }

}

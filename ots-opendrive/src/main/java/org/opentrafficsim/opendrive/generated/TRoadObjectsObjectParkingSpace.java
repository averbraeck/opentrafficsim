
package org.opentrafficsim.opendrive.generated;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Details for a parking space may be added to the object element.
 * 
 * <p>Java-Klasse für t_road_objects_object_parkingSpace complex type.</p>
 * 
 * <p>Das folgende Schemafragment gibt den erwarteten Content an, der in dieser Klasse enthalten ist.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects_object_parkingSpace">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *       </sequence>
 *       <attribute name="access" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_objects_object_parkingSpace_access" />
 *       <attribute name="restrictions" type="{http://www.w3.org/2001/XMLSchema}string" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_objects_object_parkingSpace")
@SuppressWarnings("all") public class TRoadObjectsObjectParkingSpace
    extends OpenDriveElement
{

    /**
     * Access definitions for the parking space. Parking spaces tagged with "women" and "handicapped" are vehicles of type car. For values see UML Model
     * 
     */
    @XmlAttribute(name = "access", required = true)
    protected ERoadObjectsObjectParkingSpaceAccess access;
    /**
     * Free text, depending on application
     * 
     */
    @XmlAttribute(name = "restrictions")
    protected String restrictions;

    /**
     * Access definitions for the parking space. Parking spaces tagged with "women" and "handicapped" are vehicles of type car. For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link ERoadObjectsObjectParkingSpaceAccess }
     *     
     */
    public ERoadObjectsObjectParkingSpaceAccess getAccess() {
        return access;
    }

    /**
     * Legt den Wert der access-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadObjectsObjectParkingSpaceAccess }
     *     
     * @see #getAccess()
     */
    public void setAccess(ERoadObjectsObjectParkingSpaceAccess value) {
        this.access = value;
    }

    /**
     * Free text, depending on application
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getRestrictions() {
        return restrictions;
    }

    /**
     * Legt den Wert der restrictions-Eigenschaft fest.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getRestrictions()
     */
    public void setRestrictions(String value) {
        this.restrictions = value;
    }

}


package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * <p>Java class for t_road_railroad_switch complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_railroad_switch">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="mainTrack" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_railroad_switch_mainTrack"/>
 *         <element name="sideTrack" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_railroad_switch_sideTrack"/>
 *         <element name="partner" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_railroad_switch_partner" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *       <attribute name="name" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="id" use="required" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       <attribute name="position" use="required" type="{http://code.asam.net/simulation/standard/opendrive_schema}e_road_railroad_switch_position" />
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_railroad_switch", propOrder = {
    "mainTrack",
    "sideTrack",
    "partner",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadRailroadSwitch
    extends OpenDriveElement
{

    @XmlElement(required = true)
    protected TRoadRailroadSwitchMainTrack mainTrack;
    @XmlElement(required = true)
    protected TRoadRailroadSwitchSideTrack sideTrack;
    protected TRoadRailroadSwitchPartner partner;
    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     */
    @XmlElements({
        @XmlElement(name = "include", type = TInclude.class),
        @XmlElement(name = "userData", type = TUserData.class),
        @XmlElement(name = "dataQuality", type = TDataQuality.class)
    })
    protected List<Object> gAdditionalData;
    /**
     * Unique name of the switch
     * 
     */
    @XmlAttribute(name = "name", required = true)
    protected String name;
    /**
     * Unique ID of the switch; preferably an integer number, see uint32_t
     * 
     */
    @XmlAttribute(name = "id", required = true)
    protected String id;
    /**
     * Either a switch can be operated (dynamic) or it is in a static position. For values see UML Model
     * 
     */
    @XmlAttribute(name = "position", required = true)
    protected ERoadRailroadSwitchPosition position;

    /**
     * Gets the value of the mainTrack property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadRailroadSwitchMainTrack }
     *     
     */
    public TRoadRailroadSwitchMainTrack getMainTrack() {
        return mainTrack;
    }

    /**
     * Sets the value of the mainTrack property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadRailroadSwitchMainTrack }
     *     
     */
    public void setMainTrack(TRoadRailroadSwitchMainTrack value) {
        this.mainTrack = value;
    }

    /**
     * Gets the value of the sideTrack property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadRailroadSwitchSideTrack }
     *     
     */
    public TRoadRailroadSwitchSideTrack getSideTrack() {
        return sideTrack;
    }

    /**
     * Sets the value of the sideTrack property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadRailroadSwitchSideTrack }
     *     
     */
    public void setSideTrack(TRoadRailroadSwitchSideTrack value) {
        this.sideTrack = value;
    }

    /**
     * Gets the value of the partner property.
     * 
     * @return
     *     possible object is
     *     {@link TRoadRailroadSwitchPartner }
     *     
     */
    public TRoadRailroadSwitchPartner getPartner() {
        return partner;
    }

    /**
     * Sets the value of the partner property.
     * 
     * @param value
     *     allowed object is
     *     {@link TRoadRailroadSwitchPartner }
     *     
     */
    public void setPartner(TRoadRailroadSwitchPartner value) {
        this.partner = value;
    }

    /**
     * OpenDRIVE offers the possibility to include external data. The processing of this data depends on the application.
     * Additional data may be placed at any position in OpenDRIVE.
     * 
     * Gets the value of the gAdditionalData property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the gAdditionalData property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getGAdditionalData().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TDataQuality }
     * {@link TInclude }
     * {@link TUserData }
     * </p>
     * 
     * 
     * @return
     *     The value of the gAdditionalData property.
     */
    public List<Object> getGAdditionalData() {
        if (gAdditionalData == null) {
            gAdditionalData = new ArrayList<>();
        }
        return this.gAdditionalData;
    }

    /**
     * Unique name of the switch
     * 
     * @return
     *     possible object is
     *     {@link String }
     *     
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the value of the name property.
     * 
     * @param value
     *     allowed object is
     *     {@link String }
     *     
     * @see #getName()
     */
    public void setName(String value) {
        this.name = value;
    }

    /**
     * Unique ID of the switch; preferably an integer number, see uint32_t
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
     * @see #getId()
     */
    public void setId(String value) {
        this.id = value;
    }

    /**
     * Either a switch can be operated (dynamic) or it is in a static position. For values see UML Model
     * 
     * @return
     *     possible object is
     *     {@link ERoadRailroadSwitchPosition }
     *     
     */
    public ERoadRailroadSwitchPosition getPosition() {
        return position;
    }

    /**
     * Sets the value of the position property.
     * 
     * @param value
     *     allowed object is
     *     {@link ERoadRailroadSwitchPosition }
     *     
     * @see #getPosition()
     */
    public void setPosition(ERoadRailroadSwitchPosition value) {
        this.position = value;
    }

}

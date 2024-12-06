
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
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
 *         <element name="header" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_header"/>
 *         <element name="road" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road" maxOccurs="unbounded"/>
 *         <element name="controller" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_controller" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="junction" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junction" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="junctionGroup" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_junctionGroup" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="station" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_station" maxOccurs="unbounded" minOccurs="0"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
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
    "header",
    "road",
    "controller",
    "junction",
    "junctionGroup",
    "station",
    "gAdditionalData"
})
@XmlRootElement(name = "OpenDRIVE")
@SuppressWarnings("all") public class OpenDrive {

    @XmlElement(required = true)
    protected THeader header;
    @XmlElement(required = true)
    protected List<TRoad> road;
    protected List<TController> controller;
    protected List<TJunction> junction;
    protected List<TJunctionGroup> junctionGroup;
    protected List<TStation> station;
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
     * Gets the value of the header property.
     * 
     * @return
     *     possible object is
     *     {@link THeader }
     *     
     */
    public THeader getHeader() {
        return header;
    }

    /**
     * Sets the value of the header property.
     * 
     * @param value
     *     allowed object is
     *     {@link THeader }
     *     
     */
    public void setHeader(THeader value) {
        this.header = value;
    }

    /**
     * Gets the value of the road property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the road property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getRoad().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoad }
     * </p>
     * 
     * 
     * @return
     *     The value of the road property.
     */
    public List<TRoad> getRoad() {
        if (road == null) {
            road = new ArrayList<>();
        }
        return this.road;
    }

    /**
     * Gets the value of the controller property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the controller property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getController().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TController }
     * </p>
     * 
     * 
     * @return
     *     The value of the controller property.
     */
    public List<TController> getController() {
        if (controller == null) {
            controller = new ArrayList<>();
        }
        return this.controller;
    }

    /**
     * Gets the value of the junction property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the junction property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getJunction().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunction }
     * </p>
     * 
     * 
     * @return
     *     The value of the junction property.
     */
    public List<TJunction> getJunction() {
        if (junction == null) {
            junction = new ArrayList<>();
        }
        return this.junction;
    }

    /**
     * Gets the value of the junctionGroup property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the junctionGroup property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getJunctionGroup().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TJunctionGroup }
     * </p>
     * 
     * 
     * @return
     *     The value of the junctionGroup property.
     */
    public List<TJunctionGroup> getJunctionGroup() {
        if (junctionGroup == null) {
            junctionGroup = new ArrayList<>();
        }
        return this.junctionGroup;
    }

    /**
     * Gets the value of the station property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the station property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getStation().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TStation }
     * </p>
     * 
     * 
     * @return
     *     The value of the station property.
     */
    public List<TStation> getStation() {
        if (station == null) {
            station = new ArrayList<>();
        }
        return this.station;
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

}

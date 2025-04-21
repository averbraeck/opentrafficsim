
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Contains a series of lane section elements that define the characteristics of the road cross sections with respect to the lanes along the reference line.
 * 
 * <p>Java class for t_road_lanes complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_lanes">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="laneOffset" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneOffset" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="laneSection" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_lanes_laneSection" maxOccurs="unbounded"/>
 *         <group ref="{http://code.asam.net/simulation/standard/opendrive_schema}g_additionalData" maxOccurs="unbounded" minOccurs="0"/>
 *       </sequence>
 *     </extension>
 *   </complexContent>
 * </complexType>
 * }</pre>
 * 
 * 
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "t_road_lanes", propOrder = {
    "laneOffset",
    "laneSection",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadLanes
    extends OpenDriveElement
{

    protected List<TRoadLanesLaneOffset> laneOffset;
    @XmlElement(required = true)
    protected List<TRoadLanesLaneSection> laneSection;
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
     * Gets the value of the laneOffset property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneOffset property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneOffset().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneOffset }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneOffset property.
     */
    public List<TRoadLanesLaneOffset> getLaneOffset() {
        if (laneOffset == null) {
            laneOffset = new ArrayList<>();
        }
        return this.laneOffset;
    }

    /**
     * Gets the value of the laneSection property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the laneSection property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getLaneSection().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadLanesLaneSection }
     * </p>
     * 
     * 
     * @return
     *     The value of the laneSection property.
     */
    public List<TRoadLanesLaneSection> getLaneSection() {
        if (laneSection == null) {
            laneSection = new ArrayList<>();
        }
        return this.laneSection;
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

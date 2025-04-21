
package org.opentrafficsim.opendrive.generated;

import java.util.ArrayList;
import java.util.List;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElements;
import jakarta.xml.bind.annotation.XmlType;


/**
 * Container for all objects along a road.
 * 
 * <p>Java class for t_road_objects complex type</p>.
 * 
 * <p>The following schema fragment specifies the expected content contained within this class.</p>
 * 
 * <pre>{@code
 * <complexType name="t_road_objects">
 *   <complexContent>
 *     <extension base="{http://code.asam.net/simulation/standard/opendrive_schema}_OpenDriveElement">
 *       <sequence>
 *         <element name="object" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_object" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="objectReference" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_objectReference" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="tunnel" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_tunnel" maxOccurs="unbounded" minOccurs="0"/>
 *         <element name="bridge" type="{http://code.asam.net/simulation/standard/opendrive_schema}t_road_objects_bridge" maxOccurs="unbounded" minOccurs="0"/>
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
@XmlType(name = "t_road_objects", propOrder = {
    "object",
    "objectReference",
    "tunnel",
    "bridge",
    "gAdditionalData"
})
@SuppressWarnings("all") public class TRoadObjects
    extends OpenDriveElement
{

    protected List<TRoadObjectsObject> object;
    protected List<TRoadObjectsObjectReference> objectReference;
    protected List<TRoadObjectsTunnel> tunnel;
    protected List<TRoadObjectsBridge> bridge;
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
     * Gets the value of the object property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the object property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getObject().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObject }
     * </p>
     * 
     * 
     * @return
     *     The value of the object property.
     */
    public List<TRoadObjectsObject> getObject() {
        if (object == null) {
            object = new ArrayList<>();
        }
        return this.object;
    }

    /**
     * Gets the value of the objectReference property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the objectReference property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getObjectReference().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsObjectReference }
     * </p>
     * 
     * 
     * @return
     *     The value of the objectReference property.
     */
    public List<TRoadObjectsObjectReference> getObjectReference() {
        if (objectReference == null) {
            objectReference = new ArrayList<>();
        }
        return this.objectReference;
    }

    /**
     * Gets the value of the tunnel property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the tunnel property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getTunnel().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsTunnel }
     * </p>
     * 
     * 
     * @return
     *     The value of the tunnel property.
     */
    public List<TRoadObjectsTunnel> getTunnel() {
        if (tunnel == null) {
            tunnel = new ArrayList<>();
        }
        return this.tunnel;
    }

    /**
     * Gets the value of the bridge property.
     * 
     * <p>This accessor method returns a reference to the live list,
     * not a snapshot. Therefore any modification you make to the
     * returned list will be present inside the JAXB object.
     * This is why there is not a <CODE>set</CODE> method for the bridge property.</p>
     * 
     * <p>
     * For example, to add a new item, do as follows:
     * </p>
     * <pre>
     * getBridge().add(newItem);
     * </pre>
     * 
     * 
     * <p>
     * Objects of the following type(s) are allowed in the list
     * {@link TRoadObjectsBridge }
     * </p>
     * 
     * 
     * @return
     *     The value of the bridge property.
     */
    public List<TRoadObjectsBridge> getBridge() {
        if (bridge == null) {
            bridge = new ArrayList<>();
        }
        return this.bridge;
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

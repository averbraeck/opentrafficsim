package nl.tudelft.simulation.dsol.web.animation.d2;

import nl.tudelft.simulation.dsol.animation.Locatable;

/**
 * ToggleButtonInfo.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class ToggleButtonInfo
{
    /** the name of the button. */
    private final String name;

    /** whether the class is shown or not. */
    private boolean visible;

    /**
     * @param name the name of the button
     * @param visible whether the class is initially shown or not
     */
    protected ToggleButtonInfo(String name, boolean visible)
    {
        this.name = name;
        this.visible = visible;
    }

    /**
     * @return visible
     */
    public final boolean isVisible()
    {
        return this.visible;
    }

    /**
     * @param visible set visible
     */
    public final void setVisible(boolean visible)
    {
        this.visible = visible;
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * ToggleButtonInfo.LocatableClass. <br>
     * <br>
     * Copyright (c) 2003-2024 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class LocatableClass extends ToggleButtonInfo
    {
        /** the class for which the button holds (e.g., GTU.class). */
        private final Class<? extends Locatable> locatableClass;

        /** the tool tip text to show when hovering over the button. */
        private final String toolTipText;

        /**
         * @param name the name of the button
         * @param locatableClass the class for which the button holds (e.g., GTU.class)
         * @param toolTipText the tool tip text to show when hovering over the button
         * @param visible whether the class is initially shown or not
         */
        public LocatableClass(String name, Class<? extends Locatable> locatableClass, String toolTipText, boolean visible)
        {
            super(name, visible);
            this.locatableClass = locatableClass;
            this.toolTipText = toolTipText;
        }

        /**
         * @return locatableClass
         */
        public final Class<? extends Locatable> getLocatableClass()
        {
            return this.locatableClass;
        }

        /**
         * @return toolTipText
         */
        public final String getToolTipText()
        {
            return this.toolTipText;
        }
    }

    /**
     * ToggleButtonInfo.Text. <br>
     * <br>
     * Copyright (c) 2003-2024 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class Text extends ToggleButtonInfo
    {
        /**
         * @param name the name of the button
         * @param visible whether the class is initially shown or not
         */
        public Text(String name, boolean visible)
        {
            super(name, visible);
        }
    }

    /**
     * ToggleButtonInfo.Gis. <br>
     * <br>
     * Copyright (c) 2003-2024 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
     * See for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>.
     * The source code and binary code of this software is proprietary information of Delft University of Technology.
     * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public static class Gis extends ToggleButtonInfo
    {
        /** the GIS layer name. */
        private final String layerName;

        /** the tool tip text to show when hovering over the button. */
        private final String toolTipText;

        /**
         * @param name the name of the button
         * @param layerName the GIS layer name
         * @param toolTipText the tool tip text to show when hovering over the button
         * @param visible whether the class is initially shown or not
         */
        public Gis(String name, String layerName, String toolTipText, boolean visible)
        {
            super(name, visible);
            this.layerName = layerName;
            this.toolTipText = toolTipText;
        }

        /**
         * @return layerName
         */
        public final String getLayerName()
        {
            return this.layerName;
        }

        /**
         * @return toolTipText
         */
        public final String getToolTipText()
        {
            return this.toolTipText;
        }
    }

}

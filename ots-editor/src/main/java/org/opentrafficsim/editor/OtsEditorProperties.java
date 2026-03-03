package org.opentrafficsim.editor;

import java.awt.Color;
import java.util.Properties;

import org.opentrafficsim.swing.gui.PropertiesStore;

/**
 * Interface that holds the editor properties.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("interfaceIsType")
public interface OtsEditorProperties
{

    /** Application store for preferences and recent files. */
    PropertiesStore PROPERTIES_STORE = new PropertiesStore(new Properties()
    {
        /** */
        private static final long serialVersionUID = 20260303L;
        {
            setProperty(EXPRESSION_COLOR_KEY, PropertiesStore.valueToString(new Color(252, 250, 239)));
            setProperty(INVALID_COLOR_KEY, PropertiesStore.valueToString(new Color(255, 240, 240)));
            setProperty(INACTIVE_COLOR_KEY, PropertiesStore.valueToString(new Color(160, 160, 160)));
            setProperty(MAX_RECENT_FILES_KEY, PropertiesStore.valueToString(10));
            setProperty(MAX_TOOLTIP_LENGTH_KEY, PropertiesStore.valueToString(96));
            setProperty(MAX_DROPDOWN_ITEMS_KEY, PropertiesStore.valueToString(20));
            setProperty(MAX_NAVIGATE_STEPS_KEY, PropertiesStore.valueToString(50));
            setProperty(AUTOSAVE_MS_KEY, PropertiesStore.valueToString(60000));
        }
    }, "editor", "editor user settings");

    /** Key to store expression color. */
    String EXPRESSION_COLOR_KEY = "expressionColor";

    /** Key to store invalid color. */
    String INVALID_COLOR_KEY = "invalidColor";

    /** Key to store inactive color. */
    String INACTIVE_COLOR_KEY = "inactiveColor";

    /** Key to store max number of recent files. */
    String MAX_RECENT_FILES_KEY = "maxRecentFiles";

    /** Key to store max tooltip length. */
    String MAX_TOOLTIP_LENGTH_KEY = "maxTooltipLength";

    /** Key to store max number of drop-down items. */
    String MAX_DROPDOWN_ITEMS_KEY = "maxDropdownItems";

    /** Key to store max number of navigation steps. */
    String MAX_NAVIGATE_STEPS_KEY = "maxNavigateSteps";

    /** Key to store recent files. */
    String RECENT_FILES_KEY = "recentFiles";

    /** Key to store auto-save milliseconds. */
    String AUTOSAVE_MS_KEY = "autosaveMs";

}

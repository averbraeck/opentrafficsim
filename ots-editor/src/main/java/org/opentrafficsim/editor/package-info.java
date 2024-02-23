/**
 * The OTS editor allows visual editing for a simulation in OTS. It is designed to be flexible. The editor reads the OTS XML
 * Schema (xsd) and builds a visible tree for the user from the elements as defined in the schema. Everything in the schema can
 * be specified as such, including future changes. The editor is responsive to data types, and the use of keys and keyrefs in
 * the schema. Keys define that values need to be unique, whereas keyrefs define the value of some element or attribute to be
 * one of the values as specified for a referred key. For example, node id's are a key because they need to be unique, and the
 * start and end node attributes of a link refer to one of the node id's with a keyref. The editor will not only check this, but
 * also give the existing values in for example a pick list.<br>
 * <br>
 * The editor is further decorated with a number of functions. There are many string functions, which supply information in the
 * names of the nodes in the tree, reflecting the specific element. There are also various validators that look at specific
 * constraints of nodes. For example that an attribute referring to a parent of the same type, does not create a cyclical
 * dependency. Finally there are extensions, which provide alternative methods to visually specify input. They use tabs in the
 * main window that each extension fills as appropriate. The extensions read and write the specified information in the
 * tree.<br>
 * <br>
 * All decorations interact with the tree using events, including the creation and deletion of nodes, and attribute value
 * changes.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
package org.opentrafficsim.editor;

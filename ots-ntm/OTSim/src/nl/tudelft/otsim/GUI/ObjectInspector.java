package nl.tudelft.otsim.GUI;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.EventObject;
import java.util.TreeSet;
import java.util.Vector;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellEditor;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

/**
 * ObjectInspector allows inspection and modification of any java.lang.Object 
 * including any descendants in a tree view.
 * <br />
 * The ObjectInspector is a descendant of JFrame. The correct way to destroy an
 * instance of ObjectInspector is to call the dispose method.
 * <br />
 * Descendants of Objects that must be shown by the ObjectInspector must have 
 * public methods with names that comply with the naming convention.
 * <br />
 * See the <a href="../ObjectInspector.html">tutorial</a> on using the
 * ObjectInspector for more information.
 * 
 * @author Peter Knoppers
 *
 */
public class ObjectInspector extends JFrame implements TreeSelectionListener, FocusListener, TreeWillExpandListener {
	private static final long serialVersionUID = 1L;
	
	InspectorTreeNode root;
	private JTree tree;
	private JTextField textField;
	private JPopupMenu popupMenu;
	private JLabel label;
	private JScrollPane scrollPane;
	private GraphicsPanelClient graphicsPanelClient;
	
	/**
	 * The ObjectInspector currently recognizes 8 different methods in the
	 * objects it encounters.
	 */
	private enum MethodType {
		GETTER, SETTER, VALIDATOR, ITEMIZER, ITEMIZERCAPTION, CANDELETER, DELETER, DUPLICATOR,
	};
	
	private InspectorTreeNode parseObject(String callName, Object object) {
    	java.lang.Class<?> c = object.getClass();
	    //System.out.format("Adding object: class is %s%n", c.getCanonicalName());
	    String caption = c.getCanonicalName();
	    int index = caption.lastIndexOf(".");
	    if (index >= 0)
	    	caption = caption.substring(index + 1);
	    if (null == callName)
	    	callName = caption;
	    return new InspectorTreeNode(callName, object, null);
	}
	
	/**
	 * Create an ObjectInspector
	 * @param object java.lang.Object; root object for this ObjectInspector
	 * @param graphicsPanelClient GraphicsPanel whose setModified method will 
	 * be called when changes have been made by the ObjectInspector
	 */
    public ObjectInspector(Object object, GraphicsPanelClient graphicsPanelClient) {
    	this.graphicsPanelClient = graphicsPanelClient;  
    	setMinimumSize(new Dimension(500, 800));
    	setTitle("Object inspector");
    	setLayout(new BorderLayout());
    	//System.out.println("ObjectInspector: calling parseObject for root of tree");
    	root = parseObject(null, object);
    	root.resultObject = object;
    	//System.out.println("ObjectInspector: root node created");
    	tree = initializeTree();
        scrollPane = new JScrollPane(tree);
        add(scrollPane, BorderLayout.CENTER);
        setVisible(true);
    }
    
    private JTree initializeTree() {
    	JTree newTree = new JTree(root);
    	newTree.setEditable(true);
    	newTree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
    	newTree.setCellEditor(new DefaultTreeCellEditor(newTree, new DefaultTreeCellRenderer(), new propertyCellEditor()));
    	newTree.addTreeSelectionListener(this);
    	newTree.setInvokesStopCellEditing(true);
    	newTree.addTreeWillExpandListener(this);
    	return newTree;
    }
    
    /**
     * Expand the path of objects shown in the ObjectInspector under program control.
     * @param path java.lang.ArrayList of java.lang.Object. The first object in
     * this list must be a direct descendant of the root object of the ObjectInspector,
     * the second element must be a direct descendant of the first, etc. 
     */
    public void expandPath(ArrayList<Object> path) {
    	TreeNode node = (TreeNode) (tree.getModel().getRoot());
    	for (Object o : path) {
    		System.out.println("object in supplied path is " + o.toString());
    		int childCount = node.getChildCount();
    		int i;
    		for (i = 0; i < node.getChildCount(); i++) {
    			DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
    			System.out.println("child " + i + " is " + child.toString());
    			InspectorTreeNode itn = (InspectorTreeNode) child.getUserObject();
    			Object resultObject = itn.resultObject;
    			System.out.println("resultObject is " + resultObject.toString());
    			if (resultObject.equals(o)) {
    				System.out.println("expandPath: expanding to node " + itn);
    				tree.expandPath(new TreePath(child.getPath()));
    				node = itn;
    				break;
    			}
    		}
    		if (i >= childCount) {
    			System.out.println("node " + node.toString() + " does not have " + o.toString() + " among its children");
    			//throw new Error("node " + node.toString() + " does not have " + o.toString() + " among its children");
    		}
    	}
    }

    /**
     * The InspectorTreeNode class holds all information regarding a node in
     * the tree shown by the ObjectInspector.
     * 
     * @author pknoppers
     */
	private class InspectorTreeNode extends DefaultMutableTreeNode implements Comparable<InspectorTreeNode> {
		private static final long serialVersionUID = 1L;
		String name;
		Object methodObject;
		Object resultObject;
		Method getter = null;
		Method setter = null;
		Method validator = null;
		Method itemizer = null;
		Method itemizerCaption = null;
		Method canDeleter = null;
		Method deleter = null;
		Method duplicator = null;
		boolean childrenExpanded = false;

		/**
		 * Create a new node.
		 * @param name String; name of the new node
		 * @param methodObject java.lang.Object; Object that is the instance
		 * of the class that the GETTER, SETTER, VALIDATOR and ITEMIZER 
		 * methods of this InspectorTreeNode are methods of
		 * @param resultObject java.lang.Object; Object that is the instance of
		 * the class that the CANDELETER and DUPLICATOR methods of this 
		 * InspectorTreeNode are methods of
		 */
		public InspectorTreeNode(String name, Object methodObject, Object resultObject) {
			super.setUserObject(this);
			this.name = name;
			this.methodObject = methodObject;
			this.resultObject = resultObject;
			/*
			if (null != methodObject) {
				java.lang.Class<?> c = methodObject.getClass();
				System.out.println("Created InspectorTreeNode " + name + " methodObject " + methodObject.toString() + " class " + c.getCanonicalName());
			} else
				System.out.println("Created InspectorTreeNode " + name + " methodObject is NULL");
			*/
		}
		
		/**
		 * Duplicate an InspectorTreeNode.
		 * @param itn InspectorTreeNode; node to duplicate
		 */
		public InspectorTreeNode(InspectorTreeNode itn) {
			super.setUserObject(this);
			this.name = itn.name;
			this.methodObject = itn.methodObject;
			this.resultObject = itn.resultObject;
			this.getter = itn.getter;
			this.setter = itn.setter;
			this.validator = itn.validator;
			this.canDeleter = itn.canDeleter;
			this.itemizer = itn.itemizer;
			this.itemizerCaption = itn.itemizerCaption;
			this.duplicator = itn.duplicator;
			this.deleter = itn.deleter;
		}

		/**
		 * Set (or replace) one of the methods associated with this
		 * InspectorTreeNode. The type of the method is derived from its name.
		 * @param method java.lang.reflect.Method; the method to replace. If
		 * method == null; nothing is changed
		 */
		public void setMethod(Method method) {
			if (null == method)
				return;
			String className = method.getName();
			switch(methodType(className)) {
			case GETTER: 
				this.getter = method; 
				if (null == getValue())	 // Test the getter
					this.getter = null; 
				return;
				
			case SETTER: this.setter = method; return;
			
			case VALIDATOR: this.validator = method; return;
			
			case ITEMIZER: 
				this.itemizer = method;
				try {
					//System.out.println("about to invoke itemizer");
					//System.out.println("itemizer is " + itemizer.toString());
					//System.out.println("methodObject is " + methodObject.toString());
					if (null == itemizer.invoke(methodObject, new Object[0])) {
						WED.showProblem(WED.WARNING, "Itemizer of %s returned null", name);
						itemizer = null;
					}
				} catch (Exception e) {
					//System.err.println("Oops: " + WED.exeptionStackTraceToString(e));
					WED.showProblem(WED.WARNING, "Itemizer of %s throws exception: %s\r\n%s", name, e.toString(), WED.exeptionStackTraceToString(e));
				}
				return;
			
			case ITEMIZERCAPTION:
				this.itemizerCaption = method; return;
				
			case CANDELETER: this.canDeleter = method; return;
			
			case DELETER: this.deleter = method; return;
			
			case DUPLICATOR: this.duplicator = method; return;
			
			default:
				throw new Error("Cannot identify kind of method");
			}
		}
		
		@Override
		public int compareTo(InspectorTreeNode o) {
			return name.compareTo(o.name);
		}
		
		/**
		 * Invoke the GETTER method and return the result.
		 * @return java.lang.Object; the result returned by the GETTER
		 */
		public Object getValue() {
			if (null == getter)
				return "NULL (no getter defined)";
			try {
				Object arglist[] = new Object[0];
				if (null == methodObject)
					return "NULL (no methodObject defined)";
				Object returnedObject = getter.invoke(methodObject, arglist);
				return returnedObject;
			} catch (Exception e) {
				System.err.println(String.format("%s: object is %s,  method is %s", e.toString(), methodObject.getClass().getCanonicalName(), getter.getName()));
				e.printStackTrace();
				return "Caught exception in getValue " + e.toString();
			}
		}
		
		@Override
		public String toString() {
			String currentValue = null;
			if (null != getter) {
				Object value = getValue();
				if (null != value)
					currentValue = getValue().toString();
				else
					currentValue = "NULL";
			} else if (null == resultObject)
				currentValue = "NULL (resultObject is null)";
			else
				currentValue = resultObject.toString();
			if (null != currentValue)
				return name + ": " + currentValue;
			return name;
		}
		
		//private String yesNo(boolean value) {
		//	return value ? "yes" : "no";
		//}

		/**
		 * Expand the children of this InspectorTreeNode.
		 */
		private void expandChildren() {
			//System.out.println("expandChildren " + name + " childrenExpanded is " + (childrenExpanded ? "true; returning immediately" : "false"));
			if (childrenExpanded)
				return;
			//System.out.println("expandChildren of " + name);
			childrenExpanded = true;	// to prevent infinite recursion set this NOW
			if(null == resultObject) {
				//System.out.println("expandChildren: resultObject is null; no expansion to be done");
				return;
			}
	    	java.lang.Class<?> c = resultObject.getClass();
		    //System.out.format("expanding object class is %s%n", c.getCanonicalName());
		    String caption = reduceTypeName(c.getCanonicalName());
		    // Build an ordered list of methods of this object that we may use
			TreeSet<InspectorTreeNode> itnSet = new TreeSet<InspectorTreeNode>();
			if ("ArrayList".equals(caption)) {
				// Add all elements of the ArrayList to the tree
				ArrayList<?> al = (ArrayList<?>) resultObject;
				for (Object o : al) {
					//System.out.println("adding ArrayList element " + i++ + ": " + o.toString());
					add(new InspectorTreeNode(reduceTypeName(o.getClass().getCanonicalName()), resultObject, o));
				}
				return;
			}
			// Not an ArrayList
		    for (Method m : c.getMethods()) {
		    	String methodName = m.toGenericString();
		    	int index = methodName.lastIndexOf("(");
		    	if (index >= 0)
		    		methodName = methodName.substring(0, index);
		    	String reducedMethodName = reduceName(methodName);
		    	if (null == reducedMethodName)
		    		continue;
		    	//System.out.println("found method " + methodName + " of type " + methodType(methodName));
		    	switch(methodType(methodName)) {
		    	case DELETER: this.deleter = m; continue;
		    	case CANDELETER: this.canDeleter = m; continue;
		    	case DUPLICATOR: this.duplicator = m; continue;
		    	default: break;
		    	}
		    	InspectorTreeNode newEntry = new InspectorTreeNode(reducedMethodName, resultObject, null);
		    	InspectorTreeNode itn = itnSet.floor(newEntry);
		    	if ((null == itn) || (! itn.name.equals(reducedMethodName))) {
		    		itnSet.add(newEntry);
		    		itn = newEntry;
		    	}
		    	itn.setMethod(m);
		    	//System.out.println(itn.toString() + " now has " + String.format("Name: %s, getter %s, setter %s, itemizer %s, canDeleter %s, deleter %s, duplicator %s", itn.name, 
				//		yesNo(null != itn.getter), yesNo(null != itn.setter), yesNo(null != itn.itemizer), yesNo(null != itn.canDeleter), yesNo(null != itn.deleter), yesNo(null != itn.duplicator)));
		    }
		    //System.out.println("expandChildren: found " + itnSet.size() + " children: " + itnSet.toString());
		    for(InspectorTreeNode itn : itnSet) {
    			if ((itn.setter != null) && (itn.validator == null))
    				System.out.println("No validator defined for " + itn.name);
    			if ((itn.setter != null) && (itn.getter == null))
    				System.out.println("No getter defined for " + itn.name);
    			if ((itn.validator != null) && (itn.getter == null))
    				System.out.println("No getter defined for " + itn.name);
    			if ((itn.setter != null) && (itn.validator == null))
    				System.out.println("No validator defined for " + itn.name);
	    		InspectorTreeNode newNode = new InspectorTreeNode(itn);//itn.name, resultObject, resultObject);
	    		if (null != newNode.getter)
	    			newNode.resultObject = newNode.getValue();
    			add(newNode);
		    }
			//System.out.println("expandChildren: done");
		}

		
		@Override
		public boolean isLeaf() {
			expandChildren();
			return super.getChildCount() == 0;
		}
		
		@Override
		public int getChildCount() {
			expandChildren();
			return super.getChildCount();
		}
		
		/**
		 * Reduce a name of a Java type to its last component.
		 * <br />
		 * The components are separated by a dot (<code>.</code>).
		 * <br />
		 * Example: <code>jave.lang.Component</code> is reduced to 
		 * <code>Component</code>.
		 * @param typeName String; Java type name
		 * @return String; the last component of <code>typeName</code>
		 */
		private String reduceTypeName(String typeName) {
			int index = typeName.lastIndexOf(".");
			if (index >= 0)
				typeName = typeName.substring(index + 1);
			return typeName;
		}
		
		/**
		 * Class to define the method name patterns recognized by the
		 * ObjectInspector.
		 * 
		 * @author pknoppers
		 */
		private class Pattern {
			final String prefix;
			final String suffix;
			final MethodType methodType;
			
			/**
			 * Create a name pattern.
			 * @param prefix String; prefix of the name pattern
			 * @param suffix String; suffix of the name pattern
			 * @param methodType MethodType; type of the pattern
			 */
			Pattern(String prefix, String suffix, MethodType methodType) {
				this.prefix = prefix;
				this.suffix = suffix;
				this.methodType = methodType;
			}
			
			/**
			 * Pattern matcher for the pattern.
			 * @return String; regular expression that matches methods of this
			 * pattern
			 */
			final String regexp() {
				return "^" + prefix + "(..*)" + suffix + "$"; 
			}
		}

		/**
		 * The list of patterns recognized by the ObjectInspector.
		 */
		private Pattern[] patterns = {
			new Pattern("get", "_r", MethodType.GETTER),	
			new Pattern("set", "_w", MethodType.SETTER),	
			new Pattern("validate", "_v", MethodType.VALIDATOR),
			new Pattern("itemize", "_i", MethodType.ITEMIZER),
			new Pattern("itemize", "_caption", MethodType.ITEMIZERCAPTION),
			new Pattern("mayDelete", "_d", MethodType.CANDELETER),
			new Pattern("delete", "_d", MethodType.DELETER),
			new Pattern("duplicate", "_2", MethodType.DUPLICATOR),
		};

		/**
		 * Check if a java.lang.reflect.Method is special to the 
		 * ObjectInspector. If it is return the name stripped of the prefix
		 * and suffix. If it is not return null;
		 * @param methodName String; name of the method to check.
		 * @return String; stripped method name, or null
		 */
		private String reduceName(String methodName) {
			int index = methodName.lastIndexOf(".");
			if (index >= 0)
				methodName = methodName.substring(index + 1);
			for(Pattern pattern : patterns) {		
				String result = methodName.replaceAll(pattern.regexp(), "$1");
				if ((result.length() > 0) && (! result.equals(methodName)))
					return result;
			}
			return null;
	 	}
		
		/**
		 * Return the MethodType of a method name.
		 * <br />
		 * If the method type is not recognized; this method throws an Error.
		 * @param methodName String; name of the method
		 * @return MethodType; type of the method
		 */
		private MethodType methodType(String methodName) {
			int index = methodName.lastIndexOf(".");
			String reducedName = methodName;
			if (index >= 0)
				reducedName = reducedName.substring(index + 1);
			for(Pattern pattern : patterns) {		
				String result = reducedName.replaceAll(pattern.regexp(), "$1");
				if ((result.length() > 0) && (! result.equals(reducedName)))
					return pattern.methodType;
			}
			throw new Error("Bad methodName \"" + methodName + "\"");
		}
		
		/**
		 * Determine if a PopupMenu should be used to let the user alter the
		 * value of this InspectorTreeNode. 
		 * @return Boolean; true if there is an ITEMIZER, a DUPLICATOR or a
		 * DELETER; false if none of these are defined
		 */
		public boolean usePopupMenu() {
			System.out.println("usePopupMenu: itemizer is " + (null != itemizer ? "present" : "absent"));
			return (null != duplicator) || (null != deleter) || (null != itemizer) ;
		}

		/**
		 * Determine if this InspectorTreeNode may be deleted.
		 * @return Boolean; true if this InspectorTreeNode may be deleted;
		 * false if it may not be deleted
		 */
		public boolean enableDelete() {
			if (null == canDeleter)
				return true;
			try {
				Object arglist[] = new Object[0];
				Object result = canDeleter.invoke(resultObject, arglist);
				if (result instanceof Boolean)
					return (Boolean) result;
				return false;
			} catch (Exception e) {
				System.err.println(String.format("%s: object is %s,  method is %s", e.toString(), resultObject.getClass().getCanonicalName(), deleter.getName()));
				return false;
			}	
		}
		
	}
	
    class propertyCellEditor extends JComboBox<Object> implements TreeCellEditor, ActionListener, CaretListener {
 		private static final long serialVersionUID = 1L;
		Vector<CellEditorListener> listeners = new Vector<CellEditorListener>();
    	JPanel panel;
    	InspectorTreeNode inspectorTreeNode;
    	private DefaultMutableTreeNode previousNode = null;
    	String mostRecentValue;
    	
    	public propertyCellEditor() {
     		addActionListener(this);
			panel = new JPanel(new BorderLayout());
			label = new JLabel("label");
			label.setForeground(Color.GREEN);
			Font font = label.getFont();
			label.setFont(font.deriveFont(font.getStyle() ^ Font.BOLD));
			panel.add(label, BorderLayout.LINE_START);
			textField = new JTextField("");
			textField.addCaretListener(this);
    	}
    	
    	private boolean verifyValue() {
    		if (null == inspectorTreeNode) {
    			System.err.println("Oops inspectorTreeNode is null");
    			return false;
    		}
    		else if(null != inspectorTreeNode.validator) {
    			try {
    				Object arglist[] = new Object[0];
    				InputValidator iv = (InputValidator) inspectorTreeNode.validator.invoke(inspectorTreeNode.methodObject, arglist);
    				return iv.validate(inspectorTreeNode.getValue().toString(), mostRecentValue);
    			} catch (Exception e) {
    				return false;
    			}
    		}
    		return true;
    	}
    	
    	private void editorContentsChanged() {
    		mostRecentValue = textField.getText();
    		boolean inputValid = verifyValue();
			textField.setBackground(inputValid ? Color.WHITE : Color.RED);
    		// http://stackoverflow.com/questions/1524855/how-to-calculate-the-fonts-width
    		// search for "here is a simple"
    		String text = textField.getText() + "WWWWWWWWWWWWW";
    		Font font = textField.getFont();
    		FontMetrics fm = textField.getFontMetrics(font);
    		int width = fm.stringWidth(text);
			Dimension textFieldSize = textField.getPreferredSize();
			System.out.println("setting editor width to " + width);
			textField.setPreferredSize(new Dimension(width, textFieldSize.height)); 
			textField.revalidate();
			Dimension captionSize = label.getPreferredSize();
			panel.setPreferredSize(new Dimension(width + captionSize.width, textFieldSize.height));
			panel.setMinimumSize(new Dimension(width + captionSize.width, textFieldSize.height));
			panel.revalidate();
			System.out.format("textField %d,%d, editor %d,%d\n", textField.getSize().width, textField.getSize().height, panel.getSize().width, panel.getSize().height);
    	}

		@Override
		public void addCellEditorListener(CellEditorListener cel) {
			//System.out.println("addCellEditorListener");	
			listeners.addElement(cel);
		}

		@Override
		public void cancelCellEditing() {
			System.out.println("cancelCellEditing");			
		}

		@Override
		public Object getCellEditorValue() {
			System.out.println("getCellEditorValue");			
			return textField.getText();
		}

		@Override
		public boolean isCellEditable(EventObject eo) {
			System.out.print("isCellEditable ... ");
			if (null == eo) {
				// see comment near bottom of
				// https://forums.oracle.com/forums/thread.jspa?threadID=1348574
				System.out.println("EventObject is null; returning true");
				return true;
			}
			TreePath path;
			try {
				path = ((JTree) eo.getSource()).getSelectionPath();
				if (null == path) {
					System.out.println("no selectionPath; returning false");
					return false;
				}
			} catch (Exception e) {
				System.out.println("caught exception " + e.toString());
				return false;
			}
			Object userObject = ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
			if (userObject instanceof String)
				return true;
			if (! (userObject instanceof InspectorTreeNode))
				System.err.println("ObjectInspector: userObject is neither a String, nor an InspectorTreeNode");
			System.out.print(printValue(path.getLastPathComponent()));
			InspectorTreeNode itn = (InspectorTreeNode) userObject;
			if (null != itn.duplicator) {
				System.out.println(" returning true (found a duplicator)");
				return true;
			}
			if (null != itn.deleter){
				System.out.println(" return true (found a deleter)");
				return true;
			}
			System.out.println(" returning " + ((null != itn.setter) ? true : false));
			if(null == itn.setter)
				return false;
			if(null == itn.validator)
				return true;
			try {
				Object arglist[] = new Object[0];
				InputValidator inputValidator = (InputValidator) itn.validator.invoke(itn.methodObject, arglist);
				if (inputValidator.totallyConstrained())
					return false;
			} catch (Exception e) {
				System.err.println("Caught exception in validator.totallyConstrained " + e.toString());
			}
			return true;
		}

		@Override
		public void removeCellEditorListener(CellEditorListener cel) {
			System.out.println("removeCellEditorListener");			
		    listeners.removeElement(cel);
		}

		@Override
		public boolean shouldSelectCell(EventObject eo) {
			System.out.println("shouldSelectCell ");			
			return false;
		}

		@Override
		public boolean stopCellEditing() {
			System.out.println("stopCellEditing; this is " + this.toString());
			InspectorTreeNode itn = this.inspectorTreeNode;
			if (itn.usePopupMenu()) {
				System.out.println("Menu event handler may have done something");
			}
			else {
				System.out.format("new value \"%s\" old value \"%s\" %s\n", mostRecentValue, itn.methodObject.toString(), verifyValue() ? "OK" : "BAD");
				if (! mostRecentValue.equals(itn.methodObject.toString()) && verifyValue()) {
					Class<?>[] parameterTypes =  itn.setter.getParameterTypes();
					if (parameterTypes.length != 1)
						throw new Error("cannot handle setters with number of params != 1; yet");
					System.out.format("parameter type is \"%s\"\n", parameterTypes[0]);
					Object arglist[] = new Object[1];
					arglist[0] = null;
					String typeName = "" + parameterTypes[0];
					if ("class java.lang.String".equals(typeName))
						arglist[0] = mostRecentValue;
					else if ("double".equals(typeName))
						arglist[0] = Double.parseDouble(mostRecentValue);
					else
						System.err.println("Cannot set parameter of type " + typeName + "; yet");
					if (null != arglist[0]) {
						try {
							itn.setter.invoke(itn.methodObject, arglist);
							System.out.println("Succeeded to set new value");
							if (null != graphicsPanelClient)
								graphicsPanelClient.setModified();
							rebuild(objectPath(inspectorTreeNode.getPath()));
						} catch (Exception e) {
							System.out.println("Failed to set new value: " + e.toString());
						}
					}
				}
			}
			return true;
		}

		@Override
		public Component getTreeCellEditorComponent(JTree aTree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row) {
			System.out.println("getTreeCellEditorComponent " + printValue(value));
			// Ugly clutch; but it appears to do the trick
			// The node in the tree gets replaced by a string after return from 
			// stopCellEditing. The next time that node is edited we would not
			// have a InspectorTreeNode as userObject of the node.
			// We "fix" that problem here; when (if) another node is selected.
			if (null != previousNode)	// repair "damage" done by previous edit
				previousNode.setUserObject(inspectorTreeNode);
			// remember what node we have to fix next time
			previousNode = (DefaultMutableTreeNode) aTree.getSelectionPath().getLastPathComponent();
			Object userObject = ((DefaultMutableTreeNode) value).getUserObject();
			inspectorTreeNode = (InspectorTreeNode) userObject;
			label.setText(inspectorTreeNode.name + ":");
			//System.out.print("Current node: " + inspectorTreeNode.subTree(0) + "Root \r\n" + root.subTree(0));
			if (inspectorTreeNode.usePopupMenu()) {
				textField.setFocusable(false);
				panel.add(textField, BorderLayout.CENTER);
				popupMenu = new JPopupMenu();
				if (null != inspectorTreeNode.itemizer) {
					if (null != inspectorTreeNode.itemizerCaption) {
						try {
							textField.setText((String) inspectorTreeNode.itemizerCaption.invoke(inspectorTreeNode.methodObject, new Object[0]));
						} catch (Exception e) {
							System.err.println("Invocation of ItemizerCaption failed");
							e.printStackTrace();
							textField.setText("<itemizercaption failed>");
						} 
					} else
						textField.setText("select type");
					ArrayList<String> itemNames = null;
					try {
						@SuppressWarnings("unchecked")
						ArrayList<String> list = (ArrayList<String>) inspectorTreeNode.itemizer.invoke(inspectorTreeNode.methodObject, new Object[0]);
						itemNames = list;
					} catch (Exception e) {
						WED.showProblem(WED.WARNING, "Insert failed: %s", e.toString());
					}
					for (String caption : itemNames) {
						boolean enabled = true;
						if (null != inspectorTreeNode.validator) {
		    				Object arglist[] = new Object[0];
		    				InputValidator iv;
							try {
								iv = (InputValidator) inspectorTreeNode.validator.invoke(inspectorTreeNode.methodObject, arglist);
			    				if(! iv.validate(inspectorTreeNode.getValue().toString(), mostRecentValue))
			    					enabled = false;
							} catch (Exception e) {
								System.err.println("Caught exception in inputvalidator: " + e.toString());
							}
						}
						popupMenu.add(makeMenuItem(caption, "SetValue|" + caption, enabled));
					}
				} else {
					textField.setText("modify list");
					if (null != inspectorTreeNode.duplicator)
						popupMenu.add(makeMenuItem("Insert", "Insert", true));
					if (null != inspectorTreeNode.deleter)
						popupMenu.add(makeMenuItem("Delete", "Delete", inspectorTreeNode.enableDelete()));
				}
				// pop it open ASAP
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						popupMenu.show(textField, 0, (int) (textField.getSize().getHeight()));
					}
				});
			} else {
				System.out.println("NOT using popupmenu");
				textField.setText(inspectorTreeNode.getValue().toString());
				textField.setFocusable(true);
				panel.add(textField, BorderLayout.CENTER);
			}
			editorContentsChanged();
			return(panel);
		}
		
		JMenuItem makeMenuItem(String caption, String actionCommandString, boolean enabled) {
			JMenuItem result = new JMenuItem(caption);
			result.setEnabled(enabled);
			result.setActionCommand(actionCommandString);
			result.addActionListener(this);
			return result;
		}
    	
		protected void fireEditingStopped() {
			if (listeners.size() > 0) {
				ChangeEvent ce = new ChangeEvent(this);
		    	for (int i = listeners.size() - 1; i >= 0; i--)
		    		listeners.elementAt(i).editingStopped(ce);
		    }
		}

		private ArrayList<Object> objectPath(TreeNode[] path) {
			ArrayList<Object> result = new ArrayList<Object>();
			for (Object o : path) {
				InspectorTreeNode itn = (InspectorTreeNode) o;
				result.add(itn.resultObject);
			}
			return result;
		}
		
		@Override
		public void actionPerformed(ActionEvent e) {
			String command = e.getActionCommand();
			//Object sender = e.getSource();
			System.out.println("actionPerformed; command is \"" + command + "\", event is " + e.toString());
			if ("Insert".equals(command)) {
				try {
					inspectorTreeNode.duplicator.invoke(inspectorTreeNode.resultObject, new Object[] { null });
				} catch (Exception e1) {
					WED.showProblem(WED.WARNING, "Insert failed: %s", e1.toString());
				}
				rebuild(objectPath(inspectorTreeNode.getPath()));
			} else if ("Delete".equals(command)) {
				try {
					inspectorTreeNode.deleter.invoke(inspectorTreeNode.resultObject, new Object[] {});
				} catch (Exception e1) {
					WED.showProblem(WED.WARNING, "Delete failed: %s", e1.toString());
				}
				rebuild(objectPath(inspectorTreeNode.getPath()));
			} else if(command.startsWith("SetValue|")) {
				String newValue = command.substring(9);
				try {
					Object[] args = new Object[1];
					args[0] = newValue;
					inspectorTreeNode.setter.invoke(inspectorTreeNode.methodObject, args);
				} catch (Exception e1) {
					WED.showProblem(WED.WARNING, "Setter failed: %s", e.toString());
				}
				rebuild(objectPath(inspectorTreeNode.getPath()));
			} else if (stopCellEditing())
				fireEditingStopped();
			else
				System.err.println("Unhandled ActionEvent " + e.toString());
		}
		
		private String printValue(Object value) {
			try {
				InspectorTreeNode itn;
				if (value instanceof InspectorTreeNode)
					itn = (InspectorTreeNode) value;
				else
					itn = (InspectorTreeNode) ((DefaultMutableTreeNode) value).getUserObject();
				if (null != itn.itemizer)
					System.out.println("this one has an itemizer");
				return String.format("Name: %s, getter %s, setter %s, itemizer %s, canDeleter %s, deleter %s, duplicator %s", itn.name, 
						yesNo(null != itn.getter), yesNo(null != itn.setter), yesNo(null != itn.itemizer), yesNo(null != itn.canDeleter), yesNo(null != itn.deleter), yesNo(null != itn.duplicator));
			} catch (Exception e) {
				return "Caught some exception in printValue";
			}
		}

		@Override
		public void caretUpdate(CaretEvent arg0) {
			editorContentsChanged();
		}
		
		private String yesNo(boolean value) {
			return value ? "yes" : "no";
		}

    }
    
	@Override
	public void valueChanged(TreeSelectionEvent treeSelectionEvent) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree.getLastSelectedPathComponent();
		if (null == node) {
			System.out.println("Nothing is selected");
			return;
		}
		System.out.println("node hashcode is " + node.hashCode());
		if (node.isLeaf())
			System.out.println("Leaf selected " + node.toString());
		else
			System.out.println("Node selected " + node.toString());
	}
    
	/**
	 * Rebuild this ObjectInspector from the root and expand to a particular
	 * path.
	 * <br />
	 * This method is called after the ObjectInspector has changed something
	 * in the displayed Object tree.
	 * @param path ArrayList<Object> List of objects defining the path to be
	 * expanded
	 */
	public void rebuild(ArrayList<Object> path) {
		path.remove(0);
		path.remove(path.size() - 1);
		root.removeAllChildren();
		root.childrenExpanded = false;
    	tree = initializeTree();
		expandPath(path);
		scrollPane.setViewportView(tree);
	}

	@Override
	public void focusGained(FocusEvent focusEvent) {
		System.out.println("focus gained");
	}

	@Override
	public void focusLost(FocusEvent focusEvent) {
		System.out.println("focus lost");
	}

	@Override
	public void treeWillCollapse(TreeExpansionEvent treeSelectionEvent) throws ExpandVetoException {
	}

	@Override
	public void treeWillExpand(TreeExpansionEvent treeSelectionEvent) throws ExpandVetoException {
		//System.out.println("TreeExpandsionEvent " + treeSelectionEvent);
	}
	
}

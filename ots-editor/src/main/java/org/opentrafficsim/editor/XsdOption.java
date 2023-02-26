package org.opentrafficsim.editor;

/**
 * Class that groups some information on an option for a choice node, relevant to show to the user.
 * @author wjschakel
 */
public class XsdOption
{

    /** Option node. */
    private final XsdTreeNode optionNode;

    /** Choice node, the option node is one of its options. */
    private final XsdTreeNode choice;

    /** Whether this option is first in a group and a separator might be shown in a menu. */
    private final boolean firstInGroup;

    /** Whether this option is currently selected. */
    private final boolean selected;

    /** The number of rows to move selection up from this node to return to the top. */
    private final int shiftToTopOfOption;

    /**
     * Constructor.
     * @param optionNode XsdTreeNode; option node.
     * @param choice XsdTreeNode; choice node, the option node is one of its options.
     * @param firstInGroup boolean; whether this option is first in a group and a separator might be shown in a menu.
     * @param selected boolean; whether this option is currently selected.
     * @param shiftToTopOfOption in; number of rows to move selection up from this node to return to the top. This is useful if
     *            a sequence with multiple nodes is replaced by an option with fewer nodes.
     */
    public XsdOption(final XsdTreeNode optionNode, final XsdTreeNode choice, final boolean firstInGroup, final boolean selected,
            final int shiftToTopOfOption)
    {
        this.optionNode = optionNode;
        this.choice = choice;
        this.firstInGroup = firstInGroup;
        this.selected = selected;
        this.shiftToTopOfOption = shiftToTopOfOption;
    }

    /**
     * Returns the option node.
     * @return XsdTreeNode; option node.
     */
    public XsdTreeNode getOptionNode()
    {
        return this.optionNode;
    }

    /**
     * Returns the choice.
     * @return XsdTreeNode; choice.
     */
    public XsdTreeNode getChoice()
    {
        return this.choice;
    }

    /**
     * Returns whether this option is first in a group and a separator might be shown in a menu.
     * @return boolean; whether this option is first in a group and a separator might be shown in a menu.
     */
    public boolean isFirstInGroup()
    {
        return this.firstInGroup;
    }

    /**
     * Returns whether this option is currently selected.
     * @return boolean whether this option is currently selected.
     */
    public boolean isSelected()
    {
        return this.selected;
    }

    /**
     * Returns the number of rows to move selection up from this node to return to the top. This is useful if a sequence with
     * multiple nodes is replaced by an option with fewer nodes.
     * @return int; number of rows to move selection up from this node to return to the top.
     */
    public int getShiftToTopOfOption()
    {
        return this.shiftToTopOfOption;
    }
}

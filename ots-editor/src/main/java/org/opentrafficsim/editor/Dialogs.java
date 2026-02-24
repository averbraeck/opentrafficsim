package org.opentrafficsim.editor;

import java.io.File;
import java.util.Date;
import java.util.Optional;

import javax.swing.Icon;
import javax.swing.JOptionPane;

import org.opentrafficsim.animation.IconUtil;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;

/**
 * Object that is part of {@link OtsEditor} and may be used to show dialogs.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Dialogs
{

    /** Icon for in question dialog. */
    private static final Icon QUESTION_ICON = IconUtil.of("Question24.png").get();

    /** Icon for in description dialog. */
    private static final Icon DESCRIPTION_ICON = IconUtil.of("Information24.png").get();

    /** Icon for in warning dialog. */
    private static final Icon WARNING_ICON = IconUtil.of("Warning24.png").get();;

    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor
     */
    public Dialogs(final OtsEditor editor)
    {
        this.editor = editor;
    }

    /**
     * Requests the user to confirm the deletion of a node. The default button is "Ok". The window popping up is considered
     * sufficient warning, and in this way a speedy succession of "del" and "enter" may delete a consecutive range of nodes to
     * be deleted.
     * @param node node.
     * @return {@code true} if the user confirms node removal.
     */
    public boolean confirmNodeRemoval(final XsdTreeNode node)
    {
        return JOptionPane.showConfirmDialog(this.editor, "Remove `" + node + "`?", "Remove?", JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.QUESTION_MESSAGE, QUESTION_ICON) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a dialog in a modal pane to confirm discarding unsaved changes.
     * @return whether unsaved changes can be discarded.
     */
    public boolean confirmDiscardChanges()
    {
        return JOptionPane.showConfirmDialog(this.editor, "Discard unsaved changes?", "Discard unsaved changes?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, QUESTION_ICON) == JOptionPane.OK_OPTION;
    }

    /**
     * Shows a description in a modal pane.
     * @param description description.
     * @param title title of the window, may be {@code null} but typically is the name of the node or attribute
     */
    public void showDescription(final String description, final String title)
    {
        JOptionPane.showMessageDialog(this.editor, "<html><body><p style='width: 400px;'>" + description + "</p></body></html>",
                title == null ? "Description" : OtsSimulationPanel.separatedName(title), JOptionPane.INFORMATION_MESSAGE,
                DESCRIPTION_ICON);
    }

    /**
     * Show invalid message.
     * @param invalidMessage invalid message
     * @param title title, may be {@code null}
     */
    public void showInvalidMessage(final String invalidMessage, final String title)
    {
        JOptionPane.showMessageDialog(this.editor, invalidMessage,
                title == null ? "Invalid" : OtsSimulationPanel.separatedName(title), JOptionPane.INFORMATION_MESSAGE,
                DESCRIPTION_ICON);
    }

    /**
     * Show tree invalid.
     */
    public void showInvalidToRunMessage()
    {
        JOptionPane.showMessageDialog(this.editor, "The setup is not valid. Make sure no red nodes remain.",
                "Setup is not valid", JOptionPane.INFORMATION_MESSAGE, DESCRIPTION_ICON);
    }

    /**
     * Show input parameters have a circular dependency.
     * @param message exception message
     */
    public void showCircularInputParameters(final String message)
    {
        JOptionPane.showMessageDialog(this.editor, "Input parameters have a circular dependency: " + message,
                "Circular input parameter", JOptionPane.INFORMATION_MESSAGE, WARNING_ICON);
    }

    /**
     * Show message about invalid expression.
     * @param message exception message
     */
    public void showInvalidExpression(final String message)
    {
        JOptionPane.showMessageDialog(this.editor, "An expression is not valid: " + message, "Expression not valid",
                JOptionPane.INFORMATION_MESSAGE, WARNING_ICON);
    }

    /**
     * Show unable to run.
     */
    public void showUnableToRunFromTempFile()
    {
        JOptionPane.showMessageDialog(this.editor, "Unable to run, temporary file could not be saved.", "Unable to run",
                JOptionPane.INFORMATION_MESSAGE, WARNING_ICON);
    }

    /**
     * Ask to remove temporary file that could not be loaded.
     * @return whether temporary file may be removed
     */
    public boolean confirmRemoveRecentFile()
    {
        return JOptionPane.showConfirmDialog(this.editor,
                "File could not be loaded. Do you want to remove it from recent files?", "Remove from recent files?",
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, QUESTION_ICON) == JOptionPane.OK_OPTION;
    }

    /**
     * Ask to clear recent files.
     * @return whether all recent files may be removed
     */
    public boolean confirmClearRecentFiles()
    {
        return JOptionPane.showConfirmDialog(this.editor, "Are you sure you want to clear the recent files?",
                "Clear recent files?", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
                QUESTION_ICON) == JOptionPane.OK_OPTION;
    }

    /**
     * Ask to load auto save file.
     * @param file file
     * @return whether to load auto save file, {@code true} means load, {@code false} means ok to delete, empty means do nothing
     */
    public Optional<Boolean> confirmLoadAutosaveFile(final File file)
    {
        int userInput = JOptionPane.showConfirmDialog(this.editor,
                "Autosave file " + file.getName() + " (" + new Date(file.lastModified())
                        + ") detected. Do you want to load this file? ('No' removes the file)",
                "Autosave file detected", JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, QUESTION_ICON);
        return Optional.ofNullable(userInput == JOptionPane.OK_OPTION ? Boolean.TRUE
                : (userInput == JOptionPane.NO_OPTION ? Boolean.FALSE : null));
    }

    /**
     * Ask to remove auto save file (which could not be loaded).
     * @return whether to remove auto save file
     */
    public boolean confirmRemoveAutosaveFile()
    {
        return JOptionPane.showConfirmDialog(this.editor, "Autosave file could not be loaded. Do you want to remove it?",
                "Remove autosave?", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE,
                QUESTION_ICON) == JOptionPane.YES_OPTION;
    }

    /**
     * Shows a notification.
     * @param notification notification, should be short as it is also used as the dialog title
     */
    public void notification(final String notification)
    {
        notification(notification, notification);
    }

    /**
     * Shows a notification.
     * @param notification notification
     * @param title dialog title
     */
    public void notification(final String notification, final String title)
    {
        JOptionPane.showMessageDialog(this.editor, notification, OtsSimulationPanel.separatedName(title),
                JOptionPane.WARNING_MESSAGE, DESCRIPTION_ICON);
    }

}

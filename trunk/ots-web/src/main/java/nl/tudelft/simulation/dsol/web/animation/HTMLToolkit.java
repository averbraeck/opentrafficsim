package nl.tudelft.simulation.dsol.web.animation;

import java.awt.Button;
import java.awt.Canvas;
import java.awt.Checkbox;
import java.awt.CheckboxMenuItem;
import java.awt.Choice;
import java.awt.Desktop;
import java.awt.Dialog;
import java.awt.Dialog.ModalExclusionType;
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Label;
import java.awt.List;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.Panel;
import java.awt.PopupMenu;
import java.awt.PrintJob;
import java.awt.ScrollPane;
import java.awt.Scrollbar;
import java.awt.TextArea;
import java.awt.TextField;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.dnd.DragGestureEvent;
import java.awt.dnd.InvalidDnDOperationException;
import java.awt.dnd.peer.DragSourceContextPeer;
import java.awt.font.TextAttribute;
import java.awt.im.InputMethodHighlight;
import java.awt.image.ColorModel;
import java.awt.image.ImageObserver;
import java.awt.image.ImageProducer;
import java.awt.peer.ButtonPeer;
import java.awt.peer.CanvasPeer;
import java.awt.peer.CheckboxMenuItemPeer;
import java.awt.peer.CheckboxPeer;
import java.awt.peer.ChoicePeer;
import java.awt.peer.DesktopPeer;
import java.awt.peer.DialogPeer;
import java.awt.peer.FileDialogPeer;
import java.awt.peer.FontPeer;
import java.awt.peer.FramePeer;
import java.awt.peer.KeyboardFocusManagerPeer;
import java.awt.peer.LabelPeer;
import java.awt.peer.ListPeer;
import java.awt.peer.MenuBarPeer;
import java.awt.peer.MenuItemPeer;
import java.awt.peer.MenuPeer;
import java.awt.peer.PanelPeer;
import java.awt.peer.PopupMenuPeer;
import java.awt.peer.ScrollPanePeer;
import java.awt.peer.ScrollbarPeer;
import java.awt.peer.TextAreaPeer;
import java.awt.peer.TextFieldPeer;
import java.awt.peer.WindowPeer;
import java.net.URL;
import java.util.Map;
import java.util.Properties;

import org.djutils.logger.CategoryLogger;

import nl.tudelft.simulation.dsol.logger.Cat;
import nl.tudelft.simulation.dsol.web.animation.peer.HTMLFrame;
import nl.tudelft.simulation.dsol.web.animation.peer.HTMLKeyboardFocusManagerPeer;
import nl.tudelft.simulation.dsol.web.animation.peer.HTMLLabel;
import nl.tudelft.simulation.dsol.web.animation.peer.HTMLWindow;

/**
 * HTMLToolkit.java. <br>
 * <br>
 * Copyright (c) 2003-2019 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
@SuppressWarnings("restriction")
public class HTMLToolkit extends Toolkit implements sun.awt.KeyboardFocusManagerPeerProvider
{
    /** the queue of AWT events to process. */
    EventQueue eventQueue = new EventQueue();

    /** the toolkit should implement this class. It's internal to sun, so a bit weird. */
    HTMLKeyboardFocusManagerPeer htmlKeyboardFocusManagerPeer = new HTMLKeyboardFocusManagerPeer();

    /**
     * 
     */
    public HTMLToolkit()
    {
    }

    /** {@inheritDoc} */
    @Override
    protected DesktopPeer createDesktopPeer(Desktop target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createDesktopPeer()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected ButtonPeer createButton(Button target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createButton()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected TextFieldPeer createTextField(TextField target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createTextField()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected LabelPeer createLabel(Label target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createLabel()");
        return new HTMLLabel();
    }

    /** {@inheritDoc} */
    @Override
    protected ListPeer createList(List target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createList()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected CheckboxPeer createCheckbox(Checkbox target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createCheckbox()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected ScrollbarPeer createScrollbar(Scrollbar target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createScrollbar()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected ScrollPanePeer createScrollPane(ScrollPane target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createScrollPane()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected TextAreaPeer createTextArea(TextArea target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createTextArea()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected ChoicePeer createChoice(Choice target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createChoice()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected FramePeer createFrame(Frame target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createFrame()");
        return new HTMLFrame();
    }

    /** {@inheritDoc} */
    @Override
    protected CanvasPeer createCanvas(Canvas target)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createCanvas()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected PanelPeer createPanel(Panel target)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createPanel()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected WindowPeer createWindow(Window target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createWindow()");
        return new HTMLWindow();
    }

    /** {@inheritDoc} */
    @Override
    protected DialogPeer createDialog(Dialog target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createDialog()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected MenuBarPeer createMenuBar(MenuBar target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createMenuBar()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected MenuPeer createMenu(Menu target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createMenu()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected PopupMenuPeer createPopupMenu(PopupMenu target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createPopupMenu()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected MenuItemPeer createMenuItem(MenuItem target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createMenuItem()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected FileDialogPeer createFileDialog(FileDialog target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createFileDialog()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected CheckboxMenuItemPeer createCheckboxMenuItem(CheckboxMenuItem target) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createCheckboxMenuItem()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected FontPeer getFontPeer(String name, int style)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontPeer()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Dimension getScreenSize() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenSize()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public int getScreenResolution() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getScreenResolution()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public ColorModel getColorModel() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getColorModel()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public String[] getFontList()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontList()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public FontMetrics getFontMetrics(Font font)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getFontMetrics()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void sync()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.sync()");
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage(String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image getImage(URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(String filename)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(URL url)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean prepareImage(Image image, int width, int height, ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.prepareImage()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public int checkImage(Image image, int width, int height, ImageObserver observer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.checkImage()");
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(ImageProducer producer)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Image createImage(byte[] imagedata, int imageoffset, int imagelength)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createImage()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public PrintJob getPrintJob(Frame frame, String jobtitle, Properties props)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getPrintJob()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void beep()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.beep()");
    }

    /** {@inheritDoc} */
    @Override
    public Clipboard getSystemClipboard() throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getSystemClipboard()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    protected EventQueue getSystemEventQueueImpl()
    {
        CategoryLogger.filter(Cat.WEB)
                .trace("HTMLToolkit.getSystemEventQueueImpl() -- next event is " + this.eventQueue.peekEvent());
        return this.eventQueue;
    }

    /** {@inheritDoc} */
    @Override
    public DragSourceContextPeer createDragSourceContextPeer(DragGestureEvent dge) throws InvalidDnDOperationException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.createDragSourceContextPeer()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isModalityTypeSupported(ModalityType modalityType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalityTypeSupported()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isModalExclusionTypeSupported(ModalExclusionType modalExclusionType)
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.isModalExclusionTypeSupported()");
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public Map<TextAttribute, ?> mapInputMethodHighlight(InputMethodHighlight highlight) throws HeadlessException
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.mapInputMethodHighlight()");
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public KeyboardFocusManagerPeer getKeyboardFocusManagerPeer()
    {
        CategoryLogger.filter(Cat.WEB).trace("HTMLToolkit.getKeyboardFocusManagerPeer()");
        return this.htmlKeyboardFocusManagerPeer;
    }

}

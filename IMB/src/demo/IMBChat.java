package demo;

import java.awt.BorderLayout;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import nl.tno.imb.TByteBuffer;
import nl.tno.imb.TConnection;
import nl.tno.imb.TEventEntry;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 19, 2016 <br>
 * @author TNO
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author Walter Lohman
 */
public class IMBChat
{

    protected JFrame mainform;

    private JLabel editName;

    private JTextField editFederation;

    private JTextField editMessage;

    private JTextField listboxMessages;

    private JButton btnConnect;

    private static TConnection connection = null;

    private static TEventEntry chatMessageEvent = null;

    static public Date convertWindowsTimeToDate(double comTime)
    {
        return new Date(convertWindowsTimeToMilliseconds(comTime));
    }

    static public long convertWindowsTimeToMilliseconds(double comTime)
    {
        long result = 0;

        comTime = comTime - 25569D;
        Calendar cal = Calendar.getInstance();
        result = Math.round(86400000L * comTime) - cal.get(Calendar.ZONE_OFFSET);
        cal.setTime(new Date(result));
        result -= cal.get(Calendar.DST_OFFSET);

        return result;
    }

    static public double convertDateToWindowsTime(Date javaDate)
    {
        if (javaDate == null)
        {
            throw new IllegalArgumentException("cannot convert null to windows time");
        }
        return convertMillisecondsToWindowsTime(javaDate.getTime());
    }

    static public double convertMillisecondsToWindowsTime(long milliseconds)
    {
        double result = 0.0;

        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(milliseconds);
        milliseconds += (cal.get(Calendar.ZONE_OFFSET) + cal.get(Calendar.DST_OFFSET)); // add GMT offset
        result = (milliseconds / 86400000D) + 25569D;

        return result;
    }

    /**
     * Add received message to the message received box
     * @param aDateTime Date; when was the message sent
     * @param aName String; name of sender
     * @param aMessage String; the message that was sent
     */
    void AddMessage(final Date aDateTime, final String aName, final String aMessage)
    {
        final JTextField tf = this.listboxMessages;
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                tf.setText(aDateTime.toString() + " " + aName + ": " + aMessage);
            }

        });
    }

    /**
     * Are we currently connected to an IMB server?
     * @return boolean
     */
    private boolean getConnected()
    {
        if (connection != null)
            return connection.isConnected();
        else
            return false;
    }

    private void Connect()
    {
        String server = "localhost";
        int port = 4000;
        connection = new TConnection(server, port, this.editName.getText(), 1, this.editFederation.getText());
        // TODO: connection.onDisconnect
        // connection.onDisconnect = new TConnection.TOnDisconnect()
        // {
        // @Override
        // public void dispatch(TConnection aConnection)
        // {
        // // dispatch on display thread
        // Display.getDefault().syncExec(new Runnable()
        // {
        // public void run()
        // {
        // chatMainForm.this.btnConnect.setText("Connect");
        // }
        // });
        // }
        // };
        chatMessageEvent = connection.subscribe("Chat.Message");
        chatMessageEvent.onNormalEvent = new TEventEntry.TOnNormalEvent()
        {
            @Override
            public void dispatch(TEventEntry aEvent, TByteBuffer aPayload)
            {
                // decode message
                double datetime = aPayload.readDouble();
                final String name = aPayload.readString();
                final String message = aPayload.readString();
                final Date javaDateTime = convertWindowsTimeToDate(datetime);
                // dispatch on display thread

                // show received message
                AddMessage(javaDateTime, name, message);
            }
        };
        // TODO: chatMessageEvent.OnNormalEvent
        if (getConnected())
            this.btnConnect.setText("Disconnect");
        else
            // ShowMessage("## Could not connect to hub " + aServerURI);
            // java.awt.Dialog.
            JOptionPane.showMessageDialog(null, "## Could not connect to hub ");
    }

    void Disconnect()
    {
        if (getConnected())
        {
            connection.close();
            this.btnConnect.setText("Connect");
        }
    }

    private void Send()
    {
        Date javaDateTime;
        double datetime;
        String name;
        String message;
        TByteBuffer payload;

        // send message
        javaDateTime = new Date();
        datetime = convertDateToWindowsTime(javaDateTime);
        name = this.editName.getText();
        message = this.editMessage.getText();
        // encode message
        payload = new TByteBuffer();
        payload.prepare(datetime);
        payload.prepare(name);
        payload.prepare(message);
        payload.prepareApply();
        payload.qWrite(datetime);
        payload.qWrite(name);
        payload.qWrite(message);
        chatMessageEvent.signalEvent(TEventEntry.EK_NORMAL_EVENT, payload.getBuffer());
        // show our own message
        AddMessage(javaDateTime, "me", message);
        // clear previous message text
        this.editMessage.setText("");
    }

    /**
     * Launch the application.
     * @param args
     */
    public static void main(String[] args)
    {
        try
        {
            IMBChat window = new IMBChat();
            window.open();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /**
     * Open the window.
     */
    public void open()
    {
        this.mainform = new JFrame("chat");
        this.mainform.setVisible(true);
        createContents();
        // initial values
        this.editName.setText(System.getProperty("user.name"));
        this.editFederation.setText(TConnection.DEFAULT_FEDERATION);
        // locate, add and connect hub
        // XXXXX Connect();
    }

    /**
     * Create contents of the window.
     */
    protected void createContents()
    {
        this.mainform.addWindowListener(new java.awt.event.WindowAdapter()
        {
            @Override
            public void windowClosing(java.awt.event.WindowEvent windowEvent)
            {
                Disconnect();
            }
        });
        this.mainform.setSize(693, 443);
        this.mainform.setTitle("Chat - Java");
        JPanel mainPanel = new JPanel(new BorderLayout());
        this.mainform.getContentPane().add(mainPanel);
        JPanel topPanel = new JPanel(new BorderLayout());
        mainPanel.add(topPanel, BorderLayout.NORTH);
        JPanel topLeftPanel = new JPanel(new BorderLayout());
        topLeftPanel.add(new JLabel("Name: "), BorderLayout.LINE_START);
        this.editName = new JLabel("name of the user");
        topLeftPanel.add(this.editName, BorderLayout.LINE_END);
        topPanel.add(topLeftPanel, BorderLayout.LINE_START);
        JPanel topCenterPanel = new JPanel (new BorderLayout());
        topCenterPanel.add(new JLabel("Federation: "), BorderLayout.LINE_START);
        topPanel.add(topCenterPanel, BorderLayout.CENTER);
        topPanel.add(new JLabel(" topright "), BorderLayout.LINE_END);
        mainPanel.add(new JLabel(" bottom "), BorderLayout.SOUTH);
        mainPanel.add(new JLabel(" center "), BorderLayout.CENTER);
        mainPanel.add(new JLabel(" left "), BorderLayout.LINE_START);
        mainPanel.add(new JLabel(" right "), BorderLayout.LINE_END);
        
//        JLabel lblFederation = new JLabel("Federation");
//        //lblFederation.setBounds(322, 10, 55, 15);
//        mainpanel.add(lblFederation,gbc);
//
//        JLabel lblHubs = new JLabel("Hubs");
//        //lblHubs.setBounds(10, 20, 55, 15);
//        gbc.gridx++;
//        mainpanel.add(lblHubs, gbc);
//
//        this.editFederation = new JTextField();
//        this.editFederation.setText("federation");
//        //this.editFederation.setBounds(383, 7, 147, 21);
//
//        this.btnConnect = new JButton("Connect");
//        this.btnConnect.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                if (IMBChat.this.btnConnect.getText().compareTo("Connect") == 0)
//                    Connect();
//                else
//                    Disconnect();
//            }
//        });
//        this.btnConnect.setBounds(592, 41, 75, 25);
//
//        this.listboxMessages = new JTextField("hier komen de messages");
//        //this.listboxMessages.setBounds(10, 70, 657, 298);
//        gbc.gridx = 0;
//        gbc.gridy = 1;
//        mainpanel.add(this.listboxMessages, gbc);
//
//        this.editMessage = new JTextField("");
//        this.editMessage.addKeyListener(new KeyListener()
//        {
//
//            @Override
//            public void keyTyped(KeyEvent e)
//            {
//                if (e.getKeyChar() == 13)
//                {
//                    Send();
//                }
//            }
//
//            @Override
//            public void keyPressed(KeyEvent e)
//            {
//                // ignore
//            }
//
//            @Override
//            public void keyReleased(KeyEvent e)
//            {
//                // ignore
//            }
//
//        });
//        this.editMessage.setBounds(10, 374, 565, 21);
//
//        JButton btnSend = new JButton("Send");
//        btnSend.addActionListener(new ActionListener()
//        {
//            @Override
//            public void actionPerformed(ActionEvent e)
//            {
//                Send();
//            }
//        });
//        btnSend.setBounds(592, 370, 75, 25);
        
    }
}

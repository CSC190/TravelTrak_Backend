package gui;

import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.Vector;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;

import objects.ThreadInformation;
import objects.mac_address.MacAddressData;
import objects.mac_address.MacAddressTripInformation;
import objects.mac_address.UniqueMacAddress;
import threads.Startup;

public class UserGui extends JFrame implements ItemListener, ActionListener
{
	private static final long serialVersionUID = 1L;

	private Startup startupThread;

	private Vector<String> threadTableColumnData = new Vector<String>();
	private Vector<String> eventTableColumnData = new Vector<String>();
	@SuppressWarnings("rawtypes")
	private Vector<Vector> threadTableRowData;
	@SuppressWarnings("rawtypes")
	private Vector<Vector> eventTableRowData;
	private JScrollPane threadScrollPane;
	private JTable threadTable;
	private JTable eventTable;
	private JComboBox bluetoothEventComboBox;
	private JComboBox eventTypeComboBox;

	
	public UserGui(Startup s)
	{
		this.startupThread = s;
	
		this.setSize(new Dimension(800, 600));
		getContentPane().setLayout(new CardLayout(0, 0));
		
		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		getContentPane().add(tabbedPane, "name_1389123341005869000");
		
		JPanel threadPanel = new JPanel();
		tabbedPane.addTab("Active Threads", null, threadPanel, null);
		threadPanel.setLayout(null);
		
		threadScrollPane = new JScrollPane();
		threadScrollPane.setPreferredSize(new Dimension(750, 520));
		threadScrollPane.setBounds(6, 6, 758, 485);
		threadPanel.add(threadScrollPane);
		
		threadTable = new JTable();
		threadScrollPane.setViewportView(threadTable);
		
		threadRefreshButton = new JButton("Refresh Table");
		threadRefreshButton.setBounds(329, 497, 117, 29);
		threadRefreshButton.addActionListener(this);
		threadPanel.add(threadRefreshButton);
		
		JPanel bluetoothPanel = new JPanel();
		tabbedPane.addTab("Bluetooth Events", null, bluetoothPanel, null);
		bluetoothPanel.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(6, 6, 186, 520);
		bluetoothPanel.add(panel);
		panel.setLayout(new GridLayout(0, 1, 0, 0));
		
		JLabel lblBluetoothEventSelection = new JLabel("Bluetooth Event Selection");
		panel.add(lblBluetoothEventSelection);
		
		JPanel panel_1 = new JPanel();
		panel.add(panel_1);
		panel_1.setLayout(null);
		
		JLabel lblEventType = new JLabel("Event Type");
		lblEventType.setBounds(6, 6, 174, 22);
		panel_1.add(lblEventType);
		
		eventTypeComboBox = new JComboBox();
		eventTypeComboBox.setModel(new DefaultComboBoxModel(new String[] {"Unique ID's", "Bluetooth Events"}));
		eventTypeComboBox.setBounds(6, 33, 174, 27);
		eventTypeComboBox.addItemListener(this);
		panel_1.add(eventTypeComboBox);
		
		JPanel panel_2 = new JPanel();
		panel.add(panel_2);
		panel_2.setLayout(null);
		
		JLabel lblNewLabel = new JLabel("Bluetooth Event Selector");
		lblNewLabel.setBounds(6, 6, 174, 16);
		panel_2.add(lblNewLabel);
		
		bluetoothEventComboBox = new JComboBox();
		bluetoothEventComboBox.setModel(new DefaultComboBoxModel(new String[] {"MAProcessor01", "MAProcessor23", "MAProcessor45", "MAProcessor67", "MAProcessor89", "MAProcessorAB", "MAProcessorCD", "MAProcessorEF"}));
		bluetoothEventComboBox.setBounds(6, 34, 174, 27);
		bluetoothEventComboBox.addItemListener(this);
		bluetoothEventComboBox.setEnabled(false);
		panel_2.add(bluetoothEventComboBox);
		
		eventRefreshButton = new JButton("Refresh Table");
		eventRefreshButton.setBounds(32, 138, 117, 29);
		eventRefreshButton.addActionListener(this);
		panel_2.add(eventRefreshButton);
		
		eventScrollPane = new JScrollPane();
		eventScrollPane.setPreferredSize(new Dimension(750, 520));
		eventScrollPane.setBounds(197, 104, 576, 422);
		bluetoothPanel.add(eventScrollPane);
		
		eventTable = new JTable();
		eventScrollPane.setViewportView(eventTable);
		
		JPanel panel_3 = new JPanel();
		panel_3.setBounds(197, 6, 576, 94);
		bluetoothPanel.add(panel_3);
		panel_3.setLayout(null);
		
		JLabel lblStatisticsPane = new JLabel("Event Statistics Pane");
		lblStatisticsPane.setBounds(191, 37, 139, 16);
		panel_3.add(lblStatisticsPane);
		this.setVisible(true);
	
	
		threadTableColumnData.add("Thread Name");
		threadTableColumnData.add("Thread Manager");
		threadTableColumnData.add("Thread Type");
		threadTableColumnData.add("Thread Started");

		
		this.refreshThreadsTable();
		this.refreshEventsTable();
	}
	
	
	@SuppressWarnings("rawtypes")
	public void refreshThreadsTable()
	{		
		if (this.threadTableRowData != null)
			this.threadTableRowData.clear();
		else
			this.threadTableRowData = new Vector<Vector>();
		
		

		
		if (this.threadTableRowData != null)
		{
			for (ThreadInformation ti : this.startupThread.getDeviceDaemon().returnThreadList())
			{
				Vector<String> rowData = new Vector<String>();
				
				rowData.add(ti.getThreadName());
				rowData.add(ti.getThreadManager());
				rowData.add(ti.getThreadType());
				rowData.add(ti.getDateActive().toString());
				
				this.threadTableRowData.add(rowData);
			}
			
			for (ThreadInformation ti : this.startupThread.getUniqueIDProcessor().returnThreadList())
			{
				Vector<String> rowData = new Vector<String>();
				
				rowData.add(ti.getThreadName());
				rowData.add(ti.getThreadManager());
				rowData.add(ti.getThreadType());
				rowData.add(ti.getDateActive().toString());
				
				this.threadTableRowData.add(rowData);
			}
			
			for (ThreadInformation ti : this.startupThread.getTravelTimeProcessor().returnThreadList())
			{
				Vector<String> rowData = new Vector<String>();
				
				rowData.add(ti.getThreadName());
				rowData.add(ti.getThreadManager());
				rowData.add(ti.getThreadType());
				rowData.add(ti.getDateActive().toString());
				
				this.threadTableRowData.add(rowData);
			}
		}

		
		this.threadTable = new JTable(this.threadTableRowData, this.threadTableColumnData);
		
		this.threadTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane sp = new JScrollPane(this.threadTable);
		threadScrollPane.setViewport(sp.getViewport());
	}

	private boolean refreshIsUnique = false;
	private JButton threadRefreshButton;
	private JButton eventRefreshButton;
	private JScrollPane eventScrollPane;
	
	@SuppressWarnings("rawtypes")
	public void refreshEventsTable()
	{
		if (this.eventTableColumnData != null)
			this.eventTableColumnData.clear();
		else
			this.eventTableColumnData = new Vector<String>();
		
		if (this.eventTableRowData != null)
			this.eventTableRowData.clear();
		else
			this.eventTableRowData = new Vector<Vector>();
				
		if (this.eventTableRowData != null)
		{		
			eventTableColumnData.add("Mac Address");
			eventTableColumnData.add("Unique Identifier");
			eventTableColumnData.add("Hit Count");

			if (refreshIsUnique)
			{
				Vector<UniqueMacAddress> uniques = this.startupThread.getUniqueIDProcessor().getUniqueMacData();
				
				for (UniqueMacAddress ubm : uniques)
				{
					Vector<String> rowData = new Vector<String>();
					
					rowData.add(ubm.getMacAddress());
					rowData.add(Long.toString(ubm.getUniqueID()));
					rowData.add(Integer.toString(ubm.getHitCount()));
					
					this.eventTableRowData.add(rowData);
				}
			}
			else
			{
				String name = (String)bluetoothEventComboBox.getSelectedItem();

				Vector<MacAddressTripInformation> macEvents = this.startupThread.getTravelTimeProcessor().returnThreadData(name);

				if (macEvents.size() > 0)
				{
					System.out.println();

					for (MacAddressTripInformation matte : macEvents)
					{
						System.out.print(matte.getMacAddress());
						
						for (MacAddressData mad : matte.returnBluetoothEvents())
						{
							System.out.print("\t\t" + mad.getNodeID() + ":" + mad.getTimeStamp());
						}
						
						System.out.println();
					}
				}
				else
				{
					System.out.println("No mac events available...");
				}
			}
		}

		
		this.eventTable = new JTable(this.eventTableRowData, this.eventTableColumnData);
				
		this.eventTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		
		JScrollPane sp = new JScrollPane(this.eventTable);
		eventScrollPane.setViewport(sp.getViewport());		
		
	}

	
	public JScrollPane getScrollPane() {
		return threadScrollPane;
	}
	public JComboBox getBluetoothEventComboBox() {
		return bluetoothEventComboBox;
	}
	public JComboBox getEventTypeComboBox() {
		return eventTypeComboBox;
	}

	//@Override
	public void itemStateChanged(ItemEvent e) {
		
		if (e.getSource() == this.bluetoothEventComboBox)
		{
			System.out.println("Source is bluetoothEventComboBox");
		}
		else if (e.getSource() == this.eventTypeComboBox)
		{			
			if (((String)eventTypeComboBox.getSelectedItem()).equals("Unique ID's"))
			{
				bluetoothEventComboBox.setEnabled(false);
				this.refreshIsUnique = true;
			}
			else if (((String)eventTypeComboBox.getSelectedItem()).equals("Bluetooth Events"))
			{
				bluetoothEventComboBox.setEnabled(true);
				this.refreshIsUnique = false;
			}
		}
	
		this.refreshEventsTable();
	}
	
	public JButton threadRefreshButton() {
		return threadRefreshButton;
	}
	public JButton eventRefreshButton() {
		return eventRefreshButton;
	}

	//@Override
	public void actionPerformed(ActionEvent ae) {
		if (ae.getSource() == threadRefreshButton)
		{
			this.refreshThreadsTable();

		}
		else if (ae.getSource() == eventRefreshButton)
		{
			if (((String)eventTypeComboBox.getSelectedItem()).equals("Unique ID's"))
			{
				bluetoothEventComboBox.setEnabled(false);
				this.refreshIsUnique = true;
			}
			else if (((String)eventTypeComboBox.getSelectedItem()).equals("Bluetooth Events"))
			{
				bluetoothEventComboBox.setEnabled(true);
				this.refreshIsUnique = false;
			}
			
			this.refreshEventsTable();
		}
		
	}
	public JScrollPane getEventScrollPane() {
		return eventScrollPane;
	}
}

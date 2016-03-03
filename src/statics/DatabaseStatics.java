package statics;

public final class DatabaseStatics 
{
	/*****
	 * DATABASE CONNECTION INFORMATION
	 */
	public static final String 	DATABASE_DRIVER 							= "com.mysql.jdbc.Driver";
	public static final String 	DBURL										= "jdbc:mysql://10.160.32.61";
	public static final String 	DBUSER 										= "root";
	public static final String 	DBPASS 										= "";
	public static final int 	DBPORT 										= 3006;
	public static final String 	DBNAME 										= "BCMS";
	

	/*****
	 * DEVICE TABLE INFORMATION
	 */
	public static final String DEVICE_TABLENAME 							= "C1_Devices";
	public static final String DEVICE_ID									= "dID";
	public static final String DEVICE_TYPE									= "deviceType";
	public static final String DEVICE_NAME									= "deviceName";
	public static final String DEVICE_IP									= "deviceIP";
	public static final String DEVICE_PRIMARY_PORT 							= "devicePort1";
	public static final String DEVICE_SECONDARY_PORT 						= "devicePort2";

	
	//public static final String DEVICE_REAL_LATITUDE							= "deviceRealLatitude";
	//public static final String DEVICE_REAL_LONGITUDE						= "deviceRealLongitude";
	public static final String DEVICE_CHANGED								= "deviceChanged";
	public static final String DEVICE_STATUS								= "deviceStatus";
	public static final String DEVICE_LAST_SEEN								= "deviceLastSeen";
	public static final String DEVICE_TEST									= "deviceTest";
	//public static final String DEVICE_API_KEY								= "deviceAPIKey";
	//public static final String DEVICE_SERIAL_ID								= "deviceSerialID";

	// Bluetooth specific fields
	//public static final String DEVICE_VIRTUAL_LATITUDE						= "deviceVirtualLatitude";
	//public static final String DEVICE_VIRTUAL_LONGITUDE						= "deviceVirtualLongitude";

	// C1 specific fields
	public static final String DEVICE_BUFFER_SIZE							= "deviceBufferSize";
	public static final String DEVICE_SOFTWARE_VERSION						= "deviceSoftwareVersion";
	public static final String DEVICE_MAC_ADDRESS							= "deviceMacAddress";
	
	/*****
	 * MAC ADDRESS RAW DATA TABLE INFORMATION
	 */
	public static final String MAC_RAW_TABLENAME 							= "BluetoothData_Raw";
	public static final String MAC_RAW_NODE_ID	 							= "Node_ID";
	public static final String MAC_RAW_MAC									= "MAC_Address";
	public static final String MAC_RAW_TIMESTAMP							= "Unix_Time_Stamp";
	public static final String MAC_RAW_SIGNAL_STRENGTH 						= "Signal_Strength";
	public static final String MAC_RAW_BLUETOOTH_CLASS						= "Bluetooth_Class";
	public static final String MAC_RAW_IDENTIFIER_VALUE						= "identifier_value";
	public static final String MAC_RAW_REID_CHECKED							= "ReID_Checked";
	public static final String MAC_RAW_SESSION_CHECKED						= "Session_Checked";
	
	/*****
	 * MAC ADDRESS CURRENT TRAVEL TIMES TABLE INFORMATION
	 */	
	public static final String MAC_CURR_TRAV_TIME_TABLENAME 				= "Bluetooth_Current_Travel_Times";
	public static final String MAC_CURR_TRAV_TIME_NODE_ID					= "Node_ID";
	public static final String MAC_CURR_TRAV_TIME_ADJ_NODE_ID 				= "Adjacent_Node_ID";
	public static final String MAC_CURR_TRAV_TIME_BLUETOOTH_ID				= "Bluetooth_ID";
	public static final String MAC_CURR_TRAV_TIME_TRAVEL_TIME				= "Travel_Time";
	public static final String MAC_CURR_TRAV_TIME_TIME_CALC					= "Time_Calculated";
	public static final String MAC_CURR_TRAV_TIME_ID_VALUE					= "identifier_value";
	
	/*****
	 * MAC ADDRESS NODE TRAVEL TIMES TABLE INFORMATION
	 */	
	public static final String MAC_NODE_TRAV_TIME_TABLENAME					= "Bluetooth_Node_Travel_Times";
	public static final String MAC_NODE_TRAV_TIME_NODE_ID					= "sensor_id";
	public static final String MAC_NODE_TRAV_TIME_ADJ_NODE_ID				= "adjacent_sensor_id";
	public static final String MAC_NODE_TRAV_TIME_BLUETOOTH_ID				= "mac_id";
	public static final String MAC_NODE_TRAV_TIME_TRAVEL_DURATION			= "travel_time_duration";
	public static final String MAC_NODE_TRAV_TIME_TIMESTAMP					= "travel_time_timestamp";
	public static final String MAC_NODE_TRAV_TIME_UPPER_BOUND				= "travel_time_upper_bound";
	public static final String MAC_NODE_TRAV_TIME_LOWER_BOUND				= "travel_time_lower_bound";
	public static final String MAC_NODE_TRAV_TIME_VALID						= "travel_time_valid";
	

	/*****
	 * MAC ADDRESS GRAPH TIMES TABLE INFORMATION
	 */	
	public static final String MAC_NODE_GRAPH_TIME_TABLENAME				= "Graph_Times";
	public static final String MAC_NODE_TRAV_TIME_MONTH						= "travel_time_month";
	public static final String MAC_NODE_TRAV_TIME_DAY						= "travel_time_day";
	public static final String MAC_NODE_TRAV_TIME_YEAR						= "travel_time_year";
	public static final String MAC_NODE_TRAV_TIME_HOUR						= "travel_time_hour";
	public static final String MAC_NODE_TRAV_TIME_MINUTE					= "travel_time_minute";
	public static final String MAC_NODE_TRAV_TIME_SECOND					= "travel_time_second";
	
	
	
	
	/*****
	 * MAC ADDRESS STATISTICS TABLE INFORMATION
	 */	
	public static final String MAC_STATISTICS_TABLENAME						= "Bluetooth_Statistics";
	public static final String MAC_STATISTICS_BLUETOOTH_ID					= "Bluetooth_ID";
	public static final String MAC_STATISTICS_SIGNAL_STRENGTH				= "Signal_Strength";
	public static final String MAC_STATISTICS_BLUETOOTH_CLASS				= "Bluetooth_Class";
	public static final String MAC_STATISTICS_NODE_ID						= "Node_ID";
	public static final String MAC_STATISTICS_UNIX_TIME_STAMP			 	= "Unix_Time_Stamp";
	public static final String MAC_STATISTICS_TT_CHECKED					= "TT_Checked";
	public static final String MAC_STATISTICS_ID_VALUE						= "identifier_value";
	
	/*****
	 * MAC ADDRESS SESSIONS TABLE INFORMATION
	 */	
	public static final String MAC_SESSIONS_TABLENAME						= "Bluetooth_Sessions";
	public static final String MAC_SESSIONS_NODE_ID							= "Node_ID";
	public static final String MAC_SESSIONS_FIRST_SEEN						= "First_Seen";
	public static final String MAC_SESSIONS_LAST_SEEN						= "Last_Seen";
	public static final String MAC_SESSIONS_BLUETOOTH_ID					= "Bluetooth_ID";
	public static final String MAC_SESSIONS_NO_OF_PACKETS					= "Number_of_Packets";
	public static final String MAC_SESSIONS_DURATION						= "Duration";
	public static final String MAC_SESSIONS_BLUETOOTH_CLASS					= "Bluetooth_Class";
	public static final String MAC_SESSIONS_BLUETOOTH_MAX_SIGNAL_STRENGTH 	= "Max_Signal_Strength";
	public static final String MAC_SESSIONS_BLUETOOTH_MIN_SIGNAL_STRENGTH 	= "Min_Signal_Strength";
	public static final String MAC_SESSIONS_ID_VALUE						= "identifier_value";
	
	/*****
	 * MAC ADDRESS NODE STATISTICS TABLE INFORMATION
	 */	
	public static final String MAC_NODE_STATISTICS_TABLENAME				= "Bluetooth_Node_Statistics";
	public static final String MAC_NODE_STATISTICS_DATA_DATE				= "Data_Date";
	public static final String MAC_NODE_STATISTICS_NODE_ID					= "Node_ID";
	public static final String MAC_NODE_STATISTICS_HIT_COUNT				= "Hit_Count";
	public static final String MAC_NODE_STATISTICS_MATCHES					= "Matches";
	public static final String MAC_NODE_STATISTICS_SESSIONS					= "Sessions";
	public static final String MAC_NODE_STATISTICS_UNIX_TIME_STAMP			= "Unix_Time_Stamp";
	
	/*****
	 * MAC ADDRESS TRAVEL USER TABLE INFORMATION
	 */		
	public static final String MAC_TRAVEL_USER_TABLENAME					= "Bluetooth_Travel_User";
	public static final String MAC_TRAVEL_USER_ID							= "Bluetooth_ID";
	public static final String MAC_TRAVEL_USER_ADDRESS						= "MAC_Address";
	public static final String MAC_TRAVEL_USER_TIMESTAMP					= "timestamp";
	public static final String MAC_TRAVEL_USER_BLUETOOTH_CLASS				= "Bluetooth_Class";
	public static final String MAC_TRAVEL_USER_HIT_COUNT					= "hitCount";

	/*****
	 * MAC ADDRESS STATIONARY OBJECTS TABLE INFORMATION
	 */		
	public static final String MAC_STATIONARY_OBJECTS_TABLENAME				= "Bluetooth_Stationary_Objects";
	public static final String MAC_STATIONARY_OBJECTS_NODE_ID				= "Node_ID";
	public static final String MAC_STATIONARY_OBJECTS_BLUETOOTH_ID			= "Bluetooth_ID";
	public static final String MAC_STATIONARY_OBJECTS_ID_VALUE				= "identifier_value";
	
	/*****
	 * MAC ADDRESS ADJACENT DEVICES TABLE INFORMATION	
	 */
	public static final String MAC_ADJACENT_NODE_TABLENAME					= "Bluetooth_Adjacent_Devices";
	public static final String MAC_ADJACENT_NODE_NODE_1						= "node1";
	public static final String MAC_ADJACENT_NODE_NODE_2						= "node2";
	public static final String MAC_ADJACENT_NODE_DISTANCE					= "distance_between";
	public static final String MAC_ADJACENT_BASE_TRAVEL_TIME				= "travel_time";
	public static final String MAC_ADJACENT_NODE_1_VIRTUAL_LATITUDE 		= "virtual_latitude_node_1";
	public static final String MAC_ADJACENT_NODE_1_VIRTUAL_LONGITUDE 		= "virtual_longitude_node_1";
	public static final String MAC_ADJACENT_NODE_2_VIRTUAL_LATITUDE 		= "virtual_latitude_node_2";
	public static final String MAC_ADJACENT_NODE_2_VIRTUAL_LONGITUDE 		= "virtual_longitude_node_2";
	
	/*****
	 * C1 READER DATA TABLE INFORMATION	
	 */
	public static final String C1_READER_DATA_TABLENAME						= "C1_Reader_Data_Test_Videosync_15";
	public static final String C1_READER_CHANNEL							= "C1_Reader_Channel";
	public static final String C1_READER_MILLI							    = "C1_Reader_Millis";
	public static final String C1_READER_UNIX								= "C1_Reader_Unix";
	public static final String C1_READER_STATE								= "C1_Reader_State";
	public static final String C1_READER_TICK								= "C1_Reader_Ticks";
	public static final String C1_CHIP_NUM							        = "C1_Chip_Num";
	public static final String C1_DATE							            = "C1_Date";
	public static final String C1_PIN										= "C1_Pin";
	public static final String LOG_170_CHANNEL								= "Log170_Channel";
	
}


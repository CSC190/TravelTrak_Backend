package statics;

public class PreparedStatementStatics 
{
	
	public static java.sql.Timestamp getCurrentTimeStamp()
	{
		java.util.Date today = new java.util.Date();
		return new java.sql.Timestamp(today.getTime());
	}
	
	public static String DEVICE_ACTIVE_QUERY = String.format("SELECT * FROM %s WHERE %s = 1 AND %s = 0",
															DatabaseStatics.DEVICE_TABLENAME, 
															DatabaseStatics.DEVICE_STATUS,
															DatabaseStatics.DEVICE_CHANGED);
	
	public static String DEVICE_LAST_SEEN = String.format("UPDATE %s SET %s = ? WHERE %s = 1",
															DatabaseStatics.DEVICE_TABLENAME,
															DatabaseStatics.DEVICE_LAST_SEEN,
															DatabaseStatics.DEVICE_STATUS);
	

	public static String DEVICE_INACTIVE_QUERY = String.format("SELECT * FROM %s WHERE %s = 0",
															DatabaseStatics.DEVICE_TABLENAME ,
															DatabaseStatics.DEVICE_STATUS);
	
	public static String DEVICE_NOW_ACTIVE_UPDATE = String.format("UPDATE %s SET %s = 1, %s = 1 WHERE %s = ?", 
															DatabaseStatics.DEVICE_TABLENAME, 
															DatabaseStatics.DEVICE_CHANGED, 
															DatabaseStatics.DEVICE_STATUS, 
															DatabaseStatics.DEVICE_ID);

	public static String DEVICE_CHANGED_QUERY = String.format("SELECT * FROM %s WHERE %s = 1",
															DatabaseStatics.DEVICE_TABLENAME,
															DatabaseStatics.DEVICE_CHANGED);

	public static String DEVICE_UPDATE_CHANGED = String.format("UPDATE %s SET %s = ?  WHERE %s = ?", 
															DatabaseStatics.DEVICE_TABLENAME, 
															DatabaseStatics.DEVICE_CHANGED, 
															DatabaseStatics.DEVICE_ID);
	
	public static String DEVICE_STATUS_CHANGED = String.format("UPDATE %s SET %s = ?  WHERE %s = ?", 
															DatabaseStatics.DEVICE_TABLENAME, 
															DatabaseStatics.DEVICE_STATUS, 
															DatabaseStatics.DEVICE_ID);

	/*public static String MAC_RAW_INSERT = String.format("INSERT INTO %s (%s, %s, %s) VALUES (?, ?, ?)",
															DatabaseStatics.MAC_RAW_TABLENAME,
															DatabaseStatics.MAC_RAW_NODE_ID,
															DatabaseStatics.MAC_RAW_MAC,
															DatabaseStatics.MAC_RAW_TIMESTAMP);*/

	/*public static String ADJACENT_NODES_QUERY = String.format("SELECT %s, %s, %s, %s FROM %s WHERE %s = ? OR %s = ?", 
															DatabaseStatics.MAC_ADJACENT_NODE_NODE_1,
															DatabaseStatics.MAC_ADJACENT_NODE_NODE_2,
															DatabaseStatics.MAC_ADJACENT_BASE_TRAVEL_TIME,
															DatabaseStatics.MAC_ADJACENT_NODE_DISTANCE,
															DatabaseStatics.MAC_ADJACENT_NODE_TABLENAME, 
															DatabaseStatics.MAC_ADJACENT_NODE_NODE_1,
															DatabaseStatics.MAC_ADJACENT_NODE_NODE_2);*/
	
	
	
	// *********************************************************************************************************
	// Bluetooth Statistics Processing 
	public static String GET_NODE_HITS = String.format("SELECT %s, %s, %s FROM %s WHERE %s = 0 ORDER BY '%s' ASC LIMIT 500",
															DatabaseStatics.MAC_RAW_NODE_ID,
															DatabaseStatics.MAC_RAW_MAC,
															DatabaseStatics.MAC_RAW_TIMESTAMP,
															DatabaseStatics.MAC_RAW_TABLENAME, 
															DatabaseStatics.MAC_RAW_REID_CHECKED,
															DatabaseStatics.MAC_RAW_TIMESTAMP);
	
	
	public static String INSERT_NEW_HIT = String.format("INSERT IGNORE INTO %s (%s, %s, %s) Values(?, ?, ?)", 	
															DatabaseStatics.MAC_STATISTICS_TABLENAME,
															DatabaseStatics.MAC_STATISTICS_BLUETOOTH_ID, 
															DatabaseStatics.MAC_STATISTICS_NODE_ID,
															DatabaseStatics.MAC_STATISTICS_UNIX_TIME_STAMP);
	
	
	public static String INSERT_SESSION = String.format("INSERT INTO %s (%s, %s, %s, %s, %s) VALUES (?, %s + 1, ?, ?, ?)",
															DatabaseStatics.MAC_SESSIONS_TABLENAME,
															DatabaseStatics.MAC_SESSIONS_LAST_SEEN,
															DatabaseStatics.MAC_SESSIONS_NO_OF_PACKETS,
															DatabaseStatics.MAC_SESSIONS_DURATION,
															DatabaseStatics.MAC_SESSIONS_BLUETOOTH_ID,
															DatabaseStatics.MAC_SESSIONS_NODE_ID,
															DatabaseStatics.MAC_SESSIONS_NO_OF_PACKETS);
	
	
	public static String UPDATE_SESSION = String.format("UPDATE %s SET %s = ?, %s = %s + 1, %s = ? WHERE %s = ? AND %s = ? AND %s = ?",
															DatabaseStatics.MAC_SESSIONS_TABLENAME,
															DatabaseStatics.MAC_SESSIONS_LAST_SEEN,
															DatabaseStatics.MAC_SESSIONS_NO_OF_PACKETS,
															DatabaseStatics.MAC_SESSIONS_NO_OF_PACKETS,
															DatabaseStatics.MAC_SESSIONS_DURATION,
															DatabaseStatics.MAC_SESSIONS_BLUETOOTH_ID,
															DatabaseStatics.MAC_SESSIONS_NODE_ID,
															DatabaseStatics.MAC_SESSIONS_FIRST_SEEN);
	
	

	public static String UPDATE_REID = String.format("UPDATE %s SET %s = 1 WHERE %s = ? AND %s = ? AND %s = ?", 
															DatabaseStatics.MAC_RAW_TABLENAME,
														    DatabaseStatics.MAC_RAW_REID_CHECKED,
															DatabaseStatics.MAC_RAW_MAC,
															DatabaseStatics.MAC_RAW_NODE_ID,
															DatabaseStatics.MAC_RAW_TIMESTAMP);

	
	public static String GET_ALL_USERS = String.format("SELECT %s, %s, %s FROM %s ORDER BY pid ASC", 
															DatabaseStatics.MAC_TRAVEL_USER_ID, 
															DatabaseStatics.MAC_TRAVEL_USER_ADDRESS, 
															DatabaseStatics.MAC_TRAVEL_USER_HIT_COUNT,
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME);

	
	public static String ADD_NEW_MAC_USER = String.format("INSERT INTO %s (%s, %s, %s, %s) VALUES (?, ?, ?, ?)",
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME,
															DatabaseStatics.MAC_TRAVEL_USER_ID, 
															DatabaseStatics.MAC_TRAVEL_USER_ADDRESS,
															DatabaseStatics.MAC_TRAVEL_USER_HIT_COUNT,
															DatabaseStatics.MAC_TRAVEL_USER_TIMESTAMP);

	public static String UPDATE_MAC_USER = String.format("UPDATE %s SET %s = ? WHERE %s = ?",
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME,
															DatabaseStatics.MAC_TRAVEL_USER_HIT_COUNT, 
															DatabaseStatics.MAC_TRAVEL_USER_ADDRESS);
	
	public static String GET_STATIONARY_DEVICES = String.format("SELECT * FROM %s", 
															DatabaseStatics.MAC_STATIONARY_OBJECTS_TABLENAME);

	
	public static String GET_BLUETOOTH_SESSIONS = String.format("SELECT * FROM %s", 
															DatabaseStatics.MAC_SESSIONS_TABLENAME);
	
	
	public static String DELETE_MAC_UNIQUE_ID = String.format("DELETE FROM %s WHERE %s = ? AND %s = ?",
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME,
															DatabaseStatics.MAC_TRAVEL_USER_ADDRESS,
															DatabaseStatics.MAC_TRAVEL_USER_ID);

	public static String DELETE_RAW_MAC = String.format("DELETE FROM %s WHERE %s = ?",
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME,
															DatabaseStatics.MAC_TRAVEL_USER_ADDRESS);

	public static String PURGE_RAW_MAC_HITS = String.format("DELETE FROM %s WHERE %s < ?",
															DatabaseStatics.MAC_RAW_TABLENAME, 
															DatabaseStatics.MAC_RAW_TIMESTAMP);
	
	public static String PURGE_OLD_MAC_USERS = String.format("DELETE FROM %s WHERE %s < ?",
															DatabaseStatics.MAC_TRAVEL_USER_TABLENAME,
															DatabaseStatics.MAC_TRAVEL_USER_TIMESTAMP);

	public static String GET_MAC_SENSOR_SEGMENTS = String.format("SELECT * FROM %s",
															DatabaseStatics.MAC_ADJACENT_NODE_TABLENAME);
	
	public static String GET_MAC_SENSOR_NAME = String.format("Select %s FROM %s WHERE %s = ?",
															DatabaseStatics.DEVICE_NAME,
															DatabaseStatics.DEVICE_TABLENAME,
															DatabaseStatics.DEVICE_ID);
			
	public static String INSERT_NEW_TRAVEL_TIMES = String.format("INSERT INTO %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?",
															DatabaseStatics.MAC_NODE_TRAV_TIME_TABLENAME,
															DatabaseStatics.MAC_NODE_TRAV_TIME_NODE_ID,
															DatabaseStatics.MAC_NODE_TRAV_TIME_ADJ_NODE_ID,
															DatabaseStatics.MAC_NODE_TRAV_TIME_BLUETOOTH_ID,
															DatabaseStatics.MAC_NODE_TRAV_TIME_TRAVEL_DURATION,
															DatabaseStatics.MAC_NODE_TRAV_TIME_TIMESTAMP,
															DatabaseStatics.MAC_NODE_TRAV_TIME_UPPER_BOUND,
															DatabaseStatics.MAC_NODE_TRAV_TIME_LOWER_BOUND,
															DatabaseStatics.MAC_NODE_TRAV_TIME_VALID);
	
	
	
	public static String INSERT_C1_DATA = String.format("INSERT INTO %s SET %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?, %s = ?",
															DatabaseStatics.C1_READER_DATA_TABLENAME,
															DatabaseStatics.C1_CHIP_NUM,
															DatabaseStatics.C1_READER_CHANNEL,
															DatabaseStatics.C1_PIN,
															DatabaseStatics.LOG_170_CHANNEL,
															DatabaseStatics.C1_READER_STATE,
															DatabaseStatics.C1_READER_TICK,
															DatabaseStatics.C1_READER_MILLI,
															DatabaseStatics.C1_READER_UNIX,
															DatabaseStatics.C1_DATE); 														
			
	
			
			
			
			
}

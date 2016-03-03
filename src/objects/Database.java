package objects;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import statics.DatabaseStatics;

public class Database 
{
	private Connection connection;
	private Statement sql;
	
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Database Construction 

	public Database()
	{
		connect();
	}
	
	//-------------------------------------------------------------------------------------------------------------------------------------
	//-------------------------------------------------------------------------------------------------------------------------------------
	// -- Database Connection Management 

	public boolean connect()
	{
		boolean connected = false;
		
		try
		{
			Class.forName(DatabaseStatics.DATABASE_DRIVER).newInstance();
			connection = DriverManager.getConnection(DatabaseStatics.DBURL, DatabaseStatics.DBUSER, DatabaseStatics.DBPASS);
			
			if (!connection.isClosed())
			{
				connected = true;
				sql = connection.createStatement();
				sql.execute("USE " + DatabaseStatics.DBNAME);
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();			
		} 
		
		return connected;
	}
	
	public Connection getConnection()
	{
		return this.connection;
	}
	
	/**
	 * Closes the ResultSet Object, SQLStatement Object, and the database connection.
	 * It is up to the application to initiate a close when appropriate		
	 */
	public void close()
	{
		try 
		{							
			if(sql !=null && !sql.isClosed())
				sql.close();
	
			if (connection != null && !connection.isClosed())
				connection.close();
	    } 
		catch(Exception e) 
	    {
	    	System.err.println("Error closing connection to database: " + DatabaseStatics.DBURL + "\nException: " + e.getMessage());
	    }
		
	}
	
	/**
	 * Counts the number of records in the ResultSet
	 * @param newrs - a ResultSet
	 * @return an int specifying the size of the ResultSet
	 */
	public int countResults(ResultSet newrs)
	{
		int size = 0;
		if(newrs != null)
		{
			try 
			{
				newrs.beforeFirst();
				newrs.last();
				size = newrs.getRow();
				
				newrs.beforeFirst();
			} 
			catch (SQLException e) 
			{
				System.err.println("Error counting ResultSet: " + e.getMessage());
			}
		}
		
		return size;	
	}
}

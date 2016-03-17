package Process;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

import communication.Message;

public class testBench {

	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/wirelessdb?user=root&password=maggie";

	static final String USER = "root";
	static final String PASS = "maggie";

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		
		
		
		
		double start_lat = 40.442283630;
		double start_long = -79.947059641;
		double del_lat = .00010;
		double del_long = .00005;


		Message msg = new Message("sammy",start_lat,start_long,del_lat,del_long);
		insertToDB(msg);
		
	}

	

	private static int counter=-1;
	private static void insertToDB(Message msg){
		// --------------------------------
		// testing mysql
		Connection conn = null;
		Statement stmt = null;

		String lattitude = Double.toString(msg.getLattitude());
		String longitude = Double.toString(msg.getLongitude());
		String vector_x = Double.toString(msg.getVector_x());
		String vector_y = Double.toString(msg.getVector_y());
		String name = msg.getSender();
		String seqNum = Integer.toString(++counter);
		
		
		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			//STEP 4: Execute a query
			
			// get max seqNum
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			String sql = "select max(seqNum) as seqNum from steps";

			
			System.out.println("Inserting records into the table...");
			stmt = conn.createStatement();

			sql = "insert into steps " +
					"values ("+lattitude+", "+longitude+","+vector_x+","+vector_y+",'"+name+"',"+seqNum+")";
			stmt.executeUpdate(sql);
			System.out.printf("Inserted record %s\n",sql);

		}catch(SQLException se){
			//Handle errors for JDBC
			se.printStackTrace();
		}catch(Exception e){
			//Handle errors for Class.forName
			e.printStackTrace();
		}finally{
			//finally block used to close resources
			try{
				if(stmt!=null)
					conn.close();
			}catch(SQLException se){
			}// do nothing
			try{
				if(conn!=null)
					conn.close();
			}catch(SQLException se){
				se.printStackTrace();
			}//end finally try
		}//end try
		System.out.println("Goodbye!");

		
	}

	
}

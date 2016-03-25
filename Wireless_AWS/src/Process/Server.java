package Process;


import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import communication.Message;
import communication.MessagePasser;

public class Server {
	static String config_file_address;
	private static MessagePasser messagePasser;
	
	static final String JDBC_DRIVER = "com.mysql.jdbc.Driver";  
	static final String DB_URL = "jdbc:mysql://localhost/wirelessdb?user=root&password=maggie";

	static final String USER = "root";
	static final String PASS = "maggie";

	static String serverIP;
	static int serverPort;

	public static void main(String[] args) {
		
		// --------------------------------
		// initialize - get necessary parameters from input

		//serverIP = "52.36.135.251";
		serverIP = "127.0.0.1";
		serverPort = 4001;
		System.out.println("Server address: " + config_file_address+":"+serverPort);
		

		// --------------------------------
		// instantiate the required objects
		


		
		messagePasser = new MessagePasser(serverIP,serverPort, true);

		// --------------------------------
		// Execute the receiver/sender scripts

		setCounterFromSeqNum();
		
		run_receiver(messagePasser);
		
		/*
		 * TODO: Re-factor and simplify everything:
		 * -Simplify config file so it only has the basics: port, IPs, etc.
		 * -Modify Message, so that it sends a name and GPS coordiantes
		 * 
		 */
		


	}

	private static int counter=-1;
	
	private static void setCounterFromSeqNum(){
		// --------------------------------
		// testing mysql
		Connection conn = null;
		Statement stmt = null;

		try{
			//STEP 2: Register JDBC driver
			Class.forName("com.mysql.jdbc.Driver");

			//STEP 3: Open a connection
			System.out.println("Connecting to a selected database...");
			conn = DriverManager.getConnection(DB_URL, USER, PASS);
			System.out.println("Connected database successfully...");

			//STEP 4: Execute a query
			
			// get max seqNum
			System.out.println("Getting max seqNum...");
			stmt = conn.createStatement();

			String sql = "SELECT max(seqNum) AS seqNum FROM steps";
			ResultSet rslt = stmt.executeQuery(sql);
	        while (rslt.next()) {
	            counter = rslt.getInt("seqNum");
	        }			

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
        System.out.println("Counter set to: "+counter);
	}
	
	
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

			String sql = "insert into steps " +
					"values ("+lattitude+", "+longitude+","+vector_x+","+vector_y+",'"+name+"',"+seqNum+")";
			stmt.executeUpdate(sql);

			
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

	private static void run_receiver(MessagePasser msg_passer) {
		System.out.println("Starting server");
		Thread receiver_thread = new Thread() {
			public void run() {
				while (true) {
					synchronized (messagePasser.receive_block) {
						try {
							messagePasser.receive_block.wait();
						} catch (InterruptedException e) {
							System.out.println("failed to wait");
							e.printStackTrace();
						}
						Message rcved = msg_passer.receive();
						if (rcved != null) {
							rcved.print();
							insertToDB(rcved);
						}
					}
				}
			}
		};

		receiver_thread.start();
	}
}

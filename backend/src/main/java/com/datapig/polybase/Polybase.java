package com.datapig.polybase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import com.datapig.config.PropertiesFileReader;

public class Polybase {

    private static String DB_URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DATA_SOURCE;
    private static String schema="dbo";

	public static void startSyncInfolder(String foldername) {
		// TODO Auto-generated method stub

		HashMap<String, String> configs=PropertiesFileReader.getConfigs();
		
		DB_URL=configs.get("DB_URL");
		USERNAME=configs.get("USERNAME");
		PASSWORD=configs.get("PASSWORD");
		DATA_SOURCE=configs.get("DATA_SOURCE");
		
		Connection connection=openConnection(DB_URL, USERNAME, PASSWORD);
		
		Set<String> tabesInFolder=getTableNamesByFolder(connection,foldername);
		
		closeConnection(connection);
		// Create an ExecutorService with a fixed thread pool
        ExecutorService executorService = Executors.newFixedThreadPool(tabesInFolder.size());
        for(String tableName:tabesInFolder) {
        	Connection connection2=openConnection(DB_URL, USERNAME, PASSWORD);
        	preMergeAction(connection2, tableName,foldername);
        	closeConnection(connection2);
        	PolybaseThread polybaseThread=new PolybaseThread(DB_URL,USERNAME,PASSWORD,DATA_SOURCE,schema,tableName,foldername);
        	executorService.submit(polybaseThread);
        }
        // Shutdown the executor service
        executorService.shutdown();
        
        try {
            // Wait for a maximum of 1 hour for all tasks to complete
            if (!executorService.awaitTermination(1, TimeUnit.HOURS)) {
                System.out.println("Tasks did not finish in the allotted time, forcing shutdown.");
                executorService.shutdownNow(); // Forcefully shutdown if tasks exceed the time limit
            }
        } catch (InterruptedException e) {
            System.err.println("Thread was interrupted while waiting for tasks to complete.");
            executorService.shutdownNow(); // Force shutdown on interruption
        }

        System.out.println("All data merge tasks completed for folder : " +foldername);
				
	}
	
	
	 private static void preMergeAction(Connection connection, String tableName,String folder) {
		    
		 	String dropTableSQL= "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"+tableName+"') "
	             + "BEGIN "
	             + "DROP TABLE dbo._staging_"+tableName +"\r\n"
	             + " END";
	     try {
	     	
	     	Statement statement = connection.createStatement();
			System.out.println(dropTableSQL);
	     	statement.executeUpdate(dropTableSQL);
	     	
	     	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
	     	String sql = "UPDATE MetaDataCatlog SET LastStartCopyDate=?,LastCopyStatus=?,LastUpdatedFolder=? WHERE TableName=?";
	         // Prepare the statement
	         PreparedStatement preparedStatement = connection.prepareStatement(sql);

	         // Set the parameters
	         preparedStatement.setTimestamp(1, timestamp); 
	         preparedStatement.setInt(2, 1);
	         preparedStatement.setString(3, folder);
	         preparedStatement.setString(4, tableName);
	         // Execute the update
	         int rowsAffected = preparedStatement.executeUpdate();
	         System.out.println("Updated MetaDataCatlog " + rowsAffected + " row(s).");
	         preparedStatement.close();
	         
	     	
	        } catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	 }
	
	
	private static Set<String> getTableNamesByFolder(Connection connection,String foldername){
		String query="SELECT tablename FROM [dbo].[FolderSyncStatus] WHERE folder='"+foldername+"' and copystatus=0";
        Statement statement = null;
        ResultSet resultSet = null;
        
        Set<String> tableNames=new LinkedHashSet<String>();
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                
                String result = resultSet.getString("tablename");
                System.out.println(result);
                tableNames.add(result);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return tableNames;
	}


    private static Connection openConnection(String DB_URL,String USERNAME,String PASSWORD) {
        Connection connection = null;
        try {
        	Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver");
            System.out.println("JDBC Driver registered successfully.");
        	connection = DriverManager.getConnection(DB_URL, USERNAME, PASSWORD);
            // Create the table if it does not exist
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
        return connection;
    }
 
    // Function to close the database connection
    private static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
                System.out.println("Connection closed successfully.");
            } catch (SQLException e) {
                System.out.println("Could not close connection.");
                e.printStackTrace();
            }
        }
    }

}

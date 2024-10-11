package com.datapig.polybase;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
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

import com.datapig.config.PropertiesFileReader;

public class SynapseLogParser {

    // Database credentials
    private static String DB_URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String DATA_SOURCE;

	public static void startParse(String filePath) {
		// TODO Auto-generated method stub
		HashMap<String,String> configs= PropertiesFileReader.getConfigs();
		
		DB_URL=configs.get("DB_URL");
		USERNAME=configs.get("USERNAME");
		PASSWORD=configs.get("PASSWORD");
		DATA_SOURCE=configs.get("DATA_SOURCE");
		
				
		Set<String> set=readSynapseLog(filePath);
		Connection con= openConnection(DB_URL, USERNAME, PASSWORD);
		Set<String> allfolder=allFoldersInDB(con);
		
		for(String folderName:set) {
			if(!allfolder.contains(folderName)) {
				insertChangeLog(con, folderName);
			}
		}
		
		Set<String> existingFolderNotStaged=existingFolderNotStaged(con);
		

		for(String folderName:existingFolderNotStaged) {
			Set<String> tableNames=getTableInFolder(con,folderName);
			for(String tbl:tableNames) {
				insertFolderSyncStatus(con,folderName,tbl);
			}
			updateChangeLog(con,folderName);
		}
		closeConnection(con);
		boolean flag=true;
		while(flag) {
			Connection con1= openConnection(DB_URL, USERNAME, PASSWORD);
			String folder=foldersNeedToBeProcessed(con1);
			Polybase.startSyncInfolder(folder);
			int folderCount=countFoldersNeedToBeProcessed(con1);
			updateChangeLogToComplete(con1,folder);
			if(folderCount==0) {
				flag=false;
			}
			closeConnection(con1);
		}
		
	}
	
	

	private static int countFoldersNeedToBeProcessed(Connection connection){
		String query="SELECT COUNT(*) folderCount FROM ChangeLog WHERE stagestatus=1";
		
        Statement statement = null;
        ResultSet resultSet = null;
        
        int folderCount=0;
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                int result = resultSet.getInt("folderCount");
                System.out.println(result);
                folderCount=result;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return folderCount;
	}

	
	private static String foldersNeedToBeProcessed(Connection connection){
		String query="SELECT foldername FROM ChangeLog WHERE stagetime=(SELECT MIN(stagetime) FROM ChangeLog where stagestatus=1 and foldername in (SELECT folder FROM FolderSyncStatus WHERE copystatus=0))";
		
        Statement statement = null;
        ResultSet resultSet = null;
        
        String folderName=null;
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                String result = resultSet.getString("foldername");
                System.out.println(result);
                folderName=result;
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return folderName;
	}

	private static void updateChangeLogToComplete(Connection connection,String folderName) {
        PreparedStatement preparedStatement = null;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            // Create an SQL UPDATE statement
            String sql = "UPDATE [dbo].[ChangeLog]\r\n"
            		+ "   SET [stagestatus] = ?,[stagetime] = ?\r\n"
            		+ " WHERE [foldername] = ?";

            // Prepare the statement
            preparedStatement = connection.prepareStatement(sql);

            // Set the parameters
            preparedStatement.setInt(1,2); 
            preparedStatement.setTimestamp(2, timestamp); 
            preparedStatement.setString(3, folderName);
            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Updated " + rowsAffected + " row(s).");
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
	}

	private static void updateChangeLog(Connection connection,String folderName) {
        PreparedStatement preparedStatement = null;
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        try {
            // Create an SQL UPDATE statement
            String sql = "UPDATE [dbo].[ChangeLog]\r\n"
            		+ "   SET [stagestatus] = ?,[stagetime] = ?\r\n"
            		+ " WHERE [foldername] =?";

            // Prepare the statement
            preparedStatement = connection.prepareStatement(sql);

            // Set the parameters
            preparedStatement.setInt(1,1); 
            preparedStatement.setTimestamp(2, timestamp); 
            preparedStatement.setString(3, folderName);
            // Execute the update
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Updated " + rowsAffected + " row(s).");
            preparedStatement.close();
        } catch (SQLException e) {
            e.printStackTrace();
        } 
	}
	
    // Function to insert values into the table
    private static void insertFolderSyncStatus(Connection connection,String folder,String tableName) {
    	
    	String insertSQL = "INSERT INTO [dbo].[FolderSyncStatus]\r\n"
    			+ "           ([folder]\r\n"
    			+ "           ,[tablename]\r\n"
    			+ "           ,[copystatus])\r\n"
    			+ "     VALUES\r\n"
    			+ "           (?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, folder);
            preparedStatement.setString(2, tableName);
            preparedStatement.setInt(3, 0);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Inserted " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
	private static Set<String> getTableInFolder(Connection connection,String folderName){
		String query="SELECT tablename\r\n"
				+ "			FROM\r\n"
				+ "				OPENROWSET(\r\n"
				+ "					BULK '/"+folderName+"/model.json',\r\n"
				+ "					DATA_SOURCE = '"+DATA_SOURCE+"',\r\n"
				+ "					FORMAT = 'CSV',\r\n"
				+ "					FIELDQUOTE = '0x0b',\r\n"
				+ "					FIELDTERMINATOR ='0x0b',\r\n"
				+ "					ROWTERMINATOR = '0x0b'\r\n"
				+ "				)\r\n"
				+ "				WITH \r\n"
				+ "				(\r\n"
				+ "					jsonContent varchar(MAX)\r\n"
				+ "				) AS r\r\n"
				+ "				cross apply openjson(jsonContent) with (entities nvarchar(max) as JSON)\r\n"
				+ "				cross apply openjson (entities) with([tablename] NVARCHAR(200) '$.name', [partitions] NVARCHAR(MAX) '$.partitions' as JSON ) t\r\n"
				+ "				where  [partitions] != '[]'\r\n"
				+ "				group by tablename\r\n"
				+ "";
        Statement statement = null;
        ResultSet resultSet = null;
        
        Set<String> folderNames=new LinkedHashSet<String>();
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                String result = resultSet.getString("tablename");
                System.out.println(folderName+" "+result);
                folderNames.add(result);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return folderNames;
	}
	
	
	private static Set<String> allFoldersInDB(Connection connection ){
		String query="SELECT foldername FROM [dbo].[ChangeLog]\r\n"
				+ "ORDER BY stagetime ASC";
        Statement statement = null;
        ResultSet resultSet = null;
        
        Set<String> folderNames=new LinkedHashSet<String>();
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                String result = resultSet.getString("foldername");
                System.out.println(result);
                folderNames.add(result);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return folderNames;
	}
	
	
	private static Set<String> existingFolderNotStaged(Connection connection ){
		String query="SELECT foldername FROM [dbo].[ChangeLog]\r\n"
				+ "WHERE stagestatus=0\r\n"
				+ "ORDER BY stagetime ASC";
        Statement statement = null;
        ResultSet resultSet = null;
        
        Set<String> folderNames=new LinkedHashSet<String>();
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                String result = resultSet.getString("foldername");
                System.out.println(result);
                folderNames.add(result);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return folderNames;
	}
	
	
    // Function to insert values into the table
    private static void insertChangeLog(Connection connection,String folderName) {
    	Timestamp timestamp = new Timestamp(System.currentTimeMillis());
    	String insertSQL = "INSERT INTO [dbo].[ChangeLog]\r\n"
        		+ "           ([foldername]\r\n"
        		+ "           ,[stagestatus]\r\n"
        		+ "           ,[stagetime])\r\n"
        		+ "     VALUES\r\n"
        		+ "           (?,?,?)";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, folderName);
            preparedStatement.setInt(2, 0);
            preparedStatement.setTimestamp(3, timestamp);

            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Inserted " + rowsAffected);
        } catch (SQLException e) {
            e.printStackTrace();
        }
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

    private static Set<String> readSynapseLog(String filePath) {
        Set<String> set = new LinkedHashSet<String>();
        try (BufferedReader br = new BufferedReader(new FileReader(filePath))) {
            String line;
            // Read the file line by line
            while ((line = br.readLine()) != null) {
                // Split the line using comma as delimiter
                String[] parts = line.split(",");
                // Add each part to the set
                for (String part : parts) {
                    set.add(part.trim()); // trim to remove leading/trailing spaces
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        // Print the set
        System.out.println("Set contents: " + set);
        return set;
    }

}


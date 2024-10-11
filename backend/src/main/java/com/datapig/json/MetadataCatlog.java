package com.datapig.json;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.datapig.config.PropertiesFileReader;
import com.google.gson.Gson;


public class MetadataCatlog {
    
    // Database credentials
    private static String DB_URL;
    private static String USERNAME;
    private static String PASSWORD;
    private static String environment;
    private static String storageAccount;
    private static String filePath;
	private static int offset;
	private static int STRING_MAXLENGTH;
	private static String STRING_OUTLIER_PATH;
    
	
    public static void main(String[] args) {
		
		HashMap<String, String> configs=PropertiesFileReader.getConfigs();
		
		DB_URL=configs.get("DB_URL");
		USERNAME=configs.get("USERNAME");
		PASSWORD=configs.get("PASSWORD");
		environment=configs.get("ENVIRONMENT");
		storageAccount=configs.get("STORAGE_ACCOUNT");
		filePath =configs.get("LOCAL_MOLDEL_JSON"); // Path to your input JSON file
		offset=Integer.parseInt(configs.get("STRING_OFFSET"));
		STRING_MAXLENGTH=Integer.parseInt(configs.get("STRING_MAXLENGTH"));
		STRING_OUTLIER_PATH=configs.get("STRING_OUTLIER_PATH");
        String json = readJsonFromFile(filePath);
        if (json == null) {
            System.err.println("Error reading JSON from file.");
            return;
        }

        Gson gson = new Gson();
        Root root = gson.fromJson(json, Root.class);

        // Output some parsed data
        System.out.println("Name: " + root.getName());
        System.out.println("Description: " + root.getDescription());
        System.out.println("Version: " + root.getVersion());
        
        Map<String, Map<String,String>> sqlTypeMap = createSqlTypeMap(root);
        
        
        Connection con=openConnection(DB_URL, USERNAME, PASSWORD);
        Set<String> existingTables=getMetadataCatlog(con);
        Set<String> tablenames=sqlTypeMap.keySet();
        for(String tablename:tablenames) {
        	if(!existingTables.contains(tablename)) {
        		Map<String,String> values=sqlTypeMap.get(tablename);
            	String dataFrame=values.get("dataFrame");
            	String selectQuery=values.get("selectQuery");
            	String columnNames=values.get("columnNames");
            	createTableIfNotExists(con, tablename,dataFrame);
            	insertValues(con,environment,storageAccount,tablename,selectQuery,dataFrame,columnNames);
        	}
        }
        closeConnection(con);
        
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
    
    private static Set<String> getMetadataCatlog(Connection connection){
        Statement statement = null;
        ResultSet resultSet = null;
        String query="SELECT TableName from [dbo].[MetaDataCatlog]";
        Set<String> tableNames=new HashSet<String>();
        try {
            // Create a statement
            statement = connection.createStatement();
            // Execute the query
            resultSet = statement.executeQuery(query);

            // Process the result set
            while (resultSet.next()) {
                // Example: Assuming you have a column named "column_name"
                String result = resultSet.getString("TableName");
                System.out.println(result);
                tableNames.add(result);
            }
            
        } catch (SQLException e) {
            e.printStackTrace();
        } 
        return tableNames;
    }

 
    private static void createTableIfNotExists(Connection connection, String tableName,String dataFrame) {
        String createTableSQL = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = 'dbo."+tableName+"') "
                + "BEGIN "
                + "CREATE TABLE dbo."+ tableName + "("+dataFrame+") "
                + "END";

        try (Statement statement = connection.createStatement()) {
            System.out.println(createTableSQL);
        	
        	statement.executeUpdate(createTableSQL);
 
            System.out.println("Checked and created table " + tableName );
            String createid_idx="CREATE UNIQUE INDEX dbo_"+tableName+"_Id_idx ON dbo."+tableName+"(Id) with (ONLINE=ON)";
            statement.executeUpdate(createid_idx);
            String createrecid_idx="CREATE UNIQUE INDEX dbo_"+tableName+"_RecId_idx ON dbo."+tableName+"(recid) with (ONLINE=ON)";
            statement.executeUpdate(createrecid_idx);
            String createversionnumber_idx="CREATE UNIQUE INDEX dbo_"+tableName+"_versionnumber_idx ON dbo."+tableName+"(versionnumber) with (ONLINE=ON)";
            statement.executeUpdate(createversionnumber_idx);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    
    
    // Function to insert values into the table
    private static void insertValues(Connection connection,String environment,String storageAccount,String tableName,String selectColumn,String dataFrame,String columnName) {
        String insertSQL = "INSERT INTO [dbo].[MetaDataCatlog]\r\n"
        		+ "           ([Environment]\r\n"
        		+ "           ,[StorageAccount]\r\n"
        		+ "           ,[TableName]\r\n"
        		+ "           ,[SelectColumn]\r\n"
        		+ "           ,[DataFrame]\r\n"
        		+ "           ,[ColumnNames])\r\n"
        		+ "     VALUES\r\n"
        		+ "           (?,?,?,?,?,?)\r\n";

        try (PreparedStatement preparedStatement = connection.prepareStatement(insertSQL)) {
            preparedStatement.setString(1, environment);
            preparedStatement.setString(2, storageAccount);
            preparedStatement.setString(3, tableName);
            preparedStatement.setString(4, selectColumn);
            preparedStatement.setString(5, dataFrame);
            preparedStatement.setString(6, columnName);
            int rowsAffected = preparedStatement.executeUpdate();
            System.out.println("Inserted " + rowsAffected + " row(s) into " + tableName + ".");
        } catch (SQLException e) {
            e.printStackTrace();
        }
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
    
    // Function to create a map from entity names to their SQL Server data types
    private static Map<String, Map<String,String>> createSqlTypeMap(Root root) {
    	String sqlType =null;
    	HashMap<String,HashMap<String,String>> stringOutlier=convertStringOutlier();
    	Map<String,String> columns=null;
    	Map<String, Map<String,String>> entities = new HashMap<>();
    	
        for (Entity entity : root.getEntities()) {
        	String columnNames="";
        	String dataFrame="";
        	String selectQuery="";
        	columns=new HashMap<String,String>();
            for (Attribute attribute : entity.getAttributes()) {
            	String selectSubString="";
            	HashMap<String,String> outlierEntity=stringOutlier.get(entity.getName());
            	if(outlierEntity!=null) {
            		String outlierVal=outlierEntity.get(attribute.getName());
            		if(outlierVal!=null) {
            			sqlType = outlierVal;
            		}
            		else {
            			sqlType = convertToSqlType(attribute,offset,STRING_MAXLENGTH);
            		}
            	}
            	else {
                	sqlType = convertToSqlType(attribute,offset,STRING_MAXLENGTH);
            	}
            	
                if (sqlType != null) {
                	dataFrame= dataFrame+"["+attribute.getName() +"] "+ sqlType+",";
                	if(sqlType.startsWith("DATETIME2")) {
                		selectSubString="ISNULL("+entity.getName()+"."+"["+attribute.getName()+"],'1900-01-01')";
                	}
                	else if(sqlType.startsWith("NVARCHAR")) {
                		if(attribute.getName().equalsIgnoreCase("IsDelete")) {
                    		selectSubString="ISNULL("+entity.getName()+"."+"["+attribute.getName()+"],'0')";
                    	}
                		else {
                			selectSubString="ISNULL("+entity.getName()+"."+"["+attribute.getName()+"],'')";
                		}
                	}
                	else {
                		selectSubString=entity.getName()+"."+"["+attribute.getName() +"]";
                	}
                	
                	
                	selectQuery=selectQuery+selectSubString+",";
                	columnNames=columnNames+"["+attribute.getName() +"]"+",";
                }
            }
            dataFrame=dataFrame.substring(0, dataFrame.length()-1);
            selectQuery=selectQuery.substring(0, selectQuery.length()-1);
            columnNames=columnNames.substring(0, columnNames.length()-1);
            columns.put("dataFrame",dataFrame);
            columns.put("selectQuery",selectQuery);
            columns.put("columnNames",columnNames);
            entities.put(entity.getName(), columns);
        }

        return entities;
    }
    
    
    private static HashMap<String,HashMap<String,String>> convertStringOutlier() {

    	HashMap<String,HashMap<String,String>> map=new HashMap<String,HashMap<String,String>>(); 
    	Properties properties = new Properties();
    	try (InputStream input = new FileInputStream(STRING_OUTLIER_PATH)) {
            properties.load(input);
            Set<String> keys = properties.stringPropertyNames();
            for (String key : keys) {
            	HashMap<String,String> colmap=new HashMap<String,String>();
            	System.out.println(key);
                String value=properties.getProperty(key);
                System.out.println(value);
                String[] columns=value.split("\\|");
                
                for(String cols:columns) {
                	System.out.println(columns);
                	String[] colvalues=cols.split(",");
                	System.out.println(colvalues);
                	colmap.put(colvalues[0], colvalues[1]);
                }
                map.put(key, colmap);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return map;
    }
    // Function to convert Python data types to SQL Server data types
    private static String convertToSqlType(Attribute attribute,int offset,int maxlength) {
        switch (attribute.getDataType().toLowerCase()) {
            case "guid":
                return "UNIQUEIDENTIFIER";
            case "datetime":
                return "DATETIME2(7)";
            case "datetimeoffset":
                return "DATETIME2(7)";
            case "int64":
                return "BIGINT";
            case "decimal":
                // Assuming scale and precision are part of traits
                int precision = 36; // Default precision
                int scale = 6; // Default scale
                if (attribute.getTraits() != null) {
                    for (Trait trait : attribute.getTraits()) {
                        if ("is.dataFormat.numeric.shaped".equals(trait.getTraitReference())) {
                            for (TraitArgument arg : trait.getArguments()) {
                                if ("precision".equals(arg.getName())) {
                                    precision = Integer.parseInt(arg.getValue());
                                } else if ("scale".equals(arg.getName())) {
                                    scale = Integer.parseInt(arg.getValue());
                                }
                            }
                        }
                    }
                }
                return "NUMERIC(" + precision + ", " + scale + ")";
            case "string":
            	
            	if(attribute.getMaxLength()>maxlength) {
            		return "NVARCHAR(MAX)";
            	}
                return "NVARCHAR(" + (attribute.getMaxLength()+offset) + ")";
            case "boolean":
            	if(attribute.getName().equals("IsDelete")) {
            		return "NVARCHAR(10)";
            	}
                return "BIT";
            default:
                return null; // Unrecognized data type
        }
    }

    // Function to read JSON from a file
    private static String readJsonFromFile(String filePath) {
        try {
            return new String(Files.readAllBytes(Paths.get(filePath)));
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
    
    
}

package com.datapig.polybase;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;

public class PolybaseThread implements Runnable {

	// Database credentials
	private final String DB_URL;
	private final String USERNAME;
	private final String PASSWORD;
	private final String DATA_SOURCE;
	private final String schema;
	private final String tableName;
	private final String folder;

	public PolybaseThread(String DB_URL, String USERNAME, String PASSWORD, String DATA_SOURCE, String schema,
			String tableName, String folder) {
		this.DB_URL = DB_URL;
		this.USERNAME = USERNAME;
		this.PASSWORD = PASSWORD;
		this.DATA_SOURCE = DATA_SOURCE;
		this.schema = schema;
		this.tableName = tableName;
		this.folder = folder;
	}

	public void run() {
		// TODO Auto-generated method stub

		Connection connection = openConnection(DB_URL, USERNAME, PASSWORD);
		Map<String, String> schemaString = getMetaDataCatlog(connection, tableName);
		String dataFrame = schemaString.get("dataFrame");
		String selectColumn = schemaString.get("selectColumn");
		String columnNames = schemaString.get("columnNames");

		int errorFlag = 0;
		createStagingTable(connection, tableName, dataFrame);
		stageDatafromADLS(connection, DATA_SOURCE, folder, tableName, dataFrame, selectColumn);
		try {
			createMergeSql(connection, schema, tableName, columnNames);
			postMergeAction(connection, tableName, folder);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			errorFlag = errorFlag + 1;
			if (errorFlag < 2) {
				try {
					cleanupSourceTableForLatest(connection, tableName);
					createMergeSql(connection, schema, tableName, columnNames);
					postMergeAction(connection, tableName, folder);
				} catch (SQLException e1) {
					// TODO Auto-generated catch block
					postMergeActionOnFail(connection, tableName, folder);
					e1.printStackTrace();
				}
			}

			e.printStackTrace();
		}

		closeConnection(connection);
	}

	private static Map<String, String> getMetaDataCatlog(Connection connection, String TableName) {
		String query = "select TableName,DataFrame,SelectColumn,ColumnNames from MetaDataCatlog where TableName='"
				+ TableName + "'";
		Statement statement = null;
		ResultSet resultSet = null;
		Map<String, String> metaDataCatlog = new HashMap<String, String>();
		try {
			// Create a statement
			statement = connection.createStatement();
			// Execute the query
			resultSet = statement.executeQuery(query);

			// Process the result set
			while (resultSet.next()) {
				// Example: Assuming you have a column named "column_name"
				String tName = resultSet.getString("TableName");
				String dataFrame = resultSet.getString("DataFrame");
				String selectColumn = resultSet.getString("SelectColumn");
				String columnNames = resultSet.getString("ColumnNames");
				metaDataCatlog.put("tName", tName);
				metaDataCatlog.put("dataFrame", dataFrame);
				metaDataCatlog.put("selectColumn", selectColumn);
				metaDataCatlog.put("columnNames", columnNames);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
		return metaDataCatlog;
	}

	private static void stageDatafromADLS(Connection connection, String dataSource, String folder, String tableName,
			String dataFrame, String selectCoulmn) {
		String query = "INSERT INTO dbo._staging_" + tableName + "\r\n" + "SELECT \r\n" + selectCoulmn + "\r\n"
				+ "FROM OPENROWSET(\r\n" + "BULK '/" + folder + "/" + tableName + "/*.csv',\r\n" + "FORMAT = 'CSV',\r\n"
				+ "DATA_SOURCE = '" + dataSource + "')\r\n" + "WITH\r\n" + "(" + dataFrame + ") as " + tableName;
		System.out.println(query);
		Statement statement;
		try {
			statement = connection.createStatement();
			int rowsAffected = statement.executeUpdate(query);
			System.out.println("Staged for table " + tableName + " : " + rowsAffected);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void createMergeSql(Connection connection, String schema, String tableName, String columnNames)
			throws SQLException {
		String[] coulmns = columnNames.split(",");

		String updateStatements = "";
		String insertColumns = columnNames;
		String valuesColumns = "";

		if (coulmns != null) {
			for (String col : coulmns) {
				updateStatements = updateStatements + "target." + col + " = source." + col + ",";
				valuesColumns = valuesColumns + "source." + col + ",";
			}
			updateStatements = updateStatements.substring(0, updateStatements.length() - 1);
			valuesColumns = valuesColumns.substring(0, valuesColumns.length() - 1);
		}

		String mergeQuery = "IF EXISTS (SELECT * FROM sys.objects WHERE object_id = OBJECT_ID(N'[" + schema
				+ "].[_staging_" + tableName + "]') AND type in (N'U')) " + "BEGIN "
				+ "PRINT('-- Merge data from _staging_" + tableName + " to " + tableName + "----') "
				+ "SET NOCOUNT OFF; " + "DELETE target FROM " + schema + "." + tableName + " AS target " + "INNER JOIN "
				+ schema + "._staging_" + tableName
				+ " AS source ON target.id = source.id AND source.isDelete IN ('True', '1'); " + "DELETE FROM " + schema
				+ "._staging_" + tableName + " WHERE IsDelete IN ('True', '1'); " + "MERGE INTO " + schema + "."
				+ tableName + " AS target " + "USING " + schema + "._staging_" + tableName + " AS source "
				+ "ON target.Id = source.Id " + "WHEN MATCHED AND (target.versionnumber < source.versionnumber) THEN "
				+ "UPDATE SET " + updateStatements + " " + "WHEN NOT MATCHED BY TARGET THEN " + "INSERT ("
				+ insertColumns + ") " + "VALUES (" + valuesColumns + "); " + "END";

		System.out.println(mergeQuery);

		// Execute the dynamic SQL
		Statement statement = connection.createStatement();
		statement.execute(mergeQuery); // Use execute for DML operations
		System.out.println("Executed SQL: " + mergeQuery);
	}

	private static void cleanupSourceTableForLatest(Connection connection, String tableName) {
		String query = "SELECT Id,max(versionnumber) versionnumber FROM dbo." + tableName + " \r\n"
				+ "WHERE IsDelete NOT IN ('1','True')\r\n" + "GROUP BY Id\r\n" + "HAVING count(Id) > 1";
		Statement statement = null;
		ResultSet resultSet = null;

		try {
			// Create a statement
			statement = connection.createStatement();
			// Execute the query
			resultSet = statement.executeQuery(query);

			// Process the result set
			while (resultSet.next()) {
				// Example: Assuming you have a column named "column_name"
				String Id = resultSet.getString("Id");
				String versionnumber = resultSet.getString("versionnumber");
				String sql = "DELETE FROM dbo._" + tableName + "  WHERE Id='" + Id + "' and versionnumber < '"
						+ versionnumber + "'";
				Statement statement2 = connection.createStatement();
				int rowsDeleted = statement2.executeUpdate(sql);
				System.out.println("Number of rows deleted :" + rowsDeleted);
			}

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void createStagingTable(Connection connection, String tableName, String dataFrame) {
		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " + "BEGIN " + "DROP TABLE dbo._staging_" + tableName + "\r\n" + " END";

		String createTableSQL = "IF NOT EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " + "BEGIN " + "CREATE TABLE dbo._staging_" + tableName + "(" + dataFrame + ") "
				+ "\r\n" + "END";

		try (Statement statement = connection.createStatement()) {
			System.out.println(dropTableSQL);
			statement.executeUpdate(dropTableSQL);
			System.out.println(createTableSQL);
			statement.executeUpdate(createTableSQL);

			System.out.println("Checked and created table dbo._staging_" + tableName);

		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private static void postMergeActionOnFail(Connection connection, String tableName, String folder) {
		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " + "BEGIN " + "DROP TABLE dbo._staging_" + tableName + "\r\n" + " END";
		try {
			Statement statement = connection.createStatement();
			System.out.println(dropTableSQL);
			statement.executeUpdate(dropTableSQL);

			String sql = "UPDATE MetaDataCatlog SET LastEndCopyDate=?,LastCopyStatus=?,LastUpdatedFolder=? WHERE TableName=?";
			// Prepare the statement
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			// Set the parameters
			preparedStatement.setTimestamp(1, null);
			preparedStatement.setInt(2, 3);
			preparedStatement.setString(3, folder);
			preparedStatement.setString(4, tableName);
			// Execute the update
			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Updated MetaDataCatlog " + rowsAffected + " row(s).");
			preparedStatement.close();

			String sql2 = "UPDATE FolderSyncStatus SET copystatus=? WHERE tablename=? "
					+ "and folder in (SELECT foldername FROM ChangeLog WHERE stagetime >=(SELECT stagetime FROM ChangeLog where foldername=?))";
			// Prepare the statement
			PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

			// Set the parameters
			preparedStatement2.setInt(1, 2);
			preparedStatement2.setString(2, tableName);
			preparedStatement2.setString(3, folder);
			// Execute the update
			int rowsAffected2 = preparedStatement2.executeUpdate();
			System.out.println("Updated FolderSyncStatus" + rowsAffected2 + " row(s).");
			preparedStatement2.close();

		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static void postMergeAction(Connection connection, String tableName, String folder) {

		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " + "BEGIN " + "DROP TABLE dbo._staging_" + tableName + "\r\n" + " END";
		try {
			
			Statement statement = connection.createStatement();
			System.out.println(dropTableSQL);
			statement.executeUpdate(dropTableSQL);

			Timestamp timestamp = new Timestamp(System.currentTimeMillis());
			String sql = "UPDATE MetaDataCatlog SET LastEndCopyDate=?,LastCopyStatus=?,LastUpdatedFolder=? WHERE TableName=?";
			// Prepare the statement
			PreparedStatement preparedStatement = connection.prepareStatement(sql);

			// Set the parameters
			preparedStatement.setTimestamp(1, timestamp);
			preparedStatement.setInt(2, 2);
			preparedStatement.setString(3, folder);
			preparedStatement.setString(4, tableName);
			// Execute the update
			int rowsAffected = preparedStatement.executeUpdate();
			System.out.println("Updated MetaDataCatlog " + rowsAffected + " row(s).");
			preparedStatement.close();

			String sql2 = "UPDATE FolderSyncStatus SET copystatus=? WHERE folder=? and tablename=?";
			// Prepare the statement
			PreparedStatement preparedStatement2 = connection.prepareStatement(sql2);

			// Set the parameters
			preparedStatement2.setInt(1, 1);
			preparedStatement2.setString(2, folder);
			preparedStatement2.setString(3, tableName);
			// Execute the update
			int rowsAffected2 = preparedStatement2.executeUpdate();
			System.out.println("Updated FolderSyncStatus " + rowsAffected2 + " row(s).");
			preparedStatement2.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private static Connection openConnection(String DB_URL, String USERNAME, String PASSWORD) {
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


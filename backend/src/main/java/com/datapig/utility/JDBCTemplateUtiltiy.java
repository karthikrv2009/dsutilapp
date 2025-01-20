package com.datapig.utility;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.component.DynamicDataSourceManager;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.FaultEntity;
import com.datapig.service.DatabaseConfigService;
import com.datapig.service.FaultEntityService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.slf4j.Logger;

@Service
public class JDBCTemplateUtiltiy {

	private static final Logger logger = LoggerFactory.getLogger(JDBCTemplateUtiltiy.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Autowired
	private DynamicDataSourceManager dynamicDataSourceManager;

	@Autowired
	private DatabaseConfigService databaseConfigService;

	@Autowired
    private FaultEntityService faultEntityService;

	public JdbcTemplate getJdbcTemplate(String dbIdentifier) {
		// Get the DataSource from DynamicDataSourceManager
		DataSource dataSource = dynamicDataSourceManager.getDataSource(dbIdentifier);
		if(dataSource==null){
			DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
			dynamicDataSourceManager.addDataSource(dbIdentifier, databaseConfig.getUrl(), databaseConfig.getUsername(), databaseConfig.getPassword());
			dataSource = dynamicDataSourceManager.getDataSource(dbIdentifier);
		}

		
		// Create and return a new JdbcTemplate based on the DataSource
		return new JdbcTemplate(dataSource);
	}

	public Set<String> getTableInFolder(String folderName, String DATA_SOURCE,String dbIdentifier) {
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		Set<String> tables = new LinkedHashSet<>();
		List<String> tableNames = null;
		try {
			String sql = "SELECT tablename\r\n"
					+ "			FROM\r\n"
					+ "				OPENROWSET(\r\n"
					+ "					BULK '/" + folderName + "/model.json',\r\n"
					+ "					DATA_SOURCE = '" + DATA_SOURCE + "',\r\n"
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

			// String sql = "SELECT table_name FROM information_schema.tables WHERE
			// table_schema = ?";
			logger.debug("Executing SQL query: {}", sql);
			tableNames = jdbcTemplate.query(sql, (rs, rowNum) -> rs.getString("tablename"));

			logger.info("Tables retrieved for folder {}: {}", folderName, tables);
		} catch (Exception e) {
			logger.error("An error occurred while retrieving tables for folder {}: {}", folderName, e.getMessage(), e);
		}
		return new LinkedHashSet<>(tableNames);
	}

	
	public void dropStagingTable(String tableName, String dbIdentifier) {
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " +
				"BEGIN " +
				"DROP TABLE dbo._staging_" + tableName + " " +
				"END";

		jdbcTemplate.execute(dropTableSQL);

	}

	public Integer getRowCountByTableName(String tableName,String dbIdentifier){
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String rowCountQuery="SELECT SUM(p.rows) FROM sys.partitions p " +
                        "JOIN sys.tables t ON p.object_id = t.object_id " +
                        "WHERE t.name = ? AND t.is_ms_shipped = 0 AND p.index_id IN (0, 1)";
		
		Integer count=jdbcTemplate.queryForObject(rowCountQuery, new Object[] { tableName }, Integer.class);
				
		return count;
	}
	
	public boolean createTableIfNotExists(String tableName, String dataFrame, String dbIdentifier) {
	    boolean flag=false;
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
		String createTableSQL = "CREATE TABLE dbo." + tableName + " (" + dataFrame + ")";
	try{
		// Check if the table exists
		Integer tableCount = jdbcTemplate.queryForObject(checkTableSQL, new Object[] { tableName }, Integer.class);

		if (tableCount != null && tableCount == 0) {
			// Create the table if it does not exist
			jdbcTemplate.execute(createTableSQL);
			logger.info("Table created: {}", tableName);

			// Create indexes
			createIndexes(tableName, dbIdentifier);
		} else {
			logger.info("Table already exists: {}", tableName);
		}
		flag=true;
	}catch(Exception e){
		flag=false;
		FaultEntity faultEntity=new FaultEntity();
		faultEntity.setDbIdentifier(dbIdentifier);
		faultEntity.setTableName(tableName);
		String errorMsg= getMainCauseMessage(e,createTableSQL);
		faultEntity.setErrorMsg(errorMsg);
		faultEntityService.save(faultEntity);
	}
	return flag;
	}

    public static String getMainCauseMessage(Throwable e, String query) {
        // Navigate to the root cause
        Throwable cause = e;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
    
        // Combine the cause and the query
        return cause.getMessage() + "\n\nQuery: " + query;
    }

	private void createIndexes(String tableName, String dbIdentifier) {
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String createIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_Id_idx ON dbo." + tableName
				+ "(Id) WITH (ONLINE=ON)";
		/*String createRecIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_RecId_idx ON dbo." + tableName
				+ "(recid) WITH (ONLINE=ON)";*/
		String createVersionNumberIndexSQL = "CREATE INDEX dbo_" + tableName + "_versionnumber_idx ON dbo." + tableName
				+ "(versionnumber) WITH (ONLINE=ON)";

		jdbcTemplate.execute(createIdIndexSQL);
		//jdbcTemplate.execute(createRecIdIndexSQL);
		jdbcTemplate.execute(createVersionNumberIndexSQL);

		logger.info("Indexes created for table: {}", tableName);
	}
}

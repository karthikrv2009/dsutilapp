package com.datapig.utility;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.component.DynamicDataSourceManager;

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

	public JdbcTemplate getJdbcTemplate(String dbIdentifier) {
		// Get the DataSource from DynamicDataSourceManager
		DataSource dataSource = dynamicDataSourceManager.getDataSource(dbIdentifier);
		// Create and return a new JdbcTemplate based on the DataSource
		return new JdbcTemplate(dataSource);
	}

	public Set<String> getTableInFolder(String folderName, String DATA_SOURCE) {
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

	public void createTableIfNotExists(String tableName, String dataFrame, String dbIdentifier) {
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
		String createTableSQL = "CREATE TABLE dbo." + tableName + " (" + dataFrame + ")";

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
	}

	private void createIndexes(String tableName, String dbIdentifier) {
		jdbcTemplate = getJdbcTemplate(dbIdentifier);
		String createIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_Id_idx ON dbo." + tableName
				+ "(Id) WITH (ONLINE=ON)";
		String createRecIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_RecId_idx ON dbo." + tableName
				+ "(recid) WITH (ONLINE=ON)";
		String createVersionNumberIndexSQL = "CREATE INDEX dbo_" + tableName + "_versionnumber_idx ON dbo." + tableName
				+ "(versionnumber) WITH (ONLINE=ON)";

		jdbcTemplate.execute(createIdIndexSQL);
		jdbcTemplate.execute(createRecIdIndexSQL);
		jdbcTemplate.execute(createVersionNumberIndexSQL);

		logger.info("Indexes created for table: {}", tableName);
	}
}

package com.datapig.utility;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import com.datapig.service.SynapseLogParserService;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class JDBCTemplateUtiltiy {

	private static final Logger logger = LoggerFactory.getLogger(JDBCTemplateUtiltiy.class);

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Set<String> getTableInFolder(String folderName, String DATA_SOURCE) {
		Set<String> tables = new LinkedHashSet<>();
		try {
			String sql = "SELECT table_name FROM information_schema.tables WHERE table_schema = ?";
			logger.debug("Executing SQL query: {}", sql);
			List<String> tableList = jdbcTemplate.queryForList(sql, new Object[] { folderName }, String.class);
			tables.addAll(tableList);
			logger.info("Tables retrieved for folder {}: {}", folderName, tables);
		} catch (Exception e) {
			logger.error("An error occurred while retrieving tables for folder {}: {}", folderName, e.getMessage(), e);
		}
		return tables;
	}

	public void dropStagingTable(String tableName) {
		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_"
				+ tableName + "') " +
				"BEGIN " +
				"DROP TABLE dbo._staging_" + tableName + " " +
				"END";

		jdbcTemplate.execute(dropTableSQL);

	}

	public void createTableIfNotExists(String tableName, String dataFrame) {
		String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
		String createTableSQL = "CREATE TABLE dbo." + tableName + " (" + dataFrame + ")";

		// Check if the table exists
		Integer tableCount = jdbcTemplate.queryForObject(checkTableSQL, new Object[] { tableName }, Integer.class);

		if (tableCount != null && tableCount == 0) {
			// Create the table if it does not exist
			jdbcTemplate.execute(createTableSQL);
			logger.info("Created table", tableName);

			// Create indexes
			createIndexes(tableName);
		} else {
			logger.info("Table already exist ", tableName);
		}
	}

	private void createIndexes(String tableName) {
		String createIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_Id_idx ON dbo." + tableName
				+ "(Id) WITH (ONLINE=ON)";
		String createRecIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_RecId_idx ON dbo." + tableName
				+ "(recid) WITH (ONLINE=ON)";
		String createVersionNumberIndexSQL = "CREATE INDEX dbo_" + tableName + "_versionnumber_idx ON dbo." + tableName
				+ "(versionnumber) WITH (ONLINE=ON)";

		jdbcTemplate.execute(createIdIndexSQL);
		jdbcTemplate.execute(createRecIdIndexSQL);
		jdbcTemplate.execute(createVersionNumberIndexSQL);

		logger.info("Created indexes for table ", tableName);
	}
}

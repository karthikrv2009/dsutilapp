package com.datapig.utility;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Service
public class JDBCTemplateUtiltiy {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	public Set<String> getTableInFolder(String folderName, String DATA_SOURCE) {
		String query = "SELECT tablename\r\n"
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

		List<String> tableNames = jdbcTemplate.query(query, (rs, rowNum) -> rs.getString("tablename"));
		return new LinkedHashSet<>(tableNames);
	}

	public void dropStagingTable(String tableName){
		String dropTableSQL = "IF EXISTS (SELECT * FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = '_staging_" + tableName + "') " +
		"BEGIN " +
		"DROP TABLE dbo._staging_" + tableName + " " +
		"END";

		jdbcTemplate.execute(dropTableSQL);

	}

    public void createTableIfNotExists(String tableName, String dataFrame) {
        String checkTableSQL = "SELECT COUNT(*) FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_NAME = ?";
        String createTableSQL = "CREATE TABLE dbo." + tableName + " (" + dataFrame + ")";

        // Check if the table exists
        Integer tableCount = jdbcTemplate.queryForObject(checkTableSQL, new Object[]{tableName}, Integer.class);
        
        if (tableCount != null && tableCount == 0) {
            // Create the table if it does not exist
            jdbcTemplate.execute(createTableSQL);
            System.out.println("Created table " + tableName);

            // Create indexes
            createIndexes(tableName);
        } else {
            System.out.println("Table " + tableName + " already exists.");
        }
    }

    private void createIndexes(String tableName) {
        String createIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_Id_idx ON dbo." + tableName + "(Id) WITH (ONLINE=ON)";
        String createRecIdIndexSQL = "CREATE UNIQUE INDEX dbo_" + tableName + "_RecId_idx ON dbo." + tableName + "(recid) WITH (ONLINE=ON)";
        String createVersionNumberIndexSQL = "CREATE INDEX dbo_" + tableName + "_versionnumber_idx ON dbo." + tableName + "(versionnumber) WITH (ONLINE=ON)";

        jdbcTemplate.execute(createIdIndexSQL);
        jdbcTemplate.execute(createRecIdIndexSQL);
        jdbcTemplate.execute(createVersionNumberIndexSQL);

        System.out.println("Created indexes for table " + tableName);
    }
}

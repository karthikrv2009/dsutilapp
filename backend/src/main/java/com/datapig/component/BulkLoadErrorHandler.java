package com.datapig.component;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.datapig.entity.HealthMetrics;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.service.MetaDataCatlogService;
import com.datapig.utility.JDBCTemplateUtiltiy;

@Component
public class BulkLoadErrorHandler {

    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private JDBCTemplateUtiltiy jdbcTemplateUtiltiy;

    public static boolean containsTruncationError(String errorMessage) {
        if(errorMessage.contains("Bulk load data conversion error (truncation)")) {
        	return errorMessage.contains("Bulk load data conversion error (truncation)");
        }
        else if(errorMessage.contains("Bulk load data conversion error (type mismatch or invalid character for the specified codepage)")) {
        	return errorMessage.contains("Bulk load data conversion error (type mismatch or invalid character for the specified codepage)");
        }
    	return false;
    }
    
    public String extractColumnName(String errorMessage) {

        // Regex to extract the column name after "column <number> ("
        Pattern pattern = Pattern.compile("column\\s+\\d+\\s+\\(([^)]+)\\)");
        Matcher matcher = pattern.matcher(errorMessage);

        if (matcher.find()) {
            String columnName = matcher.group(1); // Column name is inside the parentheses
            System.out.println("Detected truncation issue in column: " + columnName);
            return columnName;
        } else {
            System.out.println("Column name could not be extracted.");
        }
        return null; // Return null if no match is found
    }

    public String handleTruncationError(String errorMessage) {
        String columnName = null;
        if (containsTruncationError(errorMessage)) {
            columnName = extractColumnName(errorMessage);
            if (columnName != null) {
                System.out.println("Detected truncation issue in column: " + columnName);
                alterSchemaForColumn(columnName);
                return columnName;
            } else {
                System.out.println("Column name could not be extracted from the error message.");
            }
        } else {
            System.out.println("No truncation error found in the error message.");
        }
        return null;
    }

    public void alterSchemaForColumn(String columnName) {
        System.out.println("Altering schema for column: " + columnName);
        // Add logic to modify the schema dynamically
    }

    public void fixSchemaTruncation(String errorMessage) {

        handleTruncationError(errorMessage);
    }

    public void fixTruncateError(HealthMetrics healthMetrics) {

        if (healthMetrics != null) {
            String errorMessage = healthMetrics.getErrorMsg();
            MetaDataCatlog metaDataCatlog = metaDataCatlogService.getMetaDataCatlogByTableNameAndDbIdentifier(
                    healthMetrics.getTableName(), healthMetrics.getDbIdentifier());
            String originalDataFrame = metaDataCatlog.getDataFrame();
            String tableName = metaDataCatlog.getTableName();
            String columnname = handleTruncationError(errorMessage);
            String updatedDataFrame = updateNVarcharLengthForColumn(originalDataFrame, columnname, tableName,
                    metaDataCatlog.getDbIdentifier());
            if (updatedDataFrame != null) {
                metaDataCatlog.setDataFrame(updatedDataFrame);
                metaDataCatlogService.save(metaDataCatlog);
            // Output the updated DataFrame string
            System.out.println(updatedDataFrame);
            }

        }

    }

    private String updateNVarcharLengthForColumn(String dataFrame, String columnName, String tableName,
            String db_identifier) {
        // Regular expression to match NVARCHAR column with specified column name and
        // extract the length
        String regex = "(\\[" + columnName + "\\]) NVARCHAR\\((\\d+)\\)";

        // Create a Pattern object
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(dataFrame);

        // StringBuilder to build the updated DataFrame string
        StringBuilder updatedDataFrame = new StringBuilder(dataFrame);
        String finalDataFrame=null;
        // If the column is found, update the NVARCHAR length
        if (matcher.find()) {
            // Extract the current length
            int currentLength = Integer.parseInt(matcher.group(2));

            // Calculate the new length
            int newLength = currentLength + 100;

            // Replace the old NVARCHAR definition with the new one
            String updatedColumn = matcher.group(1) + " NVARCHAR(" + newLength + ")";
            int start = matcher.start();
            int end = matcher.end();
            if (jdbcTemplateUtiltiy.alterTable(db_identifier, tableName, updatedColumn)) {
                updatedDataFrame.replace(start, end, updatedColumn);
                finalDataFrame=updatedDataFrame.toString();
            }
            else{
                finalDataFrame=null;
            }

        }
        // Return the updated DataFrame string
        return finalDataFrame;
    }

}

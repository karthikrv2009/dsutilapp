package com.datapig.service;

import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.util.Properties;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.datapig.service.dto.ModelRoot;
import com.datapig.service.dto.ModelEntity;
import com.datapig.entity.ChangeDataTrackingCatalog;
import com.datapig.entity.DatabaseConfig;
import com.datapig.entity.EnvironmentConfig;
import com.datapig.entity.MetaDataCatlog;
import com.datapig.service.dto.ModelAttribute;
import com.datapig.service.dto.ModelTrait;
import com.datapig.service.dto.ModelTraitArgument;
import com.datapig.utility.JDBCTemplateUtiltiy;
import com.datapig.service.dto.ModelTable;
import com.datapig.service.dto.ModelTableAttributes;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ParseModelJson {


    @Autowired
    private MetaDataCatlogService metaDataCatlogService;

    @Autowired
    private JDBCTemplateUtiltiy jDBCTemplateUtiltiy;

    @Autowired
    private DatabaseConfigService databaseConfigService;

    @Autowired
    private EnvironmentConfigService environmentConfigService;

    @Autowired
    private ChangeDataTrackingCatalogService changeDataTrackingCatalogService;

    private static final Logger logger = LoggerFactory.getLogger(ParseModelJson.class);

    public List<ModelTable> parseModelJson(String dbIdentifier,boolean forCDC) {
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        EnvironmentConfig environmentConfig=environmentConfigService.getEnvironmentConfig();

        String modelJSONPath = databaseConfig.getLocalCdmFilePath();
        File file = new File(modelJSONPath);
        StringBuilder content = new StringBuilder(); // To accumulate the file content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // Append each line with a newline
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        // Convert StringBuilder to a String
        String modelJson = content.toString();

        // Print the file content as a single string
        System.out.println(modelJson);

        Gson gson = new Gson();
        ModelRoot root = gson.fromJson(modelJson, ModelRoot.class);

        List<ModelTable> resultTable = new ArrayList<>();
        ModelTable iter = null;
        for (ModelEntity entity : root.getEntities()) {
            for (ModelAttribute attribute : entity.getAttributes()) {
                for (ModelTable tbl : resultTable) {
                    if (tbl.getTableName().equals(entity.getName())) {
                        iter = tbl;
                    }
                }
                if (attribute.getName() != null) {
                    if (iter != null) {
                        List<ModelTableAttributes> attributes = iter.getAttributes();
                        ModelTableAttributes attr = new ModelTableAttributes();
                        attr.setAttributeName(attribute.getName());
                        attr.setDataType(attribute.getDataType());
                        attributes.add(attr);
                    } else {
                        ModelTable table = new ModelTable();
                        table.setTableName(entity.getName());
                        List<ModelTableAttributes> attributes = new ArrayList<ModelTableAttributes>();
                        ModelTableAttributes attr = new ModelTableAttributes();
                        attr.setAttributeName(attribute.getName());
                        attr.setDataType(attribute.getDataType());
                        attributes.add(attr);
                        table.setAttributes(attributes);
                        resultTable.add(table);
                    }
                }
                iter = null;
            }
        }

        // Output some parsed data
        logger.info("Name: " + root.getName());
        logger.info("Description: " + root.getDescription());
        logger.info("Version: " + root.getVersion());

        Map<String, Map<String, String>> sqlTypeMap = createSqlTypeMap(root,environmentConfig);
        List<MetaDataCatlog> existingMetaDataCatlog = metaDataCatlogService.findAllByDbIdentifier(dbIdentifier);
        Set<String> existingTables = new HashSet<String>();

        for (MetaDataCatlog catalog : existingMetaDataCatlog) {
            existingTables.add(catalog.getTableName());
        }

        Set<String> tablenames = sqlTypeMap.keySet();
        for (String tablename : tablenames) {
            if (!existingTables.contains(tablename)) {
                Map<String, String> values = sqlTypeMap.get(tablename);
                String dataFrame = values.get("dataFrame");
                String selectQuery = values.get("selectQuery");
                String columnNames = values.get("columnNames");
                String selectDataFrame=values.get("selectDataFrame");
                boolean flag=jDBCTemplateUtiltiy.createTableIfNotExists(tablename, dataFrame, dbIdentifier);
                if(flag){
                    loadMetaDataCatlog(tablename, selectQuery, dataFrame, columnNames, selectDataFrame,dbIdentifier);
                }
            }
        }
        return resultTable;
    }

            // Method to create a subdirectory 'cdc' and update the file path
    public static String createSubDirectoryAndUpdatePath(String filePath) {
            // Convert the input file path to a Path object
            java.nio.file.Path path = Paths.get(filePath);
    
            // Get the parent directory of the file
            java.nio.file.Path parentPath = path.getParent();
    
            // Create the 'cdc' subdirectory within the parent folder
            if (parentPath != null) {
                File cdcDir = new File(parentPath.toString(), "cdc");
                if (!cdcDir.exists()) {
                    boolean created = cdcDir.mkdirs(); // Create the 'cdc' directory if it doesn't exist
                    if (created) {
                        System.out.println("Created 'cdc' subdirectory: " + cdcDir.getPath());
                    } else {
                        System.out.println("'cdc' directory already exists.");
                    }
                }
    
                // Construct the new file path with 'cdc' as a subdirectory
                
                return filePath;
            }
            
            return null;
        }

    public List<ModelTable> parseCdcModelJson(String dbIdentifier,String cdcTableName,String tableName) {
        DatabaseConfig databaseConfig=databaseConfigService.getDatabaseConfigByIdentifier(dbIdentifier);
        EnvironmentConfig environmentConfig=environmentConfigService.getEnvironmentConfig();

        String modelCdcJSONPath = createSubDirectoryAndUpdatePath(databaseConfig.getLocalCdmFilePath());
        File file = new File(modelCdcJSONPath);
        StringBuilder content = new StringBuilder(); // To accumulate the file content
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(file)))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n"); // Append each line with a newline
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        // Convert StringBuilder to a String
        String modelJson = content.toString();

        // Print the file content as a single string
        System.out.println(modelJson);

        Gson gson = new Gson();
        ModelRoot root = gson.fromJson(modelJson, ModelRoot.class);

        List<ModelTable> resultTable = new ArrayList<>();
        ModelTable iter = null;
        for (ModelEntity entity : root.getEntities()) {
            
            for (ModelAttribute attribute : entity.getAttributes()) {
                for (ModelTable tbl : resultTable) {
                    if (tbl.getTableName().equals(entity.getName())) {
                        iter = tbl;
                    }
                }
                if (attribute.getName() != null) {
                    if (iter != null) {
                        List<ModelTableAttributes> attributes = iter.getAttributes();
                        ModelTableAttributes attr = new ModelTableAttributes();
                        attr.setAttributeName(attribute.getName());
                        attr.setDataType(attribute.getDataType());
                        attributes.add(attr);
                    } else {
                        ModelTable table = new ModelTable();
                        table.setTableName(entity.getName());
                        List<ModelTableAttributes> attributes = new ArrayList<ModelTableAttributes>();
                        ModelTableAttributes attr = new ModelTableAttributes();
                        attr.setAttributeName(attribute.getName());
                        attr.setDataType(attribute.getDataType());
                        attributes.add(attr);
                        table.setAttributes(attributes);
                        resultTable.add(table);
                    }
                }
                iter = null;
            }
        }

        // Output some parsed data
        logger.info("Name: " + root.getName());
        logger.info("Description: " + root.getDescription());
        logger.info("Version: " + root.getVersion());

        Map<String, Map<String, String>> sqlTypeMap = createSqlTypeMap(root,environmentConfig);

        Set<String> tablenames = sqlTypeMap.keySet();
        for (String tablename : tablenames) {
            if (tableName.equalsIgnoreCase(tablename)) {
                Map<String, String> values = sqlTypeMap.get(tablename);
                String dataFrame = values.get("dataFrame");
                String selectQuery = values.get("selectQuery");
                String columnNames = values.get("columnNames");
                String selectDataFrame=values.get("selectDataFrame");
                boolean flag=jDBCTemplateUtiltiy.createCdcTableIfNotExists(cdcTableName, dataFrame, dbIdentifier);
                if(flag){
                    loadChangeDataTrackingCatalog(cdcTableName, selectQuery, dataFrame, columnNames,selectDataFrame, dbIdentifier);
                }
            }
        }
        return resultTable;
    }

    private MetaDataCatlog loadMetaDataCatlog(String tablename, String selectQuery, String dataFrame,
            String columnNames, String selectDataFrame,String dbIdentifier) {
        MetaDataCatlog catalog = new MetaDataCatlog();
        catalog.setTableName(tablename);
        catalog.setColumnNames(columnNames);
        catalog.setDataFrame(dataFrame);
        catalog.setSelectColumn(selectQuery);
        catalog.setSelectDataFrame(selectDataFrame);
        catalog.setDbIdentifier(dbIdentifier);
        catalog = metaDataCatlogService.save(catalog);
        return catalog;
    }

    private ChangeDataTrackingCatalog loadChangeDataTrackingCatalog(String tablename, String selectQuery, String dataFrame,
            String columnNames,String selectDataFrame, String dbIdentifier) {
        ChangeDataTrackingCatalog catalog = new ChangeDataTrackingCatalog();
        catalog.setCdcTableName(tablename);
        catalog.setColumnNames(columnNames);
        catalog.setDataFrame(dataFrame);
        catalog.setSelectColumn(selectQuery);
        catalog.setSelectDataFrame(selectDataFrame);
        catalog.setDbIdentifier(dbIdentifier);

        catalog = changeDataTrackingCatalogService.save(catalog);
        return catalog;
    }

    private HashMap<String, HashMap<String, String>> convertStringOutlier(EnvironmentConfig environmentConfig) {

        HashMap<String, HashMap<String, String>> map = new HashMap<String, HashMap<String, String>>();

        try (InputStream input = new FileInputStream(environmentConfig.getStringOutlierPath())) {
            Properties properties = new Properties();
            properties.load(input);
            Set<String> keys = properties.stringPropertyNames();
            for (String key : keys) {
                HashMap<String, String> colmap = new HashMap<String, String>();
                System.out.println(key);
                String value = properties.getProperty(key);
                System.out.println(value);
                String[] columns = value.split("\\|");

                for (String cols : columns) {
                    System.out.println(columns);
                    String[] colvalues = cols.split(",");
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

    // Function to create a map from entity names to their SQL Server data types
    private Map<String, Map<String, String>> createSqlTypeMap(ModelRoot root,EnvironmentConfig environmentConfig) {
        String sqlType = null;
        HashMap<String, HashMap<String, String>> stringOutlier = convertStringOutlier(environmentConfig);
        Map<String, String> columns = null;
        Map<String, Map<String, String>> entities = new HashMap<>();
        int offset = Integer.parseInt(environmentConfig.getStringOffSet());
        int STRING_MAXLENGTH = environmentConfig.getMaxLength();
        for (ModelEntity entity : root.getEntities()) {
            String columnNames = "";
            String dataFrame = "";
            String selectQuery = "";
            String selectDataFrame="";
            columns = new HashMap<String, String>();
            for (ModelAttribute attribute : entity.getAttributes()) {
                String selectSubString = "";
                HashMap<String, String> outlierEntity = stringOutlier.get(entity.getName());
                if (outlierEntity != null) {
                    String outlierVal = outlierEntity.get(attribute.getName());
                    if (outlierVal != null) {
                        sqlType = outlierVal;
                    } else {
                        sqlType = convertToSqlType(attribute, offset, STRING_MAXLENGTH);
                    }
                } else {
                    sqlType = convertToSqlType(attribute, offset, STRING_MAXLENGTH);
                }

                if (sqlType != null) {
                    dataFrame = dataFrame + "[" + attribute.getName() + "] " + sqlType + ",";
                    if(!sqlType.equalsIgnoreCase("BIT")){
                        selectDataFrame = selectDataFrame + "[" + attribute.getName() + "] " + sqlType + ",";
                    }
                    if (sqlType.startsWith("DATETIME2")) {
                        selectSubString = "ISNULL(" + entity.getName() + "." + "[" + attribute.getName()
                                + "],'1900-01-01')";
                    } else if (sqlType.startsWith("NVARCHAR")) {
                            selectSubString = "ISNULL(" + entity.getName() + "." + "[" + attribute.getName() + "],'')";
                    }else if(sqlType.startsWith("BIT")){
                        selectSubString="CASE WHEN ((LOWER("+ entity.getName() + "." + "[" + attribute.getName() +"])='true') OR (LOWER("+ entity.getName() + "." + "[" + attribute.getName() +"])='1'))  THEN 1 ELSE 0 END";
                        selectDataFrame = selectDataFrame + "[" + attribute.getName() + "] NVARCHAR(10),";
                    } 
                    else {
                        selectSubString = entity.getName() + "." + "[" + attribute.getName() + "]";
                    }

                    selectQuery = selectQuery + selectSubString + ",";
                    columnNames = columnNames + "[" + attribute.getName() + "]" + ",";
                }
            }
            dataFrame = dataFrame.substring(0, dataFrame.length() - 1);
            selectDataFrame = selectDataFrame.substring(0, selectDataFrame.length() - 1);
            selectQuery = selectQuery.substring(0, selectQuery.length() - 1);
            columnNames = columnNames.substring(0, columnNames.length() - 1);
            columns.put("dataFrame", dataFrame);
            columns.put("selectDataFrame", selectDataFrame);
            columns.put("selectQuery", selectQuery);
            columns.put("columnNames", columnNames);
            entities.put(entity.getName(), columns);
        }

        return entities;
    }

    // Function to convert Python data types to SQL Server data types
    private String convertToSqlType(ModelAttribute attribute, int offset, int maxlength) {
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
                    for (ModelTrait trait : attribute.getTraits()) {
                        if ("is.dataFormat.numeric.shaped".equals(trait.getTraitReference())) {
                            for (ModelTraitArgument arg : trait.getArguments()) {
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

                if (attribute.getMaxLength() > maxlength) {
                    return "NVARCHAR(MAX)";
                }
                int len=100;
                if(attribute.getMaxLength()>0){
                    len=attribute.getMaxLength();
                }                
                return "NVARCHAR(" + (len + offset) + ")";
            case "boolean":
                return "BIT";
            default:
                return null; // Unrecognized data type
        }
    }

}

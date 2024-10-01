package com.datapig.service; 

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.datapig.service.dto.ModelRoot;
import com.datapig.service.dto.ModelEntity;
import com.datapig.service.dto.ModelAttribute;
import com.datapig.service.dto.ModelTrait;
import com.datapig.service.dto.ModelTraitArgument;
public class ParseModelJson {

    public void parseModelJson(String modelJson) {
        String json = "{ \"name\": \"cdm\", \"description\": \"cdm\", \"version\": \"1.0\", \"entities\": [...] }"; // Use the complete JSON string here

        Gson gson = new Gson();
        ModelRoot root = gson.fromJson(modelJson, ModelRoot.class);

        // Output some parsed data
        System.out.println("Name: " + root.getName());
        System.out.println("Description: " + root.getDescription());
        System.out.println("Version: " + root.getVersion());

        Map<String, String> sqlTypeMap = createSqlTypeMap(root);
        Set<String> entry = sqlTypeMap.keySet();
        // Print the SQL type map
        for (String iter : entry) {
            System.out.println("Entity: " + iter + " -> SQL Types: " + sqlTypeMap.get(iter));
        }
    }

    // Function to create a map from entity names to their SQL Server data types
    private static Map<String, String> createSqlTypeMap(ModelRoot root) {
        Map<String, String> sqlTypeMap = new HashMap<>();

        for (ModelEntity entity : root.getEntities()) {
            List<String> sqlTypes = new ArrayList<>();
            String iterAttr = "";
            for (ModelAttribute attribute : entity.getAttributes()) {
                String attributeName = attribute.getName();
                String sqlType = convertToSqlType(attribute);
                iterAttr += attributeName + " " + sqlType + ",";
                if (sqlType != null) {
                    sqlTypes.add(sqlType);
                }
            }
            sqlTypeMap.put(entity.getName(), iterAttr);
        }

        return sqlTypeMap;
    }

    // Function to convert Python data types to SQL Server data types
    private static String convertToSqlType(ModelAttribute attribute) {
        switch (attribute.getDataType().toLowerCase()) {
            case "guid":
                return "UNIQUEIDENTIFIER";
            case "datetime":
                return "DATETIME";
            case "datetimeoffset":
                return "DATETIMEOFFSET";
            case "int64":
                return "BIGINT";
            case "decimal":
                // Assuming scale and precision are part of traits
                int precision = 18; // Default precision
                int scale = 0; // Default scale
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
                return "DECIMAL(" + precision + ", " + scale + ")";
            case "string":
                return "NVARCHAR(" + attribute.getMaxLength() + ")";
            case "boolean":
                return "BIT";
            default:
                return null; // Unrecognized data type
        }
    }
}

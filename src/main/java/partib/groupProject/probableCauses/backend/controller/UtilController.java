package partib.groupProject.probableCauses.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.json.*;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Arrays;
import java.util.stream.Collectors;

import static partib.groupProject.probableCauses.backend.controller.ServerConnector.singleQueryCaller;

@RestController
@RequestMapping("/util")
public class UtilController {
    // Gets names of all available tables, returns in format ["table1", "table2", ...]
    @GetMapping("/tableNames")
    public static String getTableNames() throws InvalidCallException {
        String queryResult = singleQueryCaller(QueryController.db,
                "SELECT name FROM sqlite_master WHERE type='table' ORDER BY name");
        System.out.println(queryResult);
        JsonReader jsonReader = Json.createReader(new StringReader(queryResult));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        ArrayList<String> tableNames = new ArrayList<>();
        JsonArray names = jsonArray.getJsonArray(0);
        for (int i=0; i<names.size(); i++) {
            String name = names.getJsonObject(i).getString("name");
            if ( !(name.startsWith("bayesdb_") || name.startsWith("sqlite_")) ) {
                tableNames.add("\""+name+"\"");
            }
        }
        return "["+tableNames.stream().map(Object::toString).collect(Collectors.joining(", "))+"]";
    }

    // Gets names of all columns in given table, returns in format ["column1", "column2", ...]
    @GetMapping("/columnNames/{tableName}")
    public static String getColumnNames(@PathVariable String tableName) throws InvalidCallException {
        // Grab one row
        String row = singleQueryCaller(QueryController.db, "SELECT * FROM " + tableName + " LIMIT 1");
        // Extract list of column names from json result
        JsonReader jsonReader = Json.createReader(new StringReader(row));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();
        ArrayList<String> columnList = new ArrayList<>();
        for(Object key : jsonArray.getJsonArray(0).getJsonObject(0).keySet()) {
            columnList.add((String) key);
        }
        // Construct and return json output
        String json = "[";
        for(int i = 0; i < columnList.size(); i++) {
            json += "\""+columnList.get(i)+"\"";
            if (i+1 < columnList.size()) {
                json += ", ";
            }
        }
        json += "]";

        return json;
    }

    @GetMapping("/columnNamesPop/{populationName}")
    public static String getColumnNamesPopulation(@PathVariable String populationName) throws InvalidCallException {
        // Grab the correlation between any two columns
        String row = singleQueryCaller(QueryController.db, "SELECT * FROM (ESTIMATE CORRELATION FROM PAIRWISE VARIABLES OF " + populationName + ")");
        ArrayList<String> tmp = new ArrayList<>(Arrays.asList(row.split("\"name0\": \"")));
        tmp.remove(0);
        ArrayList<String> columnList = new ArrayList();
        for(String s : tmp){
            if(!columnList.contains(s.split("\"")[0])) columnList.add(s.split("\"")[0]);
        }

        // Construct and return json output
        String json = "[";
        for(int i = 0; i < columnList.size(); i++) {
            json += "\""+columnList.get(i)+"\"";
            if (i+1 < columnList.size()) {
                json += ", ";
            }
        }
        json += "]";

        return json;
    }

    // Gets names of all nominal columns in given table, returns in format ["column1", "column2", ...]
    @GetMapping("/nominalColumnNames/{tableName}")
    public static String getNominalColumnNames(@PathVariable String tableName) throws InvalidCallException {
        // Grab schema
        String statTypes = singleQueryCaller(QueryController.db, "GUESS SCHEMA FOR " + tableName);
        System.out.println(statTypes);
        // Extract nominal column names from json result
        JsonReader jsonReader = Json.createReader(new StringReader(statTypes));
        JsonArray jsonArray = jsonReader.readArray();
        jsonReader.close();

        ArrayList<String> columnList = new ArrayList<>();
        for (int i=0; i<jsonArray.getJsonArray(0).size(); i++) {
            JsonObject column = jsonArray.getJsonArray(0).getJsonObject(i);
            String columnName = column.getString("column");
            String statType = column.getString("stattype");
            System.out.println(columnName + " : " + statType);
            if (statType.equals("nominal")) {
                columnList.add(columnName);
            }
        }
        // Construct and return json output
        String json = "[";
        for(int i=0; i<columnList.size(); i++) {
            json += "\""+columnList.get(i)+"\"";
            if (i+1 < columnList.size()) {
                json += ", ";
            }
        }
        json += "]";

        return json;
    }

    // For testing purposes only
    @GetMapping("/anyQuery/{query}")
    public static String runAnyQuery(@PathVariable String query) throws InvalidCallException {
        return singleQueryCaller(QueryController.db, query);
    }

    public static String getDimensions(String stringJson) { // TODO Implement this ? Scrap this ?
        return null;
    }

}
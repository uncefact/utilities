package org.unece.uncefact.vocab.md;

import org.apache.commons.lang3.StringUtils;
import org.unece.uncefact.vocab.Constants;
import org.unece.uncefact.vocab.FileGenerator;
import org.unece.uncefact.vocab.UNLOCODEConstants;

import jakarta.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class UNLCDToMD {

    public static void main(String args[]){
        try {
            generate("unlcd");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void generate(String workingDir) throws IOException {
        InputStream fis = null;
        try {
            fis = new FileInputStream(workingDir.concat("/unlocode.jsonld"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonReader reader = Json.createReader(fis);
        JsonObject vocabulary = reader.readObject();
        reader.close();

        JsonArray graph = vocabulary.getJsonArray("@graph");

        Iterator<JsonValue> iterator = graph.iterator();
        int i =0;
        String csvContent = "";
        csvContent += String.format("\"%s\",", "uri");
        csvContent += String.format("\"%s\",", "label");
        csvContent += String.format("\"%s\",", "labelWithDiacritics");
        csvContent += String.format("\"%s\",", UNLOCODEConstants.FUNCTIONS_PROPERTY);
        csvContent += String.format("\"%s\",", UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY);
        csvContent += String.format("\"%s\",", UNLOCODEConstants.PROPERTY_COUNTRY_CODE);
        csvContent += String.format("\"%s\",", UNLOCODEConstants.GEO_NS + ":lat");
        csvContent += String.format("\"%s\",", UNLOCODEConstants.GEO_NS + ":long");
        csvContent += String.format("\"%s\"\n", "value");
        String countryCode = "";
        while (iterator.hasNext()) {
            /*if(i>100){
                break;
            } else {
                i = i+1;
            }*/
            JsonObject jsonObject = iterator.next().asJsonObject();
            String currentCountryCode = StringUtils.substringAfter(jsonObject.getJsonObject(UNLOCODEConstants.PROPERTY_COUNTRY_CODE).getString(Constants.ID),":").toLowerCase();

            if (countryCode.equalsIgnoreCase("")) {
                countryCode = currentCountryCode;
            }
            if (!countryCode.equalsIgnoreCase(currentCountryCode)){
                new FileGenerator().generateTextFile(csvContent, String.format(workingDir+"/_data/unlocode%s.csv", countryCode.toLowerCase()));
                csvContent = "";
                csvContent += String.format("\"%s\",", "uri");
                csvContent += String.format("\"%s\",", "label");
                csvContent += String.format("\"%s\",", "labelWithDiacritics");
                csvContent += String.format("\"%s\",", UNLOCODEConstants.FUNCTIONS_PROPERTY);
                csvContent += String.format("\"%s\",", UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY);
                csvContent += String.format("\"%s\",", UNLOCODEConstants.PROPERTY_COUNTRY_CODE);
                csvContent += String.format("\"%s\",", UNLOCODEConstants.GEO_NS + ":lat");
                csvContent += String.format("\"%s\",", UNLOCODEConstants.GEO_NS + ":long");
                csvContent += String.format("\"%s\"\n", "value");

                String mdContent = "---\n";
                mdContent += String.format("title: %s\n", "UN/LOCODE");
                mdContent += String.format("permalink: %s%s\n", "unlocode", countryCode);
                mdContent += String.format("jsonid: %s%s\n", "unlocode", countryCode);
                mdContent += String.format("label: %s\n", "UN/LOCODE");
                mdContent += String.format("comment: %s\n", "desc");
                mdContent += "excludeFromList: true\n";
                mdContent += "columns:\n";
                mdContent += "  - \n";
                mdContent += "    title: Label\n";
                mdContent += String.format("    code: %s\n", "label");
                mdContent += "  - \n";
                mdContent += "    title: Label With Diacritics\n";
                mdContent += String.format("    code: %s\n", "labelWithDiacritics");
                mdContent += "  - \n";
                mdContent += "    title: Functions\n";
                mdContent += String.format("    code: %s\n", UNLOCODEConstants.FUNCTIONS_PROPERTY);
                mdContent += "    type: uri\n";
                mdContent += "  - \n";
                mdContent += "    title: Country Code\n";
                mdContent += String.format("    code: %s\n", UNLOCODEConstants.PROPERTY_COUNTRY_CODE);
                mdContent += "    type: uri\n";
                mdContent += "  - \n";
                mdContent += "    title: Country Subdivision\n";
                mdContent += String.format("    code: %s\n", UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY);
                mdContent += "    type: uri\n";
                mdContent += "  - \n";
                mdContent += "    title: Latitude\n";
                mdContent += String.format("    code: %s\n", UNLOCODEConstants.GEO_NS+":lat");
                mdContent += "  - \n";
                mdContent += "    title: Longitude\n";
                mdContent += String.format("    code: %s\n", UNLOCODEConstants.GEO_NS+":long");
                mdContent += "---\n";
                new FileGenerator().generateTextFile(mdContent, String.format(workingDir+"/_code-lists/unlocode%s.md", countryCode));
            }
            countryCode = currentCountryCode;
            csvContent += String.format("\"%s\",", jsonObject.getString(Constants.ID));
            if (jsonObject.containsKey(Constants.RDFS_LABEL)) {
                JsonValue label = jsonObject.get(Constants.RDFS_LABEL);
                if (label instanceof JsonObject){
                    csvContent += String.format("\"%s\",,", label.asJsonObject().getString(Constants.VALUE));
                } else if (label instanceof JsonArray){
                    for (JsonObject labelObj : label.asJsonArray().getValuesAs(JsonObject.class)){
                        if (labelObj.containsKey(Constants.LANGUAGE)){
                            csvContent += String.format("\"%s\",", labelObj.getString(Constants.VALUE));
                        } else {
                            csvContent += String.format("\"%s\",", labelObj.getString(Constants.VALUE));
                        }
                    }
                }
            }
            if (jsonObject.containsKey(UNLOCODEConstants.FUNCTIONS_PROPERTY)) {
                JsonValue functions = jsonObject.get(UNLOCODEConstants.FUNCTIONS_PROPERTY);
                if (functions instanceof JsonObject){
                    csvContent += String.format("\"%s\",", functions.asJsonObject().getString(Constants.ID));
                } else if (functions instanceof JsonArray){
                    String functionsStr = "";
                    for (JsonObject function : functions.asJsonArray().getValuesAs(JsonObject.class)){
                        functionsStr = functionsStr.concat(function.getString(Constants.ID)).concat(", ");
                    }
                    functionsStr = functionsStr.substring(0, functionsStr.length()-2);
                    csvContent += String.format("\"%s\",", functionsStr);
                }
            }
            if (jsonObject.containsKey(UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY)) {
                csvContent += String.format("\"%s\",", jsonObject.get(UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY).asJsonObject().getString(Constants.ID));
            } else {
                csvContent += ",";
            }
            if (jsonObject.containsKey(UNLOCODEConstants.PROPERTY_COUNTRY_CODE)) {
                csvContent += String.format("\"%s\",", jsonObject.getJsonObject(UNLOCODEConstants.PROPERTY_COUNTRY_CODE).getString(Constants.ID));
            } else {
                csvContent += ",";
            }
            if (jsonObject.containsKey(UNLOCODEConstants.GEO_NS + ":lat")) {
                csvContent += String.format("\"%s\",", jsonObject.get(UNLOCODEConstants.GEO_NS + ":lat").asJsonObject().getString(Constants.VALUE));
            } else {
                csvContent += ",";
            }
            if (jsonObject.containsKey(UNLOCODEConstants.GEO_NS + ":long")) {
                csvContent += String.format("\"%s\",", jsonObject.get(UNLOCODEConstants.GEO_NS + ":long").asJsonObject().getString(Constants.VALUE));
            } else {
                csvContent += ",";
            }
            if (jsonObject.containsKey(Constants.RDF_VALUE)) {
                csvContent += String.format("\"%s\"\n", jsonObject.getString(Constants.RDF_VALUE));
            }
            else
                csvContent += "\n";
        }
        new FileGenerator().generateTextFile(csvContent, String.format(workingDir+"/_data/unlocode%s.csv", countryCode));
/*
        codeList.add("label", "UN/LOCODE";
        codeList.add("comment", "desc";
*/

        String mdContent = "---\n";
        mdContent += String.format("title: %s\n", "UN/LOCODE");
        mdContent += String.format("permalink: %s%s\n", "unlocode", countryCode);
        mdContent += String.format("jsonid: %s%s\n", "unlocode", countryCode);
        mdContent += String.format("label: %s\n", "UN/LOCODE");
        mdContent += String.format("comment: %s\n", "desc");
        mdContent += "excludeFromList: true\n";
        mdContent += "columns:\n";
        mdContent += "  - \n";
        mdContent += "    title: Label\n";
        mdContent += String.format("    code: %s\n", "label");
        mdContent += "  - \n";
        mdContent += "    title: Label With Diacritics\n";
        mdContent += String.format("    code: %s\n", "labelWithDiacritics");
        mdContent += "  - \n";
        mdContent += "    title: Functions\n";
        mdContent += String.format("    code: %s\n", UNLOCODEConstants.FUNCTIONS_PROPERTY);
        mdContent += "    type: uri\n";
        mdContent += "  - \n";
        mdContent += "    title: Country Code\n";
        mdContent += String.format("    code: %s\n", UNLOCODEConstants.PROPERTY_COUNTRY_CODE);
        mdContent += "    type: uri\n";
        mdContent += "  - \n";
        mdContent += "    title: Country Subdivision\n";
        mdContent += String.format("    code: %s\n", UNLOCODEConstants.COUNTRY_SUBDIVISION_PROPERTY);
        mdContent += "    type: uri\n";
        mdContent += "  - \n";
        mdContent += "    title: Latitude\n";
        mdContent += String.format("    code: %s\n", UNLOCODEConstants.GEO_NS+":lat");
        mdContent += "  - \n";
        mdContent += "    title: Longitude\n";
        mdContent += String.format("    code: %s\n", UNLOCODEConstants.GEO_NS+":long");
        mdContent += "---\n";
        new FileGenerator().generateTextFile(mdContent, String.format(workingDir+"/_code-lists/unlocode%s.md", countryCode));
        fis.close();
    }
}

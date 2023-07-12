package org.unece.uncefact.vocab;

import jakarta.json.*;
import jakarta.json.stream.JsonGenerator;
import java.io.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class FileGenerator {

    public void generateFile(final JsonObjectBuilder contextObjectBuilder,
                             final JsonArrayBuilder graphJsonArrayBuilder, boolean prettyPrint, String outputFile) {
        JsonObjectBuilder jsonObjectBuilder = Json.createObjectBuilder();
        jsonObjectBuilder.add("@context", contextObjectBuilder.build());
        if (graphJsonArrayBuilder!=null) {
            JsonArray graph = graphJsonArrayBuilder.build();
            verify(graph);
            jsonObjectBuilder.add("@graph", graph);
        }

        Map<String, Boolean> config = new HashMap<>();
        if (prettyPrint) {
            config.put(JsonGenerator.PRETTY_PRINTING, true);
        }
        StringWriter stringWriter = new StringWriter();
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);

        try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            jsonWriter.writeObject(jsonObjectBuilder.build());
        }
        try (PrintWriter writer =  new PrintWriter(outputFile, "UTF-8")){
            writer.print(stringWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void verify (JsonArray array){
        Set<String> ids = new HashSet<>();
        Map<String, String> idsLC = new HashMap<>();
        Set<String> duplicates = new HashSet<>();
        Map<String,String> duplicatesCaseIgnored = new HashMap<>();
        for (JsonObject jsonObject:array.getValuesAs(JsonObject.class)) {
            String id = jsonObject.getString(Constants.ID);
            if (ids.contains(id)) {
                duplicates.add(id);
            } else {
                ids.add(id);
            } if (idsLC.containsKey(id.toLowerCase())) {
                duplicatesCaseIgnored.put(idsLC.get(id.toLowerCase()), id);
            } else {
                idsLC.put(id.toLowerCase(), id);
            }
        }

        if (!duplicates.isEmpty()) {
            System.err.println("### :x: Resources with the same names exist in the vocabulary:");
            for (String id:duplicates){
                System.err.println(String.format("- %s", id));
            }
        }

        if (!duplicatesCaseIgnored.isEmpty()){
            System.out.println("### :warning: Resources with the same names (case ignored) exist in the vocabulary:");
            for (String id:duplicatesCaseIgnored.keySet()){
                System.out.println(String.format("- %s and %s", id, duplicatesCaseIgnored.get(id)));
            }
        }

        Set<String> missingRanges = new HashSet<>();
        for (JsonObject jsonObject:array.getValuesAs(JsonObject.class)) {
            if (jsonObject.containsKey(Constants.SCHEMA_RANGE_INCLUDES)) {
                String rangeIncludes = jsonObject.getJsonObject(Constants.SCHEMA_RANGE_INCLUDES).getString(Constants.ID);
                if (!ids.contains(rangeIncludes)) {
                    if (!missingRanges.contains(rangeIncludes)) {
                        missingRanges.add(rangeIncludes);
                    }
                }
            }
        }

        if (!missingRanges.isEmpty()){
            System.err.println("### :x: Schema range includes are used but not defined in the vocabulary:");
            for (String id:missingRanges){
                System.err.println(String.format("- %s", id));
            }
        }
    }

    public void generateFile(final JsonValue jsonValue, boolean prettyPrint, String outputFile) {

        Map<String, Boolean> config = new HashMap<>();
        if (prettyPrint) {
            config.put(JsonGenerator.PRETTY_PRINTING, true);
        }
        StringWriter stringWriter = new StringWriter();
        JsonWriterFactory writerFactory = Json.createWriterFactory(config);

        try (JsonWriter jsonWriter = writerFactory.createWriter(stringWriter)) {
            if (jsonValue instanceof JsonObject)
                jsonWriter.writeObject(jsonValue.asJsonObject());
            else if (jsonValue instanceof JsonArrayBuilder)
                jsonWriter.writeArray(jsonValue.asJsonArray());
        }
        try (PrintWriter writer =  new PrintWriter(outputFile, "UTF-8")){
            writer.print(stringWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public void generateTextFile(final String text, String outputFile) {

        StringWriter stringWriter = new StringWriter();
        stringWriter.write(text);
        try (PrintWriter writer =  new PrintWriter(outputFile, "UTF-8")){
            writer.print(stringWriter);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }
}

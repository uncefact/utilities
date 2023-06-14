package org.unece.uncefact.vocab;

import org.apache.commons.lang3.StringUtils;

import jakarta.json.*;
import jakarta.json.stream.JsonParsingException;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class JsonLDMerge {
    public static void merge(String workingDir, Set<String> inputFileNames, boolean prettyPrint, String outputFileName) throws IOException {
        workingDir = StringUtils.defaultString(workingDir, "");
        try (Stream<Path> walk = Files.walk(Paths.get(workingDir))) {

            List<String> domains = null;
            if (inputFileNames.isEmpty()){
                domains = walk.filter(Files::isRegularFile)
                        .map(x -> x.toString()).collect(Collectors.toList());
            } else {
                domains = walk.filter(Files::isRegularFile)
                        .filter(p -> inputFileNames.contains(p.getFileName().toString()))
                        .map(x -> x.toString()).collect(Collectors.toList());
            }
            boolean valid = true;
            Map<String, JsonObject> items = new HashMap<>();
            JsonObjectBuilder context = Json.createObjectBuilder();
            for (String domain : domains) {
                String domainName = StringUtils.substringBetween(domain, "vocab/", ".jsonld");
                InputStream fis = null;
                try {
                    fis = new FileInputStream(domain);
                } catch (FileNotFoundException e) {
                    throw new RuntimeException(e);
                }

                JsonReader reader = Json.createReader(fis);
                JsonObject vocabulary = null;
                try {
                    vocabulary = reader.readObject();
                } catch (JsonParsingException e) {
                    System.err.println(String.format("Can't parse the file %s.", domain));
                }
                JsonObject contextObject = vocabulary.getJsonObject("@context");
                for (String key : contextObject.keySet()) {
                    context.add(key, contextObject.get(key));
                }
                JsonArray graph = vocabulary.getJsonArray("@graph");
                Iterator<JsonValue> iterator = graph.iterator();
                while (iterator.hasNext()) {
                    JsonObject item = iterator.next().asJsonObject();
                    JsonObjectBuilder objectBuilder = Json.createObjectBuilder(item);
                    String id = item.getString("@id");
                    if (items.containsKey(id)) {
                        System.err.println(String.format("Vocabulary already contains %s resource", id));
                    } else {
                        objectBuilder.add("unece:domain", domainName);
                        items.put(id, objectBuilder.build());
                    }
                }
                reader.close();
            }
            JsonArrayBuilder mergedGraph = Json.createArrayBuilder();
            List<String> keys = new ArrayList<>(items.keySet());
            Collections.sort(keys);
            for (String key : keys) {
                mergedGraph.add(items.get(key));
            }
            new FileGenerator().generateFile(context, mergedGraph, prettyPrint, outputFileName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void format(String workingDir, Set<String> inputFileNames, boolean prettyPrint, String outputFileName){
        InputStream fis = null;
        String path = workingDir.concat(inputFileNames.iterator().next());
        try {
            fis = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonReader reader = Json.createReader(fis);
        JsonObject vocabulary = null;
        try {
            vocabulary = reader.readObject();
        } catch (JsonParsingException e) {
            System.err.println(String.format("Can't parse the file %s.", path));
        }

        new FileGenerator().generateFile(vocabulary, prettyPrint, outputFileName);
    }
}

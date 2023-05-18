package org.unece.uncefact.vocab;

import javax.json.*;
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
    public static void main (String args[]) throws IOException {
        try (Stream<Path> walk = Files.walk(Paths.get("vocab/"))) {
            // We want to find only regular files
            List<String> result = walk.filter(Files::isRegularFile)
                    .map(x -> x.toString()).collect(Collectors.toList());
            merge(result);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    public static void merge(List<String> domains) throws IOException {
        boolean valid = true;
        Map<String, JsonObject> items = new HashMap<>();
        JsonObjectBuilder context = Json.createObjectBuilder();
        for (String domain:domains) {
            InputStream fis = null;
            try {
                fis = new FileInputStream(domain);
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            JsonReader reader = Json.createReader(fis);
            JsonObject vocabulary = reader.readObject();
            JsonObject contextObject = vocabulary.getJsonObject("@context");
            for (String key:contextObject.keySet()){
                context.add(key, contextObject.get(key));
            }
            JsonArray graph = vocabulary.getJsonArray("@graph");
            Iterator<JsonValue> iterator = graph.iterator();
            while (iterator.hasNext()){
                JsonObject item = iterator.next().asJsonObject();
                String id = item.getString("@id");
                if (items.containsKey(id)){
                    System.err.println(String.format("Vocabulary already contains %s resource", id));
                } else {
                    items.put(id, item);
                }
            }
            reader.close();
        }
        JsonArrayBuilder mergedGraph = Json.createArrayBuilder();
        List<String> keys = new ArrayList<>(items.keySet());
        Collections.sort(keys);
        for (String key:keys) {
            mergedGraph.add(items.get(key));
        }
        new FileGenerator().generateFile(context, mergedGraph, true, "merged.jsonld");
    }

}

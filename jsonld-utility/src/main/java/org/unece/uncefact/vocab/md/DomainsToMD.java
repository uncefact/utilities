package org.unece.uncefact.vocab.md;

import org.apache.commons.lang3.StringUtils;
import org.unece.uncefact.vocab.Constants;
import org.unece.uncefact.vocab.FileGenerator;

import jakarta.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class DomainsToMD {
    public static void generate (String workingDir, Set<String> inputFileNames) throws IOException {
        Files.createDirectory(Paths.get(workingDir.concat("_data/")));
        Files.createDirectory(Paths.get(workingDir.concat("_code-lists/")));
        Files.createDirectory(Paths.get(workingDir.concat("_classes/")));
        Files.createDirectory(Paths.get(workingDir.concat("_properties/")));

        try {
            generate(workingDir, inputFileNames, "jargon");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public static void generate(String workingDir, Set<String> inputFileNames, final String dataSet) throws IOException {
        Map<String, JsonObject> classes = new TreeMap<>();
        Map<String, JsonObject> dataTypeProperties = new TreeMap<>();
        Map<String, JsonObject> objectsProperties = new TreeMap<>();
        Map<String, Map<String, JsonObject>> codeProperties = new TreeMap<>();
        Map<String, String> classesCommentsMap = new TreeMap<>();
        Map<String, String> codeListsCommentsMap = new TreeMap<>();
        Map<String, TreeMap<String, JsonObject>> codeListsReferrencedByMap = new TreeMap<>();
        JsonArrayBuilder batchArrayBuilder = Json.createArrayBuilder();
        Map<String, String> uniqueIDsCheck = new HashMap();
        JsonObjectBuilder combinedVocabulary = Json.createObjectBuilder();
        JsonObject context = null;
        Map<String, JsonObjectBuilder> contextItemsMap = new HashMap<>();
        JsonArrayBuilder combinedGraphVocabulary = Json.createArrayBuilder();
        Set<String> missingDefinitions = new HashSet<>();
        Set<String> classNamesLowerCased = new HashSet<>();
        Set<String> rdfPropertiesWithCodevalues = new HashSet<>();



        for (String filename:inputFileNames) {
            String domain = StringUtils.substringBefore(filename, ".jsonld");
            InputStream fis = null;
            try {
                fis = new FileInputStream(workingDir.concat(filename));
            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            }

            JsonReader reader = Json.createReader(fis);
            JsonObject vocabulary = reader.readObject();
            reader.close();
            if (context == null) {
                context = vocabulary.getJsonObject("@context");
            }
            JsonArray graph = vocabulary.getJsonArray("@graph");

            Iterator<JsonValue> iterator = graph.iterator();
            while (iterator.hasNext()) {
                JsonValue jsonValue = iterator.next();
                JsonObject jsonObject = (JsonObject) jsonValue;
                combinedGraphVocabulary.add(jsonObject);
                JsonValue type = jsonObject.get("@type");
                if (uniqueIDsCheck.keySet().contains(jsonObject.getString(Constants.ID))){
/*
                    throw new RuntimeException(String.format("Non-unique URI %s", jsonObject.getString(Transformer.ID)));
*/
                    System.err.println(String.format("Non-unique URI %s exists in two domains - %s and %s",
                            jsonObject.getString(Constants.ID),
                            domain,
                            uniqueIDsCheck.get(jsonObject.getString(Constants.ID))
                    ));
                } else {
                    uniqueIDsCheck.put(jsonObject.getString(Constants.ID), domain);
                }
                String typeValue = "";
                String id = jsonObject.getString(Constants.ID);
                JsonObjectBuilder item = Json.createObjectBuilder();
                if (type instanceof JsonArray) {
                    JsonArray typeArray = (JsonArray) type;
                    for (int i = 0; i < typeArray.size(); i++) {
                        switch (((JsonString) typeArray.get(i)).getString()) {
                            case "rdfs:Class":
                                classes.put(id, jsonObject);
                                classNamesLowerCased.add(id.toLowerCase());
/*
                                item.add(Transformer.ID, id);
                                contextItemsMap.put(id, item);
*/
                                break;
                            case "owl:DatatypeProperty":
                                dataTypeProperties.put(jsonObject.getString(Constants.ID), jsonObject);
/*
                                item.add(Transformer.ID, id);
                                item.add(Transformer.TYPE, jsonObject.getJsonObject(Transformer.SCHEMA_RANGE_INCLUDES)
                                        .getString(Transformer.ID));
                                contextItemsMap.put(id, item);
*/
                                break;
                            case "owl:ObjectProperty":
                                objectsProperties.put(jsonObject.getString(Constants.ID), jsonObject);
/*
                                item.add(Transformer.ID, id);
                                item.add(Transformer.TYPE, Transformer.ID);
                                contextItemsMap.put(id, item);
*/
                                break;
                            default:
                                if (jsonObject.containsKey(Constants.RDF_VALUE)) {
                                    String codeListId = StringUtils.substringBeforeLast(jsonObject.getString(Constants.ID), "#");
                                    Map<String, JsonObject> codeListValues = null;
                                    if (codeProperties.containsKey(codeListId)) {
                                        codeListValues = codeProperties.get(codeListId);
                                    } else {
                                        codeListValues = new TreeMap<>();
                                    }
                                    codeListValues.put(jsonObject.getString(Constants.ID), jsonObject);
                                    codeProperties.put(codeListId, codeListValues);
                                }
                                break;

                        }
                    }
                } else {
                    if (type == null){
                        System.err.println(String.format("@type isn't defined for %s", jsonObject.getString(Constants.ID)));
                    }
                    typeValue = ((JsonString) type).getString();
                    switch (typeValue) {
                        case "rdfs:Class":
                            classes.put(id, jsonObject);
                            classNamesLowerCased.add(id.toLowerCase());
/*
                            item.add(Transformer.ID, id);
                            contextItemsMap.put(id, item);
*/
                            break;
                        case "owl:DatatypeProperty":
                            dataTypeProperties.put(jsonObject.getString(Constants.ID), jsonObject);
/*
                            item.add(Transformer.ID, id);
                            item.add(Transformer.TYPE, jsonObject.getJsonObject(Transformer.SCHEMA_RANGE_INCLUDES)
                                    .getString(Transformer.ID));
                            contextItemsMap.put(id, item);
*/
                            break;
                        case "owl:ObjectProperty":
                            objectsProperties.put(jsonObject.getString(Constants.ID), jsonObject);
/*
                            item.add(Transformer.ID, id);
                            item.add(Transformer.TYPE, Transformer.ID);
                            contextItemsMap.put(id, item);
*/
                            break;
                        case "rdf:Property":
                            dataTypeProperties.put(jsonObject.getString(Constants.ID), jsonObject);
/*
                            item.add(Transformer.ID, id);
                            item.add(Transformer.TYPE, jsonObject.getJsonObject(Transformer.SCHEMA_RANGE_INCLUDES)
                                    .getString(Transformer.ID));
                            contextItemsMap.put(id, item);
*/
                            break;
                        default:
                            if (jsonObject.containsKey(Constants.RDF_VALUE)) {
                                String codeListId = StringUtils.substringBeforeLast(jsonObject.getString(Constants.ID), "#");
                                Map<String, JsonObject> codeListValues = null;
                                if (codeProperties.containsKey(codeListId)) {
                                    codeListValues = codeProperties.get(codeListId);
                                } else {
                                    codeListValues = new TreeMap<>();
                                }
                                codeListValues.put(jsonObject.getString(Constants.ID), jsonObject);
                                codeProperties.put(codeListId, codeListValues);
                            }
                            break;

                    }
                }
            }
            fis.close();
        }
        for (JsonObject jsonObject:classes.values()) {
            if (!codeProperties.containsKey(jsonObject.getString(Constants.ID))) {
                JsonObjectBuilder batchObject = Json.createObjectBuilder();
                batchObject.add("type", "add");
                batchObject.add("id", dataSet.concat("_").concat(jsonObject.getString(Constants.ID)));
                JsonObjectBuilder batchFieldsObject = Json.createObjectBuilder();
                batchFieldsObject.add("label", jsonObject.getString(Constants.RDFS_LABEL));
                JsonValue classComment = jsonObject.get(Constants.RDFS_COMMENT);
                String commentStr = "";
                if(classComment instanceof JsonString){
                    commentStr = ((JsonString) classComment).getString();
                } else if (classComment instanceof JsonArray) {
                    JsonArray commentsArray = classComment.asJsonArray();
                    for (JsonString comment:commentsArray.getValuesAs(JsonString.class)){
                        commentStr = commentStr.concat(comment.getString()).concat(" ");
                    }

                }
                batchFieldsObject.add("comment", commentStr);
                batchFieldsObject.add("type", "Class");
                batchFieldsObject.add("dataset", dataSet);
                batchObject.add("fields", batchFieldsObject.build());
                batchArrayBuilder.add(batchObject.build());
            }


            String outputFileName = StringUtils.substringAfter(jsonObject.getString(Constants.ID), ":").replaceAll("\\.","_").toLowerCase();
            JsonObjectBuilder mdClass = Json.createObjectBuilder();
            mdClass.add("label", jsonObject.getString(Constants.RDFS_LABEL));
            mdClass.add("uri", jsonObject.getString(Constants.ID));
            mdClass.add("comment", jsonObject.get(Constants.RDFS_COMMENT));

            JsonValue classComment = jsonObject.get(Constants.RDFS_COMMENT);
            if(classComment instanceof JsonString){
                classesCommentsMap.put(jsonObject.getString(Constants.ID), jsonObject.getString(Constants.RDFS_COMMENT));
            } else if (classComment instanceof JsonArray) {
                String commentStr = getFirstNWords(classComment.asJsonArray().getString(0));
                System.out.println(commentStr);
                classesCommentsMap.put(jsonObject.getString(Constants.ID), commentStr);
            }

            TreeMap<String, JsonObject> referencedBySet = new TreeMap<>();
            Map<String, JsonObject> dataTypePropertiesSet = new TreeMap<>();

            for (JsonObject dataTypeProperty:dataTypeProperties.values()){
                JsonValue domainIncludes = dataTypeProperty.get(Constants.SCHEMA_DOMAIN_INCLUDES);
                if (domainIncludes instanceof JsonObject){
                    String domain = ((JsonObject)domainIncludes).getString(Constants.ID);
                    if(domain.equalsIgnoreCase(jsonObject.getString(Constants.ID))){
                        JsonObjectBuilder mdDataTypeProperty = Json.createObjectBuilder();
                        mdDataTypeProperty.add("uri", dataTypeProperty.getString(Constants.ID));
                        mdDataTypeProperty.add("type", dataTypeProperty.getJsonObject(Constants.SCHEMA_RANGE_INCLUDES).getString(Constants.ID));
                        JsonArray cefactElementMetadata = dataTypeProperty.getJsonArray(Constants.UNECE_NS+":cefactElementMetadata");
                        if(cefactElementMetadata!=null) {
                            for (JsonObject metadata : cefactElementMetadata.getValuesAs(JsonObject.class)) {
                                if (metadata.keySet().contains(Constants.UNECE_NS+":domainName")
                                    && metadata.getString(Constants.UNECE_NS+":domainName").equalsIgnoreCase(jsonObject.getString(Constants.RDFS_LABEL))) {
                                    JsonValue comment = metadata.get(Constants.RDFS_COMMENT);
                                    mdDataTypeProperty.add("comment", comment);
                                }
                            }
                        } else {
                            JsonValue comment = dataTypeProperty.get(Constants.RDFS_COMMENT);
                            if(comment instanceof JsonString) {
                                mdDataTypeProperty.add("comment", comment);
                            } else if (comment instanceof JsonArray){
                                mdDataTypeProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                            }
                        }
/*                        JsonValue comment = dataTypeProperty.get(Transformer.RDFS_COMMENT);
                        if(comment instanceof JsonString)
                            mdDataTypeProperty.add("comment", comment);
                        else if (comment instanceof JsonArray){
                            mdDataTypeProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                        }*/
                        dataTypePropertiesSet.put(dataTypeProperty.getString(Constants.ID), mdDataTypeProperty.build());
                    }
                } else if (domainIncludes instanceof JsonArray) {
                    JsonArray domainIncludesJsonArray = (JsonArray) domainIncludes;
                    Iterator<JsonValue> domainIterator = domainIncludesJsonArray.iterator();
                    while (domainIterator.hasNext()){
                        JsonObject domainJsonObject = (JsonObject) domainIterator.next();
                        String domain = (domainJsonObject).getString(Constants.ID);

                        if(domain.equalsIgnoreCase(jsonObject.getString(Constants.ID))){
                            JsonObjectBuilder mdDataTypeProperty = Json.createObjectBuilder();
                            mdDataTypeProperty.add("uri", dataTypeProperty.getString(Constants.ID));
                            mdDataTypeProperty.add("type", dataTypeProperty.getJsonObject(Constants.SCHEMA_RANGE_INCLUDES).getString(Constants.ID));
                            JsonArray cefactElementMetadata = dataTypeProperty.getJsonArray(Constants.UNECE_NS+":cefactElementMetadata");
                            if (cefactElementMetadata!=null) {
                                for (JsonObject metadata : cefactElementMetadata.getValuesAs(JsonObject.class)) {
                                    if (metadata.keySet().contains(Constants.UNECE_NS+":domainName")
                                     && metadata.getString(Constants.UNECE_NS+":domainName").equalsIgnoreCase(jsonObject.getString(Constants.RDFS_LABEL))) {
                                        JsonValue comment = metadata.get(Constants.RDFS_COMMENT);
                                        mdDataTypeProperty.add("comment", comment);
                                    }
                                }
                            }
                            else {
                                JsonValue comment = dataTypeProperty.get(Constants.RDFS_COMMENT);
                                if(comment instanceof JsonString) {
                                    mdDataTypeProperty.add("comment", comment);
                                }
                                else if (comment instanceof JsonArray){
                                    mdDataTypeProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                                }
                            }
                            /*JsonValue comment = dataTypeProperty.get(Transformer.RDFS_COMMENT);
                            if(comment instanceof JsonString)
                                mdDataTypeProperty.add("comment", comment);
                            else if (comment instanceof JsonArray){
                                mdDataTypeProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                            }*/
                            dataTypePropertiesSet.put(dataTypeProperty.getString(Constants.ID), mdDataTypeProperty.build());;
                        }
                    }
                }
                JsonValue rangeIncludes = dataTypeProperty.get(Constants.SCHEMA_RANGE_INCLUDES);
                if (rangeIncludes instanceof JsonObject){
                    String range = ((JsonObject)rangeIncludes).getString(Constants.ID);
                    if(range.equalsIgnoreCase(jsonObject.getString(Constants.ID))){
                        JsonObjectBuilder mdReferencedBy = Json.createObjectBuilder();
                        mdReferencedBy.add("uri", dataTypeProperty.getString(Constants.ID));

                        JsonValue comment = dataTypeProperty.get(Constants.RDFS_COMMENT);
                        if(comment instanceof JsonString) {
                            mdReferencedBy.add("comment", comment);
                        }
                        else if (comment instanceof JsonArray){
                            mdReferencedBy.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                        }
                        referencedBySet.put(dataTypeProperty.getString(Constants.ID), mdReferencedBy.build());
                    }
                }
            }
            if (dataTypePropertiesSet.size()>0){
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (JsonObject dataTypeProperty:dataTypePropertiesSet.values()){
                    arrayBuilder.add(dataTypeProperty);
                }
                mdClass.add("datatypeProperties", arrayBuilder.build());
            }

            TreeMap<String, JsonObject> objectsPropertiesSet = new TreeMap<>();
            for (JsonObject objectProperty:objectsProperties.values()){
                JsonValue domainIncludes = objectProperty.get(Constants.SCHEMA_DOMAIN_INCLUDES);
                if (domainIncludes instanceof JsonObject){
                    String domain = ((JsonObject)domainIncludes).getString(Constants.ID);
                    if(domain.equalsIgnoreCase(jsonObject.getString(Constants.ID))){
                        JsonObjectBuilder mdObjectProperty = Json.createObjectBuilder();
                        mdObjectProperty.add("uri", objectProperty.getString(Constants.ID));
                        mdObjectProperty.add("type", objectProperty.getJsonObject(Constants.SCHEMA_RANGE_INCLUDES).getString(Constants.ID));
                        JsonArray cefactElementMetadata = objectProperty.getJsonArray(Constants.UNECE_NS+":cefactElementMetadata");
                        if (cefactElementMetadata!=null) {
                            for (JsonObject metadata : cefactElementMetadata.getValuesAs(JsonObject.class)) {
                                if (metadata.getString("unece:domainName").equalsIgnoreCase(jsonObject.getString(Constants.RDFS_LABEL))) {
                                    JsonValue comment = metadata.get(Constants.RDFS_COMMENT);
                                    mdObjectProperty.add("comment", comment);
                                }
                            }
                        }
                        else {
                            JsonValue comment = objectProperty.get(Constants.RDFS_COMMENT);
                            if(comment instanceof JsonString)
                                mdObjectProperty.add("comment", comment);
                        }
                        /*JsonValue comment = objectProperty.get(Transformer.RDFS_COMMENT);
                        if(comment instanceof JsonString)
                            mdObjectProperty.add("comment", comment);
                        else if (comment instanceof JsonArray){
                            mdObjectProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                        }*/
                        objectsPropertiesSet.put(objectProperty.getString(Constants.ID), mdObjectProperty.build());
                    }
                }
                if (domainIncludes instanceof JsonArray){
                    for (JsonObject domainIncludesObject:((JsonArray) domainIncludes).getValuesAs(JsonObject.class)) {
                        String domain = domainIncludesObject.getString(Constants.ID);
                        if (domain.equalsIgnoreCase(jsonObject.getString(Constants.ID))) {
                            JsonObjectBuilder mdObjectProperty = Json.createObjectBuilder();
                            mdObjectProperty.add("uri", objectProperty.getString(Constants.ID));
                            mdObjectProperty.add("type", objectProperty.getJsonObject(Constants.SCHEMA_RANGE_INCLUDES).getString(Constants.ID));
                            JsonArray cefactElementMetadata = objectProperty.getJsonArray(Constants.UNECE_NS + ":cefactElementMetadata");
                            if (cefactElementMetadata!=null) {
                                for (JsonObject metadata : cefactElementMetadata.getValuesAs(JsonObject.class)) {
                                    if (metadata.getString("unece:domainName").equalsIgnoreCase(jsonObject.getString(Constants.RDFS_LABEL))) {
                                        JsonValue comment = metadata.get(Constants.RDFS_COMMENT);
                                        mdObjectProperty.add("comment", comment);
                                    }
                                }
                            }
                            else {
                                JsonValue comment = objectProperty.get(Constants.RDFS_COMMENT);
                                if(comment instanceof JsonString)
                                    mdObjectProperty.add("comment", comment);
                            }
                        /*JsonValue comment = objectProperty.get(Transformer.RDFS_COMMENT);
                        if(comment instanceof JsonString)
                            mdObjectProperty.add("comment", comment);
                        else if (comment instanceof JsonArray){
                            mdObjectProperty.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                        }*/
                            objectsPropertiesSet.put(objectProperty.getString(Constants.ID), mdObjectProperty.build());
                        }
                    }
                }
                JsonValue rangeIncludes = objectProperty.get(Constants.SCHEMA_RANGE_INCLUDES);
                if (rangeIncludes instanceof JsonObject){
                    String range = ((JsonObject)rangeIncludes).getString(Constants.ID);
                    if(range.equalsIgnoreCase(jsonObject.getString(Constants.ID))){
                        JsonObjectBuilder mdReferencedBy = Json.createObjectBuilder();
                        mdReferencedBy.add("uri", objectProperty.getString(Constants.ID));

                        JsonValue comment = objectProperty.get(Constants.RDFS_COMMENT);
                        if(comment instanceof JsonString) {
                            mdReferencedBy.add("comment", comment);
                        } else if (comment instanceof JsonArray){
                            mdReferencedBy.add("comment", getFirstNWords(comment.asJsonArray().getString(0)));
                        }
                        referencedBySet.put(objectProperty.getString(Constants.ID), mdReferencedBy.build());
                    }
                }
            }

            if (codeProperties.containsKey(jsonObject.getString(Constants.ID))){
                codeListsCommentsMap.put(jsonObject.getString(Constants.ID), jsonObject.getString(Constants.RDFS_COMMENT));
                codeListsReferrencedByMap.put(jsonObject.getString(Constants.ID), referencedBySet);
            }


            if (objectsPropertiesSet.size()>0){
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (JsonObject objectProperty:objectsPropertiesSet.values()){
                    arrayBuilder.add(objectProperty);
                }
                mdClass.add("objectProperties", arrayBuilder.build());
            }
            if (referencedBySet.size()>0){
                JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                for (JsonObject referencedBy:referencedBySet.values()){
                    arrayBuilder.add(referencedBy);
                }
                mdClass.add("referencedBy", arrayBuilder.build());
            } else {
                System.out.println(String.format("Class %s has no references", jsonObject.getString(Constants.ID)));
            }

            if (codeProperties.containsKey(jsonObject.getString(Constants.ID))) {
                continue;
            }
            new FileGenerator().generateFile(mdClass.build(), true, workingDir.concat("_data/").concat(outputFileName).concat(".json"));
            String mdContent = "---\n";
            mdContent = mdContent.concat(String.format("title: %s", jsonObject.getString(Constants.RDFS_LABEL))).concat("\n");
            mdContent = mdContent.concat(String.format("permalink: %s", StringUtils.substringAfter(jsonObject.getString(Constants.ID),":"))).concat(".html\n");
            mdContent = mdContent.concat(String.format("jsonid: %s", outputFileName.toLowerCase())).concat("\n");
            mdContent = mdContent.concat("---\n");
            new FileGenerator().generateTextFile(mdContent,workingDir.concat("_classes/").concat(outputFileName).concat(".md"));
        }
        List<String> allocatedByClassesKeys = new ArrayList<>();
        for (String dataTypeProperty:dataTypeProperties.keySet()){
            if (classNamesLowerCased.contains(dataTypeProperty.toLowerCase())){
                allocatedByClassesKeys.add(dataTypeProperty);
            }
        }
        for (String objectTypeProperty:objectsProperties.keySet()){
            if (classNamesLowerCased.contains(objectTypeProperty.toLowerCase())){
                allocatedByClassesKeys.add(objectTypeProperty);
            }
        }
        for (String allocated: allocatedByClassesKeys) {
            System.out.print(String.format("\"/%s\",", allocated.replace(":","/")));
        }
        System.out.println();
        /*List<String> allocatedByClassesKeys = Arrays.asList(new String[]{
                "appliedAllowanceCharge","appliedChemicalTreatment","appliedTax",
                "assertion","associatedTransportEquipment","availablePeriod",
                "binaryFile","calculatedPrice","deliveryInstructions",
                "deliverySchedule","document","guarantee",
                "handlingInstructions","haulageInstructions",
                "inspectionEvent","licence","location",
                "marking","note","package","quarantineInstructions",
                "radioactiveMaterial","range","section",
                "service","specifiedFault","specifiedLocation",
                "specifiedNote","specifiedParameter","specifiedPeriod",
                "specifiedRoute","standard","subordinateSubordinateLocation",
                "transportEvent","transportMeans"});*/

        for (JsonObject jsonObject:dataTypeProperties.values()) {
            JsonObjectBuilder batchObject = Json.createObjectBuilder();
            batchObject.add("type", "add");
            batchObject.add("id", dataSet.concat("_").concat(jsonObject.getString(Constants.ID)));
            JsonObjectBuilder batchFieldsObject = Json.createObjectBuilder();
            batchFieldsObject.add("label", jsonObject.getString(Constants.RDFS_LABEL));
            JsonValue classComment = jsonObject.get(Constants.RDFS_COMMENT);
            String commentStr = "";
            if(classComment instanceof JsonString){
                commentStr = ((JsonString) classComment).getString();
            } else if (classComment instanceof JsonArray) {
                JsonArray commentsArray = classComment.asJsonArray();
                for (JsonString comment:commentsArray.getValuesAs(JsonString.class)){
                    commentStr = commentStr.concat(comment.getString()).concat(" ");
                }

            }
            batchFieldsObject.add("comment", commentStr);
            batchFieldsObject.add("type", "Data Property");
            batchFieldsObject.add("dataset", dataSet);
            batchObject.add("fields", batchFieldsObject.build());
            batchArrayBuilder.add(batchObject);

            String permalink = jsonObject.getString(Constants.ID);
            if (allocatedByClassesKeys.contains(permalink)) {
                permalink = permalink.concat("Property");
            }
            permalink = StringUtils.substringAfter(permalink,":");
            String outputFileName = permalink.replaceAll("\\.","_").toLowerCase();

            JsonObjectBuilder mdProperty = Json.createObjectBuilder();

            mdProperty.add("label", jsonObject.getString(Constants.RDFS_LABEL));
            mdProperty.add("uri", jsonObject.getString(Constants.ID));
            mdProperty.add("comment", jsonObject.get(Constants.RDFS_COMMENT));

            JsonValue rangeIncludes = jsonObject.get(Constants.SCHEMA_RANGE_INCLUDES);
            JsonArrayBuilder mdRangeIncludes = Json.createArrayBuilder();
            if(rangeIncludes instanceof JsonObject) {
                JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                String uri = rangeIncludes.asJsonObject().getString(Constants.ID);
                if (codeProperties.containsKey(uri)){
                    rdfPropertiesWithCodevalues.add(jsonObject.getString(Constants.ID));
                }
                mdDomain.add("uri", uri);
                mdRangeIncludes.add(mdDomain.build());
            }
            mdProperty.add("rangeIncludes", mdRangeIncludes.build());
            JsonValue domainIncludes = jsonObject.get(Constants.SCHEMA_DOMAIN_INCLUDES);
            JsonArrayBuilder mdDomainIncludes = Json.createArrayBuilder();
            if(domainIncludes instanceof JsonObject) {
                JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                String uri = domainIncludes.asJsonObject().getString(Constants.ID);
                mdDomain.add("uri", uri);
                if (classesCommentsMap.containsKey(uri)) {
                    mdDomain.add("comment", classesCommentsMap.get(uri));
                } else {
                    if (classes.containsKey(uri)) {
                        System.err.println(String.format("Missing comment for %s", uri));
                        mdDomain.add("comment", "missing comment");
                    }else {
                        /*System.err.println(String.format("Missing class definition for %s", uri));*/
                        missingDefinitions.add(uri);
                    }
                }
                mdDomainIncludes.add(mdDomain.build());
            } else if (domainIncludes instanceof JsonArray) {
                Iterator<JsonValue> domainsIterator = ((JsonArray) domainIncludes).iterator();
                while (domainsIterator.hasNext()){
                    JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                    String uri = domainsIterator.next().asJsonObject().getString(Constants.ID);
                    mdDomain.add("uri", uri);
                    if (classesCommentsMap.containsKey(uri)) {
                        mdDomain.add("comment", classesCommentsMap.get(uri));
                    } else {
                        if (classes.containsKey(uri)) {
                            System.err.println(String.format("Missing comment for %s", uri));
                            mdDomain.add("comment", "missing comment");
                        }else {
                            /*System.err.println(String.format("Missing class definition for %s", uri));*/
                            missingDefinitions.add(uri);
                        }
                    }
                    mdDomainIncludes.add(mdDomain.build());
                }
            }
            mdProperty.add("domainIncludes", mdDomainIncludes.build());
            new FileGenerator().generateFile(mdProperty.build(), true, workingDir.concat("_data/").concat(outputFileName).concat(".json"));

            String mdContent = "---\n";
            mdContent = mdContent.concat(String.format("title: %s", jsonObject.getString(Constants.RDFS_LABEL))).concat("\n");
            mdContent = mdContent.concat(String.format("permalink: %s%s", "", permalink)).concat(".html\n");
            mdContent = mdContent.concat(String.format("jsonid: %s", outputFileName)).concat("\n");
            mdContent = mdContent.concat("---\n");
            new FileGenerator().generateTextFile(mdContent,workingDir.concat("_properties/").concat(outputFileName).concat(".md"));
        }
        for (JsonObject jsonObject:objectsProperties.values()) {
            JsonObjectBuilder batchObject = Json.createObjectBuilder();
            batchObject.add("type", "add");
            batchObject.add("id", dataSet.concat("_").concat(jsonObject.getString(Constants.ID)));
            JsonObjectBuilder batchFieldsObject = Json.createObjectBuilder();
            batchFieldsObject.add("label", jsonObject.getString(Constants.RDFS_LABEL));

            String permalink = jsonObject.getString(Constants.ID);
            if (allocatedByClassesKeys.contains(permalink)) {
                permalink = permalink.concat("Property");
            }
            permalink = StringUtils.substringAfter(permalink,":");
            String outputFileName = permalink.replaceAll("\\.","_").toLowerCase();

            JsonValue classComment = jsonObject.get(Constants.RDFS_COMMENT);
            String commentStr = "";
            if(classComment instanceof JsonString){
                commentStr = ((JsonString) classComment).getString();
            } else if (classComment instanceof JsonArray) {
                JsonArray commentsArray = classComment.asJsonArray();
                for (JsonString comment:commentsArray.getValuesAs(JsonString.class)){
                    commentStr = commentStr.concat(comment.getString()).concat(" ");
                }

            }
            batchFieldsObject.add("comment", commentStr);
            batchFieldsObject.add("type", "Object Property");
            batchFieldsObject.add("dataset", dataSet);
            batchObject.add("fields", batchFieldsObject.build());
            batchArrayBuilder.add(batchObject);

            String prefix = StringUtils.substringBefore(jsonObject.getString(Constants.ID), ":");
            JsonObjectBuilder mdProperty = Json.createObjectBuilder();
            String label = jsonObject.getString(Constants.RDFS_LABEL);
            if (allocatedByClassesKeys.contains(label)) {
                label = label.concat("Property");
            }
            mdProperty.add("label", jsonObject.getString(Constants.RDFS_LABEL));
            mdProperty.add("uri", jsonObject.getString(Constants.ID));
            mdProperty.add("comment", jsonObject.get(Constants.RDFS_COMMENT));

            JsonArrayBuilder mdRangeIncludes = Json.createArrayBuilder();
            JsonValue rangeIncludes = jsonObject.get(Constants.SCHEMA_RANGE_INCLUDES);
            if(rangeIncludes instanceof JsonObject) {
                JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                String uri = rangeIncludes.asJsonObject().getString(Constants.ID);
                mdDomain.add("uri", uri);
                if (classesCommentsMap.containsKey(uri))
                    mdDomain.add("comment", classesCommentsMap.get(uri));
                mdRangeIncludes.add(mdDomain.build());
            } else if (rangeIncludes instanceof JsonArray) {
                Iterator<JsonValue> domainsIterator = ((JsonArray) rangeIncludes).iterator();
                while (domainsIterator.hasNext()){
                    JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                    String uri = domainsIterator.next().asJsonObject().getString(Constants.ID);
                    mdDomain.add("uri", uri);
                    mdDomain.add("comment", classesCommentsMap.get(uri));
                    mdRangeIncludes.add(mdDomain.build());
                }
            }
            mdProperty.add("rangeIncludes", mdRangeIncludes.build());
            JsonArrayBuilder mdDomainIncludes = Json.createArrayBuilder();
            JsonValue domainIncludes = jsonObject.get(Constants.SCHEMA_DOMAIN_INCLUDES);
            if(domainIncludes instanceof JsonObject) {
                JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                String uri = domainIncludes.asJsonObject().getString(Constants.ID);
                mdDomain.add("uri", uri);
                if (classesCommentsMap.containsKey(uri)) {
                    mdDomain.add("comment", classesCommentsMap.get(uri));
                } else {
                    mdDomain.add("comment", "missing comment");
                }
                mdDomainIncludes.add(mdDomain.build());
            } else if (domainIncludes instanceof JsonArray) {
                Iterator<JsonValue> domainsIterator = ((JsonArray) domainIncludes).iterator();
                Set<String> domainsSet = new HashSet<>();
                while (domainsIterator.hasNext()){
                    JsonObjectBuilder mdDomain = Json.createObjectBuilder();
                    String uri = domainsIterator.next().asJsonObject().getString(Constants.ID);
                    if (domainsSet.contains(uri)){
                        //System.err.println(String.format("Domain %s is repeated for %s", uri, jsonObject.getString(Transformer.ID)));
                        missingDefinitions.add(jsonObject.getString(Constants.ID));
                    } else {
                        domainsSet.add(uri);
                    }
                    mdDomain.add("uri", uri);
                    if (classesCommentsMap.containsKey(uri)) {
                        mdDomain.add("comment", classesCommentsMap.get(uri));
                    } else {
                        mdDomain.add("comment", "missing comment");
                    }
                    mdDomainIncludes.add(mdDomain.build());
                }
            }
            mdProperty.add("domainIncludes", mdDomainIncludes.build());
            new FileGenerator().generateFile(mdProperty.build(), true, workingDir.concat("_data/").concat(outputFileName).concat(".json"));

            String mdContent = "---\n";
            mdContent = mdContent.concat(String.format("title: %s", jsonObject.getString(Constants.RDFS_LABEL))).concat("\n");
            mdContent = mdContent.concat(String.format("permalink: %s%s", "", permalink.concat(".html\n")));
            mdContent = mdContent.concat(String.format("jsonid: %s", outputFileName.toLowerCase())).concat("\n");
            mdContent = mdContent.concat("---\n");
            new FileGenerator().generateTextFile(mdContent,workingDir.concat("_properties/").concat(outputFileName).concat(".md"));
        }
        for (String codeListId:codeProperties.keySet()) {
            String outputFileName = StringUtils.substringAfter(codeListId, ":").replaceAll("\\.","_").toLowerCase();

            JsonObjectBuilder mdCodelist = Json.createObjectBuilder();
            String label = StringUtils.substringAfter(codeListId, ":");
            mdCodelist.add("label", label);
            mdCodelist.add("uri", codeListId);
            mdCodelist.add("comment", codeListsCommentsMap.get(codeListId));

            JsonObjectBuilder batchObject = Json.createObjectBuilder();
            batchObject.add("type", "add");
            batchObject.add("id", dataSet.concat("_").concat(codeListId));
            JsonObjectBuilder batchFieldsObject = Json.createObjectBuilder();
            batchFieldsObject.add("label", label);
            batchFieldsObject.add("comment", codeListsCommentsMap.get(codeListId));
            batchFieldsObject.add("type", "Code List");
            batchFieldsObject.add("dataset", dataSet);
            batchObject.add("fields", batchFieldsObject.build());
            batchArrayBuilder.add(batchObject.build());

            /*mdProperty.add("uri", jsonObject.getString(Transformer.ID));
            mdProperty.add("comment", jsonObject.get(Transformer.RDFS_COMMENT));*/
            JsonArrayBuilder mdCodeListValues = Json.createArrayBuilder();
            Map<String, JsonObject> valuesMap = new HashMap<>();
            for (JsonObject jsonObject:codeProperties.get(codeListId).values()) {
                JsonObjectBuilder mdCodeListValue = Json.createObjectBuilder();
                mdCodeListValue.add("uri",jsonObject.getString(Constants.ID));
                mdCodeListValue.add("comment",jsonObject.getString(Constants.RDFS_COMMENT));
                mdCodeListValue.add("value",jsonObject.getString(Constants.RDF_VALUE));
                valuesMap.put(jsonObject.getString(Constants.RDF_VALUE), mdCodeListValue.build());

                batchObject = Json.createObjectBuilder();
                batchObject.add("type", "add");
                batchObject.add("id", dataSet.concat("_").concat(jsonObject.getString(Constants.ID)));
                batchFieldsObject = Json.createObjectBuilder();
                batchFieldsObject.add("label", StringUtils.substringAfter(jsonObject.getString(Constants.ID), ":"));
                batchFieldsObject.add("comment", jsonObject.getString(Constants.RDFS_COMMENT));
                batchFieldsObject.add("type", "Code List Value");
                batchFieldsObject.add("dataset", dataSet);
                batchObject.add("fields", batchFieldsObject.build());
                batchArrayBuilder.add(batchObject.build());

            }
            List<String> valuesList = new ArrayList<>();
            valuesList.addAll(valuesMap.keySet());
            Collections.sort(valuesList);
            for (String key:valuesList){
                mdCodeListValues.add(valuesMap.get(key));
            }
            mdCodelist.add("values", mdCodeListValues.build());

            if (codeListsReferrencedByMap.containsKey(codeListId)) {
                TreeMap<String, JsonObject> referencedBySet = codeListsReferrencedByMap.get(codeListId);
                if (referencedBySet.size() > 0) {
                    JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
                    for (JsonObject referencedBy : referencedBySet.values()) {
                        arrayBuilder.add(referencedBy);
                    }
                    mdCodelist.add("referencedBy", arrayBuilder.build());
                } else {
                    System.out.println(String.format("Codelist %s has no references", codeListId));
                }
            }


            new FileGenerator().generateFile(mdCodelist.build(), true, workingDir.concat("_data/").concat(outputFileName).concat(".json"));

            String mdContent = "---\n";
            mdContent = mdContent.concat(String.format("title: %s", codeListId)).concat("\n");
            mdContent = mdContent.concat(String.format("permalink: %s", StringUtils.substringAfter(codeListId,":"))).concat(".html\n");
            mdContent = mdContent.concat(String.format("jsonid: %s", outputFileName.toLowerCase())).concat("\n");
            mdContent += "columns:\n";
            mdContent += "  - \n";
            mdContent += "    title: Comment\n";
            mdContent += String.format("    code: %s\n", "comment");
            mdContent += "  - \n";
            mdContent += "    title: Value\n";
            mdContent += String.format("    code: %s\n", "value");
            mdContent = mdContent.concat("---\n");
            new FileGenerator().generateTextFile(mdContent,workingDir.concat("_code-lists/").concat(outputFileName).concat(".md"));
        }

        JsonObjectBuilder rootObject = Json.createObjectBuilder();
        rootObject.add("batch", batchArrayBuilder.build());
        new FileGenerator().generateFile(rootObject.build(), false, workingDir.concat("batch-add").concat(".json"));

        combinedVocabulary.add("@context", context);
        combinedVocabulary.add("@graph", combinedGraphVocabulary.build());
        new FileGenerator().generateFile(combinedVocabulary.build(), true, workingDir.concat("unece").concat(".jsonld"));

        if (!missingDefinitions.isEmpty()) {
            for (String uri : missingDefinitions) {
                System.err.println(uri);
            }
        }
    }

    public static String getFirstNWords (String firstComment){
        String commentValue = "";
        int count = 0;
        for (String str: StringUtils.split(firstComment)){
            commentValue = commentValue.concat(str).concat(" ");
            count +=1;
            if (count> 4){
                commentValue = commentValue.concat("...");
                break;
            }
        }
        return commentValue;
    }

}

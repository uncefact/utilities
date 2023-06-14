package org.unece.uncefact.vocab;

import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObjectBuilder;

public class JSONLDContext implements FileEntity{
    protected JsonObjectBuilder contextObjectBuilder;

    protected String outputFile;

    protected boolean prettyPrint;

    public JSONLDContext() {
        contextObjectBuilder = Json.createObjectBuilder();
    }

    public JSONLDContext(String outputFile, boolean prettyPrint) {
        this();
        this.outputFile = outputFile;
        this.prettyPrint = prettyPrint;
    }

    public JsonObjectBuilder getContextObjectBuilder() {
        return contextObjectBuilder;
    }

    public void setContextObjectBuilder(JsonObjectBuilder contextObjectBuilder) {
        this.contextObjectBuilder = contextObjectBuilder;
    }

    public String getOutputFile() {
        return outputFile;
    }

    public void setOutputFile(String outputFile) {
        this.outputFile = outputFile;
    }

    public boolean isPrettyPrint() {
        return prettyPrint;
    }

    public void setPrettyPrint(boolean prettyPrint) {
        this.prettyPrint = prettyPrint;
    }
}

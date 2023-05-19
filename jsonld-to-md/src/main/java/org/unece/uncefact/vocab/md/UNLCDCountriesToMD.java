package org.unece.uncefact.vocab.md;

import org.unece.uncefact.vocab.FileGenerator;
import org.unece.uncefact.vocab.Constants;

import javax.json.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

public class UNLCDCountriesToMD {
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
            fis = new FileInputStream(workingDir.concat("/unlocode-countries.jsonld"));
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        JsonReader reader = Json.createReader(fis);
        JsonObject vocabulary = reader.readObject();
        reader.close();

        JsonArray graph = vocabulary.getJsonArray("@graph");

        Iterator<JsonValue> iterator = graph.iterator();
        String csvContent = "";
        csvContent = csvContent.concat(String.format("\"%s\",", "uri"));
        csvContent = csvContent.concat(String.format("\"%s\",", "label"));
        csvContent = csvContent.concat(String.format("\"%s\"\n", "value"));
        while (iterator.hasNext()) {
            JsonObject jsonObject = iterator.next().asJsonObject();
            csvContent = csvContent.concat(String.format("\"%s\",", jsonObject.getString(Constants.ID)));
            csvContent = csvContent.concat(String.format("\"%s\",", jsonObject.getString(Constants.RDFS_LABEL)));
            csvContent = csvContent.concat(String.format("\"%s\"\n", jsonObject.getString(Constants.RDF_VALUE)));
        }
        new FileGenerator().generateTextFile(csvContent,workingDir+"/_data/unlocode-countries.csv");

        String mdContent = "---\n";
        mdContent = mdContent.concat(String.format("title: %s", "UN/LOCODE Countries")).concat("\n");
        mdContent = mdContent.concat(String.format("permalink: %s", "unlocode-countries")).concat("\n");
        mdContent = mdContent.concat(String.format("jsonid: %s", "unlocode-countries")).concat("\n");
        mdContent = mdContent.concat(String.format("label: %s", "UN/LOCODE Countries")).concat("\n");
        mdContent = mdContent.concat(String.format("comment: %s", "desc")).concat("\n");
        mdContent = mdContent.concat("columns:\n");
        mdContent = mdContent.concat("  - \n");
        mdContent = mdContent.concat("    title: Label\n");
        mdContent = mdContent.concat(String.format("    code: %s\n", "label"));
        mdContent = mdContent.concat("  - \n");
        mdContent = mdContent.concat("    title: Value\n");
        mdContent = mdContent.concat(String.format("    code: %s\n", "value"));
        mdContent = mdContent.concat("  - \n");
        mdContent = mdContent.concat("    title: UN/LOCODEs\n");
        mdContent = mdContent.concat(String.format("    type: %s\n", "unlocode-link"));
        mdContent = mdContent.concat("---\n");
        new FileGenerator().generateTextFile(mdContent,workingDir+"/_code-lists/unlocode-countries.md");
        fis.close();
    }
}

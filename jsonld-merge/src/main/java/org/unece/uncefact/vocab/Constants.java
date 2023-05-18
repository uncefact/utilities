package org.unece.uncefact.vocab;

import java.util.*;

public abstract class Constants {

    public static String ID = "@id";
    public static String TYPE = "@type";
    public static String VALUE = "@value";
    public static String LANGUAGE = "@language";
    public static String OWL_NS = "owl";
    public static String RDFS_NS = "rdfs";
    public static String RDF_NS = "rdf";
    public static final String UNECE_NS = "unece";
    public static String CEFACT_NS = "cefact";
    public static String XSD_NS = "xsd";
    public static String RDFS_CLASS = RDFS_NS+":Class";
    public static String RDF_PROPERTY = RDF_NS+":Property";
    public static String RDF_SEQ = RDF_NS+":Seq";
    public static String RDF_VALUE = RDF_NS+":value";
    public static String RDFS_COMMENT = RDFS_NS+":comment";
    public static String RDFS_LABEL = RDFS_NS+":label";
    public static String SCHEMA_NS = "schema";
    public static String SCHEMA_DOMAIN_INCLUDES = SCHEMA_NS+":domainIncludes";
    public static String SCHEMA_RANGE_INCLUDES = SCHEMA_NS+":rangeIncludes";
    protected static Map<String, String> NS_MAP = new HashMap<>();

}

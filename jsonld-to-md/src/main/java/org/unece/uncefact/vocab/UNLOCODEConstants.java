package org.unece.uncefact.vocab;

import org.apache.commons.lang3.StringUtils;
import org.unece.uncefact.vocab.Constants;

public class UNLOCODEConstants extends Constants {

    protected static String FUNCTION_CLASS_NAME = "Function";
    protected static String FUNCTION_CLASS = StringUtils.join(UNLOCODE_VOCAB_NS, ":", FUNCTION_CLASS_NAME);
    public static final String FUNCTIONS_PROPERTY_NAME = "functions";
    public static String FUNCTIONS_PROPERTY = StringUtils.join(UNLOCODE_VOCAB_NS, ":", FUNCTIONS_PROPERTY_NAME);
    public static String SUBDIVISION_CLASS_NAME = "Subdivision";
    public static String SUBDIVISION_CLASS = StringUtils.join(UNLOCODE_VOCAB_NS, ":", SUBDIVISION_CLASS_NAME);
    public static final String COUNTRY_SUBDIVISION_PROPERTY_NAME = "countrySubdivision";
    public static String COUNTRY_SUBDIVISION_PROPERTY = StringUtils.join(UNLOCODE_VOCAB_NS, ":", COUNTRY_SUBDIVISION_PROPERTY_NAME);
    public final static String SUBDIVISION_TYPE_PROPERTY_NAME = "subdivisionType";
    public static String SUBDIVISION_TYPE_PROPERTY = StringUtils.join(UNLOCODE_VOCAB_NS, ":", SUBDIVISION_TYPE_PROPERTY_NAME);
    public static String COUNTRY_CLASS_NAME = "Country";
    public static String COUNTRY_CLASS = StringUtils.join(UNLOCODE_VOCAB_NS, ":", COUNTRY_CLASS_NAME);
    public static String UNLOCODE_CLASS_NAME = "UNLOCODE";
    public static String UNLOCODE_CLASS = StringUtils.join(UNLOCODE_VOCAB_NS, ":", UNLOCODE_CLASS_NAME);
    public final static String PROPERTY_COUNTRY_CODE_NAME = "countryCode";
    public static String PROPERTY_COUNTRY_CODE = StringUtils.join(UNLOCODE_VOCAB_NS, ":", PROPERTY_COUNTRY_CODE_NAME);

}

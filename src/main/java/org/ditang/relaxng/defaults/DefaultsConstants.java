package org.ditang.relaxng.defaults;

/**
 * Constants for RelaxNG default value component.
 */
public interface DefaultsConstants {

    /**
     * <p>The system property used to determine whether or not the parser will
     * validate the source document against the schema.</p>
     *
     * <p>Values "true" and "yes" indicate validation, anything else does not.</p>
     */
    String VALIDATION_PROPERTY = "dita-ng.validation";

    /**
     * <p>The configuration properties file name.</p>
     *
     * <p>If found on the class path, properties are loaded from the file.
     */
    String PROPERTIES_FILE = "dita-ng.properties";

    /**
     * <p>The short name of the {@link #VALIDATION_PROPERTY VALIDATION_PROPERTY}
     *  property when read from {@link #PROPERTIES_FILE PROPERTIES_FILE}.
     */
    String PROPERTIES_FILE_VALIDATION_PROPERTY = "validation";
}

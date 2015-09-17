package org.ditang.relaxng.defaults;

import org.ditang.relaxng.ConstantsForTests;
import org.junit.BeforeClass;

public class ParserConfigurationMixin implements ConstantsForTests,
                                                 DefaultsConstants
{

    @BeforeClass
    public static void setUp() {
        System.setProperty(VALIDATION_PROPERTY, "true");
        System.setProperty(XERCES_PARSER_CONFIG_PROPERTY,
                           RelaxDefaultsParserConfiguration.class.getName());
        // To avoid Jing's internal use of com.icl.saxon.IdentityTransformer
        System.setProperty(JAXP_TRANSFORMER_FACTORY,
                           SAXON_TRANSFORMER_FACTORY);
    }

}

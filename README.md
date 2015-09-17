# DITA-NG Relax NG support for DITA-OT

## Included plugins

There are currently 3 separate plugins which can be built:

* **org.dita-ng.library** | Adds Relax NG support to DITA-OT.
* **org.not-oasis-open.dita.v1_3-cs01** | Adds DITA 1.3 CS01 grammars to the DITA-OT catalog.
* **org.dita-ng.doctypes** | Adds Relax NG support and non-oasis DITA grammars as one plugin.

These plugins have associated ant targets:

```
ant dist
```

Runs conversion scripts to convert between grammar syntaxes, then
compiles the Relax NG support library and creates a zip archive
from the result, containing the plugin.

```
ant dist.minimal
```

Runs the following to ant targets.

```
ant dist.library
```

Creates a zip archive containing the Relax NG support library as a plugin.

```
ant dist.catalog
```

Creates a zip archive containing the DITA 1.3 CS01 grammars an catalogs as a plugin.

## Installation

The *dita* command included in DITA-OT versions 2.0 or greater can be used to install the generated plugins, e.g.:


```
cd x:\src\dita-ng
ant dist.minimal
x:\dita-ot\bin\dita -install dist\org.dita-ng.library-20150916.zip -v
x:\dita-ot\bin\dita -install dist\org.not-oasis-open.dita.v1_3-cs01-20150916.zip -v
```

## Testing the installation

Processing the DITA specification is one way to test the installation:

```
cd x:\dita-ot
set demo_dir=x:\dita-ot\plugins\org.not-oasis-open.dita.v1_3-cs01\demo\dita-1.3-specification
set ditamap=%demo_dir%\dita-1.3-specification-learningTraining.ditamap
set ditaval=%demo_dir%\resources\DITA1.3-spec-learningTraining.ditaval
bin\dita -v -f xhtml -i %ditamap% -f %ditaval -o out
out\index.html
```

## Schema validation

The Relax NG support provided by DITA-NG includes adding attribute default values to the parsed result. In addition, the DITA documents can be validated against the schema by adding a properties file to the plugin directory.

**Properties file**:

```
x:\dita-ot\plugins\org.dita-ng.library\dita-ng.properties
```

**Validation property**:

```
validation=yes
```

The default value is yes, if no configuration file is found. Any value other than "yes" or "true" is treated as "false".

### Why one would want to validate their documents

For the same reason that DITA-OT can validate documents against a DTD,
you might want to validate documents against a RelaxNG schema.

While most validation would be done during authoring and editting,
validation during production could be a useful safeguard.

Another scenario is that the DITA-OT process could be largely
invisible to the user as part of a CMS. Differences between the
authoring environment and the production system could be significant
enough that documents could appear valid in one environment and not
the other. Validating during publish could catch these differences.

## Associating a schema with a document

DITA-NG uses pseudo-attributes conforming to the W3C recommendation
"Associating Style Sheets with XML Documents".

The pseudo-attributes resemble XML attribute specifications, within
the data of a processing instruction. The attributes are:

* href - A system identifier.
* type - A MIME type (application/xml or application/relax-ng-compact-syntax)
* schematypens - The Relax NG XML syntax namespace: "http://relaxng.org/ns/structure/1.0". 

Example pseudo-attribute PIs identifying a RelaxNG schema in XML syntax:

```
<?xml-model href="specialized-concept.rng"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng" type="application/xml"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng" schematypens="http://relaxng.org/ns/structure/1.0"?>
```

Example pseudo-attribute PIs identifying a RelaxNG schema in compact syntax:

```
<?xml-model href="specialized-concept.rnc"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rnc"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rnc" type="application/relax-ng-compact-syntax"?>
```

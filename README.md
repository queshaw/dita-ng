# DITA-NG Relax NG support for DITA-OT

## Installation

A plugin archive can be created using the ant dist target, e.g.:

ant dist

Commands for invoking DITA-OT with relaxng support are provided:

**Unix**:

```
dita-ng
```

**Windows**:

```
dita-ng.cmd
```

The commands are wrappers around the "dita" command in DITA-OT 2.x.

If the command is installed in the DITA-OT bin directory, it can be used to install the plugin, e.g.:

```
/opt/dita-ot/bin/dita-ng -install dist/dita-ng20150914.zip -v
```

or

```
x:\dita-ot\bin\dita-ng -install dist\dita-ng20150914.zip -v
```


## Schema validation

The Relax NG support provided by DITA-NG includes adding attribute default values to the parsed result. In addition, the DITA documents can be validated against the schema by adding a properties file to the plugin directory.

**Properties file**:

```
$DITA_HOME/plugins/org.dita-ng.doctypes/dita-ng.properties
```

**Validation property**:

```
validation=yes
```

## Associating a schema with a document

DITA-NG uses pseudo-attributes conforming to the W3C recommendation "Associating Style Sheets with XML Documents".

The pseudo-attributes resemble XML attribute specifications, within the data of a processing instruction. The attributes are:

* href - A system identifier.
* type - A MIME type (application/xml or application/relax-ng-compact-syntax)
* schematypens - The Relax NG XML syntax namespace: "http://relaxng.org/ns/structure/1.0". 

Example pseudo-attribute PIs identifying a RelaxNG schema in XML syntax:

<?xml-model href="specialized-concept.rng"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng" type="application/xml"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rng" schematypens="http://relaxng.org/ns/structure/1.0"?>

Example pseudo-attribute PIs identifying a RelaxNG schema in compact syntax:

<?xml-model href="specialized-concept.rnc"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rnc"?>

<?xml-model href="urn:oasis:names:tc:dita:rng:concept.rnc" type="application/relax-ng-compact-syntax"?>

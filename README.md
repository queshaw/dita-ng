# DITA-NG Relax NG support for DITA-OT

## Installation

A plugin archive can be created using the ant dist target, e.g.:

ant dist

Commands for invoking DITA-OT with relaxng support are provided:

Unix:

dita-ng

Windows:

dita-ng.cmd

The commands are wrappers around the "dita" command in DITA-OT 2.x.

If the command is installed in the DITA-OT bin directory, it can be used to install the plugin, e.g.:

/opt/dita-ot/bin/dita-ng -install dist\dita-ng20150914.zip -v

## Schema validation

The Relax NG support provided by DITA-NG includes adding attribute default values to the parsed result. In addition, the DITA documents can be validated against the schema by adding a properties file to the plugin directory.

Properties file:

$DITA_HOME/plugins/org.dita-ng.doctypes/dita-ng.properties

Validation property:

validation=yes

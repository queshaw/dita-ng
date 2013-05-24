<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
  xmlns:xs="http://www.w3.org/2001/XMLSchema"
  xmlns:xd="http://www.oxygenxml.com/ns/doc/xsl"
  xmlns:rng="http://relaxng.org/ns/structure/1.0"
  xmlns:rnga="http://relaxng.org/ns/compatibility/annotations/1.0"
  xmlns:rng2ditadtd="http://dita.org/rng2ditadtd"
  xmlns:relpath="http://dita2indesign/functions/relpath"
  exclude-result-prefixes="xs xd rng rnga relpath"
  version="2.0">

  <xd:doc scope="stylesheet">
    <xd:desc>
      <xd:p>RNG to DITA DTD Converter</xd:p>
      <xd:p><xd:b>Created on:</xd:b> Feb 16, 2013</xd:p>
      <xd:p><xd:b>Authors:</xd:b> ekimber, pleblanc</xd:p>
      <xd:p>This transform takes as input RNG-format DITA document type
        shells and produces from them DTD-syntax vocabulary modules
        that reflect the RNG definitions and conform to the DITA 1.3
        DTD coding requirements.
      </xd:p>
      <xd:p>The primary output is a conversion manifest, which simply
        lists the files generated. Each module is generated separately
        using xsl:result-document.
      </xd:p>
    </xd:desc>
  </xd:doc>

  <xsl:include href="../lib/relpath_util.xsl" />
  <xsl:include href="rng2ditashelldtd.xsl"/>
  <xsl:include href="rng2ditaent.xsl" />
  <xsl:include href="rng2ditamod.xsl" />

  <xsl:output 
    method="xml"
    indent="yes"
  />

  <xsl:output name="dtd"
    method="text"
  />

  <xsl:param name="doDebug" as="xs:boolean" select="false()" />

  <xsl:strip-space elements="*"/>

  <xsl:template match="/">
    <!-- Construct a sequence of all the input modules so we can
         then process them serially, rather than in tree order.
         We have to do this because in XSLT you can't start a new
         result document while you're in the process of creating
         another result document.
      -->

    <xsl:variable name="modulesToProcess" as="document-node()*">
      <xsl:apply-templates mode="gatherModules" />
    </xsl:variable>

    <xsl:if test="$doDebug">
      <xsl:message>+ [DEBUG] Initial process: Found <xsl:sequence select="count($modulesToProcess)" /> modules.</xsl:message>
    </xsl:if>

    <rng2ditadtd:conversionManifest xmlns="http://dita.org/rng2ditadtd">
      <inputDoc><xsl:sequence select="document-uri(root(.))"></xsl:sequence></inputDoc>
    <xsl:choose>
      <xsl:when test="count(rng:grammar/*)=1 and rng:grammar/rng:include">
        <!--  simple redirection, as in technicalContent\glossary.dtd -->
          <redirectedTo>
            <xsl:value-of select="concat(substring-before(rng:grammar/rng:include/@href,'.rng'),'.dtd')" />
          </redirectedTo>
      </xsl:when>
      <xsl:otherwise>
          <generatedModules>
            <xsl:apply-templates select="$modulesToProcess" mode="generate-modules">
              <xsl:with-param name="rootDocUrl"
                select="document-uri(root(.))"
                tunnel="yes"
                as="xs:string"
              />
            </xsl:apply-templates>
          </generatedModules>
      </xsl:otherwise>
    </xsl:choose>

    <!-- Generate the .dtd file: -->
    <xsl:variable name="rngDtdUrl" as="xs:string"
      select="string(document-uri(root(.)))" />
    <xsl:variable name="rngDtdDir" as="xs:string"
      select="relpath:getParent($rngDtdUrl)" />
    <xsl:variable name="resultDir"
      select="replace($rngDtdDir, '/rng','/dtd')" />

    <xsl:variable name="dtdFilename" as="xs:string"
      select="concat(relpath:getNamePart($rngDtdUrl), '.dtd')" />
    <xsl:variable name="dtdResultUrl"
      select="relpath:newFile($resultDir, $dtdFilename)" />
    <dtdFile><xsl:sequence select="$dtdResultUrl" /></dtdFile>
    <xsl:result-document href="{$dtdResultUrl}" format="dtd">
      <xsl:apply-templates mode="dtdFile">
        <xsl:with-param name="dtdFilename" select="$dtdFilename" tunnel="yes" as="xs:string" />
        <xsl:with-param name="dtdDir" select="$rngDtdDir" tunnel="yes" as="xs:string" />
        <xsl:with-param name="modulesToProcess"  select="$modulesToProcess" tunnel="yes" as="document-node()*" />
      </xsl:apply-templates>
    </xsl:result-document>
    </rng2ditadtd:conversionManifest>

  </xsl:template>

  <xsl:template match="/" mode="generate-modules">
    <xsl:param name="rootDocUrl"
      tunnel="yes"
      as="xs:string"
    />

    <xsl:variable name="rngModuleUrl" as="xs:string"
      select="string(document-uri(.))"
    />

    <xsl:variable name="rootDocDir" as="xs:string"
      select="relpath:getParent($rootDocUrl)" />

    <xsl:variable name="rngModuleDir" as="xs:string"
      select="relpath:getParent($rngModuleUrl)" />
      
    <xsl:variable name="resultDir"
      select="replace($rngModuleDir, '/rng','/dtd')" />

    <!-- The RNG modules have two "extensions": .xxx.rng -->
    <xsl:variable name="rngModuleName" as="xs:string"
      select="relpath:getNamePart($rngModuleUrl)" />
    <xsl:variable name="entFilename" as="xs:string"
      select="concat(relpath:getNamePart($rngModuleName), '.ent')" />
    <xsl:variable name="modFilename" as="xs:string"
      select="concat(relpath:getNamePart($rngModuleName), '.mod')" />
    <xsl:variable name="entResultUrl"
      select="relpath:newFile($resultDir, $entFilename)" />
    <xsl:variable name="modResultUrl"
      select="relpath:newFile($resultDir, $modFilename)" />

    <!-- Generate the DTD files if and only if they are in the same folder as the root document -->
    <xsl:variable name="modRelativePath" as="xs:string"
      select="relpath:getRelativePath($rootDocDir,$rngModuleDir)" />
    <xsl:choose>
      <xsl:when test="not($modRelativePath='')">
        <moduleFiles>
          <inputFile><xsl:sequence select="$rngModuleUrl" /></inputFile>
          <xsl:comment> DTD generation skipped because module is in another folder.</xsl:comment>
        </moduleFiles>
      </xsl:when>
      <xsl:otherwise>
        <moduleFiles>
          <inputFile><xsl:sequence select="$rngModuleUrl" /></inputFile>
          <entityFile><xsl:sequence select="$entResultUrl" /></entityFile>
          <modFile><xsl:sequence select="$modResultUrl" /></modFile>
        </moduleFiles>
        <!-- Generate the .ent file: -->
        <xsl:result-document href="{$entResultUrl}" format="dtd">
          <xsl:apply-templates mode="entityFile">
            <xsl:with-param name="thisFile" select="$entResultUrl" tunnel="yes" as="xs:string" />
          </xsl:apply-templates>
        </xsl:result-document>
        <!-- Generate the .mod file: -->
        <xsl:result-document href="{$modResultUrl}" format="dtd">
          <xsl:apply-templates mode="moduleFile" >
            <xsl:with-param name="thisFile" select="$modResultUrl" tunnel="yes" as="xs:string" />
          </xsl:apply-templates>
        </xsl:result-document>
      </xsl:otherwise>
    </xsl:choose>

  </xsl:template>

  <!-- ==============================
       Gather Modules mode
       ============================== -->

  <xsl:template match="rng:grammar" mode="gatherModules">
    <xsl:apply-templates select="rng:include" mode="#current" />
  </xsl:template>

  <xsl:template match="rng:include" mode="gatherModules">
    <xsl:variable name="rngModule" as="document-node()?" select="document(@href, .)" />
    <xsl:choose>
      <xsl:when test="$rngModule">
        <xsl:if test="$doDebug">
          <xsl:message> + [DEBUG] Resolved reference to module <xsl:sequence select="string(@href)" /></xsl:message>
        </xsl:if>
        <xsl:sequence select="$rngModule" />
        <xsl:apply-templates mode="gatherModules" select="$rngModule" />
      </xsl:when>
      <xsl:otherwise>
        <xsl:message> - [ERROR] Failed to resolve reference to module <xsl:sequence select="string(@href)" /></xsl:message>
      </xsl:otherwise>
    </xsl:choose>
  </xsl:template>

  <!-- ==============================
       Other modes and functions
       ============================== -->

  <xsl:template match="text()" mode="#all" priority="-1" />

  <xsl:template match="rng:*" priority="-1" mode="entityFile">
    <xsl:message> - [WARN] entityFile: Unhandled RNG element <xsl:sequence select="concat(name(..), '/', name(.))" /></xsl:message>
  </xsl:template>

  <xsl:template match="rng:*" priority="-1" mode="moduleFile">
    <xsl:message> - [WARN] module: Unhandled RNG element <xsl:sequence select="concat(name(..), '/', name(.))" /><xsl:copy-of select="." /></xsl:message>
  </xsl:template>

</xsl:stylesheet>
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
      <xd:p><xd:b>Author:</xd:b> ekimber</xd:p>
      <xd:p>This transform takes as input RNG-format DITA document type
      shells and produces from them vocabulary module file
      that reflect the RNG definitions and conform to the DITA 1.3 
      DTD coding requirements.
      </xd:p>
    </xd:desc>
  </xd:doc>

  <xsl:output 
    method="xml" 
    indent="yes"
  />

  <xsl:output name="dtd"
    method="text"
    encoding="UTF-8"
  />


  <!-- ==============================
       .mod file generation mode
       ============================== -->

  <xsl:template mode="moduleFile" match="/">
    <xsl:apply-templates mode="#current" />
  </xsl:template>

  <xsl:template mode="moduleFile" match="rng:grammar">
    <xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    HEADER                                     -->
&lt;!-- ============================================================= -->
&lt;!--  MODULE:    DITA Delayed Resolution Domain                    -->
&lt;!--  VERSION:   1.2                                               -->
&lt;!--  DATE:      November 2009                                     -->
&lt;!--                                                               -->
&lt;!-- ============================================================= -->

&lt;!-- ============================================================= -->
&lt;!--                    PUBLIC DOCUMENT TYPE DEFINITION            -->
&lt;!--                    TYPICAL INVOCATION                         -->
&lt;!--                                                               -->
&lt;!--  Refer to this file by the following public identifier or an 
      appropriate system identifier 
PUBLIC "-//OASIS//ELEMENTS DITA Delayed Resolution Domain//EN"
      Delivered as file "delayResolutionDomain.mod"                -->

&lt;!-- ============================================================= -->
&lt;!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
&lt;!--                                                               -->
&lt;!-- PURPOSE:    Define elements and specialization attributes     -->
&lt;!--             for Delayed Resolution Domain                     -->
&lt;!--                                                               -->
&lt;!-- ORIGINAL CREATION DATE:                                       -->
&lt;!--             February 2008                                     -->
&lt;!--                                                               -->
&lt;!--             (C) Copyright OASIS Open 2008, 2009.              -->
&lt;!--             All Rights Reserved.                              -->
&lt;!--                                                               -->
&lt;!--  UPDATES:                                                     -->
&lt;!-- ============================================================= -->

&lt;!-- ============================================================= -->
&lt;!--                   ELEMENT NAME ENTITIES                       -->
&lt;!-- ============================================================= -->

</xsl:text>
    <xsl:apply-templates select="//rng:element" mode="generate-tagname-entities" />

    <xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    ELEMENT DECLARATIONS                       -->
&lt;!-- ============================================================= -->

    </xsl:text>

    <xsl:apply-templates select="rng:define" mode="generate-element-decls" />

    <xsl:text>
&lt;!-- ============================================================= -->
&lt;!--                    SPECIALIZATION ATTRIBUTE DECLARATIONS      -->
&lt;!-- ============================================================= -->
</xsl:text>

    <xsl:apply-templates select="rng:define" mode="generate-specialization-att-decls" />

  </xsl:template>

  <xsl:template mode="generate-tagname-entities" match="rng:element" priority="10">
    <xsl:text>&lt;!ENTITY % </xsl:text>
    <xsl:sequence select="string(@name)" />
    <xsl:text> "</xsl:text>
    <xsl:sequence select="string(@name)" />
    <xsl:text>" >&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template mode="generate-element-decls" 
    match="rng:define[ends-with(@name, '.content')]" priority="10">
    <!-- Element content model entity declaration -->
    <xsl:text>&lt;!ENTITY % </xsl:text>
    <xsl:sequence select="string(@name)" />
    <xsl:text>&#x0a;   "</xsl:text>
    <xsl:call-template name="constructContentModelGroup">
      <xsl:with-param name="sep" select="concat(',&#x0a;', '        ')" as="xs:string" />
      <xsl:with-param name="rep" select="''" as="xs:string" />
    </xsl:call-template>
    <xsl:text>&#x0a;"&gt;&#x0a;</xsl:text>
  </xsl:template>

  <xsl:template mode="moduleFile" match="rng:include" />

  <xsl:template mode="moduleFile" match="comment()">
    <xsl:text>&lt;!--</xsl:text>
    <xsl:sequence select="string(.)" />
    <xsl:text>-->&#x0a;</xsl:text>
  </xsl:template>

  <!-- ==============================
       generate-content-model mode
       ============================== -->

  <xsl:template mode="generate-content-model" match="rng:choice">
    <xsl:param name="sep" as="xs:string" />
    <xsl:sequence select="$sep" />
    <xsl:call-template name="constructContentModelGroup">
      <xsl:with-param name="sep" select="concat('|&#x0a;', '        ')" as="xs:string" />
      <xsl:with-param name="rep" select="''" as="xs:string" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="generate-content-model" match="rng:zeroOrMore">
    <xsl:param name="sep" as="xs:string" />
    <xsl:sequence select="$sep" />
    <xsl:call-template name="constructContentModelGroup">
      <xsl:with-param name="sep" select="concat('|&#x0a;', '        ')" as="xs:string" />
      <xsl:with-param name="rep" select="'*'" as="xs:string" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template mode="generate-content-model" match="rng:optional">
    <xsl:param name="sep" as="xs:string" />
    <xsl:sequence select="$sep" />
    <xsl:call-template name="constructContentModelGroup">
      <xsl:with-param name="sep" select="concat('|&#x0a;', '        ')" as="xs:string" />
      <xsl:with-param name="rep" select="'?'" as="xs:string" />
    </xsl:call-template>
  </xsl:template>

  <xsl:template name="constructContentModelGroup">
    <!-- FIXME: Refine logic for determining when a set of parens can be eliminated. -->
    <!-- Separator between items within this group -->
    <xsl:param name="sep" as="xs:string" />
    <!-- Repetition indicator -->
    <xsl:param name="rep" as="xs:string" select="''" />
    <!-- NOTE: any indention or line break is handled by the separator in the outer group, if any -->
    <xsl:if test="$rep != '' or child::rng:ref">
      <xsl:sequence select="'('" />
    </xsl:if>
    <xsl:apply-templates mode="generate-content-model" select="*[position() = 1]">
      <xsl:with-param name="sep" select="''" as="xs:string" />
    </xsl:apply-templates>
    <xsl:apply-templates mode="generate-content-model" select="*[position() > 1]">
      <xsl:with-param name="sep" select="$sep" as="xs:string" />
    </xsl:apply-templates>
    <xsl:if test="$rep != '' or child::rng:ref">
      <xsl:sequence select="concat(')', $rep)" />
    </xsl:if>
  </xsl:template>

  <xsl:template mode="generate-content-model" match="rng:text">
    <xsl:text>#PCDATA</xsl:text>
  </xsl:template>

  <xsl:template mode="generate-content-model" match="rng:ref">
    <xsl:param name="sep" as="xs:string" />
    <xsl:sequence select="concat($sep, '%', @name, ';')" />
  </xsl:template>

  <xsl:template mode="generate-content-model constructContentModelGroup" match="text()" />

</xsl:stylesheet>
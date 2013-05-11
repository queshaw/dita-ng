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
      shells and produces from them the entity file
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
       .ent file generation mode
       ============================== -->

  <xsl:template mode="entityFile" match="/">
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template mode="entityFile" match="comment()">
    <!-- Suppress comments in entityFile mode -->
  </xsl:template>

  <xsl:template mode="entityFile" match="rng:grammar">
    <xsl:text>&lt;?xml version="1.0" encoding="UTF-8"?>
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
PUBLIC "-//OASIS//ENTITIES DITA Delayed Resolution Domain//EN"
      Delivered as file "delayResolutionDomain.ent"                -->

&lt;!-- ============================================================= -->
&lt;!-- SYSTEM:     Darwin Information Typing Architecture (DITA)     -->
&lt;!--                                                               -->
&lt;!-- PURPOSE:    Declaring the substitution context and domain     -->
&lt;!--             entity declarations for the delayed resolution    -->
&lt;!--             domain                                            -->
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
&lt;!--                    DELAYED RESOLUTION DOMAIN ENTITIES         -->
&lt;!-- ============================================================= -->
</xsl:text>
    <xsl:apply-templates mode="#current" select="node()" />
  </xsl:template>

  <xsl:template mode="entityFile" match="*">
    <!-- Most stuff we don't care about -->
    <xsl:apply-templates mode="#current" />
  </xsl:template>

</xsl:stylesheet>
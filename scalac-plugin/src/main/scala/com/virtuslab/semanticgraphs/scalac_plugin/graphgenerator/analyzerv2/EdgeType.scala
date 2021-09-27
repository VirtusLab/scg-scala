package com.virtuslab.semanticgraphs.scalac_plugin.graphgenerator.analyzerv2

object EdgeType {
  val DECLARATION      = "DECLARATION"
  val RETURN_TYPE      = "RETURN_TYPE"
  val EXTEND           = "EXTEND"
  val PARAMETER        = "PARAMETER"
  val CALL             = "CALL"
  val TYPE             = "TYPE"
  val TYPE_PARAMETER   = "TYPE_PARAMETER"
  val TYPE_UPPER_BOUND = "TYPE_UPPER_BOUND"
  val OVERRIDE         = "OVERRIDE"
}

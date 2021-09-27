package com.virtuslab.semanticgraphs.scalac_plugin.graphgenerator.analyzerv2

import java.util
import java.util.function.Predicate
import java.util.stream.Collectors

import com.virtuslab.semanticgraphs.proto.model.GraphNodeOuterClass.{Edge, GraphNode, Location}

import scala.collection.JavaConverters
import scala.meta.internal.semanticdb.SymbolInformation
import scala.meta.internal.semanticdb.SymbolInformation.Kind.{LOCAL, METHOD}

class GraphAccumulator(val uri: String) {
  private val nodes: java.util.Map[String, GraphNode] = new util.HashMap[String, GraphNode]()

  def exportNodes(): java.util.Collection[GraphNode] =
    nodes.values()

  def addNode(graphNode: GraphNode): Unit =
    nodes.put(graphNode.getId, graphNode)

  def getNode(symbol: String): GraphNode =
    nodes.get(createId(symbol))

  def findNode(symbol: String): Option[GraphNode] =
    Option(nodes.get(createId(symbol)))

  def addEdge(symbol: String, edge: Edge): Unit = {
    val node = nodes.get(symbol)
    addNode(node.toBuilder.addEdges(edge).build())
  }

  def upsertEdge(parent: String, child: String, _type: String, location: Location): Unit = {
    def isSameAsCurrnet(edge: Edge, child: String, _type: String, location: Location) =
      edge.getTo == child && edge.getType == _type && (!edge.hasLocation || edge.getLocation().equals(location))

    val node = nodes.get(parent)
    val updatedEdges = node.getEdgesList
      .stream()
      .filter(new Predicate[Edge] {
        // remove nodes of same child and type and with empty location (as we want to update location)
        override def test(t: Edge): Boolean = !isSameAsCurrnet(t, child, _type, location)
      })
      .collect(Collectors.toList[Edge])
    updatedEdges.add(ProtoHelper.createEdge(child, _type, Some(location)))
    val updatedNode = node.toBuilder.clearEdges().addAllEdges(updatedEdges).build()
    addNode(updatedNode)
  }

  def createNode(
    symbolInformation: SymbolInformation,
    location: Option[Location],
    children: Seq[Edge] = Seq.empty,
    additionalProperties: Map[String, String] = Map.empty
  ): GraphNode = {

    val displayName      = symbolInformation.displayName
    val id               = createId(symbolInformation.symbol)
    val extractedPackage = extractPackage(symbolInformation)
    val kind             = extractKind(symbolInformation)

    val graphNodeBuilder = GraphNode
      .newBuilder()
      .setId(id)
      .setKind(kind)

    if (location.isDefined) graphNodeBuilder.setLocation(location.get)

    graphNodeBuilder.setDisplayName(displayName)

    val properties = new util.HashMap[String, String]()
    properties.put("symbol", symbolInformation.symbol)
    properties.put("displayName", displayName)
    properties.put("package", extractedPackage)
    properties.put("isLocal", symbolInformation.isLocal.toString)
    properties.put("kind", kind)
    properties.put("uri", uri)
    properties.put("startLine", location.map(_.getStartLine).getOrElse(0).toString)
    properties.put("startCharacter", location.map(_.getStartCharacter).getOrElse(0).toString)
    properties.put("endLine", location.map(_.getEndLine).getOrElse(0).toString)
    properties.put("endCharacter", location.map(_.getEndCharacter).getOrElse(0).toString)
    properties.put("access", symbolInformation.access.toString)
    properties.put("isImplicit", symbolInformation.isImplicit.toString)
    properties.put("isFinal", symbolInformation.isFinal.toString)
    properties.put("isAbstract", symbolInformation.isAbstract.toString)
    properties.put("isVar", symbolInformation.isVar.toString())
    properties.put("isVal", symbolInformation.isVal.toString())
    additionalProperties.foreach { case (key, value) => properties.put(key, value) }
    graphNodeBuilder.putAllProperties(properties)

    import JavaConverters._
    graphNodeBuilder.addAllEdges(children.asJava)

    val graphNode = graphNodeBuilder.build()
    addNode(graphNode)
    graphNode
  }

  def createAggregationNode(
    symbol: String,
    displayName: String,
    kind: String,
    location: Location,
    children: Seq[Edge] = Seq.empty,
    additionalProperties: Map[String, String] = Map.empty
  ): GraphNode = {

    val id = createId(symbol)

    val graphNodeBuilder = GraphNode
      .newBuilder()
      .setId(id)
      .setKind(kind)

    graphNodeBuilder.setLocation(location)

    graphNodeBuilder.setDisplayName(displayName)

    val properties = new util.HashMap[String, String]()
    properties.put("symbol", id)
    properties.put("displayName", displayName)
    //properties.put("package", extractPackage(id)) TODO (aggregation nodes are missing packages)
    properties.put("kind", kind)
    properties.put("uri", uri)
    properties.put("startLine", location.getStartLine.toString)
    properties.put("startCharacter", location.getStartCharacter.toString)
    properties.put("endLine", location.getEndLine.toString)
    properties.put("endCharacter", location.getEndCharacter.toString)
    additionalProperties.foreach { case (key, value) => properties.put(key, value) }
    graphNodeBuilder.putAllProperties(properties)

    import JavaConverters._
    graphNodeBuilder.addAllEdges(children.asJava)

    val graphNode = graphNodeBuilder.build()
    addNode(graphNode)
    graphNode
  }

  def addNodeProperties(node: GraphNode, properties: Map[String, String]): GraphNode = {
    val newNodeBuilder = node.toBuilder
    properties.foreach { case (key, value) => newNodeBuilder.putProperties(key, value) }
    val newNode = newNodeBuilder.build()
    nodes.put(node.getId, newNode)
    newNode
  }

  def createId(symbol: String): String =
    if (symbol.startsWith("local")) symbol + ":" + uri else symbol

  private def extractPackage(symbolInformation: SymbolInformation): String = {
    extractPackage(symbolInformation.symbol)
  }

  private def extractPackage(symbol: String): String = {
    val fragments = symbol.split("/")
    fragments.take(fragments.size - 1).mkString(".")
  }

  def extractKind(symbolInformation: SymbolInformation): String = symbolInformation.kind match {
    case METHOD | LOCAL if symbolInformation.isVal => "VALUE"
    case METHOD | LOCAL if symbolInformation.isVar => "VARIABLE"
    case other                                     => other.name
  }

}

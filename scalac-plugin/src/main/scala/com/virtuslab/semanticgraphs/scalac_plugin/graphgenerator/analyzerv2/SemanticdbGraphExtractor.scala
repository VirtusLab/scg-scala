package com.virtuslab.semanticgraphs.scalac_plugin.graphgenerator.analyzerv2

import com.typesafe.scalalogging.Logger
import com.virtuslab.semanticgraphs.proto.model.GraphNodeOuterClass.{Edge, Location}

import scala.meta.internal.semanticdb.Type.Empty
import scala.meta.internal.semanticdb.{
  AnnotatedType,
  ClassSignature,
  ConstantType,
  ExistentialType,
  MethodSignature,
  RepeatedType,
  SingleType,
  StructuralType,
  SuperType,
  SymbolInformation,
  ThisType,
  Type,
  TypeRef,
  TypeSignature,
  UnionType,
  UniversalType,
  ValueSignature,
  WithType
}

class SemanticdbGraphExtractor(graphAccumulator: GraphAccumulator, semanticdbHelper: SemanticdbHelper) {

  val logger = Logger(classOf[SemanticdbGraphExtractor])

  def createInitialGraphBasedOnSemanticDB(): Unit = {
    val textDocument = semanticdbHelper.textDocument
    textDocument.symbols.foreach(extract)
  }

  def extract(symbolInformation: SymbolInformation): Unit = {
    val parentId = symbolInformation.symbol

    val edges = symbolInformation.signature match {
      case classSignature: ClassSignature =>
        extractEdgesFromClassSignature(classSignature, parentId)
      case methodSignature: MethodSignature =>
        extractEdgesFromMethodSignature(symbolInformation, methodSignature, parentId)
      case valueSignature: ValueSignature =>
        extractEdgesFromType(
          valueSignature.tpe,
          EdgeType.TYPE,
          parentId
        )
      case typeSignature: TypeSignature =>
        extractEdgesFromType(typeSignature.upperBound, EdgeType.TYPE_PARAMETER, parentId) ++
          extractEdgesFromType(typeSignature.lowerBound, EdgeType.TYPE_PARAMETER, parentId) ++
          createEdges(typeSignature.typeParameters.toSeq.flatMap(_.symlinks), EdgeType.TYPE_PARAMETER)
      case s => logger.debug(s"Not supported signature $s"); Seq.empty[Edge]
    }

    val annotationsEdges = symbolInformation.annotations.flatMap { annotation =>
      extractEdgesFromType(annotation.tpe, "ANNOTATED", parentId)
    }

    val overrides =
      symbolInformation.overriddenSymbols.map(overridenSymbol => createEdge(overridenSymbol, EdgeType.OVERRIDE))
    graphAccumulator.createNode(
      symbolInformation,
      extractLocation(symbolInformation.symbol),
      annotationsEdges ++ edges ++ overrides
    )
  }

  private def extractEdgesFromClassSignature(classSignature: ClassSignature, parentId: String): Seq[Edge] = {
    val declarationsEdges = classSignature.declarations
      .map { declaration =>
        createEdges(declaration.symlinks, EdgeType.DECLARATION)
      }
      .getOrElse(Seq.empty)

    val parametersEdges = classSignature.typeParameters
      .map(typeParameters => createEdges(typeParameters.symlinks, EdgeType.TYPE_PARAMETER))
      .getOrElse(Seq.empty)

    val extendsEdges = classSignature.parents.flatMap(extractEdgesFromType(_, EdgeType.EXTEND, parentId))

    declarationsEdges ++ parametersEdges ++ extendsEdges
  }

  private def extractEdgesFromMethodSignature(
    symbolInformation: SymbolInformation,
    methodSignature: MethodSignature,
    parentId: String
  ): Seq[Edge] = {

    val parametersEdges =
      methodSignature.parameterLists.flatMap(parameters => createEdges(parameters.symlinks, EdgeType.PARAMETER))
    val returnTypeEdges = extractEdgesFromType(
      methodSignature.returnType,
      if (symbolInformation.isVar || symbolInformation.isVal) EdgeType.TYPE else EdgeType.RETURN_TYPE,
      parentId
    )
    parametersEdges ++ returnTypeEdges
  }

  private def extractEdgesFromType(_type: Type, role: String, parentId: String): Seq[Edge] = _type match {
    case SingleType(prefix, symbol) =>
      Seq(createEdge(symbol, role))
    case TypeRef(prefix, symbol, typeArguments) =>
      createEdge(symbol, role) +: typeArguments.flatMap(extractEdgesFromType(_, role, parentId))
    case ExistentialType(tpe, declarations) =>
      extractEdgesFromType(tpe, role, parentId) ++ declarations
        .map(scope =>
          scope.symlinks.map { link =>
            createEdge(link, role)
          }
        )
        .getOrElse(Seq.empty)
    case AnnotatedType(annotations, tpe) =>
      annotations.map(_.tpe).flatMap(extractEdgesFromType(_, role, parentId)) ++ extractEdgesFromType(
        tpe,
        role,
        parentId
      )
    case Empty             => Seq.empty
    case RepeatedType(tpe) => extractEdgesFromType(tpe, role, parentId)
    case StructuralType(tpe, declarations) =>
      extractEdgesFromType(tpe, role, parentId) ++ declarations
        .map(scope =>
          scope.symlinks.map { link =>
            createEdge(link, role)
          }
        )
        .getOrElse(Seq.empty)
    case ConstantType(constant) => //TODO?
      Seq.empty
    case ThisType(symbol) =>
      Seq(createEdge(symbol, role))
    case SuperType(prefix, symbol) =>
      Seq(createEdge(symbol, role))
    case WithType(types) =>
      types.flatMap(extractEdgesFromType(_, role, parentId))
    case UnionType(types) =>
      types.flatMap(extractEdgesFromType(_, role, parentId))
    case UniversalType(typeParameters, tpe) =>
      extractEdgesFromType(tpe, role, parentId)
    case ref =>
      logger.debug(s"Not supported type in $role - $ref") //TODO implement the rest
      Seq.empty
  }

  def createEdges(symlinks: Seq[String], _type: String): Seq[Edge] =
    symlinks.map(createEdgeForDefinition(_, _type))

  def createEdge(child: String, _type: String) = {
    val childId = graphAccumulator.createId(child)
    ProtoHelper.createEdge(childId, _type, None)
  }

  def createEdgeForDefinition(symbol: String, _type: String): Edge = {
    val childId = graphAccumulator.createId(symbol)
    ProtoHelper.createEdge(childId, _type, extractLocation(symbol))
  }

  private def extractLocation(symbol: String): Option[Location] = {
    val occurrence = semanticdbHelper.findOccurence(symbol)
    occurrence.flatMap(_.range).map { r =>
      ProtoHelper.createLocation(graphAccumulator.uri, r.startLine, r.startCharacter, r.endLine, r.endCharacter)
    }
  }

}

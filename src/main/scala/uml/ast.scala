package uml

import plantuml.SimplePlantUMLPrettyPrinter
import scalameta.util.namespaces.{DefaultNamespace, Entry}

import scala.meta.Stat

/**
 * @todo will be used later to define operations on all nodes
 */
sealed trait UMLElement { self =>

  def pretty : String = SimplePlantUMLPrettyPrinter.format(self).layout
  def structure : String

  protected[this] def listStructure[T <: UMLElement](umlElements:List[T]):String =
    s"""List(${umlElements.map(_.structure).mkString(",")})"""

  protected[this] def optionString(os:Option[String]):String = {
    os match {
      case Some(value) => s"""Some("$value")"""
      case None => "None"
    }
  }

  protected[this] def optionUMLElement(oElement:Option[UMLElement]):String =  oElement match {
    case Some(value) => s"""Some(${value.structure})"""
    case None => "None"
  }

  protected[this] def optionAny[T](oA:T):String = oA match {
    case Some(value) => s"""Some(${value.toString})"""
    case None => "None"
  }
}

sealed case class TaggedValue(name:String,value:Option[String]) extends UMLElement {
  override def structure: String = s"""TaggedValue("$name","${optionString(value)}")"""
}
sealed case class Stereotype(name:String,taggedValues:List[TaggedValue]) extends UMLElement {
  override def structure: String = s"""Stereotype("$name",${listStructure(taggedValues)})"""
}

trait StereotypeElement extends UMLElement {
  val stereotype: List[Stereotype]
}

sealed trait TopLevelElement extends UMLElement

sealed trait CompartmentElement extends UMLElement

sealed trait PackageBodyElement extends UMLElement

sealed trait RelateableElement extends UMLElement

sealed trait NamedElement extends UMLElement {
  val identifier : String
  val namespace : Entry = DefaultNamespace
}

sealed case class UMLUnit(identifier:String,
                          toplevelElements:List[TopLevelElement]) extends UMLElement {
  override def structure: String = s"""UMLUnit("$identifier",${listStructure(toplevelElements)})"""
}

/***************
 * Packages
 **************/

sealed case class Package(identifier:String,
                          packageBodyElements:List[PackageBodyElement],
                          stereotype:List[Stereotype],
                          override val namespace:Entry=DefaultNamespace) extends
  TopLevelElement with
  PackageBodyElement with
  StereotypeElement with
  NamedElement with
  RelateableElement {
  override def structure: String =
    s"""Package("$identifier",${listStructure(packageBodyElements)},${listStructure(stereotype)}))"""
}

/***************
 * Classes
 **************/

sealed case class GenericParameter(identifier:String,
                                   concreteType:Option[String],
                                   stereotype:List[Stereotype]) extends
  StereotypeElement with
  NamedElement {
  override def structure: String =
    s"""GenericParameter("$identifier",${optionString(concreteType)},${listStructure(stereotype)})"""
}

sealed trait AccessModifier
case object Private extends AccessModifier
case object Protected extends AccessModifier
case object PackagePrivate extends AccessModifier
case object Public extends AccessModifier

sealed trait Modificator
case object Static extends Modificator
case object Abstract extends Modificator

object externalReferences {

  sealed trait ClassType
  case object Trait extends ClassType
  case object Enum extends ClassType
  case object Object extends ClassType
  case object CClass extends ClassType
  case object CCaseClass extends ClassType

  sealed case class ClassDefRef(classtype:ClassType,
                                name:String,namespace:Entry,
                                templateParameter:List[String],
                                oStat:Option[Stat] = None)
    extends TopLevelElement {
    override def structure: String =
      s"""ClassDefRef($classtype,"$name",$namespace,List(${templateParameter.mkString(",")}))"""
  }


}


sealed case class Class(isAbstract:Boolean,
                        identifier:String,
                        attributes:List[Attribute],
                        operations:List[Operation],
                        additionalCompartements:List[Compartment],
                        genericParameters: Option[List[GenericParameter]],
                        stereotype : List[Stereotype],
                        override val namespace : Entry = DefaultNamespace) extends
  TopLevelElement with
  StereotypeElement with
  PackageBodyElement with
  RelateableElement with
  NamedElement {
  override def structure: String =
    s"""Class($isAbstract,"$identifier",${
      listStructure(attributes)},${
      listStructure(operations)},${
      listStructure(additionalCompartements)},${
      if(genericParameters.map(listStructure).isDefined){
        "Some(" + genericParameters.map(listStructure).get + ")"
      } else {"None"}},${
      listStructure(stereotype)
    })""".stripMargin
}
/***************
 * Attributes
 **************/

sealed case class Attribute(modificators:Option[List[Modificator]],
                            modifier: Option[AccessModifier],
                            identifier:String,
                            attributeType:Option[String],
                            stereotype:List[Stereotype],
                            defaultValue:Option[String] = None) extends
  CompartmentElement with
  StereotypeElement with
  NamedElement {
  override def structure: String = s"""Attribute(${if(modificators.isDefined){
    s"""Some(${modificators.get.map(_.toString).mkString(",")})"""
  } else "None"},${
    optionAny(modifier)
  },"$identifier",${
    optionString(attributeType)},${listStructure(stereotype)})"""
}

/***************
 * Operations
 **************/

sealed case class Parameter(identifier:String,
                            paramType:String,
                            stereotype:List[Stereotype]) extends
  StereotypeElement with
  NamedElement {
  override def structure: String = s"""Parameter("$identifier","$paramType",${listStructure(stereotype)})"""
}


sealed case class Operation(modificator: Option[List[Modificator]],
                            accessModifier: Option[AccessModifier],
                            identifier:String,
                            paramSeq:List[List[Parameter]],
                            returnType:Option[String],
                            stereotype:List[Stereotype],
                            templateParameter:Option[List[GenericParameter]] = None) extends
  CompartmentElement  with
  StereotypeElement with
  NamedElement {
  override def structure: String = s"""Operation(${
    modificator.map(m => s"""Some(List(${m.toString.mkString(",")}))""").getOrElse("None")
  },${optionAny(accessModifier)},"$identifier",${
    if(paramSeq.isEmpty || paramSeq.head.isEmpty){"List(List())"} else {paramSeq.map(seq => s"""List(${seq.map(_.structure).mkString(",")})""")}
  },${optionString(returnType)},${listStructure(stereotype)})"""
}


sealed case class Compartment(identifier:Option[String],
                              taggedValues:List[TaggedValue],
                              stereotype:List[Stereotype]) extends
  UMLElement with
  StereotypeElement {
  override def structure: String =
    s"""Compartment("$identifier",${listStructure(taggedValues)},${listStructure(stereotype)}"""
}

/**
 * Corresponds to a UML Note.
 *
 **/
 sealed case class Note(attachedElements:List[NamedElement],
                        text:String,
                        stereotype:List[Stereotype]) extends
  TopLevelElement with
  StereotypeElement with
  PackageBodyElement {
  override def structure: String = s"""Note(${listStructure(attachedElements)},"$text",${listStructure(stereotype)})"""
}

/***************
 * Relationships
 **************/

sealed trait RelationshipType
case object Extension extends RelationshipType
case object Composition extends RelationshipType
case object Aggregation extends RelationshipType
case object Annotation extends RelationshipType
case object Association extends RelationshipType
case object Inner extends RelationshipType

sealed trait RelationshipDirection
case object FromTo extends RelationshipDirection
case object ToFrom extends RelationshipDirection
case object Without extends RelationshipDirection

sealed trait RelationshipElement extends UMLElement
sealed case class ConcreteClass(cls:RelateableElement with NamedElement) extends RelationshipElement {
  override def structure: String = s"ConcreteClass(${cls.structure})"
}
sealed case class ClassRef(name:String, namespace:Entry=DefaultNamespace) extends RelationshipElement {
  override def structure: String = s"ClassRef($name,${namespace.plantUML})"
}

sealed case class RelationshipInfo(sourceMultiplicity:Option[String],
                                   targetMultiplicity:Option[String],
                                   from: RelationshipElement,
                                   to: RelationshipElement,
                                   relationshipIdentifier:Option[String],
                                   identifierDirection:RelationshipDirection) extends UMLElement {
  def structure : String =
    s"""RelationshipInfo(${optionString(sourceMultiplicity)},${optionString(targetMultiplicity)},${
      from.structure},${to.structure},${optionString(relationshipIdentifier)},${identifierDirection.toString})"""
}

sealed case class Relationship(relationshipType: RelationshipType,
                               relationshipDirection: RelationshipDirection,
                               relationshipInfo: RelationshipInfo,
                               stereotype:List[Stereotype]) extends
  TopLevelElement with
  PackageBodyElement with
  StereotypeElement {
  override def structure: String =
    s"""Relationship(${relationshipType.toString},${relationshipDirection.toString},${
      relationshipInfo.structure},${listStructure(stereotype)})"""
}

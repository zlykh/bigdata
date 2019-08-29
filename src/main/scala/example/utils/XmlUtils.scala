package example.utils

import scala.xml.{Elem, NodeSeq}

object XmlUtils extends Serializable {

  implicit class NodeSeq2(val node: NodeSeq) {
    def attr(attr: String): String = (node \ ("@" + attr)).toString()
    def attrMap(): Map[String, String] = node.head.asInstanceOf[Elem].attributes.asAttrMap
  }

  def textToRootNode(text: String): Elem = {
    //    val xml: Elem = scala.xml.XML.loadFile("p.xml")
    val patentXml = text.split(",", 2)(1)
    scala.xml.XML.loadString(patentXml)
  }

}

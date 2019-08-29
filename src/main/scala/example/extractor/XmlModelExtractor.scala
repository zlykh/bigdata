package example.extractor

import example.model.{Abstrct, Inventor, PatentMeta, Title}
import example.utils.XmlPatentAttr._
import example.utils.XmlPatentTag._
import example.utils.XmlUtils._

import scala.xml.Elem

object XmlModelExtractor {
   def countryFromUcid(ucid: String): String = {
    val split = ucid.split("-")
    if (split.length == 3 && split(0).length == 2) {
      return split(0)
    }
    null
  }

  def extractMeta(root: Elem): PatentMeta = {
    val ucid = root.attr(ucidAttr)
    val country = countryFromUcid(ucid)

    val pubid = root.attr(pubidAttr).toInt
    val kind = root \ bibliographicDataTag \ applicationRefTag \ documentIdTag \ kindTag

    val dateFormat = new java.text.SimpleDateFormat("yyyyMMdd")
    val pubDateNode = root \ bibliographicDataTag \ publicationRefTag \ documentIdTag \ dateTag
    val fillDateNode = root \ bibliographicDataTag \ applicationRefTag \ documentIdTag \ dateTag
    val pubDate = new java.sql.Date(dateFormat.parse(pubDateNode.text).getTime)
    val fillDate = new java.sql.Date(dateFormat.parse(fillDateNode.text).getTime)

    PatentMeta(ucid, pubid, kind.text, country, pubDate, fillDate)
  }

  def extractAbstracts(root: Elem): Seq[Abstrct] = {
    val ucid = root.attr(ucidAttr)
    val abstractNode = root \ abstractTag
    abstractNode.map(node => Abstrct(ucid, node.text, node.attr(loadSourceTag)))
  }

  def extractTitles(root: Elem): Seq[Title] = {
    val ucid = root.attr(ucidAttr)
    val titleNode = root \ bibliographicDataTag \ technicalDataTag \ inventionTitleTag
    titleNode.map(node => Title(ucid, node.text, node.attr(loadSourceTag)))
  }


  def extractInventors(root: Elem): Seq[Inventor] = {
    val ucid = root.attr(ucidAttr)
    val inventorsNode = root \ bibliographicDataTag \ partiesTag \ inventorsTag
    inventorsNode.flatMap(node => node \ inventorTag)
      .map(inventorNode => {
        val loadSource = inventorNode.attr(loadSourceTag)
        val format = inventorNode.attr(formatAttr)
        val node = inventorNode \ addressBookTag
        val last = (node \ lastNameTag).text.trim.replaceAll("[!@#$%^&*()<>,.:;'\"]", "").toUpperCase
        val first = (node \ firstNameTag).text.trim.replaceAll("[!@#$%^&*()<>,.:;'\"]", "").toUpperCase
        Inventor(ucid, (last + " " + first).trim, loadSource, format)
      })
      .distinct
  }
}

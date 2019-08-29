package example.model

import java.sql.Date

case class PatentMeta(ucid: String,
                      pubid: Int,
                      kind: String,
                      country: String,
                      pubDate: Date,
                      fillDate: Date) extends UcidEntity
package example

import example.utils.HDFSConst._
import org.apache.spark.sql._
import org.apache.spark.sql.functions._
import org.apache.log4j._
import example.extractor.XmlModelExtractor._
import example.utils.XmlUtils._

object Main extends App {
  Logger.getLogger("org").setLevel(Level.ERROR)

  val spark = org.apache.spark.sql.SparkSession
    .builder()
    .appName("sparkSql")
    //for windows run
//    .master("local[*]")
//    .config("spark.sql.warehouse.dir", "file:///C:/temp")
    .getOrCreate()

  import spark.implicits._

  val rawTexts = spark.sparkContext.textFile(rawXmlDir + rawFileName)
  val rootNodes = rawTexts.map(textToRootNode)

  val meta = rootNodes.map(extractMeta)
  val titles = rootNodes.map(extractTitles)
  val abstracts = rootNodes.map(extractAbstracts)
  val inventors = rootNodes.map(extractInventors)

  var metaDF = meta.toDF()
  var titlesDF = titles.flatMap(t => t).toDF()
  var abstractsDF = abstracts.flatMap(t => t).toDF()
  var inventorsDF = inventors.flatMap(t => t).toDF()

  /* save patents after sqoop*/


  metaDF.write.parquet(parquetDir + metaFileName)
  titlesDF.write.parquet(parquetDir + titlesFileName)
  abstractsDF.write.parquet(parquetDir + abstractsFileName)
  inventorsDF.write.parquet(parquetDir + inventorsFileName)

  //save last processed mxw-id
  metaDF.agg(max("pubid") as "last-value")
    .write
    .mode("overwrite")
    .format("csv")
    .save(lastValDir)


  /* read patents after sqoop

  metaDF = spark.read.parquet(parquetDir + metaFileName)
  titlesDF = spark.read.parquet(parquetDir + titlesFileName)
  abstractsDF = spark.read.parquet(parquetDir + abstractsFileName)
  inventorsDF = spark.read.parquet(parquetDir + inventorsFileName)
*/


  spark.stop()
}
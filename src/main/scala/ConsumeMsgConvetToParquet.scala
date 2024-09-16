import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions._
import org.apache.spark.sql.types._
import org.apache.spark.sql.streaming.{Trigger, StreamingQueryException}
import java.time.format.DateTimeFormatter
import java.time.LocalDateTime
import org.apache.spark.sql.kafka010._

object ConsumeMsgConvetToParquet {
  def main(args: Array[String]): Unit = {
    // Inicializar sessão Spark
    val spark = SparkSession.builder
      .appName("Taxi Data Consumer")
      .master("local[*]") // Execute localmente para teste
      .getOrCreate()

    // Configuração de leitura do Kafka
    val kafkaDF = spark.readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "10.0.0.4:9092")
      .option("subscribe", "topic_new_york_taxis")
      .option("startingOffsets", "earliest")
      .option("maxOffsetsPerTrigger", "1000000")
      .option("failOnDataLoss", "false")
      .option("auto.offset.reset", "latest") // Começar a consumir a partir do último offset
      .load()

    // Esquema dos dados
    val schema = new StructType()
      .add("key", StringType)
      .add("pickup_datetime", TimestampType)
      .add("pickup_longitude", DoubleType)
      .add("pickup_latitude", DoubleType)
      .add("dropoff_longitude", DoubleType)
      .add("dropoff_latitude", DoubleType)
      .add("passenger_count", IntegerType)
      .add("fare_amount", DoubleType)

    val taxiDataDF = kafkaDF.selectExpr("CAST(value AS STRING) as json")
      .select(from_json(col("json"), schema).as("data"))
      .select("data.*")

    // Filtros de exemplo
    val startDate = "2013-01-01"
    val endDate = "2013-12-31"

    val filteredDataDF = taxiDataDF
      .filter(col("pickup_datetime").between(startDate, endDate))

    // Obter o timestamp atual para criar um diretório único
    val timestamp = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss").format(LocalDateTime.now())
    val outputDir = s"file:///usr/local/datasets/serasa/process_filter_$timestamp" // Diretório com timestamp

    // Escrevendo em parquet com trigger
    val query = filteredDataDF.writeStream
      .outputMode("append")
      .format("parquet")
      .option("path", outputDir)  // Novo diretório para cada execução
      .option("checkpointLocation", s"file:///usr/local/datasets/serasa/checkpoints_$timestamp") // Checkpoint com timestamp
      .trigger(Trigger.ProcessingTime("10 seconds"))
      .start()

    // Aguarda a conclusão da query
    try {
      query.awaitTermination()
    } catch {
      case e: StreamingQueryException =>
        println(s"Streaming query failed: ${e.getMessage}")
    }
  }
}

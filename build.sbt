// Versão do projeto
ThisBuild / version := "1.0.0-SNAPSHOT"

// Versão do Scala
ThisBuild / scalaVersion := "2.12.17"

// Nome do projeto
lazy val root = (project in file("."))
  .settings(
    name := "stream-taxi-data-consumer"
  )

// Dependências do Spark
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql" % "3.4.0"
libraryDependencies += "org.apache.spark" %% "spark-streaming" % "3.4.0"
libraryDependencies += "org.apache.spark" %% "spark-sql-kafka-0-10" % "3.4.0" % "provided"
libraryDependencies += "org.apache.spark" %% "spark-streaming-kafka-0-10" % "3.4.0"

// Dependências do Kafka
libraryDependencies += "org.apache.kafka" % "kafka-clients" % "3.4.0"

// Dependências do Hadoop Azure
libraryDependencies += "org.apache.hadoop" % "hadoop-azure" % "3.3.4"
libraryDependencies += "org.apache.hadoop" % "hadoop-azure-datalake" % "3.3.4"

// Dependência adicional para integração com o Azure Blob Storage
libraryDependencies += "com.microsoft.azure" % "azure-storage" % "8.6.6"



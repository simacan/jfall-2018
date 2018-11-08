
scalaVersion := "2.12.7"

val akkaV = "2.5.17"
val akkaHttpV = "10.1.5"
val playJsonV = "2.6.10"
val akkaStreamKafkaV = "0.19"
val akkaHttpJsonV = "1.21.0"
val akkaHttpCorsV = "0.3.1"

name := "jfall-http-endpoint-service"

libraryDependencies ++= Seq(
  // Json serialization
  "com.typesafe.play"      %% "play-json"              % playJsonV,
  "de.heikoseeberger"      %% "akka-http-play-json"    % akkaHttpJsonV,

  // Akka
  "com.typesafe.akka"      %% "akka-actor"             % akkaV,
  "com.typesafe.akka"      %% "akka-stream"            % akkaV,
  "com.typesafe.akka"      %% "akka-http"              % akkaHttpV,

  // Akka Kafka
  "com.typesafe.akka"      %% "akka-stream-kafka"      % akkaStreamKafkaV,

  // To be able to add cors to akka-http
  "ch.megard"              %% "akka-http-cors"         % akkaHttpCorsV,
)

enablePlugins(JavaAppPackaging)

dockerExposedPorts := Seq(8080)
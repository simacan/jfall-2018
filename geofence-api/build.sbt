scalaVersion := "2.12.7"

val akkaV = "2.5.17"
val akkaHttpV = "10.1.5"
val scalaTestV = "3.0.5"
val akkaHttpCorsV = "0.3.1"

name := "jfall-geofence-api"

libraryDependencies ++= Seq(
  "com.typesafe.akka"      %% "akka-actor"             % akkaV,
  "com.typesafe.akka"      %% "akka-stream"            % akkaV,
  "com.typesafe.akka"      %% "akka-http"              % akkaHttpV,
  "ch.megard"              %% "akka-http-cors"         % akkaHttpCorsV,


  "org.scalatest"          %% "scalatest"              % scalaTestV % Test,
  "com.typesafe.akka"      %% "akka-http-testkit"      % akkaHttpV,
)

mainClass in Compile := Some("com.simacan.jfall.geofence.api.Main")

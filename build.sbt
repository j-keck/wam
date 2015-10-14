name := "wam"

// sbt tasks
val gitDescribe = taskKey[String]("result from 'git describe --tags'")
val makeVersionProps = taskKey[Seq[File]]("creates a version.properties file")


def WAMPrj(name: String): Project = {
  Project(name, file(name)).
    settings(
      version := "0.0.1",
      scalaVersion := "2.11.7",
      resolvers += Resolver.bintrayRepo("j-keck", "maven"),
      resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots",
      resolvers += "Sonatype Public" at "https://oss.sonatype.org/content/groups/public/",
      //scalacOptions ++= Seq("-Xlog-implicits"),
      assemblyMergeStrategy in assembly := {
        case "META-INF/MANIFEST.MF" => MergeStrategy.discard
        case PathList(p @ _*) if p.exists(_.contains("sjs")) => MergeStrategy.discard
        case "JS_DEPENDENCIES" => MergeStrategy.discard
        case x => (assemblyMergeStrategy in assembly).value(x)
      },
      makeVersionProps := {
          val propFile = (resourceManaged in Compile).value / "version.properties"
          val content = s"version=${gitDescribe.value}"
          IO.write(propFile, content)
          Seq(propFile)
        },
      resourceGenerators in Compile <+= makeVersionProps,
      libraryDependencies ++= {
        Seq(
          "org.scodec" %% "scodec-core" % "1.8.2",
          "org.scodec" %% "scodec-scalaz" % "1.1.0",
          "org.scodec" %%% "scodec-core" % "1.8.2" % "provided",
          "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test"
        )
      }
    )
}


lazy val shared = WAMPrj("shared").
  enablePlugins(ScalaJSPlugin)


lazy val server = WAMPrj("server").
  dependsOn(shared).
  settings(
    assemblyJarName <<= gitDescribe map (v => s"wam-$v.jar"),
    gitDescribe in ThisBuild := Process("git describe --tags").lines.head,
    Revolver.settings,
    resolvers += Resolver.sonatypeRepo("public"),
    resourceGenerators in Compile <+= Def.task {
      val files = ((crossTarget in(client, Compile)).value ** ("*.js" || "*.map")).get
      val mappings: Seq[(File, String)] = files pair rebase((crossTarget in(client, Compile)).value, ((resourceManaged in Compile).value).getAbsolutePath)
      val map: Seq[(File, File)] = mappings.map { case (s, t) => (s, file(t)) }
      IO.copy(map).toSeq
    },

    compile <<= (compile in Compile) dependsOn (fastOptJS in Compile in client),
    assembly <<= assembly dependsOn (fullOptJS in (client, Compile)),

    libraryDependencies ++= {
      val http4sVersion = "0.10.0"
      Seq(
        "org.http4s" %% "http4s-dsl" % http4sVersion,
        "org.http4s" %% "http4s-blaze-server" % http4sVersion,
        "org.http4s" %% "http4s-blaze-client" % http4sVersion,
        "org.jsoup" % "jsoup" % "1.8.3",
        "ch.qos.logback" % "logback-classic" % "1.1.3",
        "com.github.scopt" %% "scopt" % "3.3.0"
      )
    }
  )


lazy val client = WAMPrj("client").
  dependsOn(shared).
  enablePlugins(ScalaJSPlugin).
  settings(
    scalaJSStage in Global := FastOptStage,
    libraryDependencies ++=
      Seq(
        "org.scala-js" %%% "scalajs-dom" % "0.8.2",
        "sodium" %%% "sodium" % "1.0"
      )
  )




import uk.gov.hmrc.DefaultBuildSettings.integrationTestSettings

val appName = "income-tax-submission-frontend"

lazy val coverageSettings: Seq[Setting[_]] = {
  import scoverage.ScoverageKeys

  val excludedPackages = Seq(
    "<empty>",
    ".*Reverse.*",
    ".*standardError*.*",
    ".*govuk_wrapper*.*",
    ".*main_template*.*",
    "uk.gov.hmrc.BuildInfo",
    "app.*",
    "prod.*",
    "config.*",
    "testOnly.*",
    "testOnlyDoNotUseInAppConf.*",
    ".*feedback*.*",
    "partials.*",
    "controllers.testOnly.*",
    "views.html.*[Tt]emplate*",
    "views.html.templates.helpers.*",
    "views.html.views.templates.helpers*",
    "views.html.views.templates.inputs*",
    "views.headerFooterTemplate"
  )

  Seq(
    ScoverageKeys.coverageExcludedPackages := excludedPackages.mkString(";"),
    ScoverageKeys.coverageMinimumStmtTotal := 93,
    ScoverageKeys.coverageFailOnMinimum := true,
    ScoverageKeys.coverageHighlighting := true
  )
}

lazy val twirlImports: Seq[String] = Seq(
  "config.AppConfig",
  "uk.gov.hmrc.govukfrontend.views.html.components._",
  "uk.gov.hmrc.hmrcfrontend.views.html.components._"
)

lazy val microservice = Project(appName, file("."))
  .enablePlugins(play.sbt.PlayScala, SbtAutoBuildPlugin, SbtGitVersioning, SbtDistributablesPlugin)
  .disablePlugins(sbt.plugins.JUnitXmlReportPlugin)
  .disablePlugins(JUnitXmlReportPlugin)
  .settings(
    // To resolve a bug with version 2.x.x of the scoverage plugin - https://github.com/sbt/sbt/issues/6997
    libraryDependencySchemes ++= Seq("org.scala-lang.modules" %% "scala-xml" % VersionScheme.Always,

    )

  )
  .settings(PlayKeys.playDefaultPort := 9302)
  .settings(
    majorVersion := 0,
    scalaVersion := "2.13.12",
    libraryDependencies ++= AppDependencies.compile ++ AppDependencies.test,
    TwirlKeys.templateImports ++= twirlImports,
    scalacOptions += "-Wconf:cat=unused-imports&src=html/.*:s",
    scalacOptions += "-Wconf:src=routes/.*:s"
  )
  .settings(
    fork in Test := false
  )
  .configs(IntegrationTest)
  .settings(integrationTestSettings(): _*)
  .settings(unmanagedResourceDirectories in IntegrationTest += baseDirectory.value / "it" / "resources")
  .settings(resolvers += Resolver.jcenterRepo)
  .settings(coverageSettings: _*)
  .settings(
    // concatenate js
    Concat.groups := Seq(
      "javascripts/application.js" ->
        group(Seq(
          "lib/govuk-frontend/govuk/all.js",
          "javascripts/jquery.min.js",
          "javascripts/backbutton.js"
        ))
    ),
      // prevent removal of unused code which generates warning errors due to use of third-party libs
      uglifyCompressOptions := Seq("unused=false", "dead_code=false"),
    pipelineStages := Seq(digest),
    // below line required to force asset pipeline to operate in dev rather than only prod
    pipelineStages in Assets := Seq(concat,uglify),
    // only compress files generated by concat
    includeFilter in uglify := GlobFilter("application.js")
  )

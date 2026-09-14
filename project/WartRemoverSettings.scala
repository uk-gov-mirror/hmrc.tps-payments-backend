import play.sbt.routes.RoutesKeys.routes
import sbt.Keys.*
import sbt.{Def, *}
import wartremover.Wart
import wartremover.WartRemover.autoImport.*

object WartRemoverSettings {

  val wartRemoverSettings: Seq[Def.Setting[Seq[Wart]]] =
    Seq(
      (Compile / compile / wartremoverErrors) ++= {
        if (StrictBuilding.strictBuilding.value)
          Warts.allBut(
            Wart.DefaultArguments,
            Wart.ImplicitConversion,
            Wart.ImplicitParameter,
            Wart.Nothing,
            Wart.Overloading,
            Wart.SizeIs,
            Wart.Equals,
            Wart.SortedMaxMinOption,
            Wart.Throw,
            Wart.ToString,
            Wart.PlatformDefault,
            Wart.Product,
            Wart.JavaSerializable,
            Wart.Serializable
          )
        else Nil
      },
      Test / compile / wartremoverErrors --= Seq(
        Wart.Any,
        Wart.Equals,
        Wart.GlobalExecutionContext,
        Wart.Null,
        Wart.NonUnitStatements,
        Wart.PublicInference
      )
    )

  lazy val wartRemoverSettingsPlay: Seq[Def.Setting[Task[Seq[File]]]] = Seq(
    wartremoverExcluded ++= (Compile / routes).value ++ Seq(
      sourceManaged.value / "main" / "sbt-buildinfo" / "BuildInfo.scala"
    )
  )
}

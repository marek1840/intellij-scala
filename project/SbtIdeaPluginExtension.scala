import org.jetbrains.sbtidea.AbstractSbtIdeaPlugin

object SbtIdeaPluginExtension extends AbstractSbtIdeaPlugin {
  override def trigger = allRequirements
}

package org.jetbrains.jps.incremental.scala.local.worksheet

import java.io.{File, PrintWriter}

import org.jetbrains.jps.incremental.scala.local.worksheet.ILoopWrapper
import org.jetbrains.jps.incremental.scala.local.worksheet.ILoopWrapper213Impl.DummyConfig

import scala.reflect.classTag
import scala.reflect.internal.util.Position
import scala.tools.nsc.Settings
import scala.tools.nsc.interpreter.NamedParam.Typed
import scala.tools.nsc.interpreter.StdReplTags.tagOfIMain
import scala.tools.nsc.interpreter.shell.{ILoop, ReplReporterImpl, ShellConfig}
import scala.tools.nsc.interpreter.{IMain, Results}

import scala.collection.JavaConverters._

/**
 * ATTENTION: when editing ensure to increase the ILoopWrapperFactoryHandler.WRAPPER_VERSION
 */
class ILoopWrapper213Impl(
  myOut: PrintWriter,
  wrapperReporter: ILoopWrapperReporter,
  projectFullCp: java.util.List[String]
) extends  {
  val myConfig = new DummyConfig
} with ILoop(myConfig, out = myOut)
  with ILoopWrapper {

  override def init(): Unit = {
    val mySettings = new Settings
    mySettings.classpath.append(projectFullCp.asScala.mkString(File.pathSeparator))
    // do not use java class path because it contains scala library jars with version
    // different from one that is used during compilation (it is passed from the plugin classpath)
    mySettings.usejavacp.tryToSetFromPropertyValue(false.toString)

    createInterpreter(mySettings)

    val itp = intp.asInstanceOf[IMain]
    itp.initializeCompiler()
    itp.quietBind(new Typed[IMain]("$intp", itp)(tagOfIMain, classTag[IMain]))
    itp.setContextClassLoader()
  }

  override def createInterpreter(interpreterSettings: Settings): Unit = {
    val reporter = new ReplReporterImpl(myConfig, interpreterSettings, out) {
      override def doReport(pos: Position, msg: String, severity: Severity): Unit =
        wrapperReporter.report(severity.toString, pos.line, pos.column, pos.lineContent, msg)
    }
    intp = new IMain(interpreterSettings, None, interpreterSettings, reporter)
  }

  override def reset(): Unit = {
    intp.reset()
  }

  override def shutdown(): Unit = {
    closeInterpreter()
  }

  override def processChunk(code: String): Boolean =
    intp.interpret(code) match {
      case Results.Success => true
      case _ => false
    }

  override def getOutputWriter: PrintWriter = myOut
}

object ILoopWrapper213Impl {
  class DummyConfig extends ShellConfig {
    override def filesToPaste: List[String] = List.empty

    override def filesToLoad: List[String] = List.empty

    override def batchText: String = ""

    override def batchMode: Boolean = false

    override def doCompletion: Boolean = false

    override def haveInteractiveConsole: Boolean = false
  }
}
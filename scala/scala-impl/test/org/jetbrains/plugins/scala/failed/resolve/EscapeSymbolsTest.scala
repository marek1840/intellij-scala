package org.jetbrains.plugins.scala.failed.resolve

import org.jetbrains.plugins.scala.PerfCycleTests
import org.junit.experimental.categories.Category

/**
  * Created by Anton Yalyshev on 13/04/16.
  */
@Category(Array(classOf[PerfCycleTests]))
class EscapeSymbolsTest extends FailedResolveTest("escapeSymbols") {
  def testSCL5375(): Unit = doTest()
  def testSCL10630(): Unit = doTest()
}

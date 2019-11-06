package org.jetbrains.plugins.scala.failed.annotator

import org.jetbrains.plugins.scala.PerfCycleTests
import org.jetbrains.plugins.scala.base.ScalaLightCodeInsightFixtureTestAdapter
import org.junit.experimental.categories.Category

/**
  * @author Nikolay.Tropin
  */
@Category(Array(classOf[PerfCycleTests]))
class TypeErasureOverloadingTest extends ScalaLightCodeInsightFixtureTestAdapter {

  override protected def shouldPass: Boolean = false

  def testSCL9276(): Unit = {
    checkHasErrorAroundCaret(
      """class Example {
        |  def foo(arg: => String) = Unit
        |  def foo(arg: => Option[String]) = Unit
        |  def foo(arg: => Int) = Unit
        |  def foo(arg: => Boolean) = Unit
        |}
      """.stripMargin)
  }
}

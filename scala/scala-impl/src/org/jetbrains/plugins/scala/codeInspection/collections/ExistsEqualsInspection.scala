package org.jetbrains.plugins.scala
package codeInspection
package collections

import org.jetbrains.plugins.scala.lang.psi.api.expr.{ScExpression, ScMethodCall, ScReferenceExpression}
import org.jetbrains.plugins.scala.lang.psi.impl.ScalaPsiElementFactory
import org.jetbrains.plugins.scala.lang.psi.types.ScTypeExt
import org.jetbrains.plugins.scala.lang.psi.types.result._
import org.jetbrains.plugins.scala.project.ProjectContext

class ExistsEqualsInspection extends OperationOnCollectionInspection {
  override def possibleSimplificationTypes: Array[SimplificationType] =
    Array(ExistsEquals, ForallNotEquals)
}

//noinspection ScalaUnnecessaryParentheses
object ExistsEquals extends SimplificationType {
  override def getSimplification(expr: ScExpression): Option[Simplification] = {
    expr match {
      case qual`.exists`(`x == `(e)) if canBeReplacedWithContains(qual, e) =>
        Some(replace(expr).withText(invocationText(qual, "contains", e)))
      case _ => None
    }
  }

  override def hint: String = InspectionBundle.message("exists.equals.hint")

  def canBeReplacedWithContains(qual: ScExpression, arg: ScExpression): Boolean = {
    if (qual == null) return false

    implicit val ctx: ProjectContext = qual.projectContext

    val exprText = s"(${qual.getText}).contains(${arg.getText})"
    ScalaPsiElementFactory.createExpressionWithContextFromText(exprText, qual.getContext, qual) match {
      case ScMethodCall(ref: ScReferenceExpression, Seq(a)) =>
        ref.resolve() != null &&
          a.expectedType(fromUnderscore = false).exists(a.`type`().getOrNothing.conforms(_))
      case _ => false
    }

  }
}

//noinspection ScalaUnnecessaryParentheses
object ForallNotEquals extends SimplificationType {
  override def getSimplification(expr: ScExpression): Option[Simplification] = {
    expr match {
      case qual`.forall`(`x != `(e)) if ExistsEquals.canBeReplacedWithContains(qual, e) =>
        Some(replace(expr).withText("!" + invocationText(qual, "contains", e)))
      case _ => None
    }
  }

  override def hint: String = InspectionBundle.message("forall.notEquals.hint")
}

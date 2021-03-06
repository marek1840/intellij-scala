package org.jetbrains.plugins.scala
package lang
package completion
package filters.expression

import com.intellij.psi.filters.ElementFilter
import com.intellij.psi.{PsiElement, PsiErrorElement, _}
import org.jetbrains.annotations.NonNls
import org.jetbrains.plugins.scala.lang.completion.ScalaCompletionUtil._
import org.jetbrains.plugins.scala.lang.parser._
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.ScStableReferencePattern
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.toplevel.templates.ScTemplateBody

/** 
* @author Alexander Podkhalyuzin
* Date: 22.05.2008
*/

class StatementFilter extends ElementFilter {
  def isAcceptable(element: Object, context: PsiElement): Boolean = {
    if (context.isInstanceOf[PsiComment]) return false
    val leaf = getLeafByOffset(context.getTextRange.getStartOffset, context)
    if (leaf != null) {
      val parent = leaf.getParent
      if (parent.isInstanceOf[ScReferenceExpression] &&
              !parent.getParent.isInstanceOf[ScStableReferencePattern] &&
              (!parent.getParent.isInstanceOf[ScInfixExpr]) && (parent.getPrevSibling == null ||
              parent.getPrevSibling.getPrevSibling == null ||
        (parent.getPrevSibling.getPrevSibling.getNode.getElementType != ScalaElementType.MATCH_STMT ||
                      !parent.getPrevSibling.getPrevSibling.getLastChild.isInstanceOf[PsiErrorElement]))) {
        parent.getParent match {
          case _: ScBlockExpr | _: ScBlock | _: ScTemplateBody => return true
          case x: ScExpression => return checkReplace(x, "if (true) true", x.getManager)
          case _ =>
        }
        return true
      }
    }
    false
  }

  def isClassAcceptable(hintClass: java.lang.Class[_]): Boolean = {
    true
  }

  @NonNls
  override def toString: String = {
    "statements keyword filter"
  }
}
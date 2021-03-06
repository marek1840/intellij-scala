package org.jetbrains.plugins.scala

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.fileEditor.{FileEditorManager, TextEditor}
import com.intellij.openapi.project.Project
import com.intellij.psi.util.PsiTreeUtil
import com.intellij.psi.{PsiClass, PsiElement}
import org.jetbrains.plugins.scala.extensions._
import org.jetbrains.plugins.scala.lang.psi.ElementScope
import org.jetbrains.plugins.scala.lang.psi.api.base.patterns.{ScCaseClause, ScCaseClauses}
import org.jetbrains.plugins.scala.lang.psi.api.expr._
import org.jetbrains.plugins.scala.lang.psi.api.statements.ScFunctionDefinition
import org.jetbrains.plugins.scala.lang.psi.types.api.UndefinedType
import org.jetbrains.plugins.scala.lang.psi.types.api.designator.ScDesignatorType
import org.jetbrains.plugins.scala.lang.psi.types.{ScParameterizedType, ScType, api}
import org.jetbrains.plugins.scala.project.ProjectContext

package object codeInspection {
  private[codeInspection] def getActiveEditor(element: PsiElement): Option[Editor] = getActiveEditor(element, element.getProject)
  private[codeInspection] def getActiveEditor(element: PsiElement, project: Project): Option[Editor] = for {
    file <- element.getContainingFile.toOption
    vfile <- file.getVirtualFile.toOption
    textEditor <- FileEditorManager.getInstance(project).getSelectedEditor(vfile).asOptionOf[TextEditor]
  } yield textEditor.getEditor

  private[codeInspection] def expressionResultIsNotUsed(expression: ScExpression): Boolean =
    isNonLastInBlock(expression) ||
      parents(expression).exists {
        case e: ScExpression => isNonLastInBlock(e)
        case _ => false
      } ||
      isInUnitFunctionReturnPosition(expression)

  private[this] def isNonLastInBlock(expression: ScExpression) = expression.getParent match {
    case block: ScBlock => !block.resultExpression.contains(expression)
    case _ => false
  }

  private[this] def parents(expression: ScExpression) = {
    def isNotAncestor(maybeExpression: Option[ScExpression]) =
      maybeExpression.forall(!PsiTreeUtil.isAncestor(_, expression, false))

    expression.parentsInFile.takeWhile {
      case statement: ScMatch => isNotAncestor(statement.expression)
      case statement: ScIf => isNotAncestor(statement.condition)
      case _: ScBlock |
           _: ScParenthesisedExpr |
           _: ScCaseClause |
           _: ScCaseClauses |
           _: ScTry |
           _: ScCatchBlock => true
      case _ => false
    }
  }

  private[this] def isInUnitFunctionReturnPosition(expression: ScExpression) = {
    findDefiningFunction(expression).exists { definition =>
      isUnitFunction(definition) && definition.returnUsages(expression)
    }
  }

  private[codeInspection] def findDefiningFunction(expression: ScExpression): Option[ScFunctionDefinition] =
    expression.parentOfType(classOf[ScFunctionDefinition])

  private[codeInspection] def isUnitFunction(definition: ScFunctionDefinition) =
    definition.returnType.exists(_.isUnit)

  private[codeInspection] def conformsToTypeFromClass(scType: ScType, fqn: String)
                                                     (implicit projectContext: ProjectContext): Boolean =
    (scType != api.Null) && (scType != api.Nothing) && {
      ElementScope(projectContext)
        .getCachedClass(fqn)
        .map(createParameterizedType)
        .exists(scType.conforms)
    }

  private[this] def createParameterizedType(clazz: PsiClass) = {
    val designatorType = ScDesignatorType(clazz)
    clazz.getTypeParameters match {
      case Array() => designatorType
      case parameters => ScParameterizedType(designatorType, parameters.map(UndefinedType(_)))
    }
  }
}

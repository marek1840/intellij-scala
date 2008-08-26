package org.jetbrains.plugins.scala.lang.psi.api.expr

import base.{ScStableCodeReferenceElement, ScPathElement}
import com.intellij.psi.PsiClass
import psi.ScalaPsiElement

/** 
* @author Alexander Podkhalyuzin
* Date: 14.03.2008
*/

trait ScSuperReference extends ScExpression with ScPathElement { //todo extract a separate 'this' path element
  def refClass : Option[PsiClass]
  def qualifier = findChild(classOf[ScStableCodeReferenceElement])
}
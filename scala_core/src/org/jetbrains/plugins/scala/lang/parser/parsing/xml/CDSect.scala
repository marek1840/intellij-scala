package org.jetbrains.plugins.scala.lang.parser.parsing.xml

import com.intellij.lang.PsiBuilder, org.jetbrains.plugins.scala.lang.lexer.ScalaTokenTypes
import org.jetbrains.plugins.scala.lang.parser.ScalaElementTypes
import org.jetbrains.plugins.scala.lang.parser.bnf.BNF
import com.intellij.psi.tree.TokenSet
import com.intellij.psi.tree.IElementType
import org.jetbrains.plugins.scala.lang.parser.util.ParserUtils
import org.jetbrains.plugins.scala.lang.lexer.ScalaElementType
import org.jetbrains.plugins.scala.ScalaBundle
import org.jetbrains.plugins.scala.lang.parser.parsing.expressions._
import com.intellij.psi.xml.XmlTokenType

/**
* @author Alexander Podkhalyuzin
* Date: 18.04.2008
*/

object CDSect {
  def parse(builder: PsiBuilder): Boolean = {
    val cDataMarker = builder.mark()
    builder.getTokenType match {
      case XmlTokenType.XML_CDATA_START => builder.advanceLexer()
      case _ => {
        cDataMarker.drop
        return false
      }
    }
    builder.getTokenType match {
      case XmlTokenType.XML_DATA_CHARACTERS => builder.advanceLexer
      case _ =>
    }
    builder.getTokenType match {
      case XmlTokenType.XML_CDATA_END => builder.advanceLexer()
      case _ => builder error ErrMsg("xml.cdata.end.expected") //todo
    }
    cDataMarker.drop //todo
    return true
  }
}
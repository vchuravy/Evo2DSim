package org.vastness.utils

import scala.language.experimental.macros
import scala.reflect.macros.Context

/**
 * Implements a scala style enum @see Role for a implementation.
 * http://stackoverflow.com/questions/20089920/custom-scala-enum-most-elegant-version-searched
 */
trait Enum[T] {
  def values: Set[T] = macro Enum.caseObjectsSetImpl[T]
}

object Enum {
  def caseObjectsSetImpl[A: c.WeakTypeTag](c: Context) = {
    import c.universe._

    val tSymbol = weakTypeOf[A].typeSymbol.asClass
    require(tSymbol.isSealed)
    val subclasses = tSymbol.knownDirectSubclasses
      .filter(_.asClass.isCaseClass)
      .map(s => Ident(s.companionSymbol))
      .toList

    val setTSymbol = weakTypeOf[Set[A]].typeSymbol.companionSymbol
    // Create an expression of the form: Set[A](CaseObj1, CaseObj2, ...)
    c.Expr(Apply(Ident(setTSymbol), subclasses))
  }
}

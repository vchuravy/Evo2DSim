package org.vastness.evo2dsim.utils

/**
 * Implements a scala style enum @see Color for a implementation.
 */
trait Enum[A] {
  trait Value { self: A =>
    _values ::= this}
  private var _values = List.empty[A]
  def values = _values
}

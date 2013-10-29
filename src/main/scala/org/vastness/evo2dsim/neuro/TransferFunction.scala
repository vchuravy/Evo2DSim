package org.vastness.evo2dsim.neuro

import org.apache.commons.math3.util.FastMath

object TransferFunction{
  def thanh (activity: Double) = FastMath.tanh(activity)
  def sig (activity: Double) = 1/FastMath.pow(math.E, activity)
  def binary(activity: Double) = activity match {
    case n if n >= 0 => 1
    case n if n < 0 => 0
  }
}
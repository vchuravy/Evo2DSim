package org.vastness.evo2dsim.neuro

object TransferFunction{
  def thanh (activity: Double) = math.tanh(activity)
  def sig (activity: Double) = 1/math.pow(math.E, activity)
  def binary(activity: Double) = activity match {
    case n if n >= 0 => 1
    case n if n < 0 => 0
  }
}
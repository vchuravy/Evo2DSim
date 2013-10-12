package org.vastness.evo2dsim.neuro

object TransferFunction{
  def thanh (activity: Double) = math.tanh(activity)
  def sig (activity: Double) = 1/math.pow(math.E, activity)
}
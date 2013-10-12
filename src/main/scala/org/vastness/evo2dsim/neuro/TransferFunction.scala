package org.vastness.evo2dsim.neuro

object TransferFunction extends Enumeration {
  type TransferFunction = (Double) => Double
  val thanh = (activity: Double) => math.tanh(activity)
  val sig = (activity: Double) => 1/math.pow(math.E, activity)
}
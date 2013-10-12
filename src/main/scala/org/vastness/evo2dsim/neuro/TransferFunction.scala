package org.vastness.evo2dsim.neuro

object TransferFunction extends Enumeration {
  type TransferFunction = (Double*) => Double
  val thanh = (fs: Double*) => math.tanh(fs.sum)
  val sig = (fs: Double*) => 1/math.pow(math.E,-fs.sum)
}
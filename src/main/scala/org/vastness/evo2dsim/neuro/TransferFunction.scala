package org.vastness.evo2dsim.neuro

import org.apache.commons.math3.util.FastMath
import org.vastness.evo2dsim.utils.Enum

sealed trait TransferFunction extends TransferFunction.Value {
  def name: String
  def apply(x: Double): Double
}

object TransferFunction extends Enum[TransferFunction]{
  case object THANH extends TransferFunction {
    val name = "thanh"
    def apply(activity: Double) = FastMath.tanh(activity)
  }
  case object SIG extends TransferFunction {
    val name = "sig"
    def apply(activity: Double) =  1/FastMath.pow(math.E, activity)
  }
  case object BINARY extends TransferFunction {
    val name = "binary"
    def apply(activity: Double) = activity match {
      case n if n >= 0 => 1
      case n if n < 0 => 0
    }
  }
}
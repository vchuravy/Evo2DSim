package org.vastness.evo2dsim.neuro

import org.vastness.evo2dsim.neuro.TransferFunction._

class SensorNeuron(v_bias: Double, t_func: TransferFunction, s_func: () => Double ) extends Neuron(v_bias, t_func) {
  override def calcActivity = super.calcActivity + s_func()
}

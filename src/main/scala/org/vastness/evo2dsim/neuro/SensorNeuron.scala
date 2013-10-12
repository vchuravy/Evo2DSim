package org.vastness.evo2dsim.neuro

class SensorNeuron(v_bias: Double, t_func: (Double) => Double, s_func: () => Double ) extends Neuron(v_bias, t_func) {
  override def calcActivity = super.calcActivity + s_func()
}

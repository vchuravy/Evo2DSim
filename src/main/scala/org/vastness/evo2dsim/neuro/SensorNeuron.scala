package org.vastness.evo2dsim.neuro

class SensorNeuron(v_bias: Double, t_func: (Double) => Double, var s_func: () => Double = () => 0.0 ) extends Neuron(v_bias, t_func) {
  override def calcActivity = super.calcActivity + s_func()
}

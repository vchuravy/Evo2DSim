package org.vastness.evo2dsim.neuro


class MotorNeuron(v_bias: Double, t_func: (Double) => Double, m_func: (Double) => Unit ) extends Neuron(v_bias, t_func){
  override def step() {
    super.step()
    m_func(calcOutput)
  }

}

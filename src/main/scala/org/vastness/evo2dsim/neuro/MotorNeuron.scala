package org.vastness.evo2dsim.neuro

import org.vastness.evo2dsim.neuro.TransferFunction._

class MotorNeuron(v_bias: Double, t_func: TransferFunction, m_func: (Double) => _ ) extends Neuron(v_bias, t_func){
  override def step() {
    super.step()
    m_func(calcOutput)
  }

}

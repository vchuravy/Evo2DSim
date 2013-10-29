package org.vastness.evo2dsim.gui

import org.vastness.evo2dsim.environment.Environment
import org.vastness.evo2dsim.simulator.Entity

object EnvironmentManager {
  private var environments = List.empty[Environment]

  def visibleEntities = environments match {
    case x :: xs => x.sim.entities
    case Nil => List.empty[Entity]
  }

  def addEnvironment(e: Environment){
    environments.synchronized({
      environments ::= e
    })
  }

  def clean(){
    environments.synchronized({
      environments ::= List.empty[Environment]
    })
  }
}

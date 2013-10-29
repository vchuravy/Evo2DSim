package org.vastness.evo2dsim.gui

import org.vastness.evo2dsim.environment.Environment
import org.vastness.evo2dsim.simulator.Entity
import scala.collection.mutable.ListBuffer

object EnvironmentManager {
  private var environments = ListBuffer.empty[Environment]

  def visibleEntities = environments match {
    case xs: ListBuffer[Environment] if xs.nonEmpty => xs.head.sim.entities
    case xs: ListBuffer[Environment] if xs.isEmpty => List.empty[Entity]
  }

  def addEnvironment(e: Environment){
      environments += e
  }

  def clean(){
      environments = ListBuffer.empty[Environment]
  }

  def remove(e: Environment){
      environments -= e
  }
}

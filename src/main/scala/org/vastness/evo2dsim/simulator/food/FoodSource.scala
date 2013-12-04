package org.vastness.evo2dsim.simulator.food

import org.vastness.evo2dsim.simulator.{Simulator, Entity, Agent}
import org.vastness.evo2dsim.simulator.light.LightSource
import scala.collection.mutable
import org.vastness.evo2dsim.gui.Color

/**
 * Base class for food sources. Gives reward to the first n individuals
 * @param c on which color channel the light source is sending
 * @param max maximal numbers of individuals who can feed from this source
 */
abstract class FoodSource(c: Color, var max: Int) {
  //require(c == Color.BLUE || c == Color.RED)
  protected var light: Option[LightSource] = None

  def initialize(e: Entity, sim: Simulator) {
    val l = new LightSource(c, e)
    l.active_ = true
    sim.lightManager.addLight(l)
    light = Some(l)
  }

  def color = light match{
    case Some(l) => l.color
    case None => Color.CYAN // Not initialized
  }

  val feeders = mutable.HashSet[Agent]()

  def reward: Double

  def step(){
    val r = reward
    for(a <- feeders){
      a.fitness += r
      a.currentReward = r
    }
  }
}

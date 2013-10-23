package org.vastness.evo2dsim.simulator.food

import org.vastness.evo2dsim.simulator.{Simulator, Entity, Agent}
import org.vastness.evo2dsim.simulator.light.LightSource
import scala.collection.mutable

/**
 * Base class for food sources. Gives reward to the first n individuals
 * @param color on which color channel the light source is sending
 * @param max maximal numbers of individuals who can feed from this source
 */
abstract class FoodSource(color: Int, var max: Int) {
  protected var light:LightSource = _

  def initialize(e: Entity, sim: Simulator){
    light = new LightSource(color, e)
    light.active = true
    sim.lightManager.addLight(light)
  }

  val feeders = mutable.HashSet[Agent]()

  def reward(): Double

  def step(){
    for(a <- feeders.par){
       a.fitness += reward()
    }
  }
}

package org.vastness.evo2dsim.teem.enki.sbot

import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.simulator.{Simulator, Agent}

/**
 * Implements an S-Bot agent similar to the enki simulator.
 *   val radius = 0.06f  S-Bot size 6cm
 *   val mass = 0.66f S-Bot weight 660g
 */
class SBot(id: Int, pos: Vec2, sim: Simulator)
  extends Agent(id, pos, sim, radius = 0.06f, mass = 0.66f) {

}

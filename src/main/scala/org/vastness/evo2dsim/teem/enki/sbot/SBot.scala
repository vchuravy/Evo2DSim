package org.vastness.evo2dsim.teem.enki.sbot

import org.jbox2d.common.Vec2
import org.jbox2d.dynamics.World
import org.vastness.evo2dsim.simulator.Agent

/**
 * Implements an S-Bot agent similar to the enki simulator.
 * Only important Difference that it uses a force driven motor instead of a speed controller.
 *   val radius = 0.06f  S-Bot size 6cm
 *   val mass = 0.66f S-Bot weight 660g
 */
class SBot(id: Int, pos: Vec2, world: World)
  extends Agent(id: Int, pos: Vec2, world: World, radius = 0.06f, mass = 0.66f) {

}

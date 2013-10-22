package org.vastness.evo2dsim.simulator.light

import org.vastness.evo2dsim.simulator.Entity

/**
 * Implements a simple  LightSource
 * @param color has to be either 0 = blue or 1 = red
 */

class LightSource(val color: Int, entity: Entity) {
  assert(color == 0 || color == 1, "Color is neither one nor zero. Shame on you!")

  def position = entity.position
  var active = false

}

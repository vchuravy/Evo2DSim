package org.vastness.evo2dsim.simulator.light

import org.vastness.evo2dsim.simulator.Entity
import org.vastness.evo2dsim.gui.Color

/**
 * Implements a simple  LightSource
 * @param c has to be either blue or red
 */

class LightSource( c: Color, entity: Entity) {
  //require(c == Color.BLUE || c == Color.RED, "Color is neither blue or red. Shame on you!")

  def color = if(active) c else Color.BLACK

  def position = entity.position
  var active = false

}

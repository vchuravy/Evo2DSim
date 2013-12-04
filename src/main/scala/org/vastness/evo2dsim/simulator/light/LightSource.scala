package org.vastness.evo2dsim.simulator.light

import org.vastness.evo2dsim.simulator.Entity
import org.vastness.evo2dsim.gui.Color

/**
 * Implements a simple  LightSource
 * @param c has to be either blue or red
 */

class LightSource(c: Color, entity: Entity, val category: LightCategory) {
  require(c == Color.BLUE || c == Color.RED, "Color is neither blue or red. Shame on you!")

  def position = entity.position
  def active = active_ && !forced_disable
  var active_ = false
  def forced_disable = entity.sim.lightManager.disabledCategories.contains(category)


  def color = if(active) c else Color.BLACK


}

/*
 * This file is part of Evo2DSim.
 *
 * Evo2DSim is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Evo2DSim is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Evo2DSim.  If not, see <http://www.gnu.org/licenses/>.
 */

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

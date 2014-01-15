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

import scala.collection.mutable.ArrayBuffer

class LightManager {
  var lightSources = ArrayBuffer[LightSource]()
  var disabledCategories = Set.empty[LightCategory]

  def addLight(l: LightSource){
    lightSources += l
  }


  def disableCategory(c: LightCategory) {
    disabledCategories += c
  }

  def enableCategory(c: LightCategory) {
    disabledCategories -= c
  }

  def toggleCategory(c: LightCategory) {
    if (disabledCategories.contains(c)) enableCategory(c)
    else disableCategory(c)
  }
}

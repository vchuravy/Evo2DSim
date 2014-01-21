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

package org.vastness.evo2dsim.core.environment.mixins.settings.informationTests

import org.vastness.evo2dsim.core.environment.mixins.settings.Settings
import scala.collection.immutable.NumericRange

trait OneVarTest[A] extends Settings {
  def varRange: NumericRange[Double]
  def varUpdate(): Unit
  def varIdx: Int = steps / stepsPerTest

  require(steps % varRange.size == 0)
  val stepsPerTest = steps / varRange.size
  require(stepsPerTest != 0)

  override def updateSimulation() {
    super.updateSimulation()
    if(steps % stepsPerTest == 0) varUpdate()
  }
}

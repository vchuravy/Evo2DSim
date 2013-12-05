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

package org.vastness.evo2dsim.evolution

import scala.util.Random


trait Binary {
  /**
   * Map doubles in the range of -1 to 1 to bytes
   * @param value must be in the range of -1 to 1
   * @return signed byte
   */
  def mapToByte(value: Double): Byte = {
    assert(value.abs <= 1, "Our values are out of range.")
    value match {
      case v if 0 <= v => (v * 127).toByte
      case v if v <  0 => (v * 128).toByte
    }
  }

  /**
   * Map bytes back to doubles in the range of -1 to 1
   * @param value signed Byte
   * @return Double ortedMapin the range of -1 to 1
   */
  def mapToDouble(value: Byte): Double = value match{
    case v if 0 <= v => v / 127.0
    case v if v <  0 => v / 128.0
  }

  /**
   * Creates a byte that indicates on which point on should flip a byte
   * @return Byte
   */
  def xor(length:Int = 8, p: Double): Byte = Integer.parseInt(
    Range(0,length).foldLeft[String]("")(
      (acc, _) =>  if (Random.nextDouble <= p) acc + "1" else acc + "0"
    ), 2).toByte

}

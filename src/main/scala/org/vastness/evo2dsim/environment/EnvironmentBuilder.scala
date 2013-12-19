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

package org.vastness.evo2dsim.environment

import org.vastness.utils.Enum
import org.vastness.evo2dsim.environment.mixins.foodSources._
import org.vastness.evo2dsim.environment.mixins.foodPos._

/**
 * Builder functions for Environment.
 */
sealed trait EnvironmentBuilder {
  def name: String
  def apply(timeStep:Int, steps:Int): Environment
  override def toString = name
}

object EnvironmentBuilder extends Enum[EnvironmentBuilder] {
  case object Basic extends EnvironmentBuilder {
    val name = "basic"
    def apply(t: Int, s: Int) = new BasicEnvironment(t,s) with SimpleFoodPos with StaticFoodSources
  }

  case object BasicSimpleRandom extends EnvironmentBuilder {
    val name = "basicSimpleRandom"
    def apply(t: Int, s:Int) = new BasicEnvironment(t, s) with SimpleRandomFoodPos with StaticFoodSources
  }

  case object BasicRandom extends EnvironmentBuilder {
    val name = "basicRandom"
    def apply(t: Int, s:Int) = new BasicEnvironment(t, s) with RandomFoodPos with StaticFoodSources
  }

  case object Dynamic extends EnvironmentBuilder {
    val name = "dynamic"
    def apply(t: Int, s: Int)   = new BasicEnvironment(t,s) with SimpleFoodPos with DynamicFoodSources
  }

  case object DynamicSimpleRandom extends EnvironmentBuilder {
    val name = "dynamicSimpleRandom"
    def apply(t: Int, s: Int)   = new BasicEnvironment(t,s) with SimpleRandomFoodPos with DynamicFoodSources
  }

  case object DynamicRandom extends EnvironmentBuilder {
    val name = "dynamicRandom"
    def apply(t: Int, s: Int)   = new BasicEnvironment(t,s) with RandomFoodPos with DynamicFoodSources
  }

  case object Positive extends EnvironmentBuilder {
    val name = "positive"
    def apply(t: Int, s: Int) = new BasicEnvironment(t,s) with SimpleFoodPos with PositiveStaticFoodSources
  }

  case object PositiveRandom extends EnvironmentBuilder {
    val name = "positiveRandom"
    def apply(t: Int, s:Int) = new BasicEnvironment(t, s) with RandomFoodPos with PositiveStaticFoodSources
  }
}

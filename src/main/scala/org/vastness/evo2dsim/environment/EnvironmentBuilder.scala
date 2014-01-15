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
import org.vastness.evo2dsim.environment.mixins.settings._

/**
 * Builder functions for Environment.
 */
sealed trait EnvironmentBuilder {
  def name: String
  def apply(timeStep:Int, steps:Int): Environment
  override def toString = name
}

object EnvironmentBuilder extends Enum[EnvironmentBuilder] {
  abstract class Default(t: Int, s: Int) extends BasicEnvironment(t,s) with DefaultSettings
  abstract class DefaultStatic(t: Int, s: Int) extends Default(t,s) with StaticFoodSources
  abstract class DefaultDynamic(t: Int, s: Int) extends Default(t,s) with DynamicFoodSources

  case object Basic extends EnvironmentBuilder {
    val name = "basic"
    def apply(t: Int, s: Int) = new DefaultStatic(t,s) with SimpleFoodPos
  }

  case object BasicSimpleRandom extends EnvironmentBuilder {
    val name = "basicSimpleRandom"
    def apply(t: Int, s:Int) = new DefaultStatic(t, s)  with SimpleRandomFoodPos
  }

  case object BasicRandom extends EnvironmentBuilder {
    val name = "basicRandom"
    def apply(t: Int, s:Int) = new DefaultStatic(t, s) with RandomFoodPos
  }

  case object Dynamic extends EnvironmentBuilder {
    val name = "dynamic"
    def apply(t: Int, s: Int) = new DefaultDynamic(t,s)  with SimpleFoodPos
  }

  case object DynamicSimpleRandom extends EnvironmentBuilder {
    val name = "dynamicSimpleRandom"
    def apply(t: Int, s: Int)   = new DefaultDynamic(t,s) with SimpleRandomFoodPos
  }

  case object DynamicRandom extends EnvironmentBuilder {
    val name = "dynamicRandom"
    def apply(t: Int, s: Int)   = new DefaultDynamic(t,s)  with RandomFoodPos
  }

  case object Positive extends EnvironmentBuilder {
    val name = "positive"
    def apply(t: Int, s: Int) = new Default(t,s)  with SimpleFoodPos with PositiveStaticFoodSources
  }

  case object PositiveRandom extends EnvironmentBuilder {
    val name = "positiveRandom"
    def apply(t: Int, s:Int) = new Default(t, s)  with RandomFoodPos with PositiveStaticFoodSources
  }

  case object ECECR extends EnvironmentBuilder {
    val name = "ECECR"
    def apply(t: Int, s:Int) = new BasicEnvironment(t,s) with ECECRSettings with StaticFoodSources with RandomFoodPos
  }

  case object ECECR_WO_AMemory extends EnvironmentBuilder {
    val name = "ECECR_WO_AMemory"
    def apply(t: Int, s:Int) = new BasicEnvironment(t,s) with ECECR_WO_AMemorySettings with StaticFoodSources with RandomFoodPos
  }

  case object BlueTest extends  EnvironmentBuilder {
    val name = "BlueTest"
    def apply(t: Int, s: Int) = new BasicEnvironment(t, s) with TestSettings with TestSource with TestPos
  }
}

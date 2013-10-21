package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.gui.Sprite
import org.jbox2d.common.Vec2

trait Entity {
  def sprite: Sprite
  def position: Vec2
}

class StaticEntity(s: Sprite) extends Entity{
  override def sprite = s
  override def position = s.position

}

package org.vastness.evo2dsim.simulator

import org.vastness.evo2dsim.gui.Sprite

trait Entity {
  def sprite: Sprite
}

class StaticEntity(s: Sprite) extends Entity{
  def sprite = s
}

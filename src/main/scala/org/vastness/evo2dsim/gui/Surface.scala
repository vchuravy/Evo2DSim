package org.vastness.evo2dsim.gui

import scala.swing.{Graphics2D, Component}
import org.vastness.evo2dsim.simulator.Entity
import java.awt.RenderingHints

class Surface extends Component {
  def draw(g2: Graphics2D) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    for(e: Entity <- EnvironmentManager.visibleEntities){
        e.sprite.draw(g2)
    }
  }

  override def paintComponent(g: Graphics2D) {
    super.paintComponent(g)
    g match {
      case g2: Graphics2D => draw(g2)
      case _ => throw new ClassCastException
    }
  }
}

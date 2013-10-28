package org.vastness.evo2dsim.gui

import javax.swing.JPanel
import java.awt.{RenderingHints, Graphics, Graphics2D}
import org.vastness.evo2dsim.App
import org.vastness.evo2dsim.simulator.Entity
import java.awt

class Surface extends JPanel {
  def draw(g2: Graphics2D) {
    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
    for(e: Entity <- App.visibleEntities){
        e.sprite.draw(g2)
    }
  }

  override def paintComponent(g: Graphics) {
    super.paintComponent(g)
    g match {
      case g2: Graphics2D => draw(g2)
      case _ => throw new ClassCastException
    }
  }

  override def paint(g: Graphics) {
    super.paint(g)
    setBackground(awt.Color.WHITE)
  }
}

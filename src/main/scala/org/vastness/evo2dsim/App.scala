package org.vastness.evo2dsim

import org.vastness.evo2dsim.simulator.World
import org.vastness.evo2dsim.gui.GUI
import javax.swing.{SwingUtilities, JFrame}
import java.util.Timer
import org.jbox2d.common.Vec2

/**
 * @author Valentin Churavy
 */
object App {
  val world = new World
  def getWorld = world

  var timer = new Timer()
  var running = true

  val HERTZ = 60
  val timeStep = 1.0f / HERTZ // 60fps

  val gui = new GUI

  private def updateSimulation() {
    world.step(timeStep)
  }

  private def render() {
    gui.getWorldView.repaint()
  }

  def loop() {
    timer = new Timer()
    timer.schedule(new Loop, 0, 1000 / HERTZ) //new timer at 60 fps, the timing mechanism
  }

  private class Loop extends java.util.TimerTask
  {
    override def run()
    {
      updateSimulation()
      render()

      if (!running)
      {
        timer.cancel()
      }
    }
  }

  def main(args : Array[String]) {
    println( "Evo2DSim is a simple simulator for evolutionary experiments." )

    SwingUtilities.invokeLater(new Runnable() {
      override def run() {
        val frame: JFrame = new JFrame("GUI")
        frame.setContentPane(gui.getPanel)
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE)
        frame.pack()
        frame.setVisible(true)
      }
    })

    // Construct a basic level
    val edges = Array(new Vec2(0.03f,0.03f), new Vec2(0.03f,4f), new Vec2(5f,4f), new Vec2(5f,0.03f))
    world.createWorldBoundary(edges)
    val a = world.addAgent(new Vec2(2,2))
    a.applyForce(new Vec2(3,3))
    loop()
  }

}

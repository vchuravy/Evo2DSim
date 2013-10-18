package org.vastness.evo2dsim

import org.vastness.evo2dsim.simulator.Simulator
import org.vastness.evo2dsim.gui.GUI
import javax.swing.{SwingUtilities, JFrame}
import java.util.Timer
import org.jbox2d.common.Vec2

/**
 * @author Valentin Churavy
 */
object App {
  val sim = new Simulator
  def getWorld = sim

  var timer = new Timer()
  var running = true

  val HERTZ = 120
  val timeStep = 1.0f / HERTZ // 60fps

  val gui = new GUI

  val random = new scala.util.Random()

  private def updateSimulation() {
    sim.step(timeStep)
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
    val origin = new Vec2(0.515f,0.515f)
    val halfSize = 0.5f
    val sizes = Array[Vec2](new Vec2(-halfSize,-halfSize), new Vec2(-halfSize,halfSize), new Vec2(halfSize,halfSize), new Vec2(halfSize,-halfSize))
    val edges = for(i <- 0 until sizes.length) yield origin add sizes(i)
    sim.createWorldBoundary(edges.toArray)
    val a = sim.addAgent(origin, sim.Agents.SBotControllerLinear)
    loop()
  }

}

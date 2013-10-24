package org.vastness.evo2dsim.simulator.teem.enki.sbot

import org.scalatest._
import org.vastness.evo2dsim.simulator.Simulator
import org.jbox2d.common.Vec2
import org.vastness.evo2dsim.teem.enki.sbot._


class SBotLightSensorTest extends FlatSpec with Matchers {
  trait Sim {
    val sim = new Simulator(0)
    val agent1 = sim.addAgent(new Vec2(0,0), sim.Agents.SBotControllerLinearZero).asInstanceOf[SBot]
    val agent2 = sim.addAgent(new Vec2(0,1), sim.Agents.SBotControllerLinearZero).asInstanceOf[SBot]
    sim.step(50.0f/1000.0f)
    val lightSensor1 = agent1.controller.get match{
      case c: SBotController => c.lightSensor
    }
  }

  "Lights" should "be on by default" in new Sim{
    agent1.light.active should be (true)
    agent2.light.active should be (true)
  }

  "SBotLightSensor" should "return 8 input neurons" in new Sim{
    lightSensor1.getNeurons.size should be (8)
  }





}

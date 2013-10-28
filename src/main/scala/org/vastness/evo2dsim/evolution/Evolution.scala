package org.vastness.evo2dsim.evolution

import org.vastness.evo2dsim.environment.{Environment, BasicEnvironment}

class Evolution(poolSize: Int, groupSize: Int, evaluationSteps: Int, timeStep: Int, simSpeed: Int) {
  require(poolSize % groupSize == 0)
  require(timeStep/simSpeed > 0)
  require(evaluationSteps > 0)

  var environments = for(i <- 0 until poolSize % groupSize)
    yield new BasicEnvironment(timeStep, simSpeed, evaluationSteps)

  environments.par.foreach(
    (e: Environment) =>{
      e.initializeStatic()
      e.initializeAgents(groupSize,List.empty[Genome])
    }
  )
}

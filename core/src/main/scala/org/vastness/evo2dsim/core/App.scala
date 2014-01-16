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

package org.vastness.evo2dsim.core

import org.vastness.evo2dsim.core.evolution.{EvolutionConfig, EvolutionRunner}

/**
 * @author Valentin Churavy
 */
object App {
  def main(args : Array[String]) {
    val parser = new scopt.OptionParser[EvolutionConfig]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
      opt[Int]('g', "generations") action { (x, c) =>
        c.copy(generations = x) } text "How many generations are run"
      opt[Int]('s', "steps") action { (x, c) =>
        c.copy(evaluationSteps = x) } text "Steps per Evaluation"
      opt[Int]('e', "evals") action { (x, c) =>
        c.copy(evaluationsPerGeneration = x) } text "Evaluation per Generation"
      opt[Int]('n', "numberOfIndividiums") action { (x, c) =>
        c.copy(poolSize = x) } text "Individiums per Generation"
      opt[Int]('z', "groupSize") action { (x, c) =>
        c.copy(groupSize = x) } text "Group Size"
      opt[String]('c', "evalConf") action { (x, c) =>
        c.copy(envConf = x) } text "Generation:Environment;X:Y..."
      opt[String]('a', "algorithmn") action { (x, c) =>
        c.copy(evolutionAlgorithm = x) } text "Evolution algorithm: sus (Stochastic Universal Sampling) or elite (Elitism)"
      opt[String]('y', "genomeType") action { (x, c) =>
        c.copy(genomeName = x) } text "GenomeType to be used: ByteGenome or NEATGenome"
      opt[String]('x', "genomeSettings") action { (x,c) =>
        c.copy(genomeSettings = x) } text "Specific genome settings (STD -> recurrent:numberHiddenNeurons)"
      opt[Double]('p', "probability") action { (x, c) =>
        c.copy(propability = x) } text "Probability used for Evolution"
      opt[Int]('r', "rLevel") action { (x,c) =>
        c.copy(rLevel = x) } text "Set a recording level , 0 = Everything, 1 = Networks, 2 = Agents, 3 = Nothing"
    }

    parser.parse(args, EvolutionConfig()) map { config =>
      println(s"Using Config: $config")

      for((r, e) <- config.envSetup) println("Running %s from %d until %d".format(e.name, r.start, r.end))

      val evo = new EvolutionRunner(config)

      evo.start()
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }
}

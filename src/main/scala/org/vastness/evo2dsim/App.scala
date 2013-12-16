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

package org.vastness.evo2dsim


import org.vastness.evo2dsim.evolution.{EvolutionRunner, EvolutionBuilder, SUSEvolution}
import org.vastness.evo2dsim.environment.EnvironmentBuilder

/**
 * @author Valentin Churavy
 */
object App {
  def main(args : Array[String]) {
    val parser = new scopt.OptionParser[Config]("scopt") {
      head("Evo2DSim is a simple simulator for evolutionary environment.")
      opt[Int]('t', "timeStep") action { (x, c) =>
        c.copy(timeStep = x) } text "Time step in ms"
      opt[Int]('g', "generations") action { (x, c) =>
        c.copy(generation = x) } text "How many generations are run"
      opt[Int]('s', "steps") action { (x, c) =>
        c.copy(stepsPerEvaluation = x) } text "Steps per Evaluation"
      opt[Int]('e', "evals") action { (x, c) =>
        c.copy(evaluationPerGeneration = x) } text "Evaluation per Generation"
      opt[Int]('n', "numberOfIndividiums") action { (x, c) =>
        c.copy(numberOfIndiviums = x) } text "Individiums per Generation"
      opt[Int]('z', "groupSize") action { (x, c) =>
        c.copy(groupSize = x) } text "Group Size"
      opt[String]('c', "evalConf") action { (x, c) =>
        c.copy(envConf = x) } text "Generation:Environment;X:Y..."
      opt[String]('a', "algorithmn") action { (x, c) =>
        c.copy(envConf = x) } text "Evolution algorithm: sus (Stochastic Universal Sampling) or elite (Elitism)"
    }

    parser.parse(args, Config()) map { config =>
      println("Using evo algorithm" + config.evolutionAlgorithm)
      val envConf = config.envConf.split(';') map {_.split(':').toList } map {
        case x :: List(y) => (x.toInt ,y)
        case _ => throw new IllegalArgumentException(s"Could not parse envConf: ${config.envConf}")
      }
      val envs = parse(config.generation, envConf)
      for((r, e) <- envs) println("Running %s from %d until %d".format(e.name, r.start, r.end))
      val evo = new EvolutionRunner(config.evolutionAlgorithm,config.numberOfIndiviums, config.groupSize, config.stepsPerEvaluation, config.generation, config.evaluationPerGeneration, config.timeStep)
      evo.start(envs)
    } getOrElse {
      sys.exit(1)
      // arguments are bad, usage message will have been displayed
    }
  }

  def parse(max: Int, envs: Seq[(Int, String)]) = _parse(max, envs.toList.sortBy(_._1).reverse)
  private def _parse(next: Int, elems: List[(Int, String)]): List[(Range, EnvironmentBuilder)] = elems match {
    case (gen, name) :: xs => (gen until next, resolve(name)) :: _parse(gen, xs)
    case Nil => List.empty
  }

  private def resolve(name: String) = EnvironmentBuilder.values.find(_.name == name) match {
    case Some(e) => e
    case None => throw new Exception("Could not find: " + name + " in " + EnvironmentBuilder.values)
  }

  case class Config(timeStep: Int = 50, generation: Int = 500, stepsPerEvaluation: Int = 6000, evaluationPerGeneration:Int = 5,  numberOfIndiviums:Int = 500, groupSize: Int = 10, envConf: String = "0:basic", evolutionAlgorithm: String = "sus")
}

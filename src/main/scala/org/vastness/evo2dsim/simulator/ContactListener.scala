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

package org.vastness.evo2dsim.simulator
import org.jbox2d.callbacks
import org.jbox2d.dynamics.contacts.Contact
import org.jbox2d.collision.Manifold
import org.jbox2d.callbacks.ContactImpulse
import org.vastness.evo2dsim.simulator.food.FoodSource
import org.jbox2d.dynamics.Body

/**
 * Listens for contacts between FoodSources and Agents
 * TODO: refactor this to much copy and paste code: Maybe some FP goodness would be needed
 */
class ContactListener extends callbacks.ContactListener {
  override def beginContact(contact: Contact) {
    val body1 = contact.getFixtureA.getBody
    val body2 = contact.getFixtureB.getBody

    extractFoodAgentInteraction(body1, body2) match {
      case None =>
      case Some((flag, food, agent)) => flag match {
        case true => food.feeders += agent
        case false => food.listeners += agent
      }
    }
  }

  override def endContact(contact: Contact) {
    val body1 = contact.getFixtureA.getBody
    val body2 = contact.getFixtureB.getBody

    extractFoodAgentInteraction(body1, body2) match {
      case None =>
      case Some((flag, food, agent)) => flag match {
        case true => food.feeders -= agent
        case false => {
          food.listeners -= agent
          agent.currentReward = 0
        }
      }
    }
  }

  private def extractFoodAgentInteraction(body1: Body, body2: Body): Option[(Boolean, FoodSource, Agent)] = {
    body1.getUserData match {
      case (f: Boolean, food: FoodSource) => body2.getUserData match {
        case a: Agent => Some((f, food, a))
        case _ => None
      }
      case a: Agent => body2.getUserData match {
        case (f: Boolean, food: FoodSource) => Some((f, food, a))
        case _ => None
      }
      case _ => None
    }
  }

  override def preSolve(contact: Contact, oldMainfold: Manifold){}
  override def postSolve(contact: Contact, impulse: ContactImpulse){}
}

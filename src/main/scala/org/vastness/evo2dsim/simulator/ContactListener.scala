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

/**
 * Listens for contacts between FoodSources and Agents
 * TODO: refactor this to much copy and paste code: Maybe some FP goodness would be needed
 */
class ContactListener extends callbacks.ContactListener {
  override def beginContact(contact: Contact) {
    contact.getFixtureA.getUserData match {
      case food: FoodSource if food.feeders.size < food.max =>
        contact.getFixtureB.getBody.getUserData match {
          case agent: Agent => food.feeders += agent
          case _ =>
        }
      case _ =>
    }
    contact.getFixtureB.getUserData match {
      case food: FoodSource if food.feeders.size < food.max=>
        contact.getFixtureA.getBody.getUserData match {
          case agent: Agent =>  food.feeders += agent
          case _ =>
        }
      case _ =>
    }
  }

  override def endContact(contact: Contact) {
    contact.getFixtureA.getUserData match {
      case food: FoodSource =>
        contact.getFixtureB.getBody.getUserData match {
          case agent: Agent =>
            food.feeders -= agent
            agent.currentReward = 0
          case _ =>
        }
      case _ =>
    }
    contact.getFixtureB.getUserData match {
      case food: FoodSource =>
        contact.getFixtureA.getBody.getUserData match {
          case agent: Agent =>
            food.feeders -= agent
            agent.currentReward = 0
          case _ =>
        }
      case _ =>
    }
  }

  override def preSolve(contact: Contact, oldMainfold: Manifold){}
  override def postSolve(contact: Contact, impulse: ContactImpulse){}
}

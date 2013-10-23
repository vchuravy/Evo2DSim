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
      case food: FoodSource => {
        if (food.feeders.size >= food.max + 1) {} else{
          contact.getFixtureB.getBody.getUserData match {
            case agent: Agent => {
              print("Add agent")
              food.feeders += agent
            }
            case _ => {}
          }
        }
      }
      case _ => {}
    }
    contact.getFixtureB.getUserData match {
      case food: FoodSource => {
        if (food.feeders.size >= food.max + 1) {} else{
          contact.getFixtureA.getBody.getUserData match {
            case agent: Agent => {
              print("Add agent")
              food.feeders += agent
            }
            case _ => {}
          }
        }
      }
      case _ => {}
    }
  }

  override def endContact(contact: Contact) {
    contact.getFixtureA.getUserData match {
      case food: FoodSource => {
        contact.getFixtureB.getBody.getUserData match {
          case agent: Agent => {
            food.feeders -= agent
          }
          case _ => {}
        }
      }
      case _ => {}
    }
    contact.getFixtureB.getUserData match {
      case food: FoodSource => {
        contact.getFixtureA.getBody.getUserData match {
          case agent: Agent => {
            food.feeders -= agent
          }
          case _ => {}
        }
      }
      case _ => {}
    }
  }

  override def preSolve(contact: Contact, oldMainfold: Manifold){}
  override def postSolve(contact: Contact, impulse: ContactImpulse){}
}

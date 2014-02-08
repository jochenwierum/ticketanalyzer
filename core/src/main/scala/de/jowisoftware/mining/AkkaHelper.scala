package de.jowisoftware.mining

import akka.actor.ActorSystem

object AkkaHelper {
  val system = ActorSystem("MiningSystem")
}
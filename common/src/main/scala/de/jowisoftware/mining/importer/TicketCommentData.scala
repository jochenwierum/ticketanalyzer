package de.jowisoftware.mining.importer

import java.util.Date

case class TicketCommentData(
  id: Int,
  text: String,
  author: String,
  submitted: Date = new Date,
  modified: Date = new Date)
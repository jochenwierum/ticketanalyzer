package de.jowisoftware.mining.importer.redmine

object Main {
  def main(args: Array[String]) {
    val config = Map(
        "url" -> "http://jowisoftware.de:3000/",
        "key" -> "2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5",
        "repositoryname" -> "default",
        "project" -> "1")

    new RedmineImporter().importAll(config, null)
  }
}

/*
Key besser in X-Redmine-API-Key

25 Tickets auflisten:
http://jowisoftware.de:3000/issues.xml?key=2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5&offset=0&limit=25&project_id=1

25 weitere Tickets auflisten:
http://jowisoftware.de:3000/issues.xml?key=2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5&offset=25&limit=25&project_id=1

Details zu Ticket 1 abrufen:
http://jowisoftware.de:3000/issues/1.xml?key=2ae8befe0e72f5cc5c3f0e8f364fe1c34ee340b5&include=children,attachments,relations,changesets,journals
*/
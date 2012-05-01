package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink

trait ScmScanner {
  def scan(text: String): Set[ScmLink]
}
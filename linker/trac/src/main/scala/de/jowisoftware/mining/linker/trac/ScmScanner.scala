package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.linker.ScmLink

private[trac] trait ScmScanner {
  def scan(text: String): Set[ScmLink]
}
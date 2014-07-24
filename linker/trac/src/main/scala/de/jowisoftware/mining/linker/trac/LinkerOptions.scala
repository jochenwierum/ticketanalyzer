package de.jowisoftware.mining.linker.trac

import de.jowisoftware.mining.UserOptions

class LinkerOptions extends UserOptions("linker.tracstyle") {
  protected val defaultResult: Map[String, String] = Map()

  protected val htmlDescription = """<b>Trac Style Linker</b><p>
    This linker links tickets and commits with the following patterns<br />
    (note that this linker is not limited to trac itself):
    </p><ul>
      <li>Commits:<ul>
      <li>r12</li>
      <li>[25]</li>
      <li>[23/trunk]</li>
      <li>r42:87</li>
      <li>[1:7/tags/tagX]</li>
      <li>log:/trunk@2-19</li>
      </ul></li>
      <li>Tickets:<ul>
        <li>#7</li>
        <li>ticket:6</li>
        <li>Mantis:1</li>
        <li>Mantis: 2</li>
      </ul></li>
    </ul><p>Alpha numerical commit ids are also supported</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel): Unit = {}
}

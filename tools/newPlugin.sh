#!/bin/bash

set -e

askString() {
	message="$1"
	default="$2"

	read -r -p "$message [$2] "
	if [ -z "$REPLY" ]; then
		echo $default
	else
		echo $REPLY
	fi
}

askBool() {
	message="$1"
	default="$2"

	if [ "$default" -eq 1 ]; then
		defmsg="[Y/n]"
		default=1
	else
		defmsg="[y/N]"
		default=0
	fi

	while true; do
		read -r -p "$message $defmsg " -n 1

		if [ -z "$REPLY" ]; then
			echo $default
			return
		elif [ "$REPLY" == 'y' -o "$REPLY" == 'Y' ]; then
			echo "" >&2
			echo 1
			return
		elif [ "$REPLY" == 'n' -o "$REPLY" == 'N' ]; then
			echo "" >&2
			echo 0
			return
		else
			echo "" >&2
		fi
	done
}

askType() {
	PS3="Please select plugin type: "
	types=("Importer/ITS" "Importer/SCM" "Linker" "Analyzer")

	select opt in "${types[@]}"; do
		case $opt in 
			Importer/ITS) echo 1; break ;;
			Importer/SCM) echo 2; break ;;
			Linker) echo 3; break ;;
			Analyzer) echo 4; break ;;
			*) ;;
		esac
	done
}

addDirectoryStructure() {
	dirName=${internalName,,}
	case $ptype in
		1)
			target="importer/$dirName"
			;;
		2)
			target="importer/$dirName"
			;;
		3)
			target="linker/$dirName"
			;;
		4)
			target="analyzer/$dirName"
			;;
	esac

	if [ -e $target ]; then
		echo "Error! This plugin already exists!" >&2
		exit 1
	fi

	nsdir=$(echo $namespace | sed 's/\./\//g')
	mkdir -p $target/src/main/scala/$nsdir
	mkdir -p $target/src/main/resources/META-INF
	mkdir -p $target/src/main/settings
	mkdir -p $target/src/test/scala/$nsdir
	mkdir -p $target/src/test/resources

	echo $target
}

addProjectFiles() {
	case $ptype in
		1)
			gradleName="importer"
			propertiesType=ITS
			mainSuffix=Importer
			addImporterFiles
			;;
		2)
			gradleName="importer"
			propertiesType=SCM
			mainSuffix=Importer
			addImporterFiles
			;;
		3)
			gradleName="linker"
			propertiesType=Linker
			mainSuffix=Linker
            addLinkerFiles
			;;
		4)
			gradleName="analyzer"
			propertiesType=Analyzer
			mainSuffix=Analyzer
            addAnalyzerFiles
			;;
	esac
	if [ $facade -eq 1 ]; then
		mainSuffix=Facade
	fi

	cat > $dir/build.gradle <<EOF
description = "$name $gradleName plugin"

dependencies {
}

eclipse {
  project {
    name = '$gradleName-$internalName'
  }
}
EOF

	cat > $dir/src/main/resources/META-INF/plugindata.properties <<EOF
class=$namespace.${internalName}${mainSuffix}
type=$propertiesType
name=$name
EOF
}

addOptionsFile() {
	cat > $srcDir/${internalName}Options.scala <<EOF
package $namespace

import de.jowisoftware.mining.UserOptions
import scala.swing.{ GridPanel, Panel }

class ${internalName}Options extends UserOptions("importer.${internalName}") {
  protected val defaultResult: Map[String, String] = Map()

  protected val htmlDescription = """<p><b>${name}</b><br>
    Description of ${name}</p>"""

  protected def fillPanel(panel: CustomizedGridBagPanel) {
    // TODO: finish panel
  }
}
EOF
}

addImporterFiles() {
	if [ $facade -eq 1 ]; then
		cat > $srcDir/${internalName}Facade.scala <<EOF
package $namespace

import de.jowisoftware.mining.importer.{ Importer, ImportEvents }

class ${internalName}Facade extends Importer {
  def userOptions = new ${internalName}Options
	
  def importAll(config: Map[String, String], events: ImportEvents) =
    new ${internalName}Importer(config, events).run()
}
EOF

		cat > $srcDir/${internalName}Importer.scala <<EOF
package $namespace

import de.jowisoftware.mining.importer.ImportEvents

class ${internalName}Importer(config: Map[String, String], events: ImportEvents) {
  def run() {
    // TODO: add your stuff here
    events.finish
  }
}
EOF
	else
		cat > $srcDir/${internalName}Importer.scala <<EOF
package $namespace

import de.jowisoftware.mining.importer.{ Importer, ImportEvents }

class ${internalName}Importer extends Importer {
  def userOptions = new ${internalName}Options
	
  def importAll(config: Map[String, String], events: ImportEvents) {
    // TODO: add your stuff here
    events.finish
  }
}
EOF
	fi

	addOptionsFile
}

addLinkerFiles() {
	if [ $facade -eq 1 ]; then
		cat > $srcDir/${internalName}Facade.scala <<EOF
package $namespace

import de.jowisoftware.mining.linker.{ Linker, LinkEvents }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository }

class ${internalName}Facade extends Linker {
  def userOptions = new ${internalName}Options
	
  def link(tickets: TicketRepository, commits: CommitRepository, options: Map[String, String], events: LinkEvents) =
    new ${internalName}Linker(tickets, commits, options, events).run()
}
EOF

		cat > $srcDir/${internalName}Linker.scala <<EOF
package $namespace

import de.jowisoftware.mining.linker.LinkEvents
import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository }

class ${internalName}Linker(tickets: TicketRepository, commits: CommitRepository, options: Map[String, String], events: LinkEvents) {
  def run() {
    // TODO: add your stuff here
  }
}
EOF
	else
		cat > $srcDir/${internalName}Linker.scala <<EOF
package $namespace

import de.jowisoftware.mining.linker.{ Linker, LinkEvents }
import de.jowisoftware.mining.model.nodes.{ TicketRepository, CommitRepository }

class ${internalName}Linker extends Linker {
  def userOptions = new ${internalName}Options

  def link(tickets: TicketRepository, commits: CommitRepository, options: Map[String, String], events: LinkEvents) {
	// TODO: add your stuff here
  }
}
EOF
	fi

	addOptionsFile
}

addAnalyzerFiles() {
	if [ $facade -eq 1 ]; then
		cat > $srcDir/${internalName}Facade.scala <<EOF
package $namespace

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class ${internalName}Facade extends Analyzer {
  def userOptions = new ${internalName}Options

  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) =
    new ${internalName}Analyzer(db, options, parent, waitDialog).run()
}
EOF

		cat > $srcDir/${internalName}Analyzer.scala <<EOF
package $namespace

import scala.swing.Frame

import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class ${internalName}Analyzer(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {
  def run() {
    // TODO: add your stuff here
  }
}
EOF
	else
		cat > $srcDir/${internalName}Analyzer.scala <<EOF
package $namespace

import scala.swing.Frame

import de.jowisoftware.mining.analyzer.Analyzer
import de.jowisoftware.mining.gui.ProgressDialog
import de.jowisoftware.mining.model.nodes.RootNode
import de.jowisoftware.neo4j.Database

class ${internalName}Analyzer extends Analyzer {
  def userOptions = new ${internalName}Options

  def analyze(db: Database[RootNode], options: Map[String, String], parent: Frame, waitDialog: ProgressDialog) {
	// TODO: add your stuff here
  }
}
EOF
	fi

	addOptionsFile
}



ptype=$(askType)
name=$(askString "name of the plugin:" "Test Plugin")
internalName=$(askString "internal name of the plugin (no whitespaces allowed):" "Test")
namespace=$(askString "namespace of the plugin:" "org.example.testPlugin")
facade=$(askBool "add a facade to the plugin?" 1)
executeEclipse=$(askBool "add eclipse project?" 1)
executeBuild=$(askBool "build project after creating the plugin?" 1)


dir=$(addDirectoryStructure)
srcDir=$dir/src/main/scala/$(echo $namespace | sed 's/\./\//g')
addProjectFiles


if [ $executeEclipse -eq 1 ]; then
	./gradlew eclipse processResources
fi

if [ $executeBuild -eq 1 ]; then
	./gradlew dist
fi

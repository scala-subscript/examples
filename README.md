# SubScript examples
This repository contains examples of the usage of SubScript.

## Prerequisites
You will need the following software in order to run the examples:

1. Git client - [official website](https://git-scm.com/), [download manual](https://git-scm.com/downloads)
2. SBT build tool - [official website](http://www.scala-sbt.org/), [download manual](http://www.scala-sbt.org/download.html)

Also, you need to know the basics of working with the command line.

## Getting started
To try the examples:

1. Clone this repository by running `git clone https://github.com/scala-subscript/examples.git`
2. Go to the root of the repository: `cd examples`
3. Launch the SBT console by running `sbt`
4. Run `projects` from the SBT console. You will see the list of all the examples in the repository, each belonging to its own project. `root` is not an example, but the root project, so ignore it.
5. To run an example, run the `<exmaple_project_name>/run` command, where `<example_project_name>` is the name of the desired example, as listed in the `projects` command's output. For example, if you want to try the "lookupframe" example, run `lookupframe/run`.
6. Some examples have more then one object with the `main` method. In this case the console will prompt you to specify what you want to run. In case of "Lookup Frame", entering `1` in the prompt will run a simple "LookupFrame", and entering `2` will run the more complex "LookupFrame2".
7. In order to start developing with SubScript, please head to the [main repository](https://github.com/scala-subscript/subscript) and follow its "Getting Started guide"

## Debuggin the projects
You can use graphical debugger to see how the projects work. To do so:

1. From the examples' root, run `sbt`.
2. Set the current project to a desired one: `project projectName`, where `projectName` is a name from `projects` command output.
3. Some projects have more then one main class. To see the project's main classes, run `show discoveredMainClasses`.
4. If the project has more than one main class, you'll have to specify which one you want to debug: `set mainClass in Compile := Some("main_class_name")`, where `main_class_name` is some name from the `show discoveredMainClasses` output. If the project has only one main class, no action is needed, it will be used automaticaly.
5. Run `ssDebug`.

## Examples description
- **helloworld** is a classic Hello World application
- **ab** is a swing application to demonstrate some basic scripts
- **lookupframe** demonstrates a more complex use case of scripting in the context of Swing
- **life** is the [Game of Life](https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life) simulator
- **twitter** searches Twitter's most recent tweets with a given keyword. Features dataflow, where the application is reactively bound to the input field, so that any input causes a new search.
- **pingpong** is a use case of SubScript Akka Actors. Two actors that exchange a message three times.
- **taskprocessor** is a more complex example of SubScript Actors. An actor system that is designed to process any given task by forking it, delegating parts to different actors and then joining the results together.

## Repository structure
The repository contains multiple SBT project folders with the corresponding examples. `build.sbt` is a multi-project build file binding the examples together. Each of the example projects is defined by its own `build.sbt` that can be found in its folder. The SubScript SBT plugin is defined for all the projects in `project/build.sbt`.
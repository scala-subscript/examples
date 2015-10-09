# SubScript examples
This repository contains examples of the usage of SubScript.

## Getting started
To try the examples:

1. Clone the repository by running `git clone https://github.com/scala-subscript/examples.git`
2. From the root of the repository, run `sbt`. The SBT console will be opened.
3. Run `projects` from the console. You will see the list of all the examples in the repository, each belonging to its own project.
4. To run an example, run the `<exmaple_project_name>/run` command. For example, if you want to try the "lookupframe" example, run `lookupframe/run`.
5. Some examples have more then one object with the `main` method. In this case the console will prompt you to specify what you want to run. In case of "Lookup Frame", entering `1` in the prompt will run a simple "LookupFrame", and entering `2` will run the more complex "LookupFrame2".

## Examples description
- *helloworld* is a classic Hello World application
- *ab* is a swing application to demonstrate some basic scripts
- *lookupframe* demonstrates a more complex use case of scripting in the context of Swing
- *life* is the (Game of Life)[https://en.wikipedia.org/wiki/Conway%27s_Game_of_Life] simulator
- *twitter* searches Twitter's most recent tweets with a given keyword. Features dataflow, where the application is reactively bound to the input field, so that any input causes a new search.
- *pingpong* is a use case of SubScript Akka Actors. Two actors that exchange a message three times.
- *taskprocessor* is a more complex example of SubScript Actors. An actor system that is designed to process any given task by forking it, delegating parts to different actors and then joining the results together.

## Repository structure
The repository contains multiple SBT project folders with the corresponding examples. `build.sbt` is a multi-project build file binding the examples together. Each of the example projects is defined by its own `build.sbt` that can be found in its folder. The SubScript SBT plugin is defined for all the projects in `project/build.sbt`.
# The Game of Life

We will build a GUI controller for Conway’s game of Life. In this game a human player defines which cells of a playboard are alive or dead, so that an initial pattern of living cells appears on a computer screen. The pattern on the board starts to change according to a certain algorithm, as soon as the player commands so.

![screenshot](http://subscript-lang.org/wp-content/uploads/2012/11/Game-of-Life.png)

## Conway's game of Life

We start by creating a class BasicLifeFrame, for the main window with a toolbar, a list of predefined life creatures and a special drawing area. All buttons should be disabled by default, as the SubScript GUI controller will enable and disable them as needed. This is all plain Scala-swing code; the source is here at the SubScript project site.

Class LifeFrame inherits from BasicLifeFrame, and it will contain the scripts that implement the global control of flow. A class LifeDrawingPanel will perform the computations of the Life algorithm, and draw the results efficiently on the screen.

This way the Model View Controller paradigm (MVC) applies nicely:

* LifeDrawingPanel is the model, programmed in hand-written Scala
* BasicLifeFrame is the view, implemented in Scala; a GUI painter could have generated most code.
* LifeFrame is the controller, programmed in SubScript

## Global Behaviour

The user will be able to

* operate on the drawing panel (randomizing, clearing, stepping etc.)
* paint on the drawing panel using the mouse
* copy a selected life creature from the list to the drawing panel
* change the speed all the time exit the application at any time
* exit, after confirming to a confirmation dialog

```scala
live =|| boardControl speedControl mouseInput doexit
```

In the Lookup example application, the exit script had the confirmation dialog invoked in the condition of a while construct. It runs synchronously in the Swing thread using SwingUtilities.invokeAndWait. This means that the rest of the SubScript application is inactive as long as the confirmation dialog is up. For the Game of Life it would be nicer when the life area remains active. Therefore the confirmation dialog is run in a code fragment so that it runs asynchronously in the Swing thread, under the hood using SwingUtilities.invokeLater.
Just as in the Lookup example application, an exit sequence starts with the user giving the command to exit, by pressing the exit button or by clicking in the close box.

```scala
doexit       =   exitCommand var r=false @gui: {r=confirmExit} while (!r)
exitCommand  =   exitButton + windowClosing
```

## Board Control

The user controls the game board by

* single stepping: getting a new Life generation on the board
* multistepping: getting a sequence of generations, with a controlled speed
* randomizing the board contents
* clearing the board contents
We could write this as a sequential loop of or-parallel processes:

```scala
boardControl     = ...; singleStep || multiStep || clear || randomize
```
Because of the Or-parallelism, multistepping would be disrupted by a command for singleStep, clear or randomize. This is a bit flawed: singleStep should not disrupt multistep, because it would also perform another step. For a better user interaction, multistepping should be stopped using its own stop command.
To avoid disruption by singleStep we need to bring it in the same operand of the or-parallelism as multiStep. As there are zero or more singleSteps possible before one multiStep we can write:

```scala
boardControl     = ...; (..singleStep) multiStep || clear || randomize
```

The randomize and clear operations should be done in the GUI thread; they could happen in normal code fragments, but it is a bit faster to apply a “tiny code fragment”, which acts in ACP terms like a neutral element rather than an atomic action.
To do 1 generation step, the new board contents is computed; this may be time consuming so it is done in the background thread; thereafter the board is repainted in the GUI thread.
Multistepping does a sleep after each generation step, to comply with the desired speed. This sleeping is also done in a background thread. This will be interrupted when the user commands to stop multistepping, or to randomize or to clear.

```scala
randomize        =   randomizeCommand @gui: {!board.doRandomize!}
clear            =       clearCommand @gui: {!board.doClear!}
singleStep       =        stepCommand do1Step
 multiStep       = multiStepStartCmd; ...do1Step {*sleep*} / multiStepStopCmd
   do1Step       = {*board.calculateGeneration*} @gui: {!board.repaint!}
```

The game board operations are controlled by pressing buttons and keys:

```scala
multiStepStartCmd  =     startButton + Key.Enter
 multiStepStopCmd  =      stopButton + Key.Enter
      stepCommand  =      stepButton + ' '
 randomizeCommand  = randomizeButton + 'r'
     clearCommand  =     clearButton + 'c'
```

## Speed Control

The user can set a speed variable that varies from 1 to 10, which in turn controls the sleeping time when multistepping. Changing the variable also requires changing the speed label text, so this must be done in the GUI thread.
By splitting up the sleeping in small chunks of calls to Thread.sleep, the application will react fast to ordered speed increments.
The user has several options to control the speed:

* using the slider
* pressing a speed button: ‘<’-faster, ‘>>’-max button
* pressing a digit key ’1′,’2′…’9′,’0′
The buttons should only be enabled when applicable, so two will be disabled when the speed is at its minimum or maximum. This is guarded using if-constructs. We do not need an active guard as in the Lookup application because we can easily enforce reevaluations of the guarding conditions. This is done by making speed control an eternal loop of a choice between single speed change actions. This way after each change action the involved scripts are all deactivated and reactivated.

```scala
  speedControl     = ...; speedSliderInput + speedButtonInput + speedKeyInput
 
setSpeed(s: Int)   = @gui: {!setSpeed(s)!}
 
  speedSliderInput = speedSlider setSpeed(speedSlider.value)
 
  speedButtonInput = if (speed>minSpeed) speedDecButton
                   + if (speed<maxSpeed) speedIncButton
 
    speedDecButton = minSpeedButton setSpeed(minSpeed)
                   +   slowerButton setSpeed(speed-1)
 
    speedIncButton = maxSpeedButton setSpeed(maxSpeed)
                   +   fasterButton setSpeed(speed+1)
```

There are 10 digit keys for speed control, so we make an alternative loop that makes 10 passes using the iterator times. This iterator has been predefined as

```scala
times(n:Int) = while(pass<n)
```

pass is a loop counter that is implicitly taken from the current location, here.
Inside the loop is a sequence of

* a key pressed event
* a speed change action
These both need as parameter a character code equal to the loop counter plus ’0′.
The expression pass+'0' would not suffice: pass would refer to the event-action sequence rather than the alternative loop; we need to go one level up, which is done using pass_up1. So the script becomes:

```scala
speedKeyInput    = times,10
                 + val c = (pass_up1+'0').toChar
                   key,c
                   setSpeed(digit2Speed(c))
```

## Mouse Input

Clicking in the board will toggle the state of the clicked cells, or insert a predefined Life structure that is selected at the list on the left.

To draw curves on the board you can drag the mouse. This is a bit uncomfortable: the mouse button needs to be pressed all the time. An alternative would be to double-click, then move the mouse pointer around which draws the curves, and end this by another double-click.

```scala
mouseInput    = (mouseClickInput & mouseDragInput)
              /  doubleClick (mouseMoveInput / doubleClick) ; ...
```

For this purpose the mouseInput script is continuously willing to handle both mouse press events and mouse drag events. This is supported by scripts that will activate a looping event handling code fragment; that way mouse move events and mouse drag events will efficiently call a call back method doMouseDraw that does the drawing:

```scala
mouseDragInput  = mouseDraggings(board, (e: MouseEvent) => doMouseDraw(e.point))
mouseMoveInput  = mouseMoves(    board, (e: MouseEvent) => doMouseDraw(e.point))
```

There is a minor problem, though: a mouse double click event (that switches to and from drawing just by moving the mouse pointer) does never arrive on its own; a little while earlier a mouse single click event arrives. This single click event should not immediately be handled as usual for toggling a board cell or inserting a predefined structure at the board. There should be some delay during which the double click event may disrupt the handling of the single click event. This is programmed as:

```scala
mouseClickInput  = var p:java.awt.Point
                 ; mouseSingleClick(board, ?p)
                   ( {*sleep_ms(220)*} break_up2
                   / mouseDoubleClick(board, ?p) )
                   ...
                 ; {! doMouseSingleClick(p) !}
                 ; ...
```

break_up2 is like break, but it works two levels up (1 level up would be the disrupt operator /).

Note: this may not be an ideal user interface: the handling of single clicking is delayed; even though this is about a quarter of a second, its look and feel is a bit clumsy. In general it is better to let double clicks have a kind of amplified effect of single clicks; and likewise for triple clicks and quadruple clicks. E.g. a single click may set the text cursor location; a double click selects a word; a triple click selects a paragraph; a quadruple click selects the entire document. But for this Life application the deviant handling of double clicks makes some sense, and as the code shows, it is not too hard to program this.

## Epilog

The controller takes only 39 non empty source code lines. It makes everything happen in the appropriate thread, and buttons are enabled only at the right times.
The controller is essentially a piece of event driven math.

The controller specification is also a kind of grammar for the interface between the game and the user, describing the allowed sequences of user actions and the responses.

YACC creator Stephen C. Johnson said in 2008:

The ideas and techniques underlying YACC are fundamental and have application in many areas of computer science and engineering. One application I think is promising is using compiler-design techniques to design GUIs – I think GUI designers are still writing GUIs in the equivalent of assembly language, and interfaces have become too complicated for that to work any more.


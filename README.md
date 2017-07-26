# launchpadInterface
This is my own "library" for the Novation Launchpad S which you can use when you want to write an application for that device. I have various other "launchpad*****" repositories here: these are all games or other fun projects which make use of this.

## How to use it
There are two ways to use the launchpadInterface class.
* 1: Just create an instance and call its methods to send messages to the Launchpad. If you also want to receive data, you have to make an anonymous class which overwrites the methods `buttonPressed(int row, int column)` and `buttonReleased(int row, int column)` which are called when a button on the Launchpad is pressed (including the round buttons on the sides)
* 2: How I usually do it: Extend the class. The only thing you have to do is to implement a constructor without any parameters which can throw a `javax.sound.midi.MidiUnavailableException`. The methods `buttonPressed(int row, int column)` and `buttonReleased(int row, int column)`, as said above, are called when a button on the Launchpad is pressed. Have a look at the following example.

### Example
(This uses the second method)

This program lights a button when it is pressed and turns the light off when it is released. The color is yellow. For the other colors, just look into the LaunchpadInterface.java file. The color definitions are right at the top. You can also choose a color by using the `color(int red, int green)` method, which takes a red value and a green value, each ranging from 0 to 3, and returns the color.

Main.java:
```
public class Main {
    try {
        
        // Create an instance of our extended class. When no parameters are specified, the
        // LaunchpadInterface searches for a Novation Launchpad S on its own
        MyLaunchpad launchpad = new MyLaunchpad();
        
        launchpad.open(); // Start the midi connection
        
        // Wait until the user presses enter, so the program will not stop
        // immediately after it started
        System.console().readLine();
        
        launchpad.close(); // Release all resources and turn all the lights off
        
    } catch (javax.sound.midi.MidiUnavailableException e) {
        // No Launchpad S was found
        e.printStackTrace();
    }
}
```

MyLaunchpad.java:
```
import javax.sound.midi.MidiUnavailableException;

public class MyLaunchpad extends LaunchpadInterface {
    
    public MyLaunchpad() throws MidiUnavailableException {
        super();
    }
    
    // This method is called once when a button is pressed on the Launchpad.
    // When one of the round buttons at the top is pressed, parameter "row" is -1.
    // When one of the round buttons on the right side is pressed, parameter
    // "column" is 8.
    public void buttonPressed(int row, int column) {
        // Turn the light for the pressed button on, with
        // color STRONG_YELLOW (Launchpad equivalent of #FFFF00)
        turnOn(row, column, STRONG_YELLOW);
    }
    
    // This method is called once when a button is released on the Launchpad.
    public void buttonReleased(int row, int column) {
        // Turn the light for the pressed button off
        turnOff(row, column);
    }
}

```

import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Transmitter;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.InvalidMidiDataException;
import java.util.Arrays;

public class LaunchpadInterface implements Receiver {
	private byte[][] buttons = new byte[9][9]; // like that: buttons[row][column] (buttons[0][8] isn't used)
	// -1 is value for empty cell
	
	public static final byte
			DEFAULT = 127,
			EMPTY = -1, // (LaunchpadInterface)-built-in constant, describes a "black" (turned off) cell
			NOT_DEFINED = 0, // Color which displays as "black", but is different from EMPTY, meaning it counts as a turned on cell whose color is not yet set
			EMPTY_COLOR = 12, // "Color" which cannot be seen, acts the same as turnOff method
			STRONG_YELLOW = 63,
			MEDIUM_YELLOW = 46,
			LIME_1 = 62,
			WEAK_LIME_1 = 45,
			LIME_2 = 61,
			STRONG_GREEN = 60,
			MEDIUM_GREEN = 44,
			WEAK_GREEN = 28,
			STRONG_ORANGE = 31,
			MEDIUM_ORANGE = 47,
			WEAK_ORANGE = 30,
			WEAK_YELLOW = 29,
			STRONG_RED = 15,
			MEDIUM_RED = 14,
			WEAK_RED = 13;
	
	private static final byte[] visibleColorArray = {
			STRONG_YELLOW, 
			MEDIUM_YELLOW, 
			LIME_1, 
			WEAK_LIME_1, 
			LIME_2, 
			STRONG_GREEN, 
			MEDIUM_GREEN, 
			WEAK_GREEN, 
			STRONG_ORANGE, 
			MEDIUM_ORANGE, 
			WEAK_ORANGE, 
			WEAK_YELLOW, 
			STRONG_RED, 
			MEDIUM_RED, 
			WEAK_RED
	};
	
	public static final int visibleColors = visibleColorArray.length;
	
	private int preparingLevel = 0;
	private Shape preparedShape = null;
	
	private MidiDevice inputDevice;
	private MidiDevice outputDevice;
	private Transmitter input;
	private Receiver output;
	
	private boolean isOpen = false;
	
	public LaunchpadInterface(MidiDevice input, MidiDevice output) {
		this.inputDevice = input;
		this.outputDevice = output;
		
		fillArray((byte) -1);
	}
	
	public LaunchpadInterface() throws MidiUnavailableException {
		inputDevice = null;
		outputDevice = null;
		MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
		
		for (MidiDevice.Info info : infos) {
			if (info.getName().split(" ")[0].equals("S")) { // "info.getName().equals("Launchpad S")" for Windows
				if (inputDevice == null) {
					inputDevice = MidiSystem.getMidiDevice(info);
				} else if (outputDevice == null) {
					outputDevice = MidiSystem.getMidiDevice(info);
					break;
				}
			}
		}
		
		if (inputDevice == null || outputDevice == null) {
			throw new MidiUnavailableException("No Launchpad S connected");
		} else {
			fillArray((byte) -1);
		}
	}
	
	public void send(MidiMessage message, long timestamp) {
		// Decrypt message
		byte[] msg = message.getMessage();
		byte button = msg[1];
		int row = 0;
		int column = 0;
		if (msg[0] == -112) {
			column = button % 16;
			row = button / 16;
		} else if (msg[0] == -80) {
			row = -1;
			column = button - 104;
		}
		
		// Check for validity
		if (!positionInBounds(row, column)) {
			return;
		}
		
		if (msg[2] == 0) {
			buttonReleased(row, column);
		} else {
			buttonPressed(row, column);
		}
	}
	
	private static byte positionToCode(int row, int column) throws IndexOutOfBoundsException {
		if (!positionInBounds(row, column)) {
			throw new IndexOutOfBoundsException("Row " + row + " and column " + column + " out of bounds.");
		}
		if (row == -1) {
			return (byte) (104 + column);
		} else {
			return (byte) (row * 16 + column);
		}
	}
	
	private static byte positionToCommand(int row, int column) {
		if (!positionInBounds(row, column)) {
			throw new IndexOutOfBoundsException("Row " + row + " and column " + column + " out of bounds.");
		}
		if (row == -1) {
			return -80;
		} else {
			return -112;
		}
	}
	
	public static boolean positionInBounds(int row, int column) {
		if (row < -1
				|| row >= 8
				|| column < 0
				|| column >= 9
				|| row == -1 && column == 8) {
			return false;
		}
		
		return true;
	}
	
	public static boolean positionInStrictBounds(int row, int column) {
		if (row < 0
				|| row >= 8
				|| column < 0
				|| column >= 8) {
			return false;
		}
		
		return true;
	}
	
	private static byte color(int red, int green, boolean copy, boolean clear) { // Generates color code. Red and green parameters must be 0, 1, 2 or 3
		if (red < 0 || red > 3) {
			throw new IllegalArgumentException("Red value out of range: " + red);
		}
		if (green < 0 || green > 3) {
			throw new IllegalArgumentException("Green value out of range: " + green);
		}
		
		byte color = (byte) (16 * green + red);
		
		color &= ~0b1100; // Resets copy and clear bits
		if (copy) color |= 0b100;
		if (clear) color |= 0b1000;
		
		return color;
	}
	
	public static byte color(int red, int green) { // Generates color code. Red and green parameters must be between 0 and 3 inclusively
		return color(red, green, true, true);
	}
	
	public static byte getColorByIndex(int index) {
		return visibleColorArray[index];
	}
	
	protected void buttonPressed(int row, int column) {}
	protected void buttonReleased(int row, int column) {}
	
	public void turnOn(int row, int column, byte color) {
		if (color == -1) {
			turnOff(row, column);
		}
		if (isPreparing()) {
			preparedShape.setButton(row + 1, column, color);
			return;
		}
		if (getButton(row, column) == color) {
			return; // No need to turn on
		}
		
		IndexOutOfBoundsException indexException = null;
		
		try {
			byte cmd = (byte) (row == -1 ? 0xB0 : 0x90);
			byte code = positionToCode(row, column);
			
			sendMessage(cmd, code, color);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			indexException = e;
		}
		
		if (indexException == null) {
			buttons[row + 1][column] = color;
		} else {
			throw indexException;
		}
	}
	
	public void turnOn(ShapeView shape) {
		if (shape == null) {
			throw new NullPointerException();
		}
		
		for (Button button : shape) {
			if (button.color != -1) {
				turnOn(button.row, button.column, button.color);
			}
		}
	}
	
	public void replace(ShapeView shape) {
		if (shape == null) {
			throw new NullPointerException();
		}
		
		for (Button button : shape) {
			turnOn(button.row, button.column, button.color);
		}
	}
	
	public void turnOff(ShapeView shape) {
		if (shape == null) {
			throw new NullPointerException();
		}
		
		for (Button button : shape) {
			if (button.color != -1) {
				turnOff(button.row, button.column);
			}
		}
	}
	
	public void turnOff(int row, int column) {
		if (!positionInBounds(row, column)) return;
		
		if (isPreparing()) {
			preparedShape.setButton(row + 1, column, (byte) -1);
			return;
		}
		if (getButton(row, column) == -1) {
			return; // No need to turn off
		}
		
		boolean indexException = false;
		
		try {
			byte cmd = (byte) (row == -1 ? 0xB0 : 0x80);
			byte code = positionToCode(row, column);
			
			sendMessage(cmd, code, 0);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		} catch (IndexOutOfBoundsException e) {
			indexException = true;
		}
		
		if (!indexException) {
			buttons[row + 1][column] = -1;
		}
	}
	
	public byte getButton(int row, int column) { // Returns color of specified button, -1 if turned off
		if (!positionInBounds(row, column)) {
			throw new IllegalArgumentException("Button position out of range: Row " + row + ", Column " + column);
		}
		
		return buttons[row + 1][column];
	}
	
	@Deprecated
	public byte[][] getCompleteButtonArray() {
		return buttons;
	}
	
	// Returns unique
	public byte[][] getButtonArray() {
		byte[][] array = new byte[8][];
		for (int i = 0; i < 8; i++) {
			array[i] = Arrays.copyOf(buttons[i + 1], 8);
		}
		
		return array;
	}
	
	public byte[][] getFullButtonArray() {
		byte[][] array = new byte[9][];
		for (int i = 0; i < 9; i++) {
			if (i == 0) {
				array[i] = new byte[9];
				array[i][8] = -1;
				for (int j = 0; j < 8; j++) {
					array[i][j] = buttons[i][j];
				}
			} else {
				array[i] = Arrays.copyOf(buttons[i], 9);
			}
		}
		
		return array;
	}
	
	public void test(int mode) { // 0 = low, 1 = medium, 2 = full (brightness)
		if (mode < 0 || mode > 2) {
			throw new IllegalArgumentException("Mode out of range: " + mode);
		}
		
		fillArray(color(mode + 1, mode + 1));
		
		try {
			sendMessage(0xB0, 0, 125 + mode);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isPreparing() {
		return preparingLevel != 0;
	}
	
	// Returns unique
	public Shape getShape() {
		return new Shape(getButtonArray());
	}
	
	// Returns unique
	public Shape getFullShape() {
		return new Shape(getFullButtonArray());
	}
	
	public void setPreparing(boolean state) {
		if (state == false && preparingLevel == 0) {
			throw new IllegalStateException();
		}
		
		if (state == true) {
			preparingLevel++;
			preparedShape = getFullShape();
		} else {
			preparingLevel--;
			ShapeView shape = new ShapeView(preparedShape);
			for (Button button : shape) {
				if (button.row == 0 && button.column == 8) continue;
				turnOn(button.row - 1, button.column, button.color);
			}
			preparedShape = null;
		}
	}
	
	private void fillArray(byte n) {
		for (byte[] row : buttons) {
			Arrays.fill(row, n);
		}
	}
	
	public void setDutyCycle(int numerator, int denominator) {
		if (numerator < 1 || numerator > 16) {
			throw new IllegalArgumentException("Numerator out of range: " + numerator);
		}
		if (denominator < 3 || denominator > 18) {
			throw new IllegalArgumentException("Denominator out of range: " + denominator);
		}
		
		try {
			if (numerator < 9) {
				sendMessage(0xB0, 0x1E, 0x10 * (numerator - 1) + denominator - 3);
			} else {
				sendMessage(0xB0, 0x1F, 0x10 * (numerator - 9) + denominator - 3);
			}
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	private void sendMessage(int d1, int d2, int d3) throws InvalidMidiDataException {
		output.send(new ShortMessage(d1, d2, d3), -1);
	}
	
	public void reset() { // Built-in reset event
		fillArray((byte) -1);
		
		try {
			sendMessage(0xB0, 0, 0);
		} catch (InvalidMidiDataException e) {
			e.printStackTrace();
		}
	}
	
	public void clear() { // Clear launchpad manually
		for (int row = -1; row < 8; row++) {
			for (int column = 0; column < 9; column++) {
				if (row == -1 && column == 8) {
					continue;
				}
				turnOff(row, column);
			}
		}
	}
	
	public void open() throws MidiUnavailableException {
		if (!inputDevice.isOpen()) {
			inputDevice.open();
		}
		if (!outputDevice.isOpen()) {
			outputDevice.open();
		}
		
		input = inputDevice.getTransmitter();
		output = outputDevice.getReceiver();
		input.setReceiver(this);
		isOpen = true;
		
		reset();
	}
	
	public boolean isOpen() {
		return isOpen;
	}
	
	public void close() {
		isOpen = false;
		reset();
		input.close();
		output.close();
	}
}

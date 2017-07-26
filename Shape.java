import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.awt.image.BufferedImage;
import java.awt.Color;

public class Shape {
	private int width;
	private int height;
	private byte[][] buttons;
	
	public Shape(Shape shape) {
		width = shape.getWidth();
		height = shape.getHeight();
		
		byte[][] array = new byte[height][];
		
		int counter = 0;
		for (byte[] row : shape.getButtonArray()) {
			array[counter++] = row.clone();
		}
	}
	
	public Shape(byte[][] array) {
		height = array.length;
		width = array[0].length;
		buttons = array;
	}
	
	public Shape(int width, int height) {
		this.width = width;
		this.height = height;
		buttons = new byte[height][width];
		
		for (byte[] row : buttons) {
			Arrays.fill(row, (byte) -1);
		}
	}
	
	public Shape(BufferedImage image) {
		this(image.getWidth(), image.getHeight());
		width = image.getWidth();
		height = image.getHeight();
		
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				Color color = new Color(image.getRGB(column, row));
				int red = color.getRed() / 64;
				int green = color.getGreen() / 64;
				
				if (red == 0 && green == 0) {
					buttons[row][column] = -1;
				} else {
					buttons[row][column] = LaunchpadInterface.color(red, green);
				}
			}
		}
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		for (int r = 0; r < height; r++) {
			for (int c = 0; c < width; c++) {
				if (getButton(r, c) == -1) {
					builder.append(' ');
				} else {
					builder.append('*');
				}
			}
			builder.append('\n');
		}
		
		return builder.toString();
	}
	
	public byte[][] getButtonArray() { // Returns 2D array of colors (working like array[row][column] = color)
		return buttons;
	}
	
	public void setButton(int row, int column, byte color) {
		if (!positionInBounds(row, column)) {
			throw new IllegalArgumentException("Row or column out of bounds: Row " + row + ", Column " + column);
		}
		
		buttons[row][column] = color;
	}
	
	public byte getButton(int row, int column) {
		if (!positionInBounds(row, column)) {
			throw new IllegalArgumentException("Row or column out of bounds: Row " + row + ", Column " + column);
		}
		
		return buttons[row][column];
	}
	
	public int getWidth() {
		return width;
	}
	
	public int getHeight() {
		return height;
	}
	
	public boolean positionInBounds(int row, int column) {
		if (row < 0
				|| row >= height
				|| column < 0
				|| column >= width) {
			return false;
		}
		
		return true;
	}
	
	// Replaces the button data by the data from the given shape
	// Doesn't copy the button array!!!
	public void replaceBy(Shape shape) {
		width = shape.getWidth();
		height = shape.getHeight();
		buttons = shape.getButtonArray();
	}
	
	public void fill(byte color) {
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				if (getButton(row, column) != -1) {
					setButton(row, column, color);
				}
			}
		}
	}
	
	public void fillArea(byte color) {
		for (int row = 0; row < height; row++) {
			for (int column = 0; column < width; column++) {
				setButton(row, column, color);
			}
		}
	}
	
	public static Shape hollowRectangle(int width, int height, byte color) {
		Shape shape = new Shape(width, height);
		
		for (int i = 0; i < width; i++) {
			shape.setButton(0, i, color);
			shape.setButton(height - 1, i, color);
		}
		for (int i = 0; i < height; i++) {
			shape.setButton(i, 0, color);
			shape.setButton(i, width - 1, color);
		}
		
		return shape;
	}
}

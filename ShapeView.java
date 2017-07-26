import java.util.Iterator;
import java.util.NoSuchElementException;

public class ShapeView implements Iterable<Button> {
	Shape shape;
	
	private int rowPos = 0, columnPos = 0;
	private int width, height;
	private int direction = 0; // 0 = 0°, 1 = 90°, 2 = 180°...
	private byte color = -1; // The fill color, -1 means no fill
	
	public ShapeView(Shape shape) {
		this.shape = shape;
		recalculate();
	}
	
	public ShapeView() {
		this.shape = null;
	}
	
	private void recalculate() {
		width = shape.getWidth();
		height = shape.getHeight();
	}
	
	public Iterator<Button> iterator() {
		return new Iterator<Button>() {
			boolean endReached = false;
			int row = 0;
			int column = 0;
			
			public boolean hasNext() {
				if (row >= getHeight()) {
					return false;
				} else {
					return true;
				}
			}
			
			public Button next() {
				if (!hasNext()) {
					throw new IllegalStateException();
				}
				
				Button button = new Button(row + rowPos, column + columnPos, getButton(row, column));
				nextElement();
				
				return button;
			}
			
			private void nextElement() { // Proceeds with the position
				column++;
				if (column >= getWidth()) {
					column = 0;
					row++;
				}
			}
		};
	}
	
	public void setShape(Shape shape) {
		this.shape = shape;
		recalculate();
	}
	
	public Shape getShape() {
		return shape;
	}
	
	public byte[][] getButtonArray() {
		return shape.getButtonArray(); // TODO
	}
	
	public byte getButton(int row, int column) {
		if (!positionInBounds(row, column)) {
			throw new IllegalArgumentException("Row or column out of bounds: Row " + row + ", Column " + column);
		}
		
		if (direction == 2 || direction == 1) {
			row = getHeight() - 1 - row;
			column = getWidth() - 1 - column;
		}
		if (direction % 2 == 1) {
			int column2 = column;
			column = getHeight() - 1 - row;
			row = column2;
		}
		
		byte button = shape.getButton(row, column);
		
		if (color == -1) {
			return button;
		} else {
			return button == -1 ? -1 : color;
		}
	}
	
	public int getRowPosition() {
		return rowPos;
	}
	
	public int getColumnPosition() {
		return columnPos;
	}
	
	public void changeRowPosition(int rowChange) {
		rowPos += rowChange;
	}
	
	public void changeColumnPosition(int columnChange) {
		columnPos += columnChange;
	}
	
	public void setRowPosition(int rowPos) {
		this.rowPos = rowPos;
	}
	
	public void setColumnPosition(int columnPos) {
		this.columnPos = columnPos;
	}
	
	public void setPosition(int rowPos, int columnPos) {
		this.rowPos = rowPos;
		this.columnPos = columnPos;
	}
	
	public int getWidth() {
		return (direction % 2 == 0 ? width : height);
	}
	
	public int getHeight() {
		return (direction % 2 == 0 ? height : width);
	}
	
	public void setColor(byte color) {
		this.color = color;
	}
	
	public byte getColor() {
		return color;
	}
	
	public void setDirection(int direction) {
		this.direction = (direction %= 4) < 0 ? 4 - direction : direction;
	}
	
	public int getDirection() {
		return direction;
	}
	
	public boolean positionInBounds(int row, int column) {
		if (row < 0
				|| row >= getHeight()
				|| column < 0
				|| column >= getWidth()) {
			return false;
		}
		
		return true;
	}
}

public class Button {
	public int row;
	public int column;
	public byte color;
	
	public Button(int row, int column, byte color) {
		this.row = row;
		this.column = column;
		this.color = color;
	}
	
	public Button(int row, int column) {
		this(row, column, (byte) 0);
	}
	
	public boolean equals(Object o) {
		if (o.getClass() != this.getClass()
				|| this.hashCode() != o.hashCode()) {
			return false;
		} else {
			return true;
		}
	}
	
	public boolean positionEquals(Button o) {
		if (o.row == row && o.column == column) {
			return true;
		}
		
		return false;
	}
	
	public int hashCode() {
		return row + 8 * column + 8 * 8 * color;
	}
	
	public static Button[] makeButtonArrayWithColor(int... n) {
		if (n.length >= 3 && n.length % 3 == 0) {
			Button[] array = new Button[n.length / 3];
			for (int i = 0; i < array.length; i++) {
				array[i] = new Button(n[i * 3], n[i * 3 + 1], (byte) n[i * 3 + 2]);
			}
			return array;
		} else {
			throw new IllegalArgumentException("Illegal argument amount " + n.length);
		}
	}
	
	public static Button[] makeButtonArrayWithoutColor(int... n) {
		if (n.length >= 2 && n.length % 2 == 0) {
			Button[] array = new Button[n.length / 2];
			for (int i = 0; i < array.length; i++) {
				array[i] = new Button(n[i * 2], n[i * 2 + 1]);
			}
			return array;
		} else {
			throw new IllegalArgumentException("Illegal argument amount " + n.length);
		}
	}
}

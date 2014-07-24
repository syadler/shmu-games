import java.awt.Color;

public class LeftL extends Shape {

	Color color = new Color(160, 160, 0);
	int[][][] configurations;
	
	public LeftL(int columns){
		super(columns);
		configurations = new int[][][]{{{0, 0}, {0, 1}, {1, 2}, {0, 2}}, {{0, 0}, {1, 0}, {2, 0}, {0, 1}}, {{0, 0}, {1, 0}, {1, 1}, {1, 2}}, {{0, 1}, {2, 0}, {1, 1}, {2, 1}}};
		this.x = columns/2 - getWidth()/2;
	}
	
	public Color getColor() {
		return color;
	}

	public int[][] getFigure() {
		return configurations[counter % configurations.length];
	}
	
	public int[][][] getConfigurations() {
		return configurations;
	}
	
	public void rotate() {
		counter++;
	}
}

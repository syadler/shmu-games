import java.awt.Color;

public abstract class Shape {
	int x, y, prevX, prevY, counter;
	
	public Shape() {}
	
	public Shape(int columns){
		this.y = 1;
	}
	
	public int getHeight() {
		int farthest = 0;
		for (int[] square : getFigure())
			if (square[1] > farthest)
				farthest = square[1];
		return farthest + 1;
	}
	
	public int getWidth(){
		int farthest = 0;
		for (int[] square : getFigure())
			if (square[0] > farthest)
				farthest = square[0];
		return farthest + 1;
	}
	
	public int getNumberOfConfigurations() {
		return getConfigurations().length;
	}
	
	public abstract Color getColor();
	
	public abstract int[][][] getConfigurations();
	
	public abstract int[][] getFigure();
	
	public abstract void rotate();
	
}



public class ShapeFactory {
	
	static Shape rightL;
	static Shape plus;
	static Shape line;
	static Shape square;
	static Shape leftL;
	static Shape leftTwist;
	static Shape rightTwist;
	
	
	public ShapeFactory(int columns) {
		rightL = new RightL(columns);
		plus = new Plus(columns); 
		line = new Line(columns);
		square = new Square(columns);
		leftL = new LeftL(columns);
		leftTwist = new LeftTwist(columns);
		rightTwist = new RightTwist(columns);
	}
}

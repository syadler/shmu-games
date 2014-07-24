/*
Tetris
Todo: count lines, resizing, lose when hits top, blink b4 clear

Todo now: 
*/

import javax.swing.JFrame;
import java.awt.Graphics;
import java.awt.Color;

import java.util.Random;
import static java.lang.System.out;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import static java.awt.event.KeyEvent.*;


public class TetrisFrame extends JFrame {

	int w;
	int h;
	int side;
	final int topBanner = 31;
	final int border = 8;
	
	final int ROWS = 22;
	final int COLUMNS = 12;
	
	SquareInfo[][] squares;
	Shape piece;
	Graphics graphics;
	
	boolean paused = false;
	boolean lose = false;
	boolean clearing = false;
	
	
	public TetrisFrame(int width){
		w = width + 2*border; 
		h = width*2 + border + topBanner;
		setSize(w, h);
		side = (w-2*border)/(COLUMNS-2);
		squares = new SquareInfo[ROWS][COLUMNS];
		for (int i = 0; i < ROWS; i++){
			for (int j = 0; j < COLUMNS; j++){
				//set borders
				if (i == 0 || j == 0 || i == ROWS-1 || j == COLUMNS-1)
					squares[i][j] = new SquareInfo(true, null);
				else
					squares[i][j] = new SquareInfo(false, null);
			}
		}
		//printSquares();
		new ShapeFactory(COLUMNS);
		piece = randomPieceSelector();
	}
	public static void main(String[] args){
		TetrisFrame f = new TetrisFrame(300);
		f.setVisible(true);
		f.setDefaultCloseOperation(EXIT_ON_CLOSE);
		ArrowListener al = f.new ArrowListener();
		f.addKeyListener(al);
		
		f.go();
	}
	
	public void go() {
		graphics = getGraphics();
		while (!lose){
			piece = randomPieceSelector();
			pieceFall();
			clearing = false;
		}
	}
	
	public void pieceFall() {
		redraw();
		boolean keepFalling = true;
		while(keepFalling){
			try { Thread.sleep(300); }
			catch (InterruptedException e) {}
			while (paused) {
				try { Thread.sleep(5); }
				catch (InterruptedException e) {}
			};
			if (isLegalMove(piece.x, piece.y + 1)){
				piece.y++;
				redraw();				
			}
			else
				keepFalling = false;
		}
		storePiecePermanent();
		clearLines();		
		//printSquares();
		resetPiece();
	}
	
	private synchronized void redraw() {
		graphics.setColor(piece.getColor());
		clearPiece(piece.prevX, piece.prevY);
		drawPiece(piece.x, piece.y);
	}
	
	private void clearLines() {
		clearing = true;
		int lines = piece.getHeight();
		int numCompleted = 0;
		int currentLine = 0;
		int bottom = piece.y + lines - 1;
		int shiftLine = bottom;
		int[] blinkingLines = new int[4];
		SquareInfo[][] blinkingData = new SquareInfo[4][COLUMNS];
		for (int i = lines-1; i >= 0; i--) {
			currentLine = piece.y + i;
			if (isFull(currentLine)){
				blinkingLines[numCompleted] = currentLine;
				for (int j = 1; j < COLUMNS-1; j++)
					blinkingData[numCompleted][j] = new SquareInfo(squares[currentLine][j].filled, squares[currentLine][j].color);
				numCompleted++;
			}
			else if (numCompleted > 0)
				for (int j = 0; j<numCompleted; j++){
					shift(shiftLine, numCompleted);
					shiftLine--;
				}
			else {
				shiftLine--;
				bottom = shiftLine;
			}
		}
		if (numCompleted > 0){
			shiftRest(shiftLine, numCompleted);
			blink(blinkingLines, blinkingData, numCompleted);
			redrawLinesBottomUp(bottom, numCompleted);
		}
	}
	
	private void shift(int line, int shiftNum){
		//out.println("Now setting line " + line + " to be what previously was line " + (line - shiftNum));
		for (int i = 1; i < COLUMNS-1; i++){
			squares[line][i].filled = squares[line-shiftNum][i].filled;
			squares[line][i].color = squares[line-shiftNum][i].color;
		}
	}
	
	private void shiftRest(int line, int shiftNum){
		//out.println("Now setting all lines starting with " + line + " to be what used to be " + shiftNum + " lines above them.");
		while (! isEmpty(line)){
			shift(line, shiftNum);
			line--;
		}
	}
	
	private void blink(int[] lines, SquareInfo[][] data, int numLines) {
		for (int i = 1; i < numLines; i++){
			eraseLines(lines);
			try { Thread.sleep(200); }
			catch (InterruptedException e) {}
			drawLines(lines, data);
			try { Thread.sleep(200); }
			catch (InterruptedException e) {}
		}
	}
	
	private void eraseLines(int[] lines){
		for (int line : lines)
			if (line > 0)
				eraseLine(line);
	}
	
	private void eraseLine(int line) {
		for (int i = 1; i < COLUMNS-1; i++)
			graphics.clearRect(convertX(i-1), convertY(line-1), side, side);
	}
	
	private void drawLines(int[] lines, SquareInfo[][] data) {
		for (int i = 0; i < lines.length; i++)
			if (lines[i] > 0)
				drawLine(lines[i], data[i]);
	}
	
	private void drawLine(int line, SquareInfo[] data){
		for (int i = 1; i < COLUMNS-1; i++)
			if (data[i].filled){
				//out.println("drawing x = " + i + " y = " + line + " as " + squares[i][line].color);
				graphics.setColor(data[i].color);
				graphics.fill3DRect(convertX(i-1), convertY(line-1), side, side, true);
			}
	}
	
	private void redrawLinesBottomUp(int bottom, int shiftNum){
		int line = bottom; 
		while (! isEmpty(line)){
			eraseLine(line);
			drawLine(line, squares[line]);
			line--;
		}
		//erase upper lines
		for (int i = 0; i <= shiftNum && line-i > 0; i++)
			eraseLine(line-i);
	}
	
	private void drawPiece(int x, int y){
		for (int[] square : piece.getFigure())
			graphics.fill3DRect(convertX(x-1) + square[0]*side, convertY(y-1) + square[1]*side, side, side, true);
		piece.prevX = x;
		piece.prevY = y;
		//out.println("printing " + piece.getColor() + " at " + piece.y);
	}
	
	private void clearPiece(int x, int y){
		for (int[] square : piece.getFigure())
			graphics.clearRect(convertX(x-1) + square[0]*side, convertY(y-1) + square[1]*side, side, side);
	}
	
	private int convertX(int x){
		return border + x*side;
	}
	
	private int convertY(int y){
		return topBanner + y*side;
	}
	
	private Shape randomPieceSelector() {
		Random generator = new Random();
		int number = Math.abs(generator.nextInt() % 7);
		switch (number){
			case 0: return ShapeFactory.rightL;
			case 1: return ShapeFactory.plus;
			case 2: return ShapeFactory.line;
			case 3: return ShapeFactory.leftL;
			case 4: return ShapeFactory.square;
			case 5: return ShapeFactory.leftTwist;
			default: return ShapeFactory.rightTwist;
		}	
	}
	
	private void storePiecePermanent() {
		for (int[] square : piece.getFigure()){
			squares[piece.y + square[1]][piece.x + square[0]].filled = true;
			squares[piece.y + square[1]][piece.x + square[0]].color = piece.getColor();
		}
	}

	private void printSquares() {
		String output = "";
		for (int i = 0; i < ROWS; i++){
			for (int j = 0; j < COLUMNS; j++)
				output += squares[i][j].filled ? "X" : "o";
			output += '\n';
		}
		System.out.println(output);
	}
	
	private boolean isLegalMove(int newX, int newY) {
		//check borders and check overlap with other pieces
		for (int[] square : piece.getFigure())
			if (squares[newY + square[1]][newX + square[0]].filled)
				return false;
		return true;
	}
	
	private boolean isLegalRotate() {
		for (int[] square : piece.getConfigurations()[(piece.counter + 1) % piece.getNumberOfConfigurations()])
			if (squares[piece.y + square[1]][piece.x + square[0]].filled)
				return false;
		return true;
	}
		
	
	private boolean isFull(int line){
		for (int i = 1; i < COLUMNS-1; i++)
			if (! squares[line][i].filled)
				return false;
		return true;
	}
	
	private boolean isEmpty(int line){
		for (int i = 1; i < COLUMNS-1; i++)
			if (squares[line][i].filled)
				return false;
		return true;
	}
	
	private void resetPiece() {
		piece.x = COLUMNS/2 - piece.getWidth()/2;
		piece.y = 1;
		piece.prevX = 0;
		piece.prevY = 0;
		piece.counter = 0;
	}
	
	public int getColumns() {
		return COLUMNS;
	}
	
	class ArrowListener extends KeyAdapter {
		//runs as Thread 14 on my machine
		public void keyPressed(KeyEvent k) {
			switch (k.getKeyCode()){
				case VK_UP: if (isLegalMove(piece.x, piece.y - 1)) piece.y--; break;
				case VK_DOWN: if (isLegalMove(piece.x, piece.y + 1)) piece.y++; break;
				case VK_LEFT: if (isLegalMove(piece.x - 1, piece.y)) piece.x--; break;
				case VK_RIGHT: if (isLegalMove(piece.x + 1, piece.y)) piece.x++; break;
				case VK_SPACE: 
					if (isLegalRotate()) {
						clearPiece(piece.prevX, piece.prevY);
						piece.rotate();
					}
					break;
				case VK_P:
						if (paused)
							paused = false;
						else
							paused = true;
					break;
			}
			if (! clearing)
				redraw();
		}
	}
	
}
















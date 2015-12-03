
import connectK.CKPlayer;
import connectK.BoardModel;

import java.awt.Point;
import java.util.ArrayList;


public class NewAI extends CKPlayer {
	
	//Board information
	private int boardHeight;
	private int boardWidth;
	private int kLength;
	private static long maxScore =  Long.MAX_VALUE;
	private static long minScore =  Long.MIN_VALUE;
	private int evaluations = 0;
	private Point lastMove;
	private boolean gravityOn;
	
	public NewAI(byte player, BoardModel state) {
		//Constructor 
		super(player, state);
		boardHeight = state.getHeight();
		boardWidth = state.getWidth();
		kLength = state.getkLength();
		gravityOn = state.gravity;
		teamName = "SoloKasean";
	}
	
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}

	@Override
	public Point getMove(BoardModel state) {
		//the first move has not been made
		lastMove = state.getLastMove();
		if(lastMove == null){
			//place peace in the middle of board 
			lastMove = new Point(boardWidth/2,0);
			if(!gravityOn)
				lastMove = new Point((boardWidth/2),(boardHeight/2));
			
			return lastMove;
		}
		if(gravityOn){
			return AlphaBetaSearch(new Node(state, 7));
		}
		
		return AlphaBetaSearch(new Node(state, 4));
	}
	
	private Point AlphaBetaSearch(Node state){
		long value = maxValue(state, minScore, maxScore);
		for(Node child : state.getChildren()){
			if(value == child.value){
				return child.getBoard().getLastMove();
			}
		}
		return null;
	}
	
	private long maxValue(Node state, long alpha, long beta) {
		Byte winner = state.getBoard().winner();
		//evaluate if at the lowest point in the tree or there is a winner 
		if(state.getLevel() == 0 || winner != -1) return utility(state);
		//start with the value at minimum 
		state.setValue(minScore);
		for(Point move : getPossibleMoves(state.getBoard())){
			state.setValue(Math.max(state.value, minValue(Result(state, move), alpha, beta)));
			if(state.value >= beta){
				return state.value;
			}
			alpha = Math.max(alpha, state.value);
		}
		return state.value;
	}
	
	private long minValue (Node state, long alpha, long beta) {
		//base case return
		Byte winner = state.getBoard().winner();
		//evaluate if at the lowest point in the tree or there is a winner 
		if(state.getLevel() == 0 || winner != -1) return utility(state);
		//set value to max score 
		state.setValue(maxScore);
		for(Point action : getPossibleMoves(state.getBoard())){
			state.setValue(Math.min(state.value, maxValue(Result(state, action), alpha, beta)));
			if(state.value <= alpha) return state.value;
			beta = Math.min( beta, state.value);
		}
		return state.value;
	}

	private Node Result(Node state, Point move) {
		//get the current Player
		Byte currentPlayer = Player(state.getBoard());
		//get the new board model after the move 
		BoardModel clonedState = state.getBoard().clone().placePiece(move, currentPlayer);
		//get new depth 
		int depthLevel = state.getLevel() -1;
		Node childNode = new Node(clonedState, depthLevel);
		state.addChild(childNode);
		return childNode;
	}
	
	private long utility(Node state) {
		evaluations++;
		long total = evaluateHorizontaly(state.getBoard(), player);
		total += evaluateVertically(state.getBoard(), player);
		total += evaluateDL(state.getBoard(), player);
		total += evaluateDR(state.getBoard(), player);
		state.setValue(total);
		return total;
		
	}
	
	//evaluates the board Horizontally
	private long evaluateHorizontaly(BoardModel state, byte player){
		//total score that we are going to add too and return
		int totalScore = 0;
		//loop though all of the horizontal levels
		for(int y = 0; y < boardHeight; y++){
			for(int x = 0; x <= boardWidth - kLength; x++){
			//loop in the y direction till the limit
	
				//count the number of 1, and 2s in the block 
				int numberOfOnes = 0;
				int numberOfTwos = 0;
				for(int blockNumber = 0; blockNumber < kLength; blockNumber++){
					byte tile = state.getSpace(blockNumber+x,y);
					if(tile == 1)
						numberOfOnes++;
					if(tile == 2)
						numberOfTwos++;
				}
				totalScore += evaluateBlock(numberOfOnes, numberOfTwos);
			}
		}
		return totalScore;
	}
	//evaluate 
	
	public long evaluateVertically(BoardModel state, byte player){
		//total score that we are going to add too and return
		int totalScore = 0;
		//loop though all of the vertical levels
		for(int x = 0; x < boardWidth ; x++){
			//loop in the y direction till the limit
			for(int y = 0; y <= boardHeight - kLength; y++){
				//count the number of 1, and 2s in the block of 
				int numberOfOnes = 0;
				int numberOfTwos = 0;				
				//loop though a block the size of k 
				for(int blockNumber = 0; blockNumber < kLength ; blockNumber++){
					byte tile = state.getSpace(x,y+blockNumber);
					if(tile == 1)
						numberOfOnes++;
					if(tile == 2)
						numberOfTwos++;
				}
				totalScore += evaluateBlock(numberOfOnes, numberOfTwos);
			}
		}
		return totalScore;
	}
	//evaluates a block 
	
	private long evaluateDR(BoardModel state, byte player){
		//track to total 
		long total = 0;
		//iterate from one side to right side - klength 
		for(int x = 0; x <= boardWidth-kLength ; x++){
			for(int y = 0; y <= boardHeight - kLength; y++){
				//track our current poistion and the number of 1's and 2's in a block 
				int currentx = x;
				int currenty = y;
				int numberOfOnes = 0;
				int numberOfTwos = 0;
				//loop though a block the size of k 
				for(int blockNumber = 0; blockNumber < kLength ; blockNumber++){
					byte tile = state.getSpace(currentx+blockNumber, currenty+blockNumber);
					if(tile == 1)
						numberOfOnes++;
					if(tile == 2)
						numberOfTwos++;
				}
				total += evaluateBlock(numberOfOnes, numberOfTwos);
			}
		}
		return total;
	}
	
	private long evaluateDL(BoardModel state, byte player){
		//track the total
		int totalScore = 0;
		//interate from klength to then end of board 
		for(int x = kLength-1 ; x < boardWidth; x++){
			for(int y = 0; y <= boardHeight - kLength ; y++){
				int currentx = x;
				int currenty = y;
				int numberOfOnes = 0;
				int numberOfTwos = 0;	
				for(int blockNumber = 0; blockNumber < kLength; blockNumber++){
					//get the tile at the block
					byte tile = state.getSpace(currentx-blockNumber, currenty + blockNumber);
					if(tile == 1)
						numberOfOnes++;
					if(tile == 2)
						numberOfTwos++;
				}
				//add score to total score 
				totalScore += evaluateBlock(numberOfOnes, numberOfTwos);
			}
		}
		return totalScore;
	}
	
	private long evaluateBlock(int numberOfOnes, int numberOfTwos){
		//total score
		long totalScore = 0;
		//only evaluate if the block is empty 
		if(numberOfOnes == 0 && numberOfTwos !=  0 || numberOfOnes != 0 && numberOfTwos == 0){
			int count = Math.abs(numberOfOnes - numberOfTwos);
			//if there is a win, return max total score
			if(count == kLength){
				if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
					return maxScore;
					
				else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
					return minScore;
			}	
			int score = (int)Math.pow(10, count);	
			if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
				totalScore += score;
			else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
				totalScore -= score;
		}
		return totalScore;
	}
	//Get all possible moves 
	
	
	private ArrayList<Point> getPossibleMoves(BoardModel state){
		ArrayList<Integer> searchIndex = getMoveOrder();
		//get the middle 
		ArrayList<Point> possibleMoves = new ArrayList<Point>();
		if(state.hasMovesLeft()){
			if(state.gravityEnabled()){
				//find all of the highest spaces that are empty
				int x;
				for(int i = 0; i < state.width ; i++){
					x = searchIndex.get(i);
					for(int y = 0; y < state.height ; y++){
						if(state.getSpace(x,y)  != 1 && state.getSpace(x,y)  != 2){
							 possibleMoves.add(new Point(x,y));
							 break;
						}
					}
				}
			}
			else
			{
				//find all of the highest spaces that are empty
				int x;
				for(int i = 0; i < state.width ; i++){
					x = searchIndex.get(i);
					for(int y = 0; y < state.height ; y++){
						if(state.getSpace(x,y)  != 1 && state.getSpace(x,y)  != 2){
							
							 possibleMoves.add(new Point(x,y));
						}
					}
				}
			}
		}
		return possibleMoves;
	}
	//returns a list with the move ordering
	
	
	private ArrayList<Integer> getMoveOrder(){
		ArrayList<Integer> moveOrder = new ArrayList<Integer>();	
		//if even 
		if(boardWidth%2 == 0){
			int start = (boardWidth+1)/2;
			moveOrder.add(start);
			int next = -1;
			while(start != 0){
				start+=next;
				moveOrder.add(start);
				next = staggerIncrement(next);
			}
		}else{ 
			int start = (boardWidth)/2;
			moveOrder.add(start);
			int next = -1;
			while(start != boardWidth-1){
				start+=next;
				moveOrder.add(start);
				next = staggerIncrement(next);
			}
		}
		
	
		return moveOrder;

	}
	
	
	
	private int staggerIncrement(int i){
		int newI = Math.abs(i) + 1;
		if(newI % 2 == 0)
			return newI;
		return newI * -1;
	}
	
    
	
	private Byte Player(BoardModel clonedState) {
		Byte nextPlayer;
		Byte lastPlayer = clonedState.getSpace(clonedState.getLastMove());
		//if last player is 1 then currentPlate is 2 
		if(lastPlayer == 1)
			nextPlayer = 2;
		else if(lastPlayer == 2)
			nextPlayer = 1;
		else
			nextPlayer = 1;
		return nextPlayer;
	}

	
	
	private class Node{
		//represents a node in the game
		
		private  ArrayList<Node> children;
		private BoardModel currentState;
		private long value;
		private int level;
		
		public void setValue(long val){
			value = val;
		}
		
		public long getValue(){
			return value;
		}
		
		public Node(BoardModel state, int levelNumber){
			//constructor tells what level we are on 
			currentState = state;
			level = levelNumber;
			children = new ArrayList<Node>();
		}
			
		public BoardModel getBoard() {
			//returns the current board 
			return currentState;
		}
			
		public int getLevel(){
			//returns the currentLecel
			return level;
		}
		
		public void setLevel(int levelNumber){
			//set current Level
			level = levelNumber;
		}


		public ArrayList<Node> getChildren() {
			//get all of the children 
			return children;
		}

		public int getNumberOfChildren() {
			//get the number of children 
			return children.size();
		}
	
		public void addChild(Node childNode) {
			//add child to the list of children 
			children.add(childNode);
		}
		
	}

}

















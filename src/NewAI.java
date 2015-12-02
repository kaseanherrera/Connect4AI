
import connectK.CKPlayer;
import connectK.BoardModel;

import java.awt.Point;
import java.util.ArrayList;




public class NewAI extends CKPlayer {
	
	//Board information
	private int boardHeight;
	private int boardWidth;
	private int kLength;
	private static long minScore = -1000000000;
	private static long maxScore = 1000000000;
	
	public NewAI(byte player, BoardModel state) {
		//Constructor 
		super(player, state);
		boardHeight = state.getHeight();
		boardWidth = state.getHeight();
		kLength = state.getkLength();
		teamName = "Solo";
	}
	
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}

	@Override
	public Point getMove(BoardModel state) {
		return AlphaBetaSearch(new Node(state, 1));
	}
	
		
	private Point AlphaBetaSearch(Node state){
		
		long value = maxValue(state, minScore, maxScore);
		System.out.println("Value = " + value);
		
		for(Node child : state.getChildren()){
			System.out.println("Child value " + child.level);
		}
		return null;
		
	}
	
	private long maxValue(Node state, long alpha, long beta) {
		//System.out.println("Inside Max");
		//base case return

		Byte winner = state.getBoard().winner();
		//evaluate if at the lowest point in the tree or there is a winner 
		if(state.getLevel() == 0 || winner != -1) return utility(state);
		//start with the value at minimum 
		state.value = minScore;
		for(Point move : getPossibleMoves(state.getBoard())){
			state.value = Math.max(state.value, minValue(Result(state, move), alpha, beta));
			if(state.value >= beta){
				return state.value;
			}
			alpha = Math.max(alpha, state.value);
		}
		
		//System.out.println("Leaving Max");
		return state.value;
	}
	

	private long minValue (Node state, long alpha, long beta) {
		//base case return
		Byte winner = state.getBoard().winner();
		//evaluate if at the lowest point in the tree or there is a winner 
		if(state.getLevel() == 0 || winner != -1) return utility(state);
		//set value to max score 
		state.value = maxScore;
		for(Point action : getPossibleMoves(state.getBoard())){
			state.value = Math.min(state.value, maxValue(Result(state, action), alpha, beta));
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
		//System.out.println("Inside Utility");
		// TODO Auto-generated method stub
		long total = evaluateHorizontaly(state.getBoard(), player);
		//total += evaluateVertically(state.getBoard(), player);
		//System.out.println("Leaving Utility");
		return total;
		
	}
	
	private long evaluateHorizontaly(BoardModel state, byte player){
		//System.out.println("Inside Eval Horozontally");
		//evaluates the board Horizontally
		//total score that we are going to add too and return
		int totalScore = 0;
		//edge to stop counting and stay inbounds of the board
		int xStop = state.width - state.kLength + 1;
		
		//loop though all of the horizontal levels
		for(int y = 0; y < state.height ; y++){
			//loop in the x direction till the limit
			for(int x = 0; x < xStop; x++){
				//count the number of 1, and 2s in the block of connectn
				int numberOfOnes = 0;
				int numberOfTwos = 0;
				boolean evaluate = true;
				for(int slot = x; slot < (x + state.kLength) ; slot++){
					//dont count empty spaces
					if(state.getSpace(slot, y) == 0)
						continue;
					if(state.getSpace(slot,y) == 1){
						if(numberOfTwos > 1){
							evaluate = false;
							break;
						}
						numberOfOnes++;
					}
					if(state.getSpace(slot,y) == 2){
						if(numberOfOnes > 0){
							evaluate = false;
							break;
						}
						numberOfTwos++;
					}
				}
				
				if(evaluate){
					if(numberOfOnes == 0 && numberOfTwos != 0 || numberOfTwos == 0 && numberOfOnes != 0){
						//if its a n in a row, max points 
						long  score = 0;
						int count = Math.abs(numberOfOnes - numberOfTwos);
						
						//if there is a win, return max total score
						if(count == state.kLength){
							if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
								return maxScore;
							
							else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
								return minScore;
						}
						score = (int)Math.pow(10, count);
						
						if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
							totalScore += score;
						
						else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
							totalScore -= score;
				
					}
				}
			}
		}
		//System.out.println("Leaving Horizonal");
		return totalScore;
	}

	private ArrayList<Point> getPossibleMoves(BoardModel state){
		//System.out.println("getPossibleMoves");
		//Gets all possible moves 
		ArrayList<Point> possibleMoves = new ArrayList<Point>();
		if(state.hasMovesLeft()){
			if(state.gravityEnabled()){
				//find all of the highest spaces that are empty
				for(int x = 0; x < state.width ; x++){
					for(int y = 0; y < state.height ; y++){
						if(state.getSpace(x,y)  != 1 && state.getSpace(x,y)  != 2){
							 possibleMoves.add(new Point(x,y));
							 break;
						}
					}
				}
			}else{
				//find all of the highest spaces that are empty
				for(int x = 0; x < state.width ; x++){
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
	
	private Byte Player(BoardModel clonedState) {
		//System.out.println("Inside Player");
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

	public class Node{
		//represents a node in the game
		
		private  ArrayList<Node> children;
		private BoardModel currentState;
		public long value;
		private int level;
		
		
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

















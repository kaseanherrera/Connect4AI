
import connectK.CKPlayer;
import connectK.BoardModel;
import java.math.*;
import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

//Make this shit work again 
//TODO make laess greedy
//TODO diagonal ecaluation function \
//TODO diognal evaluation function /
//Alpha beta pruning 

public class myAi extends CKPlayer {

	int boardWidth;
	int boardHeight;
	int kLength;
	long minValue = -1000000000;
	long maxValue =  1000000000;

	public myAi(byte player, BoardModel state) {
		super(player, state);
		boardHeight = state.getHeight();
		boardWidth = state.getHeight();
		kLength = state.getkLength();
		teamName = "Solo";
	}

	@Override
	public Point getMove(BoardModel state) {
		
		node headNode  = generateTree(new node(state), player, 3);
		Point move = new Point();
		//start time
		final long startTime = System.nanoTime();
		//move = minMax(headNode, player);
		move = alphaBeta(headNode, player);
		final long duration = System.nanoTime() - startTime;
		System.out.println(duration);
	
		 return move;
	}

	//calls the all of the directional evaluation functions and totals the score
	public int evaluate(BoardModel state, byte player){
		int total = evaluateHorizontaly(state, player);
		total += evaluateVertically(state, player);
		//int total = evaluationTopRBottomL(state,player);
		
		return total;
	}
	
	@Override
	public Point getMove(BoardModel state, int deadline) {
		return getMove(state);
	}	//get all of the possible movies 
	
	
	//Gets all possible moves 
	public ArrayList<Point> getPossibleMoves(BoardModel state){
		ArrayList<Point> possibleMoves = new ArrayList<Point>();
		if(state.hasMovesLeft()){
			if(state.gravityEnabled()){
				//find all of the highest spaces taht are empty
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
	//Evaluates the board vertically 
	public int evaluateVertically(BoardModel state, byte player){
		//total score that we are going to add too and return
		int totalScore = 0;
		//edge to stop counting and stay inbounds of the board
		int yStop = state.height - state.kLength + 1;
		
		//loop though all of the vertical levels
		for(int x = 0; x < state.width ; x++){
			//loop in the y direction till the limit
			for(int y = 0; y < yStop; y++){
				//count the number of 1, and 2s in the block of connectn
				int numberOfOnes = 0;
				int numberOfTwos = 0;
				boolean evaluate = true;
				for(int slot = y; slot < (y + state.kLength) ; slot++){
					//dont count empty spaces
					if(state.getSpace(x, slot) == 0)
						continue;
					if(state.getSpace(x,slot) == 1){
						if(numberOfTwos > 1){
							evaluate = false;
							break;
						}
						numberOfOnes++;
					}
					if(state.getSpace(x,slot) == 2){
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
						
						score = (count == state.kLength) ? 1000000000 : (int)Math.pow(10, count);
						
						if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos){
							totalScore += score;
						}
						
						else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
							totalScore -= score;
						
				
					}
				}
			}
		}
		return totalScore;
	}
	
	//evaluates the board Horizontally
	private int evaluateHorizontaly(BoardModel state, byte player){
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
						
						score = (count == state.kLength) ? 1000000000 : (int)Math.pow(10, count);
						
						if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
							totalScore += score;
						
						else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos)
							totalScore -= score;
				
					}
				}
			}
		}
		return totalScore;
	}
	
	//Evaluate board Diagonally this way ----> \
	public int evaluationTopRBottomL(BoardModel state, byte player){
		//total score 
		int totalScore = 0;
		ArrayList<Point> block = new ArrayList<Point>();
		
		//we want to go from the length to the end
		for(int x = kLength-1; x < boardWidth; x++){
			//go up and left until there x is grater that klenth and y + klength is less than height 
			int currentx = x;
			int currenty = 0;
			while(currentx > kLength && (currenty + kLength < boardHeight)){
				//create a block and evaluate it 
				for(int blockNumber = 0 ; blockNumber < kLength ; blockNumber++){
					Point point  = new Point(currentx - blockNumber, currenty + blockNumber);
					block.add(blockNumber, point );
				}
				
				totalScore = evaluateBlock(block, state, player);
				currentx--;
				currenty++;
			}	
		}
		
		return totalScore;
	}
	
	//function evaluate Block
	public int evaluateBlock(ArrayList<Point> block, BoardModel state, byte player){
		int score = 0;
		int numberOfOnes = 0;
		int numberOfTwos = 0;
		boolean evaluate = true;
		
		for(int pointIndex = 0 ; pointIndex < block.size() ; pointIndex++){
			int x = block.get(pointIndex).x;
			int y = block.get(pointIndex).y;
			
			if(state.getSpace(x, y) == 0)
				continue;
				
			if(state.getSpace(x, y) == 1){
				if(numberOfTwos > 1){
					evaluate = false;
					break;
				}
				numberOfOnes++;
			}
				
			if(state.getSpace(x, y) == 2){
				if(numberOfOnes > 0){
					evaluate = false;
					break;
				}
				numberOfTwos++;
			}
		}
		
		if(evaluate)
			score = evaluate(numberOfOnes, numberOfTwos, player);
	
		return score;
		
	}
	
	//function to evaluate a block statistic
	public int evaluate(int numberOfOnes, int numberOfTwos, byte player){
		int totalScore = 0;
		if(numberOfOnes == 0 && numberOfTwos != 0 || numberOfTwos == 0 && numberOfOnes != 0){
			//if its a n in a row, max points 
			long  score = 0;
			int count = Math.abs(numberOfOnes - numberOfTwos);
			
			score = (count == kLength) ? 1000000000 : (int)Math.pow(10, count);
			
			if(player == 1 && numberOfOnes > numberOfTwos || player == 2 && numberOfOnes < numberOfTwos)
				totalScore += score;
			
			else if(player == 1 && numberOfOnes < numberOfTwos || player == 2 && numberOfOnes > numberOfTwos){
				totalScore -= score;
				System.out.println(score * -1);
			}
		}
		return totalScore;
	}
	
	//min max algorithim
	public Point minMax(node headNode, byte player){
		Point move = new Point();
		long max = -1000000000;
		for(node child :  headNode.getChildren()){
			long minVal = minValue(child, player);
			if(minVal >  max){
				max = minVal;
				move = child.getBoard().getLastMove();
			}
		}
				
		return move;
		
	}
	
	//min max algorithim with alphabeta pruning
	public Point alphaBeta(node headNode, byte player){
		Point move = new Point();
		long value = maxValue(headNode, maxValue, minValue, player);
		
		for(node child :  headNode.getChildren()){
			if(child.score == value){
				move = child.getBoard().getLastMove();
			}
		}
				
		return move;
		
	}
	
	//helper function for the for alpha beta pruning 
	private long maxValue(node move, long alpha, long beta, byte player) {
			if(move.getNumberOfChildren() == 0){
				return evaluate(move.getBoard(), player);
			}
			
			long value = minValue;
			for(node nextMove: move.getChildren()){
				value = Math.max(value, minValue(nextMove,alpha, beta, player));
				if(value >= beta)
					return value;
				alpha = Math.max(alpha, value);
			}
			return value;
		}
		
	//helper min function for alpha beta pruning 
	private long minValue(node headNode, long alpha, long beta,  byte player){
			//if we are at the bottom, return the evaluation of the board
			if(headNode.getNumberOfChildren() == 0)
				return evaluate(headNode.getBoard(), player);
			
			long value = maxValue;
			//go through all of the nodes and run max
			for(node move : headNode.getChildren()){
				value = Math.min(value, maxValue(move,alpha, beta, player));
				if(value <= alpha)
					return value;
				beta = Math.min(beta, value);
			}
			return value;
		}
		
	//helper function for the min max
	private long minValue(node headNode, byte player){
		//if we are at the bottom, return the evaluation of the board
		if(headNode.getNumberOfChildren() == 0)
			return evaluate(headNode.getBoard(), player);
		
		long value = 1000000000;
		
		//go through all of the nodes and run max
		for(node move : headNode.getChildren())
			value = Math.min(value, maxValue(move, player));
		
		return value;
	}
	
	//helper function for the min max 
	private long maxValue(node move, byte player) {
		if(move.getNumberOfChildren() == 0){
			return evaluate(move.getBoard(), player);
		}
		
		long value = -1000000000;
		for(node nextMove: move.getChildren())
			value = Math.max(value, minValue(nextMove, player));
		return value;
	}
	
	//the node class 
	public class node{

		private  ArrayList<node> children;
		private BoardModel currentState;
		public int score;
		
		public node(BoardModel state){
			currentState = state;
			children = new ArrayList<node>();
		
		}

		public ArrayList<node> getChildren() {

			return children;
		}

		public int getNumberOfChildren() {
			return children.size();
		}

		public BoardModel getBoard() {
			return currentState;
		}

		public void addChild(node childNode) {
		
			children.add(childNode);
		}
		
	
		
	}
	
	//generates the game tree with just minmax algorithm 
	public node generateTree(node head, byte player, int level){
		//base case return 
		if(level == 0){
			//head.score = evaluateHorizontaly(head.getBoard(), player);
			return head;
		}
		//get the next movie
		byte nextPlayer = (byte) ((player == 1) ? 2 : 1);	
		//get all of the possible moves
		ArrayList<Point> possibleMoves = getPossibleMoves(head.getBoard());
		//place a peice in all possible movies 
		for(int i = 0; i < possibleMoves.size() ; i++){
			//clone the board
			BoardModel clonedState = head.getBoard().clone().placePiece(possibleMoves.get(i), player);
			//create a node
			node childNode = new node(clonedState);
			childNode.score = evaluate(childNode.getBoard(), player);
			//add to children
			head.addChild(childNode);
			
			generateTree(childNode, nextPlayer, level-1);
		}
	
		return head;
	}
}

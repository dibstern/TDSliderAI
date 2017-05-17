package aiproj.slider;

import java.lang.Character;
import java.util.ArrayList;
import java.util.Arrays;

import static aiproj.slider.Input.*;
import aiproj.slider.PrincipalVariation;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.tanh;
import static java.lang.Math.abs;
import static java.lang.Math.pow;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.List;

/**
 * A player for the game "Slider"
 * For Artificial Intelligence at the University of Melbourne
 * by David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * 2017-03-26
 *
 * See comments.txt for details
 *
 *  TD Leaf Lambda credit to:
 *   https://www.cs.princeton.edu/courses/archive/fall06/cos402/papers/chess-RL.pd
 *   http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.140.1523&rep=rep1&type=pdf
 *
 */
public class TDPlayerFour implements SliderPlayer {

    // Keeping Track of information about the current board
    private Board currentBoard;
    private int boardSize;
    private String ourPlayer;
    private String opponent;
    private ArrayList<Tile> playerTiles = new ArrayList<Tile>();
    private ArrayList<Tile> opponentTiles = new ArrayList<Tile>();
    private double moveCount;

    // Information about weights (for the evaluation function)
    private ArrayList<Double> weights;
    private static final String WEIGHTS_FILE = "weights_four.txt";

    // Maximum search depth
    private static final int MAX_DEPTH = 7;

    // Parameters for TDLeaf(Lambda)
    private static final double ALPHA = 1.0;
    private static final double SHRINK_FACTOR = 0.2;
    private static final double LAMBDA = 0.98;
    private static final double DELTA = 0.0001;
    private ArrayList<PrincipalVariation> principalVariations = new ArrayList<PrincipalVariation>();

    // Whether the game is complete or not
    private Boolean incomplete;

    // For turning training on or off
    private static final Boolean td = true;
    private static final Boolean makeupdates = false;


    /**
     * Prepare Slider Player for a given board and player
     *
     * @param dimension The width and height of the board in cells
     * @param board A string representation of the initial state of the board,
     * as described in the part B specification
     * @param player 'H' or 'V', corresponding to which pieces the player will
     * control for this game ('H' = Horizontal, 'V' = Vertical)
     */
    public void init(int dimension, String board, char player) {

        // Read in the weights file
        weights = Input.readWeightFile(WEIGHTS_FILE);

        // Assigning Player Pieces
        String playerType = Character.toString(player);
        ourPlayer = playerType;
        opponent = playerType.equals(Tile.PLAYER_H) ? Tile.PLAYER_V : Tile.PLAYER_H;

        // Reading in the Board
        currentBoard = readBoard(dimension, board);

        // Saving Board size
        boardSize = dimension;

        // Getting All Possible Moves
        refresh(currentBoard);
        //saveBoard(currentBoard);

        incomplete = true;

        // Initialising the move counter to 0
        moveCount = 0.0;
    }


    /**
     * Notify the player of the last move made by their opponent. In response to
     * this method, your player should update its internal representation of the
     * board state to reflect the result of the move made by the opponent.
     *
     * @param move A Move object representing the previous move made by the
     * opponent, which may be null (indicating a pass). Also, before the first
     * move at the beginning of the game, move = null.
     */
    public void update(Move move) {

        // Update the current board
        modifyBoard(currentBoard, move);
        refresh(currentBoard);
        moveCount += 1;

        // Start TDLeaf if the game has ended
        if (incomplete && (currentBoard.getHTiles().size() == 0 || currentBoard.getVTiles().size() == 0)) {
            if (td) {
                tdLeaf();
                System.out.println(weights);
            }
            incomplete = false;
        }
    }


    /**
     *  Decides which move to make (based on minimax decision)
     * @return the move
     */
    public Move move() {
        if (currentBoard.getMovesPlayer().isEmpty()) {
            return null;
        }
        Move firstMove = minimaxDecision(currentBoard);
        update(firstMove);

        return firstMove;
    }

    // ------------------------------
    //
    // TD LEAF(LAMBDA)
    //
    // ------------------------------

    /**
     *  The TD leaf lambda algorithm
     *  Called at the end of a game, updates weights in the weights file.
     */
    public void tdLeaf() {
        // Initialise ArrayList of New Weights
        ArrayList<Double> newWeights = new ArrayList<Double>();

        // Initialise Array of temporal Differences
        ArrayList<Double> tempDifferences = new ArrayList<Double>();
        for (int i = 0; i < principalVariations.size() - 1; i++) {
            tempDifferences.add(tempDiff(i));
        }

        // Calculate sumdiff arraylist for each weight, so it can be re-used for each weight (doesn't depend on weight)
        ArrayList<Double> sumDiffs = new ArrayList<Double>();
        for (int t = 0; t < principalVariations.size() - 1; t++) {
            sumDiffs.add(sumDiff(t, tempDifferences));
        }

        // For each weight i calculate the weight update
        for (int i = 0; i < weights.size(); i++) {
            double weightUpdate = ALPHA*(updateVal(i, sumDiffs));
            newWeights.add(weights.get(i) + weightUpdate);
        }
        System.out.println("Updating File to: " + newWeights);
        if (makeupdates) Input.updateWeightFile(newWeights, WEIGHTS_FILE);
    }

    /**
     *  Calculates the update value to be applied to a weight (after being modulated by alpha)
     * @param i
     * @param sumDiffs
     * @return
     */
    private double updateVal(int i, ArrayList<Double> sumDiffs) {
        double sum = 0;

        // Each time t has a principal variation, which we consider
        for (int t = 0; t < principalVariations.size() - 1; t++) {

            // Get the feature Array for the principal variation in question
            ArrayList<Double> features = principalVariations.get(t).getFeatures();

            // Get the feature so we can get the derivative
            double featureI = features.get(i) * SHRINK_FACTOR;

            // Calculate the partial derivative of the eval function wrt weight i
            double eval1 = tanh((features.get(i) * (weights.get(i) + DELTA)) * SHRINK_FACTOR);
            double eval2 = tanh((features.get(i) * (weights.get(i))) * SHRINK_FACTOR);
            double partialDerivative2 = (eval1 - eval2) / DELTA;

            // Multiply the partial derivative by the temporal difference sum
            sum += partialDerivative2 * sumDiffs.get(t);
        }
        return sum;
    }


    /**
     * Calculates the sum of the temporal differences, as modulated by LAMBDA
     * @param t
     * @param tempDifferences
     * @return
     */
    private double sumDiff(int t, ArrayList<Double> tempDifferences) {
        double sumDiff = 0;
        for (int j = t; j < principalVariations.size() - 1; j++) {

            // Takes the minimum of the temporal difference and 0, so that increased temporal differences (the
            // opponent making a sub-optimal move, by our estimation) are not included in the training
            sumDiff += pow(LAMBDA, j-t) * tempDifferences.get(j-t);
        }
        return sumDiff;
    }

    /**
     *  Returns the temporal difference
     * @param t
     * @return
     */
    private double tempDiff(int t) {
        return principalVariations.get(t+1).getValue() - principalVariations.get(t).getValue();
    }

    // ------------------------------
    //
    // EVALUATION
    //
    // ------------------------------

    /**
     *  Returns the normalised evaluation function of a board
     * @param board the board
     * @param features features of the board
     * @param weightarray feature weights
     * @return normalised evaluation function
     */
    public double evaluate(Board board, ArrayList<Double> features, ArrayList<Double> weightarray) {

        // The end state of the game should be properly evaluated by the evaluation function.
        if (board.getPlayerTiles(opponent).size() == 0) {
            return -1;
        }
        if (board.getPlayerTiles(ourPlayer).size() == 0) {
            return 1;
        }

        // Sum total based on feature weights
        double total = 0;
        for (int i = 0; i < weightarray.size();i++) {
            //System.out.println("Feature: " + i + " == " + features.get(i) + "; Weight == " + weights.get(i));
            total += weightarray.get(i) * features.get(i);
        }
        // Squashes Evaluation function to between -1 and 1, and the shrink factor allows for the high feature weights.
        return tanh(total*SHRINK_FACTOR);
    }

    /**
     * Find the features of a given board
     * @param board the board
     * @return an ArrayList of doubles representing the feature values
     */
    public ArrayList<Double> evalFeatures(Board board) {

        int boardLength = currentBoard.getLength();

        // Arraylist of player and opponent tiles
        ArrayList<Tile> ourTiles = board.getPlayerTiles(ourPlayer);
        ArrayList<Tile> theirTiles = board.getPlayerTiles(opponent);


        // Feature 1
        // the difference in the number of player and opponent tiles
        double playerTileDifference = ourTiles.size() - theirTiles.size();


        // Feature 2
        // Maximise the opponent's distances, minimise our own distances
        double sumPlayerDistances = sumDistances(ourTiles,  board.getLength());
        double sumOpponentDistances = sumDistances(theirTiles, board.getLength());

        // Feature 3
        // Maximise possible forward moves for player, minimise for opponent
        // A heuristic ratio for number of forward moves each player can make (see comments.txt for details)
        double forwardOpp = (forwardMoves(board.getMovesOpponent(), opponent) / theirTiles.size()) * (boardSize - 1);
        double forwardPla = (forwardMoves(board.getMovesPlayer(), ourPlayer) / ourTiles.size()) * (boardSize - 1);
        double forwardAdv = (Double.isNaN(forwardPla) ? 0.0 : forwardPla) - (Double.isNaN(forwardOpp) ? 0.0 : forwardOpp);


        // Features 4-18
        // Sum of total number of player and opponent tiles at each distance from the edge

        // Initialise arrays of all 0s, where index position i is the number of player/opponent tiles which have
        // a distance to the goal edge of i
        int[] playerTileDistanceTotals = new int[Board.MAX_SIZE];
        int[] opponentTileDistanceTotals = new int[Board.MAX_SIZE];

        int distance;

        // Iterate through player tiles
        for (int i = 0; i < ourTiles.size(); i++) {

            //determine distance from the edge
            if (ourPlayer.equals(Tile.PLAYER_H)) {
                distance = boardLength - (ourTiles.get(i).getX()) - 1;
            }
            else {
                distance = boardLength - (ourTiles.get(i).getY()) - 1;
            }
            //increase the counter for the number of player tiles at that distance
            playerTileDistanceTotals[distance] +=1;
        }


        // iterate through opponent's tiles
        for (int i = 0; i < theirTiles.size(); i++) {

            //determine distance from the edge
            if (opponent.equals(Tile.PLAYER_H)) {
                distance = boardLength - (theirTiles.get(i).getX()) - 1;
            }
            else {
                distance = boardLength - (theirTiles.get(i).getY()) - 1;
            }
            //increase the counter for the number of opponent tiles at that distance
            opponentTileDistanceTotals[distance] += 1;
        }


        // Add all features to an ArrayList
        ArrayList<Double> features = new ArrayList<Double>();

        features.add(playerTileDifference);
        features.add(sumOpponentDistances - sumPlayerDistances);
        features.add(forwardAdv);

        for (int i = 0; i < Board.MAX_SIZE; i++) {
            features.add((double)playerTileDistanceTotals[i]);
            features.add((double)opponentTileDistanceTotals[i]);

        }

        return features;
    }

    /**
     * Returns total number of forward (towards goal edge) moves for a player
     * @param moves arraylist of moves for the player
     * @param player the player
     * @return the number of forward moves
     */
    private double forwardMoves(ArrayList<Move> moves, String player) {
        if (moves.size() < 1) {
            return 0;
        }
        // If there are possible forward moves to check:
        double total = 0;
        for (Move move : moves) {
            if (player.equals(Tile.PLAYER_H)) {
                total += (move.d == Move.Direction.RIGHT) ? 1 : 0;
            }
            else {
                total += (move.d == Move.Direction.UP) ? 1 : 0;
            }
        }
        return total;
    }

    /**
     *  Sum the distances of a set of tiles (ie player or opponent tiles) to the respective goal edge
     * @param tiles the player or opponent tiles. All tiles passed in must belong to only one player.
     * @param boardSize size of the board
     * @return sum of distances
     */
    private double sumDistances(ArrayList<Tile> tiles, int boardSize) {

        if (tiles.size() <1) {
            return 0.0;
        }

        // Set the player type (all tiles passed in are the same player)
        String player = tiles.get(0).getCellType();
        double total = 0.0;
        for (Tile tile : tiles) {
            if (player.equals(Tile.PLAYER_H)) {
                total += boardSize-tile.getX();
            }
            else {
                total += boardSize-tile.getY();
            }
        }
        return total;
    }


    // ------------------------------
    //
    // MINIMAX
    //
    // ------------------------------

    /**
     *  Makes a minimax decision on the optimal move based on a board
     * @param board the board
     * @return the optimal move
     */
    private Move minimaxDecision(Board board) {
        ArrayList<Move> moves = board.getMovesPlayer();
        if (moves.size() == 0) {
            return null;
        }

        double maxVal = Double.NEGATIVE_INFINITY;
        double beta = Double.POSITIVE_INFINITY;

        Move maxMove = moves.get(0);
        PrincipalVariation variation = null;
        PrincipalVariation potentialVariation = null;
        for (Move move : moves) {
            Board newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = minValue(newBoard,1, maxVal, beta);
            double val = potentialVariation.getValue();

            if (val > maxVal) {
                maxVal = val;
                maxMove = move;
                variation = potentialVariation;

            }
        }
        // With the 'True Utility' propagated from the leaf node, update our weights using tdLeaf
        if (td) {
            principalVariations.add(variation);
        }
        return maxMove;
    }

    /**
     *  Returns the max principal variation based on board and parameters (see comments.txt for details)
     * @param board the board
     * @param depth depth
     * @param alpha alpha parameter (in td leaf)
     * @param beta beta parameter (in td leaf)
     * @return the max principal variation
     */
    private PrincipalVariation maxValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesPlayer();
        PrincipalVariation potentialVariation = null;

        if (terminalTest(depth) || moves.size() == 0) {
            ArrayList<Double> features = evalFeatures(board);
            return new PrincipalVariation(board, features, evaluate(board, features, weights));
        }

        double value = Double.NEGATIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = minValue(newBoard, depth+1, alpha, beta);
            value = max(value, potentialVariation.getValue());

            if (value >= beta) {
                return potentialVariation;
            }
            alpha = max(alpha, value);
        }

        return potentialVariation;
    }


    /**
     *  Returns the min principal variation based on board and parameters (see comments.txt for details)
     * @param board the board
     * @param depth depth
     * @param alpha alpha parameter (in td leaf)
     * @param beta beta parameter (in td leaf)
     * @return the min principal variation
     */

    private PrincipalVariation minValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesOpponent();
        PrincipalVariation potentialVariation = null;

        if (terminalTest(depth) || moves.size() == 0) {
            ArrayList<Double> features = evalFeatures(board);
            return new PrincipalVariation(board, features, evaluate(board,features, weights));
        }


        double value = Double.POSITIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = maxValue(newBoard, depth+1, alpha, beta);
            value = min(value, potentialVariation.getValue());

            if (value <= alpha) {
                return potentialVariation;
            }
            beta = min(beta, value);
        }

        return potentialVariation;
    }

    /**
     * Test whether the node is a terminal node (ie beyond max depth)
     * @param depth the depth
     * @return true or false (whether we have reached terminal state or not)
     */
    private boolean terminalTest(int depth) {
        if (depth >= MAX_DEPTH) {
            return true;
        }
        return false;
    }

    /**
     *  Modifies a board with a given move
     * @param board the board
     * @param move the move
     * @return the modified board
     */
    private Board modifyBoard(Board board, Move move) {

        if (move == null) {
            return board;
        }

        // Provided Move Implementation uses i as x and j as y
        int fromX = move.i;
        int fromY = move.j;
        int toX = fromX;
        int toY = fromY;

        Tile fromTile = board.getTile(fromX, fromY);
        String cellType = fromTile.getCellType();

        if (move.d == Move.Direction.LEFT) {
            toX -= 1;
        }
        if (move.d == Move.Direction.RIGHT) {
            toX += 1;
        }
        if (move.d == Move.Direction.UP) {
            toY += 1;
        }
        if (move.d == Move.Direction.DOWN) {
            toY -= 1;
        }

        // update the board
        updateTileArray(board, move);
        board.updateTile(toX, toY, cellType);
        board.updateTile(fromX, fromY, Tile.EMPTY);
        refresh(board);

        return board;
    }

    // ------------------------------
    //
    // State Maintenance
    //
    // ------------------------------

    /**
     * Refreshes variables about the board given its state and given which turn has been played
     * @param board the board
     */
    private void refresh(Board board) {
        board.setMovesPlayer(board.getAllMoves(ourPlayer));
        board.setMovesOpponent(board.getAllMoves(opponent));

        // Set list of tiles based on who the player is
        if (ourPlayer.equals(Tile.PLAYER_H)) {
            this.playerTiles = board.getHTiles();
            this.opponentTiles = board.getVTiles();

        }
        else if (ourPlayer.equals(Tile.PLAYER_V)) {
            this.playerTiles = board.getVTiles();
            this.opponentTiles = board.getHTiles();
        }
        else {
            System.out.println("Catastrophic error: incorrect player name");
            System.exit(0);
        }
    }

    /**
     * Updates the recorded array of tiles of a board, given the new move
     * @param newMove A move object, played either by the player or by the opponent.
     * @param board the board
     */

    private void updateTileArray(Board board, Move newMove) {
        // Find new X and Y
        int oldX = newMove.i;
        int oldY = newMove.j;
        Move.Direction direction = newMove.d;

        int newX = (direction == Move.Direction.LEFT) ? (oldX - 1) : oldX;
        newX = (direction == Move.Direction.RIGHT) ? (oldX + 1) : newX;

        int newY = (direction == Move.Direction.UP) ? (oldY + 1) : oldY;
        newY = (direction == Move.Direction.DOWN) ? (oldY - 1) : newY;

        // Retrieve old tile, and its type
        Tile oldTile = board.getTile(oldX, oldY);
        String tileType = oldTile.getCellType();

        // Get Positional Indices for new tile
        int[] newpos = board.getPos(newX, newY);
        int newRow = newpos[0];
        int newCol = newpos[1];

        // Use these to find New tile
        Tile newtile = new Tile(tileType, newRow, newCol, board.getLength());

        // If h, remove old tile from hTiles and add new tile to hTiles
        if (tileType.equals(Tile.PLAYER_H)) {
            board.removeHTile(board.getTile(oldX, oldY));
            if (validPos(newX, newY, board)) {
                board.addHTile(newtile);
            }
        }
        // If V (updateTiles only called on non-null moves so can assume it's V if not H)
        else if (tileType.equals(Tile.PLAYER_V)) {
            board.removeVTile(board.getTile(oldX, oldY));
            if (validPos(newX, newY,board)) {
                board.addVTile(newtile);
            }
        }
    }

    // ------------------------------
    //
    // GETTER & GENERAL METHODS
    //
    // ------------------------------

    /**
     *
     * @return the current board
     */
    public Board getBoard() {
        return currentBoard;
    }

    /**
     *
     * @return the weights
     */
    public ArrayList<Double> getWeights() {
        return weights;
    }

    /**
     *
     * @return the player as a string
     */
    public String getOurPlayer() {
        return ourPlayer;
    }

    /**
     *
     * @return the opponent as a string
     */
    public String getOpponent() {
        return opponent;
    }


    /**
     *  Returns true iff the given position of the given board is free
     * @param x x position
     * @param y y position
     * @param board the board
     * @return freeness of position
     */
    private Boolean validPos(int x, int y, Board board) {
        boardSize = board.getLength();
        if (x < boardSize && x >= 0 && boardSize-y-1 < boardSize && boardSize-y-1 >= 0) {
            return true;
        }
        return false;
    }

    /**
     *
     * @return the move count of the current board
     */
    public double getMoveCount() {
        return moveCount;
    }
}
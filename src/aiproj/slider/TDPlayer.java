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


public class TDPlayer implements SliderPlayer {

    // Keeping Track of the Board, Player Piece Types, Number of current legal moves
    private Board curr_board;
    private int boardsize;
    private String ourPlayer;
    private String Opponent;
    private ArrayList<Tile> playerTiles = new ArrayList<Tile>();
    private ArrayList<Tile> opponentTiles = new ArrayList<Tile>();
    private double movecount;
    private ArrayList<Double> weights = new ArrayList<Double>(Arrays.asList(-1.0, 1.0, -1.0, 1.0, 0.5, -0.5));//, 1.0));

    // debug
    private static final Boolean debug = false;
    private ArrayList<ArrayList<ArrayList<Double>>> vals;
    // end debug

    private static final int MAX_DEPTH = 6;

    // For TDLeaf(Lambda)
    private static final double ALPHA = 0.3;
    private static final double SHRINK_FACTOR = 0.2;  // 0.01;
    private static final double LAMBDA = 0.75;
    private static final String WEIGHTS_FILE = "weights.txt";
    private ArrayList<PrincipalVariation> principalVariations = new ArrayList<PrincipalVariation>();
    private Boolean incomplete;

    // Learn Weights w/ TDLeaf?
    private static final Boolean td = true;


    // Add more info to track here, for the Evaluation function


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
        weights = readWeightFile();

        // Assigning Player Pieces
        String playerType = Character.toString(player);
        ourPlayer = playerType;
        Opponent = playerType.equals(Tile.PLAYER_H) ? Tile.PLAYER_V : Tile.PLAYER_H;

        // Reading in the Board
        curr_board = readBoard(dimension, board);

        // Saving Board size
        boardsize = dimension;

        // Getting All Possible Moves
        refresh(curr_board);
        //saveBoard(curr_board);

        incomplete = true;

        // Initialising the move counter to 0
        movecount = 0.0;
    }


    private ArrayList<Double> readWeightFile() {

        FileReader in=null;
        String s = "";

        try {
            in = new FileReader(WEIGHTS_FILE);
            int c;
            while ((c = in.read()) != -1) {
                //System.out.println((char)c);
                s = s + (char) c;
            }
            String[] weightsString = s.split(" ");
            for (int i = 0; i < weightsString.length; i++) {
                weights.set(i, Double.parseDouble(weightsString[i]));
            }
            in.close();
        }
        catch ( Exception e) {
            System.out.println("FILE READ FAIL");
            System.exit(0);
        }

        return weights;
    }

    private void updateWeightFile(ArrayList<Double> weights) {

        FileWriter out=null;
        String s = "";

        try {
            out = new FileWriter(WEIGHTS_FILE);

            for (int i =0;i<weights.size();i++) {
                s += Double.toString(weights.get(i));
                s+= " ";
            }
            out.write(s);
            out.close();

        }
        catch ( Exception e) {
            System.out.println("FILE READ FAIL");
            System.exit(0);
        }
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
        modifyBoard(curr_board,move);
        refresh(curr_board);
        movecount += 1;

        // Start TDLeaf if the game has ended!
        //System.out.printf("After updating, H Tiles = %d, V tiles = %d\n", curr_board.getH_tiles().size(), curr_board.getV_tiles().size());
        if (incomplete && (curr_board.getH_tiles().size() == 0 || curr_board.getV_tiles().size() == 0)) {
            tdLeaf();
            System.out.println(weights);
            incomplete = false;
        }
        System.out.printf("Player Tiles = %d, Opponent Tiles = %d\n", playerTiles.size(), opponentTiles.size());
    }

    /**
     * Request a decision from the player as to which move they would like to
     * make next. Your player should consider its options and select the best
     * move available at the time, according to whatever strategy you have
     * developed.
     *
     * The move returned must be a legal move based on the current
     * state of the game. If there are no legal moves, return null (pass).
     *
     * Before returning your move, you should also update your internal
     * representation of the board to reflect the result of the move you are
     * about to make.
     *
     * @return a Move object representing the move you would like to make
     * at this point of the game, or null if there are no legal moves.
     */
    public Move move() {
        if (curr_board.getMovesPlayer().isEmpty()) {
            return null;
        }
        Move firstMove = minimaxDecision(curr_board);
        update(firstMove);

        //DEBUG
        //System.out.println("Board evaluation function (player "+ourPlayer+"): "+ evaluateBoard(curr_board));
        // end debug
        // if TERMINAL-STATE:
        //     TDLeaf(Boards, weights, EVAL, alpha)
        //

        return firstMove;
    }




    // ------------------------------
    //
    // TD LEAF(LAMBDA)
    //
    // https://www.cs.princeton.edu/courses/archive/fall06/cos402/papers/chess-RL.pd
    // http://citeseerx.ist.psu.edu/viewdoc/download?doi=10.1.1.140.1523&rep=rep1&type=pdf
    // ------------------------------

    public void tdLeaf() {

        // Calculate sumdiff arraylist for each weight, so it can be re-used for each weight
        ArrayList<Double> sumdiffs = new ArrayList<Double>();
        for (int t = 0; t < principalVariations.size() - 1; t++) {
            sumdiffs.add(sumDiff(t));
        }
        // For each weight i
        for (int i = 0; i < weights.size(); i++) {
            weights.set(i, weights.get(i) + ALPHA*(updateVal(i, sumdiffs)));
        }
        updateWeightFile(weights);
    }


    // Calculates the update value to be applied to the weight (after being modulated by alpha)
    private double updateVal(int i, ArrayList<Double> sumdiffs) {
        double sum = 0;
        // Each time t has a principal variation, which we consider
        for (int t = 0; t < principalVariations.size() - 1; t++) {

            // Get the feature Array for the principal variation in question
            ArrayList<Double> features = principalVariations.get(t).getFeatures();

            // Get the feature so we can get the derivative
            double feat_i = features.get(i) * SHRINK_FACTOR;

            // Calculate the partial derivative of the eval function wrt weight i, mult. by the temporal difference
            sum += (1.0 - tanh(feat_i) * tanh(feat_i)) * sumdiffs.get(t);
        }
        return sum;
    }


    // Calculates the sum of the temporal differences, as modulated by LAMBDA
    private double sumDiff(int t) {
        double sum_diff = 0;
        for (int j = t; j < principalVariations.size() - 1; j++) {
            // Takes the minimum of the temporal difference and 0, so that increased temporal differences (the
            // opponent making a sub-optimal move, by our estimation) are not included in the training
            sum_diff += pow(LAMBDA, j-t) * tempDiff(t);
        }
        return sum_diff;
    }

    // CHANGE:
    // Modify to return 0 if the difference is positive? Positive differences arrive from opponent errors.
    // Only do the above if the player being played against is potentially a bad player
    // Temporal Difference of the leaf node of the principal variation at time t
    private double tempDiff(int t) {
        return principalVariations.get(t+1).getValue() - principalVariations.get(t).getValue();
    }


    // ------------------------------
    //
    // EVALUATION
    //
    // ------------------------------

    public double evaluate(Board board, ArrayList<Double> features) {

        // The end state of the game should be properly evaluated by the evaluation function.
        if (opponentTiles.size() == 0) {
            return -1;
        }
        if (playerTiles.size() == 0) {
            return 1;
        }

        // Sum total based on feature weights
        double total = 0;
        for (int i = 0; i < weights.size();i++) {
            //System.out.println("Feature: " + i + " == " + features.get(i) + "; Weight == " + weights.get(i));
            total += weights.get(i) * features.get(i);
        }
        // Squashes Evaluation function to between -1 and 1, and the shrink factor allows for the high feature weights.
        return tanh(total*SHRINK_FACTOR);
    }


    public ArrayList<Double> evalFeatures(Board board) {
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

        // Need our features to be of a relatively consistent size across the game, so as to not distort the weights
        // Maximise the opponent's distances, minimise our own distances; positive weight
        double sumPlayerDistances = sumDistances(playerTiles,  board.getLength());
        double sumOpponentDistances = sumDistances(opponentTiles, board.getLength());

        // Added Forward Moves -> Players were starting out by choosing the first possible move (moves.get(0))
        // Maximise possible forward moves for player, minimise for opponent
        double forwardMovesOpp = (forwardMoves(board.getMovesOpponent(), Opponent)/opponentTiles.size())*(boardsize-1);
        double forwardMovesPla = (forwardMoves(board.getMovesPlayer(), ourPlayer)/playerTiles.size())*(boardsize-1);

        // Add all features to an ArrayList
        ArrayList<Double> features = new ArrayList<Double>();
        features.add(playerTiles.size()*1.0);            // -1.0
        features.add(opponentTiles.size()*1.0);          //  1.0
        features.add(sumPlayerDistances);           // -1.0
        features.add(sumOpponentDistances);         //  1.0
        features.add(forwardMovesPla);              //  0.5
        features.add(forwardMovesOpp);              // -0.5
        //features.add(movecount);                                //  1.0
        return features;
    }

    private double forwardMoves(ArrayList<Move> moves, String player) {
        double total = 0;

        if (moves.size() <1) {
            return 0;
        }

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


    private double sumDistances(ArrayList<Tile> tiles, int boardSize) {
        double total = 0;

        if (tiles.size() <1) {
            return 0;
        }

        //Set the player type (assume all tiles passed in are the same player)
        String player = tiles.get(0).getCellType();

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

    private Move minimaxDecision(Board board) {
        ArrayList<Move> moves = board.getMovesPlayer();
        if (moves.size() == 0) {
            return null;
        }
        // debug  -> Cannot put inside an if-statement.
        vals = new ArrayList<ArrayList<ArrayList<Double>>>();
        for (int i = 0; i < MAX_DEPTH; i++) {vals.add(new ArrayList<ArrayList<Double>>());}
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug

        double maxVal = Double.NEGATIVE_INFINITY;
        Move maxMove = moves.get(0);
        PrincipalVariation variation = null;
        PrincipalVariation potentialVariation = null;
        for (Move move : moves) {
            Board newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = minValue(newBoard,1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            double val = potentialVariation.getValue();
            //debug
            if (debug) {
                System.out.println(move.toString() + " = val: " + val);
                myVals.add(potentialVariation.getValue());
            }
            // end debug

            if (val > maxVal) {
                maxVal = val;
                maxMove = move;
                variation = potentialVariation;

            }
            /*
            ArrayList<Double> features = evalFeatures(newBoard);
            for (int i=0; i<weights.size();i++) {
                System.out.println("Feature: " + i + " == " + features.get(i) + "; Weight == " + weights.get(i));
            }
            */
        }
        // With the 'True Utility' propagated from the leaf node, update our weights using tdLeaf
        if (td) {
            principalVariations.add(variation);
        }
        //debug

        if (debug) {
            vals.get(0).add(myVals);
            System.out.println("TDPlayer " + ourPlayer + " max: " + maxVal + " from the following minimax tree:");

            for (int i = 0; i < vals.size(); i++) {
                System.out.println("depth " + i + " : " + vals.get(i));}
        }
        // end debug
        return maxMove;

    }

    private PrincipalVariation maxValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesPlayer();
        PrincipalVariation potentialVariation = null;

        if (terminalTest(depth) || moves.size() == 0) {
            ArrayList<Double> features = evalFeatures(board);
            return new PrincipalVariation(board, features, evaluate(board, features));
        }
        //debug
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug

        double value = Double.NEGATIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = minValue(newBoard, depth+1, alpha, beta);
            value = max(value, potentialVariation.getValue());
            //debug
            if (debug) {
                myVals.add(value);
            }
            // end debug
            if (value >= beta) {
                return potentialVariation;
            }
            alpha = max(alpha, value);
        }

        //debug
        if (debug) vals.get(depth).add(myVals);
        // end debug
        return potentialVariation;
    }



    private PrincipalVariation minValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesOpponent();
        PrincipalVariation potentialVariation = null;

        if (terminalTest(depth) || moves.size() == 0) {
            ArrayList<Double> features = evalFeatures(board);
            return new PrincipalVariation(board, features, evaluate(board, features));
        }

        //debug
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug


        double value = Double.POSITIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            potentialVariation = minValue(newBoard, depth+1, alpha, beta);
            value = max(value, potentialVariation.getValue());
            //debug
            if (debug) myVals.add(value);
            // end debug
            if (value <= alpha) {
                return potentialVariation;
            }
            beta = min(beta, value);
        }

        //debug
        if (debug) {
            vals.get(depth).add(myVals);
        }
        // end debug

        return potentialVariation;
    }


    private boolean terminalTest(int depth) {
        if (depth >= MAX_DEPTH) {
            return true;
        }
        return false;
    }

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
     */
    private void refresh(Board board) {
        board.setMovesPlayer(board.getAllMoves(ourPlayer));
        board.setMovesOpponent(board.getAllMoves(Opponent));
    }

    /**
     * Updates the recorded array of tiles of a board, given the new move
     * @param newMove A move object, played either by the player or by the opponent.
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

        // If h, remove old tile from h_tiles and add new tile to h_tiles
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

    /* Getter Method for Board */
    public Board getBoard() {
        return curr_board;
    }

    public ArrayList<Double> getWeights() {
        return weights;
    }

    /* Getter Method for ourPlayer */
    public String getOurPlayer() {
        return ourPlayer;
    }

    /* Getter Method for Opponent */
    public String getOpponent() {
        return Opponent;
    }


    /**
     *  Returns true iff the given position of the given board is free
     * @param x
     * @param y
     * @param board
     * @return
     */
    private Boolean validPos(int x, int y, Board board) {
        boardsize = board.getLength();
        if (x < boardsize && x >= 0 && boardsize-y-1 < boardsize && boardsize-y-1 >= 0) {
            return true;
        }
        return false;
    }

    public double getMoveCount() {
        return movecount;
    }

    /*
    private void saveBoard(Board board){
        positions.add(board);
    }
    */

    /*
    public void tdLeaf(ArrayList<Board> positions, double leafUtility) {
        for (int i = movecount; i < positions.size(); i++) {
            updateWeights(positions.get(i), leafUtility, i);
        }
        System.out.println("Weights: " + weights);
    }

    private void updateWeights(Board position, double trueUtil, int j) {
        double util = evaluateBoard(position);
        ArrayList<Double> board_features = evalFeatures(position);
        int iterations = MAX_ITERATIONS;
        while (abs(util - trueUtil) > EPS && iterations > 0) {
            for (int i = 0; i < weights.size(); i++) {

                //double new_weight = weights.get(i) - ALPHA*(pow(util - trueUtil, j))*board_features.get(i);
                //double new_weight = weights.get(i) - j*ALPHA*(util - trueUtil)*board_features.get(i);
                double new_weight = weights.get(i) - ALPHA*(util - trueUtil)*board_features.get(i);
                weights.set(i, new_weight);
                util = evaluateBoard(position);
            }
            //System.out.println("Iteration: " + (iterations));
            iterations -= 1;
        }
    }
    */




    /*
    private double evaluateBoard(Board board) {
        ArrayList<Double> features = evalFeatures(board);

        // The end state of the game should be properly evaluated by the evaluation function.
        if (opponentTiles.size() == 0) {
            return -1;
        }
        if (playerTiles.size() == 0) {
            return 1;
        }

        // Sum total based on feature weights
        double total = 0;
        for (int i=0; i<weights.size();i++) {
            //System.out.println("Feature: " + i + " == " + features.get(i) + "; Weight == " + weights.get(i));
            total += weights.get(i) * features.get(i);
        }
        // Squashes Evaluation function to between -1 and 1, and the shrink factor allows for the high feature weights.
        return tanh(total*SHRINK_FACTOR);
    }
    */






    /*
    private void updatePrevMinStates(Board newBoard, double value) {
        if (!previous_min_states.containsKey(newBoard)) {
            previous_min_states.put(newBoard, value);
        }
    }
    private void updatePrevMaxStates(Board newBoard, double value) {
        if (!previous_max_states.containsKey(newBoard)) {
            previous_max_states.put(newBoard, value);
        }
    }*/


        /* Getter Method for legalMoveCountPlayer */
    //public ArrayList<Move> getMovesPlayer() { return movesPlayer; }

    /* Getter Method for legalMoveCountPlayer */
    //public ArrayList<Move> getMovesOpponent() {
    //    return movesOpponent;
    //}

    /*
    public ArrayList<Move> getMoves(String player) {

        if (player.equals(Tile.PLAYER_H)) {
            return hMoves;
        }

    }
    */


}
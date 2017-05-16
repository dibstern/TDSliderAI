package aiproj.slider;

import java.lang.Character;
import java.util.ArrayList;

import static aiproj.slider.Input.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.lang.Math.tanh;


public class DopePlayer implements SliderPlayer {

    // Keeping Track of the Board, Player Piece Types, Number of current legal moves
    private Board curr_board;
    private int boardsize;
    private String ourPlayer;
    private String Opponent;
    private ArrayList<Tile> playerTiles;
    private ArrayList<Tile> opponentTiles;

    // debug
    private static final Boolean debug = false;
    ArrayList<ArrayList<ArrayList<Double>>> vals;
    // end debug

    private static final int MAX_DEPTH = 4;

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
    }


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
        //Move firstMove = movesPlayer.get(0);
        Move firstMove = minimaxDecision(curr_board);
        update(firstMove);

        //DEBUG
        //System.out.println("Board evaluation function (player "+ourPlayer+"): "+ evaluateBoard(curr_board));
        // end debug


        return firstMove;
    }

    private double evaluateBoard(Board board) {

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


        //FEATURE DEFINITION


        // playerTileDifference is preferable to having a separate number for each player tiles
        // This is because the tile difference will perform better in machine learning (fewer, less
        // noisy possible values)
        double playerTileDifference = playerTiles.size() - opponentTiles.size();

        // Sum the distances of each player
        double sumPlayerDistances = sumDistances(playerTiles,  board.getLength());
        double sumOpponentDistances = sumDistances(opponentTiles, board.getLength());

        // The end state of the game should be properly evaluated by the evaluation function.
        if (opponentTiles.size() == 0) {
            return -1;
        }
        if (playerTiles.size() == 0) {
            return 1;
        }

        // Maximise possible forward moves for player, minimise for opponent
        double forwardMovesOpponent = forwardMoves(board.getMovesOpponent(), Opponent);
        double forwardMovesPlayer = forwardMoves(board.getMovesPlayer(), ourPlayer);

        // Add all features to an arraylist and define weights
        // Weights are presently +ve (good) or -ve (bad)
        // Eventually we will define these elsewhere with Machine Learning

        ArrayList<Double> features = new ArrayList<Double>();
        ArrayList<Double> featureWeights = new ArrayList<Double>();
        features.add(playerTileDifference);
        featureWeights.add(-0.9);
        features.add(sumPlayerDistances);
        featureWeights.add(-1.0);
        features.add(sumOpponentDistances);
        featureWeights.add(1.0);

        // Added Forward Moves -> Players were starting out by choosing the first possible move (moves.get(0))
        features.add(forwardMovesPlayer);
        featureWeights.add(0.5);
        features.add(forwardMovesOpponent);
        featureWeights.add(-0.5);

        // Sum total based on feature weights

        double total = 0;
        for (int i=0; i<featureWeights.size();i++) {
            total += featureWeights.get(i) * features.get(i);
        }
        // Squashes Evaluation function to between -1 and 1, and the .1 allows for the high feature weights.
        return tanh(total*0.1);
    }


    private double forwardMoves(ArrayList<Move> moves, String player) {
        if (moves.size() <1) {
            return 0;
        }
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


    public Move minimaxDecision(Board board) {

        ArrayList<Move> moves = board.getMovesPlayer();

        if (moves.size() == 0) {
            return null;
        }

        // debug  -> Cannot put inside an if-statement.
        vals = new ArrayList<ArrayList<ArrayList<Double>>>();
        for (int i = 0; i < MAX_DEPTH; i++) {vals.add(new ArrayList<ArrayList<Double>>());}
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug

        double max = Double.NEGATIVE_INFINITY;
        Move maxMove = moves.get(0);
        for (Move move : moves) {
            Board newBoard = board.copyBoard();
            modifyBoard(newBoard, move);

            double val = minValue(newBoard,1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);
            //debug
            if (debug) {
                System.out.println(move.toString() + " = val: " + val);
                myVals.add(val);
            }
            // end debug

            if (val > max) {
                max = val;
                maxMove = move;
            }
        }
        //debug
        if (debug) {
            vals.get(0).add(myVals);
            System.out.println("DopePlayer " + ourPlayer + " max: " + max + " from the following minimax tree:");
            for (int i = 0; i < vals.size(); i++) {
                System.out.println("depth " + i + " : " + vals.get(i));}}
        // end debug
        return maxMove;

    }

    public double maxValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesPlayer();

        if (terminalTest(depth) || moves.size() == 0) {
            return evaluateBoard(board);
        }
        //debug
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug

        double value = Double.NEGATIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            value = max(value, minValue(newBoard, depth + 1, alpha, beta));
            //debug
            if (debug) {
                myVals.add(value);
            }
            // end debug
            if (value >= beta) {
                return value;
            }
            alpha = max(alpha, value);
        }

        //debug
        if (debug) vals.get(depth).add(myVals);
        // end debug
        return value;
    }



    public double minValue(Board board, int depth, double alpha, double beta) {

        ArrayList<Move> moves = board.getMovesOpponent();

        if (terminalTest(depth) || moves.size() == 0) {
            return evaluateBoard(board);
        }

        //debug
        ArrayList<Double> myVals = new ArrayList<Double>();
        // end debug


        double value = Double.POSITIVE_INFINITY;
        Board newBoard = null;
        for (Move move : moves) {
            newBoard = board.copyBoard();
            modifyBoard(newBoard, move);
            value = min(value, maxValue(newBoard, depth + 1, alpha, beta));
            //debug
            if (debug) myVals.add(value);
            // end debug
            if (value <= alpha) {
                return value;
            }
            beta = min(beta, value);
        }

        //debug
        if (debug) {
            vals.get(depth).add(myVals);
        }
        // end debug

        return value;
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




    /* Getter Method for Board */
    public Board getBoard() {
        return curr_board;
    }

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

}
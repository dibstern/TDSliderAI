package aiproj.slider;

/**
 * PrincipelVariation class for a  TD Leaf Lambda player for the game "Slider"
 * For Artificial Intelligence at the University of Melbourne
 * by David Stern (dstern 585870) and Hugh Edwards (hughe 584183)
 * 2017-03-26
 *
 * See comments.txt for details
 *
 */

import java.util.ArrayList;


public class PrincipalVariation {

    private Board board;
    private double value;
    private ArrayList<Double> features;

    /**
     *  Construct principle variation
     * @param given_board the given board
     * @param given_features the given features
     * @param given_value the given value
     */
    public PrincipalVariation(Board given_board, ArrayList<Double> given_features, double given_value) {
        board = given_board;
        features = given_features;
        value = given_value;
    }

    /**
     *
     * @return the board
     */
    public Board getBoard() {
        return board;
    }

    /**
     *
     * @return the value, a double
     */
    public double getValue() {
        return value;
    }

    /**
     *
     * @return the features, an arraylist of doubles
     */
    public ArrayList<Double> getFeatures() {
        return features;
    }

}

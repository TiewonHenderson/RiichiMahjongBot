package bot_package;

import java.util.*;

/**
 * This class is an extension to the Prediction class, where algorithmic judgments are made of the opponent
 * The problem with the Prediction class is it assumes concealed hand, no called groups are incorporated to the probability
 * This class will extend those results and give a variety of information formatted in a sequential map.
 */
public class Tile_map 
{
	/**
	 * Based off the danger chart from Daina Chiba’s Riichi Book 1
	 * 	Grade		Number_val			Tile_type
	 * 	SS 			[0]					Genbutsu
	 * 	S			[0-19]				4th Suji terminal; 4th honor tile
	 * 	A+			[20-64]				3rd Suji terminal; 3rd honor tile
	 * 	A			[65-84]				2nd Suji terminal
	 * 	A-			[85-99]				2nd non-seat/prev wind; 1st Suji terminal
	 * 	B+			[100-109]			2nd Honor tile
	 * 	B			[110-139]			INSIDE suji 4,5,6; behind Kabe
	 * 	B-			[140-154]			Suji 2,8
	 * 	C+			[154-164]			Suji 3,7; One Chance [layer < 2]
	 * 	C			[165-199]			First honor
	 * 	C-			[200-225]			One Chance [layer >= 2](i.e if 3 2p are out; 2,3p shape is one chance)
	 * 	F+			[225-234]			Non-suji 2,8
	 * 	F			[235-244]			Non-suji 3,7
	 * 	F-			[245-255]			Non-suji 4,5,6
	 */
	final private ArrayList<Integer> riichi_base_tile_safety_ = new ArrayList<Integer>();
	
	/**
	 * @pre The Player is confirmed to be going for a single suit flush hand
	 * 		This will only score suited / honors tiles for usable tiles in flush hands
	 * 
	 * 	Grade		Number_val			Tile_type
	 * 	SS			[0]					genbustu_suited		(riichi)
	 * 	SS			[0]					this turn discard	(canto + if applicable)
	 * 	S			[1-25]				in Player drop_pile	(canto)
	 * 	A+			[25-99]				4th Honor			(riichi)
	 * 	A-			[50-99]				4th Honor			(canto)
	 * 	B+			[100-129]			3rd Honor
	 * 	B			[130-139]			4th Terminal Suji
	 * 	B			[140-149]			3rd Terminal Suji
	 * 	B			[140-149]			2nd Terminal Suji
	 * 	B			[150-169]			1st Terminal Suji
	 * 	B-			[170-179]			Inside Suji [4,5,6]
	 * 	C+			[180-184]			Suji [2,8]
	 * 	C+			[185-194]			Suji [3,7]
	 * 	C			[195-199]			2nd Honor
	 * 	C			[200-204]			2nd Yaku Honor
	 * 	C-			[205-209]			1st Honor
	 * 	C-			[210-219]			1st Yaku Honor
	 * 	F			[220-239]			Non-Suji 4th tile	(in retrospect to their discard)
	 * 	F-			[240-249]			Non-Suji 3rd tile
	 * 	F-			[250-253]			Non-Suji 2nd tile
	 * 	F-			[254-255]			Non-Suji 1st tile
	 */
	final private ArrayList<Integer> flush_base_tile_safety = new ArrayList<Integer>();
	
	//Kokushi map is just not discarding any Orphan tiles
	
	/**
	 * Saved the current round which this Tile_map belongs to
	 */
	public MJ_round current_round_;
	
	/**
	 * Saved the current Prediction to the Player corresponding to the inputted wind_id
	 */
	protected Prediction current_predictions_;
	
	/**
	 * The wind id of the opponent Player this Tile_map would corresponding against
	 */
	protected int wind_id_;
	
	
	/**
	 * When assigned to a opponent Player, this will store numbers [0,255] that shows [genbutsu, extreme danger]
	 */
	protected ArrayList<Integer> danger_market_;
	
	/**
	 * Each index reflects the probability of this Player's hand score
	 * index 0 = normal hand
	 * index 1 = flush
	 * index 2 = kokushi
	 */
	protected ArrayList<Integer> hand_probabilities_;
	
	
	public Tile_map(Prediction player_judgements, int opponent, MJ_round this_round)
	{
		this.current_round_ = this_round;
		this.wind_id_ = opponent;
		this.current_predictions_ = player_judgements;
		ArrayList<Group> possible_calls = this.current_round_.get_validPlayerCalls(opponent);
		if(possible_calls.size() > 0)
		{
			
		}
	}
	
	public static void main(String[] args)
	{
		
	}
}

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
	 * Saved the current game which this Tile_map belongs to
	 */
	public MJ_game current_game_;
	
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
	
	
	
	public Tile_map(Prediction player_judgements, int opponent, MJ_game this_game)
	{
		this.current_game_ = this_game;
		this.wind_id_ = opponent;
		this.current_predictions_ = player_judgements;
		ArrayList<Group> possible_calls = this_game.get_validPlayerCalls(opponent);
		if(possible_calls.size() > 0)
		{
			
		}
	}
	/**
	 *	@info HashMap Integer score index:
	 * 	0: Dragon Groups -> 1													
	 *	1: Small 3 Dragons -> 3													
	 *	2: Big 3 Dragons -> 5 													
	 *	3: Rotation Wind -> 1 (from gamestatus)									
	 *	4: Seat Wind -> 1 (from player seatwind + 1) 							
	 *	5: Kongs -> 1				
	 *	6: Concealed -> 1																																			
	 *	7: All Seq -> 1														
	 *	8: All Pon -> 3														
	 *	9: Mix Suit -> 3 														
	 *	10: All Terms/Honors -> 2 (5 - 3; from pung game)		
	 *	11: 7 Pairs -> 3 (4 - 1; from concealed)				
	 *	12: Full Suit -> 6 		
	 *	-------------------------------------
	 *	13: All Terminals -> 5 (10 - 2 - 3; all term/honors, from pung game)	
	 *	14: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)		
	 *	15: 4 little winds -> 6 												
	 *	16: 4 big winds ->10 (13 - 3; from pung game)										
	 *	17: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed) 	
	 *	18: 4 quads -> 10 (13 - 3; from pung game
	 *	19: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)		
	 *	20: Nine gates -> 9 (10 - 1; from concealed)
	 *
	 * @param called_groups The list of called_groups from a Player opponent
	 * @return a HashMap<Integer, Double> that has doubles as percentage chance of the Player having that score
	 * 		   double ranges from [0,1.0] where 1.0 == 100% has that point, 0.0 == 0% has that point
	 */
	public ArrayList<Double> score_percentage(ArrayList<Group> called_groups)
	{
		ArrayList<Double> return_percent_score = new ArrayList<Double>();
		Set<Integer> suit_set = new HashSet<Integer>();
		for(int i = 0; i <= 20; i++) return_percent_score.add(0.0);
		if(called_groups.size() == 0) {return return_percent_score;}
		//Default if call groups has elements, hand cannot be concealed
		return_percent_score.set(6, -1.0);
		return_percent_score.set(17, -1.0);
		return_percent_score.set(19, -1.0);
		return_percent_score.set(20, -1.0);
		for(int i = 0; i < called_groups.size(); i++)
		{
			int[] group_info = called_groups.get(i).getGroupInfo();
			switch(group_info[0])
			{
				case 0: //not a complete group
					return new ArrayList<Double>();
				case 1:	//sequence
					if(return_percent_score.get(7) == -1)
					{
						return_percent_score.set(7, return_percent_score.get(7) + 0.25);
					}
					return_percent_score.set(8, -1.0);
					return_percent_score.set(18, -1.0);
					break;
				case 3: //quad
					return_percent_score.set(5, 1.0);
					return_percent_score.set(18, return_percent_score.get(8) + 0.25);
				case 2:	//triplet
					return_percent_score.set(7, -1.0);
					if(return_percent_score.get(8) == -1)
					{
						return_percent_score.set(8, return_percent_score.get(8) + 0.25);
					}
					break;
			}
			switch(group_info[1])
			{
				case -1:
					return new ArrayList<Double>();
				case 0:
				case 1:
				case 2:
					suit_set.add(group_info[1]);
					break;
				case 3:
					if(called_groups.get(i).get_groupTiles().get(0) == )
					
			}
		}
	}
}

package bot_package;

import java.util.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import bot_package.Prediction.Score_probability;

/**
 * This class is an extension to the Prediction class, where algorithmic judgments are made of the opponent
 * The problem with the Prediction class is it assumes concealed hand, no called groups are incorporated to the probability
 * This class will extend those results and give a variety of information formatted in a sequential map.
 */
public class Tile_map 
{
	final private static Path base_defense_score_path_ = Paths.get("src" + File.separator + "bot_package" + File.separator + "Call_progress_score.txt");
	
	/**
	 * Integer id:
	 * 10th digit == amount of calls
	 * 1st  digit == layer
	 * 
	 * Score:
	 * [0.0,1.0]
	 * 0.0 == 1% confidence ready
	 * 1.0 == 99-100& confidence ready
	 */
	private static HashMap<Integer, Double> call_progress_map_ = new HashMap<Integer, Double>();
	
	/**
	 * Sorted ArrayList of the keys for call_progress_map_ that represents amount of calls and layer
	 */
	private static ArrayList<Integer> key_list = Group.sortArray(new ArrayList<Integer>(call_progress_map_.keySet()));
	
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
	protected double[] hand_probabilities_;
	
	/**
	 * Runs all the opportunities function in this class and returns it in a integer list, index follows [0,6]
	 * 0: dragon
	 * 1: wind
	 * 2: extended wind
	 * 3: quad
	 * 4: group_type
	 * 5: group_suit
	 * 6: term/honor
	 */
	protected int[] called_opportunities;
	
	public Tile_map(Prediction player_judgements, int opponent, MJ_round this_round)
	{
		this.current_round_ = this_round;
		this.wind_id_ = opponent;
		this.current_predictions_ = player_judgements;
		init_call_progress_map(this.current_round_.game_mode_);
	}
	
	public int[] opponent_score_chance(ArrayList<Integer> tile_market)
	{
		Score_probability x = new Score_probability(this.current_round_.get_all_Players().get(this.wind_id_).get_PlayerHand().get_declaredGroups(),
													tile_market, this.wind_id_, this.current_round_.prevalent_wind_);
		return x.init_score_opportunities();
	}
	
	public ArrayList<Double> danger_map(ArrayList<Double> drop_pile)
	{
		ArrayList<Integer> discard_type = Prediction.tile_to_discardType(drop_pile);
		/*
		 * 4.0 == only valid yaku tile
		 * 3.0 == very likely
		 * 2.0 == more likely
		 * 1.0 == unknown
		 * 0.0 == less likely
		 * -1.0 == no chance
		 */
		ArrayList<Double> talking_chances = new ArrayList<Double>();
		/*
		 * 0 == early
		 * 1 == mid
		 * 2 == full
		 */
		int size_hand = drop_pile.size()/6;
		if(size_hand > 2) {size_hand = 2;}
		switch(this.current_round_.game_mode_)
		{
			case 0:	//Riichi Mahjong
				int layer = 0;
				for(int tile_i = 0; tile_i < drop_pile.size(); tile_i++)
				{
					layer = tile_i/6;
					if(discard_type.get(tile_i) == 1)
					{
						switch(layer)
						{
							case 0:
								break;
							case 1:
								break;
							default:
								break;
						}
					}
				}
				break;
			case 1:	//Canto Mahjong
				break;
		}
		return new ArrayList<Double>();
	}
	
	/**
	 * 
	 * @param drop_pile the whole drop_pile ArrayList of this Player
	 * @param ready_index where the Player is believed to be ready at this index discard
	 * @param hand_prob A 3 index array that will show the probability of the current drop pile being played a certain way
	 * @param score_opportunities given called groups, what kind of score the Player is more likely going for
	 * @return ranging from [-1,1] where 1 == 100% confidence sakigiri, -1 == 0% confidence
	 */
	public double check_sakigiri(ArrayList<Double> drop_pile, int ready_index, double[] hand_prob)
	{
		if(drop_pile.size() == 0) {return -1;}
		//Makes a binary list of tedashi tsumogiri list
		ArrayList<Integer> discard_type = Prediction.tile_to_discardType(drop_pile);
		//index of which hand is most likely according to the algorithm
		int likely_hand = 0;
		for(int i = 0; i < hand_prob.length; i++){if(hand_prob[likely_hand] < hand_prob[i]){likely_hand = i;}}
		//Last tedashi tile before ready if applicable
		int tedashi_index = ready_index;
		if(discard_type.get(ready_index) == 0)
		{
			for(int i = tedashi_index; i >= 0; i--)
			{
				if(discard_type.get(i) == 1)
				{
					tedashi_index = i;
					break;
				}
			}
		}
		
		
		return 0.0;
	}
	
	/**
	 * 
	 * @param drop_pile
	 * @param declared_Groups
	 * @return
	 */
	public double progress_score(ArrayList<Double> drop_pile, ArrayList<Group> declared_Groups) 
	{
		int layer = drop_pile.size()/6;
		if(layer > 3) {layer = 3;}
		int calls = declared_Groups.size();
		return call_progress_map_.get(calls * 10 + layer);
	}
	
	
	
	public static void init_call_progress_map(int game_mode)
	{
		try (BufferedReader reader = Files.newBufferedReader(base_defense_score_path_)) 
        {
            String line;
            int list_score_n_right_gm = 0;
            int calls;
            int layer;
            double score = -1.0;
            while ((line = reader.readLine()) != null) 
            {
            	line = line.strip();
            	if(line.length() == 0) {continue;}
            	if(line.substring(0,2).compareTo("gm") == 0)
            	{
            		if(Character.getNumericValue(line.charAt(2)) == game_mode){list_score_n_right_gm++;}
            	}
                if(line.compareTo("&%") == 0){list_score_n_right_gm++; continue;}
                else if(line.compareTo("|%") == 0){list_score_n_right_gm = 0; continue;}
                if(list_score_n_right_gm == 2)
                {
                	try
                	{
                		calls = Character.getNumericValue(line.charAt(0));
                		layer = 0;
                		String score_str = "";
                		for(int i = 1; i < line.length(); i++)
                		{
                			if(line.charAt(i) == 's')
                			{
                				line = line.substring(i + 1).strip();
                				score_str = "";
                				i = 0;
                			}
                			if(line.charAt(i) == ';')
                			{
                				score = Double.parseDouble(score_str);
                				call_progress_map_.put(calls * 10 + layer, score);
                				score_str = "";
                				layer++;
                			}
                			else
                			{
                				score_str += line.charAt(i);
                			}
                		}
                		if(layer != 4)
                		{
                			for(int i = layer; i < 4; i++)
                				call_progress_map_.put(calls * 10 + i, 1.0);
                		}
                	}
                	catch(Exception e)
                	{
                		calls = 0;
                		call_progress_map_ = new HashMap<Integer, Double>();
                	}
                }
            }
        } 
		catch (IOException e) 
		{
            e.printStackTrace();
        }
	}
	
	public static void main(String[] args)
	{
		
	}
}

package bot_package;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.file.*;
import java.util.function.*;

/**
 * Needed for bot prediction:
 * Compress_input-> Will be responsible for:
 * 		- Discard reading (tile_ID + used_tiles)
 * 			> Tile market					(updates -> MJ_game possible tiles -> tile market)
 * 			> Player Progress score 		(less impactful)
 * 			> Player prediction score		(What score the Player could be aiming for)
 * 			> Possible overflow tiles		(*optional*)
 * 		- Decision (typically calls)
 * 			> Tedashi/Tsumogiri				(Depends on the Player's prediction scores)
 * 			> Calls/Declares				(Progresses the Player further, update Progress Score)
 * 			> Ron							(Literally ends the game who cares)
 *		- MJ_game
 *			> Tiles remain					(Indicates need for speed for current Player)
 * 
 * Prediction format:
 * String:
 * 
 * Each seatWind prediction starts "wind_ID" + ":"
 * The Progress score of the Player in that wind_ID
 * The Score Prediction, what the Player could be going for
 * *optional* a Danger zone matrix to indicate most likely tile the Player has (*warning* length == 34)
 * 
 * 0:		1:		2:		3:
 */

public class Prediction
{
	/**
	 * In retrospect to the current MJ_game's whole drop pile, this will keep track of what are
	 * tiles are non-visual, but that doesn't mean it's available (can be in other Players' hand)
	 */
	ArrayList<ArrayList<Integer>> tile_market_;
	
	/**
	 * @warning Need to fix in future when exporting as application, path will not remain
	 */
	final private static Path Player_behav_file = Paths.get(Paths.get("").toAbsolutePath().normalize() + 
														   "src" + File.separator + 
														   "bot_package" + File.separator +
														   "Player_behaviors.txt");
	/**
	 * 
	 */
	protected static HashMap<Integer, String> ID_2_Player = new HashMap<Integer, String>();
	
	
	/**
	 * A dummy Player used to predict what a Player has, not an actual Player instance
	 */
	static class predict_Player
	{
		/**
		 * A HashMap that represents the algorithm guess on the wind_ID corresponding to the Player's hand
		 * Integer = the placement of the guess (lower == more confident)
		 * String = mjSTR to represent the hand
		 */
		protected HashMap<Integer,String> mjSTR_prediction_;
		
		/**
		 * Integer ranges from [0,10] inclusive
		 * 0 = starting hand
		 * 1-4 = early progression
		 * 5 = 2 confirmed groups / 3-2 shanten
		 * 6-8 = 3 confirmed groups / 2-1 shanten
		 * 9 = 4 confirmed groups / extreme confidence 1-shanten
		 * 10 = completed hand
		 */
		protected int progress_score_ = 0;
		
		/**
		 * Saves this Player's game as a Unique ID incase this instance of Player plays again
		 */
		public int player_IDs_;
		
		/**
		 * Given the end of the game, if the Player does reveal their hand, with reflection of their drop pile
		 */
		public ArrayList<Integer> unique_behav_;
	
		
		public predict_Player(Player opponenet)
		{
			this.player_IDs_ = opponenet.seatWind_;
			
		}
		
		public static boolean save_Player_behav(Player player, predict_Player save_behav)
		{
			try
			{
				String save_state = "";
				Files.write(Prediction.Player_behav_file, Arrays.asList(save_state), StandardOpenOption.APPEND);
				return true;
			}
			catch(IOException e)
			{
				return false;
			}
		}
		
		/**
		 * 
		 * @param end_hand
		 * @param drop_pile
		 * @return
		 */
		public static ArrayList<String> retrace_history(String end_hand, ArrayList<Double> drop_pile)
		{
			
		}
		
		/**
		 * 
		 * @param drop_pile
		 * @return
		 */
		public static ArrayList<ArrayList<Double>> split_droppile_layer(ArrayList<Double> drop_pile)
		{
			/*
			 * 	How importance is scored:
			 * 	Tile type:		honor = +0, terminal = +1, simple = +2
			 * 	Discard type:	tsumogiri = +0, tedashi = +4
			 * 	layer:			1st = +0, 2nd = +2, 3rd/last = +4
			 * 	Weights can be adjusted later
			 */
			ArrayList<Integer> importance_list = new ArrayList<Integer>();
			int[] pivot_points = {-1, -1};
			DoubleToIntFunction condition = (num) -> 
			{
				int score = 0;
				int play_val = Group.tileID_to_PlayVal((int)num);
				
				//tile type
				if(play_val == 1 || play_val == 9){score++;}
				else{score+=2;}
				
				//checks tsumogiri or tedashi
				if(num - (int)num == 0.5)
				{
					score+=4;
				}
				
				//checks layer
				for(int i = 0; i < 2; i++)
				{
					if(pivot_points[i] != -1)
					{
						score += 2 * (i+1);
						break;
					}
				}
				return score;
			};
			/*
			 *	Normal hand case:
			 *	Mixed with tsumogiri of useless tiles, only suited tiles should be considered 
			 *	discard order = honors -> terminals/isolate tiles -> overflow tiles
			 */
			for(int i = 0; i < drop_pile.size(); i++)
			{
				/*
				 * Checks first non honor and if 1st pivot point is not set
				 */
				if(drop_pile.get(i) < 26.5 && pivot_points[0] == -1)
				{
					pivot_points[0] = i;
				}
				// Adds importance of current tile to ArrayList
				importance_list.add(condition.applyAsInt((drop_pile.get(i))));
			}
			/*
			 * 	Half/ Full flush hand case:
			 * 	Extremely obvious after two suits are discarded frequently
			 * 	discard order = nonsuit 1/2 <-> nonsuit 2/1 -> honors -> flush suit
			 */
			
			/*
			 * 	Kokushi hand case:
			 * 	Extremely obvious to see when nonorphans are only discards
			 * 	discard order = simples -> orphans
			 */
			
		}
	}
	public static void main(String[] args)
	{
		Path path = Paths.get(System.getProperty("user.dir") + "\\src\\bot_package\\Player_behaviors.txt");
		System.out.println(path.getFileName());
		System.out.println(Paths.get("").toAbsolutePath().normalize());
		System.out.println(System.getProperty("user.dir") + "\\src\\bot_package\\Player_behaviors.txt");
		try
		{
			List<String> Player_behav_list = Files.readAllLines(path);
			for(String Player_behav: Player_behav_list) System.out.println(Player_behav);
		}
		catch(IOException e)
		{
			System.out.println("Exception catched");
		}
	}
}

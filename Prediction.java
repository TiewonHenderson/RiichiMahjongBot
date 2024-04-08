package bot_package;

import java.util.*;
import java.io.File;
import java.io.IOException;
import java.nio.*;
import java.nio.file.*;

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
	
	public static boolean update_ID_2_Player(predict_Player new_prediction)
	{
		
	}
	
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
	
		
		public predict_Player(int wind_ID)
		{
			
		}
		
		public static boolean save_Player_behav(Player player, predict_Player save_behav)
		{
			try
			{
				String save_state = "";
				
				Files.write(Prediction.Player_behav_file, Arrays.asList(save_state), StandardOpenOption.APPEND);
				
			}
			catch(IOException e)
			{
				return false;
			}
		}
		
		public static 
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

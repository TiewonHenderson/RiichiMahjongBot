package bot_package;

import java.util.*;
import bot_package.Compress_input;
import bot_package.Compress_input.Console_io;

/**
 * Game_system doesn't 
 */
public class MJ_game
{
	/**
	 * This ArrayList will keep track of all Players of this current MJ game
	 */
	private ArrayList<Player> all_players = new ArrayList<Player>();
	
	/**
	 * The user interface for the bot algorithm
	 * @todo Future UI implementation
	 */
	private Panel_Manager in_out_system_;
	
	/**
	 * A beta console input output stream used for extremely compressed commands
	 */
	private Compress_input.Console_io console_io_stream;
	
	/**
	 * This ArrayList stores all the tiles that were visibly dropped, this includes if it was called and used
	 */
	public ArrayList<Double> all_drop_tiles_ = new ArrayList<Double>();
	
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public ArrayList<Integer> possible_tiles_ = new ArrayList<Integer>();
	
	/**
	 * 0 == riichi mahjong
	 * 1 == canto mahjong
	 */
	public int game_mode_;
	
	/**
	 * The int value that represents the remaining amount of tiles that does include other Player's hand
	 * When starting a game, this doesn't take into consideration 1st tile from dealer, 
	 * first tile will come as first Compress_input
	 * 
	 * Should not include flower tiles/ or any kind of bonus tile
	 */
	public int total_wall_tiles_;
	
	/*
	 * With each index that would represent the Player's wind_ID, the int value would indicate the amount
	 * of remaining called Group possible (4 == no declared Group)
	 */
	public int[] player_status_ = new int[4];

	/**
	 * This stores the move history of a given game
	 */
	public Compress_input move_history_;
	
	/**
	 * This will be the counter responsible for whose turn it currently is
	 */
	public int wind_ID_turn_ = 0;
	
	/**
	 * game_status_ declares what the game current is progressing as, int values include:
	 * 0 = new game
	 * 1 = mid game
	 * 2 = complete game
	 * 3 = bugged game
	 */
	public int game_status_;
	
	/**
	 * The prev_wind of the current game
	 */
	public int prevalent_wind_;
	
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * 
	 * @param wind_ID This input is the current Player that would input their hand while everyone else is unknown
	 * 
	 * This default constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public MJ_game(int game_mode)
	{
		String[] info = Prepare_MJ_game.get_all_info(this.console_io_stream);
		this.wind_ID_turn_ = 0;
		this.game_status_ = 0;
		this.prevalent_wind_ =  Prepare_MJ_game.wind_2_int(info[1])[0];
		this.game_mode_ = game_mode;
		//Index 0 == hand, index 1 == winds
		
		for(int i = 0; i < 4; i++)
		{
			if(i != Prepare_MJ_game.wind_2_int(info[1])[1])
			{
				this.all_players.add(new Player(i));
			}
			else
			{
				this.all_players.add(new Player(i, "USER", info[0], this));
			}
		}
	}
	
	/**
	 * Mid game constructor, Information lost includes:
	 * 		Opponent progress score 	(Some Loss)
	 * 		Opponent decisions			(Complete loss)
	 * 		Opponent discard list		(Complete loss)
	 * 
	 * Mid-game String syntax: (indents not included)
	 * order: 
	 * [0] 		upper char initial for wind
	 * [1,2] 	amount of tiles in hand
	 * after g == groups, if empty == no groups, g must remain despite no groups, syntax order == group + suit_char
	 * after f == flowers, if empty == no flowers, f must remain despite no flowers
	 * 
	 * E07g123m9999mf13
	 * S13gf44
	 * W01g4444m678s678sxxxxxf23
	 * N10g888pf 
	 * 
	 * concealed kong = xxxxx if applicable
	 * 
	 * @param Player_str_list A 4 element list with the combine String with the same format as String example above
	 * @param droptile_list A ArrayList of tile_IDs that represents all the drop tiles by Players, does not include called Groups
	 */
	public MJ_game(String[] Player_str_list, ArrayList<Integer> droptile_list)
	{
		
	}
	
	/**
	 * Complete game constructor, Information lost depends on what is inputed
	 * Complete-game String syntax: (indents not included)
	 * order: 
	 * 'wind_char' = upper char initial for wind
	 * after h = concealed hand
	 * after g = declared Groups
	 * after f = flowers
	 * w = woo/ron, t = tsumo, l = dealt_in/paying, n = non_payers 
	 * 
	 * after l/t = dealt_in/tsumo tile as play_val, suit
	 * 
	 * E1666678mg123m9999xmf13w
	 * S19m19p1239s12345zgf44l1m
	 * W7zg4444m678s678s2222zf23n
	 * N1112345678pg888pfn
	 * 
	 * concealed kongs are revealed, but before suit indicator add 'x'
	 * 
	 */
	
	/**
	 * Accessor method to get all non-visible tiles
	 * @return Every tile in a suit by suit ArrayList<Integer> format that are not visible
	 */
	public ArrayList<Integer> get_possible_tiles()
	{
		return this.possible_tiles_;
	}
	
	/**
	 * Mutator method for the whole non-visible tiles sequential ArrayList
	 * @param new_possible_tiles A whole new non-visible tiles sequential ArrayList where index represents the tile_id
	 */
	public void set_possible_tiles(ArrayList<Integer> new_possible_tiles)
	{
		this.possible_tiles_ = new ArrayList<Integer>(new_possible_tiles);
	}
	/**
	 * Accessor method to get amount of tiles left in the wall itself
	 * @return The total number of tiles left from THE WALL, not total visible tiles
	 */
	public int total_tiles_left()
	{
		return this.total_wall_tiles_;
	}
	
	/**
	 * Mutator method to update amount of tiles in the wall
	 * @param new_amt The updated amount of tiles in the wall itself
	 */
	public void set_tiles_left(int new_amt)
	{
		this.total_wall_tiles_ = new_amt;
	}
	
	/**
	 * Accessor method to get all Players within a ArrayList
	 * @return An ArrayList of Players where each index would be the Player's respective wind_ID
	 */
	public ArrayList<Player> get_all_Players()
	{
		return this.all_players;
	}
	
	/**
	 * Accessor method to get a list of 4 Players and the amount of calls each Player has made
	 * @return Returns a list of 4 elements representing each Player and the amount of calls they have
	 */
	public int[] get_status()
	{
		return this.player_status_;
	}
	
	/**
	 * Mutator method to set a certain index that would represent a Player to an updated status
	 * @param player_index The player (referred by their wind_ID) that wants their call amount to be changed
	 * @param new_status The new call amount to be assigned to this certain Player
	 */
	public void set_status(int player_index, int new_status)
	{
		this.player_status_[player_index] = new_status; 
	}
	
	/**
	 * Accessor method to get current turn for a certain wind_ID
	 * @return The current wind_id that is allowed to go (in other words, the integer that represent which Player's turn)
	 */
	public int get_current_windID_turn()
	{
		return this.wind_ID_turn_;
	}
	
	/**
	 * Mutator method to set new wind_ID
	 * @param new_windID The new wind_ID desired for this current turn
	 */
	public void set_windID_turn(int new_windID)
	{
		this.wind_ID_turn_ = new_windID;
	}
	
	public void set_game_mode(int game_mode)
	{
		this.game_mode_ = game_mode;
	}
	
	/**
	 * 0 = new game
	 * 1 = mid game
	 * 2 = complete game
	 * 3 = bug game
	 * 
	 * Accessor method to get the current game status
	 * @return An integer that represents the status of the game
	 */
	public int game_status()
	{
		return this.game_status_;
	}
	
	/**
	 * 
	 * @param player_id The wind_id that corresponds to the Player wanting their calls
	 * @return a ArrayList<Group> that represents the VISIBLE calls they have depending on the game_mode_
	 */
	public ArrayList<Group> get_validPlayerCalls(int player_id)
	{
		ArrayList<Group> all_calls = new ArrayList<Group>(this.all_players.get(player_id).getPlayerCalls());
		switch(this.game_mode_)
		{
			case 1:
				for(int i = 0; i < all_calls.size(); i++)
				{
					if(all_calls.get(i).concealed_)
					{
						all_calls.remove(i);
						i--;
					}
				}
		}
		return all_calls;
	}
 	
	public static class Prepare_MJ_game
	{
		/**
		 * Prioritized indicators
		 * "m", "p", "s", "z" to add respective tile_id for the group/tiles
		 * ^^^ Refer to Group.suit_reference
		 *  "c", "k", "q", and "o" to determine which ArrayList to add into
		 *  
		 * @param io_stream the IO stream responsible for Scanner inputs
		 * @return a mj_str that is translatable using Player.convert_mjSTR()
		 */
		public static String get_hand_str(Console_io io_stream)
		{
			return io_stream.console_hand_input();
		}
		
		/**
		 * 
		 * @param io_stream the IO stream responsible for Scanner inputs
		 * @return A two length String that represents index 0 == prevalent wind, index 1 == seat wind
		 */
		public static String get_wind_str(Console_io io_stream)
		{
			return io_stream.console_wind_input();
		}
		
		/**
		 * 
		 * @param io_stream the IO stream responsible for Scanner inputs
		 * @return A list of all the minimal info needed to start a typically MJ game
		 */
		public static String[] get_all_info(Console_io io_stream)
		{
			String[] ret_list = {get_hand_str(io_stream), get_wind_str(io_stream)};
			return ret_list;
		}
		
		/**
		 * 
		 * @param wind_input A two length String that represents index 0 == prevalent wind, index 1 == seat wind
		 * @return a 2 indexed list where the wind symbols are translated into their wind_id
		 */
		public static int[] wind_2_int(String wind_input)
		{
			int[] return_list = new int[wind_input.length()];
			for(int i = 0; i < wind_input.length(); i++)
			{
				for(int j = 0; j < Group.wind_reference.length; j++)
				{
					if(wind_input.charAt(i) == Group.wind_reference[j])
					{
						return_list[i] = j;
					}
				}
			}
			return return_list;
		}
	}
}

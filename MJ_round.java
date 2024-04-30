package bot_package;

import java.util.*;
import bot_package.Compress_input;
import bot_package.Compress_input.Command;
import bot_package.Compress_input.Console_io;
import bot_package.Group.Hidden_Kan;
import bot_package.Player.PlayerHand;

/**
 * Game_system doesn't 
 */
public class MJ_round
{
	/**
	 * This ArrayList will keep track of all Players of this current MJ game
	 */
	private ArrayList<Player> all_players = new ArrayList<Player>();
	
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
	 * Only applies to this.game_mode_ == 0 for Riichi mahjong games
	 * returns empty String for any other cases
	 */
	public ArrayList<Integer> dora_indicators_ = new ArrayList<Integer>();
	
	/**
	 * This is a universal integer that only applies to riichi mahjong, there are ONLY two cases to use this
	 * Called / Added Kan: 
	 * drop_turn_b4_add_dora = 1 
	 * 
	 * Concealed Kan:
	 * drop_turn_b4_add_dora = 0 
	 * 
	 * After every turn, check if drop_turn_b4_add_dora == 0,
	 * if drop_turn_b4_add_dora > 0, decrement
	 * if drop_turn_b4_add_dora == 0, run MJ_round_get_info.get_instant_dora_indicator()
	 */
	public static int drop_turn_b4_add_dora = -1;
	
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
	 * The wind id of the User
	 */
	public int user_wind_;
	
	/**
	 * This would represent which wind_id player is the winner
	 */
	public int winner_wind_id = -1;
	
	/**
	 * Default constructor
	 */
	public MJ_round()
	{
		this.wind_ID_turn_ = 0;
		this.game_status_ = 0;
		this.prevalent_wind_ = 0;
		this.user_wind_ = 0;
		this.game_mode_ = 0;
	}
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * 
	 * @param game_mode the int id that represents which mahjong variant is being played
	 * 
	 * This starting constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public MJ_round(int game_mode)
	{
		String[] info = MJ_round_get_info.get_all_info(this.console_io_stream);
		switch(game_mode)
		{
			case 0:
				//total - dead_wall - nondealer hands - dealer hand
				this.total_wall_tiles_ = 136 - 14 - (3 * 13) - 14; 
				break;
			case 1:
				this.total_wall_tiles_ = 136 - (3 * 13) - 14; 
				break;
		}
		this.wind_ID_turn_ = 0;
		this.game_status_ = 0;
		this.prevalent_wind_ = MJ_round_get_info.wind_2_int(info[1])[0];
		this.user_wind_ = MJ_round_get_info.wind_2_int(info[1])[1];
		this.game_mode_ = game_mode;
		//Index 0 == hand, index 1 == winds
		
		for(int i = 0; i < 4; i++)
		{
			if(i != MJ_round_get_info.wind_2_int(info[1])[1])
			{
				this.all_players.add(new Player(i, this.game_mode_));
			}
			else
			{
				this.all_players.add(new Player(i, "USER", info[0], this.game_mode_));
			}
		}
		this.init_game();
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
	public MJ_round(String[] Player_str_list, ArrayList<Integer> droptile_list)
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
	public int get_total_tiles_left()
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
	 * 
	 * @return true if total number of tiles was decremented, false if no tiles to decrement
	 */
	public boolean decrement_tiles_left()
	{
		if(this.total_wall_tiles_ > 0)
		{
			this.total_wall_tiles_--;
			return true;
		}
		return false;
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
	
	public boolean add_dora_indicator(int tile_id)
	{
		if(this.game_mode_ == 0 && this.dora_indicators_.size() < 5)
		{
			this.dora_indicators_.add(tile_id);
			return true;
		}
		return false;
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
	
	public int take_queue_cmds(Queue<Command> all_commands)
	{
		Command prev_cmd = null;
		try
		{
			while(!all_commands.isEmpty())
			{
				Command current_cmd = all_commands.poll();
				double tile_val = current_cmd.tile_id_;
				switch(current_cmd.input_)
				{
					case CONCEALED_QUAD:
						if(current_cmd.tile_id_ != -1)//tile visible in riichi mahjong
						{
							this.get_all_Players().get(this.user_wind_).update_tile_map(current_cmd.tile_id_, 0);
							ArrayList<Integer> group_list = new ArrayList<Integer>();
							for(int i = 0; i < 4; i++) group_list.add(current_cmd.tile_id_);
							this.all_players.get(current_cmd.player_wind_id_).get_PlayerHand().add_declared_group(new Group(group_list, true, true));
						}
						else//tild_id == -1 if game_mode_ == 1
						{
							this.get_all_Players().get(current_cmd.player_wind_id_).get_PlayerHand().add_declared_group(new Hidden_Kan(current_cmd.player_wind_id_, -1));
						}
						this.set_tiles_left(this.get_total_tiles_left() - 4);
						break;
					case CALLED_PREVIOUS:
						ArrayList<Integer> group_list = new ArrayList<Integer>();
						int prev_tile_id = -1;
						if(prev_cmd == null)
						{
							prev_tile_id = this.all_drop_tiles_.get(this.all_drop_tiles_.size() - 1).intValue();
						}
						else
						{
							prev_tile_id = prev_cmd.tile_id_;
						}
						//previous drop would had already remove same index tile_visibility by 1, 
						switch(current_cmd.prev_call_type_)
						{
							case CHI:
								if(current_cmd.chi_dif_ == 0)
								{
									group_list.add(prev_tile_id - 1);
									group_list.add(prev_tile_id + 1);
								}
								else
								{
									group_list.add(prev_tile_id + (current_cmd.chi_dif_ * 2));
									group_list.add(prev_tile_id + current_cmd.chi_dif_);
								}
								for(Integer visible_tile: group_list) 
									this.get_all_Players().get(this.user_wind_).decrement_map_index(visible_tile);
								group_list.add(prev_tile_id);
								this.get_all_Players().get(current_cmd.player_wind_id_).get_PlayerHand().add_declared_group(new Group(group_list, true, false));
								this.wind_ID_turn_++;
								break;
							case CALL_KAN:
								this.get_all_Players().get(this.user_wind_).decrement_map_index(prev_tile_id);
								group_list.add(prev_tile_id);
								if(this.game_mode_ == 0)
								{
									drop_turn_b4_add_dora = 1;
								}
							case PON:
								
								for(int i = 0; i < 2; i++)
								{
									this.get_all_Players().get(this.user_wind_).decrement_map_index(prev_tile_id);
									group_list.add(prev_tile_id);
								}
								group_list.add(prev_tile_id);
								this.get_all_Players().get(current_cmd.player_wind_id_).get_PlayerHand().add_declared_group(new Group(group_list, true, false));
								this.wind_ID_turn_ = current_cmd.player_wind_id_;
								break;
							case RON:
								this.winner_wind_id = 
								this.game_status_ = 2;
								all_commands = new LinkedList<Command>();
								HashMap<Integer, String> revealing_hands = MJ_round_get_info.get_end_hands(this.console_io_stream);
								for(int key: revealing_hands.keySet())
								{
									if(key == this.user_wind_)
									{
										continue;
									}
									else
									{
										this.get_all_Players().get(key).set_PlayerHand(new PlayerHand(revealing_hands.get(key)));
									}
								}
								return 2;
						}
						break;
					case ADDED_QUAD:
						this.all_drop_tiles_.add(tile_val);
						this.decrement_tiles_left();
						this.get_all_Players().get(current_cmd.player_wind_id_).get_PlayerHand().update_added_quad(current_cmd.tile_id_);
						this.get_all_Players().get(this.user_wind_).decrement_map_index(current_cmd.tile_id_);
						break;
					case DROP:
						if(current_cmd.tedashi_)
						{
							tile_val += 0.5;
						}
						if(current_cmd.riichi_)
						{
							tile_val += 0.0001;
						}
						if(current_cmd.red_5_)
						{
							tile_val += 0.05;
						}
						this.all_drop_tiles_.add(tile_val);
						this.decrement_tiles_left();
						this.get_all_Players().get(this.user_wind_).decrement_map_index(current_cmd.tile_id_);
				}
				if(drop_turn_b4_add_dora == 0)
				{
					this.add_dora_indicator(MJ_round_get_info.get_instant_dora_indicator(this.console_io_stream));
					drop_turn_b4_add_dora = -1;
				}
				else if(drop_turn_b4_add_dora > 0) {drop_turn_b4_add_dora--;}
				prev_cmd = new Command(current_cmd);
			}
		}
		catch(Exception e) {return 3;}
		return 1;
	}
	
	public int init_game() 
	{
		while(this.game_status_ < 2)
		{
			this.game_status_ = this.take_queue_cmds(Console_io.inputed_move(this.console_io_stream.console_hand_input(), this));
			switch(this.game_status_)
			{
				case 1:		//Mid game
					
					break;
				case 2: 	//Complete game
					break;
				case 3:		//Bugged game
					break;
			}
		}
		return this.game_status_;
	}
	
	public ArrayList<Group> get_validPlayerCalls(int player_id)
	{
		ArrayList<Group> all_calls = new ArrayList<Group>(this.all_players.get(player_id).getPlayerCalls());
		switch(this.game_mode_)
		{
			case 1:
				for(int i = 0; i < all_calls.size(); i++)
				{
					if(all_calls.get(i).concealed_ || Group.group_status(all_calls.get(i)) < 1)
					{
						all_calls.remove(i);
						i--;
					}
				}
		}
		return all_calls;
	}
	
	public static class MJ_round_get_info
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
		 * @info only applies to Riichi mahjong games
		 * @param io_stream the IO stream responsible for Scanner inputs
		 * @return A two length String that represents index 0 == prevalent wind, index 1 == seat wind
		 */
		public static int get_instant_dora_indicator(Console_io io_stream)
		{
			String user_input = "";
			while(true)
			{
				System.out.println("Input new dora_indicator: ");
				user_input = io_stream.console_universal_ret_str();
				try
				{
					int input_num = Integer.parseInt(user_input);
					if(input_num > -1 && input_num < 34)
					{
						return input_num;
					}
				}
				catch(Exception e) {}
			}
		}
		
		/**
		 * @info the input should be in MJ_str format where visible calls are separate
		 * 		 input format:
		 * 		 seat_wind_char + MJ_str
		 * 		 This means the hands can be inputed in any order so long as the other 3 unique winds are added
		 * @param io_stream the IO stream responsible for Scanner inputs
		 * @return A HashMap where integer represent the wind_id of the corresponding inputed char with the MJ_str
		 */
		public static HashMap<Integer,String> get_end_hands(Console_io io_stream)
		{
			HashMap<Integer, String> return_map = new HashMap<Integer, String>();
			String needed_indicators = "ckq";
			String user_input = "";
			while(return_map.keySet().size() < 3)
			{
				System.out.println("Hand1: ");
				user_input = io_stream.console_universal_ret_str();
				if(Character.isAlphabetic(user_input.charAt(0)) && user_input.charAt(user_input.length()) == 'o')
				{
					for(int i = 0; i < user_input.length(); i++)
					{
						if(needed_indicators.length() > 0 && user_input.charAt(i) == needed_indicators.charAt(0))
						{
							needed_indicators = needed_indicators.substring(1);
						}
					}
					if(needed_indicators.isEmpty())
					{
						for(int i = 0; i < Group.wind_reference.length; i++)
						{
							if(user_input.charAt(0) == Group.wind_reference[i])
							{
								if(!return_map.keySet().contains(i))
								{
									return_map.put(i, user_input.substring(1));
								}
							}
						}
					}
				}
			}
			return return_map;
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
		
		public static Queue<Command> get_input_cmd(Console_io io_stream, MJ_round current_round)
		{
			System.out.print("in: ");
			return Console_io.inputed_move(io_stream.console_universal_ret_str(), current_round);
		}
	}
}

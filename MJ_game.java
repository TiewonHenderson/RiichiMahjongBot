package bot_package;

import java.util.*;


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
	 * This is the prototype input output of how information is to be in/out-puted.
	 * input:
	 * 		Compress_input
	 * 			- Player moves
	 * 		Info_input
	 * 			- Opponent predictions, 	range: [0,3] inclusive
	 * 			- Top progress scores,		range: [0,20] inclusive
	 * Output:
	 * 		from Compress_input
	 * 			- boolean => represents move was added
	 * 		from Info_input
	 * 			- Opponent progress score, predicted scores
	 * 			- Your progress scores for each potential yaku
	 * 			
	 */
	private Prototype_UI in_out_system_;
	
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public ArrayList<ArrayList<Integer>> possible_tiles_ = new ArrayList<ArrayList<Integer>>();
	
	/*
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
	
	/*
	 * The list of possible flowers that is either in other Player's hand or in the wall
	 * Each index represents the amount of flowers not visible
	 */
	public ArrayList<Integer> possible_flowers_; 

	/**
	 * This stores the move history of a given game
	 */
	public Stack<Compress_input> move_history_;
	
	/**
	 * This will be the counter responsible for whose turn it currently is
	 */
	public int wind_ID_turn_ = 0;
	
	/**
	 * Anything that goes wrong, or the game actually finishes, this will indicates these two cases
	 */
	public boolean complete_game = false;
	
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * 
	 * @param wind_ID This input is the current Player that would input their hand while everyone else is unknown
	 * 
	 * This default constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public MJ_game()
	{
		this.in_out_system_ = new Prototype_UI(this);							// sets in_out_system_
		
		for(int i = 0; i < 4; i++) 
		{
			this.possible_tiles_.add(new ArrayList<Integer>());					// sets possible_tiles_
			int max = 9;
			switch(i) //Used to add numbered suit and honor suit separately
			{
				case 3:
					max = 7;
				default:
					for(int j = 0; j < max; j++) this.possible_tiles_.get(i).add(4);
					break;
			}
			this.all_players.add(new Player(i, "", this));						// sets all_players_
			if(i == this.in_out_system_.get_windID())
			{
				// sets custom name from Prototype UI to User wind_ID
				this.all_players.get(i).set_Player_name(this.in_out_system_.get_username());
			}
		}
		this.total_wall_tiles_ = 136 - (4 * 13);								// sets total_wall_tiles (thus without Player tiles)
		for(int i = 0; i < 4; i++) 
		{
			this.player_status_[i] = 0;											// sets player_status_
			this.possible_flowers_.add(2);										// sets possible_flowers_
		}
		
		this.move_history_ = new Stack<Compress_input>();						// sets move_history_
		
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
		for(int i = 0; i < Player_str_list.length; i++)
		{
			int wind_ID = element_translator.windchar_to_int(Player_str_list[i].charAt(0));
			all_players.add(new Player(wind_ID, "", this));
			
		}
		this.in_out_system_ = new Prototype_UI(this);						// sets in_out_system_
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
	public MJ_game(String[] Player_str_list, ArrayList<Integer> droptile_list)
	{
		
	}
	
	/**
	 * Accessor method to get all non-visible tiles
	 * @return Every tile in a suit by suit ArrayList<Integer> format that are not visible
	 */
	public ArrayList<ArrayList<Integer>> get_possible_tiles()
	{
		return this.possible_tiles_;
	}
	
	/**
	 * Mutator method for the whole non-visible tiles 2D ArrayList
	 * @param new_possible_tiles A whole new 2D ArrayList of indexes with ratios equivalent to the [suit][play_val]
	 */
	public void set_possible_tiles(ArrayList<ArrayList<Integer>> new_possible_tiles)
	{
		this.possible_tiles_ = new ArrayList<ArrayList<Integer>>(new_possible_tiles);
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
	 * Accessor method to get an ArrayList of the possible flowers, 0 == all of that flowers are used
	 * @return Returns a list of 4 elements representing the amount of flowers left for the index representation
	 * 			of that seasoned flower, i.e flower 1 == index 0; flower 4 == index 3
	 */
	public ArrayList<Integer> get_possible_flowers()
	{
		return this.possible_flowers_;
	}
	
	/**
	 * Mutator method to change 1 element in possible_flowers_
	 * @param flower_index The flower index that wants to be changed
	 * @param new_amt The new amount of flower for a certain flower number (index = flower num - 1)
	 */
	public void set_possible_flower(int flower_index, int new_amt)
	{
		this.possible_flowers_.set(flower_index, new_amt);
	}
	
	/**
	 * Accessor method to get move_history_
	 * @return Return a ArrayList of all the previous moves that occured during this MJ_game
	 */
	public Stack<Compress_input> get_move_history()
	{
		return this.move_history_;
	}
	
	/**
	 * Removes the top of the move stack, thus the previously added Compress_input
	 */
	public void remove_previous_move()
	{
		if(this.move_history_.size() > 0)
		{
			this.move_history_.pop();
		}
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
	/**
	 * Accessor method to get the current game status
	 * @return A boolean that represents if the game is complete (true) or not (false)
	 */
	public boolean game_status()
	{
		return this.complete_game;
	}
	
	/**
	 * 
	 * @param new_turn The new turn that was just perform in order to update MJ_game in real time
	 * @return	True if the input was valid and was added properly, false otherwise and was not added
	 */
 	public boolean add_move(Compress_input new_turn)
	{
 		if(!new_turn.is_validMove()) {return false;}
 		
 		//Adds the valid turn into move_history
 		this.move_history_.push(new_turn);
 		
		//Removes input tile from wall
		int suit = new_turn.get_tileID() / 9;
		int index = new_turn.get_tileID() % 9;
		
		//No tiles to offer, no tiles remove, return false
		if(this.possible_tiles_.get(suit).get(index) == 0)
		{
			return false;
		}
		this.possible_tiles_.get(suit).set(index, this.possible_tiles_.get(suit).get(index) - 1);
		
		//Checks the same conditions for used_tiles
		for(int i = 0; i < new_turn.get_used_tiles().size(); i++)
		{
			suit = new_turn.get_used_tiles().get(i) / 9;
			index = new_turn.get_used_tiles().get(i) % 9;
			if(this.possible_tiles_.get(suit).get(index) == 0)
			{
				return false;
			}
			this.possible_tiles_.get(suit).set(index, this.possible_tiles_.get(suit).get(index) - 1);
		}
		
		// valid Move already checks if there are possible flowers, since true, it's safe to remove flowers
		for(int i = 0; i < new_turn.get_flower_list().size(); i++)
		{
			this.possible_flowers_.set(new_turn.get_flower_list().get(i), this.possible_flowers_.get(new_turn.get_flower_list().get(i)) - 1);
		}
		
		
		switch(new_turn.get_decision())
		{
			//All increment by 1 cases ([0,1] == drop + [2] == seq call)
			case 0:
			case 1:
			case 2:
				
				if(this.wind_ID_turn_ == 3)
				{
					this.wind_ID_turn_ = 0;
				}
				else
				{
					this.wind_ID_turn_++;
				}
				break;
				
			//All any but current Player cases ([3,4] = pon/kan calls)
			case 3:
			case 4:
				this.wind_ID_turn_ = new_turn.get_windID();
				break;
				
			//ron/tsumo is ALL Players
			case 7:
				//Get Player index -> Scoring.java
				this.complete_game = true;
				break;
		}
		return true;
	}
 	
 	public String loop_next_input() 
 	{
 		
 	}
 	
 	static class element_translator
 	{
 		/**
 		 * 
 		 * @param wind_char The wind_char that wants to be represented as int wind_ID
 		 * @return the int value corresponding to a value char of wind_char ('e,s,w,n','0,1,2,3') otherwise return -1
 		 */
 		public static int windchar_to_int(char wind_char)
 		{
 			if(Character.isAlphabetic(wind_char))
 			{
 				switch(Character.toLowerCase(wind_char))
 				{
 					case 'e':
 						return 0;
 					case 's':
 						return 1;
 					case 'w':
 						return 2;
 					case 'n':
 						return 3;
 				}
 			}
 			else if(Character.isDigit(wind_char))
 			{
 				int input_num = Character.getNumericValue(wind_char);
 				if(input_num > 3)
 				{
 					return 3;
 				}
 				else if(input_num < 0)
 				{
 					return 0;
 				}
 				return input_num;
 			}
 			return -1;
 		}
 	}
}

package bot_package;

import java.util.*;
import bot_package.MJ_game.MJ_round;

public class Player
{
	/*
	 * These enum represent the Player and is used in a variety of scoring, turns, and other
	 */
	enum wind{EAST, SOUTH, WEST, NORTH}
	
	/*
	 * Player name, can add custom names in the future, for now is reference to their seat wind
	 */
	public String playerName_;
	
	/*
	 * E = 0
	 * S = 1
	 * W = 2
	 * N = 3
	 */
	public int seatWind_;

    /**
     * An ArrayList that represents the drop tile history of this instance of Player
     * The reasoning behind Double and not Integer is 0.0 == tsumogiri, 0.5 == tedashi, 0.25 == tsumogiri riichi, 0.75 == tedashi riichi
     */
	public ArrayList<Double> dropPile_;
	
	/**
	 * All the called Groups for this Player, it becomes public with exception to enclosed kan
	 */
	public ArrayList<Group> called_groups_;
	
	/**
	 * Set this Player's opponent_Player true if the Player is not the user
	 */
	public boolean opponent_Player_ = true;
	
	/**
	 * The amount of tiles non-visible, so if there is a tile visible to this Player, that tile_id will decrement, 
	 * even in their personal hand, decrement
	 */
	private ArrayList<Integer> tile_market_;
	
	/*
	 * The current Playerhand of this player instance, has closed and open tiles in 2D ArrayList
	 */
	private PlayerHand playerHand_;
	
	/**
	 * Default constructor responsible for creating a random Player mainly for testing
	 */
	public Player()
	{
		this.seatWind_ = -1;
		this.dropPile_ = new ArrayList<Double>();
		this.playerHand_ = new PlayerHand();
	}
	
	/**
	 * Simple constructor for the purpose of creating an opponent Player
	 * @param wind_id the Player's wind_id of this instance of Player
	 * @param game_mode the int id that represents which mahjong variant is being played
	 */
	public Player(int wind_id, int game_mode)
	{
		this.playerName_ = wind.values()[wind_id] + " PLAYER";
		this.seatWind_ = wind_id;
		this.opponent_Player_ = true;
		this.dropPile_ = new ArrayList<Double>();
		this.playerHand_ = new HiddenHand(game_mode);
		this.tile_market_ = new ArrayList<Integer>();
	}
	
	
	/**
	 * Complex constructor for the purpose of loading a new Player of the User
	 * @param wind_id 		the wind_id of this Player
	 * @param Name	  		the alotted name of this current Player
	 * @param mj_STR  		the mj_STR where the User's hand is inputed String format to start a MJ_round
	 * @param game_mode the int id that represents which mahjong variant is being played
	 */
	public Player(int wind_id, String Name, String mj_STR, int game_mode)
	{
		this.playerName_ = Name;
		this.seatWind_ = wind_id;
		this.opponent_Player_ = false;
		this.playerHand_ = new PlayerHand(mj_STR);
		this.dropPile_ = new ArrayList<Double>();
	}
	
	public Player(int wind_id, String specific_name, MJ_round input_game)
	{
		//Invalid input for wind_ID would just result in default constructor
		if(wind_id < 0 || wind_id > 3)
		{
			this.seatWind_ = -1;
			this.dropPile_ = new ArrayList<Double>();
			this.playerHand_ = new PlayerHand();
		}
		else
		{
			//If empty String provided, put default name
			if(specific_name.length() == 0){this.playerName_ = wind.values()[wind_id] + " PLAYER";}
			else {this.playerName_ = specific_name;}
			
			this.seatWind_ = wind_id;
		}
		//Default initialize
		this.dropPile_ = new ArrayList<Double>();
		this.playerHand_ = new PlayerHand();
	}
	/*
	 * Used to create a copy of inputed Player
	 */
	public Player(Player clone)
	{
		this.playerName_ = new String(clone.playerName_);
		this.seatWind_ = clone.seatWind_;
		this.dropPile_ = new ArrayList<Double>(clone.dropPile_);
		this.playerHand_ = new PlayerHand(clone.playerHand_);
	}
	
	/*
	 * Mutator method to set a new name for this instance of Player
	 */
	public void set_Player_name(String new_name)
	{
		this.playerName_ = new_name;
	}
	
	/**
	 * Mutator method to set a new wind_ID of this instance of Player
	 * @param new_windID
	 */
	public void set_seatwind(int new_windID)
	{
		this.seatWind_ = new_windID;
	}
	
	/**
	 * Mutator method by adding the inputed tile at the end of this instance of Player drop_pile List
	 * @param tile_ID The new tile that was dropped by this instance of Player
	 */
	public void add_droptile(double tile_ID)
	{
		this.dropPile_.add(tile_ID);
	}
	
	/**
	 * Additional Mutator method mainly for non-new games to completely override old droppile
	 * @param new_droppile The new list of drop pile of this instance of Player
	 */
	public void set_drop_pile(ArrayList<Double> new_droppile)
	{
		this.dropPile_ = new_droppile;
	}
	
	public void set_user_opponent(boolean value)
	{
		this.opponent_Player_ = value;
	}
	
	/**
	 * 
	 * @return The PlayerHand object that has information of this Player's hand
	 */
	public PlayerHand get_PlayerHand()
	{	
		return this.playerHand_;
	}
	
	/**
	 * 
	 * @param in_playhand The new hand to set onto this instance of Player
	 * @return True if the mutator method was successful, false otherwise
	 */
	public boolean set_PlayerHand(PlayerHand in_playhand)
	{
		try
		{
			this.playerHand_ = new PlayerHand(in_playhand);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	/**
	 * 
	 * @return The ArrayList of tiles that would represent the drop pile of the Player, will not
	 *		   delete tiles that were called on
	 */
	public ArrayList<Double> get_drop_pile()
	{
		return this.dropPile_;
	}
	
	/**
	 * @Info this ArrayList<Group> needs to be passed through MJ_round in order to
	 * have correct Groups formatted
	 * 
	 * @return The ArrayList<Group> that are called and not within the hand
	 */
	public ArrayList<Group> getPlayerCalls()
	{
		return this.called_groups_;
	}
	
	public boolean update_tile_map(int tile_id, int new_amt)
	{
		if(this.tile_market_.get(tile_id) == 0)
		{
			return false;
		}
		this.tile_market_.set(tile_id, this.tile_market_.get(tile_id) - 1);
		return true;
	}
	
	public boolean decrement_map_index(int tile_id)
	{
		return this.update_tile_map(tile_id, this.tile_market_.get(tile_id) - 1);
	}
	
	public int get_tile_map_id(int tile_id)
	{
		return this.tile_market_.get(tile_id);
	}
	
	/**
	 *
	 * Prioritized indicators
	 * "m", "p", "s", "z" to add respective tile_id for the group/tiles
	 * ^^^ Refer to Group.suit_reference
     *  "c", "k", "q", and "o" to determine which ArrayList to add into
     *  
	 * @param mjSTR The string that represents a player's mahjong Hand as a String value
	 * @return A PlayerHand object that contains two ArrayList, the concealed hand and declared groups
	 */
	public static PlayerHand convert_mjSTR(String mjSTR)
	{
		ArrayList<Integer> close_segment = new ArrayList<Integer>();
		ArrayList<Group> declared_segment = new ArrayList<Group>();
		
		
		int suit = 3; //Used to track the suit_index
		
		/*
		 * add_mode == 3 -> add as open declared groups
		 * add_mode == 2 -> add as open declared kongs/kans
		 * add_mode == 1 -> add as concealed declared kongs/kans
		 * add_mode == 0 -> add as concealed in hand tiles
		 */
		int add_mode = 3;
		
		for(int i = mjSTR.length() - 1; i >= 0; i--)
		{
			char current_char = mjSTR.charAt(i);
			
			//Used to compare suits, the suit_index corresponding will add to translating to tile_id
			for(int suit_index = 0; suit_index < Group.suit_reference.length; suit_index++)
			{
				if(current_char == Group.suit_reference[suit_index])
				{
					suit = suit_index;
					continue;
				}
					
			}
			// 'c' is prioritized to make sure the tiles are added correctly
			if(current_char == 'c')
			{
				add_mode = 0;
				continue;
			}
			// 'k' is for concealed declared quads
			else if(current_char == 'k')
			{
				add_mode = 1;
				continue;
			}
			// 'q' is for called quads
			else if(current_char == 'q')
			{
				add_mode = 2;
				continue;
			}
			
			if(Character.isDigit(current_char)) //Checks if the character is a number
			{
				int index = i; //Need to decrement as starting index was at STR.length - 1
				ArrayList<Integer> temp_tiles = new ArrayList<Integer>();
				switch(add_mode)
				{
					case 3: //Add to open groups ArrayList
						//General check (only complex sequences can pass)
						while(Character.isDigit(mjSTR.charAt(index)) && temp_tiles.size() < 3)
						{
							temp_tiles.add(Character.getNumericValue(mjSTR.charAt(index)) + (suit * 9) - 1);
							index--;
						}
						if(Group.group_status(temp_tiles) >= 1) //Re-confirms the tiles added is a group
						{
							declared_segment.add(new Group(temp_tiles, true, false));
							i = index + 1;
							break;
						}
						//Checks complex sequence, temp_tile now becomes temp mini hand (with the context of containing completed groups)
						while(Character.isDigit(mjSTR.charAt(index)))
						{
							temp_tiles.add(Character.getNumericValue(mjSTR.charAt(index)) + (suit * 9) - 1);
							index--;
						}
						//Checks through SortedSet until the required groups is reached
						
						int group_counter = 0;
						int needed_groups = temp_tiles.size()/3;
						while(group_counter < needed_groups)
						{
							ArrayList<Integer> index_added = new ArrayList<Integer>();
							SortedSet<Integer> search_list = new TreeSet<Integer>();
							for(int temp_item = 0; temp_item < temp_tiles.size(); temp_item++)
							{
								if(search_list.size() == 3) //Breaks once group.size == 3 to confirm it is a group
								{
									break;
								}
								if(!search_list.contains(temp_tiles.get(temp_item)))
								{
									search_list.add(temp_tiles.get(temp_item));
									index_added.add(temp_item);
								}
							}
							ArrayList<Integer> substitute_temp = new ArrayList<Integer>(search_list);
							if(Group.group_status(substitute_temp) >= 1) //Re-confirms the tiles added is a group
							{
								for(int remove_i = index_added.size() - 1; remove_i >= 0; remove_i--)
								{
									//When removing bigger index to lower, shifted index won't matter
									temp_tiles.remove(index_added.get(remove_i));
								}
								declared_segment.add(new Group(substitute_temp, true, false));
								group_counter++;
							}
						}
						i = index + 1;
						break;
					case 2: //Add to open groups and set concealed to false and is quad, no break to fall into case 1
					case 1: //Add to open groups but set to concealed and is quad
						boolean isconcealed = true;
						if(add_mode == 2) {isconcealed = false;}
						while(Character.isDigit(mjSTR.charAt(index)) && temp_tiles.size() != 4)
						{
							temp_tiles.add(Character.getNumericValue(mjSTR.charAt(index)) + (suit * 9) - 1);
							index--;
						}
						if(Group.group_status(temp_tiles) == 3)
						{
							declared_segment.add(new Group(temp_tiles, true, isconcealed));
						}
						i = index + 1;
						break;
					case 0: //Add to concealed hand ArrayList
						close_segment.add(Character.getNumericValue(mjSTR.charAt(i)) + (suit * 9) - 1);
						break;
				}
			}
		}
		return new PlayerHand(Group.sortArray(close_segment), declared_segment, mjSTR);
	}
	
	/**
	 * Overload function of convert_PlayerHand to be able to accept the PlayerHand object itself
	 * or accept the 2 important ArrayList
	 * @param playerhand The PlayerHand object that wants to be represented as a mjSTR string
	 * @return A mjSTR as a String to represent the "PlayerHand" being inputed
	 */
	public static String convert_PlayerHand(PlayerHand playerhand)
	{
		return convert_PlayerHand(playerhand.get_current_hand(), playerhand.get_declaredGroups());
	}
	
	/**
	 * 
	 * @param concealed_hand The concealed non declared part of the entire PlayerHand
	 * @param declared_groups The typically open (with exception to closed quads) declared groups
	 * @return A mjSTR as a String to represent the "PlayerHand" being inputed
	 */
	public static String convert_PlayerHand(ArrayList<Integer> concealed_hand, ArrayList<Group> declared_groups)
	{
		ArrayList<ArrayList<Integer>> close_suitedlist = new ArrayList<ArrayList<Integer>>(Group.suitDivide(concealed_hand));
		ArrayList<Group> declared_list = new ArrayList<Group>();
		String ret_string = "o";
		
		if(concealed_hand.size() == 0) //A concealed hand that is empty cannot exist
		{
			return "";
		}
		
		/*
		 * The best Attempt to set declared_list in reverse order of suits (honors, sous, pins, mans)
		 */
		if(declared_groups.size() > 1)
		{
			/*
			 * group_index = ArrayList of original suit for each index in DeclaredGroup ArrayList
			 * sorted_groupindex = The suits are sorted, but now the index for groups are mixed up
			 * 
			 * Will loop through in order sorted_groupindex to see if group_index has that suit
			 * adds to declared_list and removed in group_index
			 */
			ArrayList<Integer> group_index = new ArrayList<Integer>();
			ArrayList<Integer> sorted_groupindex;
			for(Group declared_group: declared_groups) if(declared_group.get_Group_info()[1] != -1) {group_index.add(declared_group.get_Group_info()[1]);}
			sorted_groupindex = Group.sortArray(group_index);
			
			if(group_index.size() != sorted_groupindex.size())
			{
				return "Invalid_hand (Error: Called groups contained invalid tiles)";
			}
			
			for(int i = sorted_groupindex.size() - 1; i >= 0; i--) //Only sorted list matter for adding groups in reverse
			{
				for(int k = 0; k < group_index.size(); k++)
				{
					if(sorted_groupindex.get(i) == group_index.get(k))
					{
						declared_list.add(declared_groups.get(k));
						group_index.set(k, -1); //ensures the same suit is not kept
					}
				}
			}
		}
		else
		{
			//If size() = 1, it will add, if size() == 0, the for loop wont run
			for(int i = 0; i < declared_groups.size(); i++) declared_list.add(declared_groups.get(i));
		}
		
		/*
		 * Groups are sorted in reverse, so String end starts at beginning
		 * The return String will also be in reverse, and is mirrored at the end
		 */
		for(int rev_groupi = 0; rev_groupi < declared_list.size(); rev_groupi++)
		{
			Group group = declared_list.get(rev_groupi);
			ArrayList<Integer> temp_list = group.get_groupTiles();
			if(group.get_Group_info()[1] == -1 || group.get_groupTiles().size() < 3) //Don't add invalid/incomplete groups
			{
				declared_list.remove(rev_groupi); //Removed invalid groups
				rev_groupi--;
				continue;
			}
			if(group.get_Group_info()[0] == 3) //Dont add any declared quads, add in future
			{continue;}
			
			//(String cast) -> (char[] suit_list) -> (declared_group suit index in getGroupInfo())
			ret_string += Character.toString(Group.suit_reference[group.get_Group_info()[1]]);
			
			for(int i = temp_list.size() - 1; i >= 0; i--)
			{
				ret_string += Integer.toString(Group.tileID_to_PlayVal(temp_list.get(i)));
			}
			declared_list.remove(rev_groupi); //Removed means successfully added, this will allow only concealed quads to remain
			rev_groupi--;
		}
		//where declared quads are added
		ret_string += "q";
		for(int remain_groupi = 0; remain_groupi < declared_list.size(); remain_groupi++) //Assumes all invalid groups are removed above
		{
			Group group = declared_list.get(remain_groupi);
			if(group.concealed_) //concealed quads are added separately
			{
				continue;
			}
			ArrayList<Integer> temp_list = group.get_groupTiles();
			ret_string += Character.toString(Group.suit_reference[group.get_Group_info()[1]]);
			
			for(int i = temp_list.size() - 1; i >= 0; i--)
			{
				ret_string += Integer.toString(Group.tileID_to_PlayVal(temp_list.get(i)));
			}
			declared_list.remove(remain_groupi); //Removed means successfully added, this will allow only concealed quads to remain
			remain_groupi--;
		}
		
		//Where concealed declared quads are added
		ret_string += "k";
		
		for(int remain_groupi = 0; remain_groupi < declared_list.size(); remain_groupi++) //Assumes all invalid groups are removed above
		{
			Group group = declared_list.get(remain_groupi);
			ArrayList<Integer> temp_list = group.get_groupTiles();
			ret_string += Character.toString(Group.suit_reference[group.get_Group_info()[1]]);
			
			for(int i = temp_list.size() - 1; i >= 0; i--)
			{
				ret_string += Integer.toString(Group.tileID_to_PlayVal(temp_list.get(i)));
			}
			declared_list.remove(remain_groupi); //Removed means successfully added, this will allow only concealed quads to remain
			remain_groupi--;
		}
		
		//Where the concealed in hand tiles will be
		ret_string += "c";
		
		for(int suit_i = close_suitedlist.size() - 1; suit_i >= 0; suit_i--)
		{
			if(close_suitedlist.get(suit_i).size() > 0)
			{
				ret_string += Character.toString(Group.suit_reference[suit_i]); //Add suit String from char[] suit_reference from Group
				ArrayList<Integer> temp_sort = close_suitedlist.get(suit_i); //Sort tiles within
				for(int tile_i = temp_sort.size() - 1; tile_i >= 0; tile_i--)
				{
					// (String cast) -> (Play_val conversion) -> (Tile_ID) -> (reverse increment of temp_sort)
					ret_string += Integer.toString(Group.tileID_to_PlayVal(temp_sort.get(tile_i)));
				}
			}
		}
		
		String filp_STR = "";
		for(int i = ret_string.length() - 1; i >= 0; i--) filp_STR += Character.toString(ret_string.charAt(i));
		return filp_STR;
	}
	
	/*
	 * Local class
	 * Used to store currentHand of assigned player
	 * String representation of current player hand
	 */
	protected static class PlayerHand
	{
		
		/*
		 * The declaredGroups of this PlayerHand, what PlayerHand represents is not just
		 * the concealed part of what the Player is playing on, but the Entire hand
		 */
		public ArrayList<Group> declaredGroups_; 
		
		/*
		 * The ArrayList<Integer> representation of the PlayerHand
		 * Integer values should only range from [0,33]
		 */
		private ArrayList<Integer> currentHand_;
		
		/*
		 * The String representation of PlayerHand
		 * A use for this String could be Logging, Display, Saved as starting hand indicator
		 */
		private String mjSTRhand_; 
		
		/*
		 * An ArrayList to indicate what tiles can be called via index
		 * 1 => sequence can be called
		 * 2 => triplet can be called
		 * 3 => sequence and triplet can be called
		 * 4 => Quad (and automatically triplet) can be called
		 * 5 => Everything call be called
		 */
		private ArrayList<Integer> call_map_;
		
		
		/**
		 * Default constructor
		 */
		public PlayerHand()
		{
			this.declaredGroups_ = new ArrayList<Group>();
			this.currentHand_ = new ArrayList<Integer>();
			this.mjSTRhand_ = "";
		}
		
		/**
		 * Used to clone a Player's Hand
		 * @param clone A PlayerHand wanted to be copy to another instance
		 */
		public PlayerHand(PlayerHand clone)
		{
			this.declaredGroups_ = new ArrayList<Group>(clone.declaredGroups_);
			this.currentHand_ = new ArrayList<Integer>(clone.currentHand_);
			this.mjSTRhand_ = new String(clone.mjSTRhand_);
			this.call_map_ = new ArrayList<Integer>(clone.call_map_);
		}
		/**
		 * A concealed hand only PlayerHand Constructor
		 * @param in_hand The only part of the ENTIRE player's hand and is complete concealed
		 */
		public PlayerHand(ArrayList<Integer> in_hand)
		{
			this.declaredGroups_ = new ArrayList<Group>();
			this.currentHand_ = new ArrayList<Integer>(in_hand);
			this.mjSTRhand_ = convert_PlayerHand(in_hand, this.declaredGroups_);
		}
		
		/**
		 * A custom hand inputed PlayerHand Constructor
		 * @param in_hand The current concealed hand of the desired PlayerHand
		 * @param declared_groups The current Declared Groups of the desired PlayerHand, can include concealed declared quads
		 */
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups)
		{
			this.declaredGroups_ = new ArrayList<Group>(declared_groups);
			this.currentHand_ = new ArrayList<Integer>(in_hand);
			this.mjSTRhand_ = convert_PlayerHand(in_hand, declared_groups);
		}
		
		/**
		 * A custom hand inputed PlayerHand Constructor
		 * @param in_hand The current concealed hand of the desired PlayerHand
		 * @param declared_groups The current Declared Groups of the desired PlayerHand, can include concealed declared quads
		 * @param mjSTR The mjSTR of the PlayerHand, this constructor is typically used to save resources
		 */
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups, String mjSTR)
		{
			this.currentHand_ = new ArrayList<Integer>(in_hand);
			this.declaredGroups_ = new ArrayList<Group>(declared_groups);
			this.mjSTRhand_ = mjSTR;
		}
		
		/**
		 * STRING CONSTRUCTOR
		 * @param in_STRformat Input as mjSTR, will be translated to concealed hand and declared groups
		 */
		public PlayerHand(String in_STRformat)
		{
			PlayerHand temp_obj = convert_mjSTR(in_STRformat);
			
			this.currentHand_ = temp_obj.get_current_hand();
			this.declaredGroups_ = temp_obj.get_declaredGroups();
			this.mjSTRhand_ = new String(in_STRformat);
		}
		
		/*
		 * Returns the current ArrayList<Integer> of the Player's inside_hand
		 */
		public ArrayList<Integer> get_current_hand()
		{
			return this.currentHand_;
		}
		
		/*
		 * Returns the current ArrayList<Group> that are visible to other players
		 * This however does include the concealed declared quads that would consider the hand
		 * still be concealed
		 */
		public ArrayList<Group> get_declaredGroups()
		{
			return this.declaredGroups_;
		}
		
		/**
		 * If required, set the current Player inside_hand to a new hand
		 * @param in_newHand The new hand to be set to this instance
		 * @return True if the new inside hand has been set to the PlayerHand, false if error was raised
		 */
		public boolean set_current_hand(ArrayList<Integer> in_newHand)
		{
			try
			{
				this.currentHand_ = new ArrayList<Integer>(in_newHand);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * If required, 
		 * set the current Player's declared groups to a new ArrayList<Group> of declared Groups
		 * @param in_newGroups The new ArrayList<Group> for this PlayerHand instance
		 * @return True if the new_groups has been set, false if an error was raised
		 */
		public boolean set_declared_group(ArrayList<Group> in_newGroups)
		{
			try
			{
				this.declaredGroups_ = new ArrayList<Group>(in_newGroups);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * Used to add new declared groups to unique PlayerHand instance
		 * @param new_group SINGULAR group to be added to this instance of PlayerHand
		 * @return True if the new Group was added, false if an error was raised
		 */
		public boolean add_declared_group(Group new_group)
		{
			try
			{
				if(declaredGroups_.size() > 4) {return false;}
				this.declaredGroups_.add(new Group(new_group));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		public boolean update_added_quad(int fourth_tile)
		{
			for(int i = 0; i < this.declaredGroups_.size(); i++)
			{
				if(this.declaredGroups_.get(i).get_groupTiles().get(0) == fourth_tile)
				{
					return this.declaredGroups_.get(i).upgrade_pon();
				}
			}
			return false;
		}
		
		/**
		 * Only to print test, not for actual use
		 */
		public String toString()
		{
			String ret_string = "Concealed Hand: (";
			try
			{
				for(int i = 0; i < this.get_current_hand().size(); i++)
				{
					if(i == this.get_current_hand().size() - 1)
					{
						ret_string += this.get_current_hand().get(i) + ")\n";
						break;
					}
					ret_string += this.get_current_hand().get(i) + ", ";
				}
				
				ret_string += "Declared Groups: ";
				for(int i = 0; i < this.get_declaredGroups().size(); i++)
				{
					if(i == this.get_current_hand().size() - 1)
					{
						ret_string += this.get_declaredGroups().get(i) + ")\n";
						break;
					}
					ret_string += this.get_declaredGroups().get(i) + ", ";
				}
				ret_string += "\nmjSTR format: " + this.mjSTRhand_;
				return ret_string;
			}
			catch(Exception e) {return "A variable most likely has not been initialized";}
		}
		
		/**
		 * 
		 * @param in_groups The ArrayList of Groups that wants to be sorted into a 2D ArrayList depending on it's suit
		 * @return A 2D ArrayList that has groups depending on the suit index
		 */
		public static ArrayList<ArrayList<Group>> sort_suitedGroups(ArrayList<Group> in_groups)
		{
			ArrayList<ArrayList<Group>> return_ArrayList = new ArrayList<ArrayList<Group>>();
			for(int i = 0; i < 4; i++) return_ArrayList.add(new ArrayList<Group>());
			
			for(Group group: in_groups)
			{
				if(Group.group_status(group) != -3 && group.get_Group_info()[1] != -1) //To re-confirm the Group/suit is not invalid
				{
					//group.getGroupInfo()[1] == suit index
					return_ArrayList.get(group.get_Group_info()[1]).add(group);
				}
			}
			return return_ArrayList;
		}
	}
	public static class HiddenHand extends PlayerHand
	{
		
		/*
		 * The declaredGroups of this PlayerHand, what PlayerHand represents is not just
		 * the concealed part of what the Player is playing on, but the Entire hand
		 */
		public ArrayList<Group> declaredGroups_; 
		
		/*
		 * An overriding field of PlayerHand.currentHand_
		 * Since this represents a hidden hand, this ArrayList would only include -1 with size == amount of tiles
		 */
		public ArrayList<Integer> currentHand_;
		
		/**
		 * This is in reference to the MJ_round variant that is being played:
		 * Difference = 
		 * concealed kan 
		 */
		public int game_mode_;
		
		/**
		 * Starting opponent hand constructor
		 * @param game_mode the int id that represents which mahjong variant is being played
		 */
		public HiddenHand(int game_mode) 
		{
			this.currentHand_ = new ArrayList<Integer>(Collections.nCopies(13, -1));
			this.game_mode_ = game_mode;
		}

		/**
		 * 
		 * @param custom_tile_amt a custom amount of tiles in hand
		 * @param visible_groups  the visible groups that is presumes corresponds to the custom_tile_amt
		 * @param game_mode the int id that represents which mahjong variant is being played
		 */
		public HiddenHand(int custom_tile_amt, ArrayList<Group> visible_groups, int game_mode)
		{
			this.currentHand_ = new ArrayList<Integer>(Collections.nCopies(custom_tile_amt, -1));
			this.game_mode_ = game_mode;
			for(int i = 0; i < visible_groups.size(); i++)
			{
				if(!this.add_declared_group(visible_groups.get(i)))
				{
					this.set_declared_group(new ArrayList<Group>());
					this.currentHand_ = new ArrayList<Integer>(Collections.nCopies(13, -1));
					for(int j = 0; j < 13; j++) currentHand_.add(0);
					break;
				}
			}
		}
		
		public ArrayList<Integer> get_current_hand()
		{
			return this.currentHand_;
		}
		
		public int get_amt_tiles()
		{
			return this.currentHand_.size();
		}
		
		/*
		 * Returns the current ArrayList<Group> that are visible to other players
		 * This however does include the concealed declared quads that would consider the hand
		 * still be concealed
		 */
		public ArrayList<Group> get_declaredGroups()
		{
			return this.declaredGroups_;
		}
		
		public boolean set_current_hand(ArrayList<Integer> overridden_method)
		{
			try 
			{
				this.currentHand_ = new ArrayList<Integer>(Collections.nCopies(overridden_method.size(), -1));
				return true;
			}
			catch(Exception e) {return false;}
		}
		
		public boolean set_current_hand(int tile_amt)
		{
			try 
			{
				this.currentHand_ = new ArrayList<Integer>(Collections.nCopies(tile_amt, -1));
				return true;
			}
			catch(Exception e) {return false;}
		}
		
		
		/**
		 * If required, 
		 * set the current Player's declared groups to a new ArrayList<Group> of declared Groups
		 * @param in_newGroups The new ArrayList<Group> for this PlayerHand instance
		 * @return True if the new_groups has been set, false if an error was raised
		 */
		public boolean set_declared_group(ArrayList<Group> in_newGroups)
		{
			try
			{
				this.declaredGroups_ = new ArrayList<Group>(in_newGroups);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * Used to add new declared groups to unique PlayerHand instance
		 * @param new_group SINGULAR group to be added to this instance of PlayerHand
		 * @return True if the new Group was added, false if an error was raised
		 */
		public boolean add_declared_group(Group new_group)
		{
			if(!new_group.declared_)
			{
				return false;
			}
			if(Group.group_status(new_group) >= 2)
			{
				this.declaredGroups_.add(new_group);
				return true;
			}
			return false;
		}
		
		public boolean update_added_quad(int fourth_tile)
		{
			for(int i = 0; i < this.declaredGroups_.size(); i++)
			{
				if(this.declaredGroups_.get(i).get_groupTiles().get(0) == fourth_tile)
				{
					return this.declaredGroups_.get(i).upgrade_pon();
				}
			}
			return false;
		}
	}
	public static void main(String[] args)
	{
		ArrayList<Group> declared_groups = new ArrayList<Group>();
		for(int i = 0; i < 2; i++) 
		{
			Group temp_group = Test_class.random_group(true, false);
			System.out.println("Added Group: " + temp_group);
			declared_groups.add(temp_group);
			if(declared_groups.get(declared_groups.size() - 1).get_groupTiles().size() == 4)
			{
				declared_groups.get(declared_groups.size() - 1).set_conceal_status(false); //Makes all declared quads concealed
			}
			else
			{
				declared_groups.get(declared_groups.size() - 1).set_conceal_status(false); //Makes sure declared non quad groups are not concealed
			}
		}
		int[] add_tiles = {0,0,1,5,6,7,7,18,19,31};
		ArrayList<Integer> fake_hand = new ArrayList<Integer>();
		for(int i = 0; i < add_tiles.length; i++) fake_hand.add(add_tiles[i]);
		
		System.out.println(convert_PlayerHand(fake_hand, declared_groups));
		System.out.println(convert_mjSTR(convert_PlayerHand(fake_hand, declared_groups)));
		
		PlayerHand test_PHand = new PlayerHand(fake_hand, declared_groups);
		
		System.out.println(test_PHand.get_declaredGroups());
		System.out.println(test_PHand.get_current_hand());
		System.out.println("Refresh groupSN" + test_PHand.refresh_GroupSN());
		for(String key: test_PHand.get_currentGroups().keySet())
		{
			System.out.println(GroupSearch.groupSN_to_ArrayList(test_PHand.get_currentGroups().get(key)));
			for(Group group: GroupSearch.groupSN_to_ArrayList(test_PHand.get_currentGroups().get(key)))
			{
				System.out.println("Talking tiles: " + Group.get_talkingTiles(group));
			}
		}
	}
}
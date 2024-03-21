package bot_package;

import java.util.*;

public class Player
{
	/*
	 * These enum represent the Player and is used in a variety of scoring, turns, and other
	 */
	enum wind{EAST, SOUTH, WEST, NORTH}
	
	/*
	 * Used to keep track of amount of untitled players
	 * Doesn't need to be accessed outside of this class
	 */
	protected static int assign_wind = 0;
	
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

	/*
     * Integer to keep track of drawn flowers, will be of magnitude of 10^4
     * Use seatWind to determine what flower is the player's good flower (10^seatWind)
     */
    public int flower_;
    
	public ArrayList<Integer> dropPile_;
	
	/*
	 * The current Playerhand of this player instance, has closed and open tiles in 2D ArrayList
	 */
	private PlayerHand playerHand_;
	
	/*
	 * Used to created a new default player object, fields will be set as
	 * playerName = "UNNAMED" + unname_amt
	 * seatWind = seatWind_amt
	 * new drop pile and playerHand
	 */
	public Player()
	{
		this.playerName_ = wind.values()[assign_wind] + "PLAYER";
		this.seatWind_ = assign_wind;
		assign_wind += 1;
		
		if(assign_wind == 4)
		{
			assign_wind = 0;
		}
		this.dropPile_ = new ArrayList<Integer>();
		this.playerHand_ = new PlayerHand();
		this.flower_ = 0;
	}
	
	public Player(int wind_ID)
	{
		//Invalid input for wind_ID would just result in default constructor
		if(wind_ID < 0 || wind_ID > 3)
		{
			this.playerName_ = "UNNAMED" + Integer.toString(assign_wind);
			this.seatWind_ = assign_wind;
			assign_wind += 1;
			
			if(assign_wind > 3)
			{
				assign_wind = 0;
			}
		}
		else
		{
			// Assign name according to wind_ID
			switch(wind_ID)
			{
				case 0:
					this.playerName_ = "EAST Player";
					this.seatWind_ = 0;
					break;
				case 1:
					this.playerName_ = "SOUTH Player";
					this.seatWind_ = 1;
					break;
				case 2:
					this.playerName_ = "WEST Player";
					this.seatWind_ = 2;
					break;
				case 3:
					this.playerName_ = "NORTH Player";
					this.seatWind_ = 3;
					break;
			}
		}
		
		assign_wind = wind_ID + 1;
		if(wind_ID == 4)
		{
			assign_wind = 0;
		}
		this.dropPile_ = new ArrayList<Integer>();
		this.playerHand_ = new PlayerHand();
		this.flower_ = 0;
	}
	/*
	 * Used to create a copy of inputed Player
	 */
	public Player(Player clone)
	{
		this.playerName_ = new String(clone.playerName_);
		this.seatWind_ = clone.seatWind_;
		this.dropPile_ = new ArrayList<Integer>(clone.dropPile_);
		this.playerHand_ = new PlayerHand(clone.playerHand_);
		this.flower_ = clone.flower_;
	}
	
	/*
	 * Used to all details about this instance's hand, includes open/closed decalred groups
	 */
	public PlayerHand getPlayerHand()
	{	
		return this.playerHand_;
	}
	
	public boolean setPlayerHand(PlayerHand in_playhand)
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
	
	public ArrayList<Integer> getPlayerDrops()
	{
		return this.dropPile_;
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
		return convert_PlayerHand(playerhand.getCurrentHand(), playerhand.getDeclaredGroup());
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
			for(Group declared_group: declared_groups) if(declared_group.getGroupInfo()[1] != -1) {group_index.add(declared_group.getGroupInfo()[1]);}
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
			if(group.getGroupInfo()[1] == -1 || group.get_groupTiles().size() < 3) //Don't add invalid/incomplete groups
			{
				declared_list.remove(rev_groupi); //Removed invalid groups
				rev_groupi--;
				continue;
			}
			if(group.getGroupInfo()[0] == 3) //Dont add any declared quads, add in future
			{continue;}
			
			//(String cast) -> (char[] suit_list) -> (declared_group suit index in getGroupInfo())
			ret_string += Character.toString(Group.suit_reference[group.getGroupInfo()[1]]);
			
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
			ret_string += Character.toString(Group.suit_reference[group.getGroupInfo()[1]]);
			
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
			ret_string += Character.toString(Group.suit_reference[group.getGroupInfo()[1]]);
			
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
	static class PlayerHand
	{
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
		 * The declaredGroups of this PlayerHand, what PlayerHand represents is not just
		 * the concealed part of what the Player is playing on, but the Entire hand
		 */
		public ArrayList<Group> declaredGroups_; 
		
		/*
		 * 	update_Groups itself represents ArrayList<ArrayList<Group>> for each groupSN case, 
		 *	String Key will represent the groupID
		 *	String Assigned groupSN
		 * 	
		 * 
		 * 	Scoring reference:
		 * 	Completed Groups 	= 10 (Exponentially grows as more complete/called groups are formed) (peaks at 2)
		 * 	Incomp Groups 		= 4	 (Linearly grows as more complete/called groups are formed) (peaks at 2)
		 * 	Pair				= 5	 (remains the same) (peaks at 3)
		 * 	Floating Tile		= 1	 (remains the same) (peaks at 4)
		 * 
		 * 	What update_Groups prioritizes:
		 * 		ArrayList<Group> of only completed groups:
		 * 			prioritize only 2 completed groups over 3 Pairs despite lower score
		 * 	
		 * 		ArrayList<Group> of as many incomplete groups
		 * 			prioritize 2 incomplete groups over 2 pairs with complete groups <= 2
		 * 			prioritize 2 pairs over 2 incomplete groups with complete groups == 3
		 * 
		 * 		Pairs > floating Tiles
		 * 			Any situation
		 * 
		 * 		Empty ArrayList<Group>
		 * 			Only floating tile suits are better off empty if complete groups >= 2
		 * 			Untouched if 									 complete groups <= 1
		 * 
		 * 	*note* Japanese MJ terms:
		 * 		"Tsumogiri" = throwing the tile you just drew
		 * 		"Tedashi" 	= throwing the tile you have in your hand, accepting the tile drawn
		 * 
		 * 	Two Situations:
		 * 
		 * 		The Tile is kept into the hand == Tedashi?:
		 * 			Three(pseudo Four) nested Situations:
		 * 
		 * 				The Tile completes a group:
		 * 					
		 * 				The Tile builds a group:
		 * 
		 * 				The Tile harms a group:
		 * 
		 * 				Pseudo Fourth == Throw the same tile in your hand:
		 * 					
		 * 		The Tile is thrown out == Tsumogiri:
		 * 			update_Groups == update_Groups (no groups are altered)
		 */
		private HashMap<String, String> update_Groups_ = new HashMap<String, String>();
		
		
		/*
		 * This makes aware if new tiles were called/accepted into the hand, which could
		 * possibly change the hand's out-dated groups
		 */
		private boolean updated_searches_ = false;
		
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
			this.currentHand_ = new ArrayList<Integer>();
			this.declaredGroups_ = new ArrayList<Group>();
			this.mjSTRhand_ = "";
		}
		
		/**
		 * Used to clone a Player's Hand
		 * @param clone A PlayerHand wanted to be copy to another instance
		 */
		public PlayerHand(PlayerHand clone)
		{
			this.currentHand_ = new ArrayList<Integer>(clone.currentHand_);
			this.declaredGroups_ = new ArrayList<Group>(clone.declaredGroups_);
			this.mjSTRhand_ = new String(clone.mjSTRhand_);
			this.update_Groups_ = new HashMap<String, String>(clone.update_Groups_);
			this.updated_searches_ = clone.updated_searches_;
			this.call_map_ = new ArrayList<Integer>(clone.call_map_);
		}
		/**
		 * A concealed hand only PlayerHand Constructor
		 * @param in_hand The only part of the ENTIRE player's hand and is complete concealed
		 */
		public PlayerHand(ArrayList<Integer> in_hand)
		{
			this.currentHand_ = new ArrayList<Integer>(in_hand);
			this.declaredGroups_ = new ArrayList<Group>();
			this.mjSTRhand_ = convert_PlayerHand(in_hand, this.declaredGroups_);
		}
		
		/**
		 * A custom hand inputed PlayerHand Constructor
		 * @param in_hand The current concealed hand of the desired PlayerHand
		 * @param declared_groups The current Declared Groups of the desired PlayerHand, can include concealed declared quads
		 */
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups)
		{
			this.currentHand_ = new ArrayList<Integer>(in_hand);
			this.declaredGroups_ = new ArrayList<Group>(declared_groups);
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
			
			this.currentHand_ = temp_obj.getCurrentHand();
			this.declaredGroups_ = temp_obj.getDeclaredGroup();
			this.mjSTRhand_ = new String(in_STRformat);
		}
		
		/*
		 * Returns the current ArrayList<Integer> of the Player's inside_hand
		 */
		public ArrayList<Integer> getCurrentHand()
		{
			return this.currentHand_;
		}
		
		/*
		 * Returns the current ArrayList<Group> that are visible to other players
		 * This however does include the concealed declared quads that would consider the hand
		 * still be concealed
		 */
		public ArrayList<Group> getDeclaredGroup()
		{
			return this.declaredGroups_;
		}
		
		/**
		 * If required, set the current Player inside_hand to a new hand
		 * @param in_newHand The new hand to be set to this instance
		 * @return True if the new inside hand has been set to the PlayerHand, false if error was raised
		 */
		public boolean setCurrentHand(ArrayList<Integer> in_newHand)
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
		public boolean setDeclaredGroup(ArrayList<Group> in_newGroups)
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
		public boolean addDeclaredGroups(Group new_group)
		{
			try
			{
				this.declaredGroups_.add(new Group(new_group));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * 
		 * @return
		 */
		protected HashMap<String, String> get_currentGroups()
		{
			return this.update_Groups_;
		}
		
		/**
		 * Only to print test, not for actual use
		 */
		public String toString()
		{
			String ret_string = "Concealed Hand: (";
			try
			{
				for(int i = 0; i < this.getCurrentHand().size(); i++)
				{
					if(i == this.getCurrentHand().size() - 1)
					{
						ret_string += this.getCurrentHand().get(i) + ")\n";
						break;
					}
					ret_string += this.getCurrentHand().get(i) + ", ";
				}
				
				ret_string += "Declared Groups: ";
				for(int i = 0; i < this.getDeclaredGroup().size(); i++)
				{
					if(i == this.getCurrentHand().size() - 1)
					{
						ret_string += this.getDeclaredGroup().get(i) + ")\n";
						break;
					}
					ret_string += this.getDeclaredGroup().get(i) + ", ";
				}
				ret_string += "\nmjSTR format: " + this.mjSTRhand_;
				return ret_string;
			}
			catch(Exception e) {return "A variable most likely has not been initialized";}
		}
		
		/**
		 * *Note* this function does not take into consideration unique hands like 7 pairs nor 13 orphans
		 * 
		 * GroupSearch progress_score would not consider declared groups, This will add this specific PlayerHand declared groups
		 * Updates current this.update_Groups
		 * 
		 * *note* pairs are considered as incomplete groups
		 * 4110 == 4 groups, 1 pair, 1 incomp groups, 0 floating tiles == complete
		 * 
		 * 4001 == 4 groups, 0 pairs, 0 incomp groups, 1 floating tile == waiting
		 * 3220 == 3 groups, 2 pairs, 2 incomp groups, 0 floating tiles == waiting
		 * 3120 == 3 groups, 1 pair, 2 incomp groups, 0 floating tiles == waiting
		 * 
		 * 3112 == 3 groups, 1 pair, 1 incomp group (the pair itself), 2 floating tiles == incomplete
		 * etc == incomplete
		 * 
		 * 4000 == invalid
		 * @return A Integer numerical score of how far the hand is close to winning
		 */
		public int progress_score()
		{
			if(!this.updated_searches_)
			{
				this.update_Groups_ = GroupSearch.search_all_groupSN(this, false);
				this.updated_searches_ = true;
			}
			int best_score = 0;
			for(String key: this.update_Groups_.keySet())
			{
				int current_score = GroupSearch.progress_score(this.update_Groups_.get(key)) + (this.declaredGroups_.size() * 1000);
				if(current_score > best_score)
				{
					best_score = current_score;
				}
			}
			return best_score;
		}
		
		/**
		 * *Note* this function does not take into consideration unique hands like 7 pairs nor 13 orphans
		 * @return The ArrayList<Group> that has the highest score in terms of progression to a complete hand
		 */
		public ArrayList<Group> get_fastestGroups()
		{
			if(!this.updated_searches_)
			{
				this.update_Groups_ = GroupSearch.search_all_groupSN(this, false);
				this.updated_searches_ = true;
			}
			int best_score = 0;
			String best_key = "";
			for(String key: this.update_Groups_.keySet())
			{
				int current_score = GroupSearch.progress_score(this.update_Groups_.get(key)) + (this.declaredGroups_.size() * 1000);
				if(current_score > best_score)
				{
					best_score = current_score;
					best_key = key;
				}
			}
			return Group.groupSN_to_ArrayList(this.update_Groups_.get(best_key));
		}
		
		/**
		 * WIP
		 * 
		 * This function is used to fit the new inputed tile to the groups the PlayerHand currently has
		 * Without needing to search through every possible groups again using GroupSearch algorithm
		 * @param new_tile
		 * @return
		 */
		public boolean update_groups(int new_tile)
		{
			return false;
		}
		
		
		/**
		 * *Warning* Should only run when this.update_Groups is empty, otherwise waste of resources
		 * @param new_tile The new tile that assumes is newly drawn to the hand, can be discarded
		 * @return True if the current groups are updated to fit the new_tile inputed,
		 * 		   False if the tile either doesn't fit (groups didn't alter in anyway) or error was raised
		 */
		public boolean refresh_GroupSN()
		{
			try
			{
				HashMap<String, String> groupSN_map = GroupSearch.search_all_groupSN(this, false);
				for(String key: groupSN_map.keySet())
					this.update_Groups_.put(key, groupSN_map.get(key));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
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
				if(Group.group_status(group) != -3 && group.getGroupInfo()[1] != -1) //To re-confirm the Group/suit is not invalid
				{
					//group.getGroupInfo()[1] == suit index
					return_ArrayList.get(group.getGroupInfo()[1]).add(group);
				}
			}
			return return_ArrayList;
		}
	}
	public static void main(String[] args)
	{
		ArrayList<Group> declared_groups = new ArrayList<Group>();
		for(int i = 0; i < 2; i++) 
		{
			Group temp_group = Group.random_group(true, false);
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
		
		System.out.println(test_PHand.getDeclaredGroup());
		System.out.println(test_PHand.getCurrentHand());
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
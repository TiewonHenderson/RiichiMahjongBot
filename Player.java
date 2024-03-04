package bot_package;

import java.util.*;

public class Player
{
	
	/*
	 * Used to keep track of amount of untitled players
	 * Doesn't need to be accessed outside of this class
	 */
	protected static int unname_amt = 0;
	
	/*
	 * Used to keep track of wind counter (Use seatWind index to reference who to assign)
	 * Doesn't need to be accessed outside of this class
	 */
	protected static int seatWind_amt = 0;

	public String playerName;
	
	/*
	 * E = 0
	 * S = 1
	 * W = 2
	 * N = 3
	 */
	public int seatWind;

	/*
     * Integer to keep track of drawn flowers, will be of magnitude of 10^4
     * Use seatWind to determine what flower is the player's good flower (10^seatWind)
     */
    public int flower;
    
	public ArrayList<Integer> dropPile;
	
	/*
	 * The current Playerhand of this player instance, has closed and open tiles in 2D ArrayList
	 */
	private PlayerHand playerHand;
	
	/*
	 * Used to created a new default player object, fields will be set as
	 * playerName = "UNNAMED" + unname_amt
	 * seatWind = seatWind_amt
	 * new drop pile and playerHand
	 */
	public Player()
	{
		this.playerName = "UNNAMED" + Integer.toString(unname_amt);
		this.seatWind = seatWind_amt;
		unname_amt += 1;
		seatWind_amt += 1;
		
		if(unname_amt > 3)
		{
			unname_amt = 0;
		}
		if(seatWind_amt > 3)
		{
			seatWind_amt = 0;
		}
		this.dropPile = new ArrayList<Integer>();
		this.playerHand = new PlayerHand();
		this.flower = 0;
	}
	
	/*
	 * Used to create a copy of inputed Player
	 */
	public Player(Player clone)
	{
		this.playerName = new String(clone.playerName);
		this.seatWind = clone.seatWind;
		this.dropPile = new ArrayList<Integer>(clone.dropPile);
		this.playerHand = new PlayerHand(clone.playerHand);
		this.flower = clone.flower;
	}
	
	/*
	 * Used to all details about this instance's hand, includes open/closed decalred groups
	 */
	public PlayerHand getPlayerHand()
	{	
		return this.playerHand;
	}
	
	public boolean setPlayerHand(PlayerHand in_playhand)
	{
		try
		{
			this.playerHand = new PlayerHand(in_playhand);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
	}
	
	public ArrayList<Integer> getPlayerDrops()
	{
		return this.dropPile;
	}
	
	/**
	 *
	 * Prioritized indicators
	 * "m", "p", "s", "z" to add respective tile_id for the group/tiles
	 * ^^^ Refer to Group.suit_reference
     *  "c", "k", and "o" to determine which ArrayList to add into
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
		 * add_mode == 2 -> add as open declared groups
		 * add_mode == 1 -> add as concealed declared kongs/kans
		 * add_mode == 0 -> add as concealed in hand tiles
		 */
		int add_mode = 2;
		
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
			else if(current_char == 'k')
			{
				add_mode = 1;
				continue;
			}
			
			if(Character.isDigit(current_char)) //Checks if the character is a number
			{
				int index = i; //Need to decrement as starting index was at STR.length - 1
				ArrayList<Integer> temp_tiles = new ArrayList<Integer>();
				switch(add_mode)
				{
					case 2: //Add to open groups ArrayList
						while(Character.isDigit(mjSTR.charAt(index)) && temp_tiles.size() < 3)
						{
							temp_tiles.add(Character.getNumericValue(mjSTR.charAt(index)) + (suit * 9) - 1);
							System.out.println("Open groups: " + mjSTR.charAt(index) + "temp_tile size: " + temp_tiles.size());
							index--;
						}
						if(Group.group_status(temp_tiles) >= 1) //Re-confirms the tiles added is a group
						{
							declared_segment.add(new Group(temp_tiles, false));
						}
						i = index + 1;
						break;
					case 1: //Add to open groups but set to concealed and is quad
						while(Character.isDigit(mjSTR.charAt(index)) && temp_tiles.size() != 4)
						{
							temp_tiles.add(Character.getNumericValue(mjSTR.charAt(index)) + (suit * 9) - 1);
							System.out.println("Closed Quads: " + mjSTR.charAt(index) + "temp_tile size: " + temp_tiles.size());
							index--;
						}
						if(Group.group_status(temp_tiles) == 3)
						{
							declared_segment.add(new Group(temp_tiles, true));
						}
						i = index + 1;
						break;
					case 0: //Add to concealed hand ArrayList
						close_segment.add(Character.getNumericValue(mjSTR.charAt(i)) + (suit * 9) - 1);
						break;
				}
			}
		}
		return new PlayerHand(close_segment, declared_segment, mjSTR);
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
			if(group.getGroupInfo()[0] == 3  && group.concealed) //Dont add concealed declared quads, add in future
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
		private ArrayList<Integer> currentHand;
		
		/*
		 * The String representation of PlayerHand
		 * A use for this String could be Logging, Display, Saved as starting hand indicator
		 */
		private String mjSTRhand; 
		
		/*
		 * The declaredGroups of this PlayerHand, what PlayerHand represents is not just
		 * the concealed part of what the Player is playing on, but the Entire hand
		 */
		public ArrayList<Group> declaredGroups; 
		
		/*
		 * 	update_Groups itself represents ArrayList<ArrayList<Group>> for each groupSN case, 
		 *	ArrayList<ArrayList<Group>> will represent 4 ArrayList<Group> for each suit, regardless if its empty
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
		private HashMap<String, ArrayList<ArrayList<Group>>> update_Groups = new HashMap<String, ArrayList<ArrayList<Group>>>();
		
		
		
		/**
		 * Default constructor
		 */
		public PlayerHand()
		{
			this.currentHand = new ArrayList<Integer>();
			this.declaredGroups = new ArrayList<Group>();
			this.mjSTRhand = "";
		}
		
		/**
		 * Used to clone a Player's Hand
		 * @param clone A PlayerHand wanted to be copy to another instance
		 */
		public PlayerHand(PlayerHand clone)
		{
			this.currentHand = new ArrayList<Integer>(clone.currentHand);
			this.declaredGroups = new ArrayList<Group>(clone.declaredGroups);
			this.mjSTRhand = new String(clone.mjSTRhand);
			this.update_Groups = new HashMap<String, ArrayList<ArrayList<Group>>>(clone.update_Groups);
		}
		
		/**
		 * A concealed hand only PlayerHand Constructor
		 * @param in_hand The only part of the ENTIRE player's hand and is complete concealed
		 */
		public PlayerHand(ArrayList<Integer> in_hand)
		{
			this.currentHand = new ArrayList<Integer>(in_hand);
			this.declaredGroups = new ArrayList<Group>();
			this.mjSTRhand = convert_PlayerHand(in_hand, this.declaredGroups);
		}
		
		/**
		 * A custom hand inputed PlayerHand Constructor
		 * @param in_hand The current concealed hand of the desired PlayerHand
		 * @param declared_groups The current Declared Groups of the desired PlayerHand, can include concealed declared quads
		 */
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups)
		{
			this.currentHand = new ArrayList<Integer>(in_hand);
			this.declaredGroups = new ArrayList<Group>(declared_groups);
			this.mjSTRhand = convert_PlayerHand(in_hand, declared_groups);
		}
		
		/**
		 * A custom hand inputed PlayerHand Constructor
		 * @param in_hand The current concealed hand of the desired PlayerHand
		 * @param declared_groups The current Declared Groups of the desired PlayerHand, can include concealed declared quads
		 * @param mjSTR The mjSTR of the PlayerHand, this constructor is typically used to save resources
		 */
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups, String mjSTR)
		{
			this.currentHand = new ArrayList<Integer>(in_hand);
			this.declaredGroups = new ArrayList<Group>(declared_groups);
			this.mjSTRhand = mjSTR;
		}
		
		/**
		 * STRING CONSTRUCTOR
		 * @param in_STRformat Input as mjSTR, will be translated to concealed hand and declared groups
		 */
		public PlayerHand(String in_STRformat)
		{
			PlayerHand temp_obj = convert_mjSTR(in_STRformat);
			
			this.currentHand = temp_obj.getCurrentHand();
			this.declaredGroups = temp_obj.getDeclaredGroup();
			this.mjSTRhand = new String(in_STRformat);
		}
		
		/*
		 * Returns the current ArrayList<Integer> of the Player's inside_hand
		 */
		public ArrayList<Integer> getCurrentHand()
		{
			return this.currentHand;
		}
		
		/*
		 * Returns the current ArrayList<Group> that are visible to other players
		 * This however does include the concealed declared quads that would consider the hand
		 * still be concealed
		 */
		public ArrayList<Group> getDeclaredGroup()
		{
			return this.declaredGroups;
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
				this.currentHand = new ArrayList<Integer>(in_newHand);
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
				this.declaredGroups = new ArrayList<Group>(in_newGroups);
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
				this.declaredGroups.add(new Group(new_group));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
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
				ret_string += "\nmjSTR format: " + this.mjSTRhand;
				return ret_string;
			}
			catch(Exception e) {return "A variable most likely has not been initialized";}
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
				{
					// (sort_suitedGroups) -> (Convert groupSN to ArrayList<Group>) -> (keyed groupSN of groupSN_map)
					this.update_Groups.put(key, sort_suitedGroups(Group.groupSN_to_ArrayList(groupSN_map.get(key))));;
				}
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
			if(declared_groups.get(declared_groups.size() - 1).tile_list.size() == 4)
			{
				declared_groups.get(declared_groups.size() - 1).setDeclareStatus(true); //Makes all declared quads concealed
			}
			else
			{
				declared_groups.get(declared_groups.size() - 1).setDeclareStatus(false); //Makes sure declared non quad groups are not concealed
			}
		}
		int[] add_tiles = {0,0,0,5,6,7,7};
		ArrayList<Integer> fake_hand = new ArrayList<Integer>();
		for(int i = 0; i < add_tiles.length; i++) fake_hand.add(add_tiles[i]);
		
		System.out.println(convert_PlayerHand(fake_hand, declared_groups));
		System.out.println(convert_mjSTR(convert_PlayerHand(fake_hand, declared_groups)));
	}
}

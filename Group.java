package bot_package;

import java.util.*;
import java.util.function.*;
import bot_package.Player;
import bot_package.Player.PlayerHand;

public class Group
{
	//Literal list of tile_id
	public ArrayList<Integer> tile_list = new ArrayList<Integer>();
	public boolean concealed;
	
	public static char[] suit_reference = {'m','p','s','z'};
	
	/*
	 * Default Constructor
	 */
	public Group()
	{
		this.concealed = false;
	}
	
	/**
	 * ArrayList<Integer> object Compatible Constructor for Group obj
	 * @param in_group: A typical 3/4 tile ArrayList<Integer> that represents a tile group
	 * @param concealed: Declare whether the group is concealed
	 */
	public Group(ArrayList<Integer> in_group, boolean concealed)
	{
		this.tile_list = new ArrayList<Integer>(in_group);
		this.concealed = concealed;
	}
	
	/**
	 * int[] object Compatible Constructor for Group obj
	 * @param in_group: A typical 3/4 tile int[] that represents a tile group
	 * @param concealed: Declare whether the group is concealed
	 */
	public Group(int[] in_group, boolean concealed)
	{
		for(int i = 0; i < in_group.length; i++) tile_list.add(in_group[i]);
		this.concealed = concealed;
	}
	
	/*
	 * Used to create a new clone object of Group
	 */
	public Group(Group clone)
	{
		this.tile_list = clone.tile_list;
		this.concealed = clone.concealed;
	}

	/**
	 * If any digit = 0, that means the group is invalid/Uncomplete
	 * @return: Gets information of this instance of group
	 * 			digit 10^1 = group type, i.e 1 = sequence, 2 = triplets, 3 = quads
	 * 			digit 10^0 = group suit, i.e 1 = mans, 2 = pins, 3 = sous, 4 = honors
	 */
	public String getGroupInfo()
	{
		int groupType = validGroup(this.tile_list);
		if(groupType <= 0) 
		{
			return "00";
		}
		String returnInfo = Integer.toString(groupType);
		returnInfo += this.tile_list.get(0)/9;
		return returnInfo;
	}
	
	/**
	 * Used to alter if Group is concealed or not
	 * @param in_status: The new status of the group, if it's concealed or not
	 */
	public void setDeclareStatus(boolean in_status)
	{
		this.concealed = in_status;
	}
	
	/**
	 * 
	 * @param new_group: An ArrayList<Integer> of tile_ids
	 * @return: sets new group to argument and returns true if new_group.size() < 5 and > 0.
	 * 			Does nothing returns false if condition is not met.
	 */
	public boolean setGroup(ArrayList<Integer> new_group)
	{
		if(new_group.size() > 0 && new_group.size() < 5)
		{
			this.tile_list = new ArrayList<Integer>(new_group);
			return true;
		}
		return false;
	}
	
	public ArrayList<Integer> get_groupTiles()
	{
		return this.tile_list;
	}
	/**
	 * Used to print the string representation of the tile_list
	 */
	public String toString()
	{
		String return_str = "(";
		for(int tile_id: this.tile_list) 
		{
			return_str += Integer.toString(tile_id) + ",";
		}
		try
		{
			return return_str.substring(0,return_str.length() - 1) + ")";
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * 
	 * @param in_group: An ArrayList<Integer> of size 3/4 to check if valid group
	 * @return: The group type of the inputted ArrayList<Integer>, 
	 * -3 = invalid
	 * -2 = floating
	 * -1 = incomp sequence
	 * 0 = pair
	 * 1 = sequence
	 * 2 = triplets
	 * 3 = quads
	 */
	public static int validGroup(ArrayList<Integer> in_group)
	{
	    if(in_group.size() == 0 || in_group.size() > 4)
	    {
	        return -3;
	    }
	    //checks if there is only 1 distinct element in ArrayList<Integer>
	    boolean all_Same = in_group.stream().distinct().count() == 1;
	    //Checks quads
	    switch(in_group.size())
	    {
	    	case 4:
	    		//check if all the tiles are the same for quads
	    		if(all_Same)
	    		{
	    			return 3;
	    		}
	    		return -3;
	    	case 3:
	    		//check if all the tiles are the same for triplets
	    		if(all_Same)
	    		{
	    			return 2;
	    		}
	    		//check if all the tiles are increments of each other for sequences
	    		ArrayList<Integer> sortedGroup = sortArray(in_group);
	    	    for(int i = 0; i < sortedGroup.size() - 1; i++)
	    	    {
	    	        if(sortedGroup.get(i) + 1 != sortedGroup.get(i + 1))
	    	        {
	    	            return -3;
	    	        }
	    	    }
	    	    return 1;
	    	case 2:
	    		//If the two tiles are the same, its a pair
	    		if(all_Same)
	    		{
	    			return 0;
	    		}
	    		
	    		//If the two tiles are in talking distances, -2 -1 +1 +2, then its an incomplete sequence
	    		if(Math.abs(in_group.get(0) - in_group.get(1)) <= 2)
	    		{
	    			return -1;
	    		}
	    		return -3;
	    	case 1:
	    		//Just 1 tile = floating tile
	    		return -2;
	    	default:
	    		return -3;
	    }
	}
	
    /**
	 * @param in_hand: Any valid hand input
	 * @return: 4 ArrayList<Integer> within Arraylist<Integer> that represents each suit of a mahjong hand.
	 * 			The return tiles in each ArrayList<Integer> contains the TILE_VAL, not PLAY_VAL!
	 */
	public static ArrayList<ArrayList<Integer>> suitDivide(ArrayList<Integer> input_hand)
	{
		ArrayList<ArrayList<Integer>> returnSuits = new ArrayList<ArrayList<Integer>>();
		for(int suit = 0; suit < 4; suit++)
		{
			returnSuits.add(new ArrayList<Integer>());
			for(int tile: input_hand)
			{
				if(tile < ((suit + 1) * 9) && tile >= (suit * 9))
				{
					returnSuits.get(suit).add(tile);
				}
			}
		}
		return returnSuits;
	}
	
	/**
	 * 
	 * @param in_array: A input of an ArrayList<Integer> that needs to be sorted
	 * @return A sorted ArrayList<Integer> of the original inputted ArrayList<Integer>
	 */
	public static ArrayList<Integer> sortArray(ArrayList<Integer> in_array)
	{
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		ArrayList<Integer> tempList = new ArrayList<Integer>(in_array);
		int min = in_array.get(0);
		int index = 0;
		
		for(int i = 0; i < in_array.size(); i++)
		{
			min = tempList.get(index);
			for(int j = 0; j < tempList.size(); j++)
			{
				if(tempList.get(j) < min)
				{
					min = tempList.get(j);
					index = j;
				}
			}
			returnList.add(min);
			tempList.remove(index);
			index = 0;
		}
		return returnList;
	}
	
	/**
	 * Overloading method so default value for reverse_sort = false, sort by minimum to maximum
	 * @param in_array: A input of an ArrayList<Integer> that needs to be sorted
	 * @return A sorted ArrayList<Integer> of the original inputted ArrayList<Integer>
	 */
	public static ArrayList<Integer> sortArray(ArrayList<Integer> in_array, boolean reverse_sort)
	{
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		ArrayList<Integer> tempList = new ArrayList<Integer>(in_array);
		int max = in_array.get(0);
		int index = 0;
		
		if(!reverse_sort)
		{
			return sortArray(in_array);
		}
		else
		{
			//Sorting by maximum to minimum when set boolean argument to true
			for(int i = 0; i < in_array.size(); i++)
			{
				max = tempList.get(index);
				for(int j = 0; j < tempList.size(); j++)
				{
					if(tempList.get(j) > max)
					{
						max = tempList.get(j);
						index = j;
					}
				}
				returnList.add(max);
				tempList.remove(index);
				index = 0;
			}
		}
		return returnList;
	}
	
	/*
	 * Used to make an ArrayList<Integer> with int[]
	 */
	public static ArrayList<Integer> createArrayList(int[] in_list)
	{
		ArrayList<Integer> return_ArrayList = new ArrayList<Integer>();
		for(int i = 0; i < in_list.length; i++) return_ArrayList.add(in_list[i]);
		return return_ArrayList;
	}
	
	/**
	 * @Function:
	 * 		Given a groupSN (group string notation), it will give :
	 * 			all the complete
	 * 			incomplete (unfinished seq, pairs)
	 * 			floating tiles
	 *		This function prioritizes pairs > unfinished seq since pairs can function as a incomplete group and "eyes"
	 *		Complete hand = 4 groups + 1 eyes
	 *		Ready hand = 3 groups + 1 eyes OR 4 groups + 1 floating tile (to become eyes)
	 *		Incomplete hand = Any other case (with exception to special hands)
	 *		
	 * 		Search methods
	 * 		Brute force methods:
	 * 			Linear search (from left to right && right to left)
	 * 			Outside in search (Left out, right out, left in, right in)
	 * 		
	 * @param playHand: A new PlayerHand object to search for potential groups
	 * @return: A HashMap<String, String> where the String key gives information on how the Groups were searched
	 * 			String format: Index 0,1 = pairs/eye, index 2 complete/Waiting/Incomplete, index 3 way groups were search
	 * 			String: A group_SN to give information about the group
	 */
	public static HashMap<String, String> getGroups(PlayerHand playHand)
	{
		ArrayList<ArrayList<Integer>> suit_list = suitDivide(playHand.getCurrentHand());
		
		/*
		 * Searches for isolates pairs
		 * method:
		 * 	Suit divides to find pairs with no talking tiles;
		 * 	If tile = index, (if index == 2) and (index -2,-1,1,2 == 0), then it is isolate
		 * 	isolate pairs -> String isolatePairs
		 * 	pairs in general are saved -> String pairs
		 */
		String isolatePairs = "";
		String pairs = "";
		/*
		 * Separate honors search since they CANNOT form sequences
		 * If honor == only a pair == isolate and HAS to be pair of hand
		 */
		ArrayList<Integer> honor_matrix = convert_2_matrix(suit_list.get(suit_list.size() - 1));
		for(int tile_index = 0; tile_index < honor_matrix.size(); tile_index++)
		{
			if(honor_matrix.get(tile_index) == 2)
			{
				isolatePairs += Integer.toString(tile_index + (3 * 9)) + ",";
				pairs += Integer.toString(tile_index + (3 * 9)) + ",";
			}
		}
		for(int suit = 0; suit < suit_list.size() - 1; suit++) 
		{
			if(suit_list.get(suit).size() <= 0) {continue;} //Don't check empty arrays, matrix doesnt include them
			
			ArrayList<Integer> suit_matrix = convert_2_matrix(suit_list.get(suit));
			
			//max is reference to not get out of bounds with increment up to +2 when checking talking tiles
			//add min = by play_val, i.e 4p = 12 [tile_id] = [index] + [min = 4] + [suit(p) * 9 = 1 * 9]
			
			int min = suit_list.get(suit).get(0) - (suit * 9);
			
			boolean isolate = true;
			boolean isPair = true;
			for(int tile_index = 0; tile_index < suit_matrix.size(); tile_index++)
			{
				/*
				 * 1 is not a pair, 4 is almost always never a pair
				 */
				if(suit_matrix.get(tile_index) == 2)
				{
					for(int i = -2; i <= 2; i++)
					{
						/*
						 * not search for index < 0 and index > max (max = matrix.size())
						 */
						if(tile_index + i <= 0) {continue;}
						if(tile_index + i >= suit_matrix.size()) {break;}
						if(suit_matrix.get(tile_index + i) > 0) {isolate = false;}
					}
				}
				else if(suit_matrix.get(tile_index) == 3)
				{
					isolate = false;
				}
				else
				{
					/*
					 * case 4: not possible, this will not search through declared groups
					 * Suppose a IN HAND quad was your possible pair i.e 222234m
					 * 222234m -> 2234m with 2m pair -> 2m single tile wait, 234m seq, 2m pair
					 * 		problem 1: all 2ms are used
					 * 		problem 2: pair is already determined, single tile wait for pair is impossible
					 */
					isPair = false;
					isolate = false;
				}
				/*
				 * Add 1) is isolate pair, 2) is a possible pair
				 * Adding data format is in tile_id
				 */
				String tile_str = Integer.toString(tile_index + (suit * 9) + min);
				
				//Use "," as indication of end to determine digits of pair tile_id
				if(isolate) {isolatePairs += tile_str + ",";}
				if(isPair) {pairs += tile_str + ",";}
			}
		}

		/**
		 * Linear Search
		 */
		
		//Where the hands gets pairs removed/ no pairs removed
		HashMap<Integer, ArrayList<Integer>> allPossible_hands = new HashMap<Integer, ArrayList<Integer>>();
		allPossible_hands.put(-1, new ArrayList<Integer>(playHand.getCurrentHand())); //No pairs removed
		
		//If pairs are removed, removes isolate pairs first
		if(isolatePairs.length() > 0)
		{
			int start_index = 0;
			for(int i = 0; i < isolatePairs.length(); i++)
			{
				if(isolatePairs.charAt(i) == ',')
				{
					//Substring = inclusive start index to exclusive i(where the ',' is)
					int remove_pair = Integer.parseInt(isolatePairs.substring(start_index,i)); //Convert the pair from String to int
					int remove_counter = 0; //Confirms only the pair was removed
					ArrayList<Integer> temp_hand = new ArrayList<Integer>(playHand.getCurrentHand()); //Make sure copy is altered, not actual hand
					for(int j = 0; j < temp_hand.size(); j++)
					{
						if(remove_counter == 2)
						{
							break;
						}
						if(temp_hand.get(j) == remove_pair)
						{
							temp_hand.remove(j);
							remove_counter++;
							j--;
						}
					}
					if(remove_counter == 2) //fail safe, makes sure ONLY the pair was removed
					{
						allPossible_hands.put(remove_pair, temp_hand);
					}
					start_index = i + 1;
					continue;
				}
			}
			
			/*
			 * These will be added to the final return HashMap
			 * 	groupSN format = 
			 *	(complete group)(...)r={(incomplete group)(...)}[+min]suit c/o <- C = closed, O = open (no space inbetween)
			 *	i.e:
			 *		(012)(123)[+4]m(000)[+8]pr=(0)[+9]sr=(11)(2)[+1]z
			 *		 012 + 4 + (m =0 * 9) = 456m
			 *	adding min should account for tile_id -> play_val
			 */
			ArrayList<String> groupSN_list = new ArrayList<String>();
			
			/*
			 * 	groupSN_ID formate: 
			 * 	Eyes [index 0-1 inclusive](single digit tile_id follow by 0, i.e 7m = 06 tile_id): 
			 * 	C/W/I [index 2 inclusive](complete/Waiting/Incomplete, index 2)
			 *	LLNP/LLPR/LR.../OSNP... [index 3-6 inclusive](LL = Linear left, LR = Linear right, OS = Outer search, NP = No pairs, PR = pairs removed)
			 */
			ArrayList<String> groupSN_ID_list = new ArrayList<String>();
			
			//All linear searches are under here
			for(int key: allPossible_hands.keySet())
			{
				String temp_ID = Integer.toString(key);
				String temp_groupSN = "";
				
				suit_list = suitDivide(allPossible_hands.get(key));
				//LL = Linear Left to right (index 0 to index size() - 1)
				for(int suit_index = 0; suit_index < suit_list.size(); suit_index++)
				{
					temp_groupSN += list_GroupSearch(suit_list.get(suit_index), false); //adds GroupSN for each suit
				}
				System.out.println(temp_groupSN);
				System.out.println(groupSN_to_ArrayList(temp_groupSN));
				temp_groupSN = "";
				temp_ID = Integer.toString(key);
				//LR = Linear right to left (index size() - 1 to index 0)
				for(int suit_index = 0; suit_index < suit_list.size(); suit_index++)
				{
					temp_groupSN += list_GroupSearch(suit_list.get(suit_index), true); //adds GroupSN for each suit
				}
				System.out.println(temp_groupSN);
				System.out.println(groupSN_to_ArrayList(temp_groupSN));
			}
		}
		else
		{
			
		}
		
		/**
		 * Outer Search
		 */
		return null;
	}
	
	/**
	 * 
	 * @param in_groupSN: The group string notation of a given PlayerHand, used to provide information of suits of tiles
	 * @return: ArrayList<Group> of completed and incomplete groups (typically eyes if hand is ready/complete).
	 * 			Groups are in tile_id for better view comparing to player hand ArrayList<Integer> of tile_id
	 */
	public static ArrayList<Group> groupSN_to_ArrayList(String in_groupSN)
	{
		if(in_groupSN.length() == 0)
		{
			return new ArrayList<Group>();
		}
		
		//suit_reference
		ArrayList<Group> return_groups = new ArrayList<Group>();
		/*
		 * tile_mode indicates how to add tiles
		 * 0 = complete groups
		 * 1 = incomplete groups 
		 * 2 = add min
		 * 3/anything else = invalid;
		 */
		int tile_mode = 2;
		
		/*
		 * add_group indicates when to add tiles
		 * false = don't add tiles
		 * true = add tiles
		 */
		boolean add_tiles = false;
		
		/*
		 * Determine if groups are concealed or not
		 */
		boolean concealed = false;
		
		int suit = 0;
		int min = 0;
		
		//Where all the converted groups are stored
		ArrayList<ArrayList<Group>> all_groups = new ArrayList<ArrayList<Group>>();
		all_groups.add(new ArrayList<Group>()); //index 0 for completed groups
		all_groups.add(new ArrayList<Group>()); //index 1 for incompleted groups
		
		//Where the tiles flow into one group to be added
		ArrayList<Integer> add_group = new ArrayList<Integer>();
		/*
		 * Searching in reverse order due to not being able to get information of current tiles
		 * suit + add min are guaranteed at end of groupSN
		 */
		for(int char_index = in_groupSN.length() - 1; char_index >= 0; char_index--)
		{
			char current_char = in_groupSN.charAt(char_index);
			
			//If character is a alphabet indicator, however it can be suit/remainder/open/closed
			if(Character.isAlphabetic(current_char))
			{
				//suit_reference = {'m', 'p', 's', 'z'}
				for(int i = 0; i < suit_reference.length; i++)
				{
					if(current_char == suit_reference[i])
					{
						suit = i; //refers to index respective to suit
						add_tiles = false;
					}
				}
				switch(Character.toString(current_char))
				{
					case "c":
						concealed = true;
					case "o":
						concealed = false;
				}
			}
			//If character is a integer, however it can be complete group tiles/remainder tiles/add min
			else if(Character.isDigit(current_char))
			{
				int char_num = Character.getNumericValue(current_char);
				if(add_tiles)
				{
					/*
					 * x = matrix compressed index representation
					 * matrix compressed -> play_val = (x + min)
					 * play_val -> tile_id = (play_val + (suit * 9) - 1)
					 */
					add_group.add(char_num + min + (suit * 9) - 1);
				}
				else
				{
					min = char_num;
					add_tiles = true;
				}
			}
			else
			{
				//Indicates end of min number, but reversed search means start of min
				if(current_char == ']') {tile_mode = 2; add_tiles = false;} 
				
				//Indicates end of remainder, but reversed search means start remainder groups
				else if(current_char == '}') {tile_mode = 1; add_tiles = true;}
				
				//Indicates start of remainder, but reversed search means end remainder groups
				else if(current_char == '{') {tile_mode = 0;}
				
				//Indicates a start of a group, but reversed search means end group
				else if(current_char == '(') 
				{
					if(add_group.size() == 0)
					{
						continue;
					}
					Group new_group = new Group(sortArray(add_group), concealed);
					if(tile_mode == 1) { new_group.setDeclareStatus(true); } //If looking in remainder, incomplete groups are always concealed}
					if(tile_mode == 1 || tile_mode == 0){ all_groups.get(tile_mode).add(new_group); } //Adds group to corresponding index
					add_group = new ArrayList<Integer>();
				}
			}
		}
		
		/*
		 * Because the String was searched backwards, the suits are also backwards
		 * This sections will add the groups in order of suits
		 * 		This rule is after by how complete the group is applied
		 */
		for(int i = all_groups.get(0).size() - 1; i >= 0; i--) return_groups.add(all_groups.get(0).get(i));
		for(int i = all_groups.get(1).size() - 1; i >= 0; i--) if(all_groups.get(1).get(i).get_groupTiles().size() == 2) {return_groups.add(all_groups.get(1).get(i));}
		for(int i = all_groups.get(1).size() - 1; i >= 0; i--) if(all_groups.get(1).get(i).get_groupTiles().size() == 1) {return_groups.add(all_groups.get(1).get(i));}
		return return_groups;
	}
	
	/**
	 * 
	 * @param in_tile_id: A singular int value to be converted from tile_id to play_val
	 * @return: The play_val of the inputed integer, note this function only works from 0-26 inclusive
	 */
	public static int tile_id_to_PlayVal(int in_tile_id)
	{
		/*
		 * (in_tile_id / 9) = suit by flooring
		 * in_tile_id - ((in_tile_id / 9) * 9) = converts it into a man tile (all man = play_val - 1)
		 */
		return in_tile_id - ((in_tile_id / 9) * 9) + 1;
	}
	/**
	 * 
	 * @param in_array: ArrayList<Integer> with any valid tile_id integer as each element
	 * 					However if the ArrayList<Integer> includes invalid tile_ids, -1 will be added
	 * @return The ArrayList<Integer> representing the play_value of each tile, the play_value
	 * 		   represents 1-9 in each suit, i.e tile_id = 24 = 7s, play_value = 7
	 */
	public static ArrayList<Integer> tile_id_to_PlayVal(ArrayList<Integer> in_array)
	{
		ArrayList<Integer> return_array = new ArrayList<Integer>();
		if(in_array.size() < 0) {return return_array;}
		for(int tile_id: in_array)
		{
			if(tile_id < 0 || tile_id > 33) //Invalid tile_ids
			{
				return_array.add(-1);
			}
			return_array.add(tile_id_to_PlayVal(tile_id));
		}
		return return_array;
	}
	
	/**
	 * *Warning*, input must be tile_id format, otherwise suit will default -> 0 == "mans"
	 * *WARNING*, DO NOT PERFORM tile_id_to_PlayVal beforehand for argument in_suitarray, overindex can and will occur
	 * 
	 * @param in_array: Any valid ArrayList<Integer>, use case for a single suited ArrayList of tiles
	 *
	 * @return: A matrix displaying minimum in in_array as index 0 and maximum as index max - min, 
	 * 			quantity would be the value of each cell/index
	 */
	public static ArrayList<Integer> convert_2_matrix(ArrayList<Integer> in_suitarray)
	{
		if(in_suitarray.size() == 0) {return new ArrayList<Integer>();}
		/*
		 * Subtracts every element with minimum
		 * Makes sure its play_val because tile_id can exceed 9
		 */
		
		ArrayList<Integer> temp_suit = tile_id_to_PlayVal(sortArray(in_suitarray));
		int min = temp_suit.get(0);
		int max = temp_suit.get(temp_suit.size() - 1);
		/*
		 * If there are no tiles between two tiles, they are filled with 0s
		 */
		ArrayList<Integer> matrix = new ArrayList<Integer>();
		//To prevent overflow of 0s, there should only be max - min + 1 cells
		for(int i = 0; i < max - min + 1; i++) matrix.add(0);
		for(int i = 0; i < temp_suit.size(); i++)
		{
			int tile = temp_suit.get(i) - min;
			matrix.set(tile, matrix.get(tile) + 1);
		}
		return matrix;
	}
	
	/**
	 * @param  in_array: ASSUMES this is all in one suit, index out of bounds if not
	 * @param  reverse_search: Is irrelevant in triplet search since any instance of quantity 3 is taken, order doens't matter
	 * @param  only_triplets: true if groups should only be triplets, default = false
	 * @return A String representation of completed groups, remainder groups, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String list_GroupSearch(ArrayList<Integer> in_array, boolean reverse_search, boolean only_triplets)
	{
		if(!only_triplets){return list_GroupSearch(in_array, reverse_search);}
		
		String return_STR = "";
		if(in_array.size() <= 0) 
		{
			return "";
		}
		
		int suit = in_array.get(0)/9;
		
		//Convert suit to matrix
		ArrayList<Integer> matrix = convert_2_matrix(in_array);
		
		//Adds all the complete/incomplete groups into this ArrayList<String>
		ArrayList<String> group_shape_list = new ArrayList<String>();
		
		//Every tile can be a triplet
		for(int index = 0; index < matrix.size(); index++)
		{
			/*
			 * As long as tile_amt > 0, it can technically become a triplet, 
			 * of course higher amounts are more prioritized
			 * 
			 * This assumes the matrix is sorted
			 */
			String temp_str = "(";
			
			if(matrix.get(index) == 0){continue;} //excludes no tile present cells
			for(int n = 0; n < matrix.get(index); n++) temp_str += Integer.toString(index);
			temp_str += ")";
			group_shape_list.add(temp_str);
		}
		
		/*
		 * Assuming only triplets can be made, 
		 * quads needs to be declared as concealed kongs and would count as triplets
		 */
		boolean added_r_char = false;
		for(int tile_amt = 4; tile_amt >= 1; tile_amt--)
		{
			if(tile_amt >= 3)
			{
				/*
				 * checks each index if length of String corresponds to 
				 * (amount of chars - 2) > 2 to be a completed group
				 */
				for(int group_index = 0; group_index < group_shape_list.size(); group_index++)
				{
					if(group_shape_list.get(group_index).length() - 2 > 2)
					{
						/*
						 * Assuming the previous search was sorted
						 */
						return_STR += group_shape_list.get(group_index);
						group_shape_list.remove(group_index);
						group_index--;
					}
				}
			}
			else
			{
				//Starts the remainder incomplete groups section
				if(!added_r_char)
				{
					return_STR += "r={";
					added_r_char = true;
				}
				for(int group_index = 0; group_index < group_shape_list.size(); group_index++)
				{
					if(group_shape_list.get(group_index).length() - 2 == tile_amt)
					{
						/*
						 * tile_amt keeps track of which amount goes first
						 * 2 tiles groups would go first over 1 tile incomplete groups
						 */
						return_STR += group_shape_list.get(group_index);
						group_shape_list.remove(group_index);
						group_index--;
					}
				}
			}
			//completes the groups section of the groupSN
			if(tile_amt == 1)
			{
				return_STR += "}";
			}
		}
		//Finalize groupSN
		return_STR += "[+" + tile_id_to_PlayVal(sortArray(in_array)).get(0) + "]";
		return_STR += Character.toString(suit_reference[suit]);
		return return_STR;
	}
	
	/**
	 * 
	 * FUNCTION OVERLOADING WITH DEFAULT PARAMETER "only_triplets" SET TO "false"
	 * @param  in_array: ASSUMES this is all in one suit
	 * @param  reverse_search: Whether or not the search the inputted array from index 0 to last element or last element to index 0
	 * @return A String representation of completed groups, remainder groups, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String list_GroupSearch(ArrayList<Integer> in_array, boolean reverse_search)
	{
		String return_STR = "";
		if(in_array.size() <= 0) 
		{
			return "";
		}
		
		//Get suit number, minimum in array, maximum in array
		int suit = in_array.get(0)/9;
		
		//If given honors list, return GroupSearch with only_triplets as true
		if(suit == 3) {return list_GroupSearch(in_array, false, true);}
		
		ArrayList<Integer> temp_suit = tile_id_to_PlayVal(sortArray(in_array));
		int total_tiles = temp_suit.size();
		int play_val_min = temp_suit.get(0);

		//Convert suit to matrix
		ArrayList<Integer> matrix = convert_2_matrix(in_array);
		
		//Index that refers to the play_val integer in the matrix (a pointer)
		int currentIndex = 0;
		if(reverse_search)
		{
			currentIndex = matrix.size() - 1;
		}
		/*
		 * In case the given array doesn't have real groups, 
		 * temp_matrix is altered to not alter the original matrix
		 * 
		 * If valid group is found, matrix is altered accordingly
		 */
		ArrayList<Integer> temp_matrix = new ArrayList<Integer>(matrix);
		ArrayList<String> group_shape_list = new ArrayList<String>();
		boolean absolute_break = false;
		
		//There is possibly temp_suit.size() amount of groups if each group is a single tile
		for(int i = 0; i < temp_suit.size(); i++)
		{
			String temp_str = "";
			//Shape as set is how duplicates are not added
			SortedSet<Integer> shape = new TreeSet<Integer>();
			//Prevents out of bounds
			if((currentIndex >= temp_matrix.size() && !reverse_search)	||
			   (currentIndex < 0 && reverse_search)	|| 
				total_tiles <= 0)
			{
				break;
			}
			//Moves if tile cell has no tiles to offer until there is a cell with tiles
			if(temp_matrix.get(currentIndex) <= 0)
			{
				if(reverse_search)
				{
					//Reverse search decrement the index, so the minimum index is 0
					for(int j = currentIndex; j >= 0; j--)
					{
						if(temp_matrix.get(currentIndex) <= 0)
						{
							currentIndex--;
							if(currentIndex < 0)
							{
								absolute_break = true;
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
				else
				{
					//Linear order search increment index, so maximum index is size()-1
					for(int j = currentIndex; j < temp_matrix.size(); j++)
					{
						if(temp_matrix.get(currentIndex) <= 0)
						{
							currentIndex++;
							if(currentIndex >= temp_matrix.size())
							{
								absolute_break = true;
								break;
							}
						}
						else
						{
							break;
						}
					}
				}
			}
			/*
			 * The above code changes the currentIndex point of the matrix, 
			 * anything out of bounds will set absolute_break to true to break the for loop 
			 */
			if(absolute_break)
			{
				break;
			}
			
			//If get(currentIndex) >= 3, it means a triplet can be made from it
			if(temp_matrix.get(currentIndex) >= 3)
			{
				for(int n = 0; n < 3; n++) temp_str += Integer.toString(currentIndex);
				temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 3);
				total_tiles -= 3;
				group_shape_list.add(temp_str);
				continue;
			}
			
			//If triplet cannot be made, add current index
			shape.add(currentIndex);
			temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
			//total_tiles -= 1;
			
			int m = 0;
			while((m <= 2 && !reverse_search) || (m >= -2 && reverse_search))
			{
				if(!reverse_search)
				{
					m++; //first to last requires index search to start at 1
				}
				else
				{
					m--; //last to first requires index search to start at -1
				}
				//Prevents exceeding out of talking tiles
				if(!(m <= 2 && !reverse_search) && !(m >= -2 && reverse_search))
				{
					break;
				}
				//Prevents exceeding out of bounds
				int increment_index = currentIndex + m;
				if(increment_index < 0 || temp_matrix.size() - (increment_index) <= 0)
				{
					break;
				}
				if(temp_matrix.get(increment_index) > 0)
				{
					shape.add(increment_index);
					temp_matrix.set(increment_index, temp_matrix.get(increment_index) - 1);
					total_tiles--;
				}
			}
			
			//Since pairs are not looked for in above loop, this condition will fill that gap
			if(shape.size() == 1 && temp_matrix.get(currentIndex) > 0)
			{
				int total_copy = temp_matrix.get(currentIndex);
				//sets remove duplicates, but there is only one element
				for(int a = 0; a < total_copy + shape.size(); a++) 
				{
					temp_str += Integer.toString(shape.first());
					temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
					total_tiles--;
				}
			}
			else
			{
				//Since shapes removes duplicates, and i is bounded into 1,2: This only allows sequences
				for(int tile: shape) temp_str += Integer.toString(tile);
			}
			group_shape_list.add(temp_str);
			matrix = new ArrayList<Integer>(temp_matrix);
		}
		
		/*
		 * Format = 
		 * (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
		 */
		for(int i = 3; i >= 1; i--)
		{
			//Remainder groups start with size 2
			if(i == 2)
			{
				return_STR += "r={";
			}
			for(int k = 0; k < group_shape_list.size(); k++)
			{
				if(group_shape_list.get(k).length() == i)
				{
						return_STR += "(";
						return_STR += group_shape_list.get(k);
						return_STR += ")";
				}
			}	
			//Remainder groups end with size 1
			if(i == 1) 
			{
				return_STR += "}";
			}
		}
		
		return_STR += "[+" + play_val_min + "]";
		return_STR += Character.toString(suit_reference[suit]);
		return return_STR;
	}
	
	/**
	 * 
	 * @return: true if compiled, false if wrong
	 */
	public static void test()
	{
		Player example_player = new Player();
		example_player.setPlayerHand(new PlayerHand());
		//segment 1
		int[] singlesuit_hand = {9,9,10,13,14,15,17,17,17};
		ArrayList<Integer> single_suit_example = sortArray(createArrayList(singlesuit_hand));
		example_player.getPlayerHand().setCurrentHand(single_suit_example);
		
		System.out.println("example 1: " + single_suit_example);
		System.out.println("Convert to matrix: " + convert_2_matrix(single_suit_example));
		System.out.println("Search any groupLR: " + list_GroupSearch(single_suit_example, false));
		System.out.println("Search any groupRL: " + list_GroupSearch(single_suit_example, true));
		System.out.println("Search only triplets: " + list_GroupSearch(single_suit_example, false, true));
		System.out.println("Search only triplets: " + list_GroupSearch(single_suit_example, true, true));
		System.out.println("From groupSN to ArrayList<Group>: " + groupSN_to_ArrayList(list_GroupSearch(single_suit_example, false)));
		System.out.println(getGroups(example_player.getPlayerHand()) + "\n\n");
		
		//segment 2
		int[] complex_hand = {0,0,1,3,7,7,15,15,16,16,17,27,27};
		ArrayList<Integer> complex_example = sortArray(createArrayList(complex_hand));
		example_player.getPlayerHand().setCurrentHand(complex_example);
		
		System.out.println(complex_example);
		ArrayList<ArrayList<Integer>> suit_list = suitDivide(complex_example);
		for(ArrayList<Integer> suits : suit_list)
		{
			System.out.println("Suits: " + suits);
			System.out.println("Matrices: " + convert_2_matrix(suits));
			System.out.println("Search any groupLR (groupSN): " + list_GroupSearch(suits, false));
			System.out.println("Search any groupRL (groupSN): " + list_GroupSearch(suits, true));
			System.out.println("Search only triplets (groupSN): " + list_GroupSearch(suits, false, true));
		}
		System.out.println(getGroups(example_player.getPlayerHand()) + "\n\n");
		
		int[] completed_groups = {0,0,0,1,2,3,5,6,7,8,8,8};
		ArrayList<Integer> completeGroup_example = sortArray(createArrayList(completed_groups));
		example_player.getPlayerHand().setCurrentHand(completeGroup_example);
		System.out.println(completeGroup_example);
		suit_list = suitDivide(completeGroup_example);
		for(ArrayList<Integer> suits : suit_list)
		{
			System.out.println("Suits: " + suits);
			System.out.println("Matrices: " + convert_2_matrix(suits));
			System.out.println("Search any groupLR (groupSN): " + list_GroupSearch(suits,false));
			System.out.println("Search any groupRL (groupSN): " + list_GroupSearch(suits,true));
			System.out.println("Search only triplets (groupSN): " + list_GroupSearch(suits, false, true));
		}
		System.out.println(getGroups(example_player.getPlayerHand()) + "\n\n");

	}
	public static void main(String[] args)
	{
		ArrayList<Integer> lol = new ArrayList<Integer>();
		lol.get(34);
		test();
	}
}

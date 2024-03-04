package bot_package;

import java.util.*;

public class Group extends Scoring
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
		this.concealed = true;
	}
	
	/**
	 * ArrayList<Integer> object Compatible Constructor for Group obj
	 * @param in_group A typical 3/4 tile ArrayList<Integer> that represents a tile group
	 * @param concealed Declare whether the group is concealed
	 */
	public Group(ArrayList<Integer> in_group, boolean concealed)
	{
		this.tile_list = sortArray(in_group);
		this.concealed = concealed;
	}
	
	/**
	 * int[] object Compatible Constructor for Group obj
	 * @param in_group A typical 3/4 tile int[] that represents a tile group
	 * @param concealed Declare whether the group is concealed
	 */
	public Group(int[] in_group, boolean concealed)
	{
		for(int i = 0; i < in_group.length; i++) tile_list.add(in_group[i]);
		this.concealed = concealed;
	}
	
	/**
	 * Default value for concealed is true
	 * @param in_group A typical 3/4 tile ArrayList<Integer> that represents a tile group
	 */
	public Group(ArrayList<Integer> in_group)
	{
		this.tile_list = sortArray(in_group);
		this.concealed = true;
	}
	
	/**
	 * Default value for concealed is true
	 * @param in_group A typical 3/4 tile int[] that represents a tile group
	 */
	public Group(int[] in_group)
	{
		for(int i = 0; i < in_group.length; i++) tile_list.add(in_group[i]);
		this.tile_list = sortArray(this.tile_list);
		this.concealed = true;
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
	 * @return Gets information of this instance of group as integer list
	 * 			index 0 = group type, i.e 1 = sequence, 2 = triplets, 3 = quads
	 * 			index 1 = group suit, i.e -1 = invalid/mixed, 0 = mans, 1 = pins, 2 = sous, 3 = honors
	 */
	public int[] getGroupInfo()
	{
		int[] return_data = {-3, -1};
		if(this.tile_list.size() <= 0)
		{
			return return_data;
		}
		int groupType = group_status(this.tile_list);
		return_data[0] = groupType;
		int suit = this.tile_list.get(0)/9;
		for(int index = 0; index < this.tile_list.size() - 1; index++)
		{
			if(this.tile_list.get(index)/9 != this.tile_list.get(index + 1)/9)
			{
				suit = -1;
				break;
			}
		}
		return_data[1] = suit;
		return return_data;
		
	}
	
	/**
	 * Used to alter if Group is concealed or not
	 * @param in_status The new status of the group, if it's concealed or not
	 */
	public void setDeclareStatus(boolean in_status)
	{
		this.concealed = in_status;
	}
	
	/**
	 * 
	 * @param new_group An ArrayList<Integer> of tile_ids
	 * @return sets new group to argument and returns true if new_group.size() < 5 and > 0.
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
	 * @return The String value to typically add into it's total groupSN
	 */
	public String to_groupSN(int min)
	{
		String return_str = "(";
		for(int tile_id: this.tile_list) 
		{
			return_str += Integer.toString(tileID_to_PlayVal(tile_id) - min);
		}
		return return_str + ")";
	}
	/**
	 * 
	 * @param in_group An ArrayList<Integer> of size 3/4 to check if valid group
	 * @return The group type of the inputted ArrayList<Integer>, 
	 * -3 = invalid
	 * -2 = floating
	 * -1 = incomp sequence
	 * 0 = pair
	 * 1 = sequence
	 * 2 = triplets
	 * 3 = quads
	 */
	public static int group_status(ArrayList<Integer> in_group)
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
	 * @function Overloading the group_status(ArrayList<Integer> in_group) method
	 * @param in_group A group object to check it's group status
	 * @return The group type of the inputted ArrayList<Integer>, 
	 * -3 = invalid
	 * -2 = floating
	 * -1 = incomp sequence
	 * 0 = pair
	 * 1 = sequence
	 * 2 = triplets
	 * 3 = quads
	 */
	public static int group_status(Group in_group)
	{
		return group_status(in_group.get_groupTiles());
	}
	
	/**
	 * 
	 * @param in_arraylist A input of an ArrayList<Integer> that needs to be sorted
	 * @return A sorted ArrayList<Integer> of the original inputted ArrayList<Integer>
	 */
	public static ArrayList<Integer> sortArray(ArrayList<Integer> in_arraylist)
	{
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		ArrayList<Integer> tempList = new ArrayList<Integer>(in_arraylist);
		if(in_arraylist.size() == 0)
		{
			return returnList;
		}
		int min = in_arraylist.get(0);
		int index = 0;
		
		for(int i = 0; i < in_arraylist.size(); i++)
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
	 * @param in_arraylist A input of an ArrayList<Integer> that needs to be sorted
	 * @return A sorted ArrayList<Integer> of the original inputted ArrayList<Integer>
	 */
	public static ArrayList<Integer> sortArray(ArrayList<Integer> in_arraylist, boolean reverse_sort)
	{
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		ArrayList<Integer> tempList = new ArrayList<Integer>(in_arraylist);
		int max = in_arraylist.get(0);
		int index = 0;
		
		if(!reverse_sort)
		{
			return sortArray(in_arraylist);
		}
		else
		{
			//Sorting by maximum to minimum when set boolean argument to true
			for(int i = 0; i < in_arraylist.size(); i++)
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
	 * 
	 * @param in_groupSN The group string notation of a given PlayerHand, used to provide information of suits of tiles
	 * @return ArrayList<Group> of completed and incomplete groups (typically eyes if hand is ready/complete).
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
						break;
					case "o":
						concealed = false;
						break;
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
	 * @param in_arraylist this arraylist is meant for all suit included, however it is implemented to be universal
	 * 					   since it searches for groups, then saves it suit, and adds accordingly.
	 * @return Returns the groupSN corresponding to the ArrayList<Group>
	 */
	public static String ArrayList_to_groupSN(ArrayList<Group> in_arraylist)
	{
		String return_str = "";
		ArrayList<Group> temp_groups = new ArrayList<Group>(in_arraylist); //This temp ArrayList allows us to remove elements to sort
		
		HashMap<Integer, ArrayList<Group>> suit_groups = new HashMap<Integer, ArrayList<Group>>();
		for(int i = 0; i <= 3; i++) suit_groups.put(i, new ArrayList<Group>());
		
		for(int size = 3; size >= 1; size--)
		{
			for(int i = 0; i < temp_groups.size(); i++) 
			{
				Group current_group = temp_groups.get(i);
				//If somehow a group with no tiles exist, continue
				if(current_group.get_groupTiles().size() <= 0)
				{
					continue;
				}
				//This prioritizes the group_size indicated by the for loop with temp_var size
				if(current_group.get_groupTiles().size() != size)
				{
					continue;
				}
				//Add group to corresponding id of HashMap
				suit_groups.get(current_group.get_groupTiles().get(0)/9).add(current_group);
			}
		}
		
		/*
		 * Iterates through keys which represents suit since groupSN is in order
		 * This algorithm assumes the suits in HashMap is in order
		 * This algorithm loop is to indicate closed/open groups, 
		 * Next loop for each suit
		 * final loop is for each group
		 */
		for(int i = 0; i < 2; i++)
		{
			int prev_str_length = return_str.length();
			for(int key: suit_groups.keySet())
			{
				//Checks if there are any groups in this suit
				if(suit_groups.get(key).size() == 0){continue;}
				
				temp_groups = new ArrayList<Group>(); //To only contain either concealed or non-concealed groups
				for(Group given_group: suit_groups.get(key))
				{
					if(given_group.concealed && (i == 0)) {temp_groups.add(given_group);}
					else if(!given_group.concealed && (i == 1)) {temp_groups.add(given_group);}
				}
				
				if(temp_groups.size() == 0) {continue;} //If temp_groups.size() == 0, there is no concealed/non-concealed groups
				ArrayList<Integer> suit_hand = new ArrayList<Integer>();
				int min = -1;
				//Adds all the tiles into one ArrayList
				for(Group group: temp_groups) for(int tile: group.tile_list) suit_hand.add(tile);
				
				min = tileID_to_PlayVal(sortArray(suit_hand).get(0));
				
				boolean start_remain = true; //Keeps track of where to add "r={", also helps close remainder segment
				String suit_string = ""; //The return for one suit, will be added to whole return_string

				for(Group given_group: temp_groups)
				{
					if((i == 0 && given_group.concealed) || (i == 1 && !given_group.concealed))
					{
						/*
						 *	The groupSN method will return the play_val-min and set it appropriate to groupSN format 
						 */
						if(given_group.get_groupTiles().size() >= 3)
						{
							suit_string += given_group.to_groupSN(min);
						}
						else if(given_group.get_groupTiles().size() <= 2)
						{
							if(start_remain)
							{
								start_remain = false;
								suit_string += "r={";
							}
							suit_string += given_group.to_groupSN(min);
						}
					}
				}
				
				if(suit_string.length() > 0 && !start_remain)//Checks if remainder was activated to close with }
				{
					return_str += suit_string + "}[+" + Integer.toString(min) + "]" +Character.toString(suit_reference[key]); 
				}
				else if(suit_string.length() > 0)//remainder was not added, only add min and suit
				{
					return_str += suit_string + "r={}[+" + Integer.toString(min) + "]" +Character.toString(suit_reference[key]); 
				}
			}
			if(return_str.length() - prev_str_length > 0) //Only add close/open if there were even groupSN is begin with
			{
				switch(i)
				{
					case 0:
						return_str += "c";
						break;
					case 1:
						return_str += "o";
						break;
				}
			}
		}
		return return_str;
	}
	
	/**
	 * 
	 * @param in_tile_id A singular int value to be converted from tile_id to play_val
	 * @return The play_val of the inputed integer, note this function only works from 0-26 inclusive
	 */
	public static int tileID_to_PlayVal(int in_tile_id)
	{
		/*
		 * (in_tile_id / 9) = suit by flooring
		 * in_tile_id - ((in_tile_id / 9) * 9) = converts it into a man tile (all man = play_val - 1)
		 */
		return in_tile_id - ((in_tile_id / 9) * 9) + 1;
	}
	/**
	 * 
	 * @param in_arraylist ArrayList<Integer> with any valid tile_id integer as each element
	 * 					However if the ArrayList<Integer> includes invalid tile_ids, -1 will be added
	 * @return The ArrayList<Integer> representing the play_value of each tile, the play_value
	 * 		   represents 1-9 in each suit, i.e tile_id = 24 = 7s, play_value = 7
	 */
	public static ArrayList<Integer> tileID_to_PlayVal(ArrayList<Integer> in_arraylist)
	{
		ArrayList<Integer> return_array = new ArrayList<Integer>();
		if(in_arraylist.size() < 0) {return return_array;}
		for(int tile_id: in_arraylist)
		{
			if(tile_id < 0 || tile_id > 33) //Invalid tile_ids
			{
				return_array.add(-1);
			}
			return_array.add(tileID_to_PlayVal(tile_id));
		}
		return return_array;
	}
	
	/**
	 * *Warning*
	 * Input must be tile_id format, otherwise suit will default -> 0 == "mans"
	 * 
	 * *Warning*
	 * DO NOT PERFORM tile_id_to_PlayVal beforehand for argument in_suitarray, overindex can and will occur
	 * 
	 * @param in_arraylist Any valid ArrayList<Integer>, use case for a single suited ArrayList of tiles
	 *
	 * @return A matrix displaying minimum in in_arraylist as index 0 and maximum as index max - min, 
	 * 			quantity would be the value of each cell/index
	 */
	public static ArrayList<Integer> convert_2_matrix(ArrayList<Integer> in_suitarray)
	{
		if(in_suitarray.size() == 0) {return new ArrayList<Integer>();}
		/*
		 * Subtracts every element with minimum
		 * Makes sure its play_val because tile_id can exceed 9
		 */
		
		ArrayList<Integer> temp_suit = tileID_to_PlayVal(sortArray(in_suitarray));
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
	 * *note* This program will delete any leading zeros and-
	 * assumes the min is represented at the first occurrence of an index amount > 0
	 * 
	 * @param matrix The matrix that represents 1 suit of tiles
	 * @param suit The suit the matrix represents
	 * @param min The minimum play_val in the suit of ArrayList<Integer>
	 * @return The ArrayList<Integer> that represent the tile_ids of each tile
	 */
	public static ArrayList<Integer> convert_2_ArrayList(ArrayList<Integer> matrix, int suit, int min)
	{
		boolean add_true = false;
		int true_index = min - 1; //play_val - 1 to align with tile_id
		ArrayList<Integer> tile_array = new ArrayList<Integer>(); //return type
		for(int i = 0; i < matrix.size(); i++)
		{
			if(matrix.get(i) > 0)
			{
				//Adds the amount according to the quantity set in the matrix
				for(int amt = 0; amt < matrix.get(i); amt++)
				{
					tile_array.add(true_index + (suit * 9));
				}
				true_index++;
				add_true = true;
			}
			else if(add_true)
			{
				//If tile amount was spotted earlier, true_index has to change for filler 0s
				true_index++;
			}
		}
		return tile_array;
	}
	
	/**
	 * @Function Just creates random groups for better testing experience
	 * @param is_complete Whether the group should always be complete or not, false = include incomplete
	 * @param is_concealed Whether the group should always be concealed, false = include opened
	 * @return A random Group of the desired choice given the parameter values
	 */ 
	public static Group random_group(boolean is_complete, boolean is_concealed)
	{
		int suit = (int)(Math.random() * 4); //Decides random suit
		boolean concealed = true;
		if(Math.random() > 0.5 && !concealed)
		{
			concealed = false;
		}
		ArrayList<Integer> tile_list = new ArrayList<Integer>();
		int start = (int)(Math.random() * 9);
		int selected = (int)(Math.random() * 6);
		if(suit == 3)
		{
			start = (int)(Math.random() * 7);
			int[] possible_shapes = {0, 1, 3, 5};
			selected = possible_shapes[(int)(Math.random() * 4)];
		}
		if(is_complete)
		{
			selected = (int)(Math.random() * 3);
			if(suit == 3)
			{
				selected = (int)(Math.random() * 2);
			}
		}
		/*
		 * 0 == complete + quad
		 * 1 == complete + triplet
		 * 2 == complete + sequence
		 * 3 == incomplete + pair
		 * 4 == incomplete + wait sequence
		 * 5 == incomplete + floating
		 */
		switch(selected) //Decides what group to create
		{
			case 0:
				for(int i = 0; i < 4; i++) tile_list.add(start + (suit * 9));
				break;
			case 1:
				for(int i = 0; i < 3; i++) tile_list.add(start + (suit * 9));
				break;
			case 2:
				start = (int)(Math.random() * 7);
				for(int i = 0; i < 3; i++) tile_list.add(start + i + (suit * 9));
				break;
			case 3: 
				for(int i = 0; i < 2; i++) tile_list.add(start + (suit * 9));
				break;
			case 4:
				start = (int)(Math.random() * 8);
				for(int i = 0; i < 2; i++) tile_list.add(start + i + (suit * 9));
				break;
			case 5:
				tile_list.add(start + (suit * 9));
				break;
		}
		return new Group(tile_list, concealed);
	}
	
	/**
	 * 
	 * @param in_array The ArrayList<Type> that wants to remove the first instance of input item
	 * @param rm_item The input item that wants to be remove (not completely, only once)
	 * @return ArrayList<Type> where the first instance of rm_item is removed, returns same array if item not present
	 */
	public static <T> ArrayList<T> rm_first_instance(ArrayList<T> in_array, T rm_item)
	{
		for(int i = 0; i < in_array.size(); i++) if(in_array.get(i) == rm_item) {in_array.remove(i); break;};
		return in_array;
	}
	
	/**
	 * 
	 * @return true if compiled, false if wrong
	 */
	public static void test()
	{
		Player example_player = new Player();
		example_player.setPlayerHand(new Player.PlayerHand());
		//segment 1
		int[] singlesuit_hand = {9,9,10,13,14,15,17,17,17};
		ArrayList<Integer> single_suit_example = sortArray(createArrayList(singlesuit_hand));
		example_player.getPlayerHand().setCurrentHand(single_suit_example);
		
		System.out.println("example 1: " + single_suit_example);
		System.out.println("Convert to matrix: " + convert_2_matrix(single_suit_example));
		System.out.println("Search any groupLR: " + GroupSearch.list_GroupSearch(single_suit_example, false));
		System.out.println("Search any groupRL: " + GroupSearch.list_GroupSearch(single_suit_example, true));
		System.out.println("Search only triplets: " + GroupSearch.list_tripletSearch(single_suit_example, false));
		System.out.println("Search only triplets: " + GroupSearch.list_tripletSearch(single_suit_example, true));
		System.out.println("From groupSN to ArrayList<Group>: " + groupSN_to_ArrayList(GroupSearch.list_GroupSearch(single_suit_example, false)));
		System.out.println(GroupSearch.search_all_groupSN(example_player.getPlayerHand()) + "\n\n");
		
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
			System.out.println("Search any groupLR (groupSN): " + GroupSearch.list_GroupSearch(suits, false));
			System.out.println("Search any groupRL (groupSN): " + GroupSearch.list_GroupSearch(suits, true));
			System.out.println("Search only triplets (groupSN): " + GroupSearch.list_tripletSearch(suits, false));
		}
		HashMap<String, String> all_groupSearch = GroupSearch.search_all_groupSN(example_player.getPlayerHand(), false);
		for(String key: all_groupSearch.keySet())
		{
			System.out.println("Group key: " + key + ", groupSN: " + all_groupSearch.get(key));
		}
		
		int[] completed_groups = {0,0,0,1,2,3,4,5,6,7,8,8,8};
		ArrayList<Integer> completeGroup_example = sortArray(createArrayList(completed_groups));
		example_player.getPlayerHand().setCurrentHand(completeGroup_example);
		System.out.println(completeGroup_example);
		suit_list = suitDivide(completeGroup_example);
		for(ArrayList<Integer> suits : suit_list)
		{
			System.out.println("Suits: " + suits);
			System.out.println("Matrices: " + convert_2_matrix(suits));
			System.out.println("Search any groupLR (groupSN): " + GroupSearch.list_GroupSearch(suits,false));
			System.out.println("Search any groupRL (groupSN): " + GroupSearch.list_GroupSearch(suits,true));
			System.out.println("Search only triplets (groupSN): " + GroupSearch.list_tripletSearch(suits, false));
		}
		all_groupSearch = GroupSearch.search_all_groupSN(example_player.getPlayerHand(), false);
		for(String key: all_groupSearch.keySet())
		{
			System.out.println("Group key: " + key + ", groupSN: " + all_groupSearch.get(key));
		}
	}
	public static void main(String[] args)
	{
//		Tests translation of ArrayList<Group> <-> groupSN
//		for(int j = 0; j < 50; j++)
//		{
//			ArrayList<Group> random_groups = new ArrayList<Group>();
//			for(int i = 0; i < 5; i++)
//			{
//				Group this_group = random_group(false, false);
//				random_groups.add(this_group);
//				System.out.println(this_group + " Concealed: " + this_group.concealed);
//			}
//			String groupSN = ArrayList_to_groupSN(random_groups);
//			ArrayList<Group> AL = groupSN_to_ArrayList(groupSN);
//			System.out.println("AL -> groupSN" + groupSN);
//			System.out.println("groupSN -> AL" + AL);
//		}
//		
//		int[] temp = {0,1,2,6,6,6,12,13,14,32,32,33,33};
//		ArrayList<Integer> random_hand = new ArrayList<Integer>();
//		for(int i = 0; i < temp.length; i++) random_hand.add(temp[i]);
//		
//		rm_first_instance(random_hand, 6);
//		rm_first_instance(random_hand, 6);
//		
//		for(ArrayList<String> print_stuff: GroupSearch.search_groups(6, random_hand))
//		{
//			System.out.println(print_stuff);
//		}
		test();
	}
}

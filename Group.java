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
		this.concealed = false;
	}
	
	/**
	 * ArrayList<Integer> object Compatible Constructor for Group obj
	 * @param in_group A typical 3/4 tile ArrayList<Integer> that represents a tile group
	 * @param concealed Declare whether the group is concealed
	 */
	public Group(ArrayList<Integer> in_group, boolean concealed)
	{
		this.tile_list = new ArrayList<Integer>(in_group);
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
	 * @return Gets information of this instance of group
	 * 			digit 10^1 = group type, i.e 1 = sequence, 2 = triplets, 3 = quads
	 * 			digit 10^0 = group suit, i.e 1 = mans, 2 = pins, 3 = sous, 4 = honors
	 */
	public String getGroupInfo()
	{
		int groupType = group_status(this.tile_list);
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
	 * @param in_arraylist
	 * @return
	 */
	public static String ArrayList_to_groupSN(ArrayList<Group> in_arraylist)
	{
		String return_str = "";
		for(int group_status = 3; group_status >= 1; group_status++)
		{
			for(int i = 0; i < in_arraylist.size(); i++) 
			{
				if(in_arraylist.get(i).tile_list.size() == group_status)
				{
					return_str += "(" + tileID_to_PlayVal(in_arraylist.get(i).tile_list).toString() + ")";
				}
			}
		}
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
		System.out.println("Search only triplets: " + GroupSearch.list_GroupSearch(single_suit_example, false, true));
		System.out.println("Search only triplets: " + GroupSearch.list_GroupSearch(single_suit_example, true, true));
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
			System.out.println("Search only triplets (groupSN): " + GroupSearch.list_GroupSearch(suits, false, true));
		}
		System.out.println(GroupSearch.search_all_groupSN(example_player.getPlayerHand()) + "\n\n");
		
		int[] completed_groups = {0,0,0,1,2,3,5,6,7,8,8,8};
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
			System.out.println("Search only triplets (groupSN): " + GroupSearch.list_GroupSearch(suits, false, true));
		}
		System.out.println(GroupSearch.search_all_groupSN(example_player.getPlayerHand()) + "\n\n");

	}
	public static void main(String[] args)
	{
		test();
	}
}

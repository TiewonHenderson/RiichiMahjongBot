package bot_package;

import java.util.*;

import bot_package.Player.PlayerHand;

public class Group extends Scoring
{
	//Literal tile value
	public ArrayList<Integer> tile_list = new ArrayList<Integer>();
	public boolean concealed;
	
	/*
	 * ArrayList<Integer> object Compatible Constructor for Group obj
	 */
	public Group(ArrayList<Integer> in_group, boolean concealed)
	{
		this.tile_list = new ArrayList<Integer>(in_group);
		this.concealed = concealed;
	}
	
	/*
	 * int[] object Compatible Constructor for Group obj
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
	 * 
	 * @param in_group: An ArrayList<Integer> of size 3/4 to check if valid group
	 * @return: The group type of the inputted ArrayList<Integer>, -1/0 = invalid, 1 = sequence, 2 = triplets, 3 = quads
	 */
	public static int validGroup(ArrayList<Integer> in_group)
	{
	    if(in_group.size() < 3 || in_group.size() > 4)
	    {
	        return -1;
	    }
	    //checks all the tile are in same suit, and checks if is triplet
	    boolean isTriplet = true;
	    for(int i = 0; i < in_group.size() - 1; i++)
	    {
	        if(in_group.get(i)/9 != in_group.get(i + 1)/9)
	        {
	            return -1;
	        }
	        if(in_group.get(i) != in_group.get(i + 1))
	        {
	            isTriplet = false;
	        }
	    }
	    if(in_group.size() == 4)
	    {
	        if(isTriplet)
	        {
	            return 3;
	        }
	    }
	    if(isTriplet)
	    {
	        return 2;
	    }

	    //Checks if sequence, returns 0 if passes test
	    ArrayList<Integer> sortedGroup = sortArray(in_group);
	    for(int i = 0; i < sortedGroup.size() - 1; i++)
	    {
	        if(sortedGroup.get(i) + 1 != sortedGroup.get(i + 1))
	        {
	            return -1;
	        }
	    }
	    return 1;
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
	
	/*
	 * Used to make an ArrayList<Integer> with int[]
	 */
	public static ArrayList<Integer> createArrayList(int[] in_list)
	{
		ArrayList<Integer> return_ArrayList = new ArrayList<Integer>();
		for(int i = 0; i < in_list.length; i++) return_ArrayList.add(in_list[i]);
		return return_ArrayList;
	}
	
	/*
	 * Returns all pairs in the given hand
	 */
	public static ArrayList<Integer> getPair(ArrayList<Integer> in_closeHand)
	{
		ArrayList<Integer> returnPairs = new ArrayList<Integer>();
		HashMap<Integer, Integer> tileCounter = new HashMap<Integer, Integer>();
		for(int i = 0; i < in_closeHand.size(); i++)
		{
			if(tileCounter.containsKey(in_closeHand.get(i)))
			{
				tileCounter.put(in_closeHand.get(i), 1);
			}
			else
			{
				tileCounter.replace(in_closeHand.get(i), tileCounter.get(in_closeHand.get(i)) + 1);
			}
		}
		for(int key : tileCounter.keySet())
		{
			if(tileCounter.get(key) > 1)
			{
				returnPairs.add(key);
			}
		}
		return returnPairs;
	}
	
	
	public static HashMap<String, ArrayList<Group>> getGroups(PlayerHand playHand)
	{
		ArrayList<ArrayList<Integer>> suit_list = suitDivide(playHand.getCurrentHand());
		HashMap<Integer, Integer> suit_min_Map = new HashMap<Integer, Integer>();
		
		//Process to subtract min from each suit and save to suit_min_Map
		int suit_count = 0;
		for(int suit_index = 0; suit_index < suit_list.size(); suit_index++)
		{
			if(suit_list.get(suit_index).size() != 0)
			{
				int min = suit_list.get(suit_index).get(0);
				suit_min_Map.put(suit_index, min);
				for(int i = 0; i < suit_list.get(suit_index).size(); i++) suit_list.get(suit_index).set(suit_count, suit_list.get(suit_index).get(i) - min);
			}
			else
			{
				suit_list.remove(suit_index);
				suit_index--;
			}
			suit_count++;
		}
		System.out.println(suit_min_Map);
		return new HashMap<String, ArrayList<Group>>();
	}
	
	/**
	 * 
	 * @param in_array: ASSUMES this is all in one suit
	 * @return
	 */
	public static String getGroups(ArrayList<Integer> in_array)
	{
		String return_STR = "";
		ArrayList<Integer> temp_suit = sortArray(in_array);
		if(in_array.size() <= 0) 
		{
			return "";
		}
		
		//Get suit number, minimum in array, maximum in array
		int suit = temp_suit.get(0)/9;
		int min = temp_suit.get(0);
		int max = temp_suit.get(temp_suit.size() - 1);
		
		//Convert suit to matrix
		ArrayList<Integer> matrix = new ArrayList<Integer>();
		for(int i = 0; i <= max; i++) matrix.add(0);
		for(int i = 0; i < temp_suit.size(); i++)
		{
			int tile = temp_suit.get(i) - min;
			matrix.set(tile, matrix.get(tile) + 1);
		}
		
		//Adding Unique Integers to make enough groups
		int currentIndex = 0;
		/*
		 * In case the given array doesn't have real groups, 
		 * temp_matrix is altered to not alter the original matrix
		 * 
		 * If valid group is found, matrix is altered accordingly
		 */
		ArrayList<Integer> temp_matrix = new ArrayList<Integer>(matrix);
		ArrayList<String> group_shape_list = new ArrayList<String>();
		for(int i = 0; i < temp_suit.size(); i++)
		{
			String temp_str = "";
			//Shape as set is how duplicates are not added
			SortedSet<Integer> shape = new TreeSet<Integer>();
			
			//Prevents out of bounds
			if(currentIndex >= temp_matrix.size())
			{
				break;
			}
			if(temp_matrix.get(currentIndex) == 0)
			{
				currentIndex++;
				continue;
			}
			
			//If get(currentIndex) >= 3, it means a triplet can be made from it
			if(temp_matrix.get(currentIndex) >= 3)
			{
				for(int n = 0; n < 3; n++) temp_str += Integer.toString(currentIndex);
				temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 3);
				group_shape_list.add(temp_str);
				continue;
			}
			
			//If triplet cannot be made, add current index
			shape.add(currentIndex);
			temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
			
			//i <= 2 to not exceed the "talking" tiles to currentIndex
			for(int m = 1; m <= 2; m++)
			{
				//Prevents exceeding max
				int increment_index = currentIndex + m;
				if(max - (increment_index) < 0)
				{
					break;
				}
				if(temp_matrix.get(increment_index) > 0)
				{
					shape.add(m);
					temp_matrix.set(increment_index, temp_matrix.get(increment_index) - 1);
				}
			}
			
			//Since shapes removes duplicates, and i is bounded into 1,2: This only allows sequences
			for(int tile: shape) temp_str += Integer.toString(tile);
			group_shape_list.add(temp_str);
			matrix = new ArrayList<Integer>(temp_matrix);
			System.out.println(shape);
		}
		
		/*
		 * Format = 
		 * (Complete Groups)r={Remainder Shapes}[+min]'suitchar'
		 */
		for(int i = 3; i >= 1; i--)
		{
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
			if(i == 2) 
			{
				return_STR += "}";
			}
		}
		
		/*
		 * 
		 */
		String[] suit_list = {"m", "p", "s", "z"};
		return_STR += "[+" + min + "]";
		return_STR += suit_list[suit];
		return return_STR;
	}
	
	public static ArrayList<Group> getGroups(String in_tiles)
	{
		
	}
	public static void main(String[] args)
	{
		int[] player_hand = {0,0,1,1,2,2};
		System.out.println(createArrayList(player_hand));
		System.out.println(getGroups(sortArray(createArrayList(player_hand))));
	}
}

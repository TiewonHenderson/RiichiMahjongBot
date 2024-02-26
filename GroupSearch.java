package bot_package;

import java.util.*;
import java.util.function.*;

import bot_package.Player.PlayerHand;


public class GroupSearch extends Group
{
	class GroupsAndNeededTiles
	{
		public Player currentPlayer;
		public ArrayList<Group> confirmedGroups = new ArrayList<Group>();
		public ArrayList<Group> incompGroups = new ArrayList<Group>();
		public ArrayList<Integer> remainingTiles = new ArrayList<Integer>();
		
		/**
		 * New game Constructor:
		 * This constructor acts as a new 
		 * @param inPlayer = new Player input
		 */
		public GroupsAndNeededTiles(Player inPlayer)
		{
			this.currentPlayer = inPlayer;
		}
		/**
		 * Hard manual GroupsAndNeededTiles set, usually for mid-game
		 * @param currentPlayer Existing player to input for Group Search
		 * @param confirmedGroups Groups that contain tile_list.size() >= 3 which usually signifies a completed group
		 * @param uncompGroups Any group that tile_list.size() <= 2, this however does not include invalid groups
		 * @param remainingTiles Typically floating tiles, but it will most likely be present in incompGroup
		 */
		public GroupsAndNeededTiles(Player currentPlayer, ArrayList<Group> confirmedGroups, 
									ArrayList<Group> incompGroups, ArrayList<Integer> remainingTiles)
		{
			this.currentPlayer = new Player(currentPlayer);
			this.confirmedGroups = new ArrayList<Group>(confirmedGroups);
			this.incompGroups = new ArrayList<Group>(incompGroups);
			this.remainingTiles = new ArrayList<Integer>(remainingTiles);
		}
		
		/**
		 * Cloning constructors
		 * @param clone GroupsAndNeededTiles object that wants to be cloned
		 */
		public GroupsAndNeededTiles(GroupsAndNeededTiles clone)
		{
			this.currentPlayer = new Player(clone.currentPlayer);
			this.confirmedGroups = new ArrayList<Group>(clone.confirmedGroups);
			this.incompGroups = new ArrayList<Group>(clone.incompGroups);
			this.remainingTiles = new ArrayList<Integer>(clone.remainingTiles);
		}
		
	}
	
	/**
	 * 
	 * @param in_groupSN The in_groupSN of the player's hand, searched in any way
	 * @return A double score that corresponds to the list of group's status
	 * 			digit 10^3 = amount of completed groups
	 * 			digit 10^2 = amount of pairs
	 * 			digit 10^1 = amount of incompleted groups (counts pairs)
	 * 			digit 10^0 = amount of floating tiles
	 */
	public static int progress_score(String in_groupSN)
	{

		/*
		 * Used to check how the hand has progressed
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
		 */
		ArrayList<Group> all_groups = Group.groupSN_to_ArrayList(in_groupSN);
		ArrayList<Integer> group_status_list = new ArrayList<Integer>();
		for(Group group: all_groups)
		{
			group_status_list.add(Group.group_status(group));
		}
		
		
		int progress_score = 0;
		
		for(int group_status: group_status_list)
		{
			/*
			 * Group reference
			 * -3 = invalid
			 * -2 = floating
			 * -1 = incomp sequence
			 * 0 = pair
			 * 1 = sequence
			 * 2 = triplets
			 * 3 = quads
			 * 
			 * -3: doesn't add anything
			 * -2: adds 0.001
			 * -1: adds 0.01
			 * 0: adds 0.11
			 * >=1: adds 1.0
			 */
			switch(group_status)
			{
				case -2:
					progress_score += 1;
					break;
				case -1:
					progress_score += 10;
					break;
				case 0:
					progress_score += 110;
					break;
				default:
					if(group_status >= 1 && group_status <= 3)
					{
						progress_score += 1000;
					}
					break;
			}
		}
		return progress_score;
	}
	
	/**
	 * @function
	 * 		Given a Player's PlayerHand, this method 
	 *		
	 * 		Search methods
	 * 		Brute force methods:
	 * 			Linear search (from left to right && right to left)
	 * 			Outside in search (Left out, right out, left in, right in)
	 * 		
	 * @param playHand A new PlayerHand object to search for potential groups
	 * @return A HashMap<String, String> where the String key gives information on how the Groups were searched
	 * 			String format: Index 0,1 = pairs/eye, index 2 complete/Waiting/Incomplete, index 3 way groups were search
	 * 			String: A group_SN to give information about the group
	 */
	public static HashMap<String, String> search_all_groupSN(PlayerHand playHand)
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
			 *	LL/LL/LR.../OS... [index 3-4 inclusive](LL = Linear left, LR = Linear right, OS = Outer search)
			 */
			ArrayList<String> groupSN_ID_list = new ArrayList<String>();
			
			//The returning data of HashMap<groupSN_ID, groupSN>
			HashMap<String, String> return_data = new HashMap<String, String>();
			
			//All linear searches are under here
			for(int pair_remove_key: allPossible_hands.keySet())
			{
				//Start LL = Linear Left to right (index 0 to index size() - 1)
				String temp_ID = Integer.toString(pair_remove_key);
				String temp_groupSN = "";
				
				suit_list = suitDivide(allPossible_hands.get(pair_remove_key));
				for(ArrayList<Integer> suit: suit_list)
				{
					temp_groupSN += list_GroupSearch(suit, false); //adds GroupSN for each suit
				}
				temp_ID += "LL"; //Indicates Linear left to right
				groupSN_list.add(temp_groupSN);
				groupSN_ID_list.add(temp_ID);
				
				//Used to loop for detection instead of multiple if else statements
				int[] detect_progressid = {4110, 4001, 3220, 3120};
				String[] add_progressid = {"C", "W", "W", "W"};
				
				int progress_score = progress_score(temp_groupSN);
				
				for(int id_index = 0; id_index < detect_progressid.length; id_index++)
				{
					if(progress_score == detect_progressid[id_index])
					{
						temp_ID += add_progressid[id_index];
						break;
					}
					//If progress_score not equal to any case of detect_progressid, it is 99.99% incomplete for a normal hand
					if(id_index == detect_progressid.length - 1)
					{
						temp_ID += "I";
						break;
					}
				}
				
				//Start LR = Linear right to left (index size() - 1 to index 0)
				temp_groupSN = "";
				temp_ID = Integer.toString(pair_remove_key);
				for(ArrayList<Integer> suit: suit_list)
				{
					temp_groupSN += list_GroupSearch(suit, true); //adds GroupSN for each suit
				}
				
				progress_score = progress_score(temp_groupSN);
				
				for(int id_index = 0; id_index < detect_progressid.length; id_index++)
				{
					if(progress_score == detect_progressid[id_index])
					{
						temp_ID += add_progressid[id_index];
						break;
					}
					//If progress_score not equal to any case of detect_progressid, it is 99.99% incomplete for a normal hand
					if(id_index == detect_progressid.length - 1)
					{
						temp_ID += "I";
						break;
					}
				}
				temp_ID += "LR"; //Indicates Linear right to left
				groupSN_list.add(temp_groupSN);
				groupSN_ID_list.add(temp_ID);
				
				
				/*
				 * Outside search doesn't matter for order, Left outside, right outside, left inside, right inside 
				 * This only applies to each suit, since you can create Groups in different suits
				 * is the universal order for outside search and it shouldn't matter if otherwise (Perhaps in EXTREMELY RARE SITAUTIONS)
				 */
				temp_groupSN = "";
				temp_ID = Integer.toString(pair_remove_key);
				for(ArrayList<Integer> suit: suit_list)
				{
					
				}
				
			}
		}
		else
		{
			
		}
		return null;
	}
	
	/**
	 * *WARNING* outer_search only accepts single suited ArrayList<Integer>, every tile_id must fall under the same suit
	 * 
	 * A pseudo-divide and conquer method that searches groups by dividing the list in half
	 * If the ArrayList input size is odd, the right list gets the extra tile remaining
	 * 
	 * @param in_arraylist An ArrayList<Integer> that represents tile_id of a certain hand that wants to be searched over
	 * @return The String groupSN
	 */
	public static String outer_search(ArrayList<Integer> in_arraylist)
	{
		if(in_arraylist.size() <= 3)
		{
			return list_GroupSearch(in_arraylist, false);
		}
		
		ArrayList<Integer> matrix = convert_2_matrix(new ArrayList<Integer>(in_arraylist));
		
		int half_index = matrix.size()/2;
		
		int suit = in_arraylist.get(0)/9;
		
		//Keeps track of the split matrices
		ArrayList<Integer> left_matrix = sub_ArrayList(matrix, 0, half_index);
		ArrayList<Integer> right_matrix = sub_ArrayList(matrix, half_index, matrix.size());
		
		//Keeps track of minimums for both matrices
		ArrayList<Integer> minimums = new ArrayList<Integer>();
		
		//Left matrix minimum is always the first tile
		minimums.add(tileID_to_PlayVal(in_arraylist.get(0)));
		
		/*
		 * BECAUSE convert_2_ArrayList method in Group.java will assume the minimum is the first occurrence
		 * This for loop will find the first occurrence that isn't 0
		 */
		for(int i = 0; i < right_matrix.size(); i++) 
		{
			if(right_matrix.get(i) > 0) 
			{
				minimums.add(tileID_to_PlayVal(in_arraylist.get(0) + half_index + i));
			};
		}
		
		
		ArrayList<Group> left_groups  = groupSN_to_ArrayList(list_GroupSearch(convert_2_ArrayList(left_matrix, suit, minimums.get(0)), false));
		ArrayList<Group> right_groups = groupSN_to_ArrayList(list_GroupSearch(convert_2_ArrayList(right_matrix, suit , minimums.get(1)) , false));
		
		System.out.println("Left Groups: " + left_groups);
		System.out.println("Right Groups: " + right_groups);
		
		//Add complete/incomplete groups to corresponding ArrayList<Group>
		ArrayList<ArrayList<Group>> categorized_Group = new ArrayList<ArrayList<Group>>();
		for(int i = 0; i < 2; i++) categorized_Group.add(new ArrayList<Group>());
		//Iterator to be able to iterator two ArrayList at once
		Iterator<Group> l_groups = left_groups.iterator();
		Iterator<Group> r_groups = right_groups.iterator();
		
		boolean[] has_next = {true, true};
		
		while(has_next[0] || has_next[1])
		{
			//hasNext() determines if there is a next element
			if(has_next[0] && l_groups.hasNext())
			{
				Group temp_group = new Group(l_groups.next());
				if(group_status(temp_group) > 0)
				{
					categorized_Group.get(0).add(temp_group);
				}
				else
				{
					categorized_Group.get(1).add(temp_group);
				}
			}
			if(!l_groups.hasNext())
			{
				has_next[0] = false;
			}
			if(has_next[1] && r_groups.hasNext())
			{
				Group temp_group = new Group(r_groups.next());
				if(group_status(temp_group) > 0)
				{
					categorized_Group.get(0).add(temp_group);
				}
				else
				{
					categorized_Group.get(1).add(temp_group);
				}
			}
			if(!r_groups.hasNext())
			{
				has_next[1] = false;
			}
		}
		
		ArrayList<Group> temp_all_groups = new ArrayList<Group>();
		for(ArrayList<Group> groups_list: categorized_Group) for(Group group: groups_list) temp_all_groups.add(group);
		System.out.println(ArrayList_to_groupSN(temp_all_groups));
		
		
		return null;
	}
	/**
	 * *warning* In order to not go out of bounds, if inputs are invalid, the original ArrayList is returned
	 * *warning* if end_index > start_index, then start_index will be set to end_index and vice versa
	 * 
	 * @param <T> Generic data type, preferred to be a valid standard variable data type.
	 * @param in_arraylist Any ArrayList of generic data type (i.e integer, double).
	 * @param start_index Where the sub-ArrayList should start inclusively.
	 * @param end_index Where the sub-ArrayList should end exclusively.
	 * @return A sub-ArrayList of anything inputed ArrayList data type
	 * starting at the lower index inclusive (assuming start_index) to upper index (assuming end_index) exclusive.
	 */
	public static <T> ArrayList<T> sub_ArrayList(ArrayList<T> in_arraylist, int start_index, int end_index)
	{
		ArrayList<T> return_arraylist = new ArrayList<T>(); //The return data type
		
		//Avoids Index out of bounds
		if(start_index >= in_arraylist.size() || end_index > in_arraylist.size() || 
		   start_index < 0 || end_index < 0)
		{
			return in_arraylist;
		}
		int min = start_index;
		int max = end_index;
		if(start_index > end_index)
		{
			min = end_index;
			max = start_index;
		}
		for(int i = min; i < max; i++)
		{
			return_arraylist.add(in_arraylist.get(i));
		}
		return return_arraylist;
	}
	
	/**
	 * *Warning* Assumes input for arraylist is considered concealed, thus the groupSN will end with "c"
	 * @param  in_arraylist ASSUMES this is all in one suit, index out of bounds if not
	 * @param  reverse_search Is irrelevant in triplet search since any instance of quantity 3 is taken, order doens't matter
	 * @param  only_triplets true if groups should only be triplets, default = false
	 * @return A String representation of completed groups, remainder groups, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String list_GroupSearch(ArrayList<Integer> in_arraylist, boolean reverse_search, boolean only_triplets)
	{
		if(!only_triplets){return list_GroupSearch(in_arraylist, reverse_search);}
		
		String return_STR = "";
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		int suit = in_arraylist.get(0)/9;
		
		//Convert suit to matrix
		ArrayList<Integer> matrix = convert_2_matrix(in_arraylist);
		
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
		return_STR += "[+" + tileID_to_PlayVal(sortArray(in_arraylist)).get(0) + "]";
		return_STR += Character.toString(suit_reference[suit]);
		return return_STR + "c";
	}
	
	/**
	 * 
	 * FUNCTION OVERLOADING WITH DEFAULT PARAMETER "only_triplets" SET TO "false"
	 * *Warning* Assumes input for arraylist is considered concealed, thus the groupSN will end with "c"
	 * @param  in_arraylist ASSUMES this is all in one suit
	 * @param  reverse_search Whether or not the search the inputted array from index 0 to last element or last element to index 0
	 * @return A String representation of completed groups, remainder groups, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String list_GroupSearch(ArrayList<Integer> in_arraylist, boolean reverse_search)
	{
		String return_STR = "";
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		//Get suit number, minimum in array, maximum in array
		int suit = in_arraylist.get(0)/9;
		
		//If given honors list, return GroupSearch with only_triplets as true
		if(suit == 3) {return list_GroupSearch(in_arraylist, false, true);}
		
		ArrayList<Integer> temp_suit = tileID_to_PlayVal(sortArray(in_arraylist));
		int total_tiles = temp_suit.size();
		int play_val_min = temp_suit.get(0);

		//Convert suit to matrix
		ArrayList<Integer> matrix = convert_2_matrix(in_arraylist);
		
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
		return return_STR + "c";
	}
	
	public static void main(String[] args)
	{
		ArrayList<Group> random_groups = new ArrayList<Group>();
		
	}
}

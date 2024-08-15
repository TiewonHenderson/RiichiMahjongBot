package bot_package_v2;

import java.util.*;
import java.text.MessageFormat;

import bot_package_v2.MJ_tools.*;
import bot_package_v2.Player.Visible_hand;

public class Group_Search extends Group
{
	/**
	 * 
	 * @param in_groupSN The in_groupSN of the player's hand, searched in any way
	 * @return A double score that corresponds to the list of group's status
	 * 			digit 10^3 = amount of completed groups
	 * 			digit 10^2 = amount of pairs
	 * 			digit 10^1 = amount of incompleted groups (counts pairs)
	 * 			digit 10^0 = amount of floating tiles
	 */
	public static int progressScore(String in_groupSN)
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
		ArrayList<Group> all_groups = MJ_Hand_tools.groupSN_to_ArrayList(in_groupSN);
		ArrayList<Group_type> group_status_list = new ArrayList<Group_type>();
		for(Group group: all_groups)
		{
			group_status_list.add(group.groupStatus());
		}
		
		int progress_score = 0;
		
		for(Group_type group_status: group_status_list)
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
			 * -3: 	doesn't add anything
			 * -2: 	adds 1
			 * -1: 	adds 10
			 * 0: 	adds 110
			 * >=1: adds 1000
			 */
			switch(group_status)
			{
				case FLOAT_TILE:
					progress_score += 1;
					break;
				case INCOMP_SEQ:
					progress_score += 10;
					break;
				case PAIR:
					progress_score += 110;
					break;
				default:
					if(group_status.ordinal() >= 4 && group_status.ordinal() <= 7)
					{
						progress_score += 1000;
					}
					break;
			}
		}
		return progress_score;
	}
	
	/**
	 * "C" = Complete
	 * "W" = Waiting
	 * "I" = Incomplete
	 * 
	 * @param in_groupSN The input groupSN to see how the hand is progressing
	 * @return A string representation of the progress of the hand
	 */
	public static String getHandStatus(String in_groupSN)
	{
		//Used to loop for detection instead of multiple if else statements
		int[] detect_progressid = {4110, 4001, 3220, 3120};
		String[] add_progressid = {"C", "W", "W", "W"};
		
		int progress_score = progressScore(in_groupSN);
		for(int id_index = 0; id_index < detect_progressid.length; id_index++)
		{
			if(progress_score == detect_progressid[id_index])
			{
				return add_progressid[id_index];
			}
			//If progress_score not equal to any case of detect_progressid, it is 99.99% incomplete for a normal hand
			if(id_index == detect_progressid.length - 1)
			{
				return "I";
			}
		}
		return "I"; //If nothing was detected, THE HAND IS PROBABLY NOT COMPLETE
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
	public static HashMap<String, String> searchAllGroupSN(Visible_hand playHand, boolean only_isolatePair)
	{
		ArrayList<ArrayList<Tile>> suit_list = MJ_Hand_tools.suitDivide(playHand.getVisibleHand());
		
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
		ArrayList<Integer> honor_matrix = MJ_Hand_tools.convert_to_matrix(suit_list.get(suit_list.size() - 1));
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
			
			ArrayList<Integer> suit_matrix = MJ_Hand_tools.convert_to_matrix(suit_list.get(suit));
			
			//max is reference to not get out of bounds with increment up to +2 when checking talking tiles
			//add min = by play_val, i.e 4p = 12 [tile_id] = [index] + [min = 4] + [suit(p) * 9 = 1 * 9]
			
			int min = suit_list.get(suit).get(0).getTileID() - (suit * 9);
			
			for(int tile_index = 0; tile_index < suit_matrix.size(); tile_index++)
			{
				boolean isolate = true;
				boolean isPair = true;
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
		
		//Where the hands gets pairs removed / no pairs removed
		HashMap<Integer, ArrayList<Tile>> allPossible_hands = new HashMap<Integer, ArrayList<Tile>>();
		allPossible_hands.put(-1, new ArrayList<Tile>(playHand.getVisibleHand())); //No pairs removed
		
		//The returning data of HashMap<groupSN_ID, groupSN>
		HashMap<String, String> return_data = new HashMap<String, String>();
		
		//If pairs are removed, removes isolate pairs first
		if(isolatePairs.length() > 0 && only_isolatePair)
		{
			int start_index = 0;
			for(int i = 0; i < isolatePairs.length(); i++)
			{
				if(isolatePairs.charAt(i) == ',')
				{
					//Substring = inclusive start index to exclusive i(where the ',' is)
					int remove_pair = Integer.parseInt(isolatePairs.substring(start_index,i)); //Convert the pair from String to int
					int remove_counter = 0; //Confirms only the pair was removed
					ArrayList<Tile> temp_hand = new ArrayList<Tile>(playHand.getVisibleHand()); //Make sure copy is altered, not actual hand
					for(int j = 0; j < temp_hand.size(); j++)
					{
						if(remove_counter == 2)
						{
							break;
						}
						if(temp_hand.get(j).getTileID() == remove_pair)
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
			 *
			 * 	groupSN_ID formate: 
			 * 	Eyes [index 0-1 inclusive](single digit tile_id follow by 0, i.e 7m = 06 tile_id): 
			 * 	C/W/I [index 2 inclusive](complete/Waiting/Incomplete, index 2)
			 *	LL/LL/LR.../OS... [index 3-4 inclusive](LL = Linear left, LR = Linear right, OS = Outer search)
			 */
			
			//All searches are under here, Where each ISOLATE pair are removed
			for(int pair_remove_key: allPossible_hands.keySet())
			{
				ArrayList<ArrayList<String>> add_searched = searchGroups(pair_remove_key, allPossible_hands.get(pair_remove_key));
				for(ArrayList<String> searched_info: add_searched) return_data.put(searched_info.get(0), searched_info.get(1));
			}
			return return_data;
		}
//			else if(isolatePairs.length() == 0 && only_isolatePair)
//			{
//				return new HashMap<String, String>();
//			}
		else
		{
			//Copied from isolate pair, see above for reference
			int start_index = 0;
			for(int i = 0; i < pairs.length(); i++)
			{
				if(pairs.charAt(i) == ',')
				{
					int remove_pair = Integer.parseInt(pairs.substring(start_index,i));
					int remove_counter = 0;
					ArrayList<Tile> temp_hand = new ArrayList<Tile>(playHand.getVisibleHand());
					for(int j = 0; j < temp_hand.size(); j++)
					{
						if(remove_counter == 2)
						{
							break;
						}
						if(temp_hand.get(j).getTileID() == remove_pair)
						{
							temp_hand.remove(j);
							remove_counter++;
							j--;
						}
					}
					if(remove_counter == 2)
					{
						allPossible_hands.put(remove_pair, temp_hand);
					}
					start_index = i + 1;
					continue;
				}
			}
			for(int pair_remove_key: allPossible_hands.keySet())
			{
				ArrayList<ArrayList<String>> add_searched = searchGroups(pair_remove_key, allPossible_hands.get(pair_remove_key));
				for(ArrayList<String> searched_info: add_searched) return_data.put(searched_info.get(0), searched_info.get(1));
			}
			return return_data;
		}
	}
	
	/**
	 * OVERLOADED Function where default search_all_groupSN parameter "only_isolatePair" is true
	 * If we search groups only with isolate pair removed since they can contribute nothing else then become a triplet
	 * It can save resources instead of searching all pairs removed
	 * @param playHand A new PlayerHand object to search for potential groups
	 * @return A HashMap<String, String> where the String key gives information on how the Groups were searched
	 * 			String format: Index 0,1 = pairs/eye, index 2 complete/Waiting/Incomplete, index 3 way groups were search
	 * 			String: A group_SN to give information about the group
	 */
	public static HashMap<String, String> searchAllGroupSN(Visible_hand playHand)
	{
		return searchAllGroupSN(playHand, true);
	}
	
	/**
	 * 
	 * @param playHand A new PlayerHand object to search for any unique pairs
	 * @return A single String that represents Unique pairs as groups with remainders either being single Tiles or overflow Tiles
	 * 		   (Overflow Tiles being pairs created from triplets or undeclared quads)
	 */
	public static String searchAllPairs(Visible_hand playHand)
	{
		ArrayList<ArrayList<Tile>> suitList = MJ_Hand_tools.suitDivide(playHand.getVisibleHand());
		
		String returnGroupSN = "";
		
		for(ArrayList<Tile> suitedAL: suitList)
		{
			returnGroupSN += listPairsSearch(suitedAL);
		}
		
		return returnGroupSN + "ckqo";
	}
	
	/**
	 * @Function How this program works is by perform two linear searches and one "binary" search
	 * 			 The two linear search involves searchings in two orders, index 0 -> size() - 1, size() - 1 -> index 0
	 * 			 The "binary" search will cut the ArrayList<Tile> into 2 searches in two given arrays
	 * @param pair What pair was removed, if input is -1, then the algorithm assumes no pairs were removed
	 * @param given_hand The updated ArrayList<Tile> hand either pair is removed or remains
	 * @return An ArrayList<ArrayList<String>> where each element ArrayList<String> has two index, the index 0 is the 
	 * 		   groupSN_ID to represent index 1 which would be the groupSN
	 */
	public static ArrayList<ArrayList<String>> searchGroups(int pair, ArrayList<Tile> given_hand)
	{
		ArrayList<ArrayList<String>> return_array = new ArrayList<ArrayList<String>>();
		ArrayList<Tile> remove_tile = new ArrayList<Tile>(); //Adds pair if it needs to be removed
		for(int i = 0; i < 2; i++) remove_tile.add(new Tile(pair));
		
		//Start LL = Linear Left to right (index 0 to index size() - 1)
		String temp_ID = addInt(pair);
		String temp_groupSN = "";
		
		ArrayList<ArrayList<Tile>> suit_list = MJ_Hand_tools.suitDivide(given_hand);
		ArrayList<String> add_groupSN = new ArrayList<String>();
		for(ArrayList<Tile> suit: suit_list)
		{
			temp_groupSN += listGroupSearch(suit, false); //adds GroupSN for each suit
		}
		temp_groupSN += "ckqo";
		temp_groupSN = addItemTo_groupSN(temp_groupSN, remove_tile); //Adds the pair back if removed into groupSN to check status
		temp_ID += getHandStatus(temp_groupSN); //Will add the index that represents progress status
		temp_ID += "LL"; //Indicates Linear left to right
		add_groupSN.add(temp_ID);
		add_groupSN.add(temp_groupSN);
		return_array.add(add_groupSN); //Put the result return_array
		
		
		//Start LR = Linear right to left (index size() - 1 to index 0)
		add_groupSN = new ArrayList<String>();
		temp_groupSN = "";
		temp_ID = addInt(pair);
		for(ArrayList<Tile> suit: suit_list)
		{
			temp_groupSN += listGroupSearch(suit, true); //adds GroupSN for each suit
		}
		temp_groupSN += "ckqo";
		temp_groupSN = addItemTo_groupSN(temp_groupSN, remove_tile);
		temp_ID += getHandStatus(temp_groupSN);
		temp_ID += "LR"; //Indicates Linear right to left
		add_groupSN.add(temp_ID);
		add_groupSN.add(temp_groupSN);
		return_array.add(add_groupSN); //Put the result return_array
		
		/*
		 * Outside search doesn't matter for order, Left outside, right outside, left inside, right inside 
		 * This only applies to each suit, since you can create Groups in different suits
		 * is the universal order for outside search and it shouldn't matter if otherwise (Perhaps in EXTREMELY RARE SITAUTIONS)
		 * 
		 * Its debatable whether to recurse this method as many groups could be left out with only
		 * 1 iteration of divide and conquer
		 */
		add_groupSN = new ArrayList<String>();
		temp_groupSN = "";
		temp_ID = addInt(pair);
		for(ArrayList<Tile> suit: suit_list)
		{
			temp_groupSN += outerSearch(suit);
		}
		temp_groupSN = MJ_Hand_tools.addIndicators(temp_groupSN);
		temp_groupSN = addItemTo_groupSN(temp_groupSN, remove_tile);
		temp_ID += getHandStatus(temp_groupSN);
		temp_ID += "OS";
		add_groupSN.add(temp_ID);
		add_groupSN.add(temp_groupSN);
		return_array.add(add_groupSN); //Put the result return_array
		
		return return_array;
	}
	/**
	 * NEED HEAVY OPTIMIZATION WITH STRING MANIPUTATION
	 */
	public static String addItemTo_groupSN(String group_sn, ArrayList<Tile> new_tiles, boolean declared, boolean concealed)
	{
		//Will check if there no tiles to add into, or if the tile is invalid by being < 0 or > 33
		if(new_tiles.size() <= 0) {return group_sn;}
		for(int i = 0; i < new_tiles.size(); i++) if(new_tiles.get(i).getTileID() < 0 || new_tiles.get(i).getTileID() > 33) {return group_sn;}
		
		ArrayList<Group> temp_groups = MJ_Hand_tools.groupSN_to_ArrayList(group_sn);
		temp_groups.add(new Group(new_tiles, declared, concealed));
		return MJ_Hand_tools.ArrayList_to_groupSN(temp_groups);
	}
	
	/**
	 * NEED HEAVY OPTIMIZATION WITH STRING MANIPUTATION
	 * Overloading the addItemTo_groupSN to make boolean concealed to be defaulted to true and declared false
	 */
	public static String addItemTo_groupSN(String group_sn, ArrayList<Tile> new_tiles)
	{
		return addItemTo_groupSN(group_sn, new_tiles, false, true);
	}
	
	/**
	 * 
	 * @param in_integer A given Integer that wants to be added to groupSN ID, this would mean the integer needs to at minimum
	 * be 2 digits long. The number cannot be negative, if pair wasn't remove, just add -1 to String
	 * @return A string value with two index that represents a number, returns -1 for any negative numbers
	 */
	public static String addInt(int in_integer)
	{
		if(in_integer <= -1)
		{
			return "-1";
		}
		if(in_integer/10 == 0)
		{
			return "0" + Integer.toString(in_integer);
		}
		return Integer.toString(in_integer);
	}
	
	/**
	 * *WARNING* outer_search only accepts single suited ArrayList<Integer>, every tile_id must fall under the same suit
	 * 
	 * A pseudo-divide and conquer method that searches groups by dividing the list in half
	 * If the ArrayList input size is odd, the right list gets the extra tile remaining
	 * 
	 * @param in_arraylist An ArrayList<Tile> that represents Tile objects of a certain hand that wants to be searched over
	 * @return The String groupSN
	 */
	public static String outerSearch(ArrayList<Tile> in_arraylist)
	{
		if(in_arraylist.size() <= 3)
		{
			return listGroupSearch(in_arraylist, false);
		}
		
		ArrayList<Tile> sorted_Tiles = MJ_Hand_tools.sortTiles(in_arraylist, false);
		ArrayList<Integer> matrix = MJ_Hand_tools.convert_to_matrix(in_arraylist);
		
		int half_index = matrix.size()/2;
		
		int suit = sorted_Tiles.get(0).getTileID()/9;
		
		//Keeps track of the split matrices
		ArrayList<Integer> left_matrix = MJ_Hand_tools.subArrayList(matrix, 0, half_index);
		ArrayList<Integer> right_matrix = MJ_Hand_tools.subArrayList(matrix, half_index, matrix.size());

		//Keeps track of minimums for both matrices
		ArrayList<Integer> minimums = new ArrayList<Integer>();
		
		//Left matrix minimum is always the first tile
		minimums.add(Tile.tileID_to_PlayVal(sorted_Tiles.get(0)));
		
		/*
		 * BECAUSE convert_2_ArrayList method in Group.java will assume the minimum is the first occurrence
		 * This for loop will find the first occurrence that isn't 0
		 */
		for(int i = 0; i < right_matrix.size(); i++) 
		{
			if(right_matrix.get(i) > 0) 
			{
				minimums.add(Tile.tileID_to_PlayVal(sorted_Tiles.get(0).getTileID() + half_index + i));
			};
		}
		
		//Add complete/incomplete groups to corresponding ArrayList<Group>
		ArrayList<ArrayList<Group>> categorized_Group = new ArrayList<ArrayList<Group>>();
		for(int i = 0; i < 2; i++) categorized_Group.add(new ArrayList<Group>());
		
		//Iterator to be able to iterator two ArrayList at once
		Iterator<Group> l_groups = MJ_Hand_tools.groupSN_to_ArrayList(listGroupSearch(MJ_Hand_tools.convert_to_ArrayList(left_matrix, suit, minimums.get(0)), false) + "c").iterator();
		Iterator<Group> r_groups = MJ_Hand_tools.groupSN_to_ArrayList(listGroupSearch(MJ_Hand_tools.convert_to_ArrayList(right_matrix, suit , minimums.get(1)) , true) + "c").iterator();
		
		while(l_groups.hasNext() || r_groups.hasNext())
		{
			//hasNext() determines if there is a next element
			if(l_groups.hasNext())
			{
				Group temp_group = new Group(l_groups.next());
				if(temp_group.groupStatus().ordinal() > 3)
				{
					categorized_Group.get(0).add(temp_group);
				}
				else
				{
					categorized_Group.get(1).add(temp_group);
				}
			}
			if(r_groups.hasNext())
			{
				Group temp_group = new Group(r_groups.next());
				if(temp_group.groupStatus().ordinal() > 3)
				{
					categorized_Group.get(0).add(temp_group);
				}
				else
				{
					categorized_Group.get(1).add(temp_group);
				}
			}
		}
		ArrayList<Tile> remainder_tiles = new ArrayList<Tile>();
		for(Group incomp_groups : categorized_Group.get(1)) 
		{
			if(incomp_groups.getGroupTiles().size() < 3)
			{
				for(Tile tile: incomp_groups.getGroupTiles()) remainder_tiles.add(tile);
			}
		}
		if(sorted_Tiles.size() > remainder_tiles.size())
		{
			/*
			 * Recursion to see if remainder groups can upgrade
			 * see if floating tile -> incomplete group
			 * see if incomplete group -> complete group
			 * 
			 * Recursion stops once no upgrades has been made
			 * 
			 * Doesn't take account of tiles used, so just replaces old remainder tiles with new
			 * If there were completed groups in remainder groups index, it will be detected by ArrayList_to_groupSN() function
			 * For recursion, just add Groups with less then 3 tiles
			 */
			categorized_Group.set(1, MJ_Hand_tools.groupSN_to_ArrayList(outerSearch(remainder_tiles) + "c"));
		}
		
		//Will not take into consideration newly drawn Tiles
		ArrayList<Group> temp_all_groups = new ArrayList<Group>();
		for(ArrayList<Group> groups_list: categorized_Group) 
		{
			for(Group group: groups_list) 
			{
				temp_all_groups.add(group);
			}
		}

		return MJ_Hand_tools.ArrayList_to_groupSN(temp_all_groups);
	}
	
	/**
	 * *Warning* Assumes input for arraylist is considered concealed, thus the groupSN will end with "c"
	 * *Warning* Not functional with honor suit since you cannot have honor sequences
	 * @param  in_arraylist ASSUMES this is all in one suit Tiles, index out of bounds if not
	 * @return A String representation of completed Sequences, remainder incomplete sequences or floating tiles, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String listSequenceSearch(ArrayList<Tile> in_arraylist, boolean reverse_search)
	{
		String return_String = "";
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		//Get suit number, minimum in array, maximum in array
		int suit = in_arraylist.get(0).getTileID()/9;
		
		//If given honors list, return Group_Search with only_triplets as true
		if(suit == 3) {return listTripletSearch(in_arraylist);}
		
		ArrayList<Tile> temp_suit = MJ_Hand_tools.sortTiles(in_arraylist, false);
		int total_tiles = temp_suit.size();
		int play_val_min = temp_suit.get(0).getTileID();

		//Convert suit to matrix
		ArrayList<Integer> matrix = MJ_Hand_tools.convert_to_matrix(in_arraylist);
		
		//Index that refers to the play_val integer in the matrix (a pointer)
		int currentIndex = 0;
		int direction_search = 1;
		if(reverse_search)
		{
			currentIndex = matrix.size() - 1;
			direction_search = -1;
		}
		
		String remainder_groups = "r={"; //To add to last return_String
		ArrayList<ArrayList<Integer>> incomp_group_list = new ArrayList<ArrayList<Integer>>(); //To sort the incomplete groups in order
		while(total_tiles > 0)
		{
			//How the while loop increments/decrements
			if(matrix.get(currentIndex) == 0)
			{
				currentIndex += direction_search;
			}
			
			if(currentIndex >= matrix.size() || currentIndex < 0) {break;} //Out of bounds
			if(matrix.get(currentIndex) == 0) {continue;} //No tiles to offer
			
			int seq_case = 1;
			ArrayList<Integer> temp_list = new ArrayList<Integer>();
			for(int i = 0; i < 3; i++)
			{
				//Condition for out of bounds
				if(currentIndex + (direction_search * i) >= matrix.size() || currentIndex + (direction_search * i) < 0)
				{
					switch(temp_list.size())
					{
						case 1:
							seq_case = 5;
							break;
						case 2:
							seq_case = 4;
							break;
					}
					break;
				}
				//Index out of bounds override these seq_case values since no cell > no tiles to offer (in term of importance)
				if(matrix.get(currentIndex + (direction_search * i)) == 0)
				{
					switch(i)
					{
						case 1:
							seq_case = 3;
							break;
						case 2:
							if(seq_case == 3) {seq_case = 5; break;} //This indicates 100, which would have floating tile
							seq_case = 2;
							break;
					}
				}
				temp_list.add(matrix.get(currentIndex + (direction_search * i))); //Adds tile amount into temp_list
			}
			
			//111 assigned as 1
			
			//110 assigned as 2
			
			//101 assigned as 3, extend in here
				
			//10end assigned as 4
			
			//1end assigned as 5
			SortedSet<Integer> sorted_index = new TreeSet<Integer>();
			switch (seq_case)
			{
				case 1:
					return_String += "(";
					for(int i = 0; i < 3; i++) 
					{
						/*
						 * This specific function as long as index is not out of bounds or sorted_index set is not invalid or already added
						 * will return 1 to signify the tile was added, and decrement total_tiles
						 * Will return 0 if otherwise and no tile is removed
						 */
						total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex + (direction_search * i));
					}
					if(sorted_index.size() == 3)
					{
						for(int tile: sorted_index) return_String += tile;
						return_String += ")";
					}
					break;
				case 2:
					while(matrix.get(currentIndex) > 0)
					{
						for(int i = 0; i < 2; i++)
						{
							if(matrix.get(currentIndex + (direction_search * i)) > 0)
							{
								total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex + (direction_search * i));
							}
						}
						if(sorted_index.size() > 0)
						{
							incomp_group_list.add(new ArrayList<Integer>(sorted_index));
							sorted_index = new TreeSet<Integer>();
						}
					}
					break;
				case 3:
					//101end
					if(currentIndex + (direction_search * 3) >= matrix.size() || currentIndex + (direction_search * 3) < 0)
					{
						int remaining_tiles = 0;
						for(int i = 0; i < 3; i++) remaining_tiles += matrix.get(currentIndex + (i * direction_search));
						while(remaining_tiles > 0)
						{
							for(int i = 0; i < 3; i++)
							{
								if(matrix.get(currentIndex + (direction_search * i)) > 0)
								{
									if(tileExchangeManager(matrix, sorted_index, currentIndex + (direction_search * i)) == 1)
									{
										remaining_tiles--;
										total_tiles--;
									}
								}
							}
							//Makes sure to add a list that actually has tiles within
							if(sorted_index.size() > 0)
							{
								incomp_group_list.add(new ArrayList<Integer>(sorted_index));
								sorted_index = new TreeSet<Integer>();
							}
						}
						
					}
					else
					{
						//1012, should not use the outer tile as middle wait (prefer next double sided wait at worst)
						if(matrix.get(currentIndex + (direction_search * 3)) >= matrix.get(currentIndex + (direction_search * 2)))
						{
							while(matrix.get(currentIndex) > 0)
							{
								total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex);
								if(sorted_index.size() > 0)
								{
									incomp_group_list.add(new ArrayList<Integer>(sorted_index));
									sorted_index = new TreeSet<Integer>();
								}
							}
						}
						//1021
						else
						{
							int difference = matrix.get(currentIndex + (direction_search * 2)) - matrix.get(currentIndex + (direction_search * 3));
							while(difference > 0)
							{
								difference = matrix.get(currentIndex + (direction_search * 2)) - matrix.get(currentIndex + (direction_search * 3));
								//No current tiles to offer, break possible infinite loop
								if(matrix.get(currentIndex) <= 0 || matrix.get(currentIndex + (direction_search * 2)) <= 0)
								{
									break;
								}
								//No need for loop as index 1 is guaranteed amt == 0
								total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex);
								total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex + (direction_search * 2));
								if(sorted_index.size() > 0)
								{
									incomp_group_list.add(new ArrayList<Integer>(sorted_index));
									sorted_index = new TreeSet<Integer>();
								}
							}
						}
					}
					break;
				case 4:
					int remaining_tiles = 0;
					for(int i = 0; i < 2; i++) remaining_tiles += matrix.get(currentIndex + (i * direction_search));
					while(remaining_tiles > 0)
					{
						for(int i = 0; i < 2; i++)
						{
							if(matrix.get(currentIndex + (i * direction_search)) > 0)
							{
								total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex + (direction_search * i));
								remaining_tiles--;
							}
						}
						if(sorted_index.size() > 0)
						{
							incomp_group_list.add(new ArrayList<Integer>(sorted_index));
							sorted_index = new TreeSet<Integer>();
						}
					}
					break;
				case 5:
					while(matrix.get(currentIndex) > 0)
					{
						total_tiles -= tileExchangeManager(matrix, sorted_index, currentIndex);
						if(sorted_index.size() > 0)
						{
							incomp_group_list.add(new ArrayList<Integer>(sorted_index));
							sorted_index = new TreeSet<Integer>();
						}
					}
					break;
			}
		}
		for(int i = 2; i > 0; i--)
		{
			for(int j = 0; j < incomp_group_list.size(); j++)
			{
				if(incomp_group_list.get(j).size() == i)
				{
					String temp_str = "(";
					for(int tile: incomp_group_list.get(j))
					{
						temp_str += Integer.toString(tile);
					}
					temp_str += ")";
					remainder_groups += temp_str;
				}
			}
		}
		return return_String + remainder_groups + "}[" + play_val_min + "]" + Tile.suit_reference[suit];
	}
	
	/**
	 * 
	 * @param in_matrix This input matrix of single suit ArrayList<Integer> will be altered (remove tile at index)
	 * @param in_set This input set will be altered to add the index being removed from matrix
	 * @param in_index The index (reference to tile) that wants to be added to set and removed from matrix
	 * @return returns 1 if removed + added successfully, returns 0 if already in set or out of bounds or set size exceeds normal shape
	 */
	public static int tileExchangeManager(ArrayList<Integer> in_matrix, SortedSet<Integer> in_set, int in_index)
	{
		if(in_index < 0 || in_index >= in_matrix.size() || in_set.size() > 3)
		{
			return 0;
		}
		for(int index: in_set)
		{
			if(in_index == index)
			{
				return 0;
			}
		}
		in_set.add(in_index);
		in_matrix.set(in_index, in_matrix.get(in_index) - 1);
		return 1;
	}
	
	/**
	 * @param  in_arraylist ASSUMES this is all in one suit Tiles, index out of bounds if not
	 * @return A String representation of completed triplets, remainder pairs/floating tiles, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String listTripletSearch(ArrayList<Tile> in_arraylist)
	{
		
		String return_STR = "";
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		int suit = in_arraylist.get(0).getTileID()/9;
		
		//Convert suit to matrix
		ArrayList<Integer> matrix = MJ_Hand_tools.convert_to_matrix(in_arraylist);
		
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
		return_STR += "[+" + Tile.tileID_to_PlayVal(MJ_Hand_tools.sortTiles(in_arraylist, false).get(0)) + "]";
		return_STR += Character.toString(Tile.suit_reference[suit]);
		return return_STR;
	}
	
	/**
	 * 
	 * @param  in_arraylist ASSUMES this is all in one suit Tiles
	 * @param  reverse_search Whether or not the search the inputted array from index 0 to last element or last element to index 0
	 * @return A String representation of completed groups, remainder groups, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Complete Groups)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String listGroupSearch(ArrayList<Tile> in_arraylist, boolean reverse_search)
	{
		String return_STR = "";
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		//Get suit number, minimum in array, maximum in array
		int suit = in_arraylist.get(0).getTileID()/9;
		
		//If given honors list, return Group_Search with only_triplets as true
		if(suit == 3) {return listTripletSearch(in_arraylist);}
		
		ArrayList<Integer> temp_suit = MJ_Hand_tools.Tiles_to_PlayVal(MJ_Hand_tools.sortTiles(in_arraylist, false));
		int total_tiles = temp_suit.size();
		int play_val_min = temp_suit.get(0);

		//Convert suit to matrix
		ArrayList<Integer> matrix = MJ_Hand_tools.convert_to_matrix(in_arraylist);
		
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
				matrix = new ArrayList<Integer>(temp_matrix);
				total_tiles -= 3;
				group_shape_list.add(temp_str);
				continue;
			}
			
			//If triplet cannot be made, add current index
			shape.add(currentIndex);
			temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
			total_tiles--;
			
			int m = 0;
			boolean can_sequence = true;
			ArrayList<Integer> forwardTile_amt = new ArrayList<Integer>();
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
				if(increment_index < 0 || increment_index >= temp_matrix.size())
				{
					break;
				}
				forwardTile_amt.add(temp_matrix.get(increment_index));
			}
			/*
			 * This exception typically means there's no more tiles to search through,
			 * Whatever is remained will becomes the incomplete group
			 */
			if(forwardTile_amt.size() == 0)
			{
				if(temp_matrix.get(currentIndex) > 0)
				{
					while(temp_matrix.get(currentIndex) > 0)
					{
						temp_str += Integer.toString(currentIndex);
						temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
						total_tiles--;
					}
					for(int tile: shape) temp_str += Integer.toString(tile);
					group_shape_list.add(temp_str);
					matrix = new ArrayList<Integer>(temp_matrix);
				}
				else
				{
					for(int tile: shape) temp_str += Integer.toString(tile);
					group_shape_list.add(temp_str);
					matrix = new ArrayList<Integer>(temp_matrix);
				}
				continue;
			}
			/*
			 * If matrix would allow sequence or not, This will ensure that remainder shapes
			 * will not take triplets if they are presents in talking tile range
			 */
			for(int k = 0; k < forwardTile_amt.size(); k++)
			{
				if(forwardTile_amt.get(k) <= 0 || forwardTile_amt.size() < 2)
				{
					can_sequence = false;
					break;
				}
			}
			if(can_sequence)
			{
				m = 0;
				for(int k = 0; k < forwardTile_amt.size(); k++)
				{
					//Decrement if reverse search, increment otherwise
					if(reverse_search) {m--;} else if(!reverse_search) {m++;}
					
					//Adds sequence to the group_shape_list
					shape.add(currentIndex + m);
					temp_matrix.set(currentIndex + m, temp_matrix.get(currentIndex + m) - 1);
					total_tiles--;
				}
				for(int tile: shape) temp_str += Integer.toString(tile);
				group_shape_list.add(temp_str);
				matrix = new ArrayList<Integer>(temp_matrix);
				continue;
			}
			else //Since pairs are not looked for in above loop, this condition will fill that gap
			{
				//Checks to make sure no talking tiles can be triplets, no triplets == make remainder shape
				m = 0;
				boolean break_loop = false;
				for(int k = 0; k < forwardTile_amt.size(); k++)
				{
					if(reverse_search) {m--;} else if(!reverse_search) {m++;}
					switch(forwardTile_amt.get(k))
					{
						case 3: //No seq == only triplet
							break_loop = true; //Triplet found, don't add anything from it
							break;
						case 0: //Cannot include in default as cannot add no existing tiles
							//Couldn't make sequence, so shape.size() == 2 is in theory best incomplete group status
							if(temp_matrix.get(currentIndex) > 0) //There are more tiles in same index, as group as pair instead
							{
								break_loop = true;
								break;
							}
							else
							{
								/*
								 * Dilemma here:
								 * 1,2,0 = 1,2 shape since 1,2 cannot make sequence with no after previous tiles
								 * 1,0,3 ?= 1,3 shape BUT it depends if 4 amt == 0, it can be possible if 1,0,3,4,5 and the incomplete groups
								 * takes away from an actual completed shape, a simple way to check this is by checking that index after and
								 * only add this as hovering tile.
								 */
								int further_index = currentIndex;
								if(reverse_search) {further_index -= 3;} else if(!reverse_search) {further_index += 3;}
								if(further_index < 0 || further_index >= matrix.size())
								{
									continue;
								}
								if(temp_matrix.get(further_index) > 0)
								{
									//There are talking tiles around currentIndex + 2, currentIndex should safely be a hovering tile
									break_loop = true;
									break;
								}
								//However if there cannot create group in currentIndex + m, then it can be used for incomplete group
							}
							break;
						/*
						 * Only runs if 
						 * 1,1,0
						 * 1,2,0
						 * 1,0,1
						 * 1,0,2
						 */
						default: //Overflow of single tile, can make incomplete seq group
							shape.add(currentIndex + m);
							temp_matrix.set(currentIndex + m, temp_matrix.get(currentIndex + m) - 1);
							total_tiles--;
							break_loop = true;
							break;
					}
					if(break_loop)
					{
						if(shape.size() == 1 && temp_matrix.get(currentIndex) > 0)
						{						
							//sets remove duplicates, but there is only one element
							while(temp_matrix.get(currentIndex) > 0)
							{
								temp_str += currentIndex;
								temp_matrix.set(currentIndex, temp_matrix.get(currentIndex) - 1);
								total_tiles--;
							}
						}
						break;
					}
				}
			}
			//Any other possible case are false, this would be a hovering tile
			for(int tile: shape) temp_str += Integer.toString(tile);
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
		return_STR += Character.toString(Tile.suit_reference[suit]);
		return return_STR;
	}
	
	/**
	 * @note No undeclared quads/triplets will be counted as another pair, it will just be put within Remainder shapes
	 * @param  in_arraylist ASSUMES this is all in one suit Tiles, index out of bounds if not
	 * @return A String representation of unique pairs, remainder floating tiles, increment minimum, and the suit
	 * 		   The String representation is called GroupSN = Group String Notation
	 * 		   Format: (Unique pairs)r={(Remainder Shapes)}[+min]'suitchar'
	 */
	public static String listPairsSearch(ArrayList<Tile> in_arraylist)
	{
		if(in_arraylist.size() <= 0) 
		{
			return "";
		}
		
		//Add to final groupSN for pairs
		int min = MJ_Hand_tools.sortTiles(in_arraylist, false).get(0).getPlayValue();
		int suit = in_arraylist.get(0).getTileID()/9;
		
		//Convert suit to matrix
		ArrayList<Integer> matrix = MJ_Hand_tools.convert_to_matrix(in_arraylist);
		
		//Sort uniquePairs to add for later
		Queue<String> uniquePairs = new LinkedList<String>();
		Queue<String> remainders = new LinkedList<String>();
		
		//Iterate for any occurence of >= 2
		for(int i = 0; i < matrix.size(); i++)
		{
			switch(matrix.get(i))
			{
				case 4:
					remainders.add("(" + (min - 1 + i) + ")");
				case 3:
					remainders.add("(" + (min - 1 + i) + ")");
				case 2:
					uniquePairs.add(MessageFormat.format("({0}{0})", (min - 1 + i)));
					break;
				case 1:
					remainders.add("(" + (min - 1 + i) + ")");
					break;
			}
		}
		
		String returnSTR = "";
		
		//Adds all the pair "Groups" to the final groupSN
		while(uniquePairs.peek() != null)
		{
			returnSTR += uniquePairs.poll();
		}
		
		returnSTR += "{";
		
		//Adds the final remainder Tiles
		while(remainders.peek() != null)
		{
			returnSTR += remainders.poll();
		}
		
		return returnSTR += "}[" + min + "]" + Tile.suit_reference[suit];
	}
	public static class PlayerHand_Searcher
	{
		
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
		
		/**
		 * This is the valid hand that can return an ArrayList of tiles that represents a valid hand in Mahjong
		 */
		private Visible_hand valid_PlayerHand_;
		
		public PlayerHand_Searcher(Visible_hand valid_hand)
		{
			this.valid_PlayerHand_ = valid_hand;
		}
		
		/**
		 * *Note* this function does not take into consideration unique hands like 7 pairs nor 13 orphans
		 * 
		 * Group_Search progress_score would not consider declared groups, This will add this specific PlayerHand declared groups
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
		public int progressScore()
		{
			if(!this.updated_searches_)
			{
				this.update_Groups_ = Group_Search.searchAllGroupSN(this.valid_PlayerHand_, false);
				this.updated_searches_ = true;
			}
			int best_score = 0;
			for(String key: this.update_Groups_.keySet())
			{
				int current_score = Group_Search.progressScore(this.update_Groups_.get(key)) + (this.valid_PlayerHand_.getDeclaredGroups().size() * 1000);
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
		public ArrayList<Group> getFastestGroups()
		{
			if(!this.updated_searches_)
			{
				this.update_Groups_ = Group_Search.searchAllGroupSN(this.valid_PlayerHand_, false);
				this.updated_searches_ = true;
			}
			int best_score = 0;
			String best_key = "";
			for(String key: this.update_Groups_.keySet())
			{
				int current_score = Group_Search.progressScore(this.update_Groups_.get(key)) + (this.valid_PlayerHand_.getDeclaredGroups().size() * 1000);
				if(current_score > best_score)
				{
					best_score = current_score;
					best_key = key;
				}
			}
			return MJ_Hand_tools.groupSN_to_ArrayList(this.update_Groups_.get(best_key));
		}
		
		/**
		 * *Warning* Should only run when this.update_Groups is empty, otherwise waste of resources
		 * @param new_tile The new tile that assumes is newly drawn to the hand, can be discarded
		 * @return True if the current groups are updated to fit the new_tile inputed,
		 * 		   False if the tile either doesn't fit (groups didn't alter in anyway) or error was raised
		 */
		public boolean refreshGroupSN()
		{
			try
			{
				HashMap<String, String> groupSN_map = Group_Search.searchAllGroupSN(this.valid_PlayerHand_, false);
				for(String key: groupSN_map.keySet())
					this.update_Groups_.put(key, groupSN_map.get(key));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
	}
	
	//Main Method
	public static void main(String[] args)
	{
		int[] singleSuitTileIDs = {0,0,0,1,2,3,4,5,6,7,8,8,8};
		ArrayList<Tile> randomHand = new ArrayList<Tile>();
		for(int tileID: singleSuitTileIDs) randomHand.add(new Tile(tileID));
		Visible_hand x = new Visible_hand(randomHand);
		HashMap<String, String> result = searchAllGroupSN(x);
		for(String key: result.keySet())
		{
			System.out.println("Key: " + key + " groupSN: " + result.get(key));
		}
		
		System.out.println("\n");
		
		int[] randTileIDs = {0,0,4,4};
		ArrayList<Tile> randomHand2 = new ArrayList<Tile>();
		for(int tileID: randTileIDs) randomHand2.add(new Tile(tileID));
		Visible_hand x2 = new Visible_hand(randomHand2);
		HashMap<String, String> result2 = searchAllGroupSN(x2);		
		for(String key: result2.keySet())
		{
			System.out.println("Key: " + key + " groupSN: " + result2.get(key));
		}
		
		String pair_search = listPairsSearch(randomHand2);
		System.out.println(pair_search);
	}
}

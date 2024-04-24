package bot_package;

import java.util.*;


/**
 * 	Scoring Reference
 * 	0: Flower : seatwind +1 -> 1 (from player flowerlist) 					[from Player]
	1: Dragon Groups -> 1													[from GroupSearch -> ArrayList<Group>]
	2: Small 3 Dragons -> 3													[from GroupSearch -> ArrayList<Group>]
	3: Big 3 Dragons -> 5 													[from GroupSearch -> ArrayList<Group>]
	4: Rotation Wind -> 1 (from gamestatus)									[from GroupSearch -> ArrayList<Group>]
	5: Seat Wind -> 1 (from player seatwind + 1) 							[from GroupSearch -> ArrayList<Group>]
	6: Kongs -> 1															[from GroupSearch -> ArrayList<Group>]
	7: Concealed -> 1														[from GroupSearch -> groupSN]
	8: Tsumo -> 1															[from External game system class]
	9: after flower tsumo -> 1												[from External game system class]
	10 After a kong tsumo -> 1												[from External game system class]
	11: Rob a Kong -> 1														[from External game system class]
	12: Under the river (last tile) -> 1									[from External game system class]
	13: All Seq -> 1														[from GroupSearch -> ArrayList<Group>]
	14: All Pon -> 3														[from GroupSearch -> ArrayList<Group>]
	15: Mix Suit -> 3 														[from GroupSearch -> groupSN]
	16: All Terms/Honors -> 2 (5 - 3; from pung game)						[from GroupSearch -> ArrayList<Group>]
	18: 7 Pairs -> 3 (4 - 1; from concealed)								[from Unique_GroupSearch -> pairSN]
	17: Full Suit -> 6 		
	----------------------------------------------------------------------------------------------------
	18: All Terminals -> 5 (10 - 2 - 3; all term/honors, from pung game)	[from GroupSearch -> ArrayList<Group>]
	19: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)		[from GroupSearch -> ArrayList<Group>]
	20: 4 little winds -> 6 												[from GroupSearch -> ArrayList<Group>]
	21: 4 big winds ->10 (13 - 3; from pung game)							[from GroupSearch -> ArrayList<Group>]
	22: Tenhou (Heavenly Hand) -> 11(13 - 1 - 1; from concealed, tsumo)		[from External game system class]
	23: Renhou (Earthly Hand) -> 12 (13 - 1; from concealed)				[from External game system class]
	24: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed) 	[from Unique_GroupSearch -> kokushiSN]
	25: 4 quads -> 10 (13 - 3; from pung game)								[from GroupSearch -> ArrayList<Group>]
	26: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)		[from GroupSearch -> ArrayList<Group>]
	27: Nine gates -> 9 (10 - 1; from concealed)							[from Unique_GroupSearch -> ?]

 */

public class Scoring 
{
    /**
	 * @param in_hand Any valid hand input
	 * @return 4 ArrayList<Integer> within Arraylist<Integer> that represents each suit of a mahjong hand.
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
	 * @function This function would check every group concealed status, if all is true, then the whole hand was concealed,
	 * 			 and would return a concealed point
	 * 
	 * @param all_groups all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return The integer score if all groups inputed were concealed would return a score of 1, 0 otherwise
	 */
	public static int concealed(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(!all_groups.get(i).concealed_)
			{
				return 0;
			}
		}
		return 1;
	}
	
	/**
	 * @function This function checks each group if it is a quad or not, the quad also needs to be declared in order
	 * 			 to be counted as a point. This function does not take into consideration all quads points, but would
	 * 			 have to add these point into an all quads situation.
	 * 
	 * @param all_groups all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return The integer score if all groups inputed, if there were any declared quads (concealed or opened), this will add a point
	 * 		   all kongs return 6, here would add 4 kongs to total up 4 quads hand
	 */
	public static int quad_amount(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		int return_score = 0;
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(all_groups.get(i).getGroupInfo()[0] == 3 && all_groups.get(i).declared_)
			{
				return_score++;
			}
		}
		return return_score;
	}
	
	/**
	 * @function This function checks the tile of each group to see if it's a dragon tile, then adds to counter
	 * 			 with status returning triplet or pair, pairs would only matter for small dragons, and the score would
	 * 			 return depending on var temp_counter
	 * 
	 * @param all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return The integer score that would represent what the inputed ArrayList<Group> would represent
	 */
	public static int dragon_score(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		//digit 1s -> pair, digit 10s -> triplet/quad
		int temp_counter = 0;
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(all_groups.get(i).get_groupTiles().get(0) > 30) //checks dragon tiles
			{
				switch(all_groups.get(i).get_groupTiles().size())
				{
					case 2:
						temp_counter += 1;
						break;
					case 4: //quads are treated the same as triplet
					case 3:
						temp_counter += 10;
						break;
				}
			}
 		}
		/*
		 * valid cases:
		 * 30 == big 3 dragons
		 * 21 == small 3 dragons
		 * 20 == 2 points dragon
		 * etc
		 */
		switch(temp_counter)
		{
			case 30:
				return 5;
			case 21:
				return 3;
			default:
				return temp_counter/10; //remainder (pairs don't add points) are excluded
		}
	}
	
	/**
	 * @function This function does not search seat/prevalent wind, but searches the 4 winds combination score
	 * 
	 * @param all_groups all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return The integer score of only searched 4 big/small winds from all_groups, returns corresponding score, otherwise 0
	 */
	public static int four_wind_score(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		//digit 1s -> pair, digit 10s -> triplet/quad
		int temp_counter = 0;
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(all_groups.get(i).get_groupTiles().get(0) >= 27 && all_groups.get(i).get_groupTiles().get(0) <= 30) //checks wind tiles
			{
				switch(all_groups.get(i).get_groupTiles().size())
				{
					case 2:
						temp_counter += 1;
						break;
					case 4: //quads are treated the same as triplet
					case 3:
						temp_counter += 10;
						break;
				}
			}
		}
		/*
		 * valid cases:
		 * 40 == 4 big wind
		 * 31 == 4 small winds
		 * 20 == nothing, except seat/prevalent wind
		 * etc
		 */
		switch(temp_counter)
		{
			case 40:
				return 10; //13 - 3 from all triplet
			case 31:
				return 6;
		}
		return 0;
	}
	
	/**
	 * @function This function searches through each group of all_groups and checks it's GroupInfo to see if it's a
	 * 			 Sequence, Triplet, or Quad, depending on the result, some all [type] are set to false, and the score
	 * 			 is returned corresponding to what is true; This function will only allow 1 pair to pass.
	 * 
	 * Scoring reference:
	 * 1 == all_sequence
	 * 3 == all_triplet
	 * 9 == all_quads (will add 4 from kong points)
	 * 
	 * @param all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return A integer score that represents if the inputed ArrayList<Group> reflects the score being checked
	 * 			The algorithm will output 9 if all quads, 1 if all sequences, 3 if all triplet, 0 if none, and cannot be both
	 */
	public static int group_type_score(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		boolean[] group_type = {true, true, true}; //index 0 = sequence, index 1 = triplet
		boolean pair_passed = false;
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(!group_type[0] && !group_type[1]) {return 0;} //No possible cases
			switch(all_groups.get(i).getGroupInfo()[0])
			{
				case 1: //Checks completed sequences
					group_type[1] = false;
					group_type[2] = false;
					continue;
				case 2: //Checks completed triplets
					group_type[0] = false;
					group_type[2] = false;
					continue;
				case 3: //Checks completed quads
					group_type[0] = false;
					continue;
			}
			if(Group.group_status(all_groups.get(i)) == 0 && !pair_passed)
			{
				pair_passed = true;
			}
			return 0;
		}
		if(group_type[0])
		{
			return 1;
		}
		else if(group_type[2])
		{
			return 9;
		}
		else if(group_type[1])
		{
			return 3;
		}
		return 0;
	}
	
	/**
	 * @function This function assumes the all_groups is completed, and searches tiles by suitDivide function
	 * 
	 * Scoring reference:
	 * mixed_suit = 3
	 * full_suit = 6
	 * all_honors = 5 (10 with required triplet[3] and all term/honor[2]) 
	 * 
	 * @param all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return A integer representation of the score all_groups is being checked for, this algorithm checks if the given
	 * 		   groups are mixed,full suited, or even all honors
	 */
	public static int mix_full_suit(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		/*
		 * Method:
		 * Groups -> tile_list -> suit divide -> check each suit for tiles -> return result
		 */
		ArrayList<Integer> all_tiles = new ArrayList<Integer>();
		int has_suits = 0;
		for(int i = 0; i < all_groups.size(); i++) for(int j = 0; j < all_groups.get(i).get_groupTiles().size(); j++) all_tiles.add(all_groups.get(i).get_groupTiles().get(j));
		ArrayList<ArrayList<Integer>> suited_tiles = suitDivide(all_tiles);
		for(int i = 0; i < suited_tiles.size(); i++)
		{
			if(i == 3 && suited_tiles.get(i).size() > 0)
			{
				has_suits += 10;
				break;
			}
			if(suited_tiles.get(i).size() > 0)
			{
				has_suits++;
				continue;
			}
		}
		switch(has_suits)
		{
			case 11: //mixed suit
				return 3;
			case 1:	 //full suit
				return 6;
			case 10: //all honors
				return 5;
			default:
				return 0;
		}
	}
	
	/**
	 * Scoring reference:
	 * (Must be all triplet, so not adding score to orphans hand)
	 * All Terminal: 5 (10 - 2 [all term/honor] - 3 [triplets])
	 * All Honors: 2 (All honors is checked in mix_full_suit), add back all term/honor point
	 * All Term/Honor: 2 (default all term/honor)
	 * 
	 * 
	 * @param all_groups all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return A score corresponding to what was found in all the groups, see scoring reference for more details
	 */
	public static int term_honors(ArrayList<Group> all_groups)
	{
		if(all_groups.size() == 0) {return 0;}
		boolean[] has_term_honor = {false, false};
		int[] term_reference = {0,8,9,17,18,26}; //Honors > 26
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(all_groups.get(i).getGroupInfo()[0] > 1) //Must be triplet
			{
				boolean is_term = false;
				int tile = all_groups.get(i).get_groupTiles().get(0);
				if(tile >= 27) //Checks if tile_id is an honor value
				{
					has_term_honor[1] = true;
					continue;
				}
				for(int j = 0; j < term_reference.length; j++)
				{
					if(tile == term_reference[j])
					{
						is_term = true;
					}
				}
				if(is_term) //Checks if the triplet/quad is all terminal
				{
					has_term_honor[0] = true;
					continue;
				}
			}
			//Any other case will return 0
			return 0;
		}
		if(has_term_honor[0] && !has_term_honor[1])
		{
			return 7; //all terminal 10 - 3 from all triplet
		}
		else if(has_term_honor[1])
		{
			return 2;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param all_groups all_groups all_groups An ArrayList<Group> of all groups, which includes concealed/open and undeclared/declared
	 * @return An integer score that only represents if all_groups only include concealed triplets as completed groups, 0 otherwise
	 */
	public static int concealed_triplets(ArrayList<Group> all_groups)
	{
		boolean pair_passed = false;
		for(int i = 0; i < all_groups.size(); i++)
		{
			if(all_groups.get(i).concealed_)
			{
				if(!pair_passed && Group.group_status(all_groups.get(i)) == 0) //Only allows 1 concealed pair to pass
				{
					pair_passed = true;
					continue;
				}
				if(all_groups.get(i).getGroupInfo()[0] >= 2) //Checks if is concealed triplet/quad
				{
					continue;
				}
			}
			//return 0 for any false case
			return 0;
		}
		if(pair_passed)
		{
			return 6;
		}
		return 0;
	}
	/**
	 * 
	 * @param concealed_hand: NOT ArrayList<Group>, 7 pairs is unique as it requires pairs not groups, so the hand
	 * 						  must be concealed and no declared groups, also duplicate pairs (quads) don't count as 2 pairs
	 * @return The integer score representation of the given concealed_hand, if 7 pairs is complete, return 3, otherwise return 0
	 */
	public static int seven_pairs(ArrayList<Integer> concealed_hand)
	{
		if(Unique_GroupSearch.seven_pairs_score(Unique_GroupSearch.search_7pairs(concealed_hand)) == 700)
		{
			return 3;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param concealed_hand NOT ArrayList<Group>, 13 orphans is unique as it requires isolate terms/honors, so the hand
	 * 						 must be concealed and no declared groups, there has to be any copy of term/honor while having all term/honor to be complete
	 * @return The integer score representation of the given concealed_hand, if 13 orphans is complete, return 12, otherwise return 0
	 */
	public static int kokushi_musou(ArrayList<Integer> concealed_hand)
	{
		if(Unique_GroupSearch.orphan_score(Unique_GroupSearch.search_orphans(concealed_hand)) == 1301)
		{
			return 12;
		}
		return 0;
	}
	
	/**
	 * 
	 * @param concealed_hand concealed_hand NOT ArrayList<Group>, althought nine gates can be detected using ArrayList<Group>
	 * 						 it is much more labor intensive, all 9 gates share a same shape with an additional tile in that same suit
	 * 						 which would be how 9 gates is detected
	 * @return The integer score representation of the given concealed_hand, if 9 gates is complete, return 3, otherwise return 0
	 */
	public static int nine_gates(ArrayList<Integer> concealed_hand)
	{
		ArrayList<Integer> suit_scores = Unique_GroupSearch.ninegates_score(Unique_GroupSearch.search_ninegates(concealed_hand));
		for(int i = 0; i < suit_scores.size(); i++)
		{
			if(suit_scores.get(i) == 130001)
			{
				return 3; //10 - 6 - 1 from full suit, concealed
			}
		}
		return 0;
	}
	
	/**
	 * A Local class specific to unique hands that doesn't follow the traditional 4 groups 1 pair completion hand
	 */
	class Unique_GroupSearch extends GroupSearch
	{
		/**
		 * Overloading function to accept both ArrayList<Integer> and PlayerHand
		 * 
		 * The return String will be formatted as the following:
		 * (Pair1)(Pair2)...r={(FloatTile1)}[min]suit...
		 * 
		 * *Note* that 7 pairs requires no declared groups and all pairs must be unique
		 * 
		 * @param in_PlayerHand The Player's hand that wants to be checked for "groups" of pairs
		 * @return 	A String representation similar to groupSN, instead pairSN (pair String notation)
		 */
		public static String search_7pairs(Player.PlayerHand in_PlayerHand)
		{
			if(in_PlayerHand.getDeclaredGroup().size() > 0)
			{
				return "NULL";
			}
			return search_7pairs(in_PlayerHand.getCurrentHand());
		}
		
		/**
		 * The return String will be formatted as the following:
		 * (Pair1)(Pair2)...r={((FloatTile1)}[min]suit...
		 * 
		 * *Note* that 7 pairs requires no declared groups and all pairs must be unique
		 * 
		 * @param tile_list An ArrayList<Integer> representation of the concealed part of a Player's Hand
		 * @return A String representation that is similar to groupSN but only for 7 pairs
		 */
		public static String search_7pairs(ArrayList<Integer> tile_list)
		{
			//Convert to matrix only applicable to single suit ArrayList
			ArrayList<ArrayList<Integer>> suit_list = suitDivide(tile_list);
			
			//Since 7 pairs REQURIES unique pairs, we can filter by having a set
			ArrayList<SortedSet<Integer>> pairs_list = new ArrayList<SortedSet<Integer>>();
			for(int i = 0; i < 2; i++) pairs_list.add(new TreeSet<Integer>());
			
			String return_str = "";
			
			for(int suit = 0; suit < suit_list.size(); suit++)
			{
				if(suit_list.get(suit).size() == 0) {continue;}
				
				String remainder_str = "r={";
				int min = tileID_to_PlayVal(suit_list.get(suit).get(0));
				//convert the ArrayList of tiles to a suit matrix 
				suit_list.set(suit, convert_to_matrix(suit_list.get(suit)));
				for(int index = 0; index < suit_list.get(suit).size(); index++)
				{
					if(suit_list.get(suit).get(index) < 1 || suit_list.get(suit).get(index) > 4) {continue;} //Invalid amount of tiles or no tiles, ignore
					//Checks if the tiles is already added, should only try to catch quads
					boolean already_added = false;
					for(int i = 0; i < pairs_list.size(); i++) if(pairs_list.get(i).contains(PlayVal_to_tileID(index + min, suit))) {already_added = true;}
					if(already_added) {continue;}
					
					//New range = [1,4]
					switch(suit_list.get(suit).get(index))
					{
						case 1:
							remainder_str += "(" + index + ")";
							pairs_list.get(1).add(PlayVal_to_tileID(index + min, suit));
							break;
						default:
							return_str += "(" + index + Integer.toString(index) + ")";
							pairs_list.get(0).add(PlayVal_to_tileID(index + min, suit));
					}
				}
				return_str += remainder_str + "}" + "[" + Integer.toString(min) + "]" + Group.suit_reference[suit];
			}
			return return_str + "c";
		}
		
		
		/**
		 * 
		 * Overloading function to accept both ArrayList<Integer> and PlayerHand
		 * Since 13 Orphans technically doesn't have any groups, the return will be most unique compared to groupSN and pairSN
		 * It'll be named kokushiSN (named after the Japanese variant of the special hand "Kokushi Musou")
		 * 
		 * String format will be the following:
		 * (19){19}m(19){19}p(19){19}s(1234567){1234567}z
		 * () indicates the orphans you currently have
		 * {} indicates the overflow/pair you currently have
		 * To keep consistency, (){}suit will remain despite not having tiles in them
		 * 
		 * @param in_PlayerHand The PlayerHand that wants to be searched for potential orphans score
		 * @return A String representation of what tiles that are acquired and what are missing, also indicates pairs
		 */
		public static String search_orphans(Player.PlayerHand in_PlayerHand)
		{
			if(in_PlayerHand.getDeclaredGroup().size() > 0)
			{
				return "NULL";
			}
			return search_orphans(in_PlayerHand.getCurrentHand());
		}
		
		/**
		 * Since 13 Orphans technically doesn't have any groups, the return will be most unique compared to groupSN and pairSN
		 * It'll be named kokushiSN (named after the Japanese variant of the special hand "Kokushi Musou")
		 * 
		 * String format will be the following:
		 * (19){19}m(19){19}p(19){19}s(1234567){1234567}zc
		 * () indicates the orphans you currently have
		 * {} indicates the overflow/pair you currently have
		 * To keep consistency, (){}suit will remain despite not having tiles in them
		 * 
		 * @param tile_list The ArrayList<Integer> of tiles that wants to be searched for potential orphans score
		 * @return A String representation of what tiles that are acquired and what are missing, also indicates the pair
		 */
		public static String search_orphans(ArrayList<Integer> tile_list)
		{
			if(tile_list.size() == 0) {return "";}
			
			String return_str = "";
			ArrayList<ArrayList<Integer>> suited_tile_list = suitDivide(sortArray(tile_list));
			
			for(int suit_index = 0; suit_index < suited_tile_list.size(); suit_index++)
			{
				//Adds empty String representation of suit and continues
				if(suited_tile_list.get(suit_index).size() == 0) 
				{return_str += "(){}" + Group.suit_reference[suit_index]; continue;}
				
				//How each suit is added into the return_str
				String needed_str = "(";
				String overflow_str = "{";
				
				//Orphans requires all honors, other suits only needed terminal
				if(suit_index == 3)
				{
					int min = tileID_to_PlayVal(suited_tile_list.get(3).get(0));
					suited_tile_list.set(3, convert_to_matrix(suited_tile_list.get(3)));
					for(int i = 0; i < suited_tile_list.get(3).size(); i++) //Uses index amount instead since every honor is needed
					{
						for(int j = 0; j < suited_tile_list.get(3).get(i); j++)
						{
							if(j == 0)
							{
								needed_str += Integer.toString(i + min); //The flaw with converting to matrix is adding min back
								continue;
							}
							overflow_str += Integer.toString(i + min);;
						}
					}
					needed_str += ")";
					overflow_str += "}";
					return return_str + needed_str + overflow_str + "zc";
				}
				
				//Adds suits that aren't honors
				int[] counter = {0,0};
				int[] terminal_reference = {1,9};
				for(int tile: tileID_to_PlayVal(suited_tile_list.get(suit_index)))
				{
					switch(tile)
					{
						case 1:
							counter[0]++;
							break;
						case 9:
							counter[1]++;
							break;
					}
				}
				for(int i = 0; i < counter.length; i++) //Loop through counter
				{
					for(int amt = 0; amt < counter[i]; amt++) //Loops amount of tiles found
					{
						if(amt == 0)
						{
							needed_str += terminal_reference[i];
						}
						else if(amt == 1)
						{
							overflow_str += terminal_reference[i];
						}
					}
				}
				needed_str += ")";
				overflow_str += "}";
				return_str += needed_str + overflow_str + Group.suit_reference[suit_index];
			}
			return return_str + "c";
		}
		
		/**
		 * Nine Gates requires play_val list of 1112345678999 with another tile in the same suit
		 * 
		 * Return format:
		 * (in current hand)r={required tiles}o=[Overflow tiles]suit *for each suit*
		 * 
		 * completed
		 * (1112345678999)r={}o=[[1,9]]suit
		 * 
		 * waiting
		 * (Some shape missing 1 tile from 111245678999)r={that 1 missing tile}o=[[1,9]]suit
		 * or
		 * (1112345678999)r={}o=[]suit == Any wait
		 * 
		 * incomplete
		 * Anything other shape
		 * 
		 * @param tile_list The ArrayList<Integer> of tiles that wants to be searched for potential Nine Gates score
		 * @return A String representation of what tiles that are acquired and what are missing for each suit
		 */
		public static String search_ninegates(ArrayList<Integer> tile_list)
		{
			if(tile_list.size() == 0)
			{
				return "";			
			}
			
			
			String return_str = "";
			ArrayList<ArrayList<Integer>> suited_tile_list = suitDivide(sortArray(tile_list));
			for(int suit = 0; suit < 3; suit++)
			{
				if(suited_tile_list.get(suit).size() == 0)
				{
					return_str += "()r={1112345678999}o={}" + Group.suit_reference[suit];
					continue;
				}
				
				String temp_string = "(";
				String temp_remainder = "r={";
				String temp_overflow = "o=[";
				int min = tileID_to_PlayVal(suited_tile_list.get(suit).get(0));
				int max = tileID_to_PlayVal(suited_tile_list.get(suit).get(suited_tile_list.get(suit).size() - 1));
				int index = 0;
				
				ArrayList<Integer> matrixify_suit = convert_to_matrix(suited_tile_list.get(suit));
				
				//catch up
				for(int i = 1; i < min; i++)
				{
					if(i == 1 || i == 9)
					{
						for(int j = 0; j < 3; j++) temp_remainder += i;
					}
					else
					{
						temp_remainder += i;
					}
				}
				for(int i = min; i <= max; i++)
				{
					int needed_amt = 1;
					int start_amt = 0;
					if(i == 1 || i == 9) //The required "triplet" for nine gates
					{
						needed_amt = 3;
					}
					//Need every tile in the same suit, checks if there is tiles to offer
					if(matrixify_suit.get(index) > 0)
					{
						temp_string += Integer.toString(i);
						matrixify_suit.set(index, matrixify_suit.get(index) - 1);
						start_amt++;
					}
					for(int j = start_amt; j < needed_amt; j++)
					{
						if(matrixify_suit.get(index) == 0)
						{
							for(int k = j; k < needed_amt; k++)
							{
								temp_remainder += Integer.toString(i);
							}
							break;
						}
						temp_string += Integer.toString(i);
						matrixify_suit.set(index, matrixify_suit.get(index) - 1);
					}
					for(int j = 0; j < matrixify_suit.get(index); j++)
					{
						temp_overflow += Integer.toString(i);
						matrixify_suit.set(index, matrixify_suit.get(index) - 1);
					}
					index++; //Temp var "i" does not keep track of index, it's the range of what tile is in the hand
				}
				
				for(int i = max + 1; i <= 9; i++)
				{
					if(i == 1 || i == 9)
					{
						for(int j = 0; j < 3; j++) temp_remainder += i;
					}
					else
					{
						temp_remainder += i;
					}
				}
				//fill remain
				temp_string += ")";
				temp_remainder += "}";
				temp_overflow += "]";
				return_str += temp_string + temp_remainder + temp_overflow + Group.suit_reference[suit];
			}
			
			return return_str;
		}
		
		/**
		 * Scoring table:
		 * 
		 * 700 == 7 pairs, 00 floating == complete
		 * 601 == 6 pairs, 01 floating == waiting
		 * 503 == 5 pairs, 02 floating == incomplete
		 * 405 == 4 pairs, 05 floating == incomplete
		 * 307 == 3 pairs, 07 floating == incomplete
		 * 209 == 2 pairs, 09 floating == incomplete
		 * 111 == 1 pairs, 11 floating == incomplete
		 * 
		 * @param pairSN The String notation of what was searched through ArrayList<Integer> as player's hand
		 * @return A integer score similar to GroupSearch.progress_score(), just different scoring basis
		 */
		public static int seven_pairs_score(String pairSN)
		{
			int add_mode = 10; //10 = add as pair, 01 = add as floating
			int return_score = 0;
			
			for(int i = 0; i < pairSN.length(); i++)
			{
				//Checks indicators+
				if(Character.isAlphabetic(pairSN.charAt(i)))
				{
					if(pairSN.charAt(i) == 'r') {add_mode = 1;}; //'r' acts as remainder as in floating tiles after that
					for(char suit_indicator: Group.suit_reference)
					{
						if(pairSN.charAt(i) == suit_indicator) {add_mode = 10;}
					}
				}
				else if(pairSN.charAt(i) == '(') //'(' act as starting to list tiles
				{
					return_score += add_mode;
				}
			}
			return return_score;
		}
		
		/**
		 * Scoring Table:
		 * 
		 * 1301 == 13 uniques, 01 pair  == complete
		 * 1300 == 13 uniques, no pairs == waiting
		 * 1201 == 12 uniques, 01 pair  == waiting
		 * 1102 == 11 uniques, 02 pairs == incomplete
		 * etc... == incomplete
		 * 
		 * kokushiSN format:
		 * (19){19}m(19){19}p(19){19}s(1234567){1234567}zc
		 * 
		 * @param kokushiSN The String notation of what was searched through ArrayList<Integer> as player's hand
		 * @return A integer score similar to GroupSearch.progress_score(), just different scoring basis
		 */
		public static int orphan_score(String kokushiSN)
		{
			int add_mode = 2; //2 = add unique, 0 = add pairs
			int return_score = 0;
			for(int i = 0; i < kokushiSN.length(); i++)
			{
				if(Character.isDigit(kokushiSN.charAt(i)))
				{
					return_score += (int)Math.pow(10, add_mode);
					continue;
				}
				
				//The important indicator that determines what score to add
				if(kokushiSN.charAt(i) == '(')
				{
					add_mode = 2;
					continue;
				}
				else if(kokushiSN.charAt(i) == '{')
				{
					add_mode = 0;
					continue;
				}
			}
			return return_score;
		}
		
		/**
		 * Scoring Table:
		 * 
		 * Score example: 130001 
		 * 		index 0,1 -> acquired required tiles
		 * 		index 2,3 -> still needed tiles
		 * 		index 4,5 -> overflow tiles (need 1 to finish)
		 * 
		 * complete == 130001
		 * waiting == 120101 or 130000
		 * incomplete examples == 120000, 110200
		 * 
		 * chuurenSN format: (in current hand)r={required tiles}o={Overflow tiles}suit *for each suit*
		 * Examples:
		 * completed
		 * (1112345678999)r={}o[{1,9]]suit
		 * 
		 * waiting
		 * (Some shape missing 1 tile from 111245678999)r={that 1 missing tile}o=[[1,9]]suit
		 * or
		 * (1112345678999)r={}o=[]suit == Any wait
		 * 
		 * @param chuurenSN The String representation of how a player's hand progress towards nine gates
		 * @return A integer score similar to GroupSearch.progress_score(), just different scoring basis
		 */
		public static ArrayList<Integer> ninegates_score(String chuurenSN)
		{
			int add_mode = 4; // 4 == index 1(has), 2 == index 3(need), 0 == index 5(overflow)
			int suit_pointer = 0;
			ArrayList<Integer> return_scores = new ArrayList<Integer>();
			for(int i = 0; i < 3; i++) return_scores.add(0);
			
			for(int i = 0; i < chuurenSN.length(); i++)
			{
				if(Character.isAlphabetic(chuurenSN.charAt(i)))
				{
					if(chuurenSN.charAt(i) == 's') //Last suit possible to get nine gates (didn't scan honors)
					{
						break;
					}
					for(int suit = 0; suit < 3; suit++) //Changes suit by checking reference and increment pointer
					{
						if(chuurenSN.charAt(i) == Group.suit_reference[suit])
						{
							suit_pointer = suit + 1;
							break;
						}
					}
				}
				else if(Character.isDigit(chuurenSN.charAt(i)))
				{
					return_scores.set(suit_pointer, return_scores.get(suit_pointer) + (int)Math.pow(10, add_mode));
				}
				//Indicates what the hand currently has
				if(chuurenSN.charAt(i) == '(')
				{
					add_mode = 4;
					continue;
				}
				//Indicates what the hand currently needs
				else if(chuurenSN.charAt(i) == '{')
				{
					add_mode = 2;
					continue;
				}
				//Indicates what the hand currently has extra of
				else if(chuurenSN.charAt(i) == '[')
				{
					add_mode = 0;
					continue;
				}
			}
			return return_scores;
		}
	}
	
	/**
	 * A Local class that will include all the scoring algorithm within Scoring,
	 * the reason for separation from Scoring class itself is separating functional code into different descriptive classes
	 */
	class Scoring_Algorithm
	{
		
	}
	public static void main(String[] args)
	{
		/*
		 * 	0: Dragon Groups -> 1													
		 *	1: Small 3 Dragons -> 3													
		 *	2: Big 3 Dragons -> 5 													
		 *	3: Rotation Wind -> 1 (from gamestatus)									
		 *	4: Seat Wind -> 1 (from player seatwind + 1) 							
		 *	5: Kongs -> 1				
		 *	6: Concealed -> 1																																			
		 *	7: All Seq -> 1														
		 *	8: All Pon -> 3														
		 *	9: Mix Suit -> 3 														
		 *	10: All Terms/Honors -> 2 (5 - 3; from pung game)		
		 *	11: 7 Pairs -> 3 (4 - 1; from concealed)				
		 *	12: Full Suit -> 6 		
		 *	-------------------------------------
		 *	13: All Terminals -> 5 (10 - 2 - 3; all term/honors, from pung game)	
		 *	14: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)		
		 *	15: 4 little winds -> 6 												
		 *	16: 4 big winds ->10 (13 - 3; from pung game)										
		 *	17: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed) 	
		 *	18: 4 quads -> 10 (13 - 3; from pung game
		 *	19: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)		
		 *	20: Nine gates -> 9 (10 - 1; from concealed)							
		 */
		String[] example_hand_list = {	
        "123m455667p99sckq555zo",       // 1 Dragon
        "111m222s66zckq555777zo",       // Small 3 Dragons
        "77sckq123m555666777zo",        // Big 3 Dragon
        "123789m99pckq111s111zo",       // Rotation Wind (input int as rotation index) this exp == 2 (w)
        "12378999m456pckq333zo",        // Seat Wind (input int as seat index) this exp == 3 (n)
        "111234m55zc6666sk111zo",       // Concealed Kong == Kong point
        "123456999m99s111zckqo",        // Concealed
        "23488m567pckq234567so",		// All Sequences
        "22mc6666mkq555p777s111zo",		// All Triplet
        "55667799sck2222zq333zo",		// Half flush
        "111m11sckq999p999s666zo",		// All Terminal / Honors
        "2255m7799p1122s55zckqo",		// 7 pairs
        "11122299mckq677889mo",         // Chinitsu (Full Flush, Single Suit)
        "111m11sck1111pq999p999so",		// All Terminal
        "11166zc7777zkq222333zo",		// All honors
        "22zck1111zq456m333444zo",		// 4 little winds
        "88mc11112222zkq333444zo",		// 4 big winds
        "199m19p19s1234567zcko",		// KOKUSHI MUSOU
        "11mc3333m8888sk11117777zqo",	// 4 kongs
        "111555m666p88s222zckqo",		// 4 concealed Triplets
        "11112345678999pckqo",			// 9 gates
		};
		String[] score_name_list = {
		"1 Dragon",
		"Small 3 Dragon",
		"Big 3 Dragon",
		"Rotation Wind",
		"Seat Wind",
		"Kong points",
		"Concealed",
		"All Sequences",
		"All Triplet",
		"Half Flush",
		"All Terminal and Honors",
		"7 pairs",
		"Full Flush",
		"All Terminal",
		"All Honors",
		"4 Little Winds",
		"4 Big Winds",
		"13 Orphans",
		"4 Kongs",
		"4 Concealed Triplets",
		"9 Gates"
		};
		ArrayList<Player.PlayerHand> example_groupSN_list = new ArrayList<Player.PlayerHand>();
		for(String example_hand: example_hand_list) 
		{
			example_groupSN_list.add(Player.convert_mjSTR(example_hand));
		}
		for(int i = 0; i < example_groupSN_list.size(); i++)
		{	ArrayList<Group> temp_groups = new ArrayList<Group>();
			for(Group group: example_groupSN_list.get(i).getDeclaredGroup()) temp_groups.add(group);
//			System.out.println("New hand: " + example_pHand.getCurrentHand());
			
			System.out.println(	"mjString: " + example_hand_list[i]	);
			System.out.println(	score_name_list[i] + " Hand: " + example_groupSN_list.get(i).progress_score() );
			System.out.println(	"Current Hand: " + example_groupSN_list.get(i).getCurrentHand() );
			System.out.println(	"Best concealed groups: " + example_groupSN_list.get(i).get_fastestGroups() );
			for(Group group: example_groupSN_list.get(i).get_fastestGroups()) 
			{
				temp_groups.add(group);
			}
			System.out.println("Best groupSN:  " + Group.ArrayList_to_groupSN(temp_groups));
			System.out.println(	"CHITOI Score: " + Unique_GroupSearch.seven_pairs_score(Unique_GroupSearch.search_7pairs(example_groupSN_list.get(i).getCurrentHand())) + 
							   	", 7 Pairs Search: " + Unique_GroupSearch.search_7pairs(example_groupSN_list.get(i).getCurrentHand()) );
			System.out.println(	"Orphan Score: " + Unique_GroupSearch.orphan_score(Unique_GroupSearch.search_orphans(example_groupSN_list.get(i).getCurrentHand())) + 
								", Orphan Search: " + Unique_GroupSearch.search_orphans(example_groupSN_list.get(i).getCurrentHand()) );
			System.out.println(	"Nine gates score: " + Unique_GroupSearch.ninegates_score(Unique_GroupSearch.search_ninegates(example_groupSN_list.get(i).getCurrentHand())) + 
								", Nine gates Search: " + Unique_GroupSearch.search_ninegates(example_groupSN_list.get(i).getCurrentHand()) );
			System.out.println(	"\n\n");
		}
	}
}
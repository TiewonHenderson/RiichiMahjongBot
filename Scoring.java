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
				return "";
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
				return "";
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
						else
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
				//Checks indicators
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
		 * @param kokushiSN The String notation of what was searched through ArrayList<Integer> as player's hand
		 * @return A integer score similar to GroupSearch.progress_score(), just different scoring basis
		 */
		public static int orphan_score(String kokushiSN)
		{
			return -1;
		}
	}
	
	public static void main(String[] args)
	{
		/*
		 * 	1: Dragon Groups -> 1													
			2: Small 3 Dragons -> 3													
			3: Big 3 Dragons -> 5 													
			4: Rotation Wind -> 1 (from gamestatus)									
			5: Seat Wind -> 1 (from player seatwind + 1) 							
			6: Kongs -> 1				
			7: Concealed -> 1																																			
			8: All Seq -> 1														
			9: All Pon -> 3														
			10: Mix Suit -> 3 														
			11: All Terms/Honors -> 2 (5 - 3; from pung game)						
			12: Full Suit -> 6 		
			-------------------------------------
			13: All Terminals -> 5 (10 - 2 - 3; all term/honors, from pung game)	
			14: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)		
			15: 4 little winds -> 6 												
			16: 4 big winds ->10 (13 - 3; from pung game)										
			17: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed) 	
			18: 4 quads -> 10 (13 - 3; from pung game
			19: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)		
			20: Nine gates -> 9 (10 - 1; from concealed)							
		 */
		String[] example_hand_list = {	
        "123m455667p99sckq555zo",        // 1 Dragon
        "111m222s66zckq555777zo",        // Small 3 Dragons
        "77sckq123m555666777zo",         // Big 3 Dragon
        "123789m99pckq111s111zo",        // Rotation Wind (input int as rotation index) this exp == 2 (w)
        "12378999m456pckq333zo",         // Seat Wind (input int as seat index) this exp == 3 (n)
        "111234m55zc6666sk111zo",       // Concealed Kong == Kong point
        "123456999m99s111zckqo",         // Concealed
        "23488m567pckq234567so",			// All Sequences
        "22mc6666mkq555p777s111zo",		// All Triplet
        "55667799sck2222zq333zo",			// Half flush
        "111m11sckq999p999s666zo",		// All Terminal / Honors
        "11122299mckq677889mo",         	// Chinitsu (Full Flush, Single Suit)
        "111m11sck1111pq999p999so",		// All Terminal
        "11166zc7777zkq222333zo",		// All honors
        "22zckq456m1111333444zo",		// 4 little winds
        "88mc11112222zkq333444zo",		// 4 big winds
//        "199m19p19s1234567zcko",		// KOKUSHI MUSOU
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
		"Full Flush",
		"All Terminal",
		"All Honors",
		"4 Little Winds",
		"4 Big Winds",
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
			
			System.out.println("mjString: " + example_hand_list[i]);
			System.out.println(score_name_list[i] + " Hand: " + example_groupSN_list.get(i).progress_score());
			System.out.println("Current Hand: " + example_groupSN_list.get(i).getCurrentHand());
			System.out.println("Best concealed groups: " + example_groupSN_list.get(i).get_fastestGroups());
			for(Group declared_group: example_groupSN_list.get(i).getDeclaredGroup())
			{
				System.out.println(declared_group + "-> Conceal status: " + declared_group.concealed);
			}
			System.out.println("CHITOI Score: " + Unique_GroupSearch.seven_pairs_score(Unique_GroupSearch.search_7pairs(example_groupSN_list.get(i).getCurrentHand())) + 
							   ", 7 Pairs Search: " + Unique_GroupSearch.search_7pairs(example_groupSN_list.get(i).getCurrentHand()));
			System.out.println("Orphan Search: " + Unique_GroupSearch.search_orphans(example_groupSN_list.get(i).getCurrentHand()));
			System.out.println("\n\n");
		}
	}
}
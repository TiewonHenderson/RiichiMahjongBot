package bot_package;

import java.util.*;

import bot_package.Player.PlayerHand;

/**
 * 	Scoring Reference
 * 	0: Flower : seatwind +1 -> 1 (from player flowerlist) 					[Done]
	1: Dragon Groups -> 1													[]
	2: Small 3 Dragons -> 3													[]
	3: Big 3 Dragons -> 5 													[]
	4: Rotation Wind -> 1 (from gamestatus)									[]
	5: Seat Wind -> 1 (from player seatwind + 1) 							[]
	6: Kongs -> 1															[]
	7: Concealed -> 1														[]
	8: Tsumo -> 1															[]
	9: after flower tsumo -> 1												[]
	10 After a kong tsumo -> 1												[]
	11: Rob a Kong -> 1														[]
	12: Under the river (last tile) -> 1									[]
	13: All Seq -> 1														[Handled In Grouping]
	14: All Pon -> 3														[Handled In Grouping]
	15: Mix Suit -> 3 														[]
	16: All Terms/Honors -> 2 (5 - 3; from pung game)						[]
	17: Full Suit -> 6 		
	-------------------------------------
	18: All Terminals -> 5 (10 - 2 - 3; all term/honors, from pung game)	[]
	19: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)		[]
	20: 4 little winds -> 6 												[]
	21: 4 big winds ->10 (13 - 3; from pung game)							[]
	22: Tenhou (Heavenly Hand) -> 11(13 - 1 - 1; from concealed, tsumo)		[Handled In Grouping]
	23: Renhou (Earthly Hand) -> 12 (13 - 1; from concealed)				[Handled In Grouping]
	24: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed) 	[]
	25: 4 quads -> 10 (13 - 3; from pung game)								[]
	26: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)		[Handled In Grouping]
	27: Nine gates -> 9 (10 - 1; from concealed)							[Either]

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
        "123m455667p99sck555zo",        // 1 Dragon
        "111m222s66zck555777zo",        // Small 3 Dragons
        "77sck123m555666777zo",         // Big 3 Dragon
        "123789m99pck111s111zo",        // Rotation Wind (input int as rotation index) this exp == 2 (w)
        "12378999m456pck333zo",         // Seat Wind (input int as seat index) this exp == 3 (n)
        "111234m55zc6666sk111zo",       // Concealed Kong == Kong point
        "123456999m99s111zcko",         // Concealed
        "23488m567pck234567so",			// All Sequences
        "22mc6666mk555p777s111zo",		// All Triplet
        "55667799sck222333zo",			// Half flush
        "111m11sck999p999s666zo",		// All Terminal / Honors
        "11122299mck677889mo",         	// Chinitsu (Full Flush, Single Suit)
        "111m11sck111999p999so",		// All Terminal
        "11166zc7777zk222333zo",		// All honors
        "22zck456m1111333444zo",		// 4 little winds
        "88mc11112222zk333444zo",		// 4 big winds
//        "199m19p19s1234567zcko",		// KOKUSHI MUSOU
        "11mc3333m8888sk11117777zo",	// 4 kongs
        "111555m666p88s222zcko",		// 4 concealed Triplets
        "11112345678999pcko",			// 9 gates
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
		ArrayList<ArrayList<Group>> hand_groups_list = new ArrayList<ArrayList<Group>>();
		for(String example_hand: example_hand_list) example_groupSN_list.add(Player.convert_mjSTR(example_hand));
		for(int i = 0; i < example_groupSN_list.size(); i++)
		{	ArrayList<Group> temp_groups = new ArrayList<Group>();
			for(Group group: example_groupSN_list.get(i).getDeclaredGroup()) temp_groups.add(group);
//			System.out.println("New hand: " + example_pHand.getCurrentHand());
			HashMap<String, String> groupSN_map = GroupSearch.search_all_groupSN(example_groupSN_list.get(i), true);
			System.out.println();
			for(String key: groupSN_map.keySet())
			{
				System.out.println(score_name_list[i] + " groupSN: " + groupSN_map.get(key));
				if(key.charAt(2) == 'C')
				{
					for(Group group: GroupSearch.groupSN_to_ArrayList(groupSN_map.get(key))) temp_groups.add(group);
					hand_groups_list.add(temp_groups);
					temp_groups = new ArrayList<Group>();
					continue;
				}
			}
		}
		for(int i = 0; i < hand_groups_list.size(); i++)
		{
			System.out.println(i + ")" + score_name_list[i] + " Groups: " + hand_groups_list.get(i));
		}
	}
}

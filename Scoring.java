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
	 *  @param Winner player that won
	 *  @return integer score
	 */
	public static int final_score(Player winner) {
		if (
			/* 	
				the hand is a max hand 
				or
				the completed variable in grouping is true	
			*/ 
		) {
			int points;
			ArrayList<ArrayList<Integer>> suit_sorted = suitDivide(winner.getPlayerHand());
			
			// ArrayList<ArrayList<Integer>> group_sorted = getGroups();

			points += checkFlowers(winner.flower, winner.seatWind);
			points += checkHonors(sorted.get(3));
		} else {
			//pay out everyone
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
	 * @param flower_count integer of magnitude 10^4, represents drawn flowers
	 * @param seat integer 0-3, tells which seat the player is in
	 * Converts a flower integer into a string 
	 * then returns the number of points based upon the number of flowers the winner has 
	 */
	public static int checkFlowers(int flower_count, int seat) 
	{
		if(flower_count == 0) {
			// if you don't have any flowers
			return 1;
		} else {
			int good_flower = flower_str.charAt(seat)
			return good_flower;
		}
	}	

	/**
	 * @param hand hand will be already grouped
	 * @return points gotten from honor tiles
	 * Checks for All Honors, Seat Wind, Rotation Wind, Dragon Triplets, 3 Big Dragons
	 */
	public static int checkHonors(Array<integer> hand) 
	{
		int points = 0;
		
		if (hand.length() == 14) {
			points += 5; //not sure about point total
		}
		PlayerHand honors = PlayerHand(hand);
		Group sorted_honors = getGroups(honors);
		
	}
	
	
	/**
	 * 
	 * @param in_array: A 1 Dimensional Array that wants to be search through
	 * @param wantedInt: The integer that wants to be searched for in the in_array
	 * @return An ArrayList<Integer> of indexes that wantedInt occurs in in_array
	 */
	public static ArrayList<Integer> getOnlyInt1D(ArrayList<Integer> in_array, int wantedInt)
	{
		ArrayList<Integer> returnArray = new ArrayList<Integer>();
		for(int index = 0; index < in_array.size(); index++)
		{
			if(in_array.get(index) == wantedInt)
			{
				returnArray.add(index);
			}
		}
		return returnArray;
	}
	
	/**
	 * 
	 * @param in_array: A 2 Dimensional Array that wants to be search through
	 * @param wantedInt: The integer that wants to be searched for in the in_array
	 * @return An ArrayList<Integer> of indexes that wantedInt occurs in in_array
	 */
	public static ArrayList<Integer> getOnlyInt2D(ArrayList<ArrayList<Integer>> in_array, int wantedInt)
	{
		ArrayList<Integer> returnArray = new ArrayList<Integer>();
		for(int index = 0; index < in_array.size(); index++)
		{
			for(int items: getOnlyInt1D(in_array.get(index), wantedInt))
			{
				returnArray.add(items);
			}
		}
		return returnArray;
	}
	/**
	 * 
	 * @param in_hand: The current hand of the player to make AI predictions
	 * @return A decimal score representation of how many dragon points attainable
	 */
	public static double checkDragons(ArrayList<Integer> in_hand)
	{
		//Dragons = 31, 32, 33
		ArrayList<ArrayList<Integer>> temp_2DArray = suitDivide(in_hand);
		ArrayList<Integer> temp_honors = temp_2DArray.get(temp_2DArray.size() - 1);
		
		
		temp_2DArray = new ArrayList<ArrayList<Integer>>();
		for(int i = 0; i < 3; i++) temp_2DArray.add(new ArrayList<Integer>());
		for(int index = 0; index < temp_honors.size(); index++)
		{
			if(temp_honors.get(index) >= 31)
			{
				temp_2DArray.get(temp_honors.get(index) - 31).add(temp_honors.get(index));
			}
		}
		//In theory, this should return [31...],[32...],[33...]
		
		
		/*
		 * Points <= 2.0, 2 drag points
		 * Points == 2.5, 3 small drag
		 * Points == 3.0, 3 big drag
		 */
		double points = 0;
		for(int index = 0; index < temp_2DArray.size(); index++)
		{
			if(temp_2DArray.get(index).size() >= 3)
			{
				points += 1.0;
			}
			else if(temp_2DArray.get(index).size() == 2)
			{
				points += 0.5;
			}
		}
		return points;
	}
}

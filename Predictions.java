package bot_package;

import java.util.*;

/**
 * 	Scoring Reference
 * 	0: Flower : seatwind +1 -> 1 (from player flowerlist) 					[Not in predictions]
	0: Flower : flowerlist.size() == 0 -> 1									[Not in predictions]
	1: Dragon Groups -> 1 
	2: Small 3 Dragons -> 3 
	3: Big 3 Dragons -> 5 
	4: Rotation Wind -> 1 (from gamestatus)
	5: Seat Wind -> 1 (from player seatwind + 1)
	6: Kongs -> 1															[Not in predictions]
	7: Concealed -> 1														[Not in predictions]
	8: Tsumo -> 1															[Not in predictions]
	9: after flower tsumo -> 1												[Not in predictions]
	10 After a kong tsumo -> 1												[Not in predictions]
	11: Rob a Kong -> 1														[Not in predictions]
	12: Under the river (last tile) -> 1									[Not in predictions]
	13: All Seq -> 1
	14: All Pon -> 1
	15: Mix Suit -> 3
	16: All Terms/Honors -> 2 (5 - 3; from pung game)
	17: Full Suit -> 6
	18: All Terminals -> 5 (10 - 2 - 3; from pung game, all term/honors)
	19: All Honors -> 5 (10 - 2 - 3; from pung game, all term/honors)
	20: 4 little winds -> 6 
	21: 4 big winds ->10 (13 - 3; from pung game)
	22: Tenhou (Heavenly Hand) -> 11(13 - 1 - 1; from concealed, tsumo)		[Not in predictions]
	23: Renhou (Earthly Hand) -> 12 (13 - 1; from concealed)				[Not in predictions]
	24: 13 Orphans -> 10 (13 - 2 - 1; from all terms/honors, concealed)
	25: 4 quads -> 10 (13 - 3; from pung game)
	26: 4 concealed trips -> 6 (10 - 3 - 1; from pung game, concealed)
	27: Nine gates -> 9 (10 - 1; from concealed)

 */

public class Predictions
{
	public int[] validPredictID = {1,2,3,4,5,13,14,15,16,17,18,19,20,24,25,26,27};
	private ArrayList<Group> potentialGroup;
	private ArrayList<Integer> orphansGroup;
	private ArrayList<Integer> pairsGroup;
	private ArrayList<Integer> playerHand;
	
	public Predictions(ArrayList<Integer> in_hand)
	{
		this.playerHand = new ArrayList<Integer>(in_hand);
	}
	public Predictions(Predictions clone)
	{
		this.potentialGroup = clone.potentialGroup;
		this.orphansGroup = clone.orphansGroup;
		this.pairsGroup = clone.pairsGroup;
		this.playerHand = clone.playerHand;
	}
	protected ArrayList<Group> getGroups()
	{
		return this.potentialGroup;
	}
	protected ArrayList<Integer> getOrphan()
	{
		return this.orphansGroup;
	}
	protected ArrayList<Integer> getPairs()
	{
		return this.pairsGroup;
	}
	protected ArrayList<Integer> getHand()
	{
		return this.playerHand;
	}
	/**
	 * 
	 * @param in_hand: Any valid hand input
	 * @return 4 ArrayList<Integer> within Arraylist<Integer> that represents each suit of a mahjong hand.
	 */
	public static ArrayList<ArrayList<Integer>> suitDivide(ArrayList<Integer> in_hand)
	{
		ArrayList<ArrayList<Integer>> returnSuits = new ArrayList<ArrayList<Integer>>();
		for(int suit = 0; suit < 4; suit++)
		{
			returnSuits.add(new ArrayList<Integer>());
			for(int tile: in_hand)
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
	public static void main(String[] args)
	{
		int[] inHandExp = {1,1,2,2,3,3,16,17,18,31,31,31,32};
	}
}

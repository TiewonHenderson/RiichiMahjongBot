package bot_package;

import java.util.*;

public class Player
{
	
	/*
	 * Used to keep track of amount of untitled players
	 * Doesn't need to be accessed outside of this class
	 */
	protected static int unname_amt = 0;
	
	/*
	 * Used to keep track of wind counter (Use seatWind index to reference who to assign)
	 * Doesn't need to be accessed outside of this class
	 */
	protected static int seatWind_amt = 0;
	
	/*
	 * The player's name as string
	 */
	public String playerName;
	
	/*
	 * E = 0
	 * S = 1
	 * W = 2
	 * N = 3
	 */
	public int seatWind;
	
	/*
	 * Keeps track of all the player's tiles that have been dropped
	 */
	public ArrayList<Integer> dropPile;
	
	/*
	 * The current Playerhand of this player instance, has closed and open tiles in 2D ArrayList
	 */
	private PlayerHand playerHand;
	
	/*
	 * Used to created a new default player object, fields will be set as
	 * playerName = "UNNAMED" + unname_amt
	 * seatWind = seatWind_amt
	 * new drop pile and playerHand
	 */
	public Player()
	{
		this.playerName = "UNNAMED" + Integer.toString(unname_amt);
		this.seatWind = seatWind_amt;
		unname_amt += 1;
		seatWind_amt += 1;
		
		if(unname_amt > 3)
		{
			unname_amt = 0;
		}
		if(seatWind_amt > 3)
		{
			seatWind_amt = 0;
		}
		this.dropPile = new ArrayList<Integer>();
		this.playerHand = new PlayerHand();
	}
	
	/*
	 * Used to create a copy of inputed Player
	 */
	public Player(Player clone)
	{
		this.playerName = new String(clone.playerName);
		this.seatWind = clone.seatWind;
		this.dropPile = new ArrayList<Integer>(clone.dropPile);
		this.playerHand = new PlayerHand(clone.playerHand);
	}
	
	/*
	 * Used to get the player's current hand, 
	 * a pre-programmed password is used to restrict the usage of this method
	 * @return: Returns this player's hand is password is correct
	 * 			Returns empty 2D ArrayList<Integer> if incorrect
	 */
	public ArrayList<ArrayList<Integer>> getPlayerHand(String password)
	{	
		if(password.equals("ojhwaX9hOczYwTGxI472mVRhGpfhBTFR"))
		{
			return this.playerHand.getPlayerHand();
		}
		return new ArrayList<ArrayList<Integer>>();
	}
	
	public ArrayList<Integer> getPlayerDrops()
	{
		return this.dropPile;
	}
	
	/*
	 * Local class:
	 * Used to store currentHand of assigned player
	 * String representation of current player hand
	 */
	class PlayerHand
	{
		private ArrayList<ArrayList<Integer>> currentHand;
		private String mjSTRhand;
		
		public PlayerHand()
		{
			this.mjSTRhand = "";
			this.currentHand = new ArrayList<ArrayList<Integer>>();
			for(int i = 0; i < 2; i++) this.currentHand.add(new ArrayList<Integer>());
		}
		
		public PlayerHand(PlayerHand clone)
		{
			this.currentHand = new ArrayList<ArrayList<Integer>>(clone.getPlayerHand());
			this.mjSTRhand = new String(clone.getSTRhand());
		}
		
		public PlayerHand(ArrayList<ArrayList<Integer>> in_hand)
		{
			this.mjSTRhand = handto_mjSTR(in_hand);
			this.currentHand = new ArrayList<ArrayList<Integer>>(in_hand);
		}
		
		public PlayerHand(String in_STRformat)
		{
			this.mjSTRhand = new String(in_STRformat);
			this.currentHand = mjSTRtranslate(in_STRformat);
		}
		
		/*
		 * 
		 */
		private ArrayList<ArrayList<Integer>> getPlayerHand()
		{
			return this.currentHand;
		}
		
		/*
		 * Private as hand shouldn't 
		 */
		private String getSTRhand()
		{
			return this.mjSTRhand;
		}
		
		/*
		 * MJ String presentation (mjSTR format) uses key character to present suits
		 * For example 
		 * 'm' = mans = character suit
		 * 'p' = pins = circle suit
		 * 's' = sous = bamboo suit
		 * 
		 * key characters to split closed/open tiles
		 * 'c' = closed ending the left
		 * 'o' = open ending the left
		 * 
		 * 
		 * Playerhand are represented as 2 ArrayList<Integer>,
		 * ArrayList 0 = In hand Tiles
		 * ArrayList 1 = Open Tiles
		 */
		public static ArrayList<ArrayList<Integer>> mjSTRtranslate(String in_mjSTR)
		{
			if(in_mjSTR.length() <= 0)
			{
				return new ArrayList<ArrayList<Integer>>();
			}
			ArrayList<ArrayList<Integer>> returnArray = new ArrayList<ArrayList<Integer>>(2);
			for(int i = 0; i < 2; i++) returnArray.add(new ArrayList<Integer>());
			
			char[] indicators = {'m', 'p', 's', 'z'};
			int open = 0;
			int current_suit = 0;
			
			for(int i = 0; i < in_mjSTR.length(); i++)
			{
				char current_element = Character.toLowerCase(in_mjSTR.charAt(i));
				if(Character.compare(current_element, indicators[current_suit]) == 0)
				{
					current_suit += 1;
					if(current_suit == 4)
					{
						current_suit = 0;
					}
				}
				else if(Character.compare(current_element, 'c') == 0)
				{
					open += 1;
					current_suit = 0;
				}
				else if(Character.isDigit(current_element))
				{
					returnArray.get(open).add((Character.getNumericValue(current_element) + (9 * current_suit)) - 1);
				}
			}
			return returnArray;
		}
		
		public static String handto_mjSTR(ArrayList<ArrayList<Integer>> in_hand)
		{
			if(in_hand.get(0).size() <= 0 || in_hand.size() != 2)
			{
				return "";
			}
			String returnSTR = "";
			int suitnum = in_hand.get(0).get(0)/9;
			String[] suits = {"m", "p", "s", "z"};
			
			
			for(int i = 0; i < in_hand.size(); i++)
			{
				suitnum = in_hand.get(i).get(0)/9;
				for(int j = 0; j < suitnum; j++) returnSTR += suits[j];
				String[] coDetector = {"c", "o"};
				for(int j = 0; j < in_hand.get(i).size(); j++)
				{
					int current_int = in_hand.get(i).get(j);
					if(current_int/9 > suitnum)
					{
						returnSTR += suits[suitnum];
						suitnum = in_hand.get(i).get(j)/9;
					}
					returnSTR += Integer.toString((current_int - (9 * suitnum) + 1));
				}
				for(int j = suitnum; j < suits.length; j++) returnSTR += suits[j];
				returnSTR += coDetector[i];
				if(in_hand.get(1).size() == 0)
				{
					for(int j = 0; j < suits.length; j++) returnSTR += suits[j];
					returnSTR += "o";
					break;
				}
			}
			return returnSTR;
		}
		
		/*
		 * Task: Using limbogroup search to search all groups
		 * Return: 
		 */
		public static ArrayList<Group> returnGroups()
		{
			ArrayList<Group> returnGroups = new ArrayList<Group>();
			
			return returnGroups;
		}
	}
	public static void main(String[] args)
	{
		System.out.println(PlayerHand.mjSTRtranslate("mp1112345678999szcmpszo"));
		System.out.println(PlayerHand.mjSTRtranslate("123m456p789szcmps111zo"));
		
		ArrayList<ArrayList<Integer>> expHand = PlayerHand.mjSTRtranslate("123m456p789szcmps111zo");
		System.out.println(PlayerHand.handto_mjSTR(expHand));
		
	}
}

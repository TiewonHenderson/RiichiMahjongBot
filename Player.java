package bot_package;

import java.util.*;
import bot_package.Group;

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

	public String playerName;
	
	/*
	 * E = 0
	 * S = 1
	 * W = 2
	 * N = 3
	 */
	public int seatWind;

	/*
     * Integer to keep track of drawn flowers, will be of magnitude of 10^4
     * Use seatWind to determine what flower is the player's good flower (10^seatWind)
     */
    public int flower;
    
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
		this.flower = 0;
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
		this.flower = clone.flower;
	}
	
	/*
	 * Used to all details about this instance's hand, includes open/closed decalred groups
	 */
	public PlayerHand getPlayerHand()
	{	
		return this.playerHand;
	}
	
	public boolean setPlayerHand(PlayerHand in_playhand)
	{
		try
		{
			this.playerHand = new PlayerHand(in_playhand);
			return true;
		}
		catch(Exception e)
		{
			return false;
		}
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
	static class PlayerHand
	{
		private ArrayList<Integer> currentHand;
		public ArrayList<Group> declaredGroups;
		private String mjSTRhand;
		private ArrayList<String> groupSN_list;
		
		
		
		/**
		 * Default constructor
		 */
		public PlayerHand()
		{
			this.currentHand = new ArrayList<Integer>();
			this.declaredGroups = new ArrayList<Group>();
			this.mjSTRhand = "";
		}
		
		/**
		 * Used to clone a Player's Hand
		 * @param clone: A PlayerHand wanted to be copy to another instance
		 */
		public PlayerHand(PlayerHand clone)
		{
			this.currentHand = new ArrayList<Integer>(clone.currentHand);
			this.declaredGroups = new ArrayList<Group>(clone.declaredGroups);
			this.mjSTRhand = new String(clone.mjSTRhand);
		}
		
		/*
		 * Assumes all tiles are concealed
		 */
		public PlayerHand(ArrayList<Integer> in_hand)
		{
			this.currentHand = new ArrayList<Integer>(in_hand);
			this.declaredGroups = new ArrayList<Group>();
//			this.mjSTRhand = handto_mjSTR(in_hand);
		}
		
		public PlayerHand(ArrayList<Integer> in_hand, ArrayList<Group> declared_groups)
		{
			this.currentHand = new ArrayList<Integer>(in_hand);
			this.declaredGroups = new ArrayList<Group>(declared_groups);
//			this.mjSTRhand = handto_mjSTR(in_hand);
		}
		
		public PlayerHand(String in_STRformat)
		{
//			PlayerHand mjSTR_to_Hand = mjSTRtranslate(in_STRformat);
//			this.currentHand = mjSTR_to_Hand.currentHand;
//			this.declaredGroups = mjSTR_to_Hand.declaredGroups;
			this.mjSTRhand = new String(in_STRformat);
		}
		
		/*
		 * Returns the current ArrayList<Integer> of the Player's inside_hand
		 */
		public ArrayList<Integer> getCurrentHand()
		{
			return this.currentHand;
		}
		
		/*
		 * Returns the current ArrayList<Group> that are visible to other players
		 * This however does include the concealed declared quads that would consider the hand
		 * still be concealed
		 */
		public ArrayList<Group> getDeclaredGroup()
		{
			return this.declaredGroups;
		}
		
		/**
		 * If required, set the current Player inside_hand to a new hand
		 * @param in_newHand: The new hand to be set to this instance
		 * @return: True if the new inside hand has been set to the PlayerHand, false if error was raised
		 */
		public boolean setCurrentHand(ArrayList<Integer> in_newHand)
		{
			try
			{
				this.currentHand = new ArrayList<Integer>(in_newHand);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * If required, 
		 * set the current Player's declared groups to a new ArrayList<Group> of declared Groups
		 * @param in_newGroups: The new ArrayList<Group> for this PlayerHand instance
		 * @return: True if the new_groups has been set, false if an error was raised
		 */
		public boolean setDeclaredGroup(ArrayList<Group> in_newGroups)
		{
			try
			{
				this.declaredGroups = new ArrayList<Group>(in_newGroups);
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
		/**
		 * Used to add new declared groups to unique PlayerHand instance
		 * @param new_group: SINGULAR group to be added to this instance of PlayerHand
		 * @return: True if the new Group was added, false if an error was raised
		 */
		public boolean addDeclaredGroups(Group new_group)
		{
			try
			{
				this.declaredGroups.add(new Group(new_group));
				return true;
			}
			catch(Exception e)
			{
				return false;
			}
		}
		
	}
	public static void main(String[] args)
	{
		
	}
}

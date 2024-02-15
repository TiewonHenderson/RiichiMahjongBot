package bot_package;

import java.util.*;


public class GroupSearch
{
	class GroupsAndNeededTiles
	{
		public Player currentPlayer;
		public ArrayList<Group> confirmedGroups = new ArrayList<Group>();
		public ArrayList<Group> uncompGroups = new ArrayList<Group>();
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
		public GroupsAndNeededTiles(Player currentPlayer, ArrayList<Group> confirmedGroups, ArrayList<Group> uncompGroups, ArrayList<Integer> remainingTiles)
		{
			this.currentPlayer = new Player(currentPlayer);
			this.confirmedGroups = new ArrayList<Group>(confirmedGroups);
			this.uncompGroups = new ArrayList<Group>(uncompGroups);
			this.remainingTiles = new ArrayList<Integer>(remainingTiles);
		}
		
	}
	public static void main(String[] args)
	{
		
	}
}

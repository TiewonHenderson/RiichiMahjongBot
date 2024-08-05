package bot_package_v2;

import java.util.ArrayList;

import bot_package_v2.MJ_tools.Tile;

public class Player
{
	public enum Wind
	{
		EAST, SOUTH, WEST, NORTH;
	}
	
	/**
	 * The Player's inputted name
	 */
	public String name_;
	
	/**
	 * This will indicate which seat wind this Player current is
	 */
	public Wind wind_;
	
	/**
	 * This will store the amount of points this Player has within a Mahjong session
	 */
	public int points_;
	
	/**
	 * This represents the Mahjong hand of this specific Player, can be set to visible hand if this Player is the User
	 */
	public Mahjong_hand mj_hand_;
	
	/**
	 * Default Constructor
	 * 
	 * @info
	 * Creates a null Character with no characteristic (cannot enter a Mahjong session)
	 */
	public Player()
	{
		this.name_ = "";
		this.wind_ = null;
		this.points_ = -1;
	}
	
	/**
	 * Named Constructor
	 * @param name The name for this individual Player
	 * 
	 * @info
	 * This constructor does not assign a valid wind value
	 */
	public Player(String name)
	{
		this.name_ = name;
		this.wind_ = null;
		this.points_ = 25000;
	}
	
	/**
	 * New Player Constructor
	 * @param name The name for this individual Player
	 * @param wind The seat wind of this current Player
	 */
	public Player(String name, Wind wind)
	{
		this.name_ = name;
		this.wind_ = wind;
		this.points_ = 25000;
	}
	
	/**
	 * Cloning Constructor
	 * @param clone A different Player object to be copied
	 */
	public Player(Player clone)
	{
		this.name_ = clone.name_;
		this.wind_ = clone.wind_;
		this.points_ = clone.points_;
	}
	
	/**
	 * Mutator function for setting Player's name
	 * @param name A new name for this instance of Player
	 * @info
	 * COnditions for new name:
	 * - Length cannot exceed 19
	 * - Cannot be the same as previous name
	 */
	public boolean setName(String name)
	{
		if(name.length() < 20 && this.name_.compareTo(name) != 0)
		{
			this.name_ = name;
			return true;
		}
		return false;
	}
	
	/**
	 * Accessor method for name
	 * @return The current name of this instance of Player
	 */
	public String getName()
	{
		return this.name_;
	}
	
	/**
	 * Mutator method for Player's seat wind
	 * @param wind A new wind value for this instance of Player
	 */
	public void setWind(Wind wind)
	{
		this.wind_ = wind;
	}
	
	/**
	 * Accessor method for seat wind
	 * @return The current seat wind of this instance of Player
	 */
	public Wind getWind()
	{
		return this.wind_;
	}
	
	/**
	 * Mutator method to setting new points
	 * @param points The new amount of points for this instance of player
	 */
	public void setPoints(int points)
	{
		this.points_ = points;
	}
	
	/**
	 * Accessor method for Player points
	 * @return The current amount of points for this instance of Player
	 */
	public int getPoints()
	{
		return this.points_;
	}
	
	/**
	 * Mutator method for this Player's Mahjong_hand
	 * @param new_hand a New hand that is checked to be a possible Mahjong hand
	 * @return True if this instance of Player Mahjong_hand was set to the parameter value, false results in no changes
	 */
	public boolean setMahjong_hand(Mahjong_hand new_hand)
	{
		if(!new_hand.isValidHand()) return false;
		this.mj_hand_ = new_hand;
		return true;
	}
	
	/**
	 * Accessor method for Mahjong_hand
	 * @return The mj_hand_ of this Instance of Player
	 */
	public Mahjong_hand getMahjong_hand()
	{
		return this.mj_hand_;
	}
	
	public Visible_hand getVisible_hand() throws IllegalStateException
	{
		if(!this.isVisible_hand())
		{
			throw new IllegalStateException("A Player's field mj_hand_ is not set as a Visible_hand");
		}
		return (Visible_hand)this.mj_hand_;
	}
	
	/**
	 * This method makes sure this Player has a Visible_hand set for the mj_hand_ field
	 * @return True if the field sorts a Visible_hand, false if otherwise
	 */
	public boolean isVisible_hand()
	{
		if(this.mj_hand_ instanceof Visible_hand) return true;
		return false;
	}
	
	/**
	 * @info
	 * Separate class that represents a Mahjong hand for each individual Player
	 */
	public static class Mahjong_hand
	{
		/**
		 * The drop pile represents discard tiles
		 */
		public ArrayList<Tile> drop_pile_;
		
		/**
		 * This represents the groups melded by calling on discarded tiles, with the exception of hidden quads
		 */
		public ArrayList<Group> declared_groups_;
		
		/**
		 * Default constructor
		 * 
		 * @info
		 * Creates two empty ArrayLists
		 */
		public Mahjong_hand()
		{
			this.drop_pile_ = new ArrayList<Tile>();
			this.declared_groups_ = new ArrayList<Group>();
		}
		
		/**
		 * Drop_pile constructor
		 * @param drop_pile The current drop_pile of a Player
		 */
		public Mahjong_hand(ArrayList<Tile> drop_pile)
		{
			this.drop_pile_ = new ArrayList<Tile>(drop_pile);
			this.declared_groups_ = new ArrayList<Group>();
		}
		
		/**
		 * Existing hand constructor
		 * @param drop_pile The current drop_pile of a Player
		 * @param declared_groups The declared groups of the same Player
		 */
		public Mahjong_hand(ArrayList<Tile> drop_pile, ArrayList<Group> declared_groups)
		{
			this.drop_pile_ = new ArrayList<Tile>(drop_pile);
			this.declared_groups_ = new ArrayList<Group>(declared_groups);
		}
		
		/**
		 * Cloning constructor
		 * @param clone An another instance of Mahjong_hand to be cloned
		 */
		public Mahjong_hand(Mahjong_hand clone)
		{
			this.drop_pile_ = new ArrayList<Tile>(clone.drop_pile_);
			this.declared_groups_ = new ArrayList<Group>(clone.declared_groups_);
		}
		
		/**
		 * Mutator method to set the whole drop_pile
		 * @param drop_pile completely new drop_pile for this instance of Mahjong_hand
		 * @pre
		 * - The drop_piles includes valid tiles
		 * - The there are no more then 4 copies of a specific tile
		 */
		public void setDrop_pile(ArrayList<Tile> drop_pile)
		{
			this.drop_pile_ = new ArrayList<Tile>(drop_pile);
		}
		
		/**
		 * Mutator method with better use, adds valid tile into drop_pile
		 * @param new_drop A discarded tile that represents a tile_val
		 * @return True if the tile has been added, otherwise false
		 */
		public boolean addDrop_pile(Tile new_drop)
		{
			this.drop_pile_.add(new_drop);
			return true;
		}
		
		/**
		 * Accessor method to get this Mahjong_hand drop_pile
		 * @return A Tile ArrayList that represents a MJ_hand drop history
		 */
		public ArrayList<Tile> getDrop_pile()
		{
			return this.drop_pile_;
		}
		
		/**
		 * Mutator method to set this Mahjong_hand's declared groups
		 * @param new_groups A new ArrayList of declared groups
		 * @pre The groups are complete and valid
		 */
		public void setDeclaredGroups(ArrayList<Group> new_groups)
		{
			this.declared_groups_ = new ArrayList<Group>(new_groups);
		}
		
		/**
		 * Mutator method to increment new Group to declared_groups Arraylist
		 * @param new_group The new group that was melded for this Mahjong_hand
		 */
		public boolean addDeclaredGroup(Group new_group)
		{
			if(new_group.getGroupStatus() > 0 && new_group.isDeclared())
			{
				this.declared_groups_.add(new_group);
				return true;
			}
			return false;
		}
		
		/**
		 * Accessor method to get list of declared groups
		 * @return This instance of declared groups
		 */
		public ArrayList<Group> getDeclaredGroups()
		{
			return this.declared_groups_;
		}
		
		/**
		 * 
		 * @return true if this Mahjong hand is somewhat possible, otherwise false
		 */
		public boolean isValidHand()
		{
			//Checks for impossible scenarios
			if(this.drop_pile_.size() < 0 || 
			   this.drop_pile_.size() > 132 || 
			   this.declared_groups_.size() > 4 ||
			   this.declared_groups_.size() < 0) return false;
			
			//Checks for non-discarded Tiles
			for(Tile tile: this.drop_pile_)
			{
				if(tile.getDiscardType() < 0) return false;
			}
			//Checks for non-complete / invalid Groups
			for(Group group: this.declared_groups_)
			{
				if(group.getGroupStatus() < 1) return false;
			}
			return true;
		}
	}
	
	/**
	 * Inherits Mahjong_hand as it universally represents any hand
	 * @info
	 * This class will represent the User's hand (all visible hands)
	 * 		where the tiles are visible and the game will be in this point of view
	 */
	public static class Visible_hand extends Mahjong_hand
	{
		/**
		 * @note This will not include ANY declared groups
		 * This represents the current ArrayList<Tile> Mahjong hand of the User
		 */
		private ArrayList<Tile> visible_hand_;
		
		/**
		 * Default constructor, makes an empty Mahjong hand
		 */
		public Visible_hand()
		{
			this.visible_hand_ = new ArrayList<Tile>();
		}
		
		/**
		 * Parameterized Constructor, pre-initializes the Hand
		 * @param visible_hand The current hand of the User
		 */
		public Visible_hand(ArrayList<Tile> visible_hand)
		{
			this.visible_hand_ = new ArrayList<Tile>(visible_hand);
		}
		
		/**
		 * Mutator method to setting the User's Hand
		 * @param new_hand A new valid ArrayList<Tile> that represents a hand
		 * @return True if the hand is valid and is set to the instance field, false is nothing was changed
		 */
		public boolean setVisibleHand(ArrayList<Tile> new_hand)
		{
			if(new_hand.size() >= 1 && new_hand.size() < 14)
			{
				for(Tile each_tile: new_hand)
				{
					if(each_tile.getTileID() == -1) {return false;}
				}
				this.visible_hand_ = new ArrayList<Tile>(new_hand);
				return true;
			}
			return false;
		}
		
		/**
		 * Accessor method to get this instance of User Mahjong hand
		 * @return ArrayList<Tile> that represents the User's Hand
		 */
		public ArrayList<Tile> getVisibleHand()
		{
			return this.visible_hand_;
		}
	}
}

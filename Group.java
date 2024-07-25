package bot_package_v2;

import java.util.ArrayList;

import bot_package_v2.MJ_tools.*;

public class Group 
{
	/**
	 * This represents the Group type for this individual group
	 * The Group object will always fall under one of these categories
	 */
	public enum Group_type
	{
		INVALID, FLOAT_TILE, INCOMP_SEQ, PAIR, SEQ, TRIPLET, QUAD;
	}
	
	//ArrayList<Tile> that represents all the Tiles within this Group
	public ArrayList<Tile> tile_list_ = new ArrayList<Tile>();
	
	public boolean declared_; //mainly to separate declared quads
	public boolean concealed_;
	
	
	//Constructors
	
	/*
	 * Default Constructor
	 */
	public Group()
	{
		this.declared_ = false;
		this.concealed_ = true;
	}
	
	/**
	 * ArrayList<Tile> object compatible constructor for Group object
	 * @param in_group An ArrayList<Tile> that represents a tile group (can be incomplete without declared)
	 * @param declared Whether the group is even declared, so not even in the Mahjong hand
	 * @param concealed whether the group is concealed(considered not visible to others, cannot rob unless special circumstances)
	 * @param extra Null parameter in order to perform overloading 
	 */
	public Group(ArrayList<Tile> in_group, boolean declared, boolean concealed, Object extra)
	{
		this.tile_list_ = new ArrayList<Tile>(in_group);
		this.declared_ = declared;
		this.concealed_ = concealed;
	}
	
	/**
	 * ArrayList<Integer> object compatible constructor for Group object
	 * @param in_group An ArrayList<Integer> that represents a tile group (can be incomplete without declared)
	 * @param declared Whether the group is even declared, so not even in the Mahjong hand
	 * @param concealed whether the group is concealed(considered not visible to others, cannot rob unless special circumstances)
	 */
	public Group(ArrayList<Integer> in_group, boolean declared, boolean concealed)
	{
		this.tile_list_ = Tile.createTileAL(in_group);
		this.declared_ = declared;
		this.concealed_ = concealed;
	}
	
	/**
	 * int[] object Compatible Constructor for Group obj
	 * @param in_group A typical 3/4 tile int[] that represents a tile group
	 * @param declared mainly focused on concealed kan as the hand still remains concealed but kan is declared
	 * @param concealed Declare whether the group is concealed
	 */
	public Group(int[] in_group, boolean declared, boolean concealed)
	{
		for(int i = 0; i < in_group.length; i++) tile_list_.add(new Tile(in_group[i]));
		this.declared_ = declared;
		this.concealed_ = concealed;
	}
	
	/**
	 * 
	 * @param in_group A String representation of the Group, use ',' to separate tiles
	 * @param declared mainly focused on concealed kan as the hand still remains concealed but kan is declared
	 * @param concealed Declare whether the group is concealed
	 */
	public Group(String in_group, boolean declared, boolean concealed)
	{
		ArrayList<Integer> group = new ArrayList<Integer>();
		String temp_holder = "";
		for(int i = 0; i < in_group.length(); i++)
		{
			if(in_group.charAt(i) == ',')
			{
				try 
				{
					group.add(Integer.parseInt(temp_holder));
					temp_holder = "";
				}
				catch(NumberFormatException e)
				{
					temp_holder = "";
					break;
				}
			}
			else
			{
				temp_holder += in_group.charAt(i);
			}
		}
		this.declared_ = declared;
		this.concealed_ = concealed;
	}
	
	/**
	 * Default value for concealed is true
	 * @param in_group A typical 3/4 tile ArrayList<Integer> that represents a tile group
	 */
	public Group(ArrayList<Integer> in_group)
	{
		this.tile_list_ = Tile.createTileAL(in_group);
		this.declared_ = false;
		this.concealed_ = true;
	}

	/**
	 * Default value for concealed is true
	 * @param in_group A typical 3/4 tile int[] that represents a tile group
	 */
	public Group(int[] in_group)
	{
		this.tile_list_ = Tile.createTileAL(MJ_Hand_tools.createArrayList(in_group));
		this.declared_ = false;
		this.concealed_ = true;
	}
	
	/*
	 * Used to create a new clone object of Group
	 */
	public Group(Group clone)
	{
		this.tile_list_ = clone.tile_list_;
		this.declared_ = clone.declared_;
		this.concealed_ = clone.concealed_;
	}
	
	//Mutators
	
	/**
	 * Used to alter if Group is concealed or not
	 * @param in_status The new status of the group, if it's concealed or not
	 */
	public void setConcealed(boolean in_status)
	{
		this.concealed_ = in_status;
	}
	
	/**
	 * Used to alter if Group is now declared or not
	 * @param in_status The new status of the group, if it's declared or not
	 */
	public void setDeclared(boolean in_status)
	{
		this.declared_ = in_status;
	}
	
	/**
	 * 
	 * @param new_group A ArrayList<Tile> that represents a Group
	 * @return sets new group to argument and returns true if new_group.size() < 5 and > 0.
	 * 			Does nothing returns false if condition is not met.
	 */
	public boolean setGroup(ArrayList<Tile> new_group)
	{
		if(new_group.size() > 0 && new_group.size() < 5)
		{
			this.tile_list_ = new ArrayList<Tile>(new_group);
			return true;
		}
		return false;
	}
	
	/**
	 * Accessor method for tile list
	 * @return The ArrayList<Tile> that represents the Tiles for a specific Group
	 */
	public ArrayList<Tile> getGroupTiles()
	{
		return this.tile_list_;
	}
	
	/**
	 * Accessor method for if this Group is declared
	 * @return The boolean value for declaration status
	 */
	public boolean isDeclared()
	{
		return this.declared_;
	}
	
	/**
	 * Accessor method for if this Group is concealed
	 * @return The boolean value for the concealed status
	 */
	public boolean isConcealed()
	{
		return this.concealed_;
	}
	
	//Object functions
	
	/**
	 * Used to print the string representation of the tile_list_
	 */
	public String toString()
	{
		String return_str = "(";
		for(Tile tile: this.tile_list_) 
		{
			return_str += Integer.toString(tile.getTileID()) + Tile.suit_reference[tile.getTileID()/9] +  ",";
		}
		try
		{
			return return_str.substring(0,return_str.length() - 1) + ")";
		}
		catch(Exception e)
		{
			return "";
		}
	}
	/**
	 * 
	 * @return The String value to typically add into it's total groupSN
	 */
	public String toGroupSN(int min)
	{
		String return_str = "(";
		for(Tile tile_id: this.tile_list_) 
		{
			return_str += Integer.toString(Tile.tileID_to_PlayVal(tile_id) - min);
		}
		return return_str + ")";
	}
	
	/**
	 * 
	 * @return True if this Group is capable of turning into an Added quad and did update that status, false if unchanged
	 */
	public boolean upgradePon()
	{
		if(this.getGroupInfo()[0] == 3 && this.getGroupInfo()[2] == 2)
		{
			this.tile_list_.add(this.tile_list_.get(0));
			return true;
		}
		return false;
	}
	
	/**
	 * If any digit = 0, that means the group is invalid/Uncomplete
	 * @return Gets information of this instance of group as integer list
	 * 			index 0 = group type, i.e 1 = sequence, 2 = triplets, 3 = quads
	 * 			index 1 = group suit, i.e -1 = invalid/mixed, 0 = mans, 1 = pins, 2 = sous, 3 = honors
	 * 			index 2 = group concealed, i.e 0 = concealed/undeclared, 1 = concealed/declared, 2 = open/declared, (open/undeclared is impossible)
	 */
	public int[] getGroupInfo()
	{
		int[] return_data = {-3, -1, 0};
		if(this.tile_list_.size() <= 0)
		{
			return return_data;
		}
		int groupType = groupStatus(this.tile_list_).ordinal();
		return_data[0] = groupType;
		int suit = this.tile_list_.get(0).getTileID()/9;
		for(int index = 0; index < this.tile_list_.size() - 1; index++)
		{
			if(this.tile_list_.get(index).getTileID()/9 != this.tile_list_.get(index + 1).getTileID()/9)
			{
				suit = -1;
				break;
			}
		}
		if(!this.concealed_)
		{
			return_data[2]++;
		}
		if(this.declared_)
		{
			return_data[2]++;
		}
		return_data[1] = suit;
		return return_data;
		
	}
	
	/**
	 * @return The group type of this Group, 
	 * -3 = invalid
	 * -2 = floating
	 * -1 = incomp sequence
	 * 0 = pair
	 * 1 = sequence
	 * 2 = triplets
	 * 3 = quads
	 */
	public int getGroupStatus()
	{
		return groupStatus(this.getGroupTiles()).ordinal() - 3;
	}
	
	/**
	 * 
	 * @return The Group_type of this instance of Group
	 */
	public Group_type groupStatus()
	{
		return groupStatus(this.getGroupTiles());
	}
	
	/**
	 * 
	 * @param in_group An ArrayList<Integer> of size 3/4 to check if valid group
	 * @return The group type of the inputted ArrayList<Integer>, 
	 * -3 = invalid
	 * -2 = floating
	 * -1 = incomp sequence
	 * 0 = pair
	 * 1 = sequence
	 * 2 = triplets
	 * 3 = quads
	 */
	public static Group_type groupStatus(ArrayList<Tile> in_group)
	{
	    if(in_group.size() == 0 || in_group.size() > 4)
	    {
	        return Group_type.INVALID;
	    }
	    
	    //checks if there is only 1 distinct element in ArrayList<Integer>
	    ArrayList<Integer> tile_ids = new ArrayList<Integer>();
	    for(Tile tile: in_group) tile_ids.add(tile.getTileID());
	    boolean all_Same = tile_ids.stream().distinct().count() == 1;
	    
	    //Checks quads
	    switch(in_group.size())
	    {
	    	case 4:
	    		//check if all the tiles are the same for quads
	    		if(all_Same)
	    		{
	    			return Group_type.QUAD;
	    		}
	    		return Group_type.INVALID;
	    	case 3:
	    		//check if all the tiles are the same for triplets
	    		if(all_Same)
	    		{
	    			return Group_type.TRIPLET;
	    		}
	    		//check if all the tiles are increments of each other for sequences
	    		ArrayList<Tile> sortedGroup = MJ_Hand_tools.sortTiles(in_group, false);
	    	    for(int i = 0; i < sortedGroup.size() - 1; i++)
	    	    {
	    	        if(sortedGroup.get(i).getTileID() + 1 != sortedGroup.get(i + 1).getTileID())
	    	        {
	    	            return Group_type.INVALID;
	    	        }
	    	    }
	    	    return Group_type.SEQ;
	    	case 2:
	    		//If the two tiles are the same, its a pair
	    		if(all_Same)
	    		{
	    			return Group_type.PAIR;
	    		}
	    		
	    		//If the two tiles are in talking distances, -2 -1 +1 +2, then its an incomplete sequence
	    		if(Math.abs(in_group.get(0).getTileID() - in_group.get(1).getTileID()) <= 2)
	    		{
	    			return Group_type.INCOMP_SEQ;
	    		}
	    		return Group_type.INVALID;
	    	case 1:
	    		//Just 1 tile = floating tile
	    		return Group_type.FLOAT_TILE;
	    	default:
	    		return Group_type.INVALID;
	    }
	}
}

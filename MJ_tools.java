package bot_package_v2;

import java.util.*;
import java.util.function.*;
import bot_package_v2.Player.Wind;

public class MJ_tools 
{
	
	/**
	 * This represents a local class and holds key information for each tile
	 */
	public static class Tile implements Comparable<Tile>
	{
		protected final int tile_id_;			//A int ID that would respresent a tile
		protected final boolean red_five_;		//If this Tile instance is an Aka Dora
		public final Wind assigned_wind_;		//This represents who has/discarded this Tile
		
		/**
		 * Represents how this Tile was discarded, 
		 * -1 	== draw/in_hand
		 * 0/1 	== tsumogiri/tedashi respectively
		 * 10 	== riichi Tile
		 */
		public int discard_type_ = -1;
		
		//Used for character reference
		public static final char[] suit_reference = {'m','p','s','z'};
		public static final char[] wind_reference = {'e','s','w','n'};
		
		/**
		 * A semi-Default constructor that only appoints a tile_id, suitable for in_hand
		 * @param tile_id The tile value for this specific tile object
		 */
		public Tile(int tile_id)
		{
			if(isValidTile(tile_id))
			{
				this.tile_id_ = tile_id;
			}
			else
			{
				this.tile_id_ = -1;
			}
			this.assigned_wind_ = null;
			this.red_five_ = false;
		}
		
		/**
		 * Parameterized constructor has to have tile_val and player's wind or this tile is invalid
		 * @param tile_id The tile value for this specific tile object
		 * @param player The player assigned by their wind rotation that discarded this Tile
		 */
		public Tile(int tile_id, Wind player)
		{
			if(isValidTile(tile_id))
			{
				this.tile_id_ = tile_id;
			}
			else
			{
				this.tile_id_ = -1;
			}
			this.assigned_wind_ = player;
			this.discard_type_ = 0;
			this.red_five_ = false;
		}
		
		/**
		 * Parameterized constructor has to have tile_val and player's wind or this tile is invalid
		 * @param tile_id The tile value for this specific tile object
		 * @param windID The player assigned by their wind rotation that discarded this Tile (represented by integer)
		 */
		public Tile(int tile_id, int windID)
		{
			if(isValidTile(tile_id))
			{
				this.tile_id_ = tile_id;
			}
			else
			{
				this.tile_id_ = -1;
			}
			this.assigned_wind_ = Wind.values()[windID];
			this.discard_type_ = 0;
			this.red_five_ = false;
		}
		
		/**
		 * Tile constructor with more information
		 * @param tile_id The tile value for this specific tile object
		 * @param red_5 For riichi mahjong, if this tile is a Aka dora, this would be true
		 * @param discard_type If this tile was discarded, this would represent the discard type
		 */
		public Tile(int tile_id, Wind player, boolean red_5, int discard_type)
		{
			if(isValidTile(tile_id))
			{
				this.tile_id_ = tile_id;
			}
			else
			{
				this.tile_id_ = -1;
			}
			this.assigned_wind_ = player;
			this.discard_type_ = discard_type;
			this.red_five_ = red_5;
		}
		
		/**
		 * Accessor method to get the tile_id
		 * @return The tile_id of this instance of Tile
		 */
		public int getTileID()
		{
			return this.tile_id_;
		}
		
		/**
		 * Custom Accessor method for specific Play value of the tile
		 * @return The custom play value within the given suit of this Tile
		 */
		public int getPlayValue()
		{
			return tileID_to_PlayVal(this.tile_id_);
		}
		
		/**
		 * Accessor method to get the type of discard for this Tile
		 * @return An integer value for the type of discard corresponding to this Tile (-1 == not discarded)
		 */
		public int getDiscardType()
		{
			return this.discard_type_;
		}
		
		/**
		 * Accessor method to get who discarded this Tile
		 * @return The Wind enum that represents the Player's wind rotation
		 */
		public Wind getWhoDiscarded()
		{
			return this.assigned_wind_;
		}
		
		/**
		 * toString method
		 * @return How the tile will be represented by its play_value followed by it's suit
		 */
		public String toString()
		{
			return Integer.toString(this.getPlayValue()) + suit_reference[this.tile_id_/9];
		}
		
		/**
		 * Accessor method to return if this Tile is a aka-dora
		 * @return The state of which this Tile is a aka=dora (true is it is, false if not)
		 */
		public boolean isRed5()
		{
			return this.red_five_;
		}
		
		/**
		 * Mutator method to set the state of discard of the tile
		 * @param discard_type The integer representation of the discard type of this Tile
		 * @return True if a value was set, false if no changes happen
		 * 
		 * @info
		 * 	0: Represents the tile hasn't been discarded
		 * 	1: Represents the tile was tsumogiri	(From wall)
		 * 	2: Represents the tile was tedashi		(From hand)
		 */
		public boolean setDiscardType(int discard_type)
		{
			switch(discard_type)
			{
				case 0:
					this.discard_type_ = 0;
					return true;
				case 1:
					this.discard_type_ = 1;
					return true;
				case 2:
					this.discard_type_ = 2;
					return true;
				default:
					return false;
			}
		}
		
		/**
		 * @info
		 * This function will display handful of information, including (with respect to their index)
		 * 0: Suit 				//range = [0,3]
		 * 1: Play Value		//range = [1,9], -1 to honors
		 * 2: Orphan			//range = [0,1]
		 * 3: Discard_type		//range = [0,2]
		 * @return An integer Array that conveys 4 pieces of key information
		 */
		public int[] getTileInfo()
		{
			int is_orphan = 0;
			int play_val = this.getPlayValue();
			if(play_val > 10) 
			{
				play_val = -1;
				is_orphan = 1;
			}
			else if(play_val == 0 || play_val == 9)
			{
				is_orphan = 1;
			}
			return new int[] {this.tile_id_/9, play_val, is_orphan, this.getDiscardType()};	
		}
		
		/**
		 * @override override to be able to compare 
		 * @param otherTile Another Tile to be compared to this instance of Tile
		 */
	    public int compareTo(Tile otherTile) {
	        return Integer.compare(this.getTileID(), otherTile.getTileID());
	    }
		
		/**
		 * 
		 * @param in_tile_id A singular int value to be converted from tile_id to play_val
		 * @return The play_val of the inputed integer, note this function only works from 0-33 inclusive
		 */
		public static int tileID_to_PlayVal(int in_tile_id)
		{
			if(in_tile_id < 0 || in_tile_id > 33) {return -1;}
			/*
			 * (in_tile_id / 9) = suit by flooring
			 * in_tile_id - ((in_tile_id / 9) * 9) = converts it into a man tile (all man = play_val - 1)
			 */
			else return in_tile_id - ((in_tile_id / 9) * 9) + 1;
		}
		
		/**
		 * Overridding method for tileID_to_PlayVal
		 * @param in_tile_id A Tile object
		 * @return The play_val of the inputed integer, note this function only works from 0-33 inclusive
		 */
		public static int tileID_to_PlayVal(Tile tile)
		{
			return tileID_to_PlayVal(tile.getTileID());
		}
		
		/**
		 * 
		 * @param in_tileID The tile_id that would attempt to represent a tile
		 * @return True if the tile is valid, otherwise false if out of bounds
		 */
		public static boolean isValidTile(int in_tileID)
		{
			if(in_tileID < 0 || in_tileID > 33)
			{
				return false;
			}
			return true;
		}
		
		/**
		 * 
		 * @param in_tileID1 The first TileID input to compare it's suit to the second Tile
		 * @param in_tileID2 The second TileID input to compare it's suit to the first Tile
		 * @return True if both TileIDs' are valid and same suit, false otherwise
		 */
		public static boolean isSameSuit(int in_tileID1, int in_tileID2)
		{
			if(isValidTile(in_tileID1) && isValidTile(in_tileID2))
			{
				if(in_tileID1/9 == in_tileID2/9) return true;
			}
			return false;
		}
		
		/**
		 * 
		 * @param in_tile_list An ArrayList<Integer> that represents tile_ids
		 * @return An ArrayList<Tile> of possible valid tiles (tile_val can be -1 if invalid)
		 */
		public static ArrayList<Tile> createTileAL(ArrayList<Integer> in_tile_list)
		{
			ArrayList<Tile> return_list = new ArrayList<Tile>();
			ArrayList<Integer> tempInList = new ArrayList<Integer>(in_tile_list);
			Collections.sort(tempInList);
			for(int tile_id: tempInList)
			{
				return_list.add(new Tile(tile_id));
			}
			return return_list;
		}
		
		
	}
	
	public static class MJ_Hand_tools
	{	
	    /**
		 * @param in_hand Any valid hand input using Tile struct
		 * @return 4 ArrayList<Tile> within Arraylist<Tile> that represents each suit of a mahjong hand.
		 * 			The return tiles in each ArrayList<Tile> contains Tile struct
		 */
		public static ArrayList<ArrayList<Tile>> suitDivide(ArrayList<Tile> input_hand)
		{
			ArrayList<ArrayList<Tile>> returnSuits = new ArrayList<ArrayList<Tile>>();
			for(int suit = 0; suit < 4; suit++)
			{
				returnSuits.add(new ArrayList<Tile>());
				for(Tile tile: input_hand)
				{
					if(tile.getTileID() < ((suit + 1) * 9) && tile.getTileID() >= (suit * 9))
					{
						returnSuits.get(suit).add(tile);
					}
				}
			}
			return returnSuits;
		}
		
		/**
		 * @param in_arraylist A input of an ArrayList<Tile> that needs to be sorted
		 * @param reverse_sort True if the ArrayList<Tile> should be sorted descending , false if wanted ascending
		 * @return A sorted ArrayList<Tile> of the original inputted ArrayList<Tile>
		 */
		public static ArrayList<Tile> sortTiles(ArrayList<Tile> in_arraylist, boolean reverse_sort)
		{
			ArrayList<Tile> tempList = new ArrayList<Tile>(in_arraylist);
			Collections.sort(tempList);
			if(reverse_sort)
			{
				for(int i = in_arraylist.size() - 1; i >= 0; i--)
				{
					tempList.add(tempList.get(i));
					tempList.remove(0);
				}
			}
			return tempList;
		}
		
		/**
		 * 
		 * @param in_arraylist ArrayList<Tile> with valid Tile objects
		 * 					However if the ArrayList<Tile> includes invalid tile_ids, -1 will be added
		 * @return The ArrayList<Integer> representing the play_value of each tile, the play_value
		 * 		   represents 1-9 in each suit, i.e tile_id = 24 = 7s, play_value = 7
		 */
		public static ArrayList<Integer> Tiles_to_PlayVal(ArrayList<Tile> in_arraylist)
		{
			ArrayList<Integer> return_array = new ArrayList<Integer>();
			if(in_arraylist.size() < 0) {return return_array;}
			for(Tile tile: in_arraylist)
			{
				if(tile.getTileID() < 0 || tile.getTileID() > 33) //Invalid tile_ids
				{
					return_array.add(-1);
				}
				return_array.add(Tile.tileID_to_PlayVal(tile));
			}
			return return_array;
		}
		
		/**
		 * *Warning*
		 * Input must be tile_id format, otherwise suit will default -> 0 == "mans"
		 * 
		 * *Warning*
		 * DO NOT PERFORM tile_id_to_PlayVal beforehand for argument in_suitarray, overindex can and will occur
		 * 
		 * @param in_arraylist Any valid ArrayList<Integer>, use case for a single suited ArrayList of tiles
		 *
		 * @return A matrix displaying minimum in in_arraylist as index 0 and maximum as index max - min, 
		 * 			quantity would be the value of each cell/index
		 */
		public static ArrayList<Integer> convert_to_matrix(ArrayList<Tile> in_suitarray)
		{
			if(in_suitarray.size() == 0) {return new ArrayList<Integer>();}
			/*
			 * Subtracts every element with minimum
			 * Makes sure its play_val because tile_id can exceed 9
			 */
			
			ArrayList<Integer> temp_suit = Tiles_to_PlayVal(sortTiles(in_suitarray, false));
			int min = temp_suit.get(0);
			int max = temp_suit.get(temp_suit.size() - 1);
			/*
			 * If there are no tiles between two tiles, they are filled with 0s
			 */
			ArrayList<Integer> matrix = new ArrayList<Integer>();
			//To prevent overflow of 0s, there should only be max - min + 1 cells
			for(int i = 0; i < max - min + 1; i++) matrix.add(0);
			for(int i = 0; i < temp_suit.size(); i++)
			{
				int tile = temp_suit.get(i) - min;
				matrix.set(tile, matrix.get(tile) + 1);
			}
			return matrix;
		}
		
		/**
		 * *note* This program will delete any leading zeros and-
		 * assumes the min is represented at the first occurrence of an index amount > 0
		 * 
		 * @param matrix The matrix that represents 1 suit of tiles
		 * @param suit The suit the matrix represents
		 * @param min The minimum play_val in the suit of ArrayList<Integer>
		 * @return The ArrayList<Tile> that represent all the Tiles objects
		 */
		public static ArrayList<Tile> convert_to_ArrayList(ArrayList<Integer> matrix, int suit, int min)
		{
			boolean add_true = false;
			int true_index = min - 1; //play_val - 1 to align with tile_id
			ArrayList<Tile> tile_array = new ArrayList<Tile>(); //return type
			for(int i = 0; i < matrix.size(); i++)
			{
				if(matrix.get(i) > 0)
				{
					//Adds the amount according to the quantity set in the matrix
					for(int amt = 0; amt < matrix.get(i); amt++)
					{
						tile_array.add(new Tile(true_index + (suit * 9)));
					}
					true_index++;
					add_true = true;
				}
				else if(add_true)
				{
					//If tile amount was spotted earlier, true_index has to change for filler 0s
					true_index++;
				}
			}
			return tile_array;
		}
		
		/**
		 * 
		 * @param listOfTiles Any form of collection that contains Tiles
		 * @return An ArrayList of integer values representing the Tile id
		 */
		public static ArrayList<Integer> castTilesToInt(Collection<Tile> listOfTiles)
		{
			ArrayList<Integer> return_data = new ArrayList<Integer>();
			for(Tile tile: listOfTiles)
			{
				return_data.add(tile.getTileID());
			}
			return return_data;
		}
		
		/**
		 * 
		 * @param in_array The ArrayList<Type> that wants to remove the first instance of input item
		 * @param rm_item The input item that wants to be remove (not completely, only once)
		 * @return ArrayList<Type> where the first instance of rm_item is removed, returns same array if item not present
		 */
		public static <T> ArrayList<T> rmFirstInstance(ArrayList<T> in_array, T rm_item)
		{
			for(int i = 0; i < in_array.size(); i++) if(in_array.get(i) == rm_item) {in_array.remove(i); break;};
			return in_array;
		}
		
		/**
		 * *warning* In order to not go out of bounds, if inputs are invalid, the original ArrayList is returned
		 * *warning* if end_index > start_index, then start_index will be set to end_index and vice versa
		 * 
		 * @param <T> Generic data type, preferred to be a valid standard variable data type.
		 * @param in_arraylist Any ArrayList of generic data type (i.e integer, double).
		 * @param start_index Where the sub-ArrayList should start inclusively.
		 * @param end_index Where the sub-ArrayList should end exclusively.
		 * @return A sub-ArrayList of anything inputed ArrayList data type
		 * starting at the lower index inclusive (assuming start_index) to upper index (assuming end_index) exclusive.
		 */
		public static <T> ArrayList<T> subArrayList(ArrayList<T> in_arraylist, int start_index, int end_index)
		{
			ArrayList<T> return_arraylist = new ArrayList<T>(); //The return data type
			
			//Avoids Index out of bounds
			if(start_index >= in_arraylist.size() || end_index > in_arraylist.size() || 
			   start_index < 0 || end_index < 0)
			{
				return in_arraylist;
			}
			int min = start_index;
			int max = end_index;
			if(start_index > end_index)
			{
				min = end_index;
				max = start_index;
			}
			for(int i = min; i < max; i++)
			{
				return_arraylist.add(in_arraylist.get(i));
			}
			return return_arraylist;
		}
	
		
		/*
		 * Used to make an ArrayList<Integer> with int[]
		 */
		public static ArrayList<Integer> createArrayList(int[] in_list)
		{
			ArrayList<Integer> return_ArrayList = new ArrayList<Integer>();
			for(int i = 0; i < in_list.length; i++) return_ArrayList.add(in_list[i]);
			return return_ArrayList;
		}
		
		/**
		 * 
		 * @param in_groupSN The group string notation of a given PlayerHand, used to provide information of suits of tiles
		 * @return ArrayList<Group> of completed and incomplete groups (typically eyes if hand is ready/complete).
		 * 			Groups are in tile_id for better view comparing to player hand ArrayList<Integer> of tile_id
		 */
		public static ArrayList<Group> groupSN_to_ArrayList(String in_groupSN)
		{
			if(in_groupSN.length() == 0)
			{
				return new ArrayList<Group>();
			}
			
			//suit_reference
			ArrayList<Group> return_groups = new ArrayList<Group>();
			/*
			 * tile_mode indicates how to add tiles
			 * 0 = complete groups
			 * 1 = incomplete groups 
			 * 2 = add min
			 * 3/anything else = invalid;
			 */
			int tile_mode = 2;
			
			/*
			 * add_group indicates when to add tiles
			 * false = don't add tiles
			 * true = add tiles
			 */
			boolean add_tiles = false;
			
			/*
			 * Determine if groups are concealed or not
			 */
			boolean concealed = false;
			
			/*
			 * Determine if group is technically declared
			 */
			boolean declared = true;
			int suit = 0;
			int min = 0;
			
			//Where all the converted groups are stored
			ArrayList<ArrayList<Group>> all_groups = new ArrayList<ArrayList<Group>>();
			all_groups.add(new ArrayList<Group>()); //index 0 for completed groups
			all_groups.add(new ArrayList<Group>()); //index 1 for incompleted groups
			
			//Where the tiles flow into one group to be added
			ArrayList<Integer> add_group = new ArrayList<Integer>();
			/*
			 * Searching in reverse order due to not being able to get information of current tiles
			 * suit + add min are guaranteed at end of groupSN
			 */
			for(int char_index = in_groupSN.length() - 1; char_index >= 0; char_index--)
			{
				char current_char = in_groupSN.charAt(char_index);
				
				//If character is a alphabet indicator, however it can be suit/remainder/open/closed
				if(Character.isAlphabetic(current_char))
				{
					//suit_reference = {'m', 'p', 's', 'z'}
					for(int i = 0; i < Tile.suit_reference.length; i++)
					{
						if(current_char == Tile.suit_reference[i])
						{
							suit = i; //refers to index respective to suit
							add_tiles = false;
							break;
						}
					}
					//Pre-sets the condition of group status
					switch(Character.toString(current_char))
					{
						case "c":
							concealed = true;
							declared = false;
							break;
						case "o":
							concealed = false;
							break;
						case "q":
							concealed = false;
							break;
						case "k":
							concealed = true;
							break;
					}
					continue;
				}
				//If character is a integer, however it can be complete group tiles/remainder tiles/add min
				else if(Character.isDigit(current_char))
				{
					int char_num = Character.getNumericValue(current_char);
					if(add_tiles)
					{
						/*
						 * x = matrix compressed index representation
						 * matrix compressed -> play_val = (x + min)
						 * play_val -> tile_id = (play_val + (suit * 9) - 1)
						 */
						add_group.add(char_num + min + (suit * 9) - 1);
					}
					else
					{
						min = char_num;
						add_tiles = true;
					}
				}
				else
				{
					//Indicates end of min number, but reversed search means start of min
					if(current_char == ']') {tile_mode = 2; add_tiles = false;} 
					
					//Indicates end of remainder, but reversed search means start remainder groups
					else if(current_char == '}') {tile_mode = 1; add_tiles = true;}
					
					//Indicates start of remainder, but reversed search means end remainder groups
					else if(current_char == '{') {tile_mode = 0;}
					
					//Indicates a start of a group, but reversed search means end group
					else if(current_char == '(') 
					{
						if(add_group.size() == 0)
						{
							continue;
						}
						Collections.sort(add_group);
						Group new_group = new Group(add_group, declared, concealed);
						if(tile_mode == 1) { new_group.setConcealed(true); } //If looking in remainder, incomplete groups are always concealed}
						if(tile_mode == 1 || tile_mode == 0){ all_groups.get(tile_mode).add(new_group); } //Adds group to corresponding index
						add_group = new ArrayList<Integer>();
					}
				}
			}
			
			/*
			 * Because the String was searched backwards, the suits are also backwards
			 * This sections will add the groups in order of suits
			 * 		This rule is after by how complete the group is applied
			 */
			for(int i = all_groups.get(0).size() - 1; i >= 0; i--) return_groups.add(all_groups.get(0).get(i));
			for(int i = all_groups.get(1).size() - 1; i >= 0; i--) if(all_groups.get(1).get(i).getGroupTiles().size() == 2) {return_groups.add(all_groups.get(1).get(i));}
			for(int i = all_groups.get(1).size() - 1; i >= 0; i--) if(all_groups.get(1).get(i).getGroupTiles().size() == 1) {return_groups.add(all_groups.get(1).get(i));}
			return return_groups;
		}
		
		/**
		 * 
		 * @param in_arraylist this arraylist is meant for all suit included, however it is implemented to be universal
		 * 					   since it searches for groups, then saves it suit, and adds accordingly.
		 * @return Returns the groupSN corresponding to the ArrayList<Group>
		 */
		public static String ArrayList_to_groupSN(ArrayList<Group> in_arraylist)
		{
			String return_str = "";
			ArrayList<Group> temp_groups = new ArrayList<Group>(in_arraylist); //This temp ArrayList allows us to remove elements to sort
			HashMap<Integer, ArrayList<Group>> suit_groups = new HashMap<Integer, ArrayList<Group>>();
			
			for(int i = 0; i <= 3; i++) suit_groups.put(i, new ArrayList<Group>());
			
			for(int size = 4; size >= 1; size--)
			{
				for(int i = 0; i < temp_groups.size(); i++) 
				{
					Group current_group = temp_groups.get(i);
					//If somehow a group with no tiles exist, continue
					if(current_group.getGroupTiles().size() <= 0)
					{
						continue;
					}
					//This prioritizes the group_size indicated by the for loop with temp_var size
					if(current_group.getGroupTiles().size() != size)
					{
						continue;
					}
					//Add group to corresponding id of HashMap
					suit_groups.get(current_group.getGroupTiles().get(0).getTileID()/9).add(current_group);
				}
			}

			/*
			 * 0 = concealed
			 * 1 = concealed quads
			 * 2 = opened quads
			 * 3 = opened
			 */
			for(int i = 0; i < 4; i++)
			{
				for(int key: suit_groups.keySet())
				{
					//Checks if there are any groups in this suit
					if(suit_groups.get(key).size() == 0){continue;}
					
					temp_groups = new ArrayList<Group>(); //To only contain either concealed or non-concealed groups
					for(Group given_group: suit_groups.get(key))
					{
						switch(i)
						{
							case 0:
								//Don't add declared concealed quads
								if(given_group.isConcealed() && !given_group.isDeclared())
								{
									temp_groups.add(given_group);
								}
								break;
							case 1:
								if(given_group.getGroupTiles().size() == 4 && given_group.isConcealed() && given_group.isDeclared())
								{
									temp_groups.add(given_group);
								}
								break;
							case 2:
								if(given_group.getGroupTiles().size() == 4 && !given_group.isConcealed() && given_group.isDeclared())
								{
									temp_groups.add(given_group);
								}
								break;
							case 3:
								if(given_group.getGroupTiles().size() == 3 && !given_group.isConcealed() && given_group.isDeclared())
								{
									temp_groups.add(given_group);
								}
								break;
						}
					}
					if(temp_groups.size() == 0) //If temp_groups.size() == 0, there is no concealed/non-concealed groups
					{
						continue;
					}
					ArrayList<Tile> suit_hand = new ArrayList<Tile>();
					int min;
					//Adds all the tiles into one ArrayList
					for(Group group: temp_groups) for(Tile tile: group.getGroupTiles()) suit_hand.add(tile);
					
					min = Tile.tileID_to_PlayVal(sortTiles(suit_hand, false).get(0));
					
					boolean start_remain = false; //Keeps track of where to add "r={", also helps close remainder segment
					String suit_string = ""; //The return for one suit, will be added to whole return_string
	
					for(Group given_group: temp_groups)
					{
						if((i == 0 && given_group.isConcealed() && !given_group.isDeclared()))
						{
							/*
							 *	The groupSN method will return the play_val-min and set it appropriate to groupSN format 
							 */
							if(given_group.getGroupTiles().size() == 3)
							{
								suit_string += given_group.toGroupSN(min);
							}
							else if(given_group.getGroupTiles().size() <= 2)
							{
								if(!start_remain)
								{
									start_remain = true;
									suit_string += "r={";
								}
								suit_string += given_group.toGroupSN(min);
							}
						}
						else
						{
							suit_string += given_group.toGroupSN(min);
						}
					}
					if(i > 0) //Adds complete groups if confirmed
					{
						return_str += suit_string + "[+" + Integer.toString(min) + "]" +Character.toString(Tile.suit_reference[key]); 
					}
					else if(suit_string.length() > 0 && !start_remain) //No remainder was detected
					{
						return_str += suit_string + "r={}[+" + Integer.toString(min) + "]" +Character.toString(Tile.suit_reference[key]); 
					}
					else if(suit_string.length() > 0)//Last case, there was a remainder
					{
						return_str += suit_string + "}[+" + Integer.toString(min) + "]" +Character.toString(Tile.suit_reference[key]); 
					}
				}
				switch(i)
				{
					case 0:
						return_str += "c";
						break;
					case 1:
						return_str += "k";
						break;
					case 2:
						return_str += "q";
						break;
					case 3:
						return_str += "o";
						break;
				}
			}
			return return_str;
		}
		
		/**
		 * 
		 * @param in_groupSN A String that represents a groupSN to be checked if it has all the required indicators
		 * @return Each integer represents the index of the corresponding indicators, however -1 represents the indicator not being present
		 */
		public static int[] checkGroupSN(String in_groupSN)
		{
			int[] has_indicators = {-1, -1, -1, -1};
			for(int i = 0; i < in_groupSN.length(); i++)
			{
				switch(in_groupSN.charAt(i))
				{
					case 'c':
						has_indicators[0] = i;
						break;
					case 'k':
						has_indicators[1] = i;
						break;
					case 'q':
						has_indicators[2] = i;
						break;
					case 'o':
						has_indicators[3] = i;
						break;
				}
			}
			return has_indicators;
		}
		
		/**
		 * @info There is no further analysis as it is impossible to differentiate concealed declared quads and open quads
		 * 		 Only by going off of where 'c' is, then adding quad indicators and adding 'o' at last index
		 * @param in_groupSN A reference to the original groupSN to be altered if it is missing indicators
		 * @return The altered concatenation of missing indicators to in_groupSN, since altering String reference does not alter original variable
		 */
		public static String addIndicators(String in_groupSN)
		{
			int[] hasIndicators = checkGroupSN(in_groupSN);
			final char[] indicatorsChars = {'k', 'q'};
			String temp_str = in_groupSN;
			if(hasIndicators[0] == -1)
			{
				temp_str += "ckqo";
				return temp_str;
			}
			int start = hasIndicators[0];
			int offset = 1;
			for(int i = 1; i < hasIndicators.length - 1; i++)
			{
				if(hasIndicators[i] == -1)
				{
					if(temp_str.length() > start + offset)
					{
						temp_str = temp_str.substring(0, start + offset) + indicatorsChars[i - 1] + temp_str.substring(start + offset, temp_str.length());
						offset++;
					}
				}
				else
				{
					start = hasIndicators[i];
				}
			}
			if(hasIndicators[hasIndicators.length - 1] == -1)
			{
				temp_str += "o";
			}
			return temp_str;
		}
		
		/**
		 * @info Givens a two Tile shape that would create a sequence
		 * @param suit An Integer value that corresponds to a valid suit that can create sequences
		 * @return An ArrayList<int[]> where each Array represents a possible sequential shape
		 */
		public static ArrayList<int[]> allPossibleSeqShape(int suit) throws IllegalArgumentException
		{
			if(suit < 0 || suit > 2) throw new IllegalArgumentException();
			
			HashSet<int[]> container_data = new HashSet<int[]>();
			boolean valid_shape;
			for(int i = 0 + suit; i < 9 + suit; i++)
			{
				valid_shape = true;
				int[] new_group = new int[3];
				for(int j = -1; j <= 1; j++)
				{
					if(!Tile.isValidTile(i + j) || (i + j)/9 != i/9)
					{
						valid_shape = false;
						break;
					}
					new_group[j + 1] = i + j;
				}
				if(valid_shape) 
				{
					container_data.add(new int[] {new_group[0], new_group[1]});
					container_data.add(new int[] {new_group[0], new_group[2]});
					container_data.add(new int[] {new_group[1], new_group[2]});
				}
				else if(new_group.length == 2)
				{
					container_data.add(new_group);
				}
			}
			ArrayList<int[]> return_data = new ArrayList<int[]>();
			for(int[] shape : container_data) return_data.add(shape);
			return return_data;
		}
		
		/**
		 * @info The input arraylistShapes elements will be altered if elements are found equal
		 * @param arraylistShapes The ArrayList<int[]> of shapes that is designated to have elements removed
		 * @param elementsRemoval The shapes that represents elements to be removed
		 */
		public static void remove_shapes(ArrayList<int[]> arraylistShapes, ArrayList<int[]> elementsRemoval)
		{
			for(int[] shape: elementsRemoval)
			{
				for(int i = 0; i < arraylistShapes.size(); i++)
				{
					if(Arrays.equals(arraylistShapes.get(i), shape))
					{
						arraylistShapes.remove(i);
						i--;
					}
				}
			}
		}
	}
	public static void main(String[] args)
	{
		String random_groupSN = "(012)r={}[+1]mr={(0)}[+1]p(012)r={}[+3]sc(000)r={}[+7]z";
		ArrayList<Group> test1 = MJ_Hand_tools.groupSN_to_ArrayList(random_groupSN);
		System.out.println(test1);
		System.out.println(MJ_Hand_tools.ArrayList_to_groupSN(test1));
		System.out.println("what" + MJ_Hand_tools.addIndicators(random_groupSN));
	}
}

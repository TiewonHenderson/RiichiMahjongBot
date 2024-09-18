package bot_package_v2;

import java.util.*;

import bot_package_v2.MJ_tools.*;
import bot_package_v2.Player.*;

/**
 * This class will store all algorithm responsible for searching Hidden / Non-visible Mahjong hand
 */
public class Prediction 
{
	/**
	 * This will represent the whole discard_History of a given Mahjong game
	 */
	public ArrayList<Tile> discardHistory_;
	
	/**
	 * This will represent the amount of non-visible tiles by inputted from the User
	 * This means the tile_amount_ would also consider the Player's own hand.
	 */
	public int[] tileAmount_ = new int[34];
	
	/**
	 * Default access in order for other classes to not have access
	 * This would represent the Tiles the Player have
	 */
	ArrayList<Tile> playerTiles_;
	
	/**
	 * Default constructor
	 */
	public Prediction()
	{
		Arrays.fill(this.tileAmount_, 4);
		this.discardHistory_ = new ArrayList<Tile>();
	}
	
	/**
	 * Predefined discard history
	 * 
	 * @note if ArrayList<Tile> discardHistory is considered with the mutator method, default constructor is used
	 * @param discardHistory An ArrayList of Tiles that represents the entire MJ games' discards (includes all Players)
	 */
	public Prediction(ArrayList<Tile> discardHistory)
	{
		if(!this.setDiscardHistory(discardHistory))
		{
			
		}
	}
	
	/**
	 * Complete constructor
	 * 
	 * @note if ArrayList<Tile> discardHistory is considered with the mutator method, default constructor is used
	 * @param discardHistory An ArrayList of Tiles that represents the entire MJ games' discards (includes all Players)
	 * @param userHand This represents the mahjongHand of the User, includes Tiles from melded Groups
	 */
	public Prediction(ArrayList<Tile> discardHistory, ArrayList<Tile> userHand)
	{
		this.setDiscardHistory(discardHistory);
		this.playerTiles_ = userHand;
		this.verify(true);
	}
	
	/**
	 * Clone constructor
	 * @param clone Another instance of Prediction
	 */
	public Prediction(Prediction clone)
	{
		this.discardHistory_ = new ArrayList<Tile>(clone.getDiscardHistory());
		this.setTileAmount(clone.getTileAmount());
		this.playerTiles_ = new ArrayList<Tile>(clone.playerTiles_);
	}
	
	/**
	 * Mutator method for discardHistory_
	 * 
	 * @note 
	 * This function checks 
	 * if any consecutive Tiles were thrown by the same Player (theoretically impossible)
	 * if any Tile amount is negative, indicating extra impossible Tiles were discarded
	 * @param newDiscardHistory A new set of Tiles that represents the DiscardHistory (input copy if should not be altered)
	 * @return True if the discard history is valid and has been set to the static variable discardHistory_
	 */
	public boolean setDiscardHistory(ArrayList<Tile> newDiscardHistory)
	{
		int[] tileAmt = new int[34];
		Arrays.fill(tileAmt, 4);
		for(int i = 0; i < newDiscardHistory.size() - 1; i++)
		{
			//Checks for consecutive Player discards
			if(newDiscardHistory.get(i).getAssignedWind() == newDiscardHistory.get(i + 1).getAssignedWind())
			{
				return false;
			}
			//Checks for non-possible extra Tiles
			if(i == newDiscardHistory.size() - 2)
			{
				tileAmt[newDiscardHistory.get(i).getTileID()]--;
				tileAmt[newDiscardHistory.get(i + 1).getTileID()]--;
				if(tileAmt[newDiscardHistory.get(i + 1).getTileID()] < 0) return false;
			}
			else
			{
				tileAmt[newDiscardHistory.get(i).getTileID()]--;
				if(tileAmt[newDiscardHistory.get(i).getTileID()] < 0) return false;
			}
		}
		this.discardHistory_ = newDiscardHistory;
		this.tileAmount_ = tileAmt;
		return true;
	}
	
	/**
	 * Mutator method of setting specific TileAmount
	 * @param newTileAmt An Array that represents the amount of Tiles not visible to the User
	 * @return True if the given Array is set to the tileAmount_ field, false if no changes
	 */
	private boolean setTileAmount(int[] newTileAmt)
	{
		if(newTileAmt.length == 34)
		{
			for(int tileAMT: newTileAmt)
			{
				if(tileAMT < 0 || tileAMT > 4) return false;
			}
			this.tileAmount_ = newTileAmt;
			return true;
		}
		return false;
	}
	
	/**
	 * 
	 * @param visibleTiles Represents Tiles that are visible and not part of the wall (decrement each given Tile)
	 * @return True if the tile amount has been updated, false if no changes
	 */
	public boolean updateTileAmount(ArrayList<Tile> visibleTiles)
	{
		int[] temp_amount = Arrays.copyOf(this.tileAmount_, this.tileAmount_.length);
		for(Tile tile: visibleTiles)
		{
			temp_amount[tile.getTileID()]--;
			if(temp_amount[tile.getTileID()] < 0)
			{
				return false;
			}
		}
		this.tileAmount_ = temp_amount;
		return true;
	}
	
	/**
	 * Accessor method for discardHistory_
	 * @return This instance public field discardHistory_
	 */
	public ArrayList<Tile> getDiscardHistory()
	{
		return this.discardHistory_;
	}
	
	/**
	 * Accessor method for tileAmount_
	 * @returnThis instance public field tileAmount_
	 */
	public int[] getTileAmount()
	{
		return this.tileAmount_;
	}
	
	/**
	 * 
	 * @param canReset This signifies the given function can reset if something is wrong with the assigned Prediction fields
	 * @return True if everything in this Prediction seems correct, false if there is something faulty
	 */
	public boolean verify(boolean canReset)
	{
		boolean return_value = true;
		
		//Checks invalid tile amount
		for(int tileAMT: this.tileAmount_)
		{
			if(tileAMT < 0 || tileAMT > 4)
			{
				return_value = false;
			}
		}
		//Checks for consecutive Player discards
		for(int i = 0; i < this.discardHistory_.size() - 1; i++)
		{
			if(this.discardHistory_.get(i).getAssignedWind() == this.discardHistory_.get(i + 1).getAssignedWind())
			{
				return_value = false;
			}
		}
		/*
		 * Checks invalid User mahjong hand
		 * 
		 * It is possible for playerTiles to have at max 18 tiles from 4 quads and 2 tiles
		 */
		if(this.playerTiles_.size() == 0 || this.playerTiles_.size() > 18)
		{
			return_value = false;
		}
		
		//Resets if allowed and is the instance's fields are faulty
		if(!return_value && canReset)
		{
			this.discardHistory_.clear();
			this.tileAmount_ = new int[34]; Arrays.fill(this.tileAmount_, 4);
			this.playerTiles_ = new ArrayList<Tile>();
		}
		return return_value;
	}
	
	/**
	 * 
	 * @param Tile_id The ID of a given valid Tile
	 * @return An Array that allows Groups to be built with the Tile_id given
	 */
	public int[] talkingTiles(int Tile_id)
	{
		if(Tile_id < 0 || Tile_id > 33) return null;
		switch(Tile_id / 9)
		{
			case 3:
				return new int[]{Tile_id};
			default:
				int[] return_data = new int[5];
				for(int i = -2; i <= 2; i++)
				{
					if(!Tile.isSameSuit(Tile_id, Tile_id + i)) 
					{
						return_data[i+2] = -1;
					}
					else 
					{
						return_data[i+2] = this.tileAmount_[Tile_id + i];
					}
				}
				return return_data;
		}
	}
	
	/**
	 * @info Score range = [0,1]
	 * @param Tile_id The Tile_id that represents a specific mahjong Tile
	 * @return A double score that represents how useful the given Tile is by it's ID, returns -1 for any errors
	 */
	public double scoreTile(int Tile_id)
	{
		try
		{
			final double[] tile_weights = {0.0,0.2,0.07,0.17,0.2};	//weight constants on how impactful the amount of Tiles is
					
			int[] talking_tile = this.talkingTiles(Tile_id);			//amount of talking tiles around given Tile_id
			double return_score = 0.0;								//final returning score
			for(int Tile_amt: talking_tile)
			{
				return_score += tile_weights[Tile_amt];
			}
			return return_score;
		}
		catch(Exception e) //Most likely NullPointerException from invalid Tile_id
		{
			return -1;
		}
	}
	
	/**
	 * 
	 * @param playerWind The enum Wind that is unique to each Player representing their seat wind value
	 * @return An ArrayList<Tile> that is specific to a certain Player in order of their discards
	 */
	public ArrayList<Tile> getPlayerDiscards(Wind playerWind)
	{
		ArrayList<Tile> return_data = new ArrayList<Tile>();
		for(Tile tile: this.discardHistory_)
		{
			if(tile.getAssignedWind() == playerWind)
			{
				return_data.add(tile);
			}
		}
		return return_data;
	}
	
	/**
	 * This class will attempt to map out the most likely tiles a Hidden Mahjong hand has
	 */
	public static class HiddenHandSearch
	{
		/**
		 * This is the map that shows how probable a given Hidden Mahjong hand has for each Tile
		 * Probability ranges from [0,1] inclusive
		 */
		public ArrayList<Double> probability_map_;
		
		/**
		 * This would represent the possible Yaku for this hand
		 */
		public double[] possible_Yaku_;
		
		/**
		 * This represents the current Hand for this searching algorithm
		 */
		protected Mahjong_hand current_hand_;
		
		/**
		 * Consecutive discard by
		 * 0: Suit 				//range = [0,3]
		 * 1: Orphan			//range = [0,1]
		 * 2: Discard_type		//range = [1,2] *note* if 0 appears, this Tile is not a discard 
		 * 
		 */
		private String[] ordered_discards = new String[3];
		
		/*
		 * This segment of the algorithm searches for completed Groups with given Tile
		 */
		private interface IntTo2DArrayFunction {ArrayList<int[]> apply(int value);}
		
		/*
		 * A lambda expression to create sequential shapes around the given Tile id
		 */
		public static IntTo2DArrayFunction generateSeqShapes_ = (int Tile_id) -> 
		{
			if(Tile_id < 0 || Tile_id > 33) return null;
			ArrayList<int[]> return_data = new ArrayList<int[]>();
			int index;
			
			for(int i = -1; i <= 1; i++)
			{
				index = 0;
				boolean is_valid = true;					//makes sure valid groups are added
				int[] new_group = new int[2];				//keeps track of possible groups
				for(int j = -1; j <= 1; j++)
				{
					if(!Tile.isSameSuit(Tile_id, Tile_id + j + i))
					{
						is_valid = false;
						break;
					}
					if(i + j == 0) continue;
					new_group[index] = Tile_id + i + j;
					index++;
				}
				if(is_valid) return_data.add(new_group);
			}
			return return_data;
		};
		
		/**
		 * Default constructor creates null Mahjong hand and empty probabilities
		 */
		public HiddenHandSearch()
		{
			this.current_hand_ = null;
			this.probability_map_ = new ArrayList<Double>();
		}
		
		/**
		 * Minimal constructor must have a valid Mahjong_hand
		 * @param given_hand The hidden hand for this searching algorithm
		 */
		public HiddenHandSearch(Mahjong_hand given_hand)
		{
			if(setMahjong_hand(given_hand)) 
			{
				this.initDiscardTileType();
			}
			else 
			{
				this.current_hand_ = null;
			}
			this.probability_map_ = new ArrayList<Double>();
			
		}
		
		/**
		 * Cloning constructor
		 * @param clone Another instance of HiddenHand_search
		 */
		public HiddenHandSearch(HiddenHandSearch clone)
		{
			this.current_hand_ = new Mahjong_hand(clone.getMahjong_hand());
			this.probability_map_ = new ArrayList<Double>(clone.getProbability_map());
			this.ordered_discards = Arrays.copyOf(clone.getOrdered_discards(), clone.getOrdered_discards().length);
		}
		
		/**
		 * Accessor method to obtain the probability Map (Probability of having certain Tiles)
		 * @return ArrayList<Double> that represents the probabilities this Mahjong_hand having specific Tiles
		 */
		public ArrayList<Double> getProbability_map()
		{
			return this.probability_map_;
		}
		
		/**
		 * Accessor method to obtain probability of certain Yakus
		 * @return A Double Array that represents the this Mahjong_hand possibly having certain Yakus
		 */
		public double[] getPossible_Yaku()
		{
			return this.possible_Yaku_;
		}
		
		/**
		 * Accessor method to obtain this instance Mahjong_hand
		 * @return The Mahjong_hand that is being searched by this algorithm
		 */
		protected Mahjong_hand getMahjong_hand()
		{
			return this.current_hand_;
		}
		
		/**
		 * Accessor method to obtain this intances discard by types and order
		 * @return an String Array that conveys consecutive information by discards
		 */
		protected String[] getOrdered_discards()
		{
			return this.ordered_discards;
		}
		
		/**
		 * Mutator method for setting probability of each Tile
		 * @param tile_id The tile_id's probability that wants to be changed
		 * @param new_probability The new probability for the assigned Tile
		 * @return True if the new_probability is valid and has changed the tile_id index, false if no changes
		 */
		protected boolean setProbability(int tile_id, double new_probability)
		{
			if(tile_id < 0 || tile_id > 33) return false;
			if(new_probability < -1 || new_probability > 1) return false;
			this.probability_map_.set(tile_id, new_probability);
			return true;
		}
		
		/**
		 * Mutator method for setting probability of each Yaku
		 * @param yaku_id The integer ID that represents a specific Yaku
		 * @param new_probability The new probability for the assigned Yaku
		 * @return True if the new_probability was set, false if no changes
		 */
		protected boolean setYaku(int yaku_id, double new_probability)
		{
			if(yaku_id < 0 || yaku_id > this.possible_Yaku_.length)return false;
			if(new_probability < -1 || new_probability > 1) return false;
			this.possible_Yaku_[yaku_id] = new_probability;
			return true;
		}
		
		/**
		 * Mutator method for setting the current hand for the algorithm
		 * @param new_hand A Mahjong_hand that wants to be searched (should not be copy of Mahjong_hand)
		 * @return True if the new_hand is set to this instance current_hand field, False if no changes
		 */
		protected boolean setMahjong_hand(Mahjong_hand new_hand)
		{
			if(new_hand.isValidHand())
			{
				this.current_hand_ = new_hand;
				return true;
			}
			return false;
		}
		
		/**
		 * 
		 * @param newOrderedDiscards An Array where each Integer value represents sequence of discard types
		 * @return This returns true is the input Array has been set to this instance's field, false if no changes
		 */
		protected boolean setOrdered_discards(String[] newOrderedDiscards)
		{
			if(newOrderedDiscards.length != 3)
			{
				return false;
			}
			for(int i = 0; i < newOrderedDiscards.length - 1; i++)
			{
				if(newOrderedDiscards[i].length() != newOrderedDiscards[i + 1].length())
				{
					return false;
				}
			}
			this.ordered_discards = Arrays.copyOf(newOrderedDiscards, newOrderedDiscards.length);
			return true;
		}
		
		/**
		 * 
		 */
		private void initDiscardTileType()
		{
			for(Tile tile: this.getMahjong_hand().drop_pile_)
			{
				this.ordered_discards[0] += tile.getTileInfo()[0];
				this.ordered_discards[1] += tile.getTileInfo()[2];
				this.ordered_discards[2] += tile.getTileInfo()[3];
			}
		}
		
		/**
		 * 
		 * @pre discard_Tiles must be in order by their discard and in same suit
		 * @param discard_Tiles An ArrayList<Tile> discarded by an individual
		 * @return An ArrayList of Arrays that represents impossible - unlikely shapes (nested Arrays are length 2)
		 */
		public HashSet<int[]> unlikelyTiles(ArrayList<Tile> discard_Tiles)
		{
			HashSet<int[]> return_data = new HashSet<int[]>();
			for(Tile input_tile: discard_Tiles) 
				for(int[] shapes: generateSeqShapes_.apply(input_tile.getTileID()))
					return_data.add(shapes);
			
			return return_data;
		}
		
		/**
		 * @info
		 * This algorithm will check common waits with the follow conditions of order
		 * 1) Extreme redundant/Backvein	(i.e 124p (1p discard), 689p (9p discard)), (i.3 457p (7p discard), 356(3p discard))
		 * 2) Overlapping 					(i.e 566p, 667p where input was 6p)
		 * 3) Double middle / Suji			(i.e 135p, 579p where input was 5p) 
		 * @param discard_TileID The Integer Tile_id that signified the Player being true ready (not sakigiri), cannot be honor tile
		 * @return An ArrayList of different shapes assigned by and Integer as following the rules above by index
		 */
		public ArrayList<ArrayList<int[]>> commonWaits(int discard_TileID) throws IllegalArgumentException
		{
			if(!Tile.isValidTile(discard_TileID) || discard_TileID > 26) throw new IllegalArgumentException();
			
			ArrayList<ArrayList<int[]>> return_data = new ArrayList<ArrayList<int[]>>();
			for(int i = 0; i < 3; i++) return_data.add(new ArrayList<int[]>());
			
			//Extreme redundancy / Backvein algorithm
			
			//Left terminal redundancy
			if(discard_TileID % 9 == 0) 
			{
				return_data.get(0).add(new int[] {discard_TileID + 1, discard_TileID + 3});
			}
			//Right terminal redundancy
			if(discard_TileID % 9 == 8)
			{
				return_data.get(0).add(new int[] {discard_TileID - 3, discard_TileID - 1});
			}
			
			//Checks left ryanmen overlap condition
			if(Tile.isSameSuit(discard_TileID - 1, discard_TileID))
			{
				return_data.get(1).add(new int[] {discard_TileID - 1, discard_TileID});
				
				//check backvein of Tile
				if(Tile.isSameSuit(discard_TileID - 3, discard_TileID))
				{
					return_data.get(0).add(new int[] {discard_TileID - 3, discard_TileID - 2});
					
					//checks left double middle wait/ suji
					if(Tile.isSameSuit(discard_TileID - 4, discard_TileID))
					{
						return_data.get(0).add(new int[] {discard_TileID - 4, discard_TileID - 2});
					}
				}
			}
			
			//Checks right ryanmen overlap condition
			if(Tile.isSameSuit(discard_TileID, discard_TileID + 1)) 
			{
				return_data.get(1).add(new int[] {discard_TileID, discard_TileID + 1});
				//check frontvein of Tile
				if(Tile.isSameSuit(discard_TileID, discard_TileID + 3))
				{
					return_data.get(0).add(new int[] {discard_TileID + 2, discard_TileID + 3});
					
					//checks right double middle wait/ suji
					if(Tile.isSameSuit(discard_TileID, discard_TileID + 4))
					{
						return_data.get(0).add(new int[] {discard_TileID + 2, discard_TileID + 4});
					}
				}
			}
			
			return return_data;
		}
		
		/**
		 * 
		 * @param readied_TileID The Tile that have been assigned as the Tile when the opponent was ready
		 * @param unlikely_shapes A set of two TileIDs that represents either impossible or less likely sequential shapes
		 * @return An Arraylist<int[]> that represents all possible sequential waits
		 */
		public ArrayList<int[]> allSequenceWaits(int readied_TileID, HashSet<int[]> unlikely_shapes)
		{
			try
			{
				ArrayList<int[]> return_data = new ArrayList<int[]>();
				for(ArrayList<int[]> categorized_shapes : commonWaits(readied_TileID))
					for(int[] shape: categorized_shapes) return_data.add(shape);
				//Removes unlikely shapes from the common waits
				MJ_Hand_tools.remove_shapes(return_data, new ArrayList<int[]>(unlikely_shapes));
				return return_data;
			}
			catch(Exception e)
			{
				return new ArrayList<int[]>();
			}
		}
		
		/**
		 * Using the indexes from the Scoring table, each index represents the probability of the declared Groups
		 * reaching to that score or that score being impossible
		 * 
		 * @info
		 * Index represents these scores below
		 * 
		 * 0: Shousangen / Daisangen
		 * 1: 4 little Winds / 4 Big Winds
		 * 2: Toitoi
		 * 3: 4 Quads
		 * 4: Half Flush
		 * 5: Full Flush
		 * 6: Half Outside Hand
		 * 7: Full Outside Hand
		 * 8: All Terms/Honors
		 * 9: All Honors
		 * 10: All Terminal
		 * 
		 * If a score becomes impossible to attain, it is set to -1
		 * 
		 * @return A double Array which represents probability of scores within each index
		 */
		public double[] yakuFromMeldedGroups()
		{
			double[] return_data = new double[11];
			if(this.current_hand_.getDeclaredGroups().size() == 0)
			{
				return return_data;
			}
			
			ArrayList<Group> declaredGroups = this.current_hand_.getDeclaredGroups();
			
			int totalGroups = 0;
			int[] groupTypes = {0,0,0};
			int[] suitTypes = {9,9,9,9};
			/*
			 * three length string where each represent tile type
			 * 0 = simple, 1 = terminal, 2 = wind, 3 = dragon
			 */
			String[] hasTermHonor = {null, null, null, null}; 
			
			/*
			 * Adds all group type, suit, and group information to corresponding list
			 */
			for(int i = 0; i < declaredGroups.size(); i++)
			{
				int[] groupInfo = declaredGroups.get(i).getGroupInfo();
				if(groupInfo[0] >= 1 && groupInfo[0] <= 3)
					groupTypes[groupInfo[0] - 1]++;
				else continue;
				if(groupInfo[1] >= 0 && groupInfo[1] <= 3)
					suitTypes[i] = groupInfo[1];
				else continue;
				String tileTypeSTR = "";
				for(Tile groupTile: declaredGroups.get(i).getGroupTiles())
				{
					int[] tileInfo = groupTile.getTileInfo();
					switch(tileInfo[0])
					{
						case 3:
							if(tileInfo[1] <= 3)
								tileTypeSTR += 2;
							else
								tileTypeSTR += 3;
							break;
						default:
							switch(tileInfo[1])
							{
								case 1:
								case 9:
									tileTypeSTR += 1;
									break;
								default:
									tileTypeSTR += 0;
							}
					}
				}
				totalGroups++;
				hasTermHonor[i] = tileTypeSTR;
			}
			
			/*
			 * Mainly focuses on groupType
			 * Scores affected:
			 * 0,1,2,3,8,9,10
			 */
			switch(groupTypes[0])//checks sequences
			{
				case 1:
					//removes all triplets/quads, all term/honors,
					return_data[2] = -1;
					return_data[3] = -1;
					for(int i = 8; i <= 10; i++)return_data[i] = -1;
				case 2:
					//Two groups are sequences, cannot have 4 unique winds
					return_data[1] = -1;
				case 3:
					//Three groups are sequences, cannot have 3 unique dragons
					return_data[0] = -1;
				case 4:
					break;
			}
			
			/*
			 * Mainly focuses on groupSuit
			 * score affected:
			 * 4,5,9,10
			 */
			for(int i = 0; i < suitTypes.length; i++)//checks suit
			{
				if(suitTypes[i] < 3)
				{
					//removes all honors
					return_data[9] = -1;
					for(int j = i; j < suitTypes.length; j++)
					{
						if(suitTypes[i] != suitTypes[j] && suitTypes[j] < 3)
						{
							//removes half/full flush if different suit
							return_data[4] = -1;
							return_data[5] = -1;
							break;
						}
					}
					break;
				}
				else if(suitTypes[i] == 3)
				{
					//Cannot be full flush/all terminal with honor group
					return_data[5] = -1;
					return_data[10] = -1;
				}
			}
			
			/*
			 * Mainly focuses on groupTiles
			 * Scores affected:
			 * 0,1,6,7,8,9,10
			 */
			int[] groupTrait = {0,0,0,0,0};	//This allows clearer winds/dragon availability 
			for(String tileTypeSTR: hasTermHonor)
			{
				if(tileTypeSTR != null)
				{
					/*
					 * Group type:
					 * 0 = all simple (doesn't matter trip or seq)
					 * 1 = outside seq
					 * 2 = terminal pung
					 * 3 = wind pung
					 * 4 = dragon pung
					 */
					int groupType = 0;
					if(Character.getNumericValue(tileTypeSTR.charAt(0)) >= 2) //wind or dragon can only be pung
					{
						groupType = Character.getNumericValue(tileTypeSTR.charAt(0)) + 1;
					}
					else
					{
						//Since only language is 0,1, add to see amt of term/simples
						int sum = 0;
						for(int i = 0; i < tileTypeSTR.length(); i++)
							sum += Character.getNumericValue(tileTypeSTR.charAt(i));
						switch(sum)
						{
							case 1:
								groupType = 1;
								break;
							case 3:
								groupType = 2;
								break;
						}
					}
					groupTrait[groupType]++;
					switch(groupType)//Determine group type with score availability 
					{
						case 0:
							for(int i = 6; i <= 7; i++) return_data[i] = -1;
						case 1:
							for(int i = 8; i <= 10; i++) return_data[i] = -1;
							break;
						case 2:
							return_data[9] = -1;
							break;
						case 3:
						case 4:
							return_data[7] = -1;
							return_data[10] = -1;
							break;
					}
				}
			}
			//With the amount of groups used, shows which score is impossible
			switch(totalGroups)
			{
				case 4:	//no new groups avail
					if(groupTrait[3] < 3) return_data[1] = -1;
					if(groupTrait[4] < 2) return_data[0] = -1;
					break;
				case 3:	//1 groups extra
					if(groupTrait[3] < 2) return_data[1] = -1;
					if(groupTrait[4] == 0) return_data[0] = -1;
					break;
				case 2:
					if(groupTrait[3] == 0) return_data[1] = -1;
					break;
			}
		}
	}
	
	/**
	 * This class will be responsible for accessing how much Groups would respond with certain percentage of progress
	 * Progress can correspond to hand progress as a whole (hprogress) or specific score progress (sprogress)
	 */
	static class ProgressAccessor
	{
		private static final String scalars = "15rr127r358r335r135r123r25892475247947994578245035792689235022nn13791249";
		private static final double[][] declaredGroupProgress = new double[18][4];
		protected static boolean setGroupProgress = false;
		
		/**
		 * @info
		 * This will convert the flatten String to progress values by double numbers ranging [0.0,1.0]
		 * The result will always be consistent and set to a final static variable declaredGroupProgress
		 */
		private static void setGroupProgress()
		{
			int scoreIndex = -1;
			for(int i = 0; i < scalars.length(); i++)
			{
				if(i % 4 == 0) {scoreIndex++;}
				if(scalars.charAt(i) == 'r')
					declaredGroupProgress[scoreIndex][i%4] = 1.0;
				else
					declaredGroupProgress[scoreIndex][i%4] = Character.getNumericValue(scalars.charAt(i));
			}
			setGroupProgress = true;
		}
		
		/**
		 * @return A 2D double array where 1st index represents the score, the 2nd index represents the scalar
		 */
		public static double[][] getGroupProgress()
		{
			if(!setGroupProgress)
			{
				setGroupProgress();
			}
			return declaredGroupProgress;
		}
		
		/**
		 * @info
		 * This return a specific index resulting from function getGroupProgress()
		 * @param scoreIndex The first dimension index which represents which score
		 * @param qualifiedGroups The second dimension index 
		 * 						  which represents the amount of qualified Groups corresponding to the scalar
		 * @return A double scalar corresponding to the declaredGroupProgress array via parameter values
		 */
		public static double specificProgress(int scoreIndex, int qualifiedGroups)
		{
			return getGroupProgress()[scoreIndex][qualifiedGroups];
		}
	}
	
	public static void main(String[] args)
	{

	}
}

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
			if(newDiscardHistory.get(i).getWhoDiscarded() == newDiscardHistory.get(i + 1).getWhoDiscarded())
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
			if(this.discardHistory_.get(i).getWhoDiscarded() == this.discardHistory_.get(i + 1).getWhoDiscarded())
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
			if(tile.getWhoDiscarded() == playerWind)
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
		 * 
		 * @return
		 */
		public double[] yakuFromMeldedGroups()
		{
			double[] return_data = new double[];
			
		}
	}
	
	/**
	 * This class will be responsible when there are only 0-6 tiles discarded by an opponent
	 */
	public static class EarlySearch extends HiddenHandSearch
	{	
		/**
		 * This set of Tiles will represents the discards after a certain opponent is in tenpai
		 */
		public HashSet<Tile> genbutsu_;
		
		/**
		 * Minimum constructor requires a valid Mahjong_hand
		 * @param given_hand A hand that can be searched through
		 */
		public EarlySearch(Mahjong_hand given_hand)
		{
			this.setMahjong_hand(given_hand);
		}
		
		/**
		 * Cloning constructor
		 * @param clone Another instance of Early_search object
		 */
		public EarlySearch(EarlySearch clone)
		{
			this.probability_map_ = new ArrayList<Double>(clone.getProbability_map());
			this.setMahjong_hand(clone.getMahjong_hand());
		}
		
		
	}
	
	/**
	 * This class will be responsible when there are only 7-12 tiles discarded by an opponent
	 */ 
	public static class MiddleSearch extends HiddenHandSearch
	{
		/**
		 * Minimum constructor requires a valid Mahjong_hand
		 * @param given_hand
		 */
		public MiddleSearch(Mahjong_hand given_hand)
		{
			
		}
		
		/**
		 * Cloning constructor
		 * @param clone Another instance of Early_search object
		 */
		public MiddleSearch(MiddleSearch clone)
		{
			
		}
	}
	
	/**
	 * This class will be responsible when there are only 13+ tiles discarded by an opponent
	 */
	public static class LateSearch extends HiddenHandSearch
	{
		
	}
	
	
	public static boolean testPredictionDescendants()
	{
		int segment = 0;
		try
		{
			HiddenHandSearch testPrediction = new HiddenHandSearch();
			
			
			
			return true;
		}
		catch(Exception e)
		{
			System.out.println("Prediction class failed, code could not pass segment: " + segment);
			return false;
		}
	}
	
	public static void main(String[] args)
	{
		
	}
}

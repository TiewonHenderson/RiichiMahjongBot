package bot_package;

import java.util.*;
import java.util.function.*;

/**
 * Needed for bot prediction:
 * Compress_input-> Will be responsible for:
 * 		- Discard reading (tile_ID + used_tiles)
 * 			> Tile market					(updates -> MJ_game possible tiles -> tile market)
 * 			> Player Progress score 		(less impactful)
 * 			> Player prediction score		(What score the Player could be aiming for)
 * 			> Possible overflow tiles		(*optional*)
 * 		- Decision (typically calls)
 * 			> Tedashi/Tsumogiri				(Depends on the Player's prediction scores)
 * 			> Calls/Declares				(Progresses the Player further, update Progress Score)
 * 			> Ron							(Literally ends the game who cares)
 *		- MJ_game
 *			> Tiles remain					(Indicates need for speed for current Player)
 * 
 * Prediction format:
 * String:
 * 
 * Each seatWind prediction starts "wind_ID" + ":"
 * The Progress score of the Player in that wind_ID
 * The Score Prediction, what the Player could be going for
 * *optional* a Danger zone matrix to indicate most likely tile the Player has (*warning* length == 34)
 * 
 * 0:		1:		2:		3:
 */

public class Prediction
{
	
	/**
	 * The inputed drop_pile of the opponent
	 */
	public ArrayList<Double> drop_pile_;
	
	/**
	 * A instance field to store all the probability from the activation function
	 */
	public double[] all_probability_ = {-4,-4,-4};
	
	/**
	 * Used for searching index of drop pile where Player is confirmed ready (in riichi)
	 */
	protected int riichi_ = -1;
	
	/**
	 * A private int AL that is responsible for storing unique tiles that were discarded
	 */
	private ArrayList<Integer> unique_tiles_ = new ArrayList<Integer>();
	
	/**
	 * A ordered orphan to simple tile type in order of game discard
	 * element ArrayList<Integer>: index 0 == amt orphans, index 1 == amt simples
	 */
	private ArrayList<ArrayList<Integer>> orphan_ratio_list_ = new ArrayList<ArrayList<Integer>>();
	
	/**
	 * A AL of ArrayList of 4 integers representing each suit and it's amount of Unique tiles
	 * being discarded in each suit incrementing the index value
	 */
	private ArrayList<ArrayList<Integer>> suit_amt_ = new ArrayList<ArrayList<Integer>>();
	
	/**
	 * Not required, but can help in certain functions (kokushi + 7 pairs probability)
	 * this field can only be set using the mutator function (which checks valid size + tile_id amounts)
	 */
	private ArrayList<Integer> uni_tile_market_;
	
	/**
	 * Not required, but can help in certain functions (normal + flush probability, also eliminates need to search kokushi/7 pairs)
	 * this field can only be set using the mutator function (which checks valid size + tile_id amounts)
	 */
	private ArrayList<Group> player_call_groups_;
	
	private Tile_map player_info_map_;
	
	/**
	 * Parameterized Constructor where tile_market_ and call_groups_ are not given
	 * @param drop_pile
	 */
	public Prediction(ArrayList<Double> drop_pile)
	{
		
		//sets drop_pile of 1 player to this prediction instance
		this.drop_pile_ = new ArrayList<Double>(drop_pile);
		
		//Saves a list of unique tile_ids, also checks if riichi is present
		for(int i = 0; i < drop_pile.size(); i++) 
		{
			this.unique_tiles_.add(drop_pile.get(i).intValue());
			if(Double.toString(drop_pile.get(i)).charAt(Double.toString(drop_pile.get(i)).length() - 1) == '1')
			{
				this.riichi_ = i;
			}
		}		
		this.unique_tiles_ = new ArrayList<Integer>(new LinkedHashSet<Integer>(this.unique_tiles_));
		ArrayList<Integer> temp_unique_tile = new ArrayList<Integer>(this.unique_tiles_);
		
		//Will show amt of tile in each suit per turn
		ArrayList<Integer> start = new ArrayList<Integer>();
		for(int i = 0; i < 4; i++) start.add(0);
		this.suit_amt_.add(start);
		
		int orphan_count = 0;
		int simple_count = 0;
		for(int i = 0; i < this.drop_pile_.size(); i++)
		{
			switch(tile_type(this.drop_pile_.get(i)))
			{
				//3,2 == honor, terminal
				case 3:
				case 2:
					orphan_count++;
					break;
				//1,0 == edge simple, middle tile
				case 1:
				case 0:
					simple_count++;
					break;
			}
			if(temp_unique_tile.size() > 0 && this.drop_pile_.get(i).intValue() == temp_unique_tile.get(0))
			{
				ArrayList<Integer> turn_suit_amt = new ArrayList<Integer>(this.suit_amt_.get(i));
				turn_suit_amt.set(temp_unique_tile.get(0).intValue()/9, turn_suit_amt.get(temp_unique_tile.get(0).intValue()/9) + 1);
				this.suit_amt_.add(turn_suit_amt);
				temp_unique_tile.remove(0);
			}
			else
			{
				this.suit_amt_.add(this.suit_amt_.get(this.suit_amt_.size() - 1));
			}
			
			ArrayList<Integer> add_tiletype_count = new ArrayList<Integer>();
			add_tiletype_count.add(orphan_count);
			add_tiletype_count.add(simple_count);
			orphan_ratio_list_.add(add_tiletype_count);
		}
	}
	
	public boolean set_user_tile_market(ArrayList<Integer> tile_market)
	{
		if(tile_market.size() != 34)
		{
			return false;
		}
		for(int tile_id: tile_market){if(tile_id < 0 || tile_id > 4) {return false;}}
		this.uni_tile_market_ = tile_market;
		return true;
	}
	
	public ArrayList<Integer> get_uni_tile_market()
	{
		return this.uni_tile_market_;
	}
	
	public boolean add_tile(double new_tile)
	{
		drop_pile_.add(new_tile);
		int tile_id = (int)new_tile;
		
		//invalid tile id
		if(tile_id < 0 || tile_id > 33)
		{
			return false;
		}
		
		//checks if is unique tile
		boolean unique = true;
		for(int tile: this.unique_tiles_)
		{
			if(tile_id == tile)
			{
				unique = false;
				break;
			}
		}
		if(unique)
		{
			this.unique_tiles_.add(tile_id);
			ArrayList<Integer> next_suit_drop;
			try
			{
				next_suit_drop = new ArrayList<Integer>(this.suit_amt_.get(this.suit_amt_.size() - 1));
			}
			catch(IndexOutOfBoundsException e)
			{
				//This would indicate there were no previous this.suit_amt (no drop pile case)
				next_suit_drop = new ArrayList<Integer>();
				for(int i = 0; i < 4; i++) next_suit_drop.add(0);
				next_suit_drop.set(tile_id/9, next_suit_drop.get(tile_id/9) + 1);
			}
			next_suit_drop.set(tile_id/9, next_suit_drop.get(tile_id/9) + 1);
			this.suit_amt_.add(next_suit_drop);
		}
		return true;
	}
	/**
	 * @info
	 * Start layer: honors / terminal should be more abundant
	 * (tile_type 3 + tile_type 2) > (tile_type 1 + tile_type 0)
	 * index 0 - index 6 weight equation = -(4/3)ln(x-0.55) + e (caps at y = 5)
	 * 
	 * weights:
	 * 1 	->	3.7829588
	 * 2	->	2.223
	 * 3	->	1.523
	 * 4	->	1.067
	 * 5	->	0.728
	 * 6	->	0.457
	 * 
	 * MAX values(weighted ratio score) possible:
	 * 3.7829588 + 4.4457275 + 4.5704934 + 4.2684647 + 3.6387152 + 2.7447661 == 23.4511257
	 * MAX average value(weighted ratio score) where all start discard honors = 3.90852095
	 * 
	 * total_additive_ratio_score = ln(1+e^(3(average weight score)-3.5)) Softplus function
	 * where (average weight score) >= 2.84 reaches max score
	 * total_additive_ratio_score range = [0,5] inclusive
	 * 
	 * 2nd algorithm == average weighted variance score (see Numerical_tools.weighted_variance_mean() for information)
	 * total_average_weighted_variance_score range = [-3.75,3.75] inclusive
	 * 
	 * final_score range = [-3.75,8.75], midpoint == 2.5
	 * @return A score reflecting the probability of a given drop pile protraying a player going a normal hand
	 */
	public double normal_prob()
	{
		double final_prob = 0.0;
		double average_weight = 0.0;
		for(int i = 1; i < 7; i++) 
		{
			if(i > this.orphan_ratio_list_.size())
			{
				break;
			}
			
			double orphan_ratio;
			if(this.orphan_ratio_list_.get(i-1).get(1) == 0){orphan_ratio = this.orphan_ratio_list_.get(i-1).get(0).doubleValue();}
			else{orphan_ratio = this.orphan_ratio_list_.get(i-1).get(0).doubleValue() / this.orphan_ratio_list_.get(i-1).get(1).doubleValue();}
			
			average_weight += (orphan_ratio*((-4.0/3.0)*Math.log(i - 0.55) + Math.E));
		}
		average_weight /= 6;
		
		final_prob = Math.log(1.0 + Math.exp(3.0*average_weight - 3.5));
		if(final_prob > 5) {final_prob = 5;}
		
		/*
		 * Middle / End layer: dependant on speed of hand
		 * SEE Numerical_tools.weighted_variance_mean() documentation for more information
		 */
		double variance_score = Numerical_tools.weighted_variance_mean(this.suit_amt_);
		return final_prob + variance_score;
	}
	
	/**
	 * Algorithm 1: Suit distribution (range == [0.0,32.62]
	 * Excluding honors: assume the lowest unique thrown tile SUIT is the flush Player is going for
	 * 
	 * Loop through drop pile: 
	 * any occurance of same suit == decrease slope of consecutive weight function
	 * -Layer 1:
	 * 		> honor		  	 n == 19/20n scalar
	 * 		> terminal 	  	 n == 6/7n scalar
	 * 		> edge simple 	 n == 17/20n scalar
	 * 		> simple 	  	 n == 3/5n scalar
	 * -Layer 2:
	 * 	 	> honor			 n == nothing
	 * 		> terminal		 n == 9/10n scalar
	 * 		> edge simple 	 n == 6/7n scalar
	 * 		> simple 	  	 n == 2/3n scalar
	 * -Layer 3: No decrease scalar
	 * 
	 * Each consecutive non flush is added based off this Sigmoid linear unit formula
	 * (n)(x)/(2.75 + 70 e^(-x))
	 * where n == changing scalar; x == consecutive non flush count
	 * 
	 * Algorithm 2: Least unique suit discard
	 * Each turn unique suit discard is increased, if one suit isn't catching up, it
	 * could indicate that Player is going a flush hand for that suit
	 * Each turn is weighted based of the Softplus function 
	 * ln(1 + e^(x-6)/3) + 3 where x == turn
	 * 
	 * Final ratio score is inverted to a final algorithm 2 score by using the function
	 * 
	 * Algorithm factor: (Algorithm 2 score) * 3.5 > (Algorithm 1 score) * 1.5
	 * Better sign with more unique discards of other suits to show the Player -> flush hand
	 * Layer 1 flaw is having an early discard of flush suit to throw the score off
	 * 
	 * @return 
	 * final_score range = [-3.75,8.75], midpoint == 2.5
	 */
	public double flush_prob()
	{
		//Loop drop pile algorithm
		final double[][] layer_scalar = {{0.75,0.85,0.8571428571,0.95},{0.6666666667,0.8571428571,0.9,1.0}};
		ArrayList<Integer> decide_suit = new ArrayList<Integer>();
		int assume_flush_suit = -1;
		
		ArrayList<Integer> last_suit_amt;
		try{last_suit_amt = new ArrayList<Integer>(this.suit_amt_.get(this.suit_amt_.size() - 1));}
		catch(IndexOutOfBoundsException  e) {return -99.99;}
		
		double tile_size_weight = 1.0; //allow 2 layered hands to be weighted up
		
		if(this.drop_pile_.size() >= 8 && this.drop_pile_.size() < 18)
		{
			switch(this.drop_pile_.size())
			{
				case 8:
				case 9:
					tile_size_weight += 12.0/this.drop_pile_.size();
					break;
				default:
					tile_size_weight += 18.0/this.drop_pile_.size();
					break;
			}
		}
		
		/*
		 * gets the assumed suit for flush hand (min suit)
		 * if 2 suits are equal, earlier discards are out
		 */
		double min = last_suit_amt.get(0);
		decide_suit.add(0);
		for(int i = 1; i < last_suit_amt.size(); i++)
		{
			if(i == 3) {break;}
			if(min == last_suit_amt.get(i))
			{
				decide_suit.add(i);
			}
			if(min > last_suit_amt.get(i))
			{
				min = last_suit_amt.get(i);
				assume_flush_suit = i;
				decide_suit.remove(0);
				decide_suit.add(i);
			}
		}
		if(decide_suit.size() > 1)
		{
			for(int i = 0; i < this.drop_pile_.size(); i++)
			{
				if(decide_suit.size() == 1)
				{
					assume_flush_suit = decide_suit.get(0);
					break;
				}
				for(int j = 0; j < decide_suit.size(); j++)
				{
					//If same suit present earlier, delete that suit from decision
					if(this.drop_pile_.get(i).intValue()/9 == decide_suit.get(j))
					{
						decide_suit.remove(j);
					}
				}
			}
		}
		if(assume_flush_suit == -1) {assume_flush_suit = 0;}
		
//		System.out.println(last_suit_amt + "; Assumed suit: " + assume_flush_suit);
		
		int consec_nonflush_drop = 0;		//counter for consecutive non_flush drops (honors doesn't reset)
		int nonflush_drop = 0;				//counter for non_flush drops (doesn't count honor)
		int layer = 0;						//the layer of the drop_pile(default = interval of 6)
		double alg1_score = 0.0;			//the total score for this algorithm 1
		double n = 1.15;					//the adaptive scalar responding to the discard order
		int tild_id;
		for(int i = 0; i < this.drop_pile_.size(); i++)
		{
			tild_id = this.drop_pile_.get(i).intValue();
			int suit = tild_id/9;
			boolean is_flushsuit = false;
			layer = i/6;
			//Last layer is not accounted, it's extremely speed dependent
			if(layer == 2) {break;}
			//reset consecutive nonflush count if applicable
			if(suit == assume_flush_suit) {is_flushsuit = true;}
			if(is_flushsuit || (suit == 3 && layer == 0))
			{
				if(is_flushsuit){consec_nonflush_drop = 0;}
				n *= layer_scalar[layer][tile_type(this.drop_pile_.get(i))];
			}
			else{nonflush_drop++; consec_nonflush_drop++;}
			double current_grade = n * (1.5 * nonflush_drop/(2.75 + 70 * (Math.exp((-1 * consec_nonflush_drop))))) * tile_size_weight;
			if(current_grade > 5) {current_grade = 5;}
			alg1_score += current_grade;
			
//			try{System.out.print(Double.toString(alg1_score).substring(0, 5) + "; ");}
//			catch(StringIndexOutOfBoundsException e) {System.out.print(Double.toString(alg1_score) + "; ");}
		}
		alg1_score = (10/38.36) * alg1_score - 5;
		if(alg1_score > 5) {alg1_score = 5;} //max score == [-5,5]
		if(alg1_score < -5) {alg1_score = -5;}//max score == [-5,5]
//		System.out.println("\n================Final Score: " + alg1_score + "==============");

		//Ratio of nonsuit tiles to suited tiles, higher score == less likely flush
		ArrayList<Double> suit_ratio = new ArrayList<Double>();
		
		int after_decoy_index = 0;		//saves second index of drop tile with same assume flush suit
		double suit_weight = 0;			//the weight of the suit drop depending on layers, includes some honors
		int nonsuit_amt = 0;			//the amount of tiles that don't belong in the assume suit
		layer = 0;
		for(int i = 0; i < this.drop_pile_.size(); i++)
		{
			layer = i/4;
			if(layer > 2) {layer = 2;}
			
			int tile_id = this.drop_pile_.get(i).intValue();
			
			if(tile_id / 9 == assume_flush_suit) 
			{
				if(after_decoy_index == 0) {after_decoy_index = i;}
				suit_weight += 1 * Math.abs(layer - 2);
			}
			else if(tile_id / 9 == 3) {suit_weight += 0.2 * Math.abs(layer - 2);}
			else {nonsuit_amt++;}
			
			if(i == 0 || suit_weight == 0) {suit_ratio.add((double)nonsuit_amt);}
			else {suit_ratio.add((double)nonsuit_amt/(i * suit_weight));}
		}
		if(after_decoy_index == 0) {after_decoy_index = this.drop_pile_.size();} //if the suit has never appeared, set equal to drop_pile size
		
		double ratio_sum = 0.0;
		for(double ratio: suit_ratio) ratio_sum += ratio;
		ratio_sum = ratio_sum/suit_ratio.size();
		
//		System.out.println(suit_ratio);
//		System.out.println("Ratio average: " + ratio_sum);
//		System.out.println("Score bonus: " + (double)after_decoy_index/this.drop_pile.size());
		
		double alg2_score = ratio_sum + (double)after_decoy_index/this.drop_pile_.size();
		if(alg2_score > 3.5) {alg2_score = 3.5;} //max range = [0,3.5]
		
		double final_score = alg1_score + alg2_score;
		if(final_score < -3.5) {return -3.5;}
		return final_score;
	}

	/**
	 * @info
	 * Algorithm 1: weighted_values by drop_pile index (higher score == less likely to be kokushi)
	 * 
	 * weight system of equations (let x = turn)
	 * {x >= 6} 	-> -(1/10)x + 2.63013 		
	 * {9 > x >= 0}	-> -(1.67) * (e^(x/20) - 1) + 5
	 * 
	 * How to separate kokushi with flush:
	 * its more likely flush will still end up dropping terminals from other suits
	 * while kokushi depending on speed would just keep all terminals
	 * 
	 * tile score:
	 * 		> middle tile *= 0.05
	 * 		> edge simple *= 0.65
	 * 		> terminal	  *= 2.55
	 * 		> honor		  *= 3
	 * 
	 * Algorithm 2: Full drop_pile rm outlier
	 * In proportion to amount of orphans in each layer (6 drop tiles)
	 * if layer1+2/layer0 <= 0.5, remove those orphans from temp_drop_tiles in layer0 only
	 * 
	 * Scan consecutive orphans (this hopes to catch flush discard terminals in layer 1 or layer 2)
	 * max consecutive orphans scaled by e^(x/7)-1.73981: if result < 0, set alg2_score = 0
	 * 
	 * Algorithm 3 (temporary): First index honor
	 * First honor index -> further end == more weight
	 * let x = turn, y = First honor index
	 * {6  > x >=  0} 	weight == x/7 * First honor index
	 * {12 > x >=  6} 	weight == 2^((x+1)/6) * First honor index
	 * {x >= 12}		weight == 5 (max)
	 * 
	 * to avoid higher weights of flushes, divide by variance of suits (exclude honors)
	 * 
	 * @hidden if user_tile_market_ has a value, any orphan tile that are all visible (either by calls or dropped) 
	 * will return a score of -3.75 as kokushi is now impossible according to the user_tile_market_
	 * 
	 * final score = (Algorithm 1)= [0,2] (Algorithm 2) = [-0.75, 5] (Algorithm 3) = [-3,1.75]
	 * @return return score range [-3.75, 8.75], where higher means more probable for kokushi
	 */
	public double kokushi_prob()
	{
		if(this.uni_tile_market_ != null)
		{
			for(int i = 0; i < this.uni_tile_market_.size(); i++)
			{
				if(i % 9 == 0 || i % 9 == 8 || i > 25)
				{
					if(this.uni_tile_market_.get(i) == 0)
					{
						return -3.75;
					}
				}
			}
		}
		//Algorithm 1 (higher non-adjusted score == less likely to be kokushi)
		final double[] scalars = {0.05,0.55,2.55,3};
		ArrayList<Integer> orphan_index = new ArrayList<Integer>();
		ArrayList<Double> weighted_values = new ArrayList<Double>();
		int first_honor_index = -1; //For algorithm 3
		double weight = 0.0;
		double alg1_score = 0.0;
		for(int i = 0; i < this.drop_pile_.size(); i++) 
		{
			if(i >= 6)
			{
				weight = (-1/10.0) * i + 2.63013;
				if(weight < 0) {weight = 0;}
			}
			else
			{
				weight = (-1.67)*(Math.exp(i/20.0) - 1) + 5;
			}
			switch(tile_type(this.drop_pile_.get(i)))
			{
				case 2:
				case 3:
					orphan_index.add(i);
					break;
			}
			if(first_honor_index == -1 && tile_type(this.drop_pile_.get(i)) == 3) {first_honor_index = i;} //For algorithm 3
			alg1_score += weight * scalars[tile_type(this.drop_pile_.get(i))];
			weighted_values.add(weight * scalars[tile_type(this.drop_pile_.get(i))]);
		}
		alg1_score = (this.drop_pile_.size()/6 * 6)/alg1_score;
		
		//Algorithm 2
		double alg2_score = 0.0;
		int consec_simples = 0;
		switch(orphan_index.size())
		{
			case 0:
				consec_simples = this.drop_pile_.size();
				break;
			case 1:
				consec_simples = orphan_index.get(0);
				if(orphan_index.get(0) - this.drop_pile_.size() > consec_simples)
				{
					consec_simples = orphan_index.get(0) - this.drop_pile_.size();
				}
				break;
			case 2:
				consec_simples = orphan_index.get(0);
				int[] dif_list = {
						orphan_index.get(1) - orphan_index.get(0),
						this.drop_pile_.size() - orphan_index.get(1)
				};
				for(int dif : dif_list)
				{
					if(dif > consec_simples)
					{
						consec_simples = dif;
					}
				}
				break;
			default:
				/*
				 * removes outliers (early orphan discards that don't fit)
				 * note: only applies to full discard piles that can convey more information
				 */
				int max_consec = 0;
				if(this.drop_pile_.size() >= 18)
				{
					ArrayList<Double> temp_drop_pile = new ArrayList<Double>(this.drop_pile_);
					ArrayList<Integer> orphan_layer_list = new ArrayList<Integer>();
					ArrayList<Integer> layer_0_index = new ArrayList<Integer>();
					int non_start_layer_sum = 0;
					for(int i = 0; i < 4; i++) orphan_layer_list.add(0);
					for(int i = 0; i < orphan_index.size(); i++) 
					{
						orphan_layer_list.set(orphan_layer_list.get(orphan_index.get(i)/6), orphan_layer_list.get(orphan_index.get(i)/6) + 1);
						if(orphan_index.get(i)/6 > 0) {non_start_layer_sum++;}
						else {layer_0_index.add(orphan_index.get(i));}
					}
					if(orphan_layer_list.get(0) > 0 && (double)orphan_layer_list.get(0)/non_start_layer_sum <= 0.5)
					{
						for(int i = 0; i < layer_0_index.size(); i++) 
						{temp_drop_pile.remove(layer_0_index.get(i) - i);}
					}
					for(int i = 0; i < temp_drop_pile.size(); i++)
					{
						switch(tile_type(temp_drop_pile.get(i)))
						{
							case 0:
							case 1:
								consec_simples++;
								break;
							case 2:
							case 3:
								if(consec_simples > max_consec)
								{
									max_consec = consec_simples;
								}
								consec_simples = 0;
								break;
						}
					}
				}
				else
				{
					for(int i = 0; i < this.drop_pile_.size(); i++)
					{
						switch(tile_type(this.drop_pile_.get(i)))
						{
							case 0:
							case 1:
								consec_simples++;
								break;
							case 2:
							case 3:
								if(consec_simples > max_consec)
								{
									max_consec = consec_simples;
								}
								consec_simples = 0;
								break;
						}
					}
				}
				consec_simples = max_consec;
				break;
		}
		
		/*
		 * Algorithm 3
		 * First honor index -> further end == more weight
		 * let x = turn, y = First honor index
		 * {6  > x >=  0} 	x/7 * First honor index
		 * {12 > x >=  6} 	2^((x+1)/6) * First honor index
		 * {x >= 12}		max 5
		 */
		int sum_non_honor = 0;
		int first_honor_suit_list = -1;
		for(int i = 0; i < this.suit_amt_.size(); i++)
		{
			if(this.suit_amt_.get(i).get(3) > 0)
			{
				first_honor_suit_list = i;
				for(int j = 0; j < 3; j++) sum_non_honor += this.suit_amt_.get(i).get(j);
				break;
			}
		}
		double suit_variance = 0.0;
		//No honors detected, depends on size of drop_pile
		if(first_honor_index == -1) 
		{
			first_honor_index = this.drop_pile_.size();
			first_honor_suit_list = this.suit_amt_.size();
			suit_variance = Numerical_tools.calc_variance(Numerical_tools.upcast_AL_int(new ArrayList<Integer>(this.suit_amt_.get(this.suit_amt_.size() - 1).subList(0, 3))));
			for(int j = 0; j < 3; j++) sum_non_honor += this.suit_amt_.get(this.suit_amt_.size() - 1).get(j);
		}
		else {suit_variance = Numerical_tools.calc_variance(Numerical_tools.upcast_AL_int(new ArrayList<Integer>(this.suit_amt_.get(first_honor_suit_list).subList(0, 3))));;}
		
		//First tile discard should have suit_variance factor = 0
		if(first_honor_index == 0) {suit_variance = 0;}
		else{suit_variance = 2.0/(Math.exp(2 * (suit_variance - 3.5)/5.0) + 1);}
	
		double weight_1st_index = 0.0;
		
		if(first_honor_index < 6){weight_1st_index = first_honor_index/7.0;}
		else if(12 > first_honor_index && first_honor_index >= 6){weight_1st_index = Math.pow(2, (first_honor_index + 1)/6.0);}
		else if(first_honor_index >= 12){weight_1st_index = 5;}
		//Range = [0.0,6.604367777117163]
		double alg3_score = ((double)sum_non_honor/this.unique_tiles_.size() * weight_1st_index) + suit_variance;
		
		//Optimize each score of importance
		
		if(alg1_score < 0.1) {alg1_score = 0;}			//too insignificant to consider score
		alg1_score *= this.drop_pile_.size()/6;
		if(alg1_score > 2) {alg1_score = 2;} 			//range = [0,2.0]
		
		/*
		 * Consecutive weight formula: let x = consec_simples
		 * e^(x/7)-1.73981 if < 0, set alg2_score = 0
		 */
		alg2_score = Math.exp((consec_simples/7.0)) - 1.73981;
		if(alg2_score > 5) {alg2_score = 5;} 			//range = [-0.75, 5]
		
		alg3_score = (0.7196 * alg3_score - 3);
		if(alg3_score > 1.75) {alg3_score = 1.75;} 		//range = [-3,1.75]
		
		return alg1_score + alg2_score + alg3_score;
	}
	
	/**
	 * 
	 * @info
	 * The problem with 7 pairs is there are no clear pattern of discard pile with the tiles
	 * of the drop pile. The only EXTREMELY CLEAR pattern is if you are given the history of all drop pile
	 * 
	 * 7 pairs behavior (for max efficiency)
	 * 
	 * the whole point of this algorithm is to see if the opponent is discarding tiles that are scarce
	 * tile_amt == user_tile_market_ index
	 * tedashi tiles
	 * tile_amt						add_score
	 * 3(op themselves)				-1.5
	 * 2							+0.2
	 * 1							+0.8
	 * 0							+1.5
	 * 
	 * tsumogiri					add_score
	 * 3(op themselves)				-2.0
	 * 2							-0.1
	 * 1							+1.0
	 * 0							+1.0
	 * 
	 * @return return score range [-3.75, 8.75], to determine the score of the ArrayList drop pile that represents 
	 * a typical 7 pairs hand. Limited version of 7 pairs
	 */
	public double seven_pairs_prob()
	{
		double final_score = 0.0;
		ArrayList<Integer> discard_type = tile_to_discardType(this.drop_pile_);
		if(this.uni_tile_market_ == null)
		{
			return 0.0;
		}
		else
		{
			double[][] additive_scores = {{1.0,1.0,-0.1,-2.0}, {1.5,0.8,0.2,-1.5}};
			for(int i = 0; i < this.drop_pile_.size(); i++)
			{
				final_score += additive_scores[discard_type.get(i)][this.uni_tile_market_.get(this.drop_pile_.get(i).intValue())];
			}
		}
		return final_score;
	}
	
	/**
	 * @info 
	 * A previous discard counter algorithm:
	 * major weight == amount of same tile discarded before opponent discard
	 * middle weight == tedashi
	 * middle weight == layer
	 * light weight == gamemode == 0, orphans -> 5s -> simples, gamemode == 1, same weights
	 * 
	 * major weight equation: 
	 * z = total previous amt discard
	 * x = layer
	 * 
	 * z = 0			x/28.5 - 0.06
	 * z = 1			max(7-z,0)/((x+1)*5)
	 * z = 2			abs(min(z,8)-8)/(2*((e^x)+1.75)) + .5/(z+1)
	 * z = 3 			13-z/((x^2) + 5.5)
	 * 
	 * min un-weight score ~= -0.5094736842105263
	 * max un-weight score ~= 41.58560833080732
	 * 
	 * Last scalar is dependent on size of discard_index (total drop_pile), scalar formula = abs(min(x,4)-5)*0.2725
	 * 
	 * @param discard_index 	Numerical_tools.get_discard_index(MJ_round's assigned_drop_wind_id_, wind_id)
	 * @param all_drop_tiles	MJ_round's all_drop_tiles_
	 * @return 
	 */
	public static double seven_pairs_prob(ArrayList<Integer> discard_index, ArrayList<Double> all_drop_tiles)
	{
		/*
		 * Each index = amount of times before discard
		 */
		ArrayList<ArrayList<int[]>> amt_b4_discard_counter = new ArrayList<ArrayList<int[]>>();
		for(int i = 0; i < 4; i++) amt_b4_discard_counter.add(new ArrayList<int[]>());
		
		ArrayList<Integer> sorted_index = Group.sortArray(discard_index);
		for(int j = 0; j < sorted_index.size(); j++)
		{
			if(sorted_index.get(j) >= all_drop_tiles.size())
			{
				break;
			}
			int counter = 0;
			int turn_distance = 0;
			int latest_index = 0;
			int tile_id = all_drop_tiles.get(sorted_index.get(j)).intValue();
			for(int i = 0; i < sorted_index.get(j); i++)
			{	
				if(all_drop_tiles.get(i).intValue() == tile_id) 
				{
					counter++; turn_distance = 0;
				}
				//Responsible for the amount of turns the player could've discarded a tile
				else if(latest_index < sorted_index.size() && i == sorted_index.get(latest_index)) 
				{
					latest_index++; turn_distance++;
				}
			}
			if(turn_distance > 10) {turn_distance = 10;} if(turn_distance < 0 || counter == 0) {turn_distance = 0;}
			int[] new_discard_info = {sorted_index.get(j)/(6 * 4), turn_distance, -1};
			if(tile_id > 26)
			{
				new_discard_info[2] = 2;
			}
			else if(tile_id % 9 == 0 || tile_id % 9 == 8)
			{
				new_discard_info[2] = 1;
			}
			else
			{
				new_discard_info[2] = 0;
			}
			//0 = layer, 1 = turn_distance, 2 = tile type
			System.out.println(counter + " ; " + turn_distance + " ; " + tile_id + " ; " + new_discard_info[0]);
			amt_b4_discard_counter.get(counter).add(new_discard_info);
		}
		
		double weighted_total = 0.0;
		for(int i = 0; i < amt_b4_discard_counter.size(); i++)
		{
			for(int[] discard_info : amt_b4_discard_counter.get(i))
			{
				switch(i)
				{
					case 0:
						weighted_total += Math.min(discard_info[0]/28.5 - 0.06, 0);
						break;
					case 1:
						weighted_total += Math.max(7 - discard_info[1], 0)/
										  ((discard_info[0] + 1) * 5.0);
						break;
					case 2:
						weighted_total += Math.abs(Math.min(discard_info[1], 8) - 8.0)/
										  (2.0 * (Math.exp(discard_info[0]) + 1.75)) + 
										  (0.5)/(discard_info[1] + 1);
						break;
					case 3:
						weighted_total += (13.0 - discard_info[1])/(Math.pow(discard_info[0], 2) + 5.5);
						break;
				}
				System.out.println(weighted_total);
			}
		}
		weighted_total = weighted_total * Math.max((Math.abs(Math.min(discard_index.size()/6, 4)-5) * 0.2725),1);
		System.out.println("final " + weighted_total);
		if(weighted_total > 8.75) {return 8.75;}
		return weighted_total;
	}
	
	public double[] return_all_prob()
	{
		for(int i = 0; i < this.all_probability_.length; i++)
		{
			switch(i)
			{
				case 0:
					this.all_probability_[0] = score_2_percentage(this.normal_prob());
					break;
				case 1:
					this.all_probability_[1] = score_2_percentage(this.flush_prob());
					break;
				case 2:
					this.all_probability_[2] = score_2_percentage(this.kokushi_prob());
					break;
			}
		}
		return this.all_probability_;
	}
	/**
	 * @info
	 * How progression typically occurs
	 * start == multiple tedashi to get rid of useless tiles
	 * middle == mixed tedashi / tsumogiri
	 * end == majority tsumogiri, tedashi would typically only to change for better waits
	 * 
	 * the above mentioned are the 3 layers of progression
	 * All follows a exponential growth with consecutive discard pattern
	 * if you tedashi consecutively at start/middle, the progress rate grows exponentially
	 * if you tsumogiri consecutively at middle/end, the chance of ready hand grows exponentially
	 * 
	 * exceptions, tsumogiri consecutively at start -> EXTREMELY fast hand or EXTREMELY slow hand
	 * exceptions, tedashi consecutively at end 	-> folding or EXTREMELY slow hand
	 * 
	 * Average starting shanten = 3.278 (3,4)
	 * 
	 * exponential weight rate for tedashi: 	x = tedashi consective, 	z = layer;	f(x) = progress rate 	(caps at 10)
	 * 		   expoentialfactor	 	 linearlayerfactor
	 * 	f(x) = e^(0.25(x-4))-(1/e) * (0.8z + 0.8333)
	 * exponential weight rate for tsumogiri:	x = tsumogiri consective, 	z = layer;	g(x) = ready chance		(caps at 10)
	 * 		   sigmoidfactor		   				  linearlayerfactor
	 * 	g(x) = (10/(1+e^(-0.5*(x-5))) - 10/1+e^2.5) * (1.5z + 1.067)
	 * 
	 *
	 * 
	 * @param discardtype_list	A ArrayList<Integer> of only integers representing tedashi or tsumogiri
	 * @return ArrayList<ArrayList<Double>> where index 0 or each ArrayList<Double> represents tedashi weight
	 * 											  index 1 represents tsumogiri weight
	 */
	public ArrayList<ArrayList<Double>> tedashi_weight(ArrayList<Integer> discardtype_list)
	{
		ArrayList<ArrayList<Double>> return_weights = new ArrayList<ArrayList<Double>>();
		
		//total discard type count
		int tedashi_count = 0;
		int tsumogiri_count = 0;
		
		//consecutive discard type count
		int tedashi_consec = 0;
		int tsumogiri_consec = 0;
		
		//layer starts at index 0
		int layer = 0;
		
		ArrayList<Double> temp_weight_list = new ArrayList<Double>();
		for(int i = 0; i < 2; i++)temp_weight_list.add(0.0);
		for(int i = 0; i < discardtype_list.size(); i++)
		{
			for(int j = 0; j < 2; j++)temp_weight_list.set(j, 0.0);
			//changes layer
			layer = i/6;
			switch(discardtype_list.get(i))
			{
				case 0:
					tsumogiri_count++;
					tsumogiri_consec++;
					tedashi_consec = 0;
					break;
				case 1:
					tedashi_count++;
					tedashi_consec++;
					tsumogiri_consec = 0;
					break;
				default:
					return null;
			}
			
			/*
			 * x = tedashi consective, 	z = layer;	f(x) = progress rate (caps at 10)
			 * f(x) = e^(0.25(x-4))-(1/e) * (0.8z + 0.8333)
			 */
			double tedashi_weight = (Math.exp(0.25 * (tedashi_consec - 4.0)) - 
									(1.0/Math.E) + 
									(double)tsumogiri_count/18) * 
									(0.8 * layer + (0.8 + (1.0/3.0)));
			if(tedashi_weight > 10) {tedashi_weight = 10.0;}
			
			/*
			 * x = tsumogiri consective, 	z = layer;	g(x) = ready chance
			 * g(x) = (10/(1+e^(-0.5*(x-5))) - 10/1+e^2.5) * (1.5z + 1.067)
			 */
			double tsumogiri_weight = (10.0/(1.0 + Math.exp(-0.5 * (tsumogiri_consec - 5.0))) - 
									  (10.0/(1.0 + Math.exp(2.5))) + 
									  (double)tsumogiri_count/18) * 
									  (1.5*layer + 1.067);
			if(tsumogiri_weight > 10) {tsumogiri_weight = 10.0;}
			
			temp_weight_list.set(0, tedashi_weight);
			temp_weight_list.set(1, tsumogiri_weight);
			return_weights.add(new ArrayList<Double>(temp_weight_list));
		}
		
		double discard_type_ratio_val = (double)tedashi_count;
		if(tsumogiri_count > 0) {discard_type_ratio_val /= tsumogiri_count;}
		ArrayList<Double> discard_type_ratio = new ArrayList<Double>();
		discard_type_ratio.add(-1.0);
		discard_type_ratio.add(discard_type_ratio_val);
		return_weights.add(discard_type_ratio);
		
		return return_weights;
	}
	
	/**
	 * Prints out the drop tiles using the same algorithm from AL_droppile_toSTR
	 */
	public void print()
	{
		final String[] honor = {"E","S","W","N","Wh","G","R"}; ArrayList<String> print_str = new ArrayList<String>(); 
		int signal = 0; boolean riichi = false; boolean tedashi = false;
		for(double tile: this.drop_pile_){
			String this_tile = "";
			tedashi = Numerical_tools.is_tedashi(tile);
			if((tile * 100 - ((int)(tile * 100))) > 0.1){signal += 1;}
			if(Double.toString(tile).charAt(Double.toString(tile).length() - 1) == '1'){signal += 2;}
			if(((int)tile) / 9 >= 3){this_tile += honor[(int)tile % 9];}
			else
			{
				this_tile += ((int)tile % 9) + 1;if(signal % 2 == 1) {this_tile += "r";}
				switch(((int)tile) / 9){
					case 0:
						this_tile += "m";
						break;
					case 1:
						this_tile += "p";
						break;
					case 2:
						this_tile += "s";
						break;}
			}
			if(tedashi && !riichi){this_tile += "d";} else if(!riichi){this_tile += "t";}
			if(signal >= 2) {this_tile += "-"; riichi = true;}
			tedashi = false;signal = 0;print_str.add(this_tile);}
		System.out.println(print_str);
	}
	
	public static double score_2_percentage(double score)
	{
		if(((score + 3.75)/(12.5)) * 100.0 < 0.0) {return 0.0;}
		if(((score + 3.75)/(12.5)) * 100.0 > 100.0) {return 100.0;}
		return ((score + 3.75)/(12.5)) * 100.0;
	}
	
	/**
	 * 
	 * @param drop_pile The drop pile that wants to be checked for tedashi and tsumogiri
	 * @return A binary ArrayList where 0 = tsumogiri, 1 = tedashi
	 */
	public static ArrayList<Integer> tile_to_discardType(ArrayList<Double> drop_pile)
	{
		ArrayList<Integer> return_list = new ArrayList<Integer>();
		for(double tile: drop_pile) return_list.add((int)Math.round(tile - (int)tile));
		return return_list;
	}
	
	/**
	 * @info
	 * 0 = middle tile 	[3,7] inclusive
	 * 1 = edge simple 	(only 2,8)
	 * 2 = terminal		(only 1,9)
	 * 3 = honor
	 * @param tile the tile that wants to be check what type (input by tile_id)
	 * @return a integer that corresponds to the type
	 */
	public static int tile_type(double tile)
	{
		int tild_id = (int)tile;
		if(tild_id < 0 || tild_id > 33) {return -1;}
		if(tild_id > 26) {return 3;}
		if((tild_id%9 == 0) || (tild_id%9 == 8)) {return 2;}
		if((tild_id%9 == 1) || (tild_id%9 == 7)) {return 1;}
		return 0;
	}
	
	public static ArrayList<Double> read_dropSTR(String drop_pile)
	{
		ArrayList<Double> ret_droppile = new ArrayList<Double>();
		String temp_droppile = new String(drop_pile.toLowerCase());
		String get_tile = "";
		boolean riichi = false;
		while(temp_droppile.length() > 0)
		{
			double temp_tile = -1;
			//Add riichi mode
			if(temp_droppile.charAt(0) == '-' || riichi)
			{
				//if riichi not set true before, set true and go to next char
				if(!riichi)
				{
					riichi = true; 
					ret_droppile.set(ret_droppile.size() - 1, ret_droppile.get(ret_droppile.size() - 1) + 0.0001);
					continue;
				}
				
				final char[] indicators = {'e', 's', 'w', 'n', 'h', 'g', 'r', 'm', 'p', 's'};
				int num_val = 0;
				boolean red_five_case = false;
				ArrayList<Double> temp_ret_droppile = new ArrayList<Double>();
				//Iterates the remaining tile_str
				for(int i = 0; i < temp_droppile.length(); i++)
				{
					if(Character.isAlphabetic(temp_droppile.charAt(i)))
					{
						for(int j = 0; j < indicators.length; j++)
						{
							if(temp_droppile.charAt(i) == indicators[j])
							{
//								System.out.println(temp_droppile.charAt(i) + Integer.toString(num_val) + j); test print
								if(j == 6 && num_val != 0)		//red 5 case
								{
									red_five_case = true;
									break;
								}
								if(j <= 6 && num_val == 0) 		//char is an honor symbol (exception with red five)
								{
									//west would added more if white dragon is present, so if h is detected, delete one w
									temp_tile = tile_interpreter(Character.toString(indicators[j]) + "t");
									if(j == 4){temp_ret_droppile.remove(temp_ret_droppile.size() - 1);}
								}
								if((j == 1 || j > 6) && num_val != 0)	//char is suit symbol
								{
									String translate_tile = Integer.toString(num_val);					//tile play val
									if(red_five_case){translate_tile += "r"; red_five_case = false;}	//red five indicator if true
									translate_tile += Character.toString(indicators[j]) + "t";			//suit, then riichi = tsumogiri
									temp_tile = tile_interpreter(translate_tile);
									num_val = 0;
								}
								if(temp_tile > 0){temp_ret_droppile.add(temp_tile);}
								break;
							}
						}
					}
					else if(Character.isDigit(temp_droppile.charAt(i)))
					{
						num_val = Character.getNumericValue(temp_droppile.charAt(i));
					}
				}
				for(double tile: temp_ret_droppile) ret_droppile.add(tile);
				break;
			}
			
			//Add tile without riichi mode
			get_tile += temp_droppile.charAt(0);
			if(temp_droppile.length() == 1)
			{
				//How tiles are checked b4 added
				temp_tile = tile_interpreter(get_tile);
				if(temp_tile > 0){ret_droppile.add(temp_tile);}
				break;
			}
			switch(temp_droppile.charAt(0))
			{
				case 't':
				case 'd':
					
					//How tiles are checked b4 added
					temp_tile = tile_interpreter(get_tile);
					if(temp_tile >= 0){ret_droppile.add(temp_tile);}
					
					//resets string checking vars
					get_tile = "";
					break;
			}
			
			//resets string checking vars
			temp_droppile = temp_droppile.substring(1);
		}
		return ret_droppile;
	}
	
	public static String AL_droppile_toSTR(ArrayList<Double> drop_pile)
	{
		final String[] honor = {"e","s","w","n","wh","g","r"};
		String return_str = "";
		/*
		 * signal = 1 == redfive
		 * 		  = 2 == riichi
		 * 		  = 3 == both
		 */
		int signal = 0;
		boolean riichi = false;
		boolean tedashi = false;
		for(double tile: drop_pile)
		{
			int tile_id = (int)(tile);
			Numerical_tools.is_tedashi(tile);
			if(Numerical_tools.is_red_5(tile)){signal += 1;}
			if(Numerical_tools.is_riichi(tile_id)){signal += 2;}
			if(((int)tile) / 9 >= 3)
			{
				return_str += honor[(int)tile % 9];
			}
			else
			{
				return_str += ((int)tile % 9) + 1;
				if(signal % 2 == 1) {return_str += "r";}
				switch(((int)tile) / 9)
				{
					case 0:
						return_str += "m";
						break;
					case 1:
						return_str += "p";
						break;
					case 2:
						return_str += "s";
						break;
				}
			}
			if(tedashi && !riichi){return_str += "d";}
			else if(!riichi){return_str += "t";}
			
			if(signal >= 2) {return_str += "-"; riichi = true;}
			tedashi = false;
			signal = 0;
		}
		return return_str;
	}
	
	public static double tile_interpreter(String tile_str)
	{
		if(tile_str.isBlank()) {return 0.0;}
		
		String temp_str = new String(tile_str.toLowerCase());
		
		double red_five_detail = 0;
		
		//White dragon needs to replace wh with
		if(temp_str.length() >= 2)
		{
			if(temp_str.substring(0, 2).compareTo("wh") == 0)
			{
				temp_str = temp_str.substring(1);
			}
		}
		/*
		 * 0 = play_val
		 * 1 = suit
		 * 2 = discard_type
		 */
		int[] tile_data = {-1,-1,-1};
		final char[][] indicator_list = {{'e', 's', 'w', 'n', 'h', 'g', 'r'},{'m', 'p', 's'}, {'t', 'd'}};
		
		for(int i = 0; i < temp_str.length(); i++)
		{
			if(Character.isDigit(temp_str.charAt(i)))
			{
				tile_data[0] = Character.getNumericValue(temp_str.charAt(i)) - 1;
			}
			else if(Character.isAlphabetic(temp_str.charAt(i)))
			{
				int j = 0;
				if(tile_data[0] != -1)	// indicating play_val already set
				{
					if(temp_str.charAt(i) == 'r')
					{
						red_five_detail = 0.005;
					}
					j++;
				}
				while(j < indicator_list.length)
				{
					for(int k = 0; k < indicator_list[j].length; k++)
					{
						if(Character.toLowerCase(temp_str.charAt(i)) == indicator_list[j][k])
						{
							tile_data[j] = k;
							if(j == 0)
							{
								tile_data[1] = 3;
							}
							j = indicator_list.length;
							break;
						}
					}
					j++;
				}
			}
		}
		//returns -1 if not valid tile return
		for(int i = 0; i < 2; i++)
		{
			if(tile_data[i] == -1)
			{
				return -1.0;
			}
		}
//		System.out.println((tile_data[0]) + (tile_data[1] * 9) + (tile_data[2] * 0.5) + red_five_detail); test print
		return (tile_data[0]) + (tile_data[1] * 9) + (tile_data[2] * 0.5) + red_five_detail;
	}
	
	static class Numerical_tools
	{
		
		/**
		 * 
		 * @param num_list A list of numbers to calculate the mean of the whole list
		 * @return The mean of the inputed list
		 */
		public static double calc_mean(ArrayList<Double> num_list)
		{
			double mean = 0.0;
			for(double num: num_list) mean += num;
			return mean/num_list.size();
		}
		/**
		 * 
		 * @param num_list A list of numbers to calculate the variance of the whole list
		 * @return The variance of the inputed list
		 */
		public static double calc_variance(ArrayList<Double> num_list)
		{
			double mean = calc_mean(num_list);
			
			double variance = 0.0;
			for(double num: num_list) variance += Math.pow((num - mean), 2);
			
			return variance/num_list.size();
		}
		
		/**
		 * @warning when downcasting, decimal values will not be saved
		 * @param AL_double A list of doubles that needs to be downcasted and saved as a ArrayList
		 * @return an ArrayList<Integer> downcasted from an ArrayList<Double>
		 */
		public static ArrayList<Integer> downcast_AL_double(ArrayList<Double> AL_double)
		{
			ArrayList<Integer> return_AL = new ArrayList<Integer>();
			for(double num: AL_double) return_AL.add((int)num);
			return return_AL;
		}
		
		/**
		 * @info integer decimal value defaults to 0 when upcasting
		 * @param AL_double A list of integer that needs to be upcasted and saved as a ArrayList
		 * @return an ArrayList<Double> upcasted from an ArrayList<Integer>
		 */
		public static ArrayList<Double> upcast_AL_int(ArrayList<Integer> AL_int)
		{
			ArrayList<Double> return_AL = new ArrayList<Double>();
			for(int num: AL_int) return_AL.add((double)num);
			return return_AL;
		}
		
		/**
		 * @info
		 * A distribution algorithm:
		 * A variance is weighted at each turn, and it's scaled to the weighted formula
		 * indexed weight rates = i^2/40.0 (caps at 5) (where i == the index discard)
		 * 
		 * let x = the average of all variance
		 * final score = (x - 5.0) * (-0.75)
		 * Lower variance == more different suit discared == more chance for normal
		 * Higher variance == some suits are extremed out == more chance for flush
		 * @param indexed_suit_list a 2D ArrayList where each ArrayList 
		 * represents amount of unique tiles drop in each suit per turn
		 * @return The final double score of the given indexed_suit_list weighting it's variance, range = [-3.75,3.75]
		 * where positive value == more distributed, negative value == less evenly distributed/ more skewed
		 */
		public static double weighted_variance_mean(ArrayList<ArrayList<Integer>> indexed_suit_list)
		{
			ArrayList<Double> weighted_var_list = new ArrayList<Double>();
			double weight_sum = 0.0;
			for(int i = 0; i < indexed_suit_list.size(); i++)
			{
				double variance = calc_variance(upcast_AL_int(indexed_suit_list.get(i)));
				double weighted_variance = variance * (Math.pow(i, 2)/40.0);
				if(weighted_variance > 5) {weighted_variance = 5;}
				weight_sum += weighted_variance;
//				System.out.println("suit:" + indexed_suit_list.get(i) + 
//								   "\tvariance: " + variance + 
//								   "     weighted_var: " + weighted_variance);
				weighted_var_list.add(weighted_variance);
			}
			double weight_var_mean = weight_sum/(weighted_var_list.size() + 1) * 4;
			
			if(weight_var_mean > 10)	{weight_var_mean = 10;}		//Max score for LEAST evenly distributed == -3.75
			if(weight_var_mean < 0)		{weight_var_mean = 0;}		//Max score for MOST  evenly distributed == 3.75
			
			return (weight_var_mean - 5.0) * -0.75;
		}
		public static double percent_out_total(ArrayList<Double> tiles, int hand_type, int suited)
		{
			int is_tile_type = 0;
			for(int i = 0; i < tiles.size(); i++)
			{
				switch(hand_type)
				{
					case 0:
						if(tile_type(tiles.get(i)) < 2){is_tile_type++;}
						break;
					case 1:
						if(tiles.get(i).intValue()/9 == suited){is_tile_type++;}
						break;
					case 2:
						if(tile_type(tiles.get(i)) >= 2){is_tile_type++;}
						break;
				}
			}
			return is_tile_type/(double)tiles.size();
		}
		public static boolean is_red_5(double tile_val)
		{
			if(((int)tile_val) % 9 != 4) {return false;}
			String last_index = Integer.toString((int)(tile_val * 1000));
			if(last_index.charAt(last_index.length() - 1) == '5')
			{
				return true;
			}
			return false;
		}
		public static boolean is_riichi(double tile_val)
		{
			String last_index = Double.toString(tile_val);
			if(last_index.charAt(last_index.length() - 1) == '1')
			{
				return true;
			}
			return false;
		}
		public static boolean is_tedashi(double tile_val)
		{
			if(tile_val - (int)tile_val >= 0.5)
			{
				return true;
			}
			return false;
		}
		/**
		 * 
		 * @param assigned_drop_wind_id_ The wind_id representation of the drop pile, meaning numbers 0-3 in this AL
		 * @param wind_id The wind_id of the Player you want to see from the whole drop pile history
		 * @return An ArrayList of specific index that represents the drop of the given wind_id of a certain Player
		 */
		public static ArrayList<Integer> get_discard_index(ArrayList<Integer> assigned_drop_wind_id_, int wind_id)
		{
			ArrayList<Integer> indexes = new ArrayList<Integer>();
			for(int i = 0; i < assigned_drop_wind_id_.size(); i++)
			{
				if(assigned_drop_wind_id_.get(i) == wind_id)
				{
					indexes.add(i);
				}
			}
			return indexes;
		}
		public static ArrayList<ArrayList<Double>> get_index_drop_history(String whole_drop_history)
		{
			if(whole_drop_history.length() == 0) {return new ArrayList<ArrayList<Double>>();}
			
			ArrayList<Double> wind_index_list = new ArrayList<Double>();
			ArrayList<Double> tile_list = new ArrayList<Double>();
			double tile_val = 0;
			String get_tile = "";
			int wind_id = 0;
			IntUnaryOperator increment_wind_id = x -> 
			{
				if(x + 1 > 3) {return 0;}
				return ++x;
			};
			for(int i = 0; i < whole_drop_history.length(); i++)
			{
				switch(whole_drop_history.charAt(i))
				{
					case 'q':
						switch(whole_drop_history.charAt(i + 1))
						{
							case 'c':
								wind_id = increment_wind_id.applyAsInt(wind_id);
								i += 2;
								break;
							case 'p':
							case 'k':
								wind_id = Character.getNumericValue(whole_drop_history.charAt(i + 2));
								i += 2;
								break;
						}
						break;
					case 'd':
						tile_val = 0.5;
					case 't':
						wind_index_list.add((double) wind_id);
						tile_val += Compress_input.Console_io.get_tile_value(get_tile);
						tile_list.add(tile_val);
						get_tile = "";
						tile_val = 0;
						wind_id = increment_wind_id.applyAsInt(wind_id);
						break;
					default:
						get_tile += whole_drop_history.charAt(i);
						break;
				}
			}
			ArrayList<ArrayList<Double>> return_list = new ArrayList<ArrayList<Double>>();
			return_list.add(wind_index_list);
			return_list.add(tile_list);
			return return_list;
		}
	}
	
	public static class Score_probability
	{
		public ArrayList<int[]> declare_info_list_ = new ArrayList<int[]>();
		
		public ArrayList<Integer> user_tile_market_;
		
		public int seat_wind_id_;
		
		public int prev_wind_id_;
		
		public Score_probability(ArrayList<Group> called_groups, ArrayList<Integer> tile_market, int seat_wind_id, int prev_wind_id)
		{
			this.seat_wind_id_ = seat_wind_id;
			this.prev_wind_id_ = prev_wind_id;
			for(Group called_group: called_groups)
			{
				this.declare_info_list_.add(Group.group_info(called_group));
			}
			this.user_tile_market_ = tile_market;
		}
		
		/**
		 * Runs all the opportunities function in this class and returns it in a integer list, index follows [0,6]
		 * 0: dragon
		 * 1: wind
		 * 2: extended wind
		 * 3: quad
		 * 4: group_type
		 * 5: group_suit
		 * 6: term/honor
		 * 
		 * @return a list of integer values that represents the possibility of the 
		 * called groups going to achieve the score corresponding to that index
		 */
		public int[] init_score_opportunities()
		{
			int[] return_val = {
					this.dragon_opportunities(),
					this.wind_opportunities(),
					this.extend_wind_opportunities(),
					this.quad_opportunities(),
					this.mix_type_opportunities(),
					this.mix_suit_opportunities(),
					this.term_honor_opportunities()
					};
			return return_val;
			
		}
		
		/**
		 * @info
		 * 100th digit == 1/0 	representing if hand is confirmed daisangen
		 * 10th  digit == 7/2/0 representing if hand can form 7 == daisangen, 2 == shousangen, 0 == none
		 * 1st	 digit == [0,3] representing how many dragon triplets
		 * @return integer index represents informations, return is max 3 digit, mininum 0
		 */
		public int dragon_opportunities()
		{
			int point_amt = 0;
			boolean[] has_dragon = {false, false, false};	
			
			for(int[] declare_info: this.declare_info_list_)
			{
				if(declare_info[1]-30 > 0 && declare_info[0] > 1)	//WH starts at index 31
				{
					point_amt++;
					has_dragon[declare_info[1] - 31] = true;
				}
			}
			
			/*
			 * +10 	== 4/3 	non visible
			 * +1 	== 2 	non visible
			 * +0	== 0/1	non visible
			 */
			int dragon_amt = 0;
			switch(point_amt)
			{
				case 3:
					return 103;
				case 2:
					for(int i = 0; i < has_dragon.length; i++)
					{
						if(!has_dragon[i]) 
						{
							switch(this.user_tile_market_.get(i+31))
							{
								case 4:
								case 3:
									return 72;
								case 2:
									return 22;
								default:
									return 2;
							}
						}
					}
				case 1:
					for(int i = 0; i < has_dragon.length; i++)
					{
						if(!has_dragon[i]) 
						{
							switch(this.user_tile_market_.get(i+31))
							{
								case 4:
								case 3:
									dragon_amt+=10;
									break;
								case 2:
									dragon_amt++;
									break;
							}
						}
					}
					switch(dragon_amt)
					{
						case 20:
							return 71;
						case 11:
							return 21;
						default:
							return 1;
					}
				case 0:
					for(int i = 31; i < this.user_tile_market_.size(); i++)
					{
						switch(this.user_tile_market_.get(i+31))
						{
							case 4:
							case 3:
								dragon_amt+=10;
								break;
							case 2:
								dragon_amt++;
								break;
						}
					}
					switch(dragon_amt)
					{
						case 30:
							return 70;
						case 21:
							return 20;
					}
			}
			return 0;
		}
		
		/**
		 * @info
		 * return integer value:
		 * 1000th digit: 	1==has seat wind triplet 		0==no seat wind triplet
		 * 100th digit: 	5==possible	seat				0==impossible seat
		 * 10th digit: 		1==has prevalent wind triplet 	0==no prevalent wind triplet
		 * 1st digit: 		5==possible	prevalent			0==impossible prevalent
		 * 
		 * @param seat_wind the seatwind_id that would give the seat wind point
		 * @param prev_wind the prevalent_wind_id that would give the prevalent wind point
		 * @return 	A four digit integer that represents the has/possibility/impossible 
		 */
		public int wind_opportunities()
		{
			int score = 0;
			//If the visible tile amount can form a triplet
			switch(this.user_tile_market_.get(this.seat_wind_id_ + 27))
			{
				case 4:
				case 3:
					score += 500;
					break;
			}
			switch(this.user_tile_market_.get(this.prev_wind_id_ + 27))
			{
				case 4:
				case 3:
					score += 5;
					break;
			}
			for(int[] declare_info: this.declare_info_list_)
			{
				if(declare_info[1]-27 == this.seat_wind_id_ && declare_info[0] > 1)
				{
					score += 1000;
				}
				if(declare_info[1]-27 == this.prev_wind_id_ && declare_info[0] > 1)
				{
					score += 10;
				}
			}
			return score;
		}
		
		/**
		 * @info index refers to the digits from left to right as if the number was a list
		 * i.e digit 1000th == east (wind_id 0)
		 * values		meaning
		 * 0			impossible to get
		 * 1			>=3 is not visible
		 * 2			Player has that wind group
		 * @return a 4 digit integer that represents if the Player can, does, or cannot have that wind_id group
		 */
		public int extend_wind_opportunities()
		{
			int[] value = new int[4];
			for(int[] declare_info: this.declare_info_list_)
			{
				if(declare_info[1] >= 27 || declare_info[1] <= 30)
				{
					value[declare_info[1] - 27] = 2;
				}
			}
			for(int i = 0; i < value.length; i++)
			{
				if(value[i] == 0 && this.user_tile_market_.get(i + 27) >= 3)
				{
					value[i] = 1;
				}
			}
			return value[0]*1000 + value[1]*100 + value[2]*10 + value[3];
		}
		
		/**
		 * @info
		 * 10th digit 	= quad amount
		 * 1st digit  	= 1 == possible to add kan 
		 * @return a integer score that represents the amount of quads and possible added quads if the Player were to draw that tile
		 */
		public int quad_opportunities()
		{
			int score = 0;
			for(int[] declare_info: this.declare_info_list_)
			{
				if(declare_info[0] == 3)
				{
					score += 10;
				}
				else if(declare_info[0] == 2)
				{
					if(this.user_tile_market_.get(declare_info[1]) == 1)
					{
						score += 1;
					}
				}
			}
			return score;
		}
		
		/**
		 * @info
		 * 00 = not all the same 
		 * 01 = all seq
		 * 02 = all triplet/quad : no chance for all quads
		 * 12 = all triplet/quad : 4th tile not visible for all triplets
		 * 03 = all quad
		 * @return double digit int value to determine which group type score is most probable
		 */
		public int mix_type_opportunities() 
		{
			Set<Integer> call_type = new HashSet<Integer>();
			int can_all_quad = 10;
			for(int[] group_info: this.declare_info_list_)
			{
				if(call_type.size() > 0 && call_type.add(group_info[0]))
				{
					return 0;
				}
				if(group_info[0] == 2)
				{
					if(this.user_tile_market_.get(group_info[1]) == 0)
					{
						can_all_quad = 0;
					}
				}
			}
			if(call_type.size() != 1) {return 0;}
			ArrayList<Integer> get_set_element = new ArrayList<Integer>(call_type);
			switch(get_set_element.get(0))
			{
				case 1:
					return 1;
				case 2:
					return can_all_quad + 2;
				case 3:
					return 3;
			}
			return 0;
		}
		
		/**
		 * @info
		 * 10th digit: 	if there is only 1 suit that was called on, this would represent the flush suit
		 * 1st digit:	1 if there is honors, 0 is there is no honor
		 * the return value is -10/-11 is flush hand is impossible (multiple suit were called)
		 * @return double digit int value to determine which suit and if the calls include honors
		 */
		public int mix_suit_opportunities()
		{
			int has_honor = 0;
			int suit = -1;
			for(int[] declare_info: this.declare_info_list_)
			{
				if(declare_info[1]/9 == 3)
				{
					has_honor = 1;
				}
				else if(suit == -1)
				{
					suit = declare_info[1]/9;
				}
				else if(suit != declare_info[1]/9)
				{
					return -10 - has_honor;
				}
			}
			return (suit * 10) + has_honor;
		}
		
		/**
		 * @info
		 * 100th digit: amount of terminal groups
		 * 10th	 digit: amount of honor groups
		 * 1st   digit: amount of other groups
		 * @return three digit int value to determine the amount of groups called that are either terminal, honor, neither
		 */
		public int term_honor_opportunities()
		{
			/*
			 * return 0 for not terminal/honor
			 * return 1 for terminal
			 * return 2 for honor
			 */
			IntUnaryOperator doubleInt = x -> 
			{
				if(x > 26){return 2;}
				if(x%9==0 || x%9==8) {return 1;}
				return 0;
			};
			int[] groups = new int[3];
			for(int[] declare_info: this.declare_info_list_)
			{
				groups[doubleInt.applyAsInt(declare_info[2])]++;
			}
			return groups[1]*100 + groups[2]*10 + groups[0];
		}
	}
	public static void main(String[] args)
	{	
		
		//Hand: 115r6789p123678sckqo (4,7p)
		Prediction normal1 = new Prediction(read_dropSTR("wdndrdrdrdgdgdgdededed"));
		//Hand: 45567m33789sckq111zo (3,6m)
		Prediction normal2 = new Prediction(read_dropSTR("nd2pd7pd1md9md8sdwht5rpt2sdst3pt1st6ptgt"));
		//Hand: 789m234678p78s11zckqo (6,9s)
		Prediction normal3 = new Prediction(read_dropSTR("1md4md4st1pd-1s2m1swwh9pwe3m5rs4m"));
		//Hand: 3789m234p566778sckqo (3m tanki)
		Prediction normal4 = new Prediction(read_dropSTR("whd1md2sdgt1st5md9sd4sd1mt9st"));
		//Hand: 23499m455667p67s (5,8s)
		Prediction normal5 = new Prediction(read_dropSTR("sdrdrt9sted6md2pd-1p6s2s1pwhw6s6m"));
		//Hand: 123m44789p23345sckqo (1,4s)
		Prediction normal6 = new Prediction(read_dropSTR("nd2md2sd6st5rmdwhtrt9mtgdgd7pd-1pw5m5p7mgn"));
		//Hand: 78m567789p33789sckqo (6,9m)
		Prediction normal7 = new Prediction(read_dropSTR("9mdrd4st9sdrt2pd-w4m4p5m1s5rm4p6s5p"));
		
		Prediction flush1 = new Prediction(read_dropSTR("2md3md4md5md6md7md8md2pd3pd4pd5pd6pd7pd8pd"));
		
		//Hand: 9s123567778sckq555zo (no ten)
//		Prediction flush1 = new Prediction(read_dropSTR("4md9md3md3mtrd6mdsd3pd6pd6pt8pd"));
		//Hand: 12233447m57zckq789mo (noten)
		Prediction flush2 = new Prediction(read_dropSTR("7pd8pd3sd3sd6sd1pdwded3pd6pd"));
		//Hand: 13377s33zckq999s444zo (noten)
		Prediction flush3 = new Prediction(read_dropSTR("6md6pd5md5rpt6st3md8md2pd1mdgdrded5mdrtsdgt3pd"));
		//Hand: 1222355888s222zckqo (25s shanpon)
		Prediction flush4 = new Prediction(read_dropSTR("2pd4pd8pd1pd4pt2md8ptwhd1md2md3md4md6pd6pted-"));
		//Hand: 33m22zckq777z111z111mo (3m S shanpon)
		Prediction flush5 = new Prediction(read_dropSTR("4pd4sd8sd4sd7pd7pd6md9mt5pt2st5rmt5mt2md6pd4mt7st8mt2mt2pt"));
		//Hand: 12p44zckq555p678p999po (3p edge wait)
		Prediction flush6 = new Prediction(read_dropSTR("4sd1mt6stsd7stwd9st4stedgd4mt8pt6pdnt5st"));
		//Hand: 678p3444666788sckqo (Noten)
		Prediction flush7 = new Prediction(read_dropSTR("2md5pd3pd3ptndgdrdndsdrdwt1mtwhted4pt9pt8mt"));
		//Hand: 8m6p345566899s44zckqo (no ten)
		Prediction flush8 = new Prediction(read_dropSTR("9mdwhd5md3pdsdwd9pt3mt2ptrd7pt"));
		//Hand: 5r6788m66zckq444m777zo (8m g shanpon)
		Prediction flush9 = new Prediction(read_dropSTR("8sd1st9mdet6pd5pd6sd7sd7sd1st3md6mt3pt"));
		
		//Hand: 2233m55p779s4477zckqo (9s tanki) wind_id = 2
		Prediction seven_pairs1 = new Prediction(read_dropSTR("2pdwd1pt7md4mt-1mt"));
		//Hand: 66m5566p45577s11zckqo wind_id = 0
		Prediction seven_pairs2 = new Prediction(read_dropSTR("1sdrd9sd3st7pt2mt9st3pdnd6sd-8m2mwh6s"));
		//Hand: 2233m55p779s4477zckqo wind_id = 0
		Prediction seven_pairs3 = new Prediction(read_dropSTR("9sd2pd7pt2mdet2sd5sd-9m1p9p6p7pw7s9m9p1ss"));
		//Hand: 355889m11449p99sckqo wind_id = 0
		Prediction seven_pairs4 = new Prediction(read_dropSTR("sdedstwd5pt3sd6sd6pd3stgt2pdgt8st4mdwt3st8st2mt"));
		//Hand: 44m11227788p335sckqo wind_id = 3
		Prediction seven_pairs5 = new Prediction(read_dropSTR("6sded7sd1mdgt3md9sd-2ssw9m3m4ps5p1m"));
		
		//7_pairs_2 round
		ArrayList<ArrayList<Double>> example_drop_history1 = Numerical_tools.get_index_drop_history("1sdwtwhd1ptrdqp3ndwd9sdwhd8pdgd3stndwt2st7pt3sd8pted2mtst3stwt9st1pt3pt1sd3pdst5rst8mtnt4ptgdgt6sd-");
		//7_pairs_3 round
		ArrayList<ArrayList<Double>> example_drop_history2 = Numerical_tools.get_index_drop_history("9sd6mtsdgd2pded2pd1mt7ptrd8mdqcmwhd2mdwt5mtntet5rmtwtet2sd8md9mt7md5sd-");
		//7_pairs_4 round
		ArrayList<ArrayList<Double>> example_drop_history3 = Numerical_tools.get_index_drop_history("sdqp1ed8pt6sted5ptnd6mtst5stwhd3stwdwhtwht5mt5pt8mt2mt3sdqcl3pdnted6sd6pdgd7st6pd2ptqcm2sd2sd3strtrt4stgt7pt5sdnt2pd4st6pd8pdgt7mtet5rpt8st4mt1mt7md4md7mtnt2mtwtwt4stgt3st5ptqcr9pd2st8st2wht1st5st2mt9mt3ptwt1st5rst");
		//7_pairs_5 round
		ArrayList<ArrayList<Double>> example_drop_history4 = Numerical_tools.get_index_drop_history("nd6pd5mt6pd9mt4sd9pdednt4pd7st7sdnt3pt1sd1mdsd1st9sdgt3sdrtgdqp1wd5mt3mdetwd6st9sd-");
		
		ArrayList<ArrayList<Double>> worst_drop_history1 = Numerical_tools.get_index_drop_history("1md1md1mded2md2md2mdsd3md3md3mdwd4md4md4mdnd5md5md5mdwhd6md6md6mdgd7md7md7mdrd8md8md8md1sd9md9md9md2sd1pd1pd1pd3sd2pd2pd2pd4sd3pd3pd3pd5sd4pd4pd4pd6sd5pd5pd5pd7sd6pd6pd6pd8sd7pd7pd7pd9sd8pd8pd8pd9pd");
		ArrayList<ArrayList<Double>> worst_drop_history2 = Numerical_tools.get_index_drop_history("1md1md1mded2md2md2mdsd3md3md3mdwd4md4md4mdnd5md5md5mdwhd");
		ArrayList<ArrayList<Double>> best_drop_history = Numerical_tools.get_index_drop_history("1md1md1md1md2md2md2md2md3md3md3md3md4md4md4md4md5md5md5md5md6md6md6md6md7md7md7md7md8md8md8md8md9md9md9md9md1pd1pd1pd1pd2pd2pd2pd2pd3pd3pd3pd3pd4pd4pd4pd4pd5pd5pd5pd5pd6pd6pd6pd6pd7pd7pd7pd7pd8pd8pd8pd8pd9pd9pd9pd9pd1sd1sd1sd1sd2sd2sd2sd2sd3sd3sd3sd3sd4sd4sd4sd4sd5sd5sd5sd5sd6sd6sd6sd6sd7sd7sd7sd7sd8sd8sd8sd8sd9sd9sd9sd9sd");
		ArrayList<ArrayList<Double>> best_drop_history2 = Numerical_tools.get_index_drop_history("1md1md1md1md2md2md2md2md3md3md3md3md4md4md4md4md5md5md5md5md");
		
		System.out.println(example_drop_history1.get(1));
		System.out.println("I" + seven_pairs2.seven_pairs_prob() + "  II: " + seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(example_drop_history1.get(0)), 0), example_drop_history1.get(1)));
		System.out.println("I" + seven_pairs3.seven_pairs_prob() + "  II: " + seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(example_drop_history2.get(0)), 0), example_drop_history2.get(1)));
		System.out.println("I" + seven_pairs4.seven_pairs_prob() + "  II: " + seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(example_drop_history3.get(0)), 0), example_drop_history3.get(1)));
		System.out.println("I" + seven_pairs5.seven_pairs_prob() + "  II: " + seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(example_drop_history4.get(0)), 3), example_drop_history4.get(1)));
		System.out.println(seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(worst_drop_history1.get(0)), 3), worst_drop_history1.get(1)));
		System.out.println(seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(worst_drop_history2.get(0)), 3), worst_drop_history2.get(1)));
		System.out.println(seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(best_drop_history.get(0)), 3), best_drop_history.get(1)));
		System.out.println(seven_pairs_prob(Numerical_tools.get_discard_index(Numerical_tools.downcast_AL_double(best_drop_history2.get(0)), 3), best_drop_history2.get(1)));
		//Hand: Hand: 19m19p19s1234456zckqo (r kokushi wait)
		Prediction kokushi1 =  new Prediction(read_dropSTR("7sd5sd4pd3pd3pd2pd5md5md5st2mt3mt1pd"));
		//Hand: 19m19p19s1223567zkqo (north kokushi wait)
		Prediction kokushi2 =  new Prediction(read_dropSTR("7md5md7pd8pd9md4md6pd2sd7md3st9pd6pt1mded-wh5p6p1s5s"));
		//Hand: 9m19p19s12345677zkqo (1m kokushi wait)
		Prediction kokushi3 =  new Prediction(read_dropSTR("6sd5st4st7md6md5md8mt4pd5pd1sdwhd5rptwt"));
		//Fake
		Prediction kokushi4 =  new Prediction(read_dropSTR("2md2pd2sd3md3pd3sd4md4pd4sd5md5pd5sd6md6pd6sd7md7pd7sd8md8pd8sd"));
		
		Prediction extreme = new Prediction(read_dropSTR("1md2md3md4md5md6md7md8md9md1pd1sd1zd-"));

		String[] name = {"Normal", "Flush", "7 Pairs", "Kokushi"};
		ArrayList<Prediction> test_list = new ArrayList<Prediction>();
		test_list.add(normal1);test_list.add(normal2);test_list.add(normal3);
		test_list.add(normal4);test_list.add(normal5);test_list.add(normal6);
		test_list.add(normal7);test_list.add(flush1);test_list.add(flush2);
		test_list.add(flush3);test_list.add(flush4);test_list.add(flush5);
		test_list.add(flush6);test_list.add(flush7);test_list.add(flush8);
		test_list.add(flush9);test_list.add(seven_pairs1);test_list.add(seven_pairs2);test_list.add(seven_pairs3);test_list.add(kokushi1);
		test_list.add(kokushi2);test_list.add(kokushi3);test_list.add(kokushi4);test_list.add(extreme);
		
		int name_index = 0;
		int hand_index = 1;
//		for(int i = 0; i < test_list.size() - 1; i++)
//		{
//			switch(i) 
//			{
//				case 7: 
//					name_index = 1; 
//					hand_index = 1;
//					break; 
//				case 16: 
//					name_index = 2; 
//					hand_index = 1;
//					break; 
//				case 19: 
//					name_index = 3; 
//					hand_index = 1;
//					break;
//			}
//			System.out.println(name[name_index] + hand_index + ": ");
//			System.out.println(test_list.get(i).drop_pile_);
//			System.out.println("normal score: " + test_list.get(i).normal_prob());
//			System.out.println("flush score: " + test_list.get(i).flush_prob());
//			System.out.println("Kokushi score: " + test_list.get(i).kokushi_prob());
//			System.out.println("simple 7_pair score: " + test_list.get(i).seven_pairs_prob());
//			System.out.println("complex 7_pair score: " + test_list.get(i).seven_pairs_prob(Numerical_tools.downcast_AL_double(example_drop_history1.get(0)),example_drop_history1.get(1)));
//			hand_index++;
//		}
	}
}

package bot_package;

import java.util.*;
import java.util.function.*;

/**
 * This class is a temporary i/o system that will intake a String that gives information about what happened during the turn
 * String index reference:
 * [0] = player wind ID 		(0 = E, 1 = S, 2 = W, 3 = N)
 * 
 * IF someone else called on the tile, this ID would be the Player that called the tile
 * IF no one called on the tile, the player wind ID should be who dropped the tile
 * 
 * [1] = decision 				(0 = tedashi (drop from hand), 
 * 								 1 = tsumogiri (drop from draw),
 * 								 2 = chi,
 * 								 3 = pon,
 * 								 4 = call kan,
 * 								 5 = added kan,
 * 								 6 = concealed kan,
 * 								 7 = if {player wind ID == wind_ID_turn_}: tsumo
 * 									 if {player wind ID != wind_ID_turn_}: ron) 
 * [2,3] = tileID 				(refer to Nums to TilesVal.txt)
 * [4] = f						(states flowers used, still remain if no flower dropped) 
 * [5,x] = all the flowers		(the flower numbers is saved)
 * [x+1] = f
 * [x+2,x+3] = [0,4]-2          (Used to reference tileID were used to call index 2-3)
 * 								(The talking tile range is [-2,2], which is why the index is represented as [0,4]-4)
 * 								(if num > 2, then there is no tiles used to call)
 * 								(pon/kan will be formatted the same, just refer to decision to call)
 * 								*note* when added to used_tiles_, it is converted into tile_ID
 * 
 * example system, current player (me) == 2,
 * 0005f12f55 == 1st Player, drop from hand, 6m, after using flowers 1;2, no tiles used
 */
public class Compress_input 
{
	private boolean valid_move_;
	private int player_wind_ID_;
	private int decision_;
	private int tile_ID_;
	private ArrayList<Integer> used_tiles_ = new ArrayList<Integer>();
	private ArrayList<Integer> declared_flowers_ = new ArrayList<Integer>();
	private static Scanner console_scan;
	public Compress_input(String input_str, MJ_game current_game)
	{
		/*
		 * Need player wind ID, decision, tile_ID (single digit ID starts with 0), 2 'f' for flower
		 * Minimum length of the input_str expected to be 5;
		 */
		if(input_str.length() < 5)
		{
			this.valid_move_ = false;
		}
		else
		{
			int start_flower = 0;
			/*
			 *  0 = wind_ID
			 *  1 = decision
			 *  2 = tile_ID
			 *  3 = flower
			 *  4 = tiles_used
			 */
			int add_mode = 0;
			for(int i = 0; i < input_str.length(); i++)
			{
				if(input_str.charAt(i) == 'f' && start_flower == 0)
				{
					start_flower++;
					add_mode = 3;
					continue;
				}
				switch(add_mode)
				{
					case 0: 	// Set wind_ID
						this.player_wind_ID_ = Integer.parseInt(Character.toString(input_str.charAt(i)));
						break;
					case 1:		// Sets decision
						this.decision_ = Integer.parseInt(Character.toString(input_str.charAt(i)));
						break;
					case 2:		// Sets the tile_ID
						if((i + 1) < input_str.length())
						{
							this.tile_ID_ = Integer.parseInt(input_str.substring(i, i + 2));
						}
						break;
					case 3:		// Sets flowers used
						while(input_str.charAt(i) != 'f')
						{
							this.declared_flowers_.add(Integer.parseInt(Character.toString(input_str.charAt(i))));
							i++;
						}
						add_mode++;
						i++;
						break;
					case 4:		// Used tiles if called/declared
						this.used_tiles_.add(Integer.parseInt(Character.toString(input_str.charAt(i))));
						break;
				}
				add_mode++;
			}
		}
		this.valid_move_ = this.valid_turn(current_game);
	}
	
	public Compress_input(int wind_ID, int decision, int tile_ID, ArrayList<Integer> declared_flowers, ArrayList<Integer> used_tiles, MJ_game current_game)
	{
		this.player_wind_ID_ = wind_ID;
		this.decision_ = decision;
		this.tile_ID_ = tile_ID;
		this.declared_flowers_ = declared_flowers;
		this.used_tiles_ = used_tiles;
		this.valid_move_ = this.valid_turn(current_game);
	}
	
	/**
	 * checks if this input is a valid input, this function will check:
	 *	1- Valid tiles/flowers										(not out of range or doesn't exist)
	 *	2- There are no tiles in the wall 							(to be able to draw a new tile during new turn)
	 *	3- Player dropping tile was also labeled to call tile		(Player cannot call their own tile)
	 * 	4- Called sequence is called by the person after current	(only next player can call sequence)
	 * 	5- declared self-kan is not by self Player					(must be the Player's turn to declare self-kan)
	 *  
	 * @return true if all the test above passes, false is any of them fails the test
	 */
	public boolean valid_turn(MJ_game current_game)
	{
		//condition 1, valid tiles
		Predicate<Integer> withinRange = num -> num >= 0 && num <= 33;		//Lambda to see range of tile_ID input
		if(!withinRange.test(this.tile_ID_))									//Not within range
		{
			return false;
		}
		//Tile doesn't exist (all used or discarded)
		if(current_game.get_possible_tiles().get(this.tile_ID_ / 9).get(this.tile_ID_ % 9) == 0) {return false;}
		for(int i = 0; i < this.used_tiles_.size(); i++) 
		{
			int used_tileID = this.used_tiles_.get(i) + this.tile_ID_;
			if(!withinRange.test(used_tileID)) {return false;}	//Not within range
			//Tile doesn't exist (all used or discarded)
			if(current_game.get_possible_tiles().get(used_tileID / 9).get(used_tileID % 9) == 0) {return false;}
		}
		
		ArrayList<Integer> temp_flowers = new ArrayList<Integer>(current_game.get_possible_flowers());
		for(int i = 0; i < temp_flowers.size(); i++)
		{
			if(this.declared_flowers_.get(i) < 0 || this.declared_flowers_.get(i) > 3) {return false;}
			if(temp_flowers.get(this.declared_flowers_.get(i)) > 0)
			{
				//Decrements flower that appears when checking declared
				temp_flowers.set(this.declared_flowers_.get(i), temp_flowers.get(this.declared_flowers_.get(i)) - 1);
			}
			else
			{
				//No flower was found, this declare is impossible and returns false
				return false;
			}
		}
		
		//condition 2
		if(current_game.total_tiles_left() == 0)
		{
			return false;
		}
		
		//condition 3
		if(this.player_wind_ID_ == current_game.get_current_windID_turn() &&
		   (this.decision_ > 1 && this.decision_ < 5))
		{
			return false;
		}
		
		//condition 4
		if(this.decision_ == 2 && 
		   this.player_wind_ID_ != current_game.get_current_windID_turn() + 1)
		{
			return false;
		}
		
		//condition 5
		if((this.decision_ == 5 || this.decision_ == 6) &&
			this.player_wind_ID_ != current_game.get_current_windID_turn())
		{
			return false;
		}
		return true;
	}
	
	/**
	 * 
	 * @return The Player this Compress_input is referring to perform the decision being made
	 */
	public int get_windID()
	{
		return this.player_wind_ID_;
	}
	
	/**
	 * 
	 * @return The decision (drop/discard/call/any move) being made
	 */
	public int get_decision()
	{
		return this.decision_;
	}
	
	/**
	 * 
	 * @return The tile being called/discarded/added
	 */
	public int get_tileID()
	{
		return this.tile_ID_;
	}
	
	/**
	 * 
	 * @return Any flowers the Player being referred to dropped during that move
	 */
	public ArrayList<Integer> get_flower_list()
	{
		return this.declared_flowers_;
	}
	
	/**
	 * 
	 * @return The Used tiles to call/declared, if the decision was discard, this can be left empty
	 */
	public ArrayList<Integer> get_used_tiles()
	{
		return this.used_tiles_;
	}
	
	/**
	 * 
	 * @return A boolean to represent if this is a valid move or not
	 */
	public boolean is_validMove()
	{
		return this.valid_move_;
	}
	
	/**
	 * synchronized stops other threads from running until this function is complete, 
	 * it continues the threads afterwards
	 * 
	 * @info
	 * white dragon is always char == 'h'
	 * The input string can either be formatted as: 
	 * mode 1: per move tiles
	 *	   tsumogiri 	== 1 copy of the symbol 	(i.e 6m [1 copy of 6])
	 *	   tedashi		== 2 copies of the symbol	(i.e 66m [2 copies of 6])
	 *	   Honors:
	 *	   input the letter representation of the tile,
	 *	   e = east tsumogiri
	 *	   w = west tsumogiri
	 * 
	 * Flowers are considered at the end of the game, do not input
	 * 
	 * mode 2: sequential amount of moves:
	 * 		tsumogiri 	== tile + "t"
	 * 		tedashi		== tile + "d"
	 * 
	 * mode 3: single calls
	 * - with "q" == somebody called
	 * - with type of call "c" = chi, "p" = pon, "k" = "kan", "r" = "ron"
	 * - with wind_id of player that called
	 * 
	 * i.e qp2 or qc1 (it will take the previous tile and consider that called)
	 * 			sequential call
	 * 
	 * A String sequence can look like this
	 * inputed wind_id = 2
	 * 3md7mt8pt{htqp1} takes previous discard as call
	 * 
	 * @return A sequential String that represents
	 */
	public static synchronized String console_input(int wind_id)
	{
		System.out.println("Input drop tile or sequence of drop tiles: ");
		String input = "";
		while(input.isEmpty())
		{
			console_scan = new Scanner(System.in);
			input = console_scan.nextLine();
		}
		return input;
	}
	
	/**
	 * 
	 */
	public static ArrayList<Double> inputed_tiles(String console_input)
	{
		
	}
}

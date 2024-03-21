package bot_package;

import java.util.*;


/**
 * Game_system doesn't 
 */
public class MJ_game
{
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public ArrayList<ArrayList<Integer>> possible_tiles = new ArrayList<ArrayList<Integer>>();
	
	/*
	 * The int value that represents the remaining amount of tiles that does include other Player's hand
	 * When starting a game, this doesn't take into consideration 1st tile from dealer, 
	 * first tile will come as first Compress_input
	 * 
	 * Should not include flower tiles/ or any kind of bonus tile
	 */
	public int total_wall_tiles;
	
	/*
	 * With each index that would represent the Player's wind_ID, the int value would indicate the amount
	 * of remaining called Group possible (4 == no declared Group)
	 */
	public int[] player_status;
	
	/*
	 * The list of possible flowers that is either in other Player's hand or in the wall
	 * Each index represents the amount of flowers not visible
	 */
	public ArrayList<Integer> possible_flowers; 
	
	/**
	 * status int reference:
	 * 0 = normal
	 * 1 = last tile
	 * 2 = 
	 */
	public int special_status = 0;
	
	/**
	 * This stores the move history of a given game
	 */
	public ArrayList<Compress_input> move_history;
	
	/**
	 * This will be the counter responsible for whose turn it currently is
	 */
	public int wind_ID_turn = 0;
	
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * This default constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public MJ_game()
	{
		for(int i = 0; i < 4; i++) 
		{
			possible_tiles.add(new ArrayList<Integer>());
			int max = 9;
			switch(i) //Used to add numbered suit and honor suit separately
			{
				case 3:
					max = 7;
				default:
					for(int j = 0; j < max; j++) possible_tiles.get(i).add(4);
					break;
			}
		}
		this.total_wall_tiles = 136 - (4 * 13);							// 4(amt) * 34(IDs) == 136 - 4(Players) * 13(Start hand amt)
		for(int i = 0; i < 4; i++) this.possible_flowers.add(2);
	}
	
	/**
	 * @param hand_statuses For each player in the game, the index corresponds to their hand status,
	 * 						each index represents the amount of calls the player has made.
	 * @param flowers_used All the visible flowers that are declared.
	 * @param visible_tiles All the visible tiles, this includes drop pile, called tiles. (DON'T ADD COMMA)
	 */
	public MJ_game(int[] flowers_used, String visible_tiles, int current_windTurn)
	{
		for(int i = 0; i < 4; i++) 
		{
			possible_tiles.add(new ArrayList<Integer>());
			int max = 9;
			switch(i) //Used to add numbered suit and honor suit separately
			{
				case 3:
					max = 7;
				default:
					for(int j = 0; j < max; j++) possible_tiles.get(i).add(4);
					break;
			}
		}
		
		this.total_wall_tiles = 136 - (int)(visible_tiles.length()/2) - (4 * 13);
		
		String temp_str = "";
		for(int i = 0; i < visible_tiles.length(); i++)
		{
			if(temp_str.length() < 2) //Each tile will take 2 indexes
			{
				temp_str += visible_tiles.charAt(i);
			}
			else
			{
				//If tile is visible, remove it from known tiles in 2D ArrayList<Integer>
				try
				{
					int suit = Integer.parseInt(temp_str) / 9;
					int index = Integer.parseInt(temp_str) % 9;
					this.possible_tiles.get(suit).set(index, this.possible_tiles.get(suit).get(index) - 1);
				}
				catch(Exception e)
				{
					System.out.println("String inputed was not in 2 index tile_ID format");
				}
				temp_str = "";
			}
		}
		
		this.wind_ID_turn = current_windTurn;
		if(wind_ID_turn > 3)
		{
			this.wind_ID_turn = 0;
		}
	}
	
	public ArrayList<ArrayList<Integer>> get_possible_tiles(){return this.possible_tiles;}
	public int total_tiles_left() {return this.total_wall_tiles;}
	public int[] get_player_status() {return this.player_status;}
	public ArrayList<Integer> get_possible_flowers(){return this.possible_flowers;}
	public int get_special_status() {return this.special_status;}
	public ArrayList<Compress_input> get_move_history(){return this.move_history;}
	public int get_current_windID_turn() {return this.wind_ID_turn;}
	
	
 	public boolean update_game(Compress_input new_turn)
	{
 		if(!new_turn.is_validMove()) {return false;}
 		
 		//Adds the valid turn into move_history
 		this.move_history.add(new_turn);
 		
		//Removes input tile from wall
		int suit = new_turn.get_tileID() / 9;
		int index = new_turn.get_tileID() % 9;
		
		//No tiles to offer, no tiles remove, return false
		if(this.possible_tiles.get(suit).get(index) == 0)
		{
			return false;
		}
		this.possible_tiles.get(suit).set(index, this.possible_tiles.get(suit).get(index) - 1);
		
		//Checks the same conditions for used_tiles
		for(int i = 0; i < new_turn.get_used_tiles().size(); i++)
		{
			suit = new_turn.get_used_tiles().get(i) / 9;
			index = new_turn.get_used_tiles().get(i) % 9;
			if(this.possible_tiles.get(suit).get(index) == 0)
			{
				return false;
			}
			this.possible_tiles.get(suit).set(index, this.possible_tiles.get(suit).get(index) - 1);
		}
		
		// valid Move already checks if there are possible flowers, since true, it's safe to remove flowers
		for(int i = 0; i < new_turn.get_flower_list().size(); i++)
		{
			this.possible_flowers.set(new_turn.get_flower_list().get(i), this.possible_flowers.get(new_turn.get_flower_list().get(i)) - 1);
		}
		
		switch(new_turn.get_decision())
		{
			//No calls
			case 0:
			case 1:
				break;
			//Sequence call
			case 2:
				break;
			//Triplet call
			case 3:
				break;
			//Called kan
			case 4:
				break;
			//Added kan
			case 5:
				break;
			//Concealed kan
			case 6:
				break;
			//Ron
			case 7:
				break;
		}
	}
}

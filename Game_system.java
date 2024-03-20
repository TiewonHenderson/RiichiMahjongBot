package bot_package;

import java.util.*;

public class Game_system 
{
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public ArrayList<ArrayList<Integer>> possible_tiles = new ArrayList<ArrayList<Integer>>();
	
	/*
	 * The int value that represents the remaining amount of tiles that doesn't include
	 * other Player's hand
	 */
	public int total_nonbonus_tiles;
	
	/*
	 * With each index that would represent the Player's wind_ID, the int value would indicate the amount
	 * of remaining called Group possible (4 == no declared Group)
	 */
	public int[] player_status;
	
	/*
	 * The list of possible flowers that is either in other Player's hand or in the wall
	 * This keeps tracks of flowers that are possible
	 */
	public int[] possible_flowers; 
	
	/**
	 * status int reference:
	 * 0 = normal
	 * 1 = last tile
	 * 2 = 
	 */
	public int special_status = 0;
	
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * This default constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public Game_system()
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
		this.total_nonbonus_tiles = 136;							// 4(amt) * 34(IDs) == 136
		int[] new_flowers = {0,0,1,1,2,2,3,3};						// 2(amt) * 4(types) == 8
		this.possible_flowers = new_flowers;
	}
	
	/**
	 * @param hand_statuses For each player in the game, the index corresponds to their hand status,
	 * 						each index represents the amount of calls the player has made.
	 * @param flowers_used All the visible flowers that are declared.
	 * @param visible_tiles All the visible tiles, this includes drop pile, called tiles. (DON'T ADD COMMA)
	 */
	public Game_system(int[] hand_statuses, int[] flowers_used, String visible_tiles)
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
		
		this.total_nonbonus_tiles = 136 - (int)(visible_tiles.length()/2);
		
		//Takes into consideration other peoples hand into total_nonbonus_tiles
		for(int i = 0; i <  hand_statuses.length; i++)
		{
			this.total_nonbonus_tiles -= (4 - hand_statuses[i]) * 3 + 1;
		}
		
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
	}
	public boolean update_wall(Compress_input new_turn)
	{
		//Removes input tile from wall
		int suit = new_turn.get_tileID() / 9;
		int index = new_turn.get_tileID() % 9;
		
		//No tiles to offer, no tiles remove, return false
		if(this.possible_tiles.get(suit).get(index) == 0)
		{
			return false;
		}
		this.possible_tiles.get(suit).set(index, this.possible_tiles.get(suit).get(index) - 1);
		
		
	}
	
}

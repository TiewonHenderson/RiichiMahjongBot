package bot_package;

import java.util.*;

public class Game_system 
{
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public HashMap<Integer, Integer> possible_tiles = new HashMap<Integer, Integer>();
	public int total_nonbonus_tiles;
	
	public int[] possible_flowers; 
	
	public Game_system()
	{
		for(int i = 0; i <= 33; i++) possible_tiles.put(i, 4);		// In a new game, each tile amount == 4
		this.total_nonbonus_tiles = 136;							// 4(amt) * 34(IDs) == 136
		int[] new_flowers = {0,0,1,1,2,2,3,3};						// 2(amt) * 4(types) == 8
		this.possible_flowers = new_flowers;
	}
	
	/**
	 * @param hand_statuses For each player in the game, the index corresponds to their hand status,
	 * 						each index represents the amount of calls the player has made.
	 * @param flowers_used All the visible flowers that are declared.
	 * @param visible_tiles All the visible tiles, this includes drop pile, called tiles.
	 */
	public Game_system(int[] hand_statuses, int[] flowers_used, String visible_tiles)
	{
		for(int i = 0; i <= 33; i++) possible_tiles.put(i, 4);
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
				//If tile is visible, remove it from known tiles in HashMap
				try
				{
					possible_tiles.replace(Integer.parseInt(temp_str), possible_tiles.get(Integer.parseInt(temp_str)) - 1);
				}
				catch(Exception e)
				{
					System.out.println("String inputed was not in 2 index tile_ID format");
				}
				temp_str = "";
			}
		}
	}
}

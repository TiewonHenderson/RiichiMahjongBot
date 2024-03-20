package bot_package;

import java.util.*;

/**
 * This class is a temporary i/o system that will intake a String that gives information about what happened during the turn
 * String index reference:
 * [0] = player wind ID 		(0 = E, 1 = S, 2 = W, 3 = N)
 * [1] = decision 				(0 = tedashi (drop from hand), 
 * 								 1 = tsumogiri (drop from draw),
 * 								 2 = chi,
 * 								 3 = pon,
 * 								 4 = call kan,
 * 								 5 = added kan,
 * 								 6 = concealed kan,
 * 								 7 = ron) 
 * [2,3] = tileID 				(refer to Nums to TilesVal.txt)
 * [4] = f						(states flowers used, still remain if no flower dropped) 
 * [5,x] = all the flowers		(the flower numbers is saved)
 * [x+1] = f
 * 
 * 
 * example system, current player (me) == 2,
 * 0005f12f == 1st Player drop from hand 6m after using flowers 1,2
 */
public class Compress_input 
{
	public static ArrayList<Compress_input> input_history_;
	
	public int player_wind_ID;
	public int decision;
	public int tile_ID;
	public ArrayList<Integer> declared_flowers = new ArrayList<Integer>();
	
	public Compress_input(String input_str)
	{
		/*
		 * Need player wind ID, decision, tile_ID (single digit ID starts with 0), 2 'f' for flower
		 * Minimum length of the input_str expected to be 5;
		 */
		if(input_str.length() < 5)
		{
			
		}
		else
		{
			int start_flower = 0;
			/*
			 *  0 = wind_ID
			 *  1 = decision
			 *  2 = tile_ID
			 *  3 = flower
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
					case 0:
						this.player_wind_ID = Integer.parseInt(Character.toString(input_str.charAt(i)));
						break;
					case 1:
						this.decision = Integer.parseInt(Character.toString(input_str.charAt(i)));
						break;
					case 2:
						if((i + 1) < input_str.length())
						{
							this.tile_ID = Integer.parseInt(input_str.substring(i, i + 2));
						}
						break;
					case 3:
						while(input_str.charAt(i) != 'f')
						{
							this.declared_flowers.add(Integer.parseInt(Character.toString(input_str.charAt(i))));
							i++;
						}
				}
				add_mode++;
			}
		}
	}
	
	public Compress_input(int wind_ID, int decision, int tile_ID, ArrayList<Integer> declared_flowers)
	{
		this.player_wind_ID = wind_ID;
		this.decision = decision;
		this.tile_ID = tile_ID;
		this.declared_flowers = declared_flowers;
	}
	
	public int get_windID(){return this.player_wind_ID;}
	public int get_decision() {return this.decision;}
	public int get_tileID() {return this.tile_ID;}
	public ArrayList<Integer> get_flower_list(){return this.declared_flowers;}
}

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
	public static ArrayList<String> input_history_;
	
}

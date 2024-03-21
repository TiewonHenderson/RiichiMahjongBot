package bot_package;

import java.util.*;

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
	ArrayList<ArrayList<Integer>> tile_market_;
	public static void main(String[] args)
	{
		
	}
}

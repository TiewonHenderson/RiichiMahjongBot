package bot_package_v2;

import java.util.*;

import bot_package_v2.MJ_tools.*;


/**
 	0: Mezen Tsumo
	1: ChiToi
	2: 13 Orphans
	
	3: Under the river / Under the sea
	4: Robbing a kan
	5: After a kan
	
	6: Haku
	7: Hatsu
	8: Chun
	9: Shousangen
	10: Daisangen
	
	11: Seat Wind
	12: Prevalent Wind
	13: 4 little Winds
	14: 4 Big Winds

	15: Pinfu
	16: Toitoi
	17: 4 Concealed Triplets
	18: 4 Concealed Triplets Single Tile Wait
	19: 4 Quads
	
	20: Half Flush
	21: Full Flush
	22: All Honors
	23: Nine Gates
	24: Pure Nine Gates 
	
    25: Half Outside Hand
    26: Fully Outside Hand
    27: All Terms/Honors
    28: All Terminal
	
	27: Tenhou
	28: Chihou
 */
public class Scoring 
{
	/**
	 * This represents the groupSN that is the winning hand, should not include winning Tile within
	 * Meaning the groupSN should be readied
	 */
	public String winningGroupSN_;
	
	/**
	 * This represents the winning Tile of the given winning groupSN
	 */
	public Tile winningTile_;

	/**
	 * This represents the winning Player within the Mahjong round
	 */
	public Player winningPlayer_;
	
	/**
	 * This boolean ensures a MJ hand that has won is saved to this instance (even if it's valid mahjong hand)
	 */
	public boolean validHand_ = false;
	
	/**
	 * Default constructor
	 */
	public Scoring()
	{
		winningGroupSN_ = "";
	}
	
	/**
	 * Mezen winningHand
	 * @param winningHand A valid groupSN that has to represent a winning hand in order to avoid errors
	 */
	public Scoring(String winningHand)
	{
		this.winningGroupSN_ = winningHand;
		this.validHand_ = true;
	}
	
	/**
	 * Accessor method of getting the winning groupSN
	 * @return The groupSN that is assigned to this instance of scoring
	 */
	public String getWinningGroupSN()
	{
		return this.winningGroupSN_;
	}
	
	/**
	 * Accessor method to see if this scoring instance is storing a valid hand
	 * @return Boolean value that represents if this groupSN is valid and winning
	 */
	public boolean getValidStatus()
	{
		return this.validHand_;
	}
	
	/**
	 * Mutator method to set a new groupSN
	 * @param newGroupSN A new string that represents a groupSN
	 */
	public void setWinningGroupSN(String newGroupSN)
	{
		this.winningGroupSN_ = newGroupSN;
	}
	
	/**
	 * Mutator method to set the status of this instance of scoring is storing a valid or winning groupSN
	 * @param newStatus The new status that would represent the by the groupSN
	 */
	private void setValidStatus(boolean newStatus)
	{
		this.validHand_ = newStatus;
	}
	
	/**
	 * @info Function checks for 
	 * - Tile is not discard and is drawn by who
	 * - If mahjong hand is concealed
	 * @return True if the hand satisfies the conditions of being completely concealed and tsumo, false otherwise
	 */
	public boolean mezenTsumo()
	{
		if(this.winningTile_.getDiscardType() > -1)
		{
			return false;
		}
		if(this.winningTile_.assigned_wind_ != this.winningPlayer_.getWind())
		{
			return false;
		}
		
		return true;
	}
	
	/**
	 * @info
	 * The groupSN should be uniquely returned from the listPairsSearch() function
	 * @return True if the hand satisfies the conditions of being 7 pairs, false if not
	 */
	public boolean chitoi()
	{
		
	}
}

package bot_package_v2;

import java.util.*;

import bot_package_v2.MJ_tools.*;

public class Scoring 
{
	public String winningGroupSN_;

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
}

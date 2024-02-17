package bot_package;

import java.util.*;


public class Prediction extends Scoring
{
	public int[] validPredictID = {1,2,3,4,5,13,14,15,16,17,18,19,20,24,25,26,27};
	private ArrayList<Group> potentialGroup;
	private ArrayList<Integer> orphansGroup;
	private ArrayList<Integer> pairsGroup;
	private ArrayList<Integer> playerHand;
	

	public Prediction(ArrayList<Integer> in_hand)
	{
		this.playerHand = new ArrayList<Integer>(in_hand);
	}
	public Prediction(Prediction clone)
	{
		this.potentialGroup = clone.potentialGroup;
		this.orphansGroup = clone.orphansGroup;
		this.pairsGroup = clone.pairsGroup;
		this.playerHand = clone.playerHand;
	}
	protected ArrayList<Group> getGroups()
	{
		return this.potentialGroup;
	}
	protected ArrayList<Integer> getOrphan()
	{
		return this.orphansGroup;
	}
	protected ArrayList<Integer> getPairs()
	{
		return this.pairsGroup;
	}
	protected ArrayList<Integer> getHand()
	{
		return this.playerHand;
	}
	
	public static void main(String[] args)
	{
		
	}
}

package bot_package_v2;

import java.util.*;

import bot_package_v2.MJ_tools.Tile;

/**
 * This class will be responsible for both Group and Groupless Mahjong hands 
 * in order for easier scoring algorithm
 */
public class Yaku 
{
	/**
	 * This field will be responsible for all possible melded Groups
	 * Each ArrayList<Group> tile would be assigned a String value that conveys data about the MJ hand
	 */
	protected HashMap<String,ArrayList<Group>> meldedGroups_;
	
	/**
	 * This field will be responsible for 7 pairs hands
	 */
	protected ArrayList<Tile> onlyPairs_;
	
	/**
	 * This field will be responsible for Kokushi hands
	 */
	protected ArrayList<Tile> kokushi_;
	
	private Group_Search searchAlgorithm_ = new Group_Search();
}

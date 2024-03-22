package bot_package;

import java.util.*;


/**
 * Game_system doesn't 
 */
public class MJ_game
{
	/**
	 * This ArrayList will keep track of all Players of this current MJ game
	 */
	protected HashMap<Integer, Player> all_players = new HashMap<Integer, Player>();
	
	/**
	 * Possible_tiles: 			used for the searching algorithm to see what tiles are left
	 * total_nonbonus_tiles: 	used to reference for how far the current game is progressing
	 */
	public ArrayList<ArrayList<Integer>> possible_tiles_ = new ArrayList<ArrayList<Integer>>();
	
	/*
	 * The int value that represents the remaining amount of tiles that does include other Player's hand
	 * When starting a game, this doesn't take into consideration 1st tile from dealer, 
	 * first tile will come as first Compress_input
	 * 
	 * Should not include flower tiles/ or any kind of bonus tile
	 */
	public int total_wall_tiles_;
	
	/*
	 * With each index that would represent the Player's wind_ID, the int value would indicate the amount
	 * of remaining called Group possible (4 == no declared Group)
	 */
	public int[] player_status_;
	
	/*
	 * The list of possible flowers that is either in other Player's hand or in the wall
	 * Each index represents the amount of flowers not visible
	 */
	public ArrayList<Integer> possible_flowers_; 

	/**
	 * This stores the move history of a given game
	 */
	public ArrayList<Compress_input> move_history_;
	
	/**
	 * This will be the counter responsible for whose turn it currently is
	 */
	public int wind_ID_turn_ = 0;
	
	/**
	 * Anything that goes wrong, or the game actually finishes, this will indicates these two cases
	 */
	public boolean complete_game = false;
	
	/**
	 * @function This constructor does not take into consideration the player's starting hand and their first declaration
	 * 			 of flowers and recovering tiles
	 * 
	 * @param wind_ID This input is the current Player that would input their hand while everyone else is unknown
	 * 
	 * This default constructor will start a new game, so all the fields would be set to a fresh wall
	 */
	public MJ_game(int wind_ID)
	{
		for(int i = 0; i < 4; i++) 
		{
			possible_tiles_.add(new ArrayList<Integer>());
			int max = 9;
			switch(i) //Used to add numbered suit and honor suit separately
			{
				case 3:
					max = 7;
				default:
					for(int j = 0; j < max; j++) possible_tiles_.get(i).add(4);
					break;
			}
			all_players.put(i, new Player(i, "", this));
			if(i == wind_ID)
			{
				all_players.put(i, new Player(i, "", this));
			}
		}
		this.total_wall_tiles_ = 136 - (4 * 13);							// 4(amt) * 34(IDs) == 136 - 4(Players) * 13(Start hand amt)
		for(int i = 0; i < 4; i++) this.possible_flowers_.add(2);
	}
	
	/**
	 * @todo
	 * Implement mid-game MJ_game in the future here
	 */
	
	/**
	 * 
	 * @return Every tile in a suit by suit ArrayList<Integer> format that are not visible
	 */
	public ArrayList<ArrayList<Integer>> get_possible_tiles(){return this.possible_tiles_;}
	
	
	/**
	 * 
	 * @return The total number of tiles left from THE WALL, not total visible tiles
	 */
	public int total_tiles_left() {return this.total_wall_tiles_;}
	
	
	/**
	 * 
	 * @return Returns a list of 4 elements representing each Player and the amount of calls they have
	 */
	public int[] get_player_status() {return this.player_status_;}
	
	
	/**
	 * 
	 * @return Returns a list of 4 elements representing the amount of flowers left for the index representation
	 * 			of that seasoned flower, i.e flower 1 == index 0; flower 4 == index 3
	 */
	public ArrayList<Integer> get_possible_flowers(){return this.possible_flowers_;}
	
	
	/**
	 * 
	 * @return Return a ArrayList of all the previous moves that occured during this MJ_game
	 */
	public ArrayList<Compress_input> get_move_history(){return this.move_history_;}
	
	
	/**
	 * 
	 * @return The current wind_id that is allowed to go (in other words, the integer that represent which Player's turn)
	 */
	public int get_current_windID_turn() {return this.wind_ID_turn_;}
	
	
	/**
	 * 
	 * @return A boolean that represents if the game is complete (true) or not (false)
	 */
	public boolean game_status(){return this.complete_game;}
	
	/**
	 * 
	 * @param new_turn The new turn that was just perform in order to update MJ_game in real time
	 * @return	True if the input was valid and was added properly, false otherwise and was not added
	 */
 	public boolean update_game(Compress_input new_turn)
	{
 		if(!new_turn.is_validMove()) {return false;}
 		
 		//Adds the valid turn into move_history
 		this.move_history_.add(new_turn);
 		
		//Removes input tile from wall
		int suit = new_turn.get_tileID() / 9;
		int index = new_turn.get_tileID() % 9;
		
		//No tiles to offer, no tiles remove, return false
		if(this.possible_tiles_.get(suit).get(index) == 0)
		{
			return false;
		}
		this.possible_tiles_.get(suit).set(index, this.possible_tiles_.get(suit).get(index) - 1);
		
		//Checks the same conditions for used_tiles
		for(int i = 0; i < new_turn.get_used_tiles().size(); i++)
		{
			suit = new_turn.get_used_tiles().get(i) / 9;
			index = new_turn.get_used_tiles().get(i) % 9;
			if(this.possible_tiles_.get(suit).get(index) == 0)
			{
				return false;
			}
			this.possible_tiles_.get(suit).set(index, this.possible_tiles_.get(suit).get(index) - 1);
		}
		
		// valid Move already checks if there are possible flowers, since true, it's safe to remove flowers
		for(int i = 0; i < new_turn.get_flower_list().size(); i++)
		{
			this.possible_flowers_.set(new_turn.get_flower_list().get(i), this.possible_flowers_.get(new_turn.get_flower_list().get(i)) - 1);
		}
		
		
		switch(new_turn.get_decision())
		{
			//All increment by 1 cases ([0,1] == drop + [2] == seq call)
			case 0:
			case 1:
			case 2:
				
				if(this.wind_ID_turn_ == 3)
				{
					this.wind_ID_turn_ = 0;
				}
				else
				{
					this.wind_ID_turn_++;
				}
				break;
				
			//All any but current Player cases ([3,4] = pon/kan calls)
			case 3:
			case 4:
				this.wind_ID_turn_ = new_turn.get_windID();
				break;
				
			//ron/tsumo is ALL Players
			case 7:
				//Get Player index -> Scoring.java
				this.complete_game = true;
				break;
		}
		return true;
	}
}

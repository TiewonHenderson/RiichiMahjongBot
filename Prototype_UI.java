package bot_package;

import java.util.*;
import java.io.*;

public class Prototype_UI
{
	/**
	 * The current MJ_Game this Prototype_UI is assigned to
	 */
	private MJ_game given_game_;
	
	/**
	 * The io object responsible for all input outputs
	 */
	private BufferedReader console_io_ = new BufferedReader(new InputStreamReader(System.in));
	
	/**
	 * A Player instance reference to this current User, instead of accessing through MJ_game
	 */
	private Player player_reference;
	
	/**
	 * The int value corresponding to this Player's MJ wind name by initial
	 */
	public int user_windID_;
	
	/**
	 * The String value that represents the User's inputed as desired for their name
	 */
	public String username_;
	
	/**
	 * Used to print out instructions before User's input
	 */
	final private static String[] direction_list = 
	{"Input your desired name:",
	 "Input your seatwind initial:",
	 "Input the number corresponding to game status:\n0) New Game; 1) Mid Game; 2) Complete game",
	 "Input your current hand (without flowers):",
	 "Input new move / wanted information:",
	 "INVALID INPUT, THREW IOEXCEPTION, INPUT AGAIN!!!\n\n"};
	
	public Prototype_UI(MJ_game current_game)
	{
		this.given_game_ = current_game; //Save reference to the game, not copy
		
		/*
		 * Adds the User's inputed name
		 */
		this.username_ = safe_console_input(direction_list[0]);

		/*
		 * Adds the seat_wind ID of the User's game
		 */
		String seat_wind_input = safe_console_input(direction_list[1]);
		if(seat_wind_input.length() == 1)
		{
			this.user_windID_ = MJ_game.element_translator.windchar_to_int(seat_wind_input.charAt(0));
		}

		/*
		 * Adds the User's game status
		 */
		int game_status = 0;
		switch(safe_console_input(direction_list[2]))
		{
			case "1":
				game_status = 1;
				break;
			case "2":
				game_status = 2;
				break;	
		}
	}
	
	/**
	 * 
	 * @param directions Specific to this class, uses direction_list to print out instructions
	 * @return The input String from User inputed from Console
	 */
	private String safe_console_input(String directions)
	{
		boolean proper_input = false;
		String return_str = "";
		while(!proper_input)
		{
			try
			{
				System.out.println(directions);
				return_str = console_io_.readLine();
				proper_input = true;
			}
			catch(IOException e)
			{
				System.out.println(direction_list[direction_list.length - 1]);
			}
		}
		return return_str;
	}
	
	
	
	/**
	 * 
	 * @return The username assigned by the user input
	 */
	public String get_username()
	{
		return this.username_;
	}
	
	/**
	 * 
	 * @return The user's wind_ID corresponding the the MJ wind_char
	 */
	public int get_windID()
	{
		return this.user_windID_;
	}
	
	public static void main(String[] args)
	{
	}
	
}

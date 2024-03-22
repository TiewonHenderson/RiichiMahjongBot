package bot_package;

import java.util.*;

public class Prototype_UI
{
	public MJ_game given_game_;
	
	public Prototype_UI()
	{
		Scanner this_player_input = new Scanner(System.in);
		
		/*
		 * Adds the User's inputed name
		 */
		System.out.println("Input your desired name: ");
		
		String given_name = this_player_input.nextLine();
		
		/*
		 * Adds the seat_wind ID of the User's game
		 */
		System.out.println("Input your seatwind initial: ");
		
		String seat_wind = this_player_input.nextLine();
		int seat_wind_id = 0;
		
		if(seat_wind.length() == 1)
		{
			if(Character.isAlphabetic(seat_wind.charAt(0)))
			{
				switch(seat_wind.charAt(0))
				{
					//EAST
					//seat_wind_id already == 0
						
					//SOUTH
					case 'S':
					case 's':
						seat_wind_id = 1;
						break;
						
					//WEST
					case 'W':
					case 'w':
						seat_wind_id = 2;
						break;
						
					//NORTH
					case 'N':
					case 'n':
						seat_wind_id = 3;
						break;
				}
			}
			else if(Character.isDigit(seat_wind.charAt(0)))
			{
				//Sets seat_wind_id if int wind_id is valid
				if(Character.getNumericValue(seat_wind.charAt(0)) < 0 || Character.getNumericValue(seat_wind.charAt(0)) > 3){}
				else{seat_wind_id = Character.getNumericValue(seat_wind.charAt(0));}
			}
		}
	
		/*
		 * Adds the User's game status
		 */
		System.out.println( "Input the number corresponding to game status:\n" + 
							"0) New Game; 1) Mid Game; 2) Complete game");
		
		int game_status = 0;
		
		try 
		{
			switch(this_player_input.nextInt())
			{
				case 1:
					game_status = 1;
					break;
				case 2:
					game_status = 2;
					break;	
			}
		}
		catch(Exception e)
		{
			System.out.println("Invalid input, result == 0) New Game");
		}
		
		this_player_input.close();
		
		this.given_game_ = new MJ_game();
	}
	public static void main(String[] args)
	{
	}
	
}

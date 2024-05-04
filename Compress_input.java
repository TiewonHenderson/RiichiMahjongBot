package bot_package;

import java.util.*;

import bot_package.Prediction.Numerical_tools;

public class Compress_input 
{
	public MJ_round current_game_;
	
	public Compress_input(MJ_round current_game)
	{
		this.current_game_ = current_game;
	}
	
	public static void main(String[] args)
	{
		MJ_round random_round = new MJ_round();
		random_round.set_game_mode(0);
		random_round.set_windID_turn(2);
		/*
		 * start 2 -> edqp32md6sdqc19mdq?3p1mdq+e
		 *		wind_id		cmd				call_type	tile	sequence left
		 *		2			tedashi			(-1)		East	ed qp32md6sdqc09mdq?3p1md4mdq+e
		 *		3			pon				(1)			East	qp3 2md6sdqc09mdq?3p1md4mdq+e
		 *		3			tedashi 		(-1)		2m		2md 6sdqc09mdq?3p1md4mdq+e
		 *		0			tedashi			(-1)		6s		6sd qc09mdq?3p1md4mdq+e
		 *		1 			chi				(0)			6s		qc1 9mdq?3p1md4mdq+e
		 *		1			tedashi			(-1)		9m		9md q?3p1md4mdq+e
		 *		2			conceal kan		(5)			3p		q?3p 1md4mdq+e
		 *		2			tedashi			(-1)		1m		1md q+e
		 *		3 			add kan			(4)			East	q+e
		 */
		Queue<Command> cmd_q = Console_io.inputed_move("edqp02md6sdqcl9mdq?3p1mdq+e5rpdqt3m",random_round);
		while(!cmd_q.isEmpty())
		{
			System.out.println("-----------------------------------");
			System.out.println(cmd_q.poll());
			System.out.println("-----------------------------------");
		}
	}
	public static class Console_io
	{
		public boolean scanner_closed = false;
		private Scanner console_scan_;
		private static final char[] honor_char = {'e','s','w','n','h','g','r'};
		private static final char[] call_char = {'c','p','k','r','+','?', 't'};
		
		/**
		 * Encapsulated Scanner for console input
		 */
		public Console_io()
		{
			console_scan_ = new Scanner(System.in);
		}
		
		/**
		 * 
		 * @info
		 * white dragon is always char == 'h'
		 * 
		 * The input string can either be formatted as: 
		 * mode 1: per move tiles
		 *	   tsumogiri 	== 1 copy of the symbol 	(i.e 6m [1 copy of 6])
		 *	   tedashi		== 2 copies of the symbol	(i.e 66m [2 copies of 6])
		 *	   Honors:
		 *	   input the letter representation of the tile,
		 *	   e = east tsumogiri
		 *	   w = west tsumogiri
		 * 
		 * Flowers are considered at the end of the game, do not input
		 * 
		 * mode 2: sequential amount of moves:
		 * 		tsumogiri 	== tile + "t"
		 * 		tedashi		== tile + "d"
		 * 
		 * mode 3: single calls
		 * - with "q" == somebody called
		 * - with type of call 
		 * 	prev_call_tile:
		 * 		"c" = chi, 
		 * 		"p" = pon, 
		 * 		"k" = "kan", 
		 * 		"r" = "ron", 
		 * 	self_declare_tiles:
		 * 		"?" = "concealed kan", 
		 * 		"+" = "added kan"
		 * 		"t" = "tsumo"
		 * 
		 * - with wind_id of player that called
		 * 
		 * i.e qp2 or qc1 (it will take the previous tile and consider that called)
		 * 			sequential call
		 * 
		 * A String sequence can look like this
		 * inputed wind_id = 2
		 * 3md7mt8pt{htqp1} takes previous discard as call
		 * 
		 * Special symbols (must be before 't'/'d'/'q'):
		 * red five	: single 5rm/55rm 	sequential 5rmt/5rmd
		 * riichi	: single 55m-		sequential 5mt-/5md-
		 * 
		 * special call: In hand kans
		 * concealed kan: (always from hand) single q?x(canto), q?5m(riichi) 	sequential xq?(canto), 5mq?(riichi)
		 * added kan:	  (always from hand) single q+5m						sequential 5mq+
		 * 
		 * @return A sequential String that represents
		 */
		public String console_move_input()
		{
			if(this.scanner_closed)
			{
				return "";
			}
			System.out.println("Input drop tile or sequence of drop tiles:");
			return console_universal_ret_str();
		}
		
		/**
		 * Prioritized indicators
		 * "m", "p", "s", "z" to add respective tile_id for the group/tiles
		 * ^^^ Refer to Group.suit_reference
		 *  "c", "k", "q", and "o" to determine which ArrayList to add into
		 *  
		 * @return a mj_str that is translatable using Player.convert_mjSTR()
		 */
		public String console_hand_input()
		{
			if(this.scanner_closed)
			{
				return "";
			}
			System.out.println("Type mahjong hand using standard mj_str format: ");
			String input = "";
			int has_indicators = 0;
			final char[] indicators = {'c', 'k', 'q', 'o'};
			while(has_indicators < 4)
			{
				has_indicators = 0;
				input = console_universal_ret_str();
				for(int i = 0; i < input.length(); i++)
				{
					if(input.charAt(i) == indicators[has_indicators])
					{
						has_indicators++;
					}
				}
			}
			return input;
		}
		
		/**
		 * 
		 * @return A two length String that represents index 0 == prevalent wind, index 1 == seat wind
		 */
		public String console_wind_input()
		{
			if(this.scanner_closed)
			{
				return "";
			}
			System.out.println("Type two characters, 1st char == prevalent wind, 2nd char == your seat wind: ");
			String input = "";
			int is_winds = 0;
			while(is_winds != 2)
			{
				is_winds = 0;
				input = console_universal_ret_str();
				for(int i = 0; i < input.length(); i++)
				{
					for(char wind: Group.wind_reference)
					{
						if(input.charAt(i) == wind)
						{
							is_winds++;
							break;
						}
					}
				}
			}
			return input;
		}
		
		/**
		 * 
		 * @return Any String inputed using Scanner
		 */
		public String console_universal_ret_str()
		{
			if(this.scanner_closed)
			{
				return "";
			}
			String input = "";
			while(input.isEmpty())
			{
				this.console_scan_ = new Scanner(System.in);
				input = this.console_scan_.nextLine();
			}
			return input;
		}
		
		public boolean close_Scanner()
		{
			this.console_scan_.close();
			this.scanner_closed = true;
			return this.scanner_closed;
		}
		
		/**
		 * @info
		 * The return ArrayList action_tile will have size 2 ArrayList<Double>
		 * index 0 = action performed (not action of discard type)
		 * action values:
		 * x.1 == wind id x chi (in reference to the current tile)
		 * x.2 == wind id x pon
		 * x.3 == wind id x kan 
		 * x.4 == wind id x ron
		 * x.5 == wind id x added kan
		 * x.6 == wind id x concealed kan
		 * 
		 * index 1 = tile_id + discard type
		 */
		public static Queue<Command> inputed_move(String console_input, MJ_round current_game)
		{
			Queue<Command> command_list = new LinkedList<Command>();
			if(console_input.isEmpty())
			{
				return command_list;
			}
			int input_type = -1; 	//See input_type enum for reference
			int call_type = -1;
			int player = -1;	//assigned to a valid wind_id if called by that person, -1 for no call
			int wind_id_turn = current_game.get_current_windID_turn();		//wind_id_turn doesn't ma
			char chi_dif = 'n';
			
			/*
			 * Single called tile
			 * concealed kan: (always from hand) single q?(canto), q?5m(riichi) 	sequential xq?(canto), 5mq?(riichi)
			 * added kan:	  (always from hand) single q+5m						sequential 5mq+
			 * else
			 * 		0 = "c" = chi, 
			 * 		1 = "p" = pon, 
			 * 		2 = "k" = "kan", 
			 * 		3 = "r" = "ron", 
			 * 		4 = "+" = "added kan",
			 * 		5 = "?" = "concealed kan", 
			 * - with wind_id of player that called
			 * 
			 * i.e qp2 or qk1 (it will take the previous tile and consider that called), 
			 * qc since only next player can chi, increment turn_wind_id, next index represents char of chi_dif
			 * let x = tile_id
			 * 'l' == x - 2, x - 1
			 * 'm' == x - 1, x + 1
			 * 'r' == x + 1, x + 2
			 * 
			 * 			sequential call
			 * 
			 * cases: q?(canto); q?5m(riichi); q+5m; qx5m
			 */
			if(console_input.charAt(0) == 'q')
			{
//				try
//				{
					//Checks the type of call this tile is
					for(int i = 0; i < call_char.length; i++)
					{
						if(call_char[i] == console_input.charAt(1)) 
						{
							call_type = i;
							switch(call_type)
							{
								case 0:
									input_type = 0;
									player = wind_id_turn + 1;
									wind_id_turn = player;
									if(player > 3) {player = 0;}
									if(console_input.length() > 2)
									{
										chi_dif = console_input.charAt(2);
									}
									else
									{
										chi_dif = 'm';
									}
									break;
								case 1:
								case 2:
								case 3:
									input_type = 0;
									if(console_input.length() > 2)
									{
										player = Character.getNumericValue(console_input.charAt(2));
									}
									else
									{
//										throw new Exception();	//no player given throw exception and input again
									}
									wind_id_turn = player;
									break;
								case 4:
									input_type = 1;
									player = wind_id_turn;
									break;
								case 5:
									input_type = 2;
									player = wind_id_turn;
									break;
								case 6:
									input_type = 4;
									player = wind_id_turn;
									break;
							}
							break;
						}
					}
					Command add_cmd = new Command(-1, input_type, false, player, call_type, chi_dif, current_game.game_mode_);
					if(current_game.game_mode_ == 0)
					{
						for(int i = console_input.length()-1; i >= 0; i--)
						{
							double tile_value = get_tile_value(console_input.substring(console_input.length()-1,console_input.length()));
							if(tile_value != -1)
							{
								add_cmd.set_tile_id((int)tile_value);
								break;
							}
						}
					}
					command_list.offer(add_cmd);
//				}
//				catch(Exception e) {return inputed_move(console_input(current_game), current_game);}
			}
			else
			{
				boolean is_sequantial = false;
				for(int i = 0; i < console_input.length(); i++) 
				{
					if(is_sequantial) {break;}
					switch(console_input.charAt(i))
					{
						case 't':
						case 'd':
							is_sequantial = true;
							break;
					}
				}
				/*
				 * 	Example sequential
				 * 	Canto Mahjong
				 * 	start 1 -> 3md7mt8pthtqp3q?2sd
				 * 
				 *		wind_id		cmd				call_type	tile	sequence left
				 *		1			tedashi 		(-1)		3m		3md 7mt8pthtqp3q?2sd
				 *		2			tsumogiri 		(-1)		7m		7mt 8pthtqp3q?2sd
				 *		3			tsumogiri		(-1)		8p		8pt htqp3q?2sd
				 *		0			tsumogiri  		(-1)		wh		ht qp3q?2sd
				 *		3			pon				(1)			wh		qp3 q?2sd
				 *		3			conceal kan		(5)			X		q? 2sd
				 *		3			tedashi			(-1)		2s		2sd
				 *	
				 *	Riichi Mahjong
				 *	start 2 -> edqp32md6sdqcl9mdq?3p1mdq+e
				 *		wind_id		cmd				call_type	tile	sequence left
				 *		2			tedashi			(-1)		27		ed qp32md6sdqc09mdq?3p1md4mdq+e
				 *		3			pon				(1)			27		qp3 2md6sdqc09mdq?3p1md4mdq+e
				 *		3			tedashi 		(-1)		1		2md 6sdqc09mdq?3p1md4mdq+e
				 *		0			tedashi			(-1)		23		6sd qc09mdq?3p1md4mdq+e
				 *		1 			chi				(0)			23		qcl 9mdq?3p1md4mdq+e
				 *		1			tedashi			(-1)		8		9md q?3p1md4mdq+e
				 *		2			conceal kan		(5)			11		q?3p 1md4mdq+e
				 *		2			tedashi			(-1)		0		1md q+e
				 *		3 			add kan			(4)			27		q+e
				 *		
				 */
//				try
//				{
					if(is_sequantial)
					{
						String temp_input_collecter = "";
						double tile_val = 0.0;
						int input_cmd_id = -1;
						call_type = -1;
						Command add_cmd = null;
						for(int i = 0; i < console_input.length(); i++)
						{
							switch(console_input.charAt(i))
							{
								case 'd':
									tile_val = 0.5;
								case 't':
									input_cmd_id = 3;
									break;
								case 'q':
									if(i + 1 >= console_input.length()) {break;}
									/*
									 *	0 = "c" = chi, 
									 * 	1 = "p" = pon, 
									 * 	2 = "k" = "kan", 
									 * 	3 = "r" = "ron", 
									 * 	4 = "+" = "added kan",
									 * 	5 = "?" = "concealed kan", 
									 */
									for(int j = 0; j < call_char.length; j++)
									{
										if(console_input.charAt(i + 1) == call_char[j]) //CHI doesn't need wind_id_turn increment, drop already increment
										{
											call_type = j;
											switch(call_type)
											{
												case 0:
													if(i + 2 >= console_input.length() && Character.isDigit(console_input.charAt(i+2)))
													{
	//													throw new Exception();	//no player given throw exception and input again
													}
													chi_dif = console_input.charAt(i + 2);
													player = wind_id_turn;
													input_cmd_id = 1;
													i += 2;
													break;
												case 1:
												case 2:
												case 3:
													if(i + 2 >= console_input.length() && Character.isDigit(console_input.charAt(i+2)))
													{
//														throw new Exception();	//no player given throw exception and input again
													}
													player = Character.getNumericValue(console_input.charAt(i + 2));
													wind_id_turn = player;
													input_cmd_id = 1;
													i += 2;
													break;
												case 5:
													if(current_game.game_mode_ != 0)
													{
														input_cmd_id = 2;
														tile_val = -1;
														break;
													}
												case 6:
												case 4:
													input_cmd_id = 2;
													String temp_string = "";
													for(int k = i + 2; k < console_input.length(); k++)
													{
														temp_string += console_input.charAt(k);
														tile_val = get_tile_value(temp_string);
														if(tile_val != -1)
														{
															i = k;
															break;
														}
													}
													break;
											}
											break;
										}
									}
									break;
								default:
									temp_input_collecter += console_input.charAt(i);
									continue;
							}
//							System.out.println("\ninput_cmd_id: " + input_cmd_id);
//							System.out.println("tile_val: " + (int)tile_val);
//							System.out.println("call_type: " + call_type + "\n");
							switch(input_cmd_id)
							{
								case 1:
									add_cmd = new Command(-1, 0, false, player, call_type, chi_dif, current_game.game_mode_);
									break;
								case 2:
									int input_type_index = 1;
									if(call_type == 5) {input_type_index = 2;}
									if(call_type == 6)
									{
										add_cmd = new Command((int)tile_val, 4, false, wind_id_turn, 4, chi_dif, current_game.game_mode_);
									}
									else if(current_game.game_mode_ == 0 || call_type == 4)
									{
//										System.out.println((int)tile_val);
										add_cmd = new Command((int)tile_val, input_type_index, false, wind_id_turn, 4, chi_dif, current_game.game_mode_);
									}
									else if(call_type == 5)
									{
										add_cmd = new Command(-1, input_type_index, false, wind_id_turn, 4, chi_dif, current_game.game_mode_);
									}
									break;
								case 3:
									tile_val += get_tile_value(temp_input_collecter);
									add_cmd = new Command((int)tile_val, 3, Numerical_tools.is_tedashi(tile_val), wind_id_turn, -1, chi_dif, current_game.game_mode_);
									if(current_game.game_mode_ == 0)
									{
										add_cmd.set_red_5(Numerical_tools.is_red_5(tile_val));
										add_cmd.set_riichi(Numerical_tools.is_riichi(tile_val));
										add_cmd.set_game_mode(0);
									}
									else
									{
										add_cmd.set_game_mode(1);
									}
									wind_id_turn++;
									if(wind_id_turn > 3) {wind_id_turn = 0;}
									break;
							}
							if(add_cmd != null)
							{
								command_list.offer(add_cmd);
							}
							input_cmd_id = -1;
							tile_val = 0.0;
							temp_input_collecter = "";
							call_type = -1;
							add_cmd = null;
							chi_dif = 'n';
						}
					}
					else				//Single input always drop
					{
						double tile_val = get_tile_value(console_input);
						
						if(tile_val != -1)
						{
							Command add_cmd = new Command((int)tile_val, 3, Numerical_tools.is_tedashi(tile_val), wind_id_turn, -1, 'n', current_game.game_mode_);
							if(current_game.game_mode_ == 0)
							{
								add_cmd.set_red_5(Numerical_tools.is_red_5(tile_val));
								add_cmd.set_riichi(Numerical_tools.is_riichi(tile_val));
								add_cmd.set_game_mode(0);
							}
							else
							{
								add_cmd.set_game_mode(1);
							}
							command_list.offer(add_cmd);
							wind_id_turn++; if(wind_id_turn > 3) {wind_id_turn = 0;}
						}
					}
//				}
//				catch(Exception e) {return inputed_move(console_input(current_game), current_game);}
			}
			return command_list;
		}

		/**
		 * 
		 * @param input A short string that can represent a tile in String format (do not include 't' or 'd')
		 * @return A double representing the tile_id, tedashi/tsumogiri, red_5 or riichi
		 */
		public static double get_tile_value(String input)
		{
			if(input.length() == 0)
			{
				return -1;
			}
			boolean riichi = false;
			double ret_value = -1.0;
			String temp_input = new String(input);
			if(temp_input.charAt(temp_input.length() - 1) == '-')
			{
				riichi = true;
				temp_input = temp_input.substring(0,temp_input.length() - 1);
			}
			if(temp_input.length() >= 2 && temp_input.substring(0, 2).compareTo("wh") == 0) 
			{
				temp_input = temp_input.substring(1);
			}
			try
			{
				switch(temp_input.length())
				{
					/*
					 * Cases:
					 * - tsumogiri honor
					 */
					case 1:
						for(int i = 0; i < honor_char.length; i++)
						{
							if(temp_input.charAt(0) == honor_char[i])
							{
								ret_value = i + 27.0;
							}
						}
						break;
					/*
					 * Cases:
					 * - tedashi honor
					 * - number tile
					 */
					case 2:
							if(Character.isDigit(temp_input.charAt(0)))
							{
								int suit = -1;
								for(int i = 0; i < Group.suit_reference.length; i++)
									if(temp_input.charAt(1) == Group.suit_reference[i]) {suit = i; break;}
								ret_value = ((double)Character.getNumericValue(temp_input.charAt(0)) - 1) + (suit * 9.0);
							}
							else if(temp_input.charAt(0) == temp_input.charAt(1))
							{
								for(int i = 0; i < honor_char.length; i++)
								{
									if(temp_input.charAt(0) == honor_char[i])
									{
										ret_value = i + 27.0 + 0.5;
									}
								}
							}
							break;
					/*
					 * Cases:
					 * - number tedashi attempt
					 * - red 5
					 */
					case 3:
						if(temp_input.charAt(0) == temp_input.charAt(1))
						{
								int suit = -1;
								for(int i = 0; i < Group.suit_reference.length; i++)
									if(temp_input.charAt(2) == Group.suit_reference[i]) {suit = i; break;}
								ret_value = (Character.getNumericValue(temp_input.charAt(0)) - 1) + (suit * 9) + 0.5;
						}
						else if(temp_input.charAt(0) == '5' && temp_input.charAt(1) == 'r')
						{
							int suit = -1;
							for(int i = 0; i < Group.suit_reference.length; i++)
								if(temp_input.charAt(2) == Group.suit_reference[i]) {suit = i; break;}
							ret_value = 4.0 + (suit * 9) + 0.005;
						}
						break;
					/*
					 * Case:
					 * - red 5 tedashi attempt
					 */
					case 4:
						if(temp_input.charAt(0) == temp_input.charAt(1) && temp_input.charAt(2) == 'r')
						{
								int suit = -1;
								for(int i = 0; i < Group.suit_reference.length; i++)
									if(temp_input.charAt(3) == Group.suit_reference[i]) {suit = i; break;}
								ret_value = 4.0 + (suit * 9) + 0.505;
						}
						break;
					}
				}
				catch(Exception e) {return -1.0;}
				if(riichi)
				{
					ret_value += 0.0001;
				}
				return ret_value;
			}
	}
	/**
	 * This acts as a command queue that will run:
	 * Command = 1 STEP ACTION PER POSSIBLE TURN
	 * 1.e a drop and call can happen at the same unique Player's turn
	 * but a drop and call would be it's own Command
	 * Command 1 = id3 tedashi 6m
	 * Command 2 = id1 pon 6m
	 * Command 3 = id1 tedashi 9m
	 * 
	 * Special cases: added kan and concealed kan
	 * riichi mahjong:
	 * Command 1 = id3 + 6m			(dropper_wind_id_ = 3, caller_wind_id_ = 3, call_type_ = 4
	 * Command 2 = id3 tedashi 4m
	 */
	public static class Command
	{
		enum input_type
		{
			CALLED_PREVIOUS,	//Take previous Command's tile and use that as called tile, save wind_id_turn to person who called
			ADDED_QUAD,			//Added tile is saved to same Command as declared "add kan", wind_id_turn remains the same unless someone robs kan
			/**
			 * Canto: 	tile_id_ is labeled as -1(unknown)
			 * Riichi: 	tile_id_ is set to visible tile_id
			 * wind_id_turn remains the same unless someone robs kan with kokushi
			 */
			CONCEALED_QUAD,
			DROP,				//Increment/loop wind_id_turn
			TSUMO
		}
		
		/**
		 * A integer ID that represents call type:
		 * 	0 = chi, 
		 * 	1 = pon, 
		 * 	2 = "kan", 
		 * 	3 = "ron", 
		 * 	4 = none
		 */
		enum prev_call_type
		{
			CHI,
			PON,
			CALL_KAN,
			RON,
			NONE
		}
		/**
		 * Range: [0,33] represents the id of the tile
		 */
		public int tile_id_ = -1;
		
		/**
		 * represents false = tsumogiri, true = tedashi
		 */
		public boolean tedashi_;	

		/**
		 * The call_type of this command if applicable
		 */
		public prev_call_type prev_call_type_ = prev_call_type.NONE;
		
		/**
		 * The wind_id that performed this action
		 */
		public int player_wind_id_;
		
		/**
		 * Determine what call type this command is
		 */
		public input_type input_ = input_type.DROP;
		
		/**
		 * If this is a red_5, set this to true
		 */
		public boolean red_5_ = false;
		
		/**
		 * If the discarded Player called riichi on this tile, set this to true
		 */
		public boolean riichi_ = false;
		
		/**
		 * Instead of inputing the MJ_game as a parameter, this will auto assign if there are inputed details of red_5 or riichi_
		 * 0 = Riichi Mahjong
		 * 1 = Canto Mahjong
		 */
		public int game_mode_ = -1;
		
		/**
		 * Instead of adding two extra tiles, this will extend towards the chi_dif
		 * i.e if:
		 * chi_dif == -1, 	its (x-2)	(x-1)	x
		 * chi_dif == 0, 	its (x-1)	x		(x+2)
		 * chi_dif == +1, 	its x		(x+1)	(x-2)
		 */
		public int chi_dif_;
		
		/**
		 * Null command, default constuctor
		 */
		public Command()
		{
			
		}
		
		
		/**
		 * Non-Riichi Mahjong Command constructor
		 * @param tile_id 		refers to the tile_id in nums to TileVal			(Set -1 if input_id == 0 or input_id == 2 && MJ_game.game_mode_ == 1)
		 * @param input_id		refers to the input command of the console input, this reflects to enum input_type values index
		 * @param tedashi 		false == tsumogiri, true = tedashi
		 * @param player_id		the wind_id of the Player that perform this action
		 * @param call_type_id	the type of call of this tile that is represented by the call_type ID
		 * @param chi_dif		the direction the chi goes depending on the tile_id	(inputs == l,m,r)
		 * @param game_mode		the variant of mahjong that is current happening
		 */
		public Command(int tile_id, int input_id, boolean tedashi, int player_id, int call_type_id, char chi_dif, int game_mode)
		{
			this.tedashi_ = tedashi;
			this.tile_id_ = tile_id;
			switch(input_id)
			{
				case 4:
				case 1:
					this.tile_id_ = tile_id;
				case 2:
					if(game_mode == 0)
					{
						this.tile_id_ = tile_id;
					}
				case 0:
					this.game_mode_ = game_mode;
					if(call_type_id > 3)
					{
						this.prev_call_type_ = prev_call_type.values()[4];
					}
					else
					{
						this.prev_call_type_ = prev_call_type.values()[call_type_id];
					}
					this.input_ = input_type.values()[input_id];
					break;
			}
			if(call_type_id == 0 && chi_dif != 'n')
			{
				final char[] chi_char = {'l', 'm','r'};
				for(int i = 0; i < chi_char.length; i++)
				{
					if(chi_dif == chi_char[i])
					{
						this.chi_dif_ = i - 1;
						break;
					}
				}
			}
			else
			{
				this.chi_dif_ = -9;
			}
			this.player_wind_id_ = player_id;
			
			this.game_mode_ = 1;
		}
		
		/**
		 * Sequential call/declare Command constructor -> Riichi Mahjong
		 * @param tile_id 		refers to the tile value, with include decimals to indi
		 * @param tedashi 		false == tsumogiri, true = tedashi
		 * @param player_id		the wind_id of the Player that perform this action
		 * @param call_type_id	the type of call of this tile that is represented by the call_type ID
		 * @param chi_dif		the direction the chi goes depending on the tile_id	(inputs == l,m,r)
		 * @param red_five		if this tile is considered in riichi mahjong a red dora 5
		 * @param riichi		if the Player declared riichi on this specific tile (doesn't matter if called)
		 * @param game_mode		the variant of mahjong that is current happening
		 */
		public Command(int tile_id, int input_id, boolean tedashi, int player_id, int call_type_id, char chi_dif, boolean red_five, boolean riichi, int game_mode)
		{
			this.tedashi_ = tedashi;
			this.tile_id_ = tile_id;
			switch(input_id)
			{
				case 4:
				case 1:
					this.tile_id_ = tile_id;
				case 2:
					if(game_mode == 0)
					{
						this.tile_id_ = tile_id;
					}
				case 0:
					this.game_mode_ = game_mode;
					if(call_type_id > 3)
					{
						this.prev_call_type_ = prev_call_type.values()[4];
					}
					else
					{
						this.prev_call_type_ = prev_call_type.values()[call_type_id];
					}
					this.input_ = input_type.values()[input_id];
					break;
			}
			if(call_type_id == 0 && chi_dif != 'n')
			{
				final char[] chi_char = {'l', 'm','r'};
				for(int i = 0; i < chi_char.length; i++)
				{
					if(chi_dif == chi_char[i])
					{
						this.chi_dif_ = i;
						break;
					}
				}
			}
			else
			{
				this.chi_dif_ = -9;
			}
			this.player_wind_id_ = player_id;
			
			this.game_mode_ = 0;
			this.red_5_ = red_five;
			this.riichi_ = riichi;
		}
		
		/**
		 * Clone constructor
		 * @param clone A Command to clone
		 */
		public Command(Command clone)
		{
			this.prev_call_type_ = clone.prev_call_type_;
			this.chi_dif_ = clone.chi_dif_;
			this.game_mode_ = clone.game_mode_;
			this.input_ = clone.input_;
			this.player_wind_id_ = clone.player_wind_id_;
			this.red_5_ = clone.red_5_;
			this.riichi_ = clone.riichi_;
			this.tedashi_ = clone.tedashi_;
			this.tile_id_ = clone.tile_id_;
		}
		
		public boolean set_tile_id(int tile_id)
		{
			if(tile_id < 0 || tile_id > 33) {return false;}
			this.tile_id_ = tile_id;
			return true;
		}
		public void set_tedashi(boolean tedashi)
		{
			this.tedashi_ = tedashi;
		}
		public boolean set_call_type(int call_type_id)
		{
			if(call_type_id < 0 || call_type_id > 4) {return false;}
			this.prev_call_type_ = prev_call_type.values()[call_type_id];
			return true;
		}
		public boolean set_player_wind_id(int wind_id)
		{
			if(wind_id < 0 || wind_id > 3) {return false;}
			this.player_wind_id_ = wind_id;
			return true;
		}
		public boolean set_input_type(int type_id)
		{
			if(type_id < 0 || type_id > 4) {return false;}
			this.input_ = input_type.values()[type_id];
			return true;
		}
		public void set_red_5(boolean is_red_5)
		{
			this.red_5_ = is_red_5;
		}
		public void set_riichi(boolean is_riichi_tile)
		{
			this.riichi_ = is_riichi_tile;
		}
		/**
		 * 
		 * @param game_mode either 0 == riichi mahjong, 1 == canto mahjong
		 * @return true if set properly, false if not valid input and not been set
		 */
		public boolean set_game_mode(int game_mode)
		{
			if(game_mode < 0 || game_mode > 1) {return false;}
			this.game_mode_ = game_mode;
			return true;
		}
		public String toString()
		{
			String info = "Tile_ID: " + this.tile_id_ + 
						  "\nTedashi: " + this.tedashi_ + 
						  "\nCall_type: " + this.prev_call_type_ + 
						  "\nChi_shape: " + this.chi_dif_ + 
						  "\nPlayer_wind_ID: " + this.player_wind_id_ +
						  "\nInput_CMD_Type: " + this.input_ +
						  "\nIs red 5: " + this.red_5_ +
						  "\nIs riichi tile: " + this.riichi_;
			return info;
		}
	}
}

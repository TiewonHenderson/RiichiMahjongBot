package bot_package_v2;

import java.util.*;
import java.util.function.Function;

import bot_package_v2.Group.*;
import bot_package_v2.Group_Search.*;
import bot_package_v2.MJ_tools.*;
import bot_package_v2.Player.*;

public class Test_class 
{
	//random instance of Tile to test Tile algorithms
	public ArrayList<Tile> randomTile_ = new ArrayList<Tile>();
	
	//random instance of Group to test Group's algorithm
	public ArrayList<Group> randomGroup_ = new ArrayList<Group>();
	
	//random instance of a Mahjong hand (Tiles only)
	public ArrayList<ArrayList<Tile>> randomMJHand_ = new ArrayList<ArrayList<Tile>>();
	
	//random instance of a Player's hand that is represented in game
	public ArrayList<Visible_hand> randomPlayerHand_ = new ArrayList<Visible_hand>();
	
	//random instance of opponent's hand in game
	public ArrayList<Mahjong_hand> randomOpponentHand_ = new ArrayList<Mahjong_hand>();
	
	/*
	 * Default constructor
	 * Creates a default 100 different instances of simple classes (i.e Tile, Group)
	 * Creates a default 10 different instances of complex classes (i.e AL<Tile>, Mahjong_hand)
	 */
	public Test_class()
	{
		for(int i = 0; i < 100; i++)
		{
			this.randomTile_.add(createRandomTile(-1,-1,-1,-1,null));
			this.randomGroup_.add(createRandomGroup(-1,-1,-1,-1,-1));
		}
	}
	
	/**
	 * Parameterized constructor to specify amount of test instances per field
	 * @param tileAmt amount of random Tiles to test with
	 * @param groupAmt amount of random Groups to test with
	 * @param mjHandAmt amount of random ArrayList<Tile> represented Hands to test with
	 * @param visHandAmt amount of random Visible_hand instances to test with
	 * @param oppHandAmt amount of random Mahjong_hand (typically opponents) to test with
	 */
	public Test_class(int tileAmt, int groupAmt, int mjHandAmt, int visHandAmt, int oppHandAmt)
	{
		
	}
	
	/**
	 * Accessor methods to get all random Tiles
	 * @return an ArrayList<Tile> with generated Tiles
	 */
	public ArrayList<Tile> getAllTiles()
	{
		return this.randomTile_;
	}
	
	/**
	 * Access methods to get all random Groups
	 * @return an ArrayList<Group> with generated Groups
	 */
	public ArrayList<Group> getAllGroups()
	{
		return this.randomGroup_;
	}
	
	/**
	 * Access methods to get all random mahjong hands
	 * @return an ArrayList<ArrayList<Tile>> with generated mahjong hand
	 */
	public ArrayList<ArrayList<Tile>> getAllMJHands()
	{
		return this.randomMJHand_;
	}
	
	/**
	 * Access methods to get all random Visible mahjong hands
	 * @return an ArrayList<Visible_hand> with generated Visible mahjong hands
	 */
	public ArrayList<Visible_hand> getAllUserHands()
	{
		return this.randomPlayerHand_;
	}
	
	/**
	 * Access methods to get all random opponents mahjong hands
	 * @return an ArrayList<Mahjong_hand> with generated opponents mahjong hands
	 */
	public ArrayList<Mahjong_hand> getAllOpponentHands()
	{
		return this.randomOpponentHand_;
	}
	/**
	 * 
	 * @param suit What suit the random generated Tile should be in (-1 for any)
	 * @param tileType Describes what the Tile is categorized as in 
	 * 				   terms of 1) terminals, 2) edge simples (2,3,7,8), 3) middle simples (4,5,6) (-1 for any, doesn't apply to honors)
	 * @param discardType Describes the discard type of the Tile, 
	 * 					  0 = non_discard
	 * 					  1 = tsumogiri
	 * 					  2 = tedashi
	 * 					+10 = riichi (-1 for any)
	 * @param windID the Integer representation of the seat wind Player (-1 for any)
	 * @param unwantedTiles An Array that represents Tiles that should not be generated again
	 * @return A randomly generated Tile with the specific characteristics with the parameters given
	 */
	public static Tile createRandomTile(int suit, int tileType, int discardType, int windID, Set<Integer> unwantedTiles) throws IllegalArgumentException
	{
		/*
		 * This lambda will return the distance the tileID is to the Tile's play value to 5 within the same suit, 
		 * does not apply to honors
		 */
		 Function<Integer, Integer> distanceToMiddle = tileID -> 
		 {
			if(tileID > 26) {return -1;}
			//Equation: 5 - (ID - suitIncrement) *represents Tile play value*
			return Math.abs(5 - (tileID - (tileID/9)*9));
		 };
		
		//Generates random tileID
		int tileID;
		ArrayList<Integer> wantedTileID = new ArrayList<Integer>();
		for(int i = 0; i < 34; i++)
		{
			//unwantedTiles first filter
			if(unwantedTiles != null && !unwantedTiles.isEmpty() && unwantedTiles.contains(i)) continue;
			
			//suit filter
			if(suit != -1 && i/9 != suit) continue;
			
			//Tile type filter
			boolean isWantedType = false;
			int distanceValue = distanceToMiddle.apply(i);
			if(distanceValue == -1) {isWantedType = true;} //tileType doesn't apply to honors, honors output -1
			if(tileType != -1 && i/9 < 3)
			{
				switch(tileType)
				{
					case 1:
						if(distanceValue == 4) isWantedType = true;
						break;
					case 2: 
						if(distanceValue == 3 || distanceValue == 2) isWantedType = true;
						break;
					case 3: 
						if(distanceValue <= 1) isWantedType = true;
						break;
				}
				if(!isWantedType) continue;
			}
			wantedTileID.add(i);
		}
		if(wantedTileID.isEmpty())
		{
			throw new IllegalArgumentException("All Tile ID is unwanted, cannot return createRandomTile");
		}
		tileID = wantedTileID.get((int)(Math.random() * wantedTileID.size())); 
		
		//Generates random discard Type (valid input is offsetted by 1)
		int discTypeID = discardType - 1; 
		if(discardType == -1)
		{
			discTypeID = (int)(Math.random() * 3) - 1;
			if(Math.random() > 0.5) discTypeID += 10;
		}
		
		//Generates random Wind enum
		Wind seatWind;
		if(windID < 0 || windID > 3)
		{
			seatWind = Wind.values()[(int)(Math.random() * 4)];
		}
		else
		{
			seatWind = Wind.values()[windID];
		}
		
		return new Tile(tileID, seatWind, false, discTypeID);
	}
	
	/**
	 * 
	 * @param suit What suit the random generated Group should be in (-1 for any)
	 * @param tileType This corresponds with createRandomTile's tileType, where the Group must contain at least one
	 * 				   of the Tiles with that Type (-1 for no restriction, does not apply to honors)
	 * @param groupType This corresponds to the Group type this random group must be (offset = -2)
	 * 					0 = floating
	 * 					1 = incomplete sequence
	 * 					2 = pair
	 * 					3 = sequence
	 * 					4 = triplet
	 * 					5 = quad  (-1 for any)
	 * @param meldStatus This includes if the Group is declared and if Group is concealed
	 * 					 0 = !declared && concealed
	 * 					 1 = declared && concealed *groupType will default to 5*
	 * 					 2 = declared && !concealed  (-1 for any)
	 * @param windID The windID this Group would belong to (-1 for any)
	 * @return A randomly generated Group with characteristic customized by the parameters
	 */
	public static Group createRandomGroup(int suit, int tilesType, int groupType, int meldStatus, int windID) throws IllegalArgumentException
	{
		//Honors cannot make sequences, throws error if asked
		if(suit == 3 && (meldStatus == 1 || meldStatus == 3))
		{
			throw new IllegalArgumentException("Cannot make sequence with honor suit");
		}
		
		//Init windID this Group belongs to
		Wind seatWind;
		if(windID < 0 || windID > 4)
		{
			seatWind = Wind.values()[(int)(Math.random() * 4)];
		}
		else 
		{
			seatWind = Wind.values()[windID];
		}
		
		ArrayList<Tile> groupTiles = new ArrayList<Tile>();
		Tile pivotTile = createRandomTile(suit, tilesType, 0, seatWind.ordinal(), null);
		
		/*
		 * Init meld status 
		 * index 0 = declared status
		 * index 1 = concealed status
		 */
		boolean[] final_meldStatuses = {false, true}; //default to meldStatus being 0
		int meldID = meldStatus;
		if(meldID < 0 || meldID > 2)
		{
			meldID = (int)(Math.random() * 3);
		}
		switch(meldID)
		{
			case 2:
				final_meldStatuses[1] = false;
			case 1:
				final_meldStatuses[0] = true;
				break;
		}
		
		//Init groupID that corresponds to a valid group status
		int groupID = groupType;
		if(pivotTile.getTileInfo()[0] < 3)
		{
			if(groupID < 0 || groupID > 5)
			{
				groupID = (int)(Math.random() * 6);
			}
		}
		else //Honor suit cannot make sequences
		{
			if(groupID < 0 || groupID > 5)
			{
				final int[] groupTypes = {0,2,4,5};
				groupID = groupTypes[(int)(Math.random() * 4)];
			}
		}
		
		//Switch case to initialize which type of Group
		final int[] tileOffset = {-2,-1,1,2}; //talking Tiles offset that can make sequences
		switch(groupID)
		{
			case 5:		//quad
				groupTiles.add(new Tile(pivotTile));
			case 4:		//triplet
				groupTiles.add(new Tile(pivotTile));
			case 2:		//pair
				groupTiles.add(new Tile(pivotTile));
				break;
						//floating/single Tile (Tile gets added in the end anyways)
				
			case 1:		//incomplete Sequence
				ArrayList<Integer> validOffsets = new ArrayList<Integer>();
				for(int i = 0; i < tileOffset.length; i++)
				{
					if((pivotTile.getTileID() + tileOffset[i])/9 == pivotTile.getTileInfo()[0])
					{
						validOffsets.add(tileOffset[i]);
					}
				}
				if(validOffsets.size() == 0) throw new IllegalArgumentException("Couldn't make sequence shape with given Tile ID");
				int talkingTile = pivotTile.getTileID() + validOffsets.get((int)(Math.random() * validOffsets.size()));
				groupTiles.add(new Tile(talkingTile, seatWind, false, -1));
				break;
			case 3:		//complete sequence
				final int[][] completeSeqOffsets = {{-2,-1},{-1,1},{1,2}};		//potential offsets
				ArrayList<int[]> validCompleteOffsets = new ArrayList<int[]>();
				for(int i = 0; i < completeSeqOffsets.length; i++)
				{
					boolean suited = true;
					for(int offsets: completeSeqOffsets[i])
					{
						//Checks if adding offset will still remain in same suit
						if((pivotTile.getTileID() + offsets)/9 != pivotTile.getTileInfo()[0])
						{
							suited = false;
							break;
						}
					}
					//saves if all offsets within inside Array if they are all within suit
					if(!suited) 
					{
						continue;
					}
					validCompleteOffsets.add( completeSeqOffsets[i]);
				}
				if(validCompleteOffsets.size() == 0) 
				{
					throw new IllegalArgumentException("Couldn't make complete sequence shape with given Tile ID");
				}
				for(int offsets: validCompleteOffsets.get((int)(Math.random()*validCompleteOffsets.size())))
				{
					groupTiles.add(new Tile(pivotTile.getTileID() + offsets, seatWind, false, -1));
				}
				break;
		}
		groupTiles.add(new Tile(pivotTile));
		
		return new Group(groupTiles, final_meldStatuses[0], final_meldStatuses[1]);
	}
	
	/**
	 * 
	 * @param singleSuit Indicates whether this hand should be completely single suited (-1 for allsuits)
	 * @param windID Indicates who's Player this hand belongs to by their windID (-1 for any single windID)
	 * @param declaredGroups Indicates the amount of Groups that were declared, 
	 * 						 the ArrayList size will adjust accordingly (-1 for random amount)
	 * @return An ArrayList<Tile> that are randomly generated with characteristics being inputed by parameter
	 */
	public static ArrayList<Tile> createRandomMJHand(int singleSuit, int windID, int declaredGroups)
	{
		ArrayList<Tile> returnMJHand = new ArrayList<Tile>();
		
		//Generates random Wind enum
		int seatWindID = windID;
		if(seatWindID < 0 || seatWindID > 3)
		{
			seatWindID = (int)(Math.random() * 4);
		}
		
		if(singleSuit >= 0 && singleSuit <= 3)
		{
			for(int i = 0; i < 13 - (declaredGroups * 3); i++)
			{
				returnMJHand.add(createRandomTile(singleSuit, -1, 0, seatWindID, null));
			}
		}
		else
		{
			for(int i = 0; i < 13 - (declaredGroups * 3); i++)
			{
				returnMJHand.add(createRandomTile(-1, -1, 0, seatWindID, null));
			}
		}
		return returnMJHand;
	}
	public static void main(String[] args)
	{
		Test_class x = new Test_class();
		int newLine = 0;
		for(Tile tile: x.getAllTiles()) 
		{
			if(newLine % 15 == 0)
			{
				System.out.println(tile);
			}
			else
			{
				System.out.print(tile + " ");
			}
			newLine++;
		}
	}
}

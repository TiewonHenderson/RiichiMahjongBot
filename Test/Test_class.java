package bot_package.Test;

import java.util.*;

import bot_package.Actual_Gameplay.Group;
import bot_package.Actual_Gameplay.Player.PlayerHand;

public class Test_class 
{
	public static ArrayList<Double> create_rand_droppile(int layers, boolean inc_tedashi)
	{
		ArrayList<Double> return_drop = new ArrayList<Double>();
		int layer_amt = layers;
		int[] tile_amt = new int[34]; for(int i = 0; i < tile_amt.length; i++) tile_amt[i] = 4;
		double random_tile = (int)(Math.random() * 34);
		for(int i = 0; i < layer_amt * 6; i++)
		{
			while(tile_amt[(int)random_tile] < 0) {random_tile = (int)(Math.random() * 34);}
			if(Math.random() > 0.8 && inc_tedashi) {random_tile += 0.5;}
			return_drop.add(random_tile);
			random_tile = (int)(Math.random() * 34);
		}
		return return_drop;
	}
	
	public static ArrayList<Double> create_rand_wholeDroppile(int layers, boolean inc_tedashi)
	{
		return create_rand_droppile(layers * 4, inc_tedashi);
	}
	
	public static ArrayList<Integer> create_rand_Hand(int calls)
	{
		ArrayList<Integer> return_hand = new ArrayList<Integer>();
		for(double tile: create_rand_droppile(3, false))
		{
			return_hand.add((int)tile);
			if(return_hand.size() >= (13 - calls * 3))
			{
				break;
			}
		}
		return Group.sortArray(return_hand);
	}
	
	/**
	 * @Function Just creates random groups for better testing experience
	 * @param is_complete Whether the group should always be complete or not, false = include incomplete
	 * @param is_concealed Whether the group should always be concealed, false = include opened
	 * @return A random Group of the desired choice given the parameter values
	 */ 
	public static Group random_group(boolean is_complete, boolean is_concealed)
	{
		int suit = (int)(Math.random() * 4); //Decides random suit
		boolean declared = false;
		boolean concealed = true;
		if(Math.random() > 0.5 && !concealed)
		{
			concealed = false;
			declared = true;
		}
		ArrayList<Integer> tile_list = new ArrayList<Integer>();
		int start = (int)(Math.random() * 9);
		int selected = (int)(Math.random() * 6);
		if(suit == 3)
		{
			start = (int)(Math.random() * 7);
			int[] possible_shapes = {0, 1, 3, 5};
			selected = possible_shapes[(int)(Math.random() * 4)];
		}
		if(is_complete)
		{
			selected = (int)(Math.random() * 3);
			if(suit == 3)
			{
				selected = (int)(Math.random() * 2);
			}
		}
		/*
		 * 0 == complete + quad
		 * 1 == complete + triplet
		 * 2 == complete + sequence
		 * 3 == incomplete + pair
		 * 4 == incomplete + wait sequence
		 * 5 == incomplete + floating
		 */
		switch(selected) //Decides what group to create
		{
			case 0:
				for(int i = 0; i < 4; i++) tile_list.add(start + (suit * 9));
				declared = true;
				break;
			case 1:
				for(int i = 0; i < 3; i++) tile_list.add(start + (suit * 9));
				break;
			case 2:
				start = (int)(Math.random() * 7);
				for(int i = 0; i < 3; i++) tile_list.add(start + i + (suit * 9));
				break;
			case 3: 
				for(int i = 0; i < 2; i++) tile_list.add(start + (suit * 9));
				break;
			case 4:
				start = (int)(Math.random() * 8);
				for(int i = 0; i < 2; i++) tile_list.add(start + i + (suit * 9));
				break;
			case 5:
				tile_list.add(start + (suit * 9));
				break;
		}
		return new Group(tile_list, declared, concealed);
	}
	
	public static PlayerHand create_rand_PlayerHand(int calls)
	{
		ArrayList<Group> exist_groups = new ArrayList<Group>();
		for(int i = 0; i < calls; i++) exist_groups.add(random_group(true, false));
		return new PlayerHand(create_rand_Hand(calls), exist_groups);
	}
	public static void main(String[] args)
	{
		System.out.println(create_rand_wholeDroppile(3, true));
		for(int i = 0; i <= 4; i++)
		{
			System.out.println(create_rand_Hand(i));
		}
	}
}

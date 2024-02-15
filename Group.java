package bot_package;

import java.util.*;
public class Group 
{
	protected ArrayList<Integer> threeTile = new ArrayList<Integer>();
	protected boolean completed;
	public boolean isKan;
	
	/*
	 * ArrayList<Integer> object Compatible Constructor
	 * Used to make a confirmed Group
	 */
	public Group(ArrayList<Integer> in_group)
	{
		this.threeTile = new ArrayList<Integer>(in_group);
		setGroupFields(this.threeTile.size());
	}
	
	/*
	 * int[] object Compatible Constructor
	 * Used to make a confirmed Group
	 */
	public Group(int[] in_group)
	{
		for(int i = 0; i < in_group.length; i++) threeTile.add(in_group[i]);
		setGroupFields(this.threeTile.size());
	}
	
	/*
	 * Used to create a new clone object of Group
	 */
	public Group(Group clone)
	{
		this.threeTile = clone.threeTile;
		this.completed = clone.completed;
		this.isKan = clone.isKan;
	}
	
	/*
	 * Used to initialize if object fields is kan or complete
	 */
	public void setGroupFields(int size_case)
	{
		switch(size_case)
		{
		case 4:
			this.completed = true;
			this.isKan = true;
		case 3:
			this.completed = true;
			this.isKan = false;
		default:
			this.completed = false;
			this.isKan = false;
		}
	}
	
	/**
	 * 
	 * @param in_array: A input of an ArrayList<Integer> that needs to be sorted
	 * @return A sorted ArrayList<Integer> of the original inputted ArrayList<Integer>
	 */
	public static ArrayList<Integer> sortArray(ArrayList<Integer> in_array)
	{
		ArrayList<Integer> returnList = new ArrayList<Integer>();
		ArrayList<Integer> tempList = new ArrayList<Integer>(in_array);
		int min = in_array.get(0);
		int index = 0;
		
		for(int i = 0; i < in_array.size(); i++)
		{
			min = tempList.get(index);
			for(int j = 0; j < tempList.size(); j++)
			{
				if(tempList.get(j) < min)
				{
					min = tempList.get(j);
					index = j;
				}
			}
			returnList.add(min);
			tempList.remove(index);
			index = 0;
		}
		return returnList;
	}
	
	/*
	 * Used to make an ArrayList<Integer> with int[]
	 */
	public static ArrayList<Integer> createArrayList(int[] in_list)
	{
		ArrayList<Integer> return_ArrayList = new ArrayList<Integer>();
		for(int i = 0; i < in_list.length; i++) return_ArrayList.add(in_list[i]);
		return return_ArrayList;
	}
	public static void main(String[] args)
	{
		int[] player_hand = {28, 31, 31, 31, 32, 32, 32, 33, 33, 33, 27, 27, 27};
		System.out.println(createArrayList(player_hand));
		System.out.println(sortArray(createArrayList(player_hand)));
	}
}

package nl.tudelft.otsim.Utilities;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Sort the elements of a Collection
 * Based on http://stackoverflow.com/questions/740299/how-do-i-sort-a-set-to-a-list-in-java
 */
public class Sorter {
	
	/**
	 * @param c Collection to sort
	 * @return List<T>; the elements of the Collection in sorted order
	 */
	public static
	<T extends Comparable<? super T>> List<T> asSortedList(Collection<T> c) {
	  List<T> list = new ArrayList<T>(c);
	  java.util.Collections.sort(list);
	  return list;
	}

}

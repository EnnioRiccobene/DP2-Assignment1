package it.polito.dp2.BIB.sol1;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.sol1.jaxb.BiblioItemType;

abstract class MyBiblioItem implements it.polito.dp2.BIB.ItemReader {
	
    BiblioItemType item;
    HashMap<Integer, ItemReader> itemMap; 
    
	public MyBiblioItem(final BiblioItemType item, final HashMap<Integer, ItemReader> itemMap) {
		super();
		this.item = item;
		this.itemMap = itemMap;

	}

	@Override
	public String[] getAuthors() {
		return this.item.getAuthor().toArray(new String[0]);
	}

	@Override
	public Set<ItemReader> getCitingItems() {
		final Set<ItemReader> itemSet = new HashSet<ItemReader>();
		for(final BigInteger cited : this.item.getCitedBy()) {			
			
			itemSet.add(this.itemMap.get(cited.intValue()));
		}
		return itemSet;
	}

	@Override
	public String getSubtitle() {
		return this.item.getSubtitle();
	}

	@Override
	public String getTitle() {
		return this.item.getTitle();
	}
	
	public int getId() {
		return this.item.getId().intValue();
	}
	
	public Set<Integer> getCitingItemsIds() {
		final Set<Integer> itemSet = new HashSet<Integer>();
		for(final BigInteger cited : this.item.getCitedBy()) {
			itemSet.add(cited.intValue());
		}
		return itemSet;
	}

}

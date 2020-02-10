package it.polito.dp2.BIB.sol1;

import java.util.HashMap;

import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.sol1.jaxb.BiblioItemType;
import it.polito.dp2.BIB.sol1.jaxb.BookType;

public class MyBookReader extends MyBiblioItem implements it.polito.dp2.BIB.BookReader {

	BookType book;
	
	public MyBookReader(final BiblioItemType item, final HashMap<Integer, ItemReader> itemMap) throws BibReaderException {
		super(item, itemMap);
		this.book = (BookType) item; 
		
		if(this.book.getPublicationYear().getYear() < 0) throw new BibReaderException();
	}
	
	@Override
	public String getISBN() {
		return this.book.getISBN();
	}

	@Override
	public String getPublisher() {
		return this.book.getPublisher();
	}

	@Override
	public int getYear() {
		return this.book.getPublicationYear().getYear();
	}
}

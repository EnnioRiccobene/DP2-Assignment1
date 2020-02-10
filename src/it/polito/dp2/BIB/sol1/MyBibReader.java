package it.polito.dp2.BIB.sol1;

import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;
import it.polito.dp2.BIB.sol1.jaxb.BiblioItemType;
import it.polito.dp2.BIB.sol1.jaxb.BookType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;

public class MyBibReader implements it.polito.dp2.BIB.BibReader{
	
	private List<BiblioItemType> items;
	private List<JournalType> journals;
	private List<MyJournalReader> myJournals;
	private String schemaFilename;
	private HashMap<Integer, ItemReader> itemMap; //mappa di item e i loro id
	
    public MyBibReader() throws Exception {
        this.schemaFilename = "xsd/biblio_e.xsd";
        
        final String inputFileName = System.getProperty("it.polito.dp2.BIB.sol1.BibInfo.file");
        this.init(inputFileName);
    }
    
    private void init(final String inputFileName) throws Exception {

        System.out.println("Input file: " + inputFileName);
        final JAXBContext jc = JAXBContext.newInstance("it.polito.dp2.BIB.sol1.jaxb");
        final Unmarshaller u = jc.createUnmarshaller();
        try {
            final SchemaFactory sf = SchemaFactory.newInstance("http://www.w3.org/2001/XMLSchema");
            u.setSchema(sf.newSchema(new File(this.schemaFilename)));
            final Biblio biblio = (Biblio)u.unmarshal((InputStream)new FileInputStream(inputFileName));
            
            items = new ArrayList<>();
            journals = new ArrayList<>();
            itemMap = new HashMap<>();
            myJournals = new ArrayList<>();
            
            for(JournalType j : biblio.getJournal()){
            	journals.add(j);
            }

            for(JournalType j : journals){
    			MyJournalReader jo = new MyJournalReader(j, itemMap);
    			myJournals.add(jo); 			
    		}            

            for(BookType book : biblio.getBook()){
            	items.add(book);
            	itemMap.put(book.getId().intValue(), new MyBookReader(book, itemMap));
            }
            for(ArticleType article : biblio.getArticle()){
            	items.add(article);
            	itemMap.put(article.getId().intValue(), new MyArticleReader(article, itemMap, myJournals));
            }
        }
        catch(BibReaderException e3){
            System.err.println("Could not use schema " + this.schemaFilename);
            throw e3;
        }
        catch (SAXException e) {
            System.err.println("Could not use schema " + this.schemaFilename);
            throw e;
        }
        catch (FileNotFoundException e2) {
            System.err.println("Could not use schema " + this.schemaFilename);
            throw e2;
        }
    }

	@Override
	public BookReader getBook(String isbn) {
		
		for(BiblioItemType item : items){
			if(item instanceof BookType){
				if(((BookType) item).getISBN().equals(isbn)){
					MyBookReader book;
					try {
						book = new MyBookReader(item, itemMap);
						return book;
					} catch (BibReaderException e) {
						System.err.println("Could not generete an element of the schema.");
						e.printStackTrace();
					}	
				}				
			}
		}		
		return null;
	}

	@Override
	public Set<ItemReader> getItems(String keyword, int since, int to) {
        final Set<ItemReader> itemsSet = new HashSet<ItemReader>();

        
        HashMap<Integer, MyBiblioItem> myitemMap = new HashMap<>();
        for(ItemReader i : itemMap.values()){ 
        	myitemMap.put(MyBibReader.getKeyByValue(itemMap, i), (MyBiblioItem) i);
        }
        
        for (final MyBiblioItem i : myitemMap.values()) {
            int year = 0;
            if(i instanceof MyBookReader) {
            	year = ((MyBookReader) i).getYear();
            }
            else if (i instanceof MyArticleReader){
            	year = ((MyArticleReader) i).getIssue().getYear();
            }
            	
            if(keyword == null && year >= since && year <= to){
            	itemsSet.add(i);
            } else if (i.getTitle().contains(keyword) && year >= since && year <= to){
            	itemsSet.add(i);
            }
        }
        return itemsSet;
	}

	@Override
	public JournalReader getJournal(String issn) {
		for(JournalType j : journals){
			if(j.getISSN().equals(issn)){
				MyJournalReader journal;				
				try {			
					journal = new MyJournalReader(j, itemMap);
					return journal;
					
				} catch (BibReaderException e) {
					System.err.println("Could not generete an element of the schema.");
					e.printStackTrace();
				}
			}
		}
		return null; 
	}

	@Override
	public Set<JournalReader> getJournals(String keyword) {
		final Set<JournalReader> journalSet = new HashSet<JournalReader>();
		for(JournalType j : journals){
			
			if(keyword == null){ 
				MyJournalReader jo;	
				try {				
					jo = new MyJournalReader(j, itemMap);
					journalSet.add(jo);					
				} catch (BibReaderException e) {
					System.err.println("Could not generete an element of the schema.");
					e.printStackTrace();
				}				
			} else if(j.getTitle().contains(keyword) || j.getPublisher().contains(keyword)){
				MyJournalReader jo;				
				try {
					jo = new MyJournalReader(j, itemMap);
					journalSet.add(jo);					
				} catch (BibReaderException e) {
					System.err.println("Could not generete an element of the schema.");
					e.printStackTrace();
				}					
			}
		}		
		return journalSet;
	}
	
	private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
	    for (Entry<T, E> entry : map.entrySet()) {
	        if (Objects.equals(value, entry.getValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

}

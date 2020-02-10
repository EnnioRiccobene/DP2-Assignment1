package it.polito.dp2.BIB.sol1;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.GregorianCalendar;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeConstants;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;

import org.xml.sax.SAXException;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.BibReaderFactory;
import it.polito.dp2.BIB.BookReader;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.Biblio;
import it.polito.dp2.BIB.sol1.jaxb.BookType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType.Issue;


public class BibInfoSerializer {
	private BibReader monitor;

	
	/**
	 * Default constructror
	 * @throws BibReaderException 
	 */
	public BibInfoSerializer() throws BibReaderException {
		BibReaderFactory factory = BibReaderFactory.newInstance();
		monitor = factory.newBibReader();
	}
	
	public BibInfoSerializer(BibReader monitor) {
		super();
		this.monitor = monitor;
	}

	/**
	 * @param args
	 * @throws DatatypeConfigurationException 
	 * @throws ParseException 
	 * @throws SAXException 
	 */
	public static void main(String[] args) throws ParseException, DatatypeConfigurationException, SAXException {

		if(args.length != 1){
			System.err.println("Invalid number of arguments.");
			System.exit(1);
		} else {
			String outputFileName = args[0];
			
			BibInfoSerializer wf;
			try {
				wf = new BibInfoSerializer();
				//wf.printAll();
				
				//wf.jaxbObjectToXML(wf.generateBiblio());
				Biblio biblio = new Biblio();
				wf.generateBiblio(biblio);
				
				
				//Create JAXB Context
			    JAXBContext jaxbContext = JAXBContext.newInstance(Biblio.class);
			         
			    //Create Marshaller
			    Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

			    //Perform validation
			    SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI); 
		        Schema schema = sf.newSchema(new File("xsd/biblio_e.xsd"));
			    jaxbMarshaller.setSchema(schema);
			    
			    //Required formatting??
			    jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);

			    //Print XML String to Console
			    StringWriter sw = new StringWriter();
			         
			    //Write XML to StringWriter
			    jaxbMarshaller.marshal(biblio, sw);			    
			         
			    //Verify XML Content on console
			    String xmlContent = sw.toString();
			    System.out.println( xmlContent );
			    
			    //Write XML to file
			    OutputStream os = new FileOutputStream( outputFileName );
			    jaxbMarshaller.marshal( biblio, os );
			
			} catch (BibReaderException e) {
				System.err.println("Could not instantiate data generator.");
				e.printStackTrace();
				System.exit(1);
			} catch (JAXBException e) {
				System.err.println("Could not marshall object to xml.");
	            e.printStackTrace();
	            System.exit(1);
	        } catch (FileNotFoundException e) {
	        	System.err.println("File not found.");
				e.printStackTrace();
				System.exit(1);
			} 
		}
	}
    
    private void associateIdToItems(Set<ItemReader> set, TreeMap<String, Integer> map){
    	if(!map.isEmpty()){
    		return;
    	} 

		int i = 0;
		TreeSet<OrderedArticle> orderedArt = new TreeSet<>();
		TreeSet<OrderedBook> orderedBook = new TreeSet<>();
		
		for(ItemReader item : set){
			if(item instanceof BookReader ){
				orderedBook.add(new OrderedBook((BookReader) item));
			} else if(item instanceof ArticleReader ){
				orderedArt.add(new OrderedArticle ((ArticleReader) item));
			}								
		}
		
		for(OrderedArticle art : orderedArt){
			map.put(art.getArticle().getTitle(), i);
			i++;
		}
		for(OrderedBook book : orderedBook){
			map.put(book.getBook().getISBN(), i);
			i++;
		}
    }
    
    private void associateArticleToJournals(Set<ItemReader> set, TreeMap<String, Integer> map, 
    		TreeMap<Integer, String> mapArticleJournal){
    	if(!mapArticleJournal.isEmpty())
    		return;
    	
    	associateIdToItems(set, map);
    	for(ItemReader item : set){
    		if(item instanceof ArticleReader){
    			ArticleReader article = (ArticleReader) item;
    			mapArticleJournal.put(map.get(item.getTitle()), article.getJournal().getISSN()); 
    		}
    	}    	
    }
    
    private void associateIdToIssues(Set<JournalReader> set, TreeMap<IssueUniqueness, Integer> mapIssues){
    	if(!mapIssues.isEmpty())
    		return;
    	
    	int i = 0;
    	TreeSet<IssueUniqueness> orderedSet = new TreeSet<>();
    	for(JournalReader journal : set){
    		for (IssueReader issue: journal.getIssues(0, 3000)) {
    			orderedSet.add(new IssueUniqueness(journal.getISSN(), issue.getNumber(), issue.getYear()));   			
    		}
    	}
    	
    	for(IssueUniqueness issueU : orderedSet){
    		mapIssues.put(issueU, i);
    		i++;
    	}
    }
	
	public void generateBiblio(Biblio biblio) throws ParseException, DatatypeConfigurationException{
		
		// Get the list of Items
		Set<ItemReader> set = monitor.getItems(null, 0, 3000);
		// Get the list of journals
		Set<JournalReader> setJ = monitor.getJournals(null);
		
		TreeMap<String, Integer> map = new TreeMap<>(); //key: isbn|title, value: id
		TreeMap<Integer, String> mapArticleJournal = new TreeMap<>(); //key: articleId, value: issn
		TreeMap<IssueUniqueness, Integer> mapIssues = new TreeMap<>(); // key: la coppia di interi number e year, value: id
		
		associateIdToItems(set, map);
		associateArticleToJournals(set, map, mapArticleJournal);
		associateIdToIssues(setJ, mapIssues);
		
		for(JournalReader item : setJ){
			biblio.getJournal().add(generateJournal(item, mapIssues));
		}
		
		for(ItemReader item : set){
			
			if (item instanceof BookReader) {
				biblio.getBook().add(generateBook(item, map));
			}
			
			if (item instanceof ArticleReader) {
				biblio.getArticle().add(generateArticle(item, map, mapArticleJournal, mapIssues));
			}
		}	
	}
	
	private BookType generateBook(ItemReader item, TreeMap<String, Integer> map) throws ParseException, DatatypeConfigurationException{
		
		BookType book = new BookType();
		
		book.setId(BigInteger.valueOf(map.get(((BookReader) item).getISBN())));
		book.setISBN(((BookReader) item).getISBN());
		
		GregorianCalendar calendar = new GregorianCalendar();
		XMLGregorianCalendar year = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
		year.setYear(((BookReader) item).getYear());
		year.setTimezone( DatatypeConstants.FIELD_UNDEFINED );
		book.setPublicationYear(year);
		
		for(String author: item.getAuthors()){
			book.getAuthor().add(author);
		}
		book.setTitle(item.getTitle());
		if (item.getSubtitle()!=null)
			book.setSubtitle(item.getSubtitle());	
		book.setPublisher(((BookReader) item).getPublisher());
		for(ItemReader citingItem : item.getCitingItems()){
			book.getCitedBy().add(BigInteger.valueOf(map.get(citingItem.getTitle())));
		}
		
		return book;
	}
	
	private ArticleType generateArticle(ItemReader item, TreeMap<String, Integer> map, 
			TreeMap<Integer, String> mapArticleJournal, TreeMap<IssueUniqueness, Integer> mapIssueUniqueId){
	
		ArticleType article = new ArticleType();
		
		if(item instanceof ArticleReader){
			article.setId(BigInteger.valueOf(map.get(item.getTitle())));			
			article.setJournal(((ArticleReader) item).getJournal().getISSN());
			for(String author: item.getAuthors()){
				article.getAuthor().add(author);
			}
			article.setTitle(item.getTitle());
			if (item.getSubtitle()!=null)
				article.setSubtitle(item.getSubtitle());
			for(ItemReader citingItem : item.getCitingItems()){
				article.getCitedBy().add(BigInteger.valueOf(map.get(citingItem.getTitle())));
			}
			IssueUniqueness issueKey = new IssueUniqueness(((ArticleReader) item).getJournal().getISSN() ,((ArticleReader) item).getIssue().getNumber(),
														   ((ArticleReader) item).getIssue().getYear());
			article.setIssue(BigInteger.valueOf(mapIssueUniqueId.get(issueKey)));
		}
		
		return article;
	}
	
	public JournalType generateJournal(JournalReader item, TreeMap<IssueUniqueness, Integer> mapIssues) 
			throws ParseException, DatatypeConfigurationException {
		
		JournalType journal = new JournalType();
		TreeSet<IssueUniqueness> issueSet = new TreeSet<>();
		
		journal.setISSN(item.getISSN());
		journal.setTitle(item.getTitle());
		journal.setPublisher(item.getPublisher());
		for (IssueReader issue: item.getIssues(0, 3000)) {			
			IssueUniqueness issueKey = new IssueUniqueness(item.getISSN(), issue.getNumber(), issue.getYear());
			issueSet.add(issueKey);
		}
		
		for(IssueUniqueness i : issueSet){
			Issue myIssue = new Issue();
			
			myIssue.setId(BigInteger.valueOf(mapIssues.get(i)));
			myIssue.setNumber(BigInteger.valueOf(i.getNumber()));
			
			GregorianCalendar calendar = new GregorianCalendar();
			XMLGregorianCalendar year = DatatypeFactory.newInstance().newXMLGregorianCalendar(calendar);
			year.setYear(i.getYear());
			year.setTimezone( DatatypeConstants.FIELD_UNDEFINED );

			myIssue.setYear(year);
			
			journal.getIssue().add(myIssue);
		}
		
		return journal;
	}
	
	private class OrderedBook implements Comparable<OrderedBook>{
		private BookReader book;

		public OrderedBook(BookReader book) {
			super();
			this.book = book;
		}

		public BookReader getBook() {
			return book;
		}

		@Override
		public int compareTo(OrderedBook o) {
			int last = this.book.getISBN().compareTo(o.getBook().getISBN());
			return last;
		}
	}
	
	private class OrderedArticle implements Comparable<OrderedArticle>{
		private ArticleReader article;

		public OrderedArticle(ArticleReader article) {
			super();
			this.article = article;
		}

		public ArticleReader getArticle() {
			return article;
		}

		@Override
		public int compareTo(OrderedArticle o) {
			int last = this.article.getJournal().getISSN().compareTo(o.getArticle().getJournal().getISSN());
			if(last == 0){
				last = this.article.getTitle().compareTo(o.getArticle().getTitle());
				return last;
			} else return last;
		}
	}
	
	private class IssueUniqueness implements Comparable<IssueUniqueness> {
		private String issn;
		private int number;
		private int year;
		
		public IssueUniqueness(String issn, int number, int year) {
			super();
			this.issn = issn;
			this.number = number;
			this.year = year;
		}
		
		public String getIssn() {
			return issn;
		}
		
		public int getNumber() {
			return number;
		}

		public int getYear() {
			return year;
		}

		@Override
		public int hashCode(){
			return number * 31 + year;		
		}
		
		@Override
		public boolean equals(Object o){
			return number == ((IssueUniqueness) o).number && year == ((IssueUniqueness) o).year && issn == ((IssueUniqueness) o).issn;
		}

		@Override
		public int compareTo(IssueUniqueness o) {
			int last = this.issn.compareTo(o.getIssn());
			if(last == 0) {
				if(this.year == o.getYear()){
					if(this.number == o.getNumber()){
						return 0;
					} else if(this.number > o.getNumber()){
						return 1;
					} else return -1;
				} else if(this.year > o.getYear()){
					return 1;
				} else return -1;				
				
			} else return last;
		}
	}

}
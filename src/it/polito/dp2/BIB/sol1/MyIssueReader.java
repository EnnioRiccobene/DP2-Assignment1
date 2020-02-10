package it.polito.dp2.BIB.sol1;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import it.polito.dp2.BIB.ArticleReader;
import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;
import it.polito.dp2.BIB.sol1.jaxb.JournalType.Issue;

public class MyIssueReader implements it.polito.dp2.BIB.IssueReader {
	
	private JournalType.Issue issue;
    private JournalReader journal;
    private Set<ArticleReader> articleSet;
    private HashMap<Integer, ItemReader> itemMap;

	public MyIssueReader(final Issue issue, final MyJournalReader myJournal, HashMap<Integer, ItemReader> itemMap) throws BibReaderException { 
		super();
		this.issue = issue;
		this.journal = (MyJournalReader) myJournal;
		this.articleSet = new HashSet<>();
		this.itemMap = itemMap;
		
		if(this.issue.getNumber().intValue() < 0 || this.issue.getYear().getYear() < 0) throw new BibReaderException();

		for(ItemReader i : itemMap.values()){
			if(i instanceof MyArticleReader){
				
				if(((MyArticleReader) i).getIssue() == null){
					throw new BibReaderException("No issue for this Article: "+i.getTitle());
				} else if(((MyArticleReader) i).getIssue().getNumber() == this.getNumber() && 
						((MyArticleReader) i).getIssue().getYear() == this.getYear() &&
						((MyArticleReader) i).getIssue().getJournal().getISSN().equals(this.getJournal().getISSN())){
					articleSet.add((MyArticleReader) i);
				}
			}
		}
	}

	@Override
	public Set<ArticleReader> getArticles() {
		return this.articleSet;
	}

	@Override
	public JournalReader getJournal() {
		return this.journal;
	}

	@Override
	public int getNumber() {
		return this.issue.getNumber().intValue();
	}

	@Override
	public int getYear() {
		return this.issue.getYear().getYear();
	}
	
	public int getId() {
		return this.issue.getId().intValue();
	}
}
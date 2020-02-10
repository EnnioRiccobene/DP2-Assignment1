package it.polito.dp2.BIB.sol1;

import java.util.HashMap;
import java.util.List;

import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.JournalReader;
import it.polito.dp2.BIB.sol1.jaxb.ArticleType;
import it.polito.dp2.BIB.sol1.jaxb.BiblioItemType;

public class MyArticleReader extends MyBiblioItem implements it.polito.dp2.BIB.ArticleReader{
	
	private ArticleType article;
	private HashMap<String, JournalReader> issnJournalMap;
	private List<MyJournalReader> journals;

	public MyArticleReader(BiblioItemType item, HashMap<Integer, ItemReader> itemMap, List<MyJournalReader> journals) {
		super(item, itemMap);
		this.article = (ArticleType) item; 
		this.journals = journals;
		
		if(issnJournalMap == null){
			issnJournalMap = new HashMap<>();
			for(MyJournalReader j : this.journals){
				if(j.getISSN().equals(article.getJournal()))
					this.issnJournalMap.put(this.article.getJournal(), j);
			}			
		} else {
			for(MyJournalReader j : this.journals){
				if(j.getISSN().equals(article.getJournal()))
					this.issnJournalMap.put(this.article.getJournal(), j);
			}	
		}
	}

	@Override
	public IssueReader getIssue() {
		for(MyJournalReader j : this.journals){
			if(j.getIssueById(article.getIssue()) != null){ 		
				return j.getIssueById(article.getIssue());
			}
		}
		return null; 
	}

	@Override
	public JournalReader getJournal() {
		JournalReader journal = this.issnJournalMap.get(this.article.getJournal());
		return journal;
	}
}

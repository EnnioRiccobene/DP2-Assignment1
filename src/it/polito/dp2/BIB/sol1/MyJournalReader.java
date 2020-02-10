package it.polito.dp2.BIB.sol1;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.polito.dp2.BIB.BibReaderException;
import it.polito.dp2.BIB.IssueReader;
import it.polito.dp2.BIB.ItemReader;
import it.polito.dp2.BIB.sol1.jaxb.JournalType;

public class MyJournalReader implements it.polito.dp2.BIB.JournalReader {
	
	private JournalType journal;
	private HashMap<BigInteger, MyIssueReader> issuesById;
	private HashMap<IssuesOfJournalUniqueness, IssueReader> issuesNumbersAndYears;
	
	private HashMap<Integer, ItemReader> itemMap;

	public MyJournalReader(final JournalType journal, HashMap<Integer, ItemReader> itemMap) throws BibReaderException {
		super();
		this.journal = journal;
		this.itemMap = itemMap;

		for(JournalType.Issue issue : this.journal.getIssue()){
			MyIssueReader myIssue = new MyIssueReader(issue, this, itemMap);

			if(issuesById == null){
				issuesById = new HashMap<>();
				issuesById.put(issue.getId(), myIssue);
			} else {
				issuesById.put(issue.getId(), myIssue);
			}

			if(issuesNumbersAndYears == null){
				issuesNumbersAndYears = new HashMap<>();
				IssuesOfJournalUniqueness issueUniqueness = 
						new IssuesOfJournalUniqueness(this.journal.getISSN(), issue.getNumber().intValue(), issue.getYear().getYear());				
				this.issuesNumbersAndYears.put(issueUniqueness, myIssue);
			} else {
				IssuesOfJournalUniqueness issueUniqueness = 
						new IssuesOfJournalUniqueness(this.journal.getISSN(), issue.getNumber().intValue(), issue.getYear().getYear());
				this.issuesNumbersAndYears.put(issueUniqueness, myIssue);
			}		
		}
	}

	@Override
	public String getISSN() {
		return this.journal.getISSN();
	}

	@Override
	public IssueReader getIssue(int year, int number) {
		if((this.issuesNumbersAndYears).containsKey(new IssuesOfJournalUniqueness(this.journal.getISSN(), number, year))){
			IssueReader issue = this.issuesNumbersAndYears.get(new IssuesOfJournalUniqueness(this.journal.getISSN(), number, year));
			return issue;
		} else return null;		
	}

	@Override
	public Set<IssueReader> getIssues(int since, int to) {
		Set<IssueReader> issuesSet = new HashSet<IssueReader>();
		for(Map.Entry<IssuesOfJournalUniqueness, IssueReader> entry : issuesNumbersAndYears.entrySet()){
			IssuesOfJournalUniqueness key = entry.getKey();
			IssueReader issue = entry.getValue();
			if(key.getYear() >= since && key.getYear() <= to) {
				issuesSet.add(issue);
			}
		}
		return issuesSet;
	}

	@Override
	public String getPublisher() {
		return this.journal.getPublisher();
	}

	@Override
	public String getTitle() {
		return this.journal.getTitle();
	}
	
	public IssueReader getIssueById(BigInteger id){
		return this.issuesById.get(id);
	}
}

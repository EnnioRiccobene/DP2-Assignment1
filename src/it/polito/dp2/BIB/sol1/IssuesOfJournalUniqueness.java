package it.polito.dp2.BIB.sol1;

public class IssuesOfJournalUniqueness implements Comparable<IssuesOfJournalUniqueness> {
	private String issn;
	private int number;
	private int year;
	
	public IssuesOfJournalUniqueness(String issn, int number, int year) {
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
		return number == ((IssuesOfJournalUniqueness) o).number && year == ((IssuesOfJournalUniqueness) o).year 
				&& issn == ((IssuesOfJournalUniqueness) o).issn;
	}
	
	@Override
	public int compareTo(IssuesOfJournalUniqueness o) {
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

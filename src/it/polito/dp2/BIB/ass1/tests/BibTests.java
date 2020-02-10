package it.polito.dp2.BIB.ass1.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import it.polito.dp2.BIB.*;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
//import java.util.TimeZone;
import java.util.TreeSet;

public class BibTests {
	protected static BibReader referenceBibReader; // reference input data
	protected static BibReader testBibReader; // implementation under test

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {

		// Create reference data generator
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.Random.BibReaderFactoryImpl");
		referenceBibReader = BibReaderFactory.newInstance().newBibReader();

		// Create implementation under test
		System.setProperty("it.polito.dp2.BIB.BibReaderFactory", "it.polito.dp2.BIB.sol1.BibReaderFactory");
		testBibReader = BibReaderFactory.newInstance().newBibReader();

	}

	@Before
	public void setUp() throws Exception {
		assertNotNull("Internal tester error during test setup: null reference", referenceBibReader);
		assertNotNull("Could not run tests: the implementation under test generated a null BibReader", testBibReader);
	}

	
	@Test
	// Check that getItems(null,0,9999) returns the expected data
	public final void testGetItems() {
		int since = 0;
		int to = 9999;
		// call getItems on the two implementations
		Set<ItemReader> ris = referenceBibReader.getItems(null, since, to);
		Set<ItemReader> tis = testBibReader.getItems(null, since, to);
		

		// compare the returned sets
		List<Iterator<ItemReader>> list = startComparison(ris, tis, "Items");
		if (list != null) {
			Iterator<ItemReader> ri = list.get(0);
			Iterator<ItemReader> ti = list.get(1);

			while (ri.hasNext() && ti.hasNext()) {
				ItemReader rir = ri.next();
				ItemReader tir = ti.next();
				compareItemReader(rir, tir);
			}
		}
	}

	// private method for comparing two ItemReader objects
	protected void compareItemReader(ItemReader rir, ItemReader tir) {
		// check the ItemReaders are not null
		assertNotNull("internal tester error: null item reader", rir);
		assertNotNull("unexpected null item reader", tir);

		System.out.println("Comparing biblio item " + rir.getTitle());

		// check the ItemReaders return the same data
		compareTitleSubtitleAuhor(rir, tir, " item ");

		List<Iterator<ItemReader>> list = startComparison(rir.getCitingItems(), tir.getCitingItems(), "citing items");
		if (list != null) {
			System.out.println("  citing items:");
			Iterator<ItemReader> ri = list.get(0);
			Iterator<ItemReader> ti = list.get(1);
			while (ri.hasNext() && ti.hasNext()) {
				ItemReader rItem = ri.next();
				ItemReader tItem = ti.next();
				compareTitleSubtitleAuhor(rItem, tItem, " citing item ");
				compareArticleOrBook(rItem, tItem);
			}
		}
	}

	private void compareArticleOrBook(ItemReader rItem, ItemReader tItem) {
		
		if (rItem instanceof BookReader) {
			System.out.println("     book "+ rItem.getTitle());
			compareString(((BookReader) rItem).getISBN(), ((BookReader) tItem).getISBN(), " book reader ISBN ");
			compareString(((BookReader) rItem).getPublisher(), ((BookReader) tItem).getPublisher(),
					" book reader Publisher ");
			assertEquals("wrong publication year ", ((BookReader) rItem).getYear(), ((BookReader) tItem).getYear());
		} else if (rItem instanceof ArticleReader) {
			System.out.println("     article "+ rItem.getTitle());
			compareString(((ArticleReader) rItem).getJournal().getISSN(),
					((ArticleReader) tItem).getJournal().getISSN(), " article reader ISSN");
			assertEquals("wrong article reader issue number", ((ArticleReader) rItem).getIssue().getNumber(),
					((ArticleReader) tItem).getIssue().getNumber());
		}
	}

	protected void compareTitleSubtitleAuhor(ItemReader rItem, ItemReader tItem, String meaning) {
		compareString(rItem.getTitle(), tItem.getTitle(), meaning + " title");
		assertTrue("lists of authors do not match ", compareStringArray(rItem.getAuthors(), tItem.getAuthors()));
		if(rItem.getSubtitle()!=null) compareString(rItem.getSubtitle(), tItem.getSubtitle(), meaning + " subtitle");
	}

	@Test
	// Check that testGetJournals(null) returns the expected data
	public final void testGetJournals() {

		Set<JournalReader> rjs = referenceBibReader.getJournals(null);
		Set<JournalReader> tjs = testBibReader.getJournals(null);

		assertNotNull("internal tester error: null journal reader", rjs);
		assertNotNull("unexpected null journal reader", tjs);

		assertEquals("wrong Number of Journals", rjs.size(), tjs.size());

		// create TreeSets of elements, using the comparator for sorting, one for
		// reference and one for implementation under test
		TreeSet<JournalReader> rts = new TreeSet<JournalReader>(new JournalReaderComparator());
		TreeSet<JournalReader> tts = new TreeSet<JournalReader>(new JournalReaderComparator());

		rts.addAll(rjs);
		tts.addAll(tjs);

		Iterator<JournalReader> listRef = rts.iterator();
		Iterator<JournalReader> listTest = tts.iterator();
		while (listRef.hasNext() && listTest.hasNext()) {
			JournalReader refJR = listRef.next();
			JournalReader testJR = listTest.next();
			compareJournals(refJR, testJR);
		}

	}

	private void compareJournals(JournalReader refJR, JournalReader testJR) {
		int since = 0;
		int to = 9999;
		// check the JournalReaders are not null
		assertNotNull("internal tester error: null journal reader", refJR);
		assertNotNull("unexpected null journal reader", testJR);

		System.out.println("Comparing journal " + refJR.getTitle());

		compareJournalStrings(refJR, testJR, " journal ");

		assertNotNull("unexpected null issue issue set", testJR.getIssues(since, to));

		assertEquals("wrong number of journal issues ", refJR.getIssues(since, to).size(),
				testJR.getIssues(since, to).size());

		// create TreeSets of elements, using the comparator for sorting, one for
		// reference and one for implementation under test
		TreeSet<IssueReader> rts = new TreeSet<IssueReader>(new IssueReaderComparator());
		TreeSet<IssueReader> tts = new TreeSet<IssueReader>(new IssueReaderComparator());

		rts.addAll(refJR.getIssues(since, to));
		tts.addAll(testJR.getIssues(since, to));

		Iterator<IssueReader> listRef = rts.iterator();
		Iterator<IssueReader> listTest = tts.iterator();

		while (listRef.hasNext() && listTest.hasNext()) {
			IssueReader refIssue = listRef.next();
			IssueReader testIssue = listTest.next();

			System.out.println("  issue number "+ refIssue.getNumber()+" have "+refIssue.getArticles().size()+ " articles");
			compareIssueReaders(refIssue, testIssue);
		}

	}

	private void compareIssueReaders(IssueReader refIssue, IssueReader testIssue) {
		
		assertNotNull("unexpected null issue reader", testIssue);
		assertEquals("wrong issue number ", refIssue.getNumber(), testIssue.getNumber());
		assertEquals("wrong issue year in journals", refIssue.getYear(), testIssue.getYear());

		Set<ArticleReader> rps = refIssue.getArticles();
		Set<ArticleReader> tps = testIssue.getArticles();

		
		
		// compare the returned sets
		List<Iterator<ArticleReader>> list = startComparison(rps, tps, " Articles ");
		if (list != null) {
			Iterator<ArticleReader> ri = list.get(0);
			Iterator<ArticleReader> ti = list.get(1);
			while (ri.hasNext() && ti.hasNext()) {
				ArticleReader rplace = ri.next();
				ArticleReader tplace = ti.next();
				compareTitleSubtitleAuhor(rplace, tplace, " articles ");
			}
		}

	}

	private void compareJournalStrings(JournalReader refJR, JournalReader testJR, String meaning) {
		compareString(refJR.getISSN(), testJR.getISSN(), meaning + " ISSN ");
		compareString(refJR.getPublisher(), testJR.getPublisher(), meaning + " publisher ");
		compareString(refJR.getTitle(), testJR.getTitle(), meaning + " title ");
	}
	
	
	class ItemReaderComparator implements Comparator<ItemReader> {
		public int compare(ItemReader f0, ItemReader f1) {
			return f0.getTitle().compareTo(f1.getTitle());
		}
	}

	class JournalReaderComparator implements Comparator<JournalReader> {
		public int compare(JournalReader f0, JournalReader f1) {
			return f0.getISSN().compareTo(f1.getISSN());
		}
	}

	class IssueReaderComparator implements Comparator<IssueReader> {
		public int compare(IssueReader f0, IssueReader f1) {
			int i = (f0.getNumber() - f1.getNumber());
			if (i != 0)
				return i;
			return f0.getYear() - (f1.getYear());
		}
	}

	
	// method for comparing two strings arrays that should be non-null
	public static boolean compareStringArray(String[] rs, String[] ts) {
		if (rs == ts)
			return true;

		if (rs == null || ts == null)
			return false;

		int n = rs.length;
		if (n != ts.length)
			return false;

		for (int i = 0; i < n; i++) {
			if (!rs[i].equals(ts[i]))
				return false;
		}

		return true;
	}

	// method for comparing two strings that should be non-null
	public void compareString(String rs, String ts, String meaning) {
		assertNotNull(rs);
		assertNotNull("null " + meaning, ts);
		assertEquals("wrong " + meaning, rs, ts);
	}

	/**
	 * Starts the comparison of two sets of elements that extend ItemReader. This
	 * method already makes some comparisons that are independent of the type (e.g.
	 * the sizes of the sets must match). Then the method arranges the set elements
	 * into ordered sets (TreeSet) and returns a pair of iterators that can be used
	 * later on for one-to-one matching of the set elements
	 * 
	 * @param rs   the first set to be compared
	 * @param ts   the second set to be compared
	 * @param type a string that specified the type of data in the set (will appear
	 *             in test messages)
	 * @return a list made of two iterators to be used for one-to-one comparisons of
	 *         the set elements
	 */
	public <T extends ItemReader> List<Iterator<T>> startComparison(Set<T> rs, Set<T> ts, String type) {
		// if one of the two sets is null while the other isn't null, the test fails
		if ((rs == null) && (ts != null) || (rs != null) && (ts == null)) {
			fail("returned set of " + type + " was null when it should be non-null or vice versa");
			return null;
		}

		// if both sets are null, there are no data to compare, and the test passes
		if ((rs == null) && (ts == null)) {
			assertTrue("there are no " + type + "!", true);
			return null;
		}

		// check that the number of elements matches
		assertEquals("wrong Number of " + type, rs.size(), ts.size());

		// create TreeSets of elements, using the comparator for sorting, one for
		// reference and one for implementation under test
		TreeSet<T> rts = new TreeSet<T>(new ItemReaderComparator());
		TreeSet<T> tts = new TreeSet<T>(new ItemReaderComparator());

		rts.addAll(rs);
		tts.addAll(ts);

		// get iterators and store them in a list
		List<Iterator<T>> list = new ArrayList<Iterator<T>>();
		list.add(rts.iterator());
		list.add(tts.iterator());

		// return the list
		return list;

	}

}

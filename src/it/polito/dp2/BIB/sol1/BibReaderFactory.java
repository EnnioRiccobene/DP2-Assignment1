package it.polito.dp2.BIB.sol1;

import it.polito.dp2.BIB.BibReader;
import it.polito.dp2.BIB.BibReaderException;

public class BibReaderFactory extends it.polito.dp2.BIB.BibReaderFactory {

	@Override
	public BibReader newBibReader() throws BibReaderException {
        try {
            return (BibReader)new MyBibReader();
        }
        catch (Exception e) {
            throw new BibReaderException(e);
        }
	}

}

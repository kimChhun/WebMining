package ch.heigvd.wem.labo1;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.StringTokenizer;

/**
 * {@link StringTokenizer} that skips common words
 */
public class CustomStringTokenizer extends StringTokenizer {
	
	private String _nextToken;
	private boolean haveNext = true;
	private Set<String> commonWords;
	
	public CustomStringTokenizer(String str, Set<String> commonWords) throws IOException {
		super(str);
		this.commonWords = commonWords;
		seek();
	}

	/**
	 * prepare the next token that isn't a common word
	 */
	private void seek() {
		do {
			haveNext = super.hasMoreTokens();
			if (haveNext) _nextToken = super.nextToken();
			else return;
			_nextToken = preprocess(_nextToken);
		} while (dropToken(_nextToken));
	}
	
	private boolean dropToken(String token) {
		return commonWords.contains(token.toLowerCase());
	}

	private String preprocess(String token) {
		// TODO any further preprocessing (stemming, lemmatization, etc...)
		return token;
	}

	@Override
	public String nextToken() {
		if (!haveNext) throw new NoSuchElementException();
		String token = _nextToken;
		seek();
		return token;
	}

	@Override
	public boolean hasMoreTokens() {
		return haveNext;
	}
	
	@Override
	public boolean hasMoreElements() {
		return haveNext;
	}
	
	@Override
	public Object nextElement() {
		return nextToken();
	}
	
	@Override
	public String nextToken(String delim) {
		throw new UnsupportedOperationException();
	}
}

package org.spoofax.jsglr.client.imploder;

import static java.lang.Math.min;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR;
import static org.spoofax.jsglr.client.imploder.IToken.TK_ERROR_KEYWORD;
import static org.spoofax.jsglr.client.imploder.IToken.*;
import static org.spoofax.jsglr.client.imploder.IToken.TK_RESERVED;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import org.spoofax.jsglr.client.KeywordRecognizer;

/**
 * @author Lennart Kats <lennart add lclnet.nl>
 * @author Karl Trygve Kalleberg <karltk near strategoxt dot org>
 */
public class Tokenizer extends AbstractTokenizer {
	
	private static final double EXPECTED_TOKENS_DIVIDER = 1.3;
	
	private final KeywordRecognizer keywords;

	private final ArrayList<Token> tokens;
	
	/** Start of the next token. */
	private int startOffset;
	
	/** Line number of the next token. */
	private int line;

	private int offsetAtLineStart;
	
	/**
	 * Creates a new tokenizer for the given
	 * file name (if applicable) and contents.
	 */
	public Tokenizer(String input, String filename, KeywordRecognizer keywords) {
		super(input, filename);
		this.keywords = keywords;
		this.tokens = new ArrayList<Token>((int) (input.length() / EXPECTED_TOKENS_DIVIDER));
		startOffset = 0;
		line = 0;
		offsetAtLineStart = 0;
		// Ensure there's at least one token
		tokens.add(new Token(this, 0, line, 0, 0, -1, TK_RESERVED));
	}
	
	public final int getStartOffset() {
		return startOffset;
	}

	public void setStartOffset(int startOffset) {
		assert isAmbigous();
		this.startOffset = startOffset;
		IToken lastToken = getTokenAtOffset(startOffset);
		this.offsetAtLineStart = lastToken.getStartOffset() - lastToken.getColumn();
		this.line = lastToken.getLine();
	}

	public IToken currentToken() {
		return tokens.size() == 0
			? null
			: tokens.get(tokens.size() - 1);
	}
	
	public int getTokenCount() {
		return tokens.size();
	}
	
	public IToken getTokenAt(int i) {
		return tokens.get(i);
	}
	
	public IToken getTokenAtOffset(int offset) {
		assert getTokenCount() < 2 || getTokenAt(getTokenCount() - 1).getEndOffset()
			== getTokenAt(getTokenCount() - 2).getEndOffset()
			: "Unordered tokens at end of tokenizer";
		Token key = new Token(this, -1, -1, -1, offset, offset, TK_RESERVED);
		int resultIndex = Collections.binarySearch(tokens, key);
		return resultIndex == -1 ? null : getTokenAt(resultIndex);
	}
	
	public final IToken makeToken(int endOffset, int kind, boolean allowEmptyToken) {
		return makeToken(endOffset, kind, allowEmptyToken, null);
	}
		
	public IToken makeToken(int endOffset, int kind, boolean allowEmptyToken, String errorMessage) {
		String input = getInput();
		assert endOffset <= input.length();
		if (!allowEmptyToken && startOffset > endOffset) // empty token
			return null;
		
		assert endOffset + 1 >= startOffset || (kind == TK_RESERVED && startOffset == 0)
			: "Creating token ending before current start offset";
		
		int offset;
		IToken token = null;
		for (offset = min(startOffset, endOffset); offset < endOffset; offset++) {
			if (input.charAt(offset) == '\n') {
				if (offset - 1 > startOffset)
					token = internalMakeToken(kind, offset - 1, errorMessage);
				internalMakeToken(TK_LAYOUT, offset, errorMessage); // newline
				line++;
				offsetAtLineStart = startOffset;
			}
		}
		
		if (token == null || offset <= endOffset) {
			int oldStartOffset = startOffset;
			token = internalMakeToken(kind, offset, errorMessage);
			if (offset >= oldStartOffset && input.charAt(offset) == '\n') {
				line++;
				offsetAtLineStart = startOffset;
			}
			return token;
		} else {
			return token;
		}
	}

	protected Token internalMakeToken(int kind, int endOffset, String errorMessage) {
		Token result = new Token(this, tokens.size(), line, startOffset - offsetAtLineStart, startOffset, endOffset, kind);
		if (errorMessage != null) result.setError(errorMessage);
		tokens.add(result);
		startOffset = endOffset + 1;
		return result;
	}
	
	public void setErrorMessage(IToken leftToken, IToken rightToken, String message) {
		assert leftToken.getTokenizer() == this && rightToken.getTokenizer() == this;
		for (int i = leftToken.getIndex(), max = rightToken.getIndex(); i <= max; i++) {
			tokens.get(i).setError(message);
		}
	}

	public void tryMakeSkippedRegionToken(int offset) {
		char inputChar = getInput().charAt(offset);
		
		boolean isInputKeywordChar = KeywordRecognizer.isPotentialKeywordChar(inputChar);
		if (isAtPotentialKeywordEnd(offset, isInputKeywordChar)) {
			if (keywords.isKeyword(toString(startOffset, offset - 1))) {
				makeToken(offset - 1, TK_ERROR_KEYWORD, false, ERROR_SKIPPED_REGION);
			} else {
				makeToken(offset - 1, TK_ERROR, false, ERROR_SKIPPED_REGION);
			}
		}
		if (isAtPotentialKeywordStart(offset, isInputKeywordChar)) {
			if (keywords.isKeyword(toString(startOffset, offset))) {
				makeToken(offset, TK_ERROR_KEYWORD, false, ERROR_SKIPPED_REGION);
			} else {
				makeToken(offset, TK_ERROR, false, ERROR_SKIPPED_REGION);
			}
		}
	}

	private boolean isAtPotentialKeywordEnd(int offset, boolean isInputKeywordChar) {
		if (offset >= 1 && offset > startOffset) {
			char prevChar = getInput().charAt(offset - 1);
			return 	(isInputKeywordChar && !isKeywordChar(prevChar))
					|| (!isInputKeywordChar && isKeywordChar(prevChar));
		}
		return false;
	}

	private boolean isAtPotentialKeywordStart(int offset, boolean isInputKeywordChar) {
		if (offset + 1 < getInput().length()) {
			char nextChar = getInput().charAt(offset + 1);
			if ((isInputKeywordChar && !isKeywordChar(nextChar))
					|| (!isInputKeywordChar && isKeywordChar(nextChar))) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Determines whether the given character could possibly 
	 * be part of a keyword (as opposed to an operator).
	 */
	private boolean isKeywordChar(char c) {
		return Character.isLetterOrDigit(c) || c == '_';
	}
	
	@Override
	public String toString() {
		String input = getInput();
		StringBuilder result = new StringBuilder();
		result.append('[');
		for (IToken token : tokens) {
			int offset = token.getStartOffset();
			result.append(input, offset, token.getEndOffset() + 1);
			result.append(',');
		}
		if (tokens.size() > 0)
			result.deleteCharAt(result.length() - 1);
		result.append(']');
		return result.toString();
	}

	protected KeywordRecognizer getKeywordRecognizer() {
		return keywords;
	}

	public Iterator<IToken> iterator() {
		@SuppressWarnings("unchecked") // covariance
		Iterator<IToken> result = (Iterator<IToken>) (Iterator<?>) tokens.iterator();
		return result;
	}

}

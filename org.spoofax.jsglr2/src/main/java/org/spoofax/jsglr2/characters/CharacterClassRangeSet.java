package org.spoofax.jsglr2.characters;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.ImmutableRangeSet;
import com.google.common.collect.Range;
import com.google.common.collect.RangeSet;
import com.google.common.collect.TreeRangeSet;

public abstract class CharacterClassRangeSet<C extends Number & Comparable<C>> implements ICharacters {

    private static final int BITMAP_SEGMENT_SIZE = 6; // 2^6 = 64 = 1/4 * 256

    protected ImmutableRangeSet<C> rangeSet; // Contains ranges in range [-128, 127]
    protected boolean containsEOF;

    private final boolean useCachedBitSet;
    private long word0; // [0, 63]
    private long word1; // [64, 127]
    private long word2; // [128, 191]
    private long word3; // [192, 255]

    protected CharacterClassRangeSet() {
        this(ImmutableRangeSet.copyOf(TreeRangeSet.create()), false);
    }

    protected CharacterClassRangeSet(final ImmutableRangeSet<C> rangeSet, boolean containsEOF) {
        assert rangeSet.isEmpty() || rangeSet.span().lowerEndpoint().intValue() >= -128;
        assert rangeSet.isEmpty() || rangeSet.span().upperEndpoint().intValue() < (EOF_INT - 128);

        this.rangeSet = rangeSet;
        this.containsEOF = containsEOF;

        this.useCachedBitSet = tryOptimize();
    }

    protected abstract CharacterClassRangeSet<C> from(final ImmutableRangeSet<C> rangeSet, boolean containsEOF);

    protected abstract C byteToInternalNumber(byte b);

    private final long wordAt(int wordIndex) {
        switch(wordIndex) {
            case -2:
                return word0;
            case -1:
                return word1;
            case 0:
                return word2;
            case 1:
                return word3;
            default:
                return 0L;
        }
    }

    @Override public final boolean containsCharacter(byte character) {
        if(useCachedBitSet) {
            final int wordIndex = character >> BITMAP_SEGMENT_SIZE;
            final long word = wordAt(wordIndex);

            return (word & (1L << character)) != 0;
        } else
            return rangeSet.contains(byteToInternalNumber(character));
    }

    @Override public final boolean containsEOF() {
        return containsEOF;
    }

    protected final CharacterClassRangeSet<C> addRange(byte from, byte to) {
        final RangeSet<C> mutableRangeSet = TreeRangeSet.create(rangeSet);

        mutableRangeSet.add(Range.closed(byteToInternalNumber(from), byteToInternalNumber(to)));

        return from(ImmutableRangeSet.copyOf(mutableRangeSet), containsEOF);
    }

    protected final CharacterClassRangeSet<C> addSingle(byte character) {
        final RangeSet<C> mutableRangeSet = TreeRangeSet.create(rangeSet);

        mutableRangeSet.add(Range.singleton(byteToInternalNumber(character)));

        return from(ImmutableRangeSet.copyOf(mutableRangeSet), containsEOF);
    }

    protected final CharacterClassRangeSet<C> addEOF() {
        return from(ImmutableRangeSet.copyOf(rangeSet), true);
    }

    public boolean tryOptimize() {
        if(!rangeSet.isEmpty()) {
            final BitSet bitSet = convertToBitSet(rangeSet);

            final long[] words = bitSet.toLongArray();
            switch(words.length) {
                case 4:
                    word3 = words[3];
                case 3:
                    word2 = words[2];
                case 2:
                    word1 = words[1];
                case 1:
                    word0 = words[0];
            }
            return true;
        } else {
            return false;
        }
    }

    private final BitSet convertToBitSet(final RangeSet<C> rangeSet) {
        if(rangeSet.isEmpty()) {
            return new BitSet();
        }

        final BitSet bitSet = new BitSet(rangeSet.span().upperEndpoint().intValue() + 128);

        rangeSet.asRanges().forEach(range -> {
            bitSet.set(range.lowerEndpoint().intValue() + 128, range.upperEndpoint().intValue() + 128 + 1);
        });

        return bitSet;
    }

    @Override public int hashCode() {
        return rangeSet.hashCode(); // TODO: add containsEOF
    }

    @Override public boolean equals(Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }

        CharacterClassRangeSet<C> that = (CharacterClassRangeSet<C>) o;

        return rangeSet.equals(that.rangeSet); // TODO: add containsEOF
    }

    @Override public <C2 extends Number & Comparable<C2>> CharacterClassRangeSet<C2>
        rangeSetUnion(CharacterClassRangeSet<C2> other) {
        return other.union((CharacterClassRangeSet<C2>) this);
    }

    protected abstract CharacterClassRangeSet<C> union(CharacterClassRangeSet<C> other);

    @Override public String toString() {
        final List<String> ranges = new ArrayList<>();

        rangeSet.asRanges().forEach(range -> {
            final byte from = range.lowerEndpoint().byteValue();
            final byte to = range.upperEndpoint().byteValue();

            if(from != to)
                ranges.add("" + ICharacters.byteToString(from) + "-" + ICharacters.byteToString(to));
            else
                ranges.add("" + ICharacters.byteToString(from));
        });

        if(containsEOF)
            ranges.add("EOF");

        return "{" + StringUtils.join(ranges, ",") + "}";
    }

}

package org.spoofax.jsglr2.characters;

public abstract class RangeSetCharacterClassFactory<C extends Number & Comparable<C>> extends CharacterClassFactory {

    public ICharacters fromEmpty() {
        return emptyRangeSet();
    }

    protected abstract CharacterClassRangeSet<C> emptyRangeSet();

    @Override public final ICharacters fromRange(int from, int to) {
        return emptyRangeSet().addRange((byte) (from - 128), (byte) (to - 128));
    }

    @Override public final ICharacters union(ICharacters a, ICharacters b) {
        boolean aIsRangeSet = a instanceof CharacterClassRangeSet;
        boolean bIsRangeSet = b instanceof CharacterClassRangeSet;

        if(aIsRangeSet || bIsRangeSet) {
            CharacterClassRangeSet<C> rangeSet;
            ICharacters other;

            if(aIsRangeSet) {
                rangeSet = (CharacterClassRangeSet<C>) a;
                other = b;
            } else {
                rangeSet = (CharacterClassRangeSet<C>) b;
                other = a;
            }

            return other.rangeSetUnion(rangeSet);
        } else {
            CharacterClassRangeSet<C> result = emptyRangeSet();

            result = a.rangeSetUnion(result);
            result = b.rangeSetUnion(result);

            return result;
        }
    }


}

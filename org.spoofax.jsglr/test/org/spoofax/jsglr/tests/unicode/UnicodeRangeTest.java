package org.spoofax.jsglr.tests.unicode;

import static org.junit.Assert.*;

import org.junit.Test;
import org.spoofax.jsglr.unicode.UnicodeInterval;
import org.spoofax.jsglr.unicode.UnicodeRange;

public class UnicodeRangeTest {
	
	private static UnicodeRange createRange(int start, int end) {
		return new UnicodeRange(new UnicodeInterval(start, end));
	}

	@Test
	public void testUnite() {
		UnicodeRange u11 = createRange(0,5);
		UnicodeRange u12 = createRange(15,20);
		UnicodeRange u13 = createRange(23,28);
		UnicodeRange u14 = createRange(29,33);
		UnicodeRange u1 = new UnicodeRange();
		u1.unite(u11);
		u1.unite(u12);
		u1.unite(u13);
		u1.unite(u14);
		
		UnicodeRange exp1 = createRange(23, 33);
		exp1.unite(u12);
		exp1.unite(u11);
		assertEquals(exp1, u1);
		
		UnicodeRange u21 = createRange(1,4);
		UnicodeRange u22 = createRange(13,16);
		UnicodeRange u23 = createRange(24, 27);
		UnicodeRange u2 = new UnicodeRange();
		u2.unite(u21);
		u2.unite(u22);
		u2.unite(u23);
		u2.unite(u14);
		
		UnicodeRange u31 = createRange(25, 28);
		UnicodeRange u32 = createRange(28, 29);
		UnicodeRange u3 = new UnicodeRange();
		u3.unite(u31);
		u3.unite(u32);
		
		assertEquals(createRange(25, 29), u3);
		
		UnicodeRange u4 = createRange(3, 8);
		
		UnicodeRange u5 = createRange(12, 17);
		
		UnicodeRange u6 = createRange(34, 34);
		
		UnicodeRange u = new UnicodeRange();
		u.unite(u1);
		assertEquals(u1, u);
		u.unite(u2);
		
		u.unite(u3);
		u.unite(u4);
		u.unite(u5);
		u.unite(u6);
		
		UnicodeRange expected = new UnicodeRange();
		expected.unite(createRange(0, 8));
		expected.unite(createRange(12, 20));
		expected.unite(createRange(23, 34));
		
		assertEquals(expected, u);
	}
	
	@Test
	public void testIntersect() {
		UnicodeRange u1 = createRange(0, 20);
		u1.unite(createRange(22, 30));
		UnicodeRange u2 = createRange(6, 25);
		UnicodeRange u3 = createRange(4, 20);
		UnicodeRange u4 = createRange(8, 15);
		u1.unite(createRange(16, 19));
		UnicodeRange u5 = createRange(10, 13);
		UnicodeRange u6 = createRange(11, 13);
		UnicodeRange u7 = createRange(11, 12);
		UnicodeRange u8 = createRange(12, 14);
		UnicodeRange u9 = createRange(12, 12);
		UnicodeRange u10 = createRange(11, 11);
		
		u1.intersect(u2);
		UnicodeRange exp1 = createRange(6,20);
		exp1.unite(createRange(22, 25));
		assertEquals(exp1, u1);
		u1.intersect(u3);
		assertEquals(createRange(6, 20), u1);
		u1.intersect(u4);
		assertEquals(u4, u1);
		u1.intersect(u5);
		assertEquals(createRange(10, 13), u1);
		u1.intersect(u6);
		assertEquals(createRange(11, 13), u1);
		u1.intersect(u7);
		assertEquals(createRange(11, 12), u1);
		u1.intersect(u8);
		assertEquals(createRange(12, 12), u1);
		u1.intersect(u9);
		assertEquals(createRange(12, 12), u1);
		u1.intersect(u10);
		assertEquals(new UnicodeRange(), u1);
	}
	
	@Test
	public void testDiff() {
		UnicodeRange u1 = createRange(3, 14);
		u1.unite(createRange(17, 25));
		u1.unite(createRange(28, 34));
		
		UnicodeRange u2 = createRange(11, 18);
		u2.unite(createRange(22, 23));
		u2.unite(createRange(30, 32));
		
		UnicodeRange u3 = createRange(5, 5);
		u3.unite(createRange(9, 19));
		u3.unite(createRange(27, 30));
		
		UnicodeRange u4 = createRange(3, 6);
		u4.unite(createRange(22, 35));
		
		UnicodeRange u5 = createRange(7, 23);
		
		UnicodeRange exp1 = createRange(3,10);
		exp1.unite(createRange(19, 21));
		exp1.unite(createRange(24, 25));
		exp1.unite(createRange(28, 29));
		exp1.unite(createRange(33, 34));
		
		UnicodeRange exp2 = createRange(3, 4);
		exp2.unite(createRange(6, 8));
		exp2.unite(createRange(20, 21));
		exp2.unite(createRange(24, 25));
		exp2.unite(createRange(33, 34));
		
		UnicodeRange exp3 = createRange(7, 8);
		exp3.unite(createRange(20, 21));
		
		UnicodeRange exp4 = new UnicodeRange();
		
		u1.diff(u2);
		assertEquals(u1, exp1);
		u1.diff(u3);
		assertEquals(u1, exp2);
		u1.diff(u4);
		assertEquals(u1, exp3);
		u1.diff(u5);
		assertEquals(u1, exp4);
		
	}

}
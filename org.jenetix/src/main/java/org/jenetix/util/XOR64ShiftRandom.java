/*
 * Java Genetic Algorithm Library (@__identifier__@).
 * Copyright (c) @__year__@ Franz Wilhelmstötter
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Author:
 *    Franz Wilhelmstötter (franz.wilhelmstoetter@gmx.at)
 */
package org.jenetix.util;

import static org.jenetics.internal.util.Equality.eq;

import org.jenetics.internal.util.Equality;
import org.jenetics.internal.util.Hash;

import org.jenetics.util.Random64;
import org.jenetics.util.math;


/**
 * <q align="justified" cite="http://www.nr.com/"><em>
 * This generator was discovered and characterized by George Marsaglia
 * [<a href="http://www.jstatsoft.org/v08/i14/paper">Xorshift RNGs</a>]. In just
 * three XORs and three shifts (generally fast operations) it produces a full
 * period of 2<sup>64</sup> - 1 on 64 bits. (The missing value is zero, which
 * perpetuates itself and must be avoided.) High and low bits pass Diehard.
 * </em></q>
 *
 * <p align="left">
 * <strong>Numerical Recipes 3rd Edition: The Art of Scientific Computing</strong>
 * <br/>
 * <em>Chapter 7. Random Numbers, Section 7.1.2, Page 345</em>
 * <br/>
 * <small>Cambridge University Press New York, NY, USA ©2007</small>
 * <br/>
 * ISBN:0521880688 9780521880688
 * <br/>
 * [<a href="http://www.nr.com/">http://www.nr.com/</a>].
 * <p/>
 *
 * <p><b>
 * The <i>main</i> class of this PRNG is not thread safe. To create an thread
 * safe instances of this PRNG, use the {@link XOR64ShiftRandom.ThreadSafe} class.
 * </b></p>
 *
 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
 * @since !__version__!
 * @version !__version__! &mdash; <em>$Date: 2014-07-11 $</em>
 */
public class XOR64ShiftRandom extends Random64 {

	private static final long serialVersionUID = 1L;


	/**
	 * This field can be used to initial the {@link org.jenetics.util.RandomRegistry}
	 * with a fast and thread safe random engine of this type; each thread gets
	 * a <i>local</i> copy of the {@code XORShiftRandom} engine.
	 *
	 * [code]
	 * RandomRegistry.setRandom(new XORShiftRandom.ThreadLocal());
	 * [/code]
	 *
	 * Calling the {@link XOR64ShiftRandom#setSeed(long)} method on the returned
	 * instance will throw an {@link UnsupportedOperationException}.
	 *
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
	 * @since 1.1
	 * @version 1.1 &mdash; <em>$Date: 2014-07-11 $</em>
	 */
	public static final class ThreadLocal extends java.lang.ThreadLocal<XOR64ShiftRandom> {

		private final long _seed = math.random.seed();

		@Override
		protected XOR64ShiftRandom initialValue() {
			return new TLXOR64ShiftRandom(math.random.seed(_seed));
		}
	}

	private static final class TLXOR64ShiftRandom extends XOR64ShiftRandom {

		private static final long serialVersionUID = 1L;

		private final Boolean _sentry = Boolean.TRUE;

		private TLXOR64ShiftRandom(final long seed) {
			super(seed);
		}

		@Override
		public void setSeed(final long seed) {
			if (_sentry != null) {
				throw new UnsupportedOperationException(
					"The 'setSeed(long)' method is not supported " +
					"for thread local instances."
				);
			}
		}

	}

	/**
	 * This class is a <i>thread safe</i> version of the {@code XORShiftRandom}
	 * engine. Instances of <i>this</i> class and instances of the non-thread
	 * safe variants, with the same seed, will generate the same sequence of
	 * random numbers.
	 * [code]
	 * final XORShiftRandom a = new XORShiftRandom(123);
	 * final XORShiftRandom b = XORShiftRandom.ThreadSafe(123);
	 * for (int i = 0; i < 1000;  ++i) {
	 *     assert (a.nextLong() == b.nextLong());
	 *     assert (a.nextDouble() == b.nextDouble());
	 * }
	 * [/code]
	 *
	 * @author <a href="mailto:franz.wilhelmstoetter@gmx.at">Franz Wilhelmstötter</a>
	 * @since 1.1
	 * @version 1.1 &mdash; <em>$Date: 2014-07-11 $</em>
	 */
	public static final class ThreadSafe extends XOR64ShiftRandom {
		private static final long serialVersionUID = 1L;

		/**
		 * Create a new <i>thread safe</i> instance of the XOR-Shift PRNG, with
		 * an seed of {@link System#nanoTime()}.
		 */
		public ThreadSafe() {
		}

		/**
		 * Create a new <i>thread safe</i> instance of the XOR-Shift PRNG.
		 *
		 * @param seed the seed of the PRNG.
		 */
		public ThreadSafe(final long seed) {
			super(seed);
		}

		@Override
		public synchronized void setSeed(final long seed) {
			super.setSeed(seed);
		}

		@Override
		public synchronized long nextLong() {
			return super.nextLong();
		}

	}

	private long _x;

	/**
	 * Create a new <i>non-thread safe</i> instance of the XOR-Shift PRNG, with
	 * an seed of {@link System#nanoTime()}.
	 */
	public XOR64ShiftRandom() {
		this(math.random.seed());
	}

	/**
	 * Create a new <i>non-thread safe</i> instance of the XOR-Shift PRNG.
	 *
	 * @param seed the seed of the PRNG.
	 */
	public XOR64ShiftRandom(final long seed) {
		_x = init(seed);
	}

	private static long init(final long seed) {
		return seed == 0 ? 0xdeadbeef : seed;
	}

	@Override
	public long nextLong() {
//		The list of the suggested shift values:
//		21, 35, 4
//		20, 41, 5
//		17, 31, 8
//		11, 29, 14
//		14, 29, 11
//		30, 35, 13
//		21, 37, 4
//		21, 43, 4
//		23, 41, 18

		_x ^= (_x << 21);
		_x ^= (_x >>> 35);
		_x ^= (_x << 4);
		return _x;
	}

	@Override
	public void setSeed(final long seed) {
		_x = init(seed);
	}

	@Override
	public int hashCode() {
		return Hash.of(getClass())
			.and(_x).value();
	}

	@Override
	public boolean equals(final Object obj) {
		return Equality.of(this, obj).test(random ->
			eq(_x, random._x)
		);
	}

	@Override
	public String toString() {
		return String.format("%s[%d]", getClass().getName(), _x);
	}

}


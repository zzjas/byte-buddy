package net.bytebuddy.asm;

import net.bytebuddy.ByteBuddy;
import net.bytebuddy.dynamic.loading.ClassLoadingStrategy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static net.bytebuddy.matcher.ElementMatchers.named;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(Parameterized.class)
public class AdviceAssignReturnedTypeTest {

    private static final String FOO = "foo";

    private final Class<?> sample, skipAdvice, noSkipAdvice, skipDelegationAdvice, noSkipDelegationAdvice;

    private final Object skipReturn, noSkipReturn;

    public AdviceAssignReturnedTypeTest(Class<?> sample,
                                        Class<?> skipAdvice,
                                        Class<?> noSkipAdvice,
                                        Class<?> skipDelegationAdvice,
                                        Class<?> noSkipDelegationAdvice,
                                        Object skipReturn,
                                        Object noSkipReturn) {
        this.sample = sample;
        this.skipAdvice = skipAdvice;
        this.noSkipAdvice = noSkipAdvice;
        this.skipDelegationAdvice = skipDelegationAdvice;
        this.noSkipDelegationAdvice = noSkipDelegationAdvice;
        this.skipReturn = skipReturn;
        this.noSkipReturn = noSkipReturn;
    }

    @Parameterized.Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {BooleanSample.class, BooleanSkipAdvice.class, BooleanNoSkipAdvice.class, BooleanSkipDelegationAdvice.class, BooleanNoSkipDelegationAdvice.class, true, false},
                {ByteSample.class, ByteSkipAdvice.class, ByteNoSkipAdvice.class, ByteSkipDelegationAdvice.class, ByteNoSkipDelegationAdvice.class, (byte) 1, (byte) 0},
                {ShortSample.class, ShortSkipAdvice.class, ShortNoSkipAdvice.class, ShortSkipDelegationAdvice.class, ShortNoSkipDelegationAdvice.class, (short) 1, (short) 0},
                {CharSample.class, CharSkipAdvice.class, CharNoSkipAdvice.class, CharSkipDelegationAdvice.class, CharNoSkipDelegationAdvice.class, (char) 1, (char) 0},
                {IntSample.class, IntSkipAdvice.class, IntNoSkipAdvice.class, IntSkipDelegationAdvice.class, IntNoSkipDelegationAdvice.class, 1, 0},
                {LongSample.class, LongSkipAdvice.class, LongNoSkipAdvice.class, LongSkipDelegationAdvice.class, LongNoSkipDelegationAdvice.class, 1L, 0L},
                {FloatSample.class, FloatSkipAdvice.class, FloatNoSkipAdvice.class, FloatSkipDelegationAdvice.class, FloatNoSkipDelegationAdvice.class, 1f, 0f},
                {DoubleSample.class, DoubleSkipAdvice.class, DoubleNoSkipAdvice.class, DoubleSkipDelegationAdvice.class, DoubleNoSkipDelegationAdvice.class, 1d, 0d},
                {ReferenceSample.class, ReferenceSkipAdvice.class, ReferenceNoSkipAdvice.class, ReferenceSkipDelegationAdvice.class, ReferenceNoSkipDelegationAdvice.class, FOO, null}
        });
    }

    @Test
    public void testSkip() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(sample)
                .visit(Advice.withCustomMapping()
                        .with(new Advice.AssignReturned.Factory())
                        .to(skipAdvice)
                        .on(named(FOO)))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getMethod(FOO).invoke(type.getConstructor().newInstance()), is(skipReturn));
    }

    @Test
    public void testNoSkip() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(sample)
                .visit(Advice.withCustomMapping()
                        .with(new Advice.AssignReturned.Factory())
                        .to(noSkipAdvice)
                        .on(named(FOO)))
                .make()
                .load(ClassLoadingStrategy.BOOTSTRAP_LOADER, ClassLoadingStrategy.Default.WRAPPER)
                .getLoaded();
        assertThat(type.getMethod(FOO).invoke(type.getConstructor().newInstance()), is(noSkipReturn));
    }

    @Test
    public void testSkipDelegation() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(sample)
                .visit(Advice.withCustomMapping()
                        .with(new Advice.AssignReturned.Factory())
                        .to(skipDelegationAdvice)
                        .on(named(FOO)))
                .make()
                .load(skipDelegationAdvice.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
                .getLoaded();
        assertThat(type.getMethod(FOO).invoke(type.getConstructor().newInstance()), is(skipReturn));
    }

    @Test
    public void testNoSkipDelegation() throws Exception {
        Class<?> type = new ByteBuddy()
                .redefine(sample)
                .visit(Advice.withCustomMapping()
                        .with(new Advice.AssignReturned.Factory())
                        .to(noSkipDelegationAdvice)
                        .on(named(FOO)))
                .make()
                .load(noSkipDelegationAdvice.getClassLoader(), ClassLoadingStrategy.Default.CHILD_FIRST)
                .getLoaded();
        assertThat(type.getMethod(FOO).invoke(type.getConstructor().newInstance()), is(noSkipReturn));
    }

    public static class BooleanSample {

        public boolean foo() {
            return true;
        }
    }

    public static class BooleanSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static boolean enter() {
            return false;
        }
    }

    public static class BooleanNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static boolean enter() {
            return false;
        }
    }

    public static class BooleanSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static boolean enter() {
            return false;
        }
    }

    public static class BooleanNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static boolean enter() {
            return false;
        }
    }

    public static class ByteSample {

        public byte foo() {
            return 1;
        }
    }

    public static class ByteSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static byte enter() {
            return 0;
        }
    }

    public static class ByteNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static byte enter() {
            return 0;
        }
    }

    public static class ByteSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static byte enter() {
            return 0;
        }
    }

    public static class ByteNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static byte enter() {
            return 0;
        }
    }

    public static class ShortSample {

        public short foo() {
            return 1;
        }
    }

    public static class ShortSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static short enter() {
            return 0;
        }
    }

    public static class ShortNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static short enter() {
            return 0;
        }
    }

    public static class ShortSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static short enter() {
            return 0;
        }
    }

    public static class ShortNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static short enter() {
            return 0;
        }
    }

    public static class CharSample {

        public char foo() {
            return 1;
        }
    }

    public static class CharSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static char enter() {
            return 0;
        }
    }

    public static class CharNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static char enter() {
            return 0;
        }
    }

    public static class CharSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static char enter() {
            return 0;
        }
    }

    public static class CharNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static char enter() {
            return 0;
        }
    }

    public static class IntSample {

        public int foo() {
            return 1;
        }
    }

    public static class IntSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static int enter() {
            return 0;
        }
    }

    public static class IntNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static int enter() {
            return 0;
        }
    }

    public static class IntSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static int enter() {
            return 0;
        }
    }

    public static class IntNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static int enter() {
            return 0;
        }
    }

    public static class LongSample {

        public long foo() {
            return 1;
        }
    }

    public static class LongSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static long enter() {
            return 0;
        }
    }

    public static class LongNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static long enter() {
            return 0;
        }
    }

    public static class LongSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static long enter() {
            return 0;
        }
    }

    public static class LongNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static long enter() {
            return 0;
        }
    }

    public static class FloatSample {

        public float foo() {
            return 1;
        }
    }

    public static class FloatSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static float enter() {
            return 0;
        }
    }

    public static class FloatNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static float enter() {
            return 0;
        }
    }

    public static class FloatSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static float enter() {
            return 0;
        }
    }

    public static class FloatNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static float enter() {
            return 0;
        }
    }

    public static class DoubleSample {

        public double foo() {
            return 1;
        }
    }

    public static class DoubleSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static double enter() {
            return 0;
        }
    }

    public static class DoubleNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static double enter() {
            return 0;
        }
    }

    public static class DoubleSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static double enter() {
            return 0;
        }
    }

    public static class DoubleNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static double enter() {
            return 0;
        }
    }

    public static class ReferenceSample {

        public Object foo() {
            return FOO;
        }
    }

    public static class ReferenceSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static Object enter() {
            return null;
        }
    }

    public static class ReferenceNoSkipAdvice {

        @Advice.OnMethodExit
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static Object enter() {
            return null;
        }
    }

    public static class ReferenceSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar
        @Advice.AssignReturned.ToReturned
        public static Object enter() {
            return null;
        }
    }

    public static class ReferenceNoSkipDelegationAdvice {

        @Advice.OnMethodExit(inline = false)
        @Advice.AssignReturned.AsScalar(skipOnDefaultValue = false)
        @Advice.AssignReturned.ToReturned
        public static Object enter() {
            return null;
        }
    }
}

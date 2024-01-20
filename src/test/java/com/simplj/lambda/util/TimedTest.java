package com.simplj.lambda.util;

import com.simplj.lambda.util.Timed.TimedExecution;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class TimedTest {
    @Test
    public void testTimedExecute() throws Exception {
        TimedExecution<Integer> r = Timed.execute(this::delayedTime);
        assertEquals(1, (int) r.result());
        assertTrue(r.duration() >= 100);
        r = Timed.execute(Try.execute(this::delayedTime));
        assertEquals(1, (int) r.result());
        assertTrue(r.duration() >= 100);
    }

    @Test
    public void testTimedApply() {
        TimedExecution<Integer> r = Timed.apply(this::delayedTime);
        assertEquals(1, (int) r.result());
        assertTrue(r.duration() >= 100);
        TimedExecution<Either<Exception, Integer>> te = Timed.apply(Try.execute(this::delayedTime));
        assertTrue(te.result().isRight());
        assertEquals(1, (int) te.result().right());
        assertTrue(te.duration() >= 100);
        assertNotNull(te.toString());
    }

    private int delayedTime() {
        Try.execute(() -> TimeUnit.MILLISECONDS.sleep(100)).run();
        return 1;
    }
}

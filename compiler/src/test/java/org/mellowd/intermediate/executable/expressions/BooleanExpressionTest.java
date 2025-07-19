package org.mellowd.intermediate.executable.expressions;

import org.junit.jupiter.api.Test;
import org.mellowd.primitives.Pitch;
import org.mellowd.testutil.DummyEnvironment;

import java.util.Arrays;
import java.util.BitSet;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class BooleanExpressionTest {

    @Test
    public void shortCircuitAND() throws Exception {
        BitSet expressionsExecuted = new BitSet();
        List<Expression<Boolean>> exprs = Arrays.asList(
                Expression.lift(env -> {
                    expressionsExecuted.set(0);
                    return true;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(1);
                    return false;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(2);
                    return true;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(3);
                    return false;
                })
        );

        BooleanANDChain and = new BooleanANDChain(exprs);

        and.evaluate(DummyEnvironment.getInstance());

        String msg = "%s expression %s executed in ( true & false & true & false )";
        assertTrue(expressionsExecuted.get(0), String.format(msg, "First", "not"));
        assertTrue(expressionsExecuted.get(1), String.format(msg, "Second", "not"));
        assertFalse(expressionsExecuted.get(2), String.format(msg, "Third", "was"));
        assertFalse(expressionsExecuted.get(3), String.format(msg, "Fourth", "was"));
    }

    @Test
    public void shortCircuitOR() throws Exception {
        BitSet expressionsExecuted = new BitSet();
        List<Expression<Boolean>> exprs = Arrays.asList(
                Expression.lift(env -> {
                    expressionsExecuted.set(0);
                    return false;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(1);
                    return true;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(2);
                    return false;
                }),
                Expression.lift(env -> {
                    expressionsExecuted.set(3);
                    return true;
                })
        );

        BooleanORChain or = new BooleanORChain(exprs);

        or.evaluate(DummyEnvironment.getInstance());

        String msg = "%s expression %s executed in ( false | true | false | true )";
        assertTrue(expressionsExecuted.get(0), String.format(msg, "First", "not"));
        assertTrue(expressionsExecuted.get(1), String.format(msg, "Second", "not"));
        assertFalse(expressionsExecuted.get(2), String.format(msg, "Third", "was"));
        assertFalse(expressionsExecuted.get(3), String.format(msg, "Fourth", "was"));
    }

    @Test
    public void boolEvalBooleanTrue() throws Exception {
        Expression<Boolean> evalTrue = new BooleanEvaluationExpression(Expression.lift(env -> true));
        assertTrue(evalTrue.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanTrueBoxed() throws Exception {
        Expression<Boolean> evalTrueBox = new BooleanEvaluationExpression(Expression.lift(env -> Boolean.TRUE));
        assertTrue(evalTrueBox.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanFalse() throws Exception {
        Expression<Boolean> evalFalse = new BooleanEvaluationExpression(Expression.lift(env -> false));
        assertFalse(evalFalse.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalBooleanFalseBoxed() throws Exception {
        Expression<Boolean> evalFalseBox = new BooleanEvaluationExpression(Expression.lift(env -> Boolean.FALSE));
        assertFalse(evalFalseBox.evaluate(DummyEnvironment.getInstance()));
    }

    @Test
    public void boolEvalNumberLargerThan0() throws Exception {
        Expression<Boolean> evalInt = new BooleanEvaluationExpression(Expression.lift(env -> 10));
        Expression<Boolean> evalDouble = new BooleanEvaluationExpression(Expression.lift(env -> 30.0));
        Expression<Boolean> evalByte = new BooleanEvaluationExpression(Expression.lift(env -> (byte) 1));

        assertTrue(evalInt.evaluate(DummyEnvironment.getInstance()), "boolEval(10)");
        assertTrue(evalDouble.evaluate(DummyEnvironment.getInstance()), "boolEval(30.0)");
        assertTrue(evalByte.evaluate(DummyEnvironment.getInstance()), "boolEval((byte) 1)");
    }

    @Test
    public void boolEvalNumberEq0() throws Exception {
        Expression<Boolean> evalInt = new BooleanEvaluationExpression(Expression.lift(env -> 0));
        Expression<Boolean> evalDouble = new BooleanEvaluationExpression(Expression.lift(env -> 0.0));
        Expression<Boolean> evalByte = new BooleanEvaluationExpression(Expression.lift(env -> (byte) 0));

        assertFalse(evalInt.evaluate(DummyEnvironment.getInstance()), "boolEval(0)");
        assertFalse(evalDouble.evaluate(DummyEnvironment.getInstance()), "boolEval(0.0)");
        assertFalse(evalByte.evaluate(DummyEnvironment.getInstance()), "boolEval((byte) 0)");
    }

    @Test
    public void boolEvalPitchRest() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression(Expression.lift(env -> Pitch.REST));

        assertFalse(eval.evaluate(DummyEnvironment.getInstance()), "boolEval(Pitch.REST)");
    }

    @Test
    public void boolEvalPitch() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression(Expression.lift(env -> Pitch.A));

        assertTrue(eval.evaluate(DummyEnvironment.getInstance()), "boolEval(Pitch.A)");
    }

    @Test
    public void boolEvalNull() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression(Expression.lift(env -> null));

        assertFalse(eval.evaluate(DummyEnvironment.getInstance()), "boolEval(null)");
    }

    @Test
    public void boolEvalNonNullObject() throws Exception {
        Expression<Boolean> eval = new BooleanEvaluationExpression(Expression.lift(env -> new Object()));

        assertTrue(eval.evaluate(DummyEnvironment.getInstance()), "boolEval(new Object())");
    }


}
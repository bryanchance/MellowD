package org.mellowd.jupyter.live;

import org.mellowd.compiler.MellowD;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.Qualifier;
import org.mellowd.intermediate.executable.expressions.Abstraction;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.functions.Parameter;
import org.mellowd.intermediate.functions.Parameters;
import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.intermediate.variables.NullMemory;
import org.mellowd.plugin.MellowDPlugin;

public class Pick implements MellowDPlugin {
    public static final Qualifier NAMESPACE = Qualifier.fromString("pick");

    private static class Selector<T, U> implements Indexable<T, U> {
        private final Indexable<T, U> from;
        private int position;

        public Selector(Indexable<T, U> from, int position) {
            this.from = from;
            this.position = position;
        }

        public void shift() {
            position += 1;
        }

        @Override
        public T getAtIndex(int i) {
            return this.from.getAtIndex(this.position + i);
        }

        @Override
        public U getAtRange(int l, int h) {
            return this.from.getAtRange(this.position + l, this.position + h);
        }
    }

    @Override
    public void apply(MellowD mellowD) {
        Parameter<Indexable> fromParam = Parameter.newRequiredParameter("from", Indexable.class);
        Parameter<Integer> startParam = Parameter.newOptionalParameter("start", Integer.class);

        mellowD.getGlobals().set(NAMESPACE.qualify("cycle"),
                new Closure(NullMemory.getInstance(), new Abstraction(
                        new Parameters(fromParam, startParam), false,
                        Statement.lift((env, out) -> {
                            Indexable from = fromParam.dereference(env.getMemory());
                            Integer start = startParam.dereference(env.getMemory());

                            env.getMemory().set(Closure.RETURN_NAME, new Selector(from, start != null ? start : 0));
                        })
                )));

        Parameter<Selector> selectorParam = Parameter.newRequiredParameter("selector", Selector.class);
        mellowD.getGlobals().set(NAMESPACE.qualify("shift"),
                new Closure(NullMemory.getInstance(), new Abstraction(
                        new Parameters(selectorParam), false,
                        Statement.lift((env, out) -> {
                            Selector selector = selectorParam.dereference(env.getMemory());
                            selector.shift();
                        })
                )));
    }
}

package org.mellowd.jupyter.live;

import org.mellowd.compiler.MellowD;
import org.mellowd.intermediate.Closure;
import org.mellowd.intermediate.Qualifier;
import org.mellowd.intermediate.executable.expressions.Abstraction;
import org.mellowd.intermediate.executable.statements.Statement;
import org.mellowd.intermediate.functions.Parameter;
import org.mellowd.intermediate.functions.Parameters;
import org.mellowd.intermediate.functions.operations.Indexable;
import org.mellowd.intermediate.variables.Memory;
import org.mellowd.intermediate.variables.NullMemory;
import org.mellowd.plugin.MellowDPlugin;

import java.util.concurrent.ThreadLocalRandom;

public class Random implements MellowDPlugin {
    public static final Qualifier QUALIFIER = Qualifier.fromString("random");

    private static Abstraction makePick() {
        Parameter<Indexable> from = Parameter.newRequiredParameter("from", Indexable.class);
        Parameter<Integer> lower = Parameter.newRequiredParameter("start", Integer.class);
        Parameter<Integer> upper = Parameter.newRequiredParameter("end", Integer.class);

        Statement body = Statement.lift((env, out) -> {
            Memory locals = env.getMemory();
            Indexable list = from.dereference(locals);
            Integer low = lower.dereference(locals);
            Integer high = upper.dereference(locals);

            int index = ThreadLocalRandom.current().nextInt(low, high);

            locals.set(Closure.RETURN_NAME, list.getAtIndex(index));
        });

        return new Abstraction(new Parameters(from, lower, upper), false, body);
    }

    private final Abstraction pick;

    public Random() {
        this.pick = Random.makePick();
    }

    @Override
    public void apply(MellowD mellowD) {
        Memory globals = mellowD.getGlobals();

        globals.set(QUALIFIER.qualify("pick"), new Closure(NullMemory.getInstance(), this.pick));
    }
}

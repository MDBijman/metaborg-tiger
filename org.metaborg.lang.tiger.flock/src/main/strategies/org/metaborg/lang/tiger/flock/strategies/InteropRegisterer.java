package org.metaborg.lang.tiger.flock.strategies;

import org.metaborg.lang.tiger.flock.ae.AvailableExpressionsStrategies;
import org.metaborg.lang.tiger.flock.ast.flock_analyse_program_0_0;
import org.metaborg.lang.tiger.flock.ast.flock_debug_graph_0_0;
import org.metaborg.lang.tiger.flock.ast.flock_debug_graph_0_1;
import org.metaborg.lang.tiger.flock.ast.flock_make_id_0_0;
import org.metaborg.lang.tiger.flock.ast.flock_print_impl_0_0;
import org.metaborg.lang.tiger.flock.ast.flock_remove_node_0_0;
import org.metaborg.lang.tiger.flock.ast.flock_replace_node_impl_0_1;
import org.metaborg.lang.tiger.flock.impl.LiveVariablesStrategies;
import org.metaborg.lang.tiger.flock.value.FlowAnalysisStrategies;
import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

public class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(new Strategy[] {
    		flock_analyse_program_0_0.instance,
    		flock_replace_node_impl_0_1.instance,
    		flock_remove_node_0_0.instance,
    		flock_make_id_0_0.instance,
    		flock_debug_graph_0_0.instance,
    		flock_debug_graph_0_1.instance,
    		flock_print_impl_0_0.instance,
    		LiveVariablesStrategies.get_live_0_0.instance,
    		FlowAnalysisStrategies.get_values_0_0.instance,
    		AvailableExpressionsStrategies.get_expressions_0_0.instance,
        });
    }
}

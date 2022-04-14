package org.metaborg.lang.tiger.flock.strategies;

import org.strategoxt.lang.JavaInteropRegisterer;
import org.strategoxt.lang.Strategy;

public class InteropRegisterer extends JavaInteropRegisterer {
    public InteropRegisterer() {
        super(new Strategy[] {
    		flock_initialize_impl_0_0.instance,
    		flock_replace_node_impl_0_1.instance,
    		flock_remove_node_0_0.instance,
    		flock_make_id_0_0.instance,
    		flock_traverse_cfg_1_0.instance,
    		flock_reset_id_counter_0_0.instance,
    		flock_begin_timer_0_0.instance,
    		flock_end_timer_0_0.instance,
    		flock_debug_graph_0_0.instance,
    		flock_debug_termgraph_0_0.instance,
    		flock_debug_graph_0_1.instance,
    		flock_debug_graph_spt_0_0.instance,
    		flock_debug_runtime_0_0.instance,
    		flock_print_impl_0_0.instance,
    		flock_disable_timing_0_0.instance,
    		flock_enable_timing_0_0.instance,
    		flock_disable_logging_0_0.instance,
    		flock_enable_logging_0_0.instance,
    		flock_opt_3_0.instance,
    		get_live_0_0.instance,
    		get_values_0_0.instance,
    		get_values_0_1.instance,
    		get_expressions_0_0.instance,
        });
    }
}

package org.example;

import java.io.IOException;

import org.metaborg.core.MetaborgException;
import org.metaborg.core.context.IContext;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Param;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.spoofax.interpreter.terms.IStrategoTerm;

@State(Scope.Thread)
@Fork(jvmArgsAppend = { "-Xms4g", "-Xmx16g", "-Xss16m" })
public class BranchBenchmark extends BaseBenchmark {
	public IContext context;
	public IStrategoTerm ast;
	
	@Param({"1", "10", "100", "1000", "2000", "3000", "4000", "5000"})
	int count;
	
	@Setup
	public void setup() throws MetaborgException, IOException {
		ISpoofaxParseUnit tigerParseUnit = this.parseTiger("branches_" + count);
		context = this.getContext(tigerParseUnit);
		ast = tigerParseUnit.ast();
		this.doStrategyTransformation(context, ast, "disable-logs");
	}
	
	@Benchmark
	public IStrategoTerm run() throws MetaborgException {
		return this.doStrategyTransformation(context, ast, "pipeline");
	}	
}

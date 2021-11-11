/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.example;

import java.io.IOException;
import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.vfs2.FileObject;
import org.metaborg.core.MetaborgException;
import org.metaborg.core.action.EndNamedGoal;
import org.metaborg.core.context.ContextException;
import org.metaborg.core.context.IContext;
import org.metaborg.core.language.ILanguageImpl;
import org.metaborg.core.project.IProject;
import org.metaborg.core.project.ISimpleProjectService;
import org.metaborg.core.syntax.ParseException;
import org.metaborg.core.transform.TransformException;
import org.metaborg.spoofax.core.Spoofax;
import org.metaborg.spoofax.core.stratego.IStrategoCommon;
import org.metaborg.spoofax.core.transform.ISpoofaxTransformService;
import org.metaborg.spoofax.core.unit.ISpoofaxInputUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxParseUnit;
import org.metaborg.spoofax.core.unit.ISpoofaxTransformUnit;
import org.metaborg.util.concurrent.IClosableLock;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.spoofax.interpreter.terms.IStrategoTerm;

@State(Scope.Thread)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@Fork(jvmArgsAppend = { "-Xms512m", "-Xmx2g", "-Xss16m" })
public class BaseBenchmark {
	static final String RESOURCES_FOLDER = "resources";

	Spoofax spoofax;
	IProject project;
	ILanguageImpl language;

	@Setup
	public void setupSpoofax() throws MetaborgException {
		this.spoofax = new Spoofax();
		FileObject evaluationDir = spoofax.resourceService.resolve("./");
		FileObject languageDir = spoofax.resourceService.resolve("../org.metaborg.lang.tiger.flock");
		this.project = makeProject(spoofax, evaluationDir);
		this.language = spoofax.languageDiscoveryService.languageFromDirectory(languageDir);
	}

	protected String readFile(String filename) {
		FileObject file = spoofax.resourceService.resolve(RESOURCES_FOLDER + "/" + filename);
		String result = null;
		try {
			result = file.getContent().getString("UTF-8");
		} catch (Throwable e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return result;
	}

	protected IContext getContext(ISpoofaxParseUnit parseUnit) {
		IContext context = null;
		try {
			context = spoofax.contextService.get(parseUnit.source(), project, language);
		} catch (ContextException e1) {
			e1.printStackTrace();
		}
		return context;
	}
	
	protected ISpoofaxParseUnit parseTiger(String filename) {
		String tigerString = this.readFile("tiger/" + filename + ".tig");
		ISpoofaxInputUnit input = spoofax.unitService.inputUnit(tigerString, language, null);
		try {
			ISpoofaxParseUnit parseUnit = spoofax.syntaxService.parse(input);
			return parseUnit;
		} catch (ParseException e) {
			e.printStackTrace();
			System.exit(-1);
		}
		return null;
	}

	protected IStrategoTerm doEditorTransformation(ISpoofaxParseUnit input, String transformationName) {
		ISpoofaxTransformService transformService = spoofax.transformService;
		IContext context = null;
		try {
			context = spoofax.contextService.get(input.source(), project, language);
		} catch (ContextException e1) {
			e1.printStackTrace();
		}

		try (IClosableLock lock = context.read()) {
			try {
				Collection<ISpoofaxTransformUnit<ISpoofaxParseUnit>> x = transformService.transform(input, context,
						new EndNamedGoal(transformationName));

				for (ISpoofaxTransformUnit<ISpoofaxParseUnit> unit : x) {
					return unit.ast();
				}
			} catch (TransformException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	protected IStrategoTerm doStrategyTransformation(IContext context, IStrategoTerm input, String transformationName) throws MetaborgException {
		IStrategoCommon strategies = spoofax.strategoCommon;

		try (IClosableLock lock = context.read()) {
			return strategies.invoke(language, context, input, transformationName);
		}
	}

	private static IProject makeProject(Spoofax spoofax, FileObject location) {
		ISimpleProjectService projectService = (ISimpleProjectService) spoofax.projectService;
		IProject project;

		try {
			project = projectService.create(location);
		} catch (MetaborgException e) {
			e.printStackTrace();
			return null;
		}

		return project;
	}
}

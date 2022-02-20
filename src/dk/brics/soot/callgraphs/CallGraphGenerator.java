package dk.brics.soot.callgraphs;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CallGraphGenerator {
	public static void main(String[] args) throws IOException, InterruptedException {
		// Soot classpath
		String path = "exampleProjects/plantuml-master/target/classes";

		// Setting the classpath programmatically
		Options.v().set_prepend_classpath(true);
		Options.v().set_soot_classpath(path);

		Options.v().set_whole_program(true);
		Options.v().keep_line_number();
		Options.v().set_no_writeout_body_releasing(true);
		Options.v().set_app(true);
		Options.v().set_allow_phantom_refs(true);
		Options.v().set_output_format(Options.output_format_jimple);
		Options.v().set_src_prec(Options.src_prec_java);

		Main.main(new String[]{"-w", "-process-dir", path});
		Scene.v().loadNecessaryClasses();
		CallGraph cg = Scene.v().getCallGraph();
		HashSet<String> strings = new HashSet<>();

		// Create the file to write the call graph in text
		FileWriter myFileWriter = new FileWriter("graphVisualizationSoftware/g.gv");
		myFileWriter.write("digraph {\n");

		int edgeLimit = 0;

		callGraphProcess:
		for (SootClass sc : Scene.v().getApplicationClasses()) {
			for (SootMethod m : sc.getMethods()) {
				Iterator<MethodOrMethodContext> targets = new Targets(cg.edgesOutOf(m));
				while (targets.hasNext()) {
					SootMethod tgt = (SootMethod) targets.next();
					String call = m.getName() + " -> " + tgt.getName() + ";\n";
					if (!strings.contains(call)) {
						strings.add(call);
						myFileWriter.write(call);
						edgeLimit++;
						if (edgeLimit > 20){
							break callGraphProcess;
						}
					}
				}
			}
		}

		// Close the g.gv file
		myFileWriter.write("}");
		myFileWriter.close();

		// Process the g.gv file to create the graph image
		ProcessBuilder processBuilder = new ProcessBuilder();
		processBuilder.command("cmd.exe", "/c", "graphVisualizationSoftware\\dot.exe -Tpng graphVisualizationSoftware\\g.gv -o file.png");
		Process process = processBuilder.start();
		int exitVal = process.waitFor();
		if (exitVal == 0) {
			System.out.println("Call graph image generated.");
		} else {
			System.out.println("Call graph image was not generated.");
		}
	}
}

package dk.brics.soot.callgraphs;

import soot.*;
import soot.jimple.toolkits.callgraph.CallGraph;
import soot.jimple.toolkits.callgraph.Targets;
import soot.options.Options;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class CallGraphGenerator {
	public static void main(String[] args) throws IOException {
		// Soot classpath
		String path = "/Users/Paris/Downloads/plantuml-master/target/classes";

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

		// Create the folder for digraphsInText
		File theDir = new File("digraphsInText");
		if (!theDir.exists()){
			boolean success = theDir.mkdirs();
			if (!success){
				System.out.println("The directory for digraphs could not be created");
			}
		}

		// Create the file to write the call graph in text
		FileWriter myFileWriter = new FileWriter("digraphsInText/g.gv");
		myFileWriter.write("digraph {\n");

		int edgeLimit = 0;
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
						if (edgeLimit > 50){
							myFileWriter.write("}");
							myFileWriter.close();
							return;
						}
					}
				}
			}
		}
		myFileWriter.write("}");
		myFileWriter.close();
	}
}

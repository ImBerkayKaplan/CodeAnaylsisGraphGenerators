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
	public static void main(String[] args) throws InterruptedException, IOException {

		// Soot classpath
		String path = "";
		String project = args[0];
		String[] folders = (new File("exampleProjects/" + project)).list();
		List<String> foldersAsList = new ArrayList<>(Arrays.asList(Objects.requireNonNull(folders)));
		if (foldersAsList.contains("target")) {
			path = "exampleProjects/" + project + "/target/classes";
		}else if(foldersAsList.contains("build")){
			path = "exampleProjects/" + project + "/build/classes";
		}

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
		FileWriter myFileWriter = new FileWriter("graphVisualizationSoftware/" + project + ".gv");
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
		processBuilder.command("cmd.exe", "/c", "graphVisualizationSoftware\\dot.exe -Tpng graphVisualizationSoftware\\" + project + ".gv -o graphImages\\" + project + ".png");
		Process process = processBuilder.start();
		process.waitFor();
	}
}

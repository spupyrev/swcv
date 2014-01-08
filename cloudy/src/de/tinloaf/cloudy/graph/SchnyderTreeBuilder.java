package de.tinloaf.cloudy.graph;

import de.tinloaf.cloudy.utils.Logger;

import org.jgrapht.ext.EdgeNameProvider;
import org.jgrapht.ext.GmlExporter;
import org.jgrapht.ext.VertexNameProvider;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Iterator;

public class SchnyderTreeBuilder {
	private WordGraph g;
	private WordGraph backgroundGraph;
	
	private static final String GMLFILE = "tmp/graph.gml";
	private static final String GMLFILE_OUT = "tmp/graph-out.gml";
	private static final String ANNOTATION_FILE = "tmp/annotation.txt";
	private static final String PATH_TO_EXEC = "tmp/schnyder.exe";
	
	public WordGraph tree1 = null; 
	public WordGraph tree2 = null;
	public WordGraph tree3 = null;
	
	public SchnyderTreeBuilder(WordGraph g, WordGraph backgroundGraph) {
		this.g = g;
		this.backgroundGraph = backgroundGraph;
	}
	
	private class VertexLabelProvider implements VertexNameProvider<Vertex> {
		private HashMap<Vertex,Integer> vIDs;
		
		VertexLabelProvider(HashMap<Vertex,Integer> map) {
			this.vIDs = map;
		}
		
		@Override
		public String getVertexName(Vertex arg0) {
			return Integer.toString(this.vIDs.get(arg0));
		}
		
	}
	
	private class EdgeLabelProvider implements EdgeNameProvider<Edge> {
		private HashMap<Edge,Integer> eIDs;
		
		EdgeLabelProvider(HashMap<Edge,Integer> map) {
			this.eIDs = map;
		}
		
		@Override
		public String getEdgeName(Edge arg0) {
			return Integer.toString(this.eIDs.get(arg0));
		}
		
	}
	
	public void run() {
		// First, build ids for vertices and edges
		HashMap<Vertex, Integer> vIDs = new HashMap<Vertex,Integer>();
		HashMap<Edge, Integer> eIDs = new HashMap<Edge,Integer>();
		HashMap<Integer, Edge> edgeReverse = new HashMap<Integer,Edge>();
		HashMap<Integer, Vertex> vertexReverse = new HashMap<Integer, Vertex>();
		
		// prepare the trees
		this.tree1 = (WordGraph)this.g.clone();
		this.tree2 = (WordGraph)this.g.clone();
		this.tree3 = (WordGraph)this.g.clone();
		this.tree1.removeAllEdges(this.g.edgeSet());
		this.tree2.removeAllEdges(this.g.edgeSet());
		this.tree3.removeAllEdges(this.g.edgeSet());
		
		int i = 1;
		for (Vertex w: this.g.vertexSet()) {
			vIDs.put(w, new Integer(i));
			vertexReverse.put(i, w);
			i++;
		}
		
		i = 0;
		for(Edge e: this.g.edgeSet()) {
			eIDs.put(e, new Integer(i));
			edgeReverse.put(i, e);
			i++;
		}
		
		// Now, export to GML
		VertexLabelProvider vlp = new VertexLabelProvider(vIDs);
		EdgeLabelProvider elp = new EdgeLabelProvider(eIDs);
		GmlExporter<Vertex,Edge> exporter = new GmlExporter<Vertex,Edge>(vlp, vlp, elp, elp);
		exporter.setPrintLabels(GmlExporter.PRINT_EDGE_LABELS | GmlExporter.PRINT_EDGE_VERTEX_LABELS);
		
		FileWriter outputWriter;
		try {
			outputWriter = new FileWriter(SchnyderTreeBuilder.GMLFILE);
			exporter.export(outputWriter, this.g);
			
			// Run!
			Runtime r = Runtime.getRuntime();
			Process p = r.exec(new String[] {SchnyderTreeBuilder.PATH_TO_EXEC, 
					SchnyderTreeBuilder.GMLFILE,
					SchnyderTreeBuilder.GMLFILE_OUT,
					SchnyderTreeBuilder.ANNOTATION_FILE});
			
			BufferedReader stdoutReader = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line = null;
			while ((line = stdoutReader.readLine()) != null) {
				Logger.log(line + "\n");
			}
			
			p.waitFor();
			
			// Read the file back in
			BufferedReader reader = new BufferedReader(new FileReader(SchnyderTreeBuilder.ANNOTATION_FILE));
			while ((line = reader.readLine()) != null) {
				String[] parts = line.split("\\|");
				
				if ((parts.length == 1) && (parts[0].length() == 0)) {
					continue;
				}
				
				if (parts.length != 3) {
					Logger.log("'" + parts[0]+ "'\n");
					throw new IllegalArgumentException("File corrupted");
				}
				
				Integer startVertexID = Integer.parseInt(parts[0].trim());
				Integer endVertexID = Integer.parseInt(parts[1].trim());
				Integer treeID = Integer.parseInt(parts[2].trim());
				
				Edge e;
				// Find the edge
				Vertex start = vertexReverse.get(startVertexID);
				Vertex end = vertexReverse.get(endVertexID);
				
				if (this.g.containsEdge(start, end)) {
					e = this.g.getEdge(start, end);
				} else {
					e = this.backgroundGraph.getEdge(start, end);
				}
				
				switch (treeID) {
				case 1:
					this.tree1.addEdge(start, 
							end, e);
					break;
				case 2:
					this.tree2.addEdge(start, 
							end, e);
					break;
				case 3:
					this.tree3.addEdge(start, 
							end, e);
					break;
				default:
					throw new IllegalArgumentException("Corrupted fiel");
				}
			}
			reader.close();
			
			// Degree 0 vertices are evil. connect them to a random vertex.
			// tree 1
			// TODO use heaviest adjacency?
			for (Vertex v: this.tree1.vertexSet()) {
				if (this.tree1.degreeOf(v) == 0) {
					Iterator<Vertex> vIt = this.tree1.vertexSet().iterator();					
					Vertex other = vIt.next();
					
					while (vIt.hasNext() && (this.tree1.degreeOf(other) == 0)) {
						other = vIt.next();
					}
					
					if (other != v) { // 1-vertex tree	
						Edge dummyEdge = this.backgroundGraph.getEdge(v, other);
						this.tree1.addEdge(v, other, dummyEdge);
					}
				}
			}
			
			for (Vertex v: this.tree2.vertexSet()) {
				if (this.tree2.degreeOf(v) == 0) {
					Iterator<Vertex> vIt = this.tree2.vertexSet().iterator();					
					Vertex other = vIt.next();
					
					while (vIt.hasNext() && (this.tree2.degreeOf(other) == 0)) {
						other = vIt.next();
					}
					
					if (other != v) { // 1-vertex tree	
						Edge dummyEdge = this.backgroundGraph.getEdge(v, other);
						this.tree2.addEdge(v, other, dummyEdge);
					}
				}
			}

			
			for (Vertex v: this.tree3.vertexSet()) {
				if (this.tree3.degreeOf(v) == 0) {
					Iterator<Vertex> vIt = this.tree3.vertexSet().iterator();					
					Vertex other = vIt.next();
					
					while (vIt.hasNext() && (this.tree3.degreeOf(other) == 0)) {
						other = vIt.next();
					}
					
					if (other != v) { // 1-vertex tree	
						Edge dummyEdge = this.backgroundGraph.getEdge(v, other);
						this.tree3.addEdge(v, other, dummyEdge);
					}
				}
			}
			
			// TODO remove!
			Logger.log("Tree 1\n");
			for (Edge e : this.tree1.edgeSet()) {
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeSource(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeSource(e).word + ")");
				Logger.log("-");
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeTarget(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeTarget(e).word + ")");
				Logger.log(" ");
			}
			Logger.log("=========\n");
			
			// TODO remove!
			Logger.log("Tree 2\n");
			for (Edge e : this.tree2.edgeSet()) {
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeSource(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeSource(e).word + ")");
				Logger.log("-");
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeTarget(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeTarget(e).word + ")");
				Logger.log(" ");
			}
			Logger.log("=========\n");
			
			// TODO remove!
			Logger.log("Tree 3\n");
			for (Edge e : this.tree3.edgeSet()) {
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeSource(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeSource(e).word + ")");
				Logger.log("-");
				Logger.log(Integer.toString(vIDs.get(this.backgroundGraph.getEdgeTarget(e))));
				Logger.log("(" + this.backgroundGraph.getEdgeTarget(e).word + ")");
				Logger.log(" ");
			}
			Logger.log("=========\n");
			
		} catch (IOException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
}

package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphConverter;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;

import java.util.List;

/**
 * this class is just for testing implementations of ParallelFOFC
 * Created by Erich on 9/21/2016.
 */
public class TestParallelFOFC {
    public static void main(String... args){
        //simple factor model with 2 clusters of 4 measures
        Graph structure = GraphConverter.convert("L0-->X0,L0-->X2,L0-->X4,L0-->X6,X6-->X8," +
                "L0-->X8,L0-->X10,L1-->X1,L1-->X3,L1-->X5,L1-->X7,L1-->X9,L1-->X11,L0-->L1");
        //make L0 and L1 latent
        structure.getNode("L0").setNodeType(NodeType.LATENT);
        structure.getNode("L1").setNodeType(NodeType.LATENT);
        //generate data from a set seed
        SemPm sempm = new SemPm(structure);
        SemIm semim = new SemIm(sempm);
        int samplesize = 1000;
        DataSet data = semim.simulateData(samplesize, (long) 2,false);
        //System.out.println(data);

        long startTime = System.nanoTime();
        FindOneFactorClusters fofc = new FindOneFactorClusters(data,TestType.TETRAD_WISHART,
                FindOneFactorClusters.Algorithm.GAP,(double) 1/samplesize);
        fofc.search();
        List<List<Node>> clusters = fofc.getClusters();
        long endTime = System.nanoTime();
        System.out.println(clusters);
        System.out.println("executed in: " + (endTime - startTime));

        startTime = System.nanoTime();
        ParallelFOFC pFOFC = new ParallelFOFC(data,TestType.TETRAD_WISHART,
                ParallelFOFC.Algorithm.GAP,(double) 1/samplesize);
        pFOFC.search();
        List<List<Node>> pClusters = pFOFC.getClusters();
        System.out.println(pClusters);
        endTime = System.nanoTime();
        System.out.println("executed in: " + (endTime - startTime));


    }
}

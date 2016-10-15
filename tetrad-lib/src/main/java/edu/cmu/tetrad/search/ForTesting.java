package edu.cmu.tetrad.search;

import edu.cmu.tetrad.data.DataSet;
import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.Node;
import edu.cmu.tetrad.graph.NodeType;
import edu.cmu.tetrad.sem.SemIm;
import edu.cmu.tetrad.sem.SemPm;
import edu.cmu.tetrad.simulation.HsimUtils;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Created by Erich on 9/26/2016.
 */
public class ForTesting {
    public static void main(String... args){
        //assign parameter values for simulation test, e.g. numbers of latents and measures
        List<Integer> numLatents = Arrays.asList(2);//the number of latent variables
        List<Integer> numMeasuresPerLatent = Arrays.asList(4);//the number of measures per latent
        int numIMsPerStructure = 10;//the number of IMs per structure

        //initialize some things for use later on
        List<long[]> timestats = new ArrayList<>();
        //iterate through process creating structures according to above parameters
        for (Integer numLat : numLatents){
            for (Integer cSize : numMeasuresPerLatent){
                System.out.println("numLat: "+numLat+" cSize: "+cSize);
                int[] strucArray = new int[numLat];
                for (int i=0;i<strucArray.length;i++){
                    strucArray[i]=cSize;
                }
                Graph structure = FactorModelUtils.makePure1FactorModel(strucArray);
                //System.out.println(structure);
                for (int i=0;i<numLat;i++){
                    String nodename = "L"+i;
                    structure.getNode(nodename).setNodeType(NodeType.LATENT);
                }
                //initialize lists for averaging later
                List<Long> timesF = new ArrayList<Long>();
                List<Long> timesP = new ArrayList<Long>();
                //for each structure, parameterize it and create data a few times
                System.out.print("k=");
                for (int k=0;k<numIMsPerStructure;k++){
                    System.out.print(k);
                    SemPm sempm = new SemPm(structure);
                    SemIm semim = new SemIm(sempm);
                    int samplesize = 1000;
                    DataSet data = semim.simulateData(samplesize, false);
                    //run FOFC and ParallelFOFC on the data set. time each one.
                    long startTime = System.nanoTime();
                    FindOneFactorClusters fofc = new FindOneFactorClusters(data,TestType.TETRAD_WISHART,
                            FindOneFactorClusters.Algorithm.GAP,(double) 1/samplesize);
                    fofc.search();
                    List<List<Node>> fClusters = fofc.getClusters();
                    long endTime = System.nanoTime();
                    //System.out.println(fClusters);
                    //long durationF = endTime-startTime;
                    timesF.add(endTime-startTime);
                    //next is ParallelFOFC
                    startTime = System.nanoTime();
                    ParallelFOFC pFOFC = new ParallelFOFC(data,TestType.TETRAD_WISHART,
                            ParallelFOFC.Algorithm.GAP,(double) 1/samplesize);
                    pFOFC.search();
                    List<List<Node>> pClusters = pFOFC.getClusters();
                    endTime = System.nanoTime();
                    //System.out.println(pClusters);
                    //long durationP = endTime-startTime;
                    timesP.add(endTime-startTime);
                }
                System.out.println(" ");
                //average together the times for each structure
                long totalF=0;
                long totalP=0;
                for (Long time : timesF){
                    totalF+=time;
                }
                for (Long time : timesP){
                    totalP+=time;
                }
                timestats.add(new long[]{(long)numLat,(long)cSize,totalF/numIMsPerStructure,
                        totalP/numIMsPerStructure});
            }
        }
        //turn the data in timestats into a table for viewing
        //first turn the data into an appropriately formatted String[][] array
        //this matrix has 4 columns and as many rows as entries in timestats
        String[][] timestatsTableArray = new String[timestats.size()+1][4];
        timestatsTableArray[0][0] = "numLat";
        timestatsTableArray[0][1] = "cSize";
        timestatsTableArray[0][2] = "F_avTime";
        timestatsTableArray[0][3] = "P_avTime";
        int i = 0;
        for (long[] entry : timestats){
            i++;
            timestatsTableArray[i][0] = String.format("%7d", entry[0]);
            timestatsTableArray[i][1] = String.format("%7d", entry[1]);
            timestatsTableArray[i][2] = String.format("%7.2e",(double) entry[2]);
            timestatsTableArray[i][3] = String.format("%7.2e",(double) entry[3]);
        }
        //then turn this matrix into a latex formatted table with my handy utility
        String toFile = HsimUtils.makeLatexTable(timestatsTableArray);

        //finally, print this to a file
        try {
            PrintWriter writer = new PrintWriter("SpeedtestingTable-FvP.txt", "UTF-8");
            writer.println(toFile);
            writer.close();
        }
        catch(Exception IOException){
            IOException.printStackTrace();
        }
    }
}

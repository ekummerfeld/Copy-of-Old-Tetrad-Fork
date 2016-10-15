package edu.cmu.tetrad.search;

import edu.cmu.tetrad.graph.Graph;
import edu.cmu.tetrad.graph.GraphConverter;

/**
 * This class includes some basic utilities used for studying factor model algorithms such as FOFC
 *
 * Created by Erich on 9/26/2016.
 */
public class FactorModelUtils {
    public static Graph makePure1FactorModel(int[] clusters){
        //initialize some things
        String alledges = "";
        int mCount = 0;
        String latentname;
        String measurename;
        String edge = "-->";
        String comma = ",";
        int totalmeasures = 0;
        //calculate total number of measures, for use later
        for (int i=0;i<clusters.length;i++){
            totalmeasures += clusters[i];
        }
        //iterate through the latents
        for (int i=0;i<clusters.length;i++){
            latentname = "L"+i;
            //iterate through the measures in that latent
            for (int j=0;j<clusters[i];j++){
                mCount++;
                measurename = "X"+mCount;
                alledges=alledges+latentname+edge+measurename;
                if (mCount<totalmeasures) alledges=alledges+comma;
            }
        }
        //add edges amongst the latents so that they are all correlated.
        for (int i=1;i<clusters.length;i++){
            alledges=alledges+comma+"L"+(i-1)+edge+"L"+i;
        }
        //System.out.println(alledges);
        return GraphConverter.convert(alledges);
    }
}

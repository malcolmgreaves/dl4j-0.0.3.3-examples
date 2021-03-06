package org.deeplearning4j.deepbelief;


import org.deeplearning4j.datasets.iterator.DataSetIterator;
import org.deeplearning4j.datasets.iterator.MultipleEpochsIterator;
import org.deeplearning4j.datasets.iterator.impl.MnistDataSetIterator;
import org.deeplearning4j.eval.Evaluation;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.layers.RBM;
import org.deeplearning4j.nn.conf.override.ClassifierOverride;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.deeplearning4j.optimize.api.IterationListener;
import org.deeplearning4j.optimize.listeners.ScoreIterationListener;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.lossfunctions.LossFunctions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Collections;


/**
 * Created by agibsonccc on 9/11/14.
 *
 * Diff from small conducts pretraining+fine-tuning on every single batch
 */
public class DBNMnistReconstructExample {

    private static Logger log = LoggerFactory.getLogger(DBNMnistReconstructExample.class);

    public static void main(String[] args) throws Exception {

        log.info("Load data....");
        DataSetIterator iter = new MnistDataSetIterator(100,1000);

        log.info("Build model....");
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .layer(new RBM())
                .nIn(784)
                .nOut(10)
                .weightInit(WeightInit.VI)
                .constrainGradientToUnitNorm(true)
                .iterations(5)
                .lossFunction(LossFunctions.LossFunction.RMSE_XENT)
                .learningRate(1e-1f)
                .list(4)
                .hiddenLayerSizes(new int[]{600, 500, 400})
                .override(3,new ClassifierOverride())
                .build();
        MultiLayerNetwork model = new MultiLayerNetwork(conf);
        model.init();
        Collections.singletonList((IterationListener) new ScoreIterationListener(1));

        log.info("Train model....");

        while(iter.hasNext()) {
            DataSet mnist = iter.next();
            mnist.normalizeZeroMeanZeroUnitVariance();
            model.fit(mnist);
        }
        iter.reset();

        log.info("Evaluate model....");
        Evaluation eval = new Evaluation();
        while(iter.hasNext()) {
            DataSet testData = iter.next();
            testData.normalizeZeroMeanZeroUnitVariance();
            INDArray predict2 = model.output(testData.getFeatureMatrix());
            eval.eval(testData.getLabels(), predict2);
        }

        log.info(eval.stats());
        log.info("****************Example finished********************");

    }

}

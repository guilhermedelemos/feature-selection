package io.github.guilhermedelemos.fs;

import io.github.guilhermedelemos.Log;
import io.github.guilhermedelemos.fs.gui.AppFx;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;

import java.io.File;

public class App {
    public String getGreeting() {
        return "FEATURE SELECTION";
    }

    public static void main(String[] args) {
        if(args.length == 0) {
            App app = new App();
            app.executeStandard();
        } else {
            if(args[0].equals("GUI")) {
                AppFx.main(args);
            } else if(args[0].equals("E")) {
                App app = new App();
                app.executeEvaluation();
            } else if(args[0].equals("B")) {
                App app = new App();
                app.executeBruteForce();
            } else {
                App app = new App();
                app.executeStandard();
            }
        }
    }

    public void executeStandard() {
        String learningDataset = "dataset" + File.separator + "learning-dataset.arff";
        String testDataset = "dataset" + File.separator + "test-dataset.arff";
        System.out.println(new App().getGreeting());
        Log log = new Log();
        for(int i=0; i<2; i++) {
            FeatureSelection fs = new FeatureSelection(log, learningDataset, testDataset);
            fs.setMaxGen(1000);
            fs.setCrossoverConstant(0.3);
            fs.setMutationConstant(0.1);
            if (i == 0) {
                fs.setClassifier(new J48());
            } else {
                fs.setClassifier(new RandomForest());
            }
            fs.run();
        }
    }

    public void executeEvaluation() {
        String learningDataset = "dataset" + File.separator + "learning-dataset.arff";
        String testDataset = "dataset" + File.separator + "test-dataset.arff";
        System.out.println(new App().getGreeting());
        Log log = new Log();
        for(int i=0; i<2; i++) {
            for(int y=1;y<10;y++) {
                for(int z=1;z<10;z++) {
                    double a = (double)y/10;
                    double b = (double)z/10;
                    System.out.println("["+a+","+b+"]");
                    for(int j=0;j<10;j++) {
                        String fileName = "";
                        if(i == 0) {
                            fileName = "DT-";
                        } else {
                            fileName = "RF-";
                        }
                        fileName += "CR"+y+"MU"+z;

                        Log logFitness = new Log(fileName+"-fitness.csv");
                        Log logImprovement = new Log(fileName+"-improvement.csv");
                        FeatureSelection fs = new FeatureSelection(log, learningDataset, testDataset);
                        fs.setLogFitness(logFitness);
                        fs.setLogImprovement(logImprovement);
                        fs.setCrossoverConstant(a);
                        fs.setMutationConstant(b);
                        if (i == 0) {
                            fs.setClassifier(new J48());
                        } else {
                            fs.setClassifier(new RandomForest());
                        }
                        fs.run();
                    }
                }
            }
            break;
        }
    }

    public void executeBruteForce() {
        String learningDataset = "dataset" + File.separator + "learning-dataset.arff";
        String testDataset = "dataset" + File.separator + "test-dataset.arff";
        System.out.println(new App().getGreeting());
        Log log = new Log();
        for(int i=0; i<2; i++) {
            FeatureSelection fs = new FeatureSelection(log, learningDataset, testDataset);
            fs.setMaxGen(1);
            fs.setCrossoverConstant(0.3);
            fs.setMutationConstant(0.1);
            if (i == 0) {
                fs.setClassifier(new J48());
            } else {
                fs.setClassifier(new RandomForest());
            }
            //fs.runBruteForce();
            fs.runBruteForceMemory();
        }
    }

}

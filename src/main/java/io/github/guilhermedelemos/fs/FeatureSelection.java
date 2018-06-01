package io.github.guilhermedelemos.fs;

import io.github.guilhermedelemos.Log;
import weka.classifiers.Classifier;
import weka.classifiers.Evaluation;
import weka.classifiers.trees.J48;
import weka.classifiers.trees.RandomForest;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Remove;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class FeatureSelection {
    private Log log;
    private Log logFitness;
    private Log logImprovement;
    private String learningDataset;
    private String testDataset;
    private List<Element> population; // P
    private int populationSize; // NP
    private int dimension; // D = chromosome size
    private double crossoverConstant; // CR
    private double mutationConstant; // F
    private int maxGen; //
    private double initialFitness;
    private double improvement;
    private Classifier classifier;
    private String relationName;
    private int countFiles;

    public FeatureSelection() {
        super();
        this.loadDefaultValues();
    }

    public FeatureSelection(Log log, String learningDataset, String testDataset) {
        this.loadDefaultValues();
        this.log = log;
        this.learningDataset = learningDataset;
        this.testDataset = testDataset;
    }

    private void loadDefaultValues() {
        this.populationSize = 10;
        this.dimension = 25;
        this.crossoverConstant = 0.5;
        this.mutationConstant = 0.1;
        this.maxGen = 100;
        this.learningDataset = "";
        this.testDataset = "";
        this.initialFitness = 0.0;
        this.classifier = new J48();
        this.relationName = "";
        this.improvement = 0;
        this.countFiles = 1;
    }

    public void run() {
        this.analyseData();
        this.welcomeMessage();
        this.population = this.generateRandomizedPopulation(this.populationSize, this.dimension);
        List<Element> knownSolutions = new ArrayList<>();
        this.calculatePopulationFitness(this.population);
        boolean stopCriteria = false;
        int generation = 1;
        String logFitness = "";
        do {
            this.log.add("Generation #" + generation);
            this.population.sort(Comparator.comparing(Element::getFitness).reversed());
            this.log.add("POPULATION");
            this.printPopulation(this.population);

            //  SELECTION (tournament)
            Element parent1 = null;
            Element parent2 = null;
            if (knownSolutions.size() > 2) {
                parent1 = this.fittest(knownSolutions);
                parent2 = this.fittest(knownSolutions);
            } else {
                parent1 = this.fittest(this.population);
                parent2 = this.fittest(this.population);
            }

            //  CROSSOVER
            List<Element> newGeneration = this.crossover(parent1, parent2, this.crossoverConstant);

            //  MUTATION
            Element child1M = this.mutation(newGeneration.get(0), this.mutationConstant);
            child1M.setFitness(this.calculateFitness(child1M));

            Element child2M = this.mutation(newGeneration.get(1), this.mutationConstant);
            child2M.setFitness(this.calculateFitness(child2M));

            this.log.add("E(g+1)' : " + child1M.toString() + " FITNESS: " + child1M.getFitness());
            this.log.add("E(g+1)'': " + child2M.toString() + " FITNESS: " + child2M.getFitness());

            Element best = this.localSearch(knownSolutions, child1M);
            if (child2M.getFitness() > best.getFitness()) {
                best = child2M;
                if (!knownSolutions.contains(child1M)) {
                    knownSolutions.add(child1M);
                }
            } else {
                if (!knownSolutions.contains(child2M)) {
                    knownSolutions.add(child2M);
                }
                if (best != child1M) {
                    if (!knownSolutions.contains(child1M)) {
                        knownSolutions.add(child1M);
                    }
                }
            }

            if (best.getFitness() > this.population.get(this.population.size() - 1).getFitness()) {
                Element worst1 = this.population.remove(this.populationSize - 1);
                this.population.add(best);
                logFitness += ";" + best.getFitness();
                if (!knownSolutions.contains(worst1)) {
                    knownSolutions.add(worst1);
                }
            } else {
                if (!knownSolutions.contains(best)) {
                    knownSolutions.add(best);
                }
            }

            generation++;
            stopCriteria = generation > this.maxGen;
        } while (!stopCriteria);
        this.population.sort(Comparator.comparing(Element::getFitness).reversed());
        this.improvement = this.population.get(0).getFitness() - this.initialFitness;
        this.log.add("BEST FEATURE SET: " + this.population.get(0).toString() + " FITNESS: " + this.population.get(0).getFitness());
        this.log.add("IMPROVEMENT: " + this.improvement);
        this.logImprovement(this.improvement);
        this.logFitness(logFitness);
        this.goodbyeMessage();
    }

    private Element fittest(List<Element> set) {
        if (set.size() < 2) {
            return null;
        }
        Element fittest = null;
        int random1 = ThreadLocalRandom.current().nextInt(0, set.size() - 1);
        int random2 = ThreadLocalRandom.current().nextInt(0, set.size() - 1);
        while (random1 == random2) {
            random2 = ThreadLocalRandom.current().nextInt(0, set.size() - 1);
        }
        if (set.get(random1).getFitness() > set.get(random2).getFitness()) {
            fittest = set.get(random1);
        } else {
            fittest = set.get(random2);
        }
        return fittest;
    }

    private void analyseData() {
        try {
            DataSource learningDataSource = new DataSource(this.learningDataset);
            Instances learningData = learningDataSource.getDataSet();
            if (learningData.classIndex() == -1) {
                learningData.setClassIndex(learningData.numAttributes() - 1);
            }

            DataSource testDataSource = new DataSource(this.testDataset);
            Instances testData = testDataSource.getDataSet();
            if (testData.classIndex() == -1) {
                testData.setClassIndex(testData.numAttributes() - 1);
            }
            Evaluation eval = this.evaluate(learningData, testData);
            this.initialFitness = eval.pctCorrect() / 100;
            this.setDimension(testData.numAttributes() - 1);
            this.setRelationName(testData.relationName());
        } catch (Exception e) {

        }
    }

    private Element localSearch(List<Element> populacao, Element target) {
        Element best = target;
        Iterator<Element> it = populacao.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            if (e.getFitness() > best.getFitness()) {
                best = e;
            }
        }
        return best;
    }

    private Evaluation decisionTree(Instances learningData, Instances testData) {
        Evaluation eval = null;
        try {
            Classifier cls = new J48();
            cls.buildClassifier(learningData);
            eval = new Evaluation(learningData);
            eval.evaluateModel(cls, testData);
        } catch (Exception e) {
            return null;
        }
        return eval;
    }

    private Evaluation randomForest(Instances learningData, Instances testData) {
        Evaluation eval = null;
        try {
            Classifier cls = new RandomForest();
            cls.buildClassifier(learningData);
            eval = new Evaluation(learningData);
            eval.evaluateModel(cls, testData);
        } catch (Exception e) {
            return null;
        }
        return eval;
    }

    private List<Element> crossover(Element parent1, Element parent2, double crossoverConstant) {
        List newGeneration = new ArrayList();
        int cut = ThreadLocalRandom.current().nextInt(0, this.dimension - 1);
        int[] offspring1 = new int[parent1.getChromosome().length];
        int[] offspring2 = new int[parent1.getChromosome().length];
        boolean applyCrossover = ThreadLocalRandom.current().nextDouble() >= crossoverConstant;
        for (int i = 0; i < parent1.getChromosome().length; i++) {
            if (applyCrossover) {
                if (i <= cut) {
                    offspring1[i] = parent1.getChromosome()[i];
                    offspring2[i] = parent2.getChromosome()[i];
                } else {
                    offspring1[i] = parent2.getChromosome()[i];
                    offspring2[i] = parent1.getChromosome()[i];
                }
            } else {
                offspring1[i] = parent1.getChromosome()[i];
                offspring2[i] = parent2.getChromosome()[i];
            }
        }
        newGeneration.add(new Element(offspring1));
        newGeneration.add(new Element(offspring2));
        return newGeneration;
    }

    private Element mutation(Element e, double mutationConstant) {
        for (int i = 0; i < e.getChromosome().length; i++) {
            if (ThreadLocalRandom.current().nextDouble() <= mutationConstant) {
                if (e.getChromosome()[i] == 0) {
                    e.getChromosome()[i] = 1;
                } else {
                    e.getChromosome()[i] = 0;
                }
            }
        }
        return e;
    }

    private List<Element> generateRandomizedPopulation(int populationSize, int chromosomeSize) {
        List<Element> population = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            int[] chromosome = new int[chromosomeSize];
            for (int j = 0; j < chromosomeSize; j++) {
                chromosome[j] = ThreadLocalRandom.current().nextInt(0, 2);
            }
            Element e = new Element();
            e.setChromosome(chromosome);
            population.add(e);
        }
        return population;
    }

    public void runBruteForce() {
        this.analyseData();
        this.welcomeMessage();
        this.population = this.generateAllPossibleCombinationsPopulation(this.dimension);
        this.calculatePopulationFitness(this.population);
        this.printPopulation(this.population);
        this.goodbyeMessage();
    }

    public void runBruteForceMemory() {
        this.analyseData();
        this.welcomeMessage();
        //this.population = this.generateAllPossibleCombinationsPopulation(this.dimension);
        List<String> values = new ArrayList<>();
        values.add("0");
        values.add("1");
        Combinations c = new Combinations();
        //List<String[]> chromosomes = c.generateCombinations(this.dimension, values);
        int carry = -1;
        int[] indices = new int[this.dimension];
        do {
            String[] result = new String[this.dimension];
            int idx = 0;
            for (int index : indices) {
                result[idx++] = values.get(index);
                //System.out.print(possibleValues.get(index) + " ");
            }
            //combinations.add(result);
            //System.out.println("");

            String[] ch = result;
            int[] chInt = new int[ch.length];
            int countZero = 0;
            for (int j = 0; j < ch.length; j++) {
                chInt[j] = Integer.valueOf(ch[j]);
                if (chInt[j] == 0) {
                    countZero++;
                }
            }
            if (countZero != this.dimension) {
                Element e = new Element(chInt);
                e.setFitness(this.calculateFitness(e));
                this.log.add("Element [" + this.countFiles + "]: " + e.toString() + " FITNESS: " + e.getFitness());
            }

            carry = 1;
            for (int i = indices.length - 1; i >= 0; i--) {
                if (carry == 0)
                    break;

                indices[i] += carry;
                carry = 0;

                if (indices[i] == values.size()) {
                    carry = 1;
                    indices[i] = 0;
                }
            }
        }
        while (carry != 1); // Call this method iteratively until a carry is left over


        //this.calculatePopulationFitness(this.population);
        //this.printPopulation(this.population);
        this.goodbyeMessage();
    }

    private List<Element> generateAllPossibleCombinationsPopulation(int chromosomeSize) {
        List<Element> population = new ArrayList<>();
        List<String> values = new ArrayList<>();
        values.add("0");
        values.add("1");
        Combinations c = new Combinations();
        List<String[]> chromosomes = c.generateCombinations(chromosomeSize, values);
        for (int i = 0; i < chromosomes.size(); i++) {
            String[] ch = chromosomes.get(i);
            int[] chInt = new int[ch.length];
            int countZero = 0;
            for (int j = 0; j < ch.length; j++) {
                chInt[j] = Integer.valueOf(ch[j]);
                if (chInt[j] == 0) {
                    countZero++;
                }
            }
            if (countZero == chromosomeSize) {
                continue;
            }
            Element e = new Element(chInt);
            population.add(e);
        }
        return population;
    }

    private void calculatePopulationFitness(List<Element> population) {
        Iterator<Element> it = population.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            e.setFitness(this.calculateFitness(e));
        }
    }

    private double calculateFitness(Element e) {
        double result = -1.0; //ThreadLocalRandom.current().nextDouble();
        if (this.learningDataset.equals("") || this.testDataset.equals("")) {
            return ThreadLocalRandom.current().nextDouble();
        }
        try {
            DataSource learningDataSource = new DataSource(this.learningDataset);
            Instances learningData = learningDataSource.getDataSet();
            if (learningData.classIndex() == -1) {
                learningData.setClassIndex(learningData.numAttributes() - 1);
            }

            DataSource testDataSource = new DataSource(this.testDataset);
            Instances testData = testDataSource.getDataSet();
            if (testData.classIndex() == -1) {
                testData.setClassIndex(testData.numAttributes() - 1);
            }

            // FILTER
            Instances newLearningData = this.filterData(e, learningData);
            ArffSaver saver = new ArffSaver();
            saver.setInstances(newLearningData);
            saver.setFile(new File("dataset" + File.separator + "result" + File.separator + "exe-learning-" + this.countFiles + ".arff"));
            saver.writeBatch();

            Instances newTestData = this.filterData(e, testData);
            saver = new ArffSaver();
            saver.setInstances(newLearningData);
            saver.setFile(new File("dataset" + File.separator + "result" + File.separator + "exe-test-" + this.countFiles + ".arff"));
            saver.writeBatch();
            this.countFiles++;

            Evaluation eval = this.evaluate(newLearningData, newTestData);

            /*this.log.add("WEKA: " + eval.toSummaryString("\nResults\n======\n", false));
            this.log.add("WEKA CORRECT: " + eval.correct());
            this.log.add("WEKA CORRECT (%): " + eval.pctCorrect());
            this.log.add("WEKA INCORRECT: " + eval.incorrect());
            this.log.add("WEKA INCORRECT (%): " + eval.pctIncorrect());
            this.log.add("WEKA KAPPA: " + eval.kappa());*/
            // :)
            //result = eval.fMeasure(0);
            //result = eval.fMeasure(1);
            //result = eval.pctCorrect()/100;
            result = eval.weightedFMeasure();
        } catch (Exception ex) {
            return -1;
        }
        return result;
    }

    private Evaluation evaluate(Instances learningData, Instances testData) {
        if (this.classifier instanceof RandomForest) {
            return this.randomForest(learningData, testData);
        } else if (this.classifier instanceof J48) {
            return this.decisionTree(learningData, testData);
        } else {
            return null;
        }
    }

    private Instances filterData(Element e, Instances data) {
        List<Integer> featuresList = new ArrayList<>();
        for (int i = 0; i < e.getChromosome().length; i++) {
            if (e.getChromosome()[i] == 0) {
                featuresList.add(i);
            }
        }
        int[] featuresArray = new int[featuresList.size()];
        for (int i = 0; i < featuresList.size(); i++) {
            featuresArray[i] = featuresList.get(i);
        }
        Instances filteredData = null;
        try {
            Remove remove = new Remove();
            remove.setAttributeIndicesArray(featuresArray);
            remove.setInvertSelection(false);
            remove.setInputFormat(data);
            filteredData = Filter.useFilter(data, remove);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return filteredData;
    }

    protected void welcomeMessage() {
        String classifier = "";
        if (this.classifier instanceof RandomForest) {
            classifier = "Random Forest";
        } else if (this.classifier instanceof J48) {
            classifier = "Decision Tree";
        } else {
            classifier = "Unknown";
        }
        this.log.add("/******************************");
        if (this.relationName.isEmpty()) {
            this.log.add("* FEATURE SELECTION");
        } else {
            this.log.add("* FEATURE SELECTION - " + this.relationName);
        }
        this.log.add("* Population Size: " + this.populationSize);
        this.log.add("* Chromosome Size: " + this.dimension);
        this.log.add("* Crossover Constant: " + this.crossoverConstant);
        this.log.add("* Mutation Constant: " + this.mutationConstant);
        this.log.add("* Max Gen: " + this.maxGen);
        this.log.add("* Classifier: " + classifier);
        this.log.add("* FITNESS: " + this.initialFitness);
        this.log.add("*******************************/");
        this.log.add("START", true);
    }

    protected void goodbyeMessage() {
        this.log.add("******************************");
        this.log.add("END", true);
    }

    protected void printPopulation(List<Element> population) {
        int elements = 1;
        Iterator<Element> it = population.iterator();
        while (it.hasNext()) {
            Element e = it.next();
            String chromosome = e.toString();
            chromosome = "Element [" + elements + "]: " + chromosome + " FITNESS: " + e.getFitness();
            if (this.log == null) {
                System.out.println(chromosome);
            } else {
                this.log.add(chromosome);
            }
            elements++;
        }
    }

    private void logFitness(String s) {
        if (this.logFitness != null) {
            this.logFitness.add(s);
        }
    }

    private void logImprovement(double i) {
        if (this.logImprovement != null) {
            this.logImprovement.add(Double.toString(i));
        }
    }

    /********** GETTER / SETTER **********/
    public Log getLog() {
        return log;
    }

    public void setLog(Log log) {
        this.log = log;
    }

    public int getPopulationSize() {
        return populationSize;
    }

    public void setPopulationSize(int populationSize) {
        this.populationSize = populationSize;
    }

    public int getDimension() {
        return dimension;
    }

    public void setDimension(int dimension) {
        this.dimension = dimension;
    }

    public double getCrossoverConstant() {
        return crossoverConstant;
    }

    public void setCrossoverConstant(double crossoverConstant) {
        this.crossoverConstant = crossoverConstant;
    }

    public double getMutationConstant() {
        return mutationConstant;
    }

    public void setMutationConstant(double mutationConstant) {
        this.mutationConstant = mutationConstant;
    }

    public int getMaxGen() {
        return maxGen;
    }

    public void setMaxGen(int maxGen) {
        this.maxGen = maxGen;
    }

    public double getInitialFitness() {
        return initialFitness;
    }

    public void setInitialFitness(double initialFitness) {
        this.initialFitness = initialFitness;
    }

    public Classifier getClassifier() {
        return classifier;
    }

    public void setClassifier(Classifier classifier) {
        this.classifier = classifier;
    }

    public String getRelationName() {
        return relationName;
    }

    public void setRelationName(String relationName) {
        this.relationName = relationName;
    }

    public double getImprovement() {
        return improvement;
    }

    public void setImprovement(double improvement) {
        this.improvement = improvement;
    }

    public Log getLogFitness() {
        return logFitness;
    }

    public void setLogFitness(Log logFitness) {
        this.logFitness = logFitness;
    }

    public Log getLogImprovement() {
        return logImprovement;
    }

    public void setLogImprovement(Log logImprovement) {
        this.logImprovement = logImprovement;
    }
}

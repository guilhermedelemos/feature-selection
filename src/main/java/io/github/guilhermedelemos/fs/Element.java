package io.github.guilhermedelemos.fs;

public class Element {
    private int[] chromosome;
    private double fitness;

    public Element() {
        super();
    }

    public Element(int[] chromosome) {
        this.chromosome = chromosome;
    }

    @Override
    public String toString() {
        String chromosome = "";
        for(int i=0;i<this.getChromosome().length;i++) {
            chromosome += this.getChromosome()[i] + ",";
        }
        return chromosome;
    }

    public int[] getChromosome() {
        return chromosome;
    }

    public void setChromosome(int[] chromosome) {
        this.chromosome = chromosome;
    }

    public double getFitness() {
        return fitness;
    }

    public void setFitness(double fitness) {
        this.fitness = fitness;
    }
}

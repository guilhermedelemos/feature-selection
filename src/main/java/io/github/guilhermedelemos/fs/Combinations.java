package io.github.guilhermedelemos.fs;

import java.util.ArrayList;
import java.util.List;

public class Combinations {
    public List<String[]> generateCombinations(int arraySize, List<String> possibleValues) {
        List<String[]> combinations = new ArrayList<>();
        int carry;
        int[] indices = new int[arraySize];
        do {
            String[] result = new String[arraySize];
            int idx = 0;
            for (int index : indices) {
                result[idx++] = possibleValues.get(index);
                //System.out.print(possibleValues.get(index) + " ");
            }
            combinations.add(result);
            //System.out.println("");

            carry = 1;
            for (int i = indices.length - 1; i >= 0; i--) {
                if (carry == 0)
                    break;

                indices[i] += carry;
                carry = 0;

                if (indices[i] == possibleValues.size()) {
                    carry = 1;
                    indices[i] = 0;
                }
            }
        }
        while (carry != 1); // Call this method iteratively until a carry is left over
        return combinations;
    }

}

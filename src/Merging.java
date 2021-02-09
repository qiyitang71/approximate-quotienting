import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;

class Distribution {
    private double[] distr;
    public int size;

    public Distribution(int size) {
        this.distr = new double[size];
        this.size = size;
    }

    public Distribution(double[] distr) {
        this.distr = distr;
        this.size = distr.length;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + Arrays.hashCode(distr);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Distribution other = (Distribution) obj;
        if (!Arrays.equals(distr, other.distr))
            return false;
        return true;
    }

    public double getProbability(int index) {
        return distr[index];
    }

    public void updateEntry(int index, double probability) {
        distr[index] = probability;
    }
}

class StatePair {
    int state1;
    int state2;

    public StatePair(int s1, int s2) {
        this.state1 = s1;
        this.state2 = s2;
    }
}

public class Merging {
    //input
    public int numOfStates;
    public int numOfTrans;
    private Map<Integer, Map<Integer, Double>> transitions;

    private int[] states;

    private Map<Integer, Integer> labelMap;

    //output
    public int newNumOfStates;
    public int newNumOfTrans;

    private int[] newStates;

    private Map<Integer, Integer> newLabelMap;
    private Map<Integer, Map<Integer, Double>> newTransitions = new HashMap<>();


    public double[] discrepancy;
    public double epsilon2;
    PrintStream output = null;


    public void readFile(String[] args) {
        Scanner input = null;

        // parse input file
        if (args.length != 3) {
            System.out.println(
                    "Use java ValueIteration 0: <inputFile> 1: <outputDistanceFile> 2: <epsilon2>");
            return;
        }
        // process the command line arguments
        try {
            input = new Scanner(new File(args[0]));
        } catch (FileNotFoundException e) {
            System.out.printf("Input file %s not found%n", args[0]);
            System.exit(1);
        }

        try {
            output = new PrintStream(new File(args[1]));
        } catch (FileNotFoundException e) {
            System.out.printf("Output file %s not created%n", args[1]);
            System.exit(1);
        }

        try {
            this.epsilon2 = Double.parseDouble(args[2]);
            assert this.epsilon2 < 1 : String.format("Discount factor %f should be less than 1", this.epsilon2);
            assert this.epsilon2 > 0 : String.format("Discount factor %f should be greater than 0", this.epsilon2);
        } catch (NumberFormatException e) {
            System.out.println("Discount factor not provided in the right format");
            System.exit(1);
        }

        //while (input.hasNextInt()) {
        try {
            this.numOfStates = input.nextInt();
            this.numOfTrans = input.nextInt();

            this.states = new int[numOfStates];

            this.transitions = new HashMap<>();
            this.labelMap = new HashMap<>();

            for (int i = 0; i < numOfStates; i++) {
                states[i] = input.nextInt();
            }

            for (int i = 0; i < numOfStates; i++) {
                int label = input.nextInt();
                labelMap.put(states[i], label);
            }

            for (int j = 0; j < numOfTrans; j++) {
                int state = input.nextInt();
                int next = input.nextInt();
                double probability = input.nextDouble();
                transitions.computeIfAbsent(state, x -> new HashMap<>()).put(next, probability);
            }
        } catch (NoSuchElementException e) {
            System.out.printf("Input file %s not in the correct format%n", args[0]);
        }
        // }
    }


    public void printOutput() {
        System.out.println(this.newNumOfStates + " " + this.newNumOfTrans);
        for (int i = 0; i < this.newNumOfStates; i++) {
            System.out.print(this.newStates[i] + " ");
        }
        System.out.println();

        for (int i : this.newStates) {
            System.out.print(this.newLabelMap.get(i) + " ");
        }
        System.out.println();
        for (int i : this.newStates) {
            Map<Integer, Double> map = newTransitions.get(i);
            for (int state : map.keySet()) {
                System.out.println(i + " " + state + " " + map.get(state));
            }
        }
        System.out.println();
    }

    public void printInput() {
        System.out.println(this.numOfStates + " " + this.numOfTrans);
        for (int i = 0; i < this.numOfStates; i++) {
            System.out.print(this.states[i] + " ");
        }
        System.out.println();

        for (int i : this.states) {
            System.out.print(this.labelMap.get(i) + " ");
        }
        System.out.println();
        for (int i : this.states) {
            Map<Integer, Double> map = transitions.get(i);
            for (int state : map.keySet()) {
                System.out.println(i + " " + state + " " + map.get(state));
            }
        }
        System.out.println();
    }

    public void writeInputToFile() {
        output.println(this.numOfStates + " " + this.numOfTrans);
        for (int i = 0; i < this.numOfStates; i++) {
            output.print(this.states[i] + " ");
        }
        output.println();

        for (int i : this.states) {
            output.print(this.labelMap.get(i) + " ");
        }
        output.println();

        for (int i : this.states) {
            Map<Integer, Double> map = transitions.get(i);
            for (int state : map.keySet()) {
                output.println(i + " " + state + " " + map.get(state));
            }
        }
        output.println();
    }


    public void writeOutputToFile() {
        output.println(this.newNumOfStates + " " + this.newNumOfTrans);
        for (int i = 0; i < this.newNumOfStates; i++) {
            output.print(this.newStates[i] + " ");
        }
        output.println();

        for (int i : this.newStates) {
            output.print(this.newLabelMap.get(i) + " ");
        }
        output.println();

        for (int i : this.newStates) {
            Map<Integer, Double> map = newTransitions.get(i);
            for (int state : map.keySet()) {
                output.println(i + " " + state + " " + map.get(state));
            }
        }
        output.println();
    }

    public List<List<Integer>> partition;

    //partition the states by their labels
    public void createInitialPartition(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        partition = new ArrayList<>();

        for (int state : trans.keySet()) {
            boolean isAdd = false;
            for (List<Integer> list : partition) {
                int label = lMap.get(list.get(0));
                if (lMap.get(state) == label) {
                    list.add(state);
                    isAdd = true;
                    break;
                }
            }
            //create the new list
            if (!isAdd) {
                ArrayList<Integer> list = new ArrayList<>();
                list.add(state);
                partition.add(list);
            }
        }
    }

    //if s1 < 0 then it is normal partition
    // return true if split; return false if fixpoint reached
    public boolean split(int s1, int s2, Map<Integer, Map<Integer, Double>> trans) {

        List<List<Integer>> newPartition = new ArrayList<>();
        List<List<Integer>> tmpPartition = new ArrayList<>(partition);
        int size = partition.size();
        for (List<Integer> list : partition) {
            if (s1 >= 0) {
                newPartition.add(list);
                continue;
            }
            List<List<Integer>> splitPartition = new ArrayList<>();

            //1st iteration: build a list of distributions
            Map<Integer, Distribution> distrMap = new HashMap<>();
            for (int state : list) {
                Distribution distr = new Distribution(size);
                for (int i = 0; i < size; i++) {
                    List<Integer> other = tmpPartition.get(i);
                    // compute probability to the other partition
                    double sum = 0;
                    for (int next : other) {
                        if (trans.get(state).containsKey(next)) {
                            sum += trans.get(state).get(next);
                        }
                    }
                    distr.updateEntry(i, sum);
                }
                distrMap.put(state, distr);
            }


            //split the states
            for (int state : list) {
                Distribution distr = distrMap.get(state);
                boolean isAdd = false;
                for (int i = 0; i < splitPartition.size(); i++) {
                    List<Integer> l = splitPartition.get(i);
                    int rdState = l.get(0);
                    if (distr.equals(distrMap.get(rdState))) {
                        l.add(state);
                        isAdd = true;
                        break;
                    }
                }
                if (!isAdd) {
                    ArrayList<Integer> l = new ArrayList<>();
                    l.add(state);
                    splitPartition.add(l);
                }
            }
            newPartition.addAll(splitPartition);
        }

        if (partition.size() == newPartition.size()) {
            return false;
        }

        partition = newPartition;
        return true;
    }

    public void partitionRefine(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        createInitialPartition(trans, lMap);
        while (split(-1, -1, trans)) {
        }
        mergePartition(trans, lMap);
    }

    public void mergePartition(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        this.newNumOfStates = partition.size();
        this.newLabelMap = new HashMap<>();
        this.newStates = new int[this.newNumOfStates];
        this.newNumOfTrans = 0;
        this.newTransitions = new HashMap<>();
        for (int i = 0; i < this.newNumOfStates; i++) {
            int rdState = partition.get(i).get(0);
            int label = lMap.get(rdState);
            this.newStates[i] = i;
            this.newLabelMap.put(i, label);
            for (int j = 0; j < this.newNumOfStates; j++) {
                List<Integer> l = partition.get(j);
                double sum = 0;
                for (int next : l) {
                    if (trans.get(rdState).containsKey(next)) {
                        sum += trans.get(rdState).get(next);
                    }
                }
                if (sum > 0) {
                    this.newNumOfTrans++;
                    this.newTransitions.computeIfAbsent(i, x -> new HashMap<>()).put(j, sum);
                }
            }
        }
    }

    public double computeTVDistance(Distribution d1, Distribution d2) {
        if (d1.size != d2.size) {
            System.err.println("tv distance size match");
            return -1;
        }
        double sum = 0;
        for (int i = 0; i < d1.size; i++) {
            double p1 = d1.getProbability(i);
            double p2 = d2.getProbability(i);
            if (p1 > p2) {
                sum += (p1 - p2);
            }
        }
        return sum;
    }

//    public void updateSystem() {
//        this.states = this.newStates;
//        this.labelMap = this.newLabelMap;
//        this.numOfTrans = this.newNumOfTrans;
//        this.transitions = this.newTransitions;
//    }

    public Distribution getDistributionOnPatitions(Map<Integer, Map<Integer, Double>> trans, int state) {
        int size = partition.size();
        Distribution distr = new Distribution(size);
        for (int i = 0; i < size; i++) {
            List<Integer> list = partition.get(i);
            double sum = 0;
            for (int next : list) {
                if (trans.get(state).containsKey(next)) {
                    sum += trans.get(state).get(next);
                }
            }
            distr.updateEntry(i, sum);
        }
        return distr;
    }

    public void splitWithPair(int s1, int s2) {
        List<List<Integer>> newPartition = new ArrayList<>();
        for (int i = 0; i < this.partition.size(); i++) {
            List<Integer> list = this.partition.get(i);
            if (list.contains(s1)) {
                List<Integer> l1 = new ArrayList<>();
                l1.add(s1);
                l1.add(s2);
                List<Integer> l2 = new ArrayList<>();
                for (int j : list) {
                    if (j != s1 && j != s2) {
                        l2.add(j);
                    }
                }
                newPartition.add(l1);
                newPartition.add(l2);
            } else {
                newPartition.add(list);
            }
        }
        this.partition = newPartition;
    }

    public StatePair getMinLocalDistance(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        double min = 1;
        StatePair sp = null;
        for (List<Integer> list : partition) {
            if (list.size() <= 1) continue;
            for (int i = 0; i < list.size(); i++) {
                for (int j = 0; j < i; j++) {
                    createInitialPartition(trans, lMap);
                    int s1 = list.get(i);
                    int s2 = list.get(j);
                    splitWithPair(s1, s2);
                    while (split(s1, s2, trans)) {
                    }
                    Distribution d1 = getDistributionOnPatitions(trans, s1);
                    Distribution d2 = getDistributionOnPatitions(trans, s2);
                    double localDistance = computeTVDistance(d1, d2);
                    if (localDistance < min && localDistance < epsilon2) {
                        sp = new StatePair(s1, s2);
                        min = localDistance;
                    }

                }
            }
        }
        return sp;
    }

    //return false if no merge
    public boolean mergeSinglePair(Map<Integer, Map<Integer, Double>> trans, Map<Integer, Integer> lMap) {
        createInitialPartition(trans, lMap);
        StatePair sp = getMinLocalDistance(trans, lMap);
        Map<Integer, Map<Integer, Double>> tmpTransitions = new HashMap<>(trans);
        if (sp == null) {
            return false;
        }

        int s1 = sp.state1;
        int s2 = sp.state2;
        createInitialPartition(trans, lMap);
        splitWithPair(s1, s2);
        while (split(s1, s2, trans)) {
        }
        Distribution d1 = getDistributionOnPatitions(trans, s1);
        Distribution d2 = getDistributionOnPatitions(trans, s2);
        Map<Integer, Double> m1 = new HashMap<>();
        Map<Integer, Double> m2 = new HashMap<>();

        int size = d1.size;
        for (int i = 0; i < size; i++) {
            double variation = (d1.getProbability(i) - d2.getProbability(i)) / 2.0;
            List<Integer> list = partition.get(i);
            int si = s1;
            int sj = s2;
            Map<Integer, Double> mi = m1;
            Map<Integer, Double> mj = m2;
            //si needs to be increased
            if (variation > 0) {
                si = s2;
                sj = s1;
                mi = m2;
                mj = m1;
            }

            //fix distributions for s1 and s2
            double left = -Math.abs(variation);
            for (int j = 0; j < list.size(); j++) {
                int state = list.get(j);
                double p1 = 0;
                if (trans.get(si).containsKey(state)) {
                    p1 = trans.get(si).get(state);
                }
                if (variation != 0 && j == 0) {
                    mi.put(state, p1 - variation);
                } else if (j != 0 && p1 > 0) {
                    mi.put(state, p1);
                }

                double p2 = 0;
                if (trans.get(sj).containsKey(state)) {
                    p2 = trans.get(sj).get(state);
                }
                if (p2 <= Math.abs(left)) {
                    left += p2;
                } else {
                    mj.put(state, p2 + left);
                    left = 0;
                }
            }
        }
        tmpTransitions.replace(s1, m1);
        tmpTransitions.replace(s2, m2);
        this.newTransitions = tmpTransitions;
        return true;
    }

    public void smoothTransitions(Map<Integer, Map<Integer, Double>> trans) {
        for (int state : trans.keySet()) {
            Map<Integer, Double> map = trans.get(state);
            int len = map.size();
            double sum = 0;
            int i = 0;
            for (int next : map.keySet()) {
                if (i == len - 1) {
                    map.put(next, 1 - sum);
                    break;
                }
                sum += map.get(next);
                i++;
            }

        }
    }
    public static void main(String[] args) {
        Merging merge = new Merging();
        merge.readFile(args);
        merge.printInput();

        //compute probabilistic bisimulation
        merge.partitionRefine(merge.transitions, merge.labelMap);
        merge.printOutput();

        //merge states
//        merge.mergeSinglePair(merge.newTransitions, merge.newLabelMap);
//        merge.printOutput();
//
//        merge.partitionRefine(merge.newTransitions, merge.newLabelMap);
//        merge.printOutput();

        while (merge.mergeSinglePair(merge.newTransitions, merge.newLabelMap)) {
            merge.partitionRefine(merge.newTransitions, merge.newLabelMap);

        }

        merge.printOutput();
        merge.smoothTransitions(merge.newTransitions);
        merge.writeOutputToFile();
    }

}

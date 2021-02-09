import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintStream;
import java.util.*;


class Transition {
    public int state;
    public double probability;

    Transition(int state, double probability) {
        this.state = state;
        this.probability = probability;
    }
}

public class Sampling {
    public int numOfStates;
    public int numOfTrans;
    private Map<Integer, List<Transition>> transitions;
    private Map<Integer, List<Transition>> samplingTransitions = new HashMap<>();

    private int[] states;
    private Map<Integer, Integer> labelMap;
    public double epsilon, delta;
    PrintStream output = null;


//    public Sampling(int numOfStates, Map<Integer, List<Transition>> transitions, int[] labels, double epsilon, double delta, double epsilon2) {
//        this.numOfStates = numOfStates;
//        this.transitions = transitions;
//        this.labels = labels;
//        this.discrepancy = new double[numOfStates * numOfStates];
//        this.epsilon = epsilon;
//        this.delta = delta;
//        this.epsilon2 = epsilon2;
//    }

    public void readFile(String[] args) {
        Scanner input = null;

        // parse input file
        if (args.length != 4) {
            System.out.println(
                    "Use java ValueIteration 0: <inputFile> 1: <outputDistanceFile> 2: <epsilon> 3: <delta>");
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
            this.epsilon = Double.parseDouble(args[2]);
            assert this.epsilon < 1 : String.format("Discount factor %f should be less than 1", this.epsilon);
            assert this.epsilon > 0 : String.format("Discount factor %f should be greater than 0", this.epsilon);
        } catch (NumberFormatException e) {
            System.out.println("Discount factor not provided in the right format");
            System.exit(1);
        }
        try {
            this.delta = Double.parseDouble(args[3]);
            assert this.delta <= 1 : String.format("Accuracy %f should be less than or equal to 1", this.delta);
            assert this.delta > 0 : String.format("Accuracy %f should be greater than 0", this.delta);
        } catch (NumberFormatException e) {
            System.out.println("Accuracy not provided in the right format");
            System.exit(1);
        }

        //while (input.hasNextInt()) {
        try {
            this.numOfStates = input.nextInt();
            this.numOfTrans = input.nextInt();
            this.states = new int[numOfStates];
            this.labelMap = new HashMap<>();
            transitions = new HashMap<>();
            for (int i = 0; i < numOfStates; i++) {
                this.states[i] = input.nextInt();
            }
            for (int i : this.states) {
                int label = input.nextInt();
                this.labelMap.put(i, label);
            }
            for (int j = 0; j < numOfTrans; j++) {
                int row = input.nextInt();
                int state = input.nextInt();
                double probability = input.nextDouble();
                Transition tran = new Transition(state, probability);
                transitions.computeIfAbsent(row, x -> new ArrayList<>()).add(tran);
            }
        } catch (NoSuchElementException e) {
            System.out.printf("Input file %s not in the correct format%n", args[0]);
        }
        // }
    }

    public void singleExperiment(int state) {
        List<Transition> list = transitions.get(state);
        int numTran = list.size();
        if (numTran == 1) {
            samplingTransitions.put(state, list);
            return;
        }

        long totalCnt = (long) Math.ceil(numTran / (4 * this.epsilon * this.epsilon * this.delta));
        Map<Integer, Long> cntMap = new HashMap<>();
        for (int i = 0; i < totalCnt; i++) {
            double rnd = new Random().nextDouble();
            double sum = 0;
            for (Transition tran : list) {
                int next = tran.state;
                double probability = tran.probability;
                sum += probability;
                if (rnd <= sum) {
                    if (cntMap.containsKey(next)) {
                        long cnt = cntMap.get(next);
                        cntMap.replace(next, ++cnt);
                    } else {
                        cntMap.put(next, 1l);
                    }
                    break;
                }
            }
        }
        List<Transition> newList = new ArrayList<>();
        for (Transition tran : list) {
            int next = tran.state;
            double probability = cntMap.get(next) * 1.0 / totalCnt;
            newList.add(new Transition(next, probability));
        }
        samplingTransitions.put(state, newList);
    }

    public void experiment() {
        for (int i = 0; i < this.numOfStates; i++) {
            singleExperiment(i);
        }
    }

    public void printOutput() {
        System.out.println(this.numOfStates + " " + this.numOfTrans);
        for (int i : this.states) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i : this.states) {
            System.out.print(this.labelMap.get(i) + " ");
        }
        System.out.println();
        for (int i : this.states) {
            List<Transition> list = samplingTransitions.get(i);
            for (Transition tran : list) {
                System.out.println(i + " " + tran.state + " " + tran.probability);
            }
        }
        System.out.println();
    }

    public void printInput() {
        System.out.println(this.numOfStates + " " + this.numOfTrans);
        for (int i : this.states) {
            System.out.print(i + " ");
        }
        System.out.println();
        for (int i : this.states) {
            System.out.print(this.labelMap.get(i) + " ");
        }
        System.out.println();
        for (int i : this.states) {
            List<Transition> list = transitions.get(i);
            for (Transition tran : list) {
                System.out.println(i + " " + tran.state + " " + tran.probability);
            }
        }
        System.out.println();
    }

    public void writeToFile() {
        output.println(this.numOfStates + " " + this.numOfTrans);
        for (int i : this.states) {
            output.print(i + " ");
        }
        output.println();

        for (int i : this.states) {
            output.print(this.labelMap.get(i) + " ");
        }
        output.println();
        for (int i : this.states) {
            List<Transition> list = samplingTransitions.get(i);
            for (Transition tran : list) {
                output.println(i + " " + tran.state + " " + tran.probability);
            }
        }
        output.println();
    }

    public void smoothTransitions() {
        for (int state : this.samplingTransitions.keySet()) {
            List<Transition> lst = this.samplingTransitions.get(state);
            int len = lst.size();
            double sum = 0;
            for (int i = 0; i < len; i++) {
                if (i == len - 1) {
                    lst.get(i).probability = 1 - sum;
                    break;
                }
                sum += lst.get(i).probability;
            }

        }
    }

    public static void main(String[] args) {
        Sampling sampling = new Sampling();
        sampling.readFile(args);
        sampling.printInput();
        sampling.experiment();
        sampling.printOutput();
        sampling.smoothTransitions();
        sampling.printOutput();
        sampling.writeToFile();
    }

}

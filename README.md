# Approximate Minimisation Algorithms 

## Models
We evaluate the performance of the approximate minimisation algorithms on several LMCs. These LMCs model randomised algorithms and probabilistic protocols that are part of the probabilistic model checker [PRISM](https://www.prismmodelchecker.org/). The models can be found [here](models.csv).

We call models with less than 300 states as small models, and otherwise large models.

## Experiments

For small LMCs, we sample the successor distribution for each state and obtain an approximation of it with error parameter ε and error bound δ. For large LMCs, sampling is not practical so we perturb the successor distribution by adding small noise to the successor transition probabilities so that for each state with at least probability 1-δ the L1-distance of the successor distributions of it and the one in the approximation model is at most ε and otherwise the L1-distance is 2ε. We vary error parameter ε '∈' {0.00001, 0.0001, 0.001, 0.01} and fix error bound δ = 0.01. For each original LMC and a pair of ε and δ, we generate 5 approximation LMCs. 

We apply all three minimisation algorithms with ε2 ∈ {0.00001, 0.0001, 0.001, 0.01, 0.1} to the small LMCs (both the original models and the approximation models), while we only apply the approximate minimisation algorithm using approximate partition refinement with ε2 ∈ {0.00001, 0.0001, 0.001, 0.01, 0.1} to the large LMCs (both the original models and the approximation models), since in practice the other two algorithms take too long on the large LMCs.

To run the experiments, firstly choose a model. If the model is small (with less than 300 states), go to `runSmall.sh` and replace the model name on line 8 by one of the options on line 7. Simply run the bash script:
>bash runSmall.sh

If the model is large (with at least 300 states), go to `runLarge.sh` and replace the model name on line 8 by one of the options on line 7. Simply run the bash script:
>bash runLarge.sh


## Results
The full experimental results can be found here: https://docs.google.com/spreadsheets/d/1JJnBYsOTfbC3mXHEWO4AEJG492YoD0yXaAs8DsrwGOA/edit?usp=sharing

# HIPERMovelets

Source code for the papers:

a) **HiPerMovelets: high-performance movelet extraction for trajectory classification**, published in International Journal of Geographical Information Science
\[ [publication](https://doi.org/10.1080/13658816.2021.2018593) ] 
b) **UltraMovelets: Efficient Movelet Extraction for Multiple Aspect Trajectory Classification**, published in The 35th International Conference on Database and Expert Systems Applications (DEXA 2024)
\[ [publication](https://#) ] 


References: \[ [bibtex](./bibliography.bib) ]


## Versions


This is a project with the HIPERMovelets (Portela, 2020) implementation, with three options of optimizations.


- *HiPerMovelets*: new optimization for MASTERMovelets, with greedy search (`-version hiper`).
- *HiPerMovelets-Log*: plus, limits the movelets size to the ln size of the trajectory (`-version hiper -Ms -3`).
- *HiPerPivots*: limits the movelets search space to the points that are neighbour of well qualified movelets of size one (`-version hiper-pivots`).
- *HiPerPivots-Log*: plus, limits the movelets size to the ln size of the trajectory (`-version hiper-pivots -Ms -3`).


- *RandomMovelets*: randomly evaluates subtrajectories to discover movelets (`-version random`).
- *UltraMovelets*: uses a recursive incremental strategy to limit the search space (`-version ultra`). Most memory efficient method. Use `-Ms -1` to disable Log limit, as it is set by default.


## Setup

A. In order to run the code you first need to install Java 8 (or superior). Be sure to have enough RAM memory available. 

B. Download the `MoveletDiscovery.jar` file from the releases, or compile and export the jar file for the main class: `br.ufsc.mov3lets.run.Mov3letsRun`

## Usage

### 1. You can run the HIPERMovelets with the following command:

```Shell
-curpath "$BASIC_PATH" 
-respath "$RESULT_PATH" 
-descfile "$DESC_FILE"  
-version hiper
-nt 8
```


Where:
- `BASIC_PATH`: The path for the input CSV training and test files.
- `RESULT_PATH`: The destination folder for CSV results files.
- `DESC_FILE`: Path for the descriptor file. File that describes the dataset attributes and similarity measures.
- `-version`: Method to run (hiper, hiper-pvt, ...)
- `-nt`: Number of threads

    
### 2. For instance:

To run the HIPERMovelets you can run the java code with the following default entries as example:


```Shell
java -Xmx80G -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version hiper -nt 8 -ed true -samples 1 -sampleSize 0.5 -medium "none" -output "discrete" -lowm "false" -ms 1 -Ms -3 | tee -a "output.txt"
```


This will run with 80G memory limit, 8 threads, and save the output to the file output.txt`. 

It is the same as (without the output file):


```Shell
java -Xmx80G -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version hiper -nt 8
```

### Examples

**HIPERMovelets** (with log)


```Shell
java -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version hiper 
```

**HIPERMovelets-Pivots** (with log)


```Shell
java -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version hiper-pivots 
```

**UltraMovelets**


```Shell
java -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version ultra 
```

**RandomMovelets**


```Shell
java -jar MoveletDiscovery.jar 
-curpath "$BASIC_PATH" -respath "$RESULT_PATH" -descfile "$DESC_FILE" 
-version random 
```

**To a complete list of parameters:**

```Shell
java -jar MoveletDiscovery.jar --help
```

## Change Log

Refer to [CHANGELOG.md](./CHANGELOG.md).

## Author

Tarlis Portela
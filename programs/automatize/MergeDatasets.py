import sys, os 
sys.path.insert(0, os.path.abspath(os.path.join('.')))

if len(sys.argv) < 1:
    print('Please run as:')
    print('\tMergeDatasets.py', 'PATH TO FOLDER')
    print('Example:')
    print('\tensemble-cls.py', '"./results/MASTERMovelets"')
    exit()

results_path = sys.argv[1]

from automatize.run import mergeDatasets

mergeDatasets(results_path, 'train.csv')
mergeDatasets(results_path, 'test.csv')
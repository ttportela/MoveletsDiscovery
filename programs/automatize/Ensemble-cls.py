import sys, os 
from datetime import datetime
sys.path.insert(0, os.path.abspath(os.path.join('.')))

if len(sys.argv) < 4:
    print('Please run as:')
    print('\tensemble-cls.py', 'PATH TO DATASET', 'PATH TO RESULTS_DIR', 'ENSEMBLES', 'DATASET')
    print('Example:')
    print('\tensemble-cls.py', '"./data"', '"./results"', '"{\'movelets\': \'./movelets-res\', \'marc\': \'./data\', \'poifreq\': \'./poifreq-res\'}"', 'specific')
    exit()

data_path = sys.argv[1]
results_path = sys.argv[2]
ensembles = eval(sys.argv[3])
dataset = sys.argv[4]

from automatize.ensemble import ClassifierEnsemble
time = datetime.now()

ClassifierEnsemble(data_path, results_path, ensembles, dataset, save_results=True)

time_ext = (datetime.now()-time).total_seconds() * 1000

print("Done. Processing time: " + str(time_ext) + " milliseconds")
print("# ---------------------------------------------------------------------------------")
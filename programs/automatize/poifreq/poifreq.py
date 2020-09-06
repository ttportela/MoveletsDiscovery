import sys, os 
sys.path.insert(0, os.path.abspath(os.path.join('.')))

if len(sys.argv) < 4:
    print('Please run as:')
    print('\tpoifreq.py', 'METHOD', 'SEQUENCES', 'FEATURES', 'DATASET', 'PATH TO DATASET', 'PATH TO RESULTS_DIR')
    print('Example:')
    print('\tpoifreq.py', 'npoi', '"1,2,3"', '"poi,hour"', 'specific', '"./data"', '"./results"')
    exit()

METHOD = sys.argv[1]
SEQUENCES = [int(x) for x in sys.argv[2].split(',')]
FEATURES = sys.argv[3].split(',')
DATASET = sys.argv[4]
path_name = sys.argv[5]
RESULTS_DIR = sys.argv[6]

from automatize.ensemble_models.poifreq_model import poifreq
poifreq(SEQUENCES, DATASET, FEATURES, path_name, RESULTS_DIR, method=METHOD, save_all=True)
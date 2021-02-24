import sys, os 
import pandas as pd
sys.path.insert(0, os.path.abspath(os.path.join('.')))

if len(sys.argv) < 1:
    print('Please run as:')
    print('\tPrintResults.py', '"PATH TO FOLDER"', '"PREFIX"')
    print('Example:')
    print('\tPrintResults.py', '"./results/method"', '"dataset"')
    exit()

results_path = sys.argv[1]
prefix = ""

if len(sys.argv) > 1:
    prefix = sys.argv[2]

from automatize.results import addResults

def results2df(res_path, prefix, modelfolder='model', isformat=True):
    filelist = []
    filesList = []

    path = os.path.join(res_path, '**', prefix, '**', '*.txt' )

    # 1: Build up list of files:
    import glob2 as glob
    print("Looking for result files in " + path)
    for files in glob.glob(path):
        fileName, fileExtension = os.path.splitext(files)
        filelist.append(fileName) #filename without extension
        filesList.append(files) #filename with extension
    
    filesList = set(filesList)
    #return 0
    # 2: Create and concatenate in a DF:
#     ct = 1
    df = pd.DataFrame()

    cols = []

    df[' '] = ['Candidates', 'Movelets', 'ACC (MLP)', 'ACC (RF)', 'ACC (SVM)' , 'Time (Movelets)', 'Time (MLP)', 'Time (RF)', 'Time (SVM)', 'Trajs. Compared', 'Trajs. Pruned', 'Date']
    df['Dataset'] = ""
    df['Dataset'][0] = prefix
#     df = df[['Dataset',' ']]
    for ijk in filesList:
        method = os.path.basename(ijk)[:-4]
        cols.append(method)
        
        path = os.path.dirname(ijk)
        df[method] = addResults(df, ijk, path, method, modelfolder, isformat)
      
    print("Done.")
    cols.sort()
    cols = ['Dataset',' '] + cols
    return df[cols]


dirr = os.path.join(results_path, "**")
#coringa = ""

df = results2df(dirr, prefix)

with pd.option_context('display.max_rows', None, 'display.max_columns', None):  # more options can be specified also
    print(df)

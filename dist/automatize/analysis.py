'''
Created on Jun, 2020

@author: Tarlis Portela
'''
# --------------------------------------------------------------------------------
# ANALYSIS
import os
import sys
import numpy as np
import pandas as pd
import glob2 as glob
from datetime import datetime

from sklearn import preprocessing

import keras
from keras.models import Model
from keras.layers import Dense, Dropout, LSTM, Input, Activation
from keras import optimizers
# --------------------------------------------------------------------------------
# Para garantir reprodutibilidade
from numpy.random import seed
import tensorflow # import set_random_seed

# --------------------------------------------------------------------------------
from .Methods import Approach1, Approach2, ApproachRF, ApproachRFHP , ApproachMLP, ApproachDT, ApproachSVC
# --------------------------------------------------------------------------------

def def_random_seed(random_num=1, seed_num=1):
    seed(seed_num)
    tensorflow.compat.v1.set_random_seed(random_num)
    
# --------------------------------------------------------------------------------------

def results2df(res_path, prefix, modelfolder='model'):
    filelist = []
    filesList = []

    # 1: Build up list of files:
    for files in glob.glob(os.path.join(res_path, prefix, '*', '*.txt' )):
        fileName, fileExtension = os.path.splitext(files)
        filelist.append(fileName) #filename without extension
        filesList.append(files) #filename with extension
    
    # 2: Create and concatenate in a DF:
    ct = 1
    df = pd.DataFrame()

    cols = []

    df[' '] = ['Candidates', 'Movelets', 'MLP', 'RF', 'SVM' , 'Time', 'T. MLP', 'T. RF', 'T. SVM', 'Trajs. Looked', 'Trajs. Ignored']
    df['Dataset'] = ""
    df['Dataset'][0] = prefix
#     df = df[['Dataset',' ']]
    for ijk in filesList:
        data = read_csv(ijk)
        total_can = get_total_number_of_candidates_file_by_dataframe("Number of Candidates: ", data)
        total_mov = get_total_number_of_candidates_file_by_dataframe("Total of Movelets: ", data)
        trajs_looked = get_total_number_of_candidates_file_by_dataframe("Trajs. Looked: ", data)
        trajs_ignored = get_total_number_of_candidates_file_by_dataframe("Trajs. Ignored: ", data)
        time = get_total_number_of_ms("Processing time: ", data)
        
        method = os.path.basename(ijk)[:-4]
        cols.append(method)
        
        rf_acc  = getACC_RF(res_path, prefix,  method, modelfolder) * 100
        mlp_acc = getACC_MLP(res_path, prefix, method, modelfolder) * 100
        svm_acc = getACC_SVM(res_path, prefix, method, modelfolder) * 100
        
        rf_t  = getACC_time(res_path, prefix, method, modelfolder, 'RF')
        mlp_t = getACC_time(res_path, prefix, method, modelfolder, 'MLP')
        svm_t = getACC_time(res_path, prefix, method, modelfolder, 'SVM')
        
        rf_t  = '%d:%d:%d' % printHour(rf_t)  if rf_t  > 0 else "-"
        svm_t = '%d:%d:%d' % printHour(svm_t) if svm_t > 0 else "-"
        mlp_t = '%d:%d:%d' % printHour(mlp_t) if mlp_t > 0 else "-"
        
        rf  = "{:.3f}".format(rf_acc)  if rf_acc  > 0 else "-"
        svm = "{:.3f}".format(svm_acc) if svm_acc > 0 else "-"
        mlp = "{:.3f}".format(mlp_acc) if mlp_acc > 0 else "-"
        
        trajs_looked  = '{:,}'.format(trajs_looked)  if trajs_looked  > 0 else "-"
        trajs_ignored = '{:,}'.format(trajs_ignored) if trajs_ignored > 0 else "-"
        
        df[method] = ('{:,}'.format(total_can), '{:,}'.format(total_mov), mlp, rf, svm, 
                      '%d:%d:%d' % printHour(time), mlp_t, rf_t, svm_t, 
                      trajs_looked, trajs_ignored)
      
    cols.sort()
    cols = ['Dataset',' '] + cols
    return df[cols]

def ACC4All(res_path, prefix, save_results = True, modelfolder='model'):
    filelist = []
    filesList = []

    # 1: Build up list of files:
    for files in glob.glob(os.path.join(res_path, prefix, "*")):
        fileName, fileExtension = os.path.splitext(files)
        filelist.append(fileName) #filename without extension
        filesList.append(files) #filename with extension
    
    for ijk in filesList:
        method = ijk[len(res_path)+len(prefix)+2:]
        todo = not os.path.exists( os.path.join(res_path, prefix, method, modelfolder) )
        empty = not os.path.exists( os.path.join(res_path, prefix, method, "train.csv") )
        if todo and not empty:
            ALL3(res_path, prefix, method, save_results, modelfolder)
        else:
            print(method + (" Done." if not empty else " Empty."))
            
# ----------------------------------------------------------------------------------
def ALL3(res_path, prefix, dir_path, save_results = True, modelfolder='model'):
#     def_random_seed(random_num, seed_num)
    
    dir_path = os.path.join(res_path, prefix, dir_path)
#     X_train, y_train, X_test, y_test = loadData(dir_path)
    t_svm = Classifier_SVM(dir_path, save_results, modelfolder)
    t_rf  = Classifier_RF(dir_path, save_results, modelfolder)
    t_mlp = Classifier_MLP(dir_path, save_results, modelfolder)
    
    # ------
    if (save_results) :
        if not os.path.exists(os.path.join(dir_path, modelfolder)):
            os.makedirs(os.path.join(dir_path, modelfolder))
        pd.DataFrame({'SVM': [t_svm], 'RF': [t_rf], 'MLP': [t_mlp]}).to_csv(
            os.path.join(res_path, dir_path, modelfolder, "classification_times.csv"))
    
def MLP(res_path, prefix, dir_path, save_results = True, modelfolder='model'):
#     def_random_seed(random_num, seed_num)
    
    dir_path = os.path.join(res_path, prefix, dir_path)
    t = Classifier_MLP(dir_path, save_results, modelfolder)
    return t

def RF(res_path, prefix, dir_path, save_results = True, modelfolder='model'):
#     def_random_seed(random_num, seed_num)
    
    dir_path = os.path.join(res_path, prefix, dir_path)
    t = Classifier_RF(dir_path, save_results, modelfolder)
    return t

def SVM(res_path, prefix, dir_path, save_results = True, modelfolder='model'):
#     def_random_seed(random_num, seed_num)
    
    dir_path = os.path.join(res_path, prefix, dir_path)
    t = Classifier_SVM(dir_path, save_results, modelfolder)
    return t

# ----------------------------------------------------------------------------------
def Classifier_MLP(dir_path, save_results = True, modelfolder='model', X_train = None, y_train = None, X_test = None, y_test = None):
    if X_train is None:
        X_train, y_train, X_test, y_test = loadData(dir_path)
     
    # ---------------------------------------------------------------------------
    # Neural Network - Definitions:
    par_droupout = 0.5
    par_batch_size = 200
    par_epochs = 80
    par_lr = 0.00095
    
    # Building the neural network-
    print("Building neural network")
    lst_par_epochs = [80,50,50,30,20]
    lst_par_lr = [0.00095,0.00075,0.00055,0.00025,0.00015]
    
    time = datetime.now()
    Approach2(X_train, y_train, X_test, y_test, par_batch_size, lst_par_epochs, lst_par_lr, par_droupout, save_results, dir_path, modelfolder)
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time

# ----------------------------------------------------------------------------------
def Classifier_RF(dir_path, save_results = True, modelfolder='model', X_train = None, y_train = None, X_test = None, y_test = None):
    if X_train is None:
        X_train, y_train, X_test, y_test = loadData(dir_path)
    
    # ---------------------------------------------------------------------------
    # Random Forest - Definitions:
    # Este experimento eh para fazer uma varredura de arvores em random forestx
    #n_estimators = np.arange(10, 751, 10)
    #n_estimators = np.append([1], n_estimators)
    n_estimators = [300]
    print(n_estimators)
    
    print("Building random forest models")
    time = datetime.now()
    ApproachRF(X_train, y_train, X_test, y_test, n_estimators, save_results, dir_path, modelfolder)
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time
    
# ----------------------------------------------------------------------------------
def Classifier_SVM(dir_path, save_results = True, modelfolder='model', X_train = None, y_train = None, X_test = None, y_test = None):
    if X_train is None:
        X_train, y_train, X_test, y_test = loadData(dir_path)
    
    print("Building SVM models")
    time = datetime.now()
    ApproachSVC(X_train, y_train, X_test, y_test, save_results, dir_path, modelfolder)
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time

# def SVM_for_all(res_path, prefix):
#     filelist = []
#     filesList = []

#     # 1: Build up list of files:
#     for files in glob.glob(os.path.join(res_path, prefix, "*")):
#         fileName, fileExtension = os.path.splitext(files)
#         filelist.append(fileName) #filename without extension
#         filesList.append(files) #filename with extension
    
#     for ijk in filesList:
#         method = ijk[len(res_path)+len(prefix)+2:]
#         empty = not os.path.exists( os.path.join(res_path, prefix, method, "train.csv") )
#         if not empty:
#             ACC_SVM(prefix, method, modelfolder)
#         else:
#             print(method + (" Done." if not empty else " Empty."))

# ----------------------------------------------------------------------------------
# ----------------------------------------------------------------------------------
def getACC_time(res_path, prefix, method, label, modelfolder='model'):
    acc = 0.0
    data = getACC_data(os.path.join(res_path, prefix, method, modelfolder), 'classification_times.csv')
    if data is not None:
        acc = data[label][0]
    return acc

def getACC_RF(res_path, prefix, method, modelfolder='model'):
    acc = 0
    data = getACC_data(os.path.join(res_path, prefix, method, modelfolder), 'model_approachRF300_history.csv')
    if data is not None:
        acc = data['1'].iloc[-1]
    return acc

def getACC_SVM(res_path, prefix, method, modelfolder='model'):
    acc = 0
    data = getACC_data(os.path.join(res_path, prefix, method, modelfolder), 'model_approachSVC_history.csv')
    if data is not None:
        acc = data.loc[0].iloc[-1]
    return acc

def getACC_MLP(res_path, prefix, method, modelfolder='model'):
    acc = 0
    
    if "MARC" in method:
        res_file = os.path.join(res_path, prefix, method + '_results.csv')
        if os.path.isfile(res_file):
            data = pd.read_csv(res_file)
            acc = data['test_acc'].iloc[-1]
    else:
        data = getACC_data(os.path.join(res_path, prefix, method, modelfolder), 'model_approach2_history_Step5.csv')
        if data is not None:
            acc = data['val_accuracy'].iloc[-1]
    return acc

def getACC_data(path, approach_file):
    res_file = os.path.join(path, approach_file)
    if os.path.isfile(res_file):
        data = pd.read_csv(res_file)
        return data
    else:
        return None

# --------------------------------------------------------------------------------->
def printHour(millis):
    millis = int(millis)
    seconds=(millis/1000)%60
    seconds = int(seconds)
    minutes=(millis/(1000*60))%60
    minutes = int(minutes)
    hours=(millis//(1000*60*60))

#     print ("%dh%dm%ds" % (hours, minutes, seconds))
    return (hours, minutes, seconds)

# def printProcess(prefix, dir_path):
#     file = os.path.join(prefix, dir_path)
#     res_file = os.path.join(RES_PATH, file + '.txt')
    
#     data = read_csv(res_file)
#     total_can = get_total_number_of_candidates_file_by_dataframe("Number of Candidates: ", data)
#     total_mov = get_total_number_of_candidates_file_by_dataframe("Total of Movelets: ", data)
#     trajs_looked = get_total_number_of_candidates_file_by_dataframe("Trajs. Looked: ", data)
#     trajs_ignored = get_total_number_of_candidates_file_by_dataframe("Trajs. Ignored: ", data)
#     time = get_total_number_of_ms("Processing time: ", data)
    
#     print('# <=====================================================>')
#     print('# '+file)
#     print("# Number of Candidates: " + str(total_can))
#     print("# Total of Movelets:    " + str(total_mov))
#     print("# Processing time:      " + str(time) + ' ms -- %d:%d:%d' % printHour(time))
#     print('# --')
    
#     acc  = getACC_SVM(prefix, method) * 100
#     if acc is not 0:
#         print("# SVM ACC:    " + acc)
    
#     acc  = getACC_RF(prefix, method) * 100
#     if acc is not 0:
#         print("# Random Forest ACC:    " + acc)
        
#     acc  = getACC_MLP(prefix, method) * 100
#     if acc is not 0:
#         print("# Neural Network ACC:   " + acc)
        
    
#     print('# --')
#     print("# Total of Trajs. Looked: " + str(trajs_looked))
#     print("# Total of Trajs. Ignored:   " + str(trajs_ignored))
#     print("# Total of Trajs.:    " + str(trajs_looked+trajs_ignored))

# --------------------------------------------------------------------------------->   
def read_csv(file_name):
#     file_name = DIR_V1 + "results/"+file_name + '.txt'
    data = pd.read_csv(file_name, header = None, error_bad_lines=False, warn_bad_lines=False, delimiter='-=-')
    data.columns = ['content']
    return data

def get_lines_with_separator(data, str_splitter):
    lines_with_separation = []
    for index,row in data.iterrows():#
        if str_splitter in row['content']:
            print(row)
            lines_with_separation.insert(len(lines_with_separation), index)
    return lines_with_separation

def get_titles(data):
    titles = []
    for index,row in data.iterrows():#
        if "Loading train and test data from" in row['content']:
            titles.insert(len(titles), row['content'])
    return titles

def split_df_to_dict(data, lines_with_separation):
    df_dict = {}
    lines_with_separation.pop(0)
    previous_line = 0
    for line in lines_with_separation:#
        print(data.iloc[previous_line:line,:])
        df_dict[previous_line] = data.iloc[previous_line:line,:]
        previous_line=line
    df_dict['last'] = data.iloc[previous_line:,:]
    return df_dict

def get_total_number_of_candidates_file(str_target, df_dict):
    total_per_file = []
    for key in df_dict:
        total = 0
        for index,row in df_dict[key].iterrows():
            if str_target in row['content']:
                number = row['content'].split(str_target)[1]
                total = total + int(number)
        total_per_file.insert(len(total_per_file), total)
    return total_per_file

def get_total_number_of_candidates_file_by_dataframe(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(".")[0]
            total = total + int(number)
    return total

def get_total_number_of_ms(str_target, df):
    total = 0
    for index,row in df.iterrows():
        if str_target in row['content']:
            number = row['content'].split(str_target)[1]
            number = number.split(" milliseconds")[0]
            total = total + int(number)
    return total

def split_string(string, delimiter):
    return str(string.split(delimiter)[1])

# --------------------------------------------------------------------------------->  
# Importing the dataset
def loadData(dir_path):
    print("Loading train and test data from... " + dir_path)
    dataset_train = pd.read_csv(os.path.join(dir_path, "train.csv"))
    dataset_test  = pd.read_csv(os.path.join(dir_path, "test.csv"))
#     n_jobs = N_THREADS
    print("Done.")

    nattr = len(dataset_train.iloc[1,:])
    print("Number of attributes: " + str(nattr))

    # Separating attribute data (X) than class attribute (y)
    X_train = dataset_train.iloc[:, 0:(nattr-1)].values
    y_train = dataset_train.iloc[:, (nattr-1)].values

    X_test = dataset_test.iloc[:, 0:(nattr-1)].values
    y_test = dataset_test.iloc[:, (nattr-1)].values

    # Replace distance 0 for presence 1
    # and distance 2 to non presence 0
    X_train[X_train == 0] = 1
    X_train[X_train == 2] = 0
    X_test[X_test == 0] = 1
    X_test[X_test == 2] = 0
    
    # Scaling data
    min_max_scaler = preprocessing.MinMaxScaler()
    X_train = min_max_scaler.fit_transform(X_train)
    X_test = min_max_scaler.transform(X_test)
    
    return X_train, y_train, X_test, y_test

# --------------------------------------------------------------------------------->  
def printLatex(df, ajust=12):
    n_cols = (len(df.columns)-2)
    n_rows = len(df)
    
    print('\\begin{table*}[!ht]')
    print('\\centering')
    print('\\begin{tabular}{|c|r||'+('r|'*n_cols)+'}')
    print('\\hline')
    print('\\hline')
    print((' & '.join(df.columns)) + ' \\\\')
    print('\\hline')
    print('\\multirow{'+str(n_rows)+'}{2cm}{'+df['Dataset'][0]+'}')
    
    print(printLatex_line(df, 0, ajust))
    print(printLatex_line(df, 1, ajust))
    print('\\cline{2-'+str(n_cols+2)+'}')
    print(printLatex_line(df, 2, ajust))
    print(printLatex_line(df, 3, ajust))
    print(printLatex_line(df, 4, ajust))
    print('\\cline{2-'+str(n_cols+2)+'}')
    print(printLatex_line(df, 5, ajust))
    print(printLatex_line(df, 6, ajust))
    print(printLatex_line(df, 7, ajust))
    print(printLatex_line(df, 8, ajust))
    print('\\cline{2-'+str(n_cols+2)+'}')
    print(printLatex_line(df, 9, ajust))
    print(printLatex_line(df, 10,ajust))
    
    print('\\hline')
    print('\\hline')
    print('\\end{tabular}')
    print('\\caption{Results for '+df['Dataset'][0]+' dataset.}')
    print('\\label{tab:results_'+df['Dataset'][0]+'}')
    print('\\end{table*}')
    
def printLatex_line(df, l, ajust=12):
    line = str(df.at[l,df.columns[1]]).rjust(15, ' ') + ' '
    for i in range(2, len(df.columns)):
        line = line + '& '+ str(df.at[l,df.columns[i]]).rjust(ajust, ' ') + ' '
    line = line + '\\\\'
    return line
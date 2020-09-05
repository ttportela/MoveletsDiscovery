'''
Created on Aug, 2020

@author: Tarlis Portela
'''
#TODO: RF não pode fazer aquele reshape?
#TODO: MARC Model

# --------------------------------------------------------------------------------
# ANALYSIS By Ensemble Learning Models
from datetime import datetime
from automatize.analysis import loadData
from automatize.ensemble_models.movelets_model import model_movelets

def ApproachEnsemble(dir_path, dir_path2, save_results=True, method2='poifreq', modelfolder='model'):
        
    import os
    import pandas as pd
    import numpy as np
    import tensorflow
        
    X_train, y_train, X_test, y_test = loadData(dir_path)
        
    labels   = list(set(y_train))
    y_train  = [labels.index(x) for x in y_train]
    y_test   = [labels.index(x) for x in y_test]
#     y_train2 = [labels.index(x) for x in y_train2]
#     y_test2  = [labels.index(x) for x in y_test2]
    
    print("Building Ensemble models")
    time = datetime.now()
    
    tensorflow.keras.backend.clear_session()
    pred1 = model_movelets(X_train, y_train, X_test, y_test)

    tensorflow.keras.backend.clear_session()
    if method2 is 'marc':
        from automatize.ensemble_models.marc_model import model_marc
        pred2 = model_marc(dir_path2)
    if method2 is 'poifreq':
        from automatize.ensemble_models.poifreq_model import model_poifreq
        pred2 = model_poifreq(dir_path2)
    if method2 is 'rf':
        from automatize.ensemble_models.randomforrest_model import model_rf
        pred2 = model_rf(dir_path2)

    y_pred1 = [np.argmax(f) for f in pred1]
    y_pred2 = [np.argmax(f) for f in pred2]

    final_pred = (pred1*0.5+pred2*0.5)
    y_pred = [np.argmax(f) for f in final_pred]
    
    print('Models results:')
    print(get_line(y_test, y_pred1))
    print(get_line(y_test, y_pred2))
    
    print('Ensembled results:')
    line=get_line(y_test, y_pred)
    print(line)
    
    # ---------------------------------------------------------------------------------
    if (save_results) :
        if not os.path.exists(os.path.join(dir_path, modelfolder)):
            os.makedirs(os.path.join(dir_path, modelfolder))
        report = classification_report(y_test, classifier.predict(X_test) )
        classification_report_csv(report, os.path.join(dir_path, modelfolder, "model_approachEnsemble_report.csv"),"Ensemble") 
        pd.DataFrame(line).to_csv(os.path.join(dir_path, modelfolder, "model_approachEnsemble_history.csv")) 
    
    # ----------------------------------------------------------------------------------
    time = (datetime.now()-time).total_seconds() * 1000
    # ---------------------------------------------------------------------------------
    print("Done. " + str(time) + " milliseconds")
    print("---------------------------------------------------------------------------------")
    return time
# --------------------------------------------------------------------------------

# Statistics:
def get_line(y_true, y_pred):
    acc = accuracy(y_true, y_pred)
    f1  = f1_macro(y_true, y_pred)
    prec= precision_macro(y_true, y_pred)
    rec = recall_macro(y_true, y_pred)
    accTop5 = 0 #calculateAccTop5(classifier, X_test, y_test, 5)
    line=[acc, f1, prec, rec, accTop5]
    return line

def precision_macro(y_true, y_pred):
    from sklearn.metrics import precision_score
    return precision_score(y_true, y_pred, average='macro')
def recall_macro(y_true, y_pred):
    from sklearn.metrics import recall_score
    return recall_score(y_true, y_pred, average='macro')
def f1_macro(y_true, y_pred):
    from sklearn.metrics import f1_score
    return f1_score(y_true, y_pred, average='macro')
def accuracy(y_true, y_pred):
    from sklearn.metrics import accuracy_score
    return accuracy_score(y_true, y_pred, normalize=True)
# --------------------------------------------------------------------------------
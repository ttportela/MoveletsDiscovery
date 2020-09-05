# MODELS:
def model_rf(keys, vocab_size, num_classes, max_length, x_train, y_train, x_test, y_test):
    from sklearn.ensemble import RandomForestClassifier
    classifier = RandomForestClassifier()

    nx, nsamples, ny = np.shape(x_train)
    x_train = x_train.reshape((nsamples,nx*ny))
    nx, nsamples, ny = np.shape(x_test)
    x_test = x_test.reshape((nsamples,nx*ny))

    print("[Data Model:] Building random forrest")
    classifier = RandomForestClassifier(n_estimators=500, n_jobs = -1, random_state = 1, criterion = 'gini', bootstrap=True)
    classifier.fit(x_train, y_train)
    print("[Data Model:] OK")

    return classifier.predict_proba(x_test)

#     return classifier

def model_movelets(X_train, y_train, X_test, y_test):
    from keras.models import Sequential
    from keras.layers import Dense, Dropout
    from keras.optimizers import Adam
    import os
    import pandas as pd
    from automatize.analysis import loadData
    from automatize.Methods import classification_report, classification_report_csv, calculateAccTop5, f1

    # ---------------------------------------------------------------------------
    # Neural Network - Definitions:
    par_dropout = 0.5
    par_batch_size = 200
    par_epochs = 80
    par_lr = 0.00095

    # Building the neural network-
    print("[Movelets:] Building neural network")
    lst_par_epochs = [80,50,50,30,20]
    lst_par_lr = [0.00095,0.00075,0.00055,0.00025,0.00015]

    nattr = len(X_train[1,:])    

    # Scaling y and transforming to keras format
    from sklearn import preprocessing
    le = preprocessing.LabelEncoder()
    le.fit(y_train)
    y_train = le.transform(y_train) 
    y_test = le.transform(y_test)
    from keras.utils import to_categorical
    y_train1 = to_categorical(y_train)
    y_test1 = to_categorical(y_test)
    nclasses = len(le.classes_)
    from keras import regularizers

    #Initializing Neural Network
    classifier = Sequential()
    # Adding the input layer and the first hidden layer
    classifier.add(Dense(units = 100, kernel_initializer = 'uniform', kernel_regularizer= regularizers.l2(0.02), activation = 'relu', input_dim = (nattr)))
    #classifier.add(BatchNormalization())
    classifier.add(Dropout( par_dropout )) 
    # Adding the output layer       
    classifier.add(Dense(units = nclasses, kernel_initializer = 'uniform', activation = 'softmax'))
    # Compiling Neural Network
    adam = Adam(lr=par_lr)
    classifier.compile(optimizer = adam, loss = 'categorical_crossentropy', metrics = ['accuracy','top_k_categorical_accuracy',f1])
    # Fitting our model 
    history = classifier.fit(X_train, y_train1, validation_data = (X_test, y_test1), batch_size = par_batch_size, epochs = par_epochs, verbose=0)

    print("[Movelets:] OK")

    return classifier.predict(X_test)
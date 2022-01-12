/* 
 * This file is part of the UEA Time Series Machine Learning (TSML) toolbox.
 *
 * The UEA TSML toolbox is free software: you can redistribute it and/or 
 * modify it under the terms of the GNU General Public License as published 
 * by the Free Software Foundation, either version 3 of the License, or 
 * (at your option) any later version.
 *
 * The UEA TSML toolbox is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with the UEA TSML toolbox. If not, see <https://www.gnu.org/licenses/>.
 */
 
package br.ufsc.mov3lets.run;

import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.math3.util.Pair;

/**
 * File for reading sktime format data into TimeSeriesInstances object
 * 
 * @author Aaron Bostrom, pushed 22/4/2020
 */

public class TSReader {

    // need to change this to a map function.
    public static final String PROBLEM_NAME = "@problemName";
    public static final String TIME_STAMPS = "@timeStamps";
    public static final String CLASS_LABEL = "@classLabel";
    public static final String UNIVARIATE = "@univariate";
    public static final String MISSING = "@missing";
    public static final String DATA = "@data";

    private HashMap<String, String> variables;

    private final StreamTokenizer m_Tokenizer;
    private int m_Lines;

    private String description;
    private String problemName;
    private boolean univariate;
    private boolean missing;
    private boolean timeStamps;
    private boolean classLabel;
    private List<String> classLabels;

    private List<List<List<Double>>> raw_data;

    private List<Double> raw_labels;

    public TSReader(Reader reader) throws IOException {
        variables = new HashMap<>();
        m_Tokenizer = new StreamTokenizer(reader);
        initTokenizer();

        readHeader();

        CreateTimeSeriesInstances();
    }

    private void CreateTimeSeriesInstances() throws IOException {
        raw_data = new ArrayList<>();
        raw_labels = new ArrayList<>();

        // read each line and extract a data Instance
        Pair<List<List<String>>, String> multi_series_and_label;
        // extract the multivariate series, and the possible label.
        while ((multi_series_and_label = readMultivariateInstance()) != null) {
//            raw_data.add(multi_series_and_label.var1);
//            raw_labels.add(multi_series_and_label.var2);
        }

        // create timeseries instances object.
//        m_data = new TimeSeriesInstances(raw_data, classLabels.toArray(new String[classLabels.size()]), raw_labels);
//        m_data.setProblemName(problemName);
////        m_data.setHasTimeStamps(timeStamps); // todo this has been temp removed, should be computed from the data
//        m_data.setDescription(description);
    }

//    public TimeSeriesInstances GetInstances() {
//        return m_data;
//    }

    private Pair<List<List<String>>, String> readMultivariateInstance() throws IOException {
        getFirstToken();
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
            return null;
        }

        List<List<String>> multi_timeSeries = new ArrayList<>();
        String classValue = "";

        ArrayList<String> timeSeries = new ArrayList<>();
        do {
            // this means we're about to get the class value
            if (m_Tokenizer.ttype == ':' && classLabel) {
                // add the current time series to the list.
                multi_timeSeries.add(timeSeries);
                timeSeries = new ArrayList<>();
                System.out.println("---");
            } else {
//            	String val = m_Tokenizer.nval;
//                //Nasty hack to deal with Exponents not being supported in tokenizer - Look away!
//                //Aaron 15/02/2021
//                if(m_Tokenizer.sval != null && m_Tokenizer.sval.contains("E")){
//                    val = Double.parseDouble(m_Tokenizer.nval + m_Tokenizer.sval);
//                    //remove the value we just added. as it was a partial.
//                    timeSeries.remove(timeSeries.size()-1);
//                }
//                else if(m_Tokenizer.sval == "?")
//                    val = Double.NaN;
//
//                timeSeries.add(val);
                classValue = m_Tokenizer.sval == null ? "" + m_Tokenizer.nval : m_Tokenizer.sval; 
                
            }
            System.out.println(m_Tokenizer.sval + "|" + m_Tokenizer.nval + "|" + m_Tokenizer.ttype);
//            m_Tokenizer.
            getNextToken();
        } while (m_Tokenizer.ttype != StreamTokenizer.TT_EOL);
        
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOL)
        	throw new RuntimeException("OK");

        // don't add the last series to the list, instead extract the first element and
        // figure out what the class value is.
//        String classVal = classLabel ? (double) this.classLabels.indexOf(classValue) : -1.0;
        return new Pair<>(multi_timeSeries, classValue);
    }

    private void initTokenizer() {
        // Setup the tokenizer to read the stream.
        m_Tokenizer.resetSyntax();

        //// ignore 0-9 chars
        m_Tokenizer.wordChars(' ' + 1, '\u00FF');

//        m_Tokenizer.parseNumbers();

        // setup the white space tokens
        m_Tokenizer.whitespaceChars(' ', ' ');
        m_Tokenizer.whitespaceChars(',', ',');

        // if we encounter a colon it means we need to start a new line? or it means a
        // new multivariate instance.
        m_Tokenizer.ordinaryChar(':');

        // setup the comment char
        m_Tokenizer.commentChar('#');

        // end of line is a significant token. it means the end of an instance.
        m_Tokenizer.eolIsSignificant(true);
    }

    // this function reads upto the @data bit in the file.
    protected void readHeader() throws IOException {
        // first token should be @problem name. as we skip whitespace and comments.

        // this gets the token there may be weirdness at the front of the file.
        getFirstToken();
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
            errorMessage("premature end of file");
        }

        do {

            String token = m_Tokenizer.sval;

            if (token.equalsIgnoreCase(CLASS_LABEL)) {
                ExtractClassLabels();
            } else {
                variables.put(token, ExtractVariable(token));
            }

            getNextToken();

        } while (!m_Tokenizer.sval.equalsIgnoreCase(DATA));

        // these are required.
        problemName = variables.get(PROBLEM_NAME);
        if (problemName == null) {
            errorMessage("keyword " + PROBLEM_NAME + " expected");
        }

        if (variables.get(UNIVARIATE) == null) {
            errorMessage("keyword " + UNIVARIATE + " expected");
        } else {
            univariate = Boolean.parseBoolean(variables.get(UNIVARIATE));
        }

        // set optionals.
        if (variables.get(MISSING) != null)
            missing = Boolean.parseBoolean(variables.get(MISSING));
        if (variables.get(TIME_STAMPS) != null)
            timeStamps = Boolean.parseBoolean(variables.get(TIME_STAMPS));

        // clear out last tokens.
        getLastToken(false);
    }

    private void ExtractClassLabels() throws IOException {
        classLabels = new ArrayList<>();
        getNextToken();
        classLabel = Boolean.parseBoolean(m_Tokenizer.sval);

        if (!classLabel) {
            getLastToken(false);
            return;
        }

        getNextToken();
        // now read all the class values until we reach the EOL
        do {
            classLabels.add(m_Tokenizer.sval == null ? "" + m_Tokenizer.nval : m_Tokenizer.sval);
            m_Tokenizer.nextToken();
        } while (m_Tokenizer.ttype != StreamTokenizer.TT_EOL);
    }

    private String ExtractVariable(String VARIABLE) throws IOException {
        // check if the current token matches the hardcoded value for @types e.g.
        // @problemName etc.
        getNextToken();
        String value = m_Tokenizer.sval;
        getLastToken(false);
        return value;
    }

    /**
     * Gets next token, skipping empty lines.
     *
     * @throws IOException if reading the next token fails
     */
    protected void getFirstToken() throws IOException {
        while (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {}
        ;
        // this handles quotations single and double/
        if ((m_Tokenizer.ttype == '\'') || (m_Tokenizer.ttype == '"')) {
            m_Tokenizer.ttype = StreamTokenizer.TT_WORD;
            // this handles ? in the file.
        } else if ((m_Tokenizer.ttype == StreamTokenizer.TT_WORD) && (m_Tokenizer.sval.equals("?"))) {
            m_Tokenizer.ttype = '?';
        }
    }

    /**
     * Gets next token, checking for a premature and of line.
     *
     * @throws IOException if it finds a premature end of line
     */
    protected void getNextToken() throws IOException {
        if (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
            errorMessage("premature end of line");
        }
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
            errorMessage("premature end of file");
        } else if ((m_Tokenizer.ttype == '\'') || (m_Tokenizer.ttype == '"')) {
            m_Tokenizer.ttype = StreamTokenizer.TT_WORD;
        } else if ((m_Tokenizer.ttype == StreamTokenizer.TT_WORD) && (m_Tokenizer.sval.equals("?"))) {
            m_Tokenizer.ttype = '?';
        }
    }

    /**
     * Gets token and checks if its end of line.
     *
     * @param endOfFileOk whether EOF is OK
     * @throws IOException if it doesn't find an end of line
     */
    protected void getLastToken(boolean endOfFileOk) throws IOException {
        if ((m_Tokenizer.nextToken() != StreamTokenizer.TT_EOL)
                && ((m_Tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
            errorMessage("end of line expected");
        }
    }

    /**
     * Throws error message with line number and last token read.
     *
     * @param msg the error message to be thrown
     * @throws IOException containing the error message
     */
    protected void errorMessage(String msg) throws IOException {
        String str = msg + ", read " + m_Tokenizer.toString();
        if (m_Lines > 0) {
            int line = Integer.parseInt(str.replaceAll(".* line ", ""));
            str = str.replaceAll(" line .*", " line " + (m_Lines + line - 1));
        }
        throw new IOException(str);
    }
}

package br.ufsc.mov3lets.method.loader;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import br.ufsc.mov3lets.method.structures.descriptor.AttributeDescriptor;
import br.ufsc.mov3lets.method.structures.descriptor.Descriptor;
import br.ufsc.mov3lets.model.MAT;
import br.ufsc.mov3lets.model.Point;

/**
 * The Class CSVLoader.
 *
 * @param <T> the generic type
 */
public class TSInternLoader_old<T extends MAT<?>> implements InterningLoaderAdapter<T>  {
	
    public final char SEQ_SEP = ':';
	
    public final String PROBLEM_NAME = "@problemName";
    public final String TIME_STAMPS = "@timeStamps";
    public final String CLASS_LABEL = "@classLabel";
    public final String UNIVARIATE = "@univariate";
    public final String MISSING = "@missing";
    public final String DATA = "@data";
	
	protected static int tid = 1;
	
	protected HashMap<String, String> variables;
	protected List<String> classLabels;
	
	protected StreamTokenizer m_Tokenizer;
//	protected int m_Lines;
	
	protected List<AttributeDescriptor> attributes = new ArrayList<AttributeDescriptor>();

	protected HashMap<String, String> initTokenizer(String file) throws IOException {
		
		HashMap<String, String> variables = new HashMap<>();
        
		m_Tokenizer = new StreamTokenizer(new FileReader(new File(file)));
        
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
        m_Tokenizer.ordinaryChar(SEQ_SEP);

        // setup the comment char
        m_Tokenizer.commentChar('#');

        // end of line is a significant token. it means the end of an instance.
        m_Tokenizer.eolIsSignificant(true);
        
        // first token should be @problem name. as we skip whitespace and comments.
        // this gets the token there may be weirdness at the front of the file.
        firstToken();
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        	throw new RuntimeException("Premature end of file while reading data");
        }

        do {
            String token = m_Tokenizer.sval;

            if (token.equalsIgnoreCase(CLASS_LABEL)) {
//                ExtractClassLabels();
            	classLabels = new ArrayList<>();
                nextToken();
                boolean classLabel = Boolean.parseBoolean(m_Tokenizer.sval);

                if (!classLabel) {
                    lastToken(false);
                    break;
                }

                nextToken();
                // now read all the class values until we reach the EOL
                do {
                    classLabels.add(m_Tokenizer.sval == null ? "" + m_Tokenizer.nval : m_Tokenizer.sval);
                    m_Tokenizer.nextToken();
                } while (m_Tokenizer.ttype != StreamTokenizer.TT_EOL);
            } else {
            	nextToken();
                String value = m_Tokenizer.sval;
                lastToken(false);
                variables.put(token, value);
            }

            nextToken();

        } while (!m_Tokenizer.sval.equalsIgnoreCase(DATA));

//        // these are required.
//        String problemName = variables.get(PROBLEM_NAME);
//        if (problemName == null) {
//        	throw new RuntimeException("Error while reading data, keyword " + PROBLEM_NAME + " expected");
//        }
//
//        boolean univariate = false;
//        if (variables.get(UNIVARIATE) == null) {
//        	throw new RuntimeException("Error while reading data, keyword " + UNIVARIATE + " expected");
//        } else {
//            univariate = Boolean.parseBoolean(variables.get(UNIVARIATE));
//        }
//
//        // set optionals.
//        boolean missing = false, timeStamps = false;
//        if (variables.get(MISSING) != null)
//            missing = Boolean.parseBoolean(variables.get(MISSING));
//        if (variables.get(TIME_STAMPS) != null)
//            timeStamps = Boolean.parseBoolean(variables.get(TIME_STAMPS));

        // clear out last tokens.
        lastToken(false);
        
//        System.out.println(variables.toString());
//        System.out.println(classLabels.toString());
        
		return variables;
	}

    /**
     * Gets next token, skipping empty lines.
     *
     * @throws IOException if reading the next token fails
     */
    protected void firstToken() throws IOException {
        while (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {};
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
	protected void nextToken() throws IOException {
        if (m_Tokenizer.nextToken() == StreamTokenizer.TT_EOL) {
        	throw new RuntimeException("Premature end of line while reading data");
        }
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
        	throw new RuntimeException("Premature end of file while reading data");
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
    protected void lastToken(boolean endOfFileOk) throws IOException {
        if ((m_Tokenizer.nextToken() != StreamTokenizer.TT_EOL)
                && ((m_Tokenizer.ttype != StreamTokenizer.TT_EOF) || !endOfFileOk)) {
        	throw new RuntimeException("Unexpected end of line while reading data");
        }
    }

    protected MAT<?> readMultivariateInstance() throws IOException {
        firstToken();
        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOF) {
            return null;
        }

        // IF MO type is String:
    	//			MO mo = new MO();
        MAT<String> mat = new MAT<String>();
		mat.setTid(tid++);
		
		int i = 0, attr = 0;
		String value = null;

//		print();
		do{

			if(m_Tokenizer.ttype == SEQ_SEP && isClass()){
                //this means we're about to get the class value or the next dimension
            	i = 0;
            	attr++;
            	m_Tokenizer.nextToken();
//    			print();
            }
            else{       
    			value = m_Tokenizer.sval;
    			
    			m_Tokenizer.nextToken();
//    			print();
    			if (m_Tokenizer.ttype != StreamTokenizer.TT_EOL) {

	            	// For each attribute of POI
	    			Point poi;
	    			if (mat.getPoints().size() <= i) {
	    				poi = new Point();	
	    				poi.setTrajectory(mat);
	    				mat.getPoints().add(poi);
	    			} else {
	    				poi = mat.getPoints().get(i);
	    			}
	    			poi.getAspects().add(this.instantiateAspect(getAttributeDesc(attr), value));
	    			i++;
	    			
    			} // else is the class value
            }
            
        } while(m_Tokenizer.ttype != StreamTokenizer.TT_EOL);
		
		// Can use like this:
//		mo = (MO) new MovingObject<String>(label);
		// OR -- this for typing String:
		mat.setMovingObject(value);

//        if (m_Tokenizer.ttype == StreamTokenizer.TT_EOL)
//        	throw new RuntimeException("OK");
		
        return mat;
    }

//    private void print() {
//		System.out.println(m_Tokenizer.ttype+"|"+m_Tokenizer.nval+"|"+m_Tokenizer.sval);
//	}

	protected AttributeDescriptor getAttributeDesc(int i) {
		try {
			return getAttributes().get(i);
		} catch (Exception e) {
			getAttributes().add(new AttributeDescriptor(
					i, "numeric", "attr"+i, "difference", -1.0));
			return getAttributes().get(i);
		}
	}

	protected boolean isClass() {
		return classLabels != null && !classLabels.isEmpty();
	}

	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		initTokenizer(file + ".ts");
		
		if (descriptor != null)
			getAttributes().addAll(descriptor.getAttributes());
		
		List<MAT<?>> trajectories = new ArrayList<MAT<?>>();
		MAT<?> mat = null;
		
		while ((mat = readMultivariateInstance()) != null) {
			trajectories.add(mat);
		}
		
		return (List<T>) trajectories;
	}
	
	public List<AttributeDescriptor> getAttributes() {
		return attributes;
	}
	
	public void setAttributes(List<AttributeDescriptor> attributes) {
		this.attributes = attributes;
	}

}

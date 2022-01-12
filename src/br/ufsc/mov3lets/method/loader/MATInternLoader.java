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

/**
 * The Class CSVLoader.
 *
 * @param <T> the generic type
 */
public class MATInternLoader<T extends MAT<?>> extends TSInternLoader<T>  {
	
//    public final char SEQ_SEP = ':';
	
    public final String DATASET_NAME = "@name";
    public final String DIMENSION_TYPES = "@dimensionTypes";
    public final String DIMENSION_DISTANCES = "@dimensionDistances";
	
	protected List<String> dimensionTypes;
	protected List<String> dimensionDistances;
	
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
        
        m_Tokenizer.quoteChar('"');

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
            } else if (token.equalsIgnoreCase(DIMENSION_TYPES)) {
            	dimensionTypes = readLabels(); 
//            	System.out.println(dimensionTypes);
			} else if (token.equalsIgnoreCase(DIMENSION_DISTANCES)) {
				dimensionDistances = readLabels(); 
//            	System.out.println(dimensionDistances);
			} else {
				nextToken();
			    String value = m_Tokenizer.sval;
			    lastToken(false);
			    variables.put(token, value);
			}

            nextToken();

        } while (!m_Tokenizer.sval.equalsIgnoreCase(DATA));

        // clear out last tokens.
        lastToken(false);
        
		return variables;
	}

    protected List<String> readLabels() throws IOException {
    	List<String> labels = new ArrayList<>();
        nextToken();
        // now read all the class values until we reach the EOL
        do {
            labels.add(m_Tokenizer.sval == null ? "" + m_Tokenizer.nval : m_Tokenizer.sval);
            m_Tokenizer.nextToken();
        } while (m_Tokenizer.ttype != StreamTokenizer.TT_EOL);
        
        return labels;
	}

    @Override
    protected AttributeDescriptor getAttributeDesc(int i) {
		try {
			return getAttributes().get(i);
		} catch (Exception e) {
			getAttributes().add(new AttributeDescriptor(
					i, getLabel(dimensionTypes, i, "nominal"), "attr"+i, getLabel(dimensionDistances, i, "equals"), -1.0));
			return getAttributes().get(i);
		}
	}

    protected String getLabel(List<String> labels, int i, String def) {
		if (labels == null || labels.isEmpty())
			return def;
		else if (labels.size() > i)
			return labels.get(i);
		else
			return labels.get(0);
	}

	@Override
	public List<T> loadTrajectories(String file, Descriptor descriptor) throws IOException {
		initTokenizer(file + ".mat");
		
		if (descriptor != null)
			getAttributes().addAll(descriptor.getAttributes());
		
		List<MAT<?>> trajectories = new ArrayList<MAT<?>>();
		MAT<?> mat = null;
		
		while ((mat = readMultivariateInstance()) != null) {
			trajectories.add(mat);
		}
		
		return (List<T>) trajectories;
	}

}

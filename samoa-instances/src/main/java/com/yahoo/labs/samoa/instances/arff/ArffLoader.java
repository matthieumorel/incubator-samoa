package com.yahoo.labs.samoa.instances.arff;

/*
 * #%L
 * SAMOA
 * %%
 * Copyright (C) 2013 - 2015 Yahoo! Inc.
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */

import com.yahoo.labs.samoa.instances.Attribute;
import com.yahoo.labs.samoa.instances.DenseInstance;
import com.yahoo.labs.samoa.instances.Instance;
import com.yahoo.labs.samoa.instances.SparseInstance;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ArffLoader {

  private final Reader reader;
  private final int size;
  private final int classAttribute;
  transient protected StreamTokenizer streamTokenizer;
  private ArffMetadata arffMetadata;
  private ArrayList<Attribute> attributes;

  public ArffLoader(Reader reader, int size, int classAttribute) {
    this.reader = reader;
    this.size = size;
    this.classAttribute = classAttribute;
    initStreamTokenizer(reader);
  }

  public Instance readInstance(Reader reader) {
    if (streamTokenizer == null) {
      initStreamTokenizer(reader);
    }
    while (streamTokenizer.ttype == StreamTokenizer.TT_EOL) {
      try {
        streamTokenizer.nextToken();
      } catch (IOException ex) {
        Logger.getLogger(ArffLoader.class.getName()).log(Level.SEVERE, null, ex);
      }
    }
    if (streamTokenizer.ttype == '{') {
      return readInstanceSparse();
    } else {
      return readInstanceDense();
    }

  }

  private Instance readInstanceSparse() {
    SparseInstance.Builder builder = new SparseInstance.Builder();
    int numAttribute;
    try {
      streamTokenizer.nextToken(); // Remove the '{' char
      // For each line
      while (streamTokenizer.ttype != StreamTokenizer.TT_EOL
          && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
        while (streamTokenizer.ttype != '}') {
          if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            numAttribute = (int) streamTokenizer.nval;
          } else {
            numAttribute = Integer.parseInt(streamTokenizer.sval);
          }
          streamTokenizer.nextToken();

          if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            this.setSparseAttributeOrLabel(numAttribute, streamTokenizer.nval, true, builder);
          } else if (streamTokenizer.sval != null && (streamTokenizer.ttype == StreamTokenizer.TT_WORD
              || streamTokenizer.ttype == 34)) {
            if (attributes.get(numAttribute).isNumeric()) {
              this.setSparseAttributeOrLabel(numAttribute,
                  Double.valueOf(streamTokenizer.sval).doubleValue(), true, builder);
            } else {
              this.setSparseAttributeOrLabel(numAttribute, this.arffMetadata
                  .attribute(numAttribute).indexOfValue(streamTokenizer.sval), false, builder);
            }
          }
          streamTokenizer.nextToken();
        }
        streamTokenizer.nextToken(); // Remove the '}' char
      }
      streamTokenizer.nextToken();
      // System.out.println("EOL");
      // }

    } catch (IOException ex) {
      Logger.getLogger(ArffLoader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return builder.build();

  }

  private void setSparseAttributeOrLabel(int numAttribute, double value, boolean isNumber,
      SparseInstance.Builder builder) {
    double valueAttribute;
    if (isNumber && this.arffMetadata.attribute(numAttribute).isNominal()) {
      valueAttribute = this.arffMetadata.attribute(numAttribute).indexOfValue(Double.toString(value));
    } else {
      valueAttribute = value;
    }
    if (this.arffMetadata.classIndex() == numAttribute) {
      builder.setLabel(valueAttribute);
    } else {
      builder.setAttribute(numAttribute, valueAttribute);
    }
  }

  public Instance readInstanceDense() {
    DenseInstance.Builder builder = new DenseInstance.Builder();
    int numAttribute = 0;
    try {
      while (numAttribute == 0 && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
        // For each line
        while (streamTokenizer.ttype != StreamTokenizer.TT_EOL
            && streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
          // For each item
          if (streamTokenizer.ttype == StreamTokenizer.TT_NUMBER) {
            setDenseAttributeOrLabel(numAttribute, streamTokenizer.nval, true, builder);
            numAttribute++;

          } else if (streamTokenizer.sval != null && (streamTokenizer.ttype == StreamTokenizer.TT_WORD
              || streamTokenizer.ttype == 34)) {
            // System.out.println(streamTokenizer.sval + "Str");
            boolean isNumeric = attributes.get(numAttribute).isNumeric();
            double value;
            if ("?".equals(streamTokenizer.sval)) {
              value = Double.NaN; // Utils.missingValue();
            } else if (isNumeric == true) {
              value = Double.valueOf(streamTokenizer.sval).doubleValue();
            } else {
              value = this.arffMetadata.attribute(numAttribute).indexOfValue(streamTokenizer.sval);
            }
            setDenseAttributeOrLabel(numAttribute, value, isNumeric, builder);
            numAttribute++;
          }
          streamTokenizer.nextToken();
        }
        streamTokenizer.nextToken();
      }

    } catch (IOException ex) {
      Logger.getLogger(ArffLoader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return (numAttribute > 0) ? builder.build() : null;
  }

  private void setDenseAttributeOrLabel(int numAttribute, double value, boolean isNumber,
      DenseInstance.Builder builder) {
    double valueAttribute;
    if (isNumber && this.arffMetadata.attribute(numAttribute).isNominal()) {
      valueAttribute = this.arffMetadata.attribute(numAttribute).indexOfValue(Double.toString(value));
      // System.out.println(value +"/"+valueAttribute+" ");

    } else {
      valueAttribute = value;
      // System.out.println(value +"/"+valueAttribute+" ");
    }
    if (this.arffMetadata.classIndex() == numAttribute) {
      builder.setLabel(valueAttribute);
    } else {
      builder.setAttribute(numAttribute, valueAttribute);
    }
  }

  private ArffMetadata parseHeader() {

    String relation = "file stream";
    // System.out.println("RELATION " + relation);
    attributes = new ArrayList<Attribute>();
    try {
      streamTokenizer.nextToken();
      while (streamTokenizer.ttype != StreamTokenizer.TT_EOF) {
        // For each line
        // if (streamTokenizer.ttype == '@') {
        if (streamTokenizer.ttype == StreamTokenizer.TT_WORD && streamTokenizer.sval.startsWith("@") == true) {
          // streamTokenizer.nextToken();
          String token = streamTokenizer.sval.toUpperCase();
          if (token.startsWith("@RELATION")) {
            streamTokenizer.nextToken();
            relation = streamTokenizer.sval;
            // System.out.println("RELATION " + relation);
          } else if (token.startsWith("@ATTRIBUTE")) {
            streamTokenizer.nextToken();
            String name = streamTokenizer.sval;
            // System.out.println("* " + name);
            if (name == null) {
              name = Double.toString(streamTokenizer.nval);
            }
            streamTokenizer.nextToken();
            String type = streamTokenizer.sval;
            // System.out.println("* " + name + ":" + type + " ");
            if (streamTokenizer.ttype == '{') {
              streamTokenizer.nextToken();
              List<String> attributeLabels = new ArrayList<String>();
              while (streamTokenizer.ttype != '}') {

                if (streamTokenizer.sval != null) {
                  attributeLabels.add(streamTokenizer.sval);
                  // System.out.print(streamTokenizer.sval + ",");
                } else {
                  attributeLabels.add(Double.toString(streamTokenizer.nval));
                  // System.out.print(streamTokenizer.nval + ",");
                }

                streamTokenizer.nextToken();
              }
              // System.out.println();
              attributes.add(new Attribute(name, attributeLabels));
            } else {
              // Add attribute
              attributes.add(new Attribute(name));
            }

          } else if (token.startsWith("@DATA")) {
            // System.out.print("END");
            streamTokenizer.nextToken();
            break;
          }
        }
        streamTokenizer.nextToken();
      }

    } catch (IOException ex) {
      Logger.getLogger(ArffLoader.class.getName()).log(Level.SEVERE, null, ex);
    }
    return new ArffMetadata(relation, attributes);
  }

  private void initStreamTokenizer(Reader reader) {
    BufferedReader br = new BufferedReader(reader);

    // Init streamTokenizer
    streamTokenizer = new StreamTokenizer(br);

    streamTokenizer.resetSyntax();
    streamTokenizer.whitespaceChars(0, ' ');
    streamTokenizer.wordChars(' ' + 1, '\u00FF');
    streamTokenizer.whitespaceChars(',', ',');
    streamTokenizer.commentChar('%');
    streamTokenizer.quoteChar('"');
    streamTokenizer.quoteChar('\'');
    streamTokenizer.ordinaryChar('{');
    streamTokenizer.ordinaryChar('}');
    streamTokenizer.eolIsSignificant(true);

    arffMetadata = this.parseHeader();
    if (classAttribute < 0) {
      arffMetadata.setClassIndex(arffMetadata.numAttributes() - 1);
      // System.out.print(this.instanceInformation.classIndex());
    } else if (classAttribute > 0) {
      arffMetadata.setClassIndex(classAttribute - 1);
    }
  }

}

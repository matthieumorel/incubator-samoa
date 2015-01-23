package com.yahoo.labs.samoa.instances;

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

import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;

public class SparseInstance extends AbstractInstance {

  private static final long serialVersionUID = 2978800297651832155L;

  private final Map<Integer, Double> attributes;
  private final int numAttributes;

  /**
   * @param attributes
   *          map of attributes: index - value
   * @param numAttributes
   *          total number of attributes (usually more than the size of {@link #attributes} for a sparse instance)
   */
  private SparseInstance(Map<Integer, Double> attributes, int numAttributes,
      double label, double weight,
      BitSet numericAttributes, BitSet nominalAttributes, BitSet dateAttributes) {
    super(label, weight, numericAttributes, nominalAttributes, dateAttributes);
    this.attributes = attributes;
    this.numAttributes = numAttributes;
  }

  @Override
  public int getNumAttributes() {
    return numAttributes;
  }

  @Override
  public double getAttribute(int index) {
    Double value = attributes.get(index);
    if (value == null) {
      return 0;
    } else {
      return value;
    }
  }

  @Override
  public double[] getAttributes() {
    double[] result = new double[getNumAttributes()];
    for (int i = 0; i < result.length; i++) {
      Double value = attributes.get(i);
      if (value == null) {
        result[i] = 0;
      } else {
        result[i] = value;
      }
    }
    return result;
  }

  public static class Builder extends AbstractInstance.Builder {

    private Map<Integer, Double> attributes = new HashMap<>();
    private int numAttributes;

    public Builder setAttribute(Integer index, Double value) {
      attributes.put(index, value);
      return this;
    }

    public Builder numAttributes(int num) {
      this.numAttributes = num;
      return this;
    }

    public Builder addAttributes(Map<Integer, Double> a) {
      attributes.putAll(a);
      return this;
    }

    public SparseInstance build() {
      SparseInstance instance = new SparseInstance(attributes, numAttributes,
          getLabel(), getWeight(),
          numericAttributes, nominalAttributes, dateAttributes);
      return instance;
    }

    /**
     * newBuilderFrom creates a new builder initialized with the data from the instance passed in parameter.
     *
     * Note that the metadata is shallow-copied
     * 
     * @param instance
     * @return
     */
    public static Builder newBuilderFrom(SparseInstance instance) {
      Builder builder = new Builder();
      builder.numericAttributes = (BitSet) instance.numericAttributes.clone();
      builder.nominalAttributes = (BitSet) instance.nominalAttributes.clone();
      builder.dateAttributes = (BitSet) instance.dateAttributes.clone();

      double[] attributes = instance.getAttributes();
      builder.numAttributes(attributes.length);
      for (int i = 0; i < attributes.length; i++) {
        builder.setAttribute(i, attributes[i]);
        builder.setAttributeMetadata(i, instance.isNumeric(i), instance.isNominal(i), instance.isDate(i));
      }
      builder.setLabel(instance.getLabel());
      builder.setWeight(instance.getWeight());
      return builder;
    }
  }
}

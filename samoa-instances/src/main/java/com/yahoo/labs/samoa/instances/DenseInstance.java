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

public class DenseInstance extends AbstractInstance {

  private static final long serialVersionUID = 6129026438277113411L;

  private final double[] attributeValues;

  private DenseInstance(double[] attributeValues,
                        double label, double weight,
                        BitSet numericAttributes, BitSet nominalAttributes, BitSet dateAttributes) {
    super(label, weight, numericAttributes, nominalAttributes, dateAttributes);
    this.attributeValues = attributeValues;
  }

  @Override
  public int getNumAttributes() {
    return attributeValues.length;
  }

  @Override
  public double getAttribute(int index) {
    return attributeValues[index];
  }

  public double[] getAttributes() {
    return attributeValues;
  }


  public static class Builder extends AbstractInstance.Builder {

    private double[] attributeValues;



    public Builder setAttributes(double[] attributeValues) {
      this.attributeValues = attributeValues;
      numericAttributes = new BitSet(attributeValues.length);
      nominalAttributes = new BitSet(attributeValues.length);
      dateAttributes = new BitSet(attributeValues.length);
      return this;
    }

    public Builder setAttribute(int index, double value) {
      attributeValues[index] = value;
      return this;
    }

    public DenseInstance build() {
      DenseInstance instance = new DenseInstance(attributeValues, getLabel(), getWeight(),
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
    public static Builder newBuilderFrom(DenseInstance instance) {
      // TODO avoid duplicate code with sparse instance
      Builder builder = new Builder();
      builder.numericAttributes = (BitSet) instance.numericAttributes.clone();
      builder.nominalAttributes= (BitSet) instance.nominalAttributes.clone();
      builder.dateAttributes = (BitSet) instance.dateAttributes.clone();
      double[] attributes = instance.getAttributes();
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

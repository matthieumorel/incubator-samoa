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
import java.util.Map;

public abstract class AbstractInstance implements Instance {

  private double label;
  private double weight;

  protected BitSet numericAttributes;
  protected BitSet nominalAttributes;
  protected BitSet dateAttributes;

  protected AbstractInstance(double label, double weight, BitSet numericAttributes, BitSet nominalAttributes, BitSet dateAttributes) {
    this.label = label;
    this.weight = weight;
    this.numericAttributes = numericAttributes;
    this.nominalAttributes = nominalAttributes;
    this.dateAttributes = dateAttributes;
  }

  @Override
  public double getLabel() {
    return label;
  }

  @Override
  public double getWeight() {
    return weight;
  }

  @Override
  public boolean isNumeric(int attributeIndex) {
    return numericAttributes.get(attributeIndex);
  }

  @Override
  public boolean isDate(int attributeIndex) {
    return dateAttributes.get(attributeIndex);
  }

  @Override
  public boolean isNominal(int attributeIndex) {
    return nominalAttributes.get(attributeIndex);
  }

  static abstract class Builder implements InstanceBuilder {

    private double label;
    private double weight;
    protected BitSet numericAttributes;
    protected BitSet dateAttributes;
    protected BitSet nominalAttributes;

    protected double getLabel() {
      return label;
    }

    protected double getWeight() {
      return weight;
    }

    @Override
    public Builder setLabel(double label) {
      this.label = label;
      return this;
    }

    @Override
    public Builder setWeight(double weight) {
      this.weight = weight;
      return this;
    }

    @Override
    public Builder setAttributeMetadata(int index, boolean isNumeric, boolean isNominal, boolean isDate) {
      numericAttributes.set(index, isNumeric);
      nominalAttributes.set(index, isNominal);
      dateAttributes.set(index, isDate);
      return this;
    }

  }

}

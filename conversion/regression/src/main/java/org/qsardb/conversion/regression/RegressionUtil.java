/*
 * Copyright (c) 2011 University of Tartu
 */
package org.qsardb.conversion.regression;

import java.util.*;

import org.qsardb.cargo.pmml.*;
import org.qsardb.model.*;

import org.jpmml.manager.*;

import org.dmg.pmml.*;

public class RegressionUtil {

	private RegressionUtil(){
	}

	static
	public PMML parse(Qdb qdb, String string) throws ParseException {
		EquationParser parser = new EquationParser();
		Equation equation = parser.parseEquation(string);

		RegressionModelManager modelManager = parse(qdb, equation);
		return modelManager.getPmml();
	}

	static
	public RegressionModelManager parse(Qdb qdb, Equation equation){
		RegressionModelManager modelManager = new RegressionModelManager();
		modelManager.createModel(MiningFunctionType.REGRESSION);

		Property property = qdb.getProperty(equation.getIdentifier());
		if(property == null){
			throw new IllegalArgumentException("Property \'" + equation.getIdentifier() + "\' not found");
		}

		FieldName propertyField = FieldNameUtil.addPropertyField(modelManager, property);
		modelManager.setTarget(propertyField);

		RegressionTable regressionTable = new RegressionTable(Double.NaN);
		modelManager.getRegressionTables().add(regressionTable);

		List<Equation.Term> terms = equation.getTerms();
		for(Equation.Term term : terms){
			Double coefficient = Double.valueOf(term.getCoefficient());

			if(term.isIntercept()){
				regressionTable.setIntercept(coefficient);
			} else

			{
				Descriptor descriptor = qdb.getDescriptor(term.getIdentifier());
				if(descriptor == null){
					throw new IllegalArgumentException("Descriptor \'" + term.getIdentifier() + "\' not found");
				}

				FieldName descriptorField = FieldNameUtil.addDescriptorField(modelManager, descriptor);
				RegressionModelManager.addNumericPredictor(regressionTable, descriptorField, coefficient);
			}
		}

		return modelManager;
	}

	static
	public Equation format(Qdb qdb, RegressionModelManager modelManager){
		Equation equation = new Equation();

		FieldName propertyName = modelManager.getTarget();

		Property property = FieldNameUtil.decodeProperty(qdb, propertyName);
		if(property == null){
			throw new IllegalArgumentException("Property \'" + propertyName.getValue() + "\' not found");
		}

		equation.setIdentifier(property.getId());

		List<Equation.Term> terms = new ArrayList<Equation.Term>();
		RegressionTable regressionTable = modelManager.getRegressionTables().get(0);
		List<NumericPredictor> numericPredictors = regressionTable.getNumericPredictors();
		for(NumericPredictor numericPredictor : numericPredictors){
			Equation.Term term = new Equation.Term();

			Double coefficient = Double.valueOf(numericPredictor.getCoefficient());
			term.setCoefficient(coefficient.toString());

			FieldName descriptorName = numericPredictor.getName();

			Descriptor descriptor = FieldNameUtil.decodeDescriptor(qdb, descriptorName);
			if(descriptor == null){
				throw new IllegalArgumentException("Descriptor \'" + descriptorName.getValue() + "\' not found");
			}

			term.setIdentifier(descriptor.getId());

			terms.add(term);
		}

		{
			Equation.Term term = new Equation.Term();

			Double intercept = regressionTable.getIntercept();
			term.setCoefficient(intercept.toString());

			terms.add(term);
		}

		equation.setTerms(terms);

		return equation;
	}
}
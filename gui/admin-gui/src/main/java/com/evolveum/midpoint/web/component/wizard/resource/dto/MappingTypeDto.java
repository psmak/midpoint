/*
 * Copyright (c) 2010-2013 Evolveum
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.evolveum.midpoint.web.component.wizard.resource.dto;

import com.evolveum.midpoint.prism.PrismContext;
import com.evolveum.midpoint.util.exception.SchemaException;
import com.evolveum.midpoint.util.logging.LoggingUtils;
import com.evolveum.midpoint.util.logging.Trace;
import com.evolveum.midpoint.util.logging.TraceManager;
import com.evolveum.midpoint.web.util.ExpressionUtil;
import com.evolveum.midpoint.xml.ns._public.common.common_3.*;
import com.evolveum.prism.xml.ns._public.types_3.ItemPathType;

import javax.xml.bind.JAXBElement;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *  @author shood
 * */
public class MappingTypeDto implements Serializable {

    private static final Trace LOGGER = TraceManager.getTrace(MappingTypeDto.class);

    public static final String F_MAPPING = "mappingObject";
    public static final String F_EXPRESSION = "expression";
    public static final String F_CONDITION = "condition";
    public static final String F_TARGET = "target";
    public static final String F_SOURCE = "source";
    public static final String F_EXPRESSION_TYPE = "expressionType";
    public static final String F_CONDITION_TYPE = "conditionType";
    public static final String F_EXPRESSION_LANG = "expressionLanguage";
    public static final String F_CONDITION_LANG = "conditionLanguage";
    public static final String F_EXPRESSION_POLICY_REF = "expressionPolicyRef";
    public static final String F_CONDITION_POLICY_REF = "conditionPolicyRef";

    private MappingType mappingObject;
    private String expression;
    private String condition;
    private String target;
    private List<String> source = new ArrayList<>();
    private ExpressionUtil.ExpressionEvaluatorType expressionType = null;
    private ExpressionUtil.ExpressionEvaluatorType conditionType = null;
    private ExpressionUtil.Language expressionLanguage = ExpressionUtil.Language.GROOVY;
    private ExpressionUtil.Language conditionLanguage = ExpressionUtil.Language.GROOVY;
    private ObjectReferenceType expressionPolicyRef = null;
    private ObjectReferenceType conditionPolicyRef = null;

    public MappingTypeDto(MappingType mapping, PrismContext prismContext){

        if(mapping == null){
            mappingObject = new MappingType();
        } else {
            mappingObject = mapping;
        }

        if(mappingObject.getChannel().isEmpty()){
            mappingObject.getChannel().add(new String());
        }

        if(mappingObject.getExceptChannel().isEmpty()){
        mappingObject.getExceptChannel().add(new String());
        }

        for(MappingSourceDeclarationType mappingSource: mappingObject.getSource()){
            if(mappingSource.getPath() != null && mappingSource.getPath().getItemPath() != null){
                source.add(mappingSource.getPath().getItemPath().toString());
            }
        }

        if(source.isEmpty()){
            source.add(new String());
        }

        if(mappingObject.getTarget() != null && mappingObject.getTarget().getPath() != null
                && mappingObject.getTarget().getPath().getItemPath() != null){
            target = mappingObject.getTarget().getPath().getItemPath().toString();
        }

        loadExpressions(prismContext);
        loadConditions(prismContext);
    }

    private void loadExpressions(PrismContext context){
        if(mappingObject.getExpression() != null && mappingObject.getExpression().getExpressionEvaluator() != null
                && !mappingObject.getExpression().getExpressionEvaluator().isEmpty()){

            try {
                if(mappingObject.getExpression().getExpressionEvaluator().size() == 1){
                    expression = context.serializeAtomicValue(mappingObject.getExpression().getExpressionEvaluator().get(0), PrismContext.LANG_XML);
                } else{
                    StringBuilder sb = new StringBuilder();
                    for(JAXBElement<?> element: mappingObject.getExpression().getExpressionEvaluator()){
                        String subElement = context.serializeAtomicValue(element, PrismContext.LANG_XML);
                        sb.append(subElement).append("\n");
                    }
                    expression = sb.toString();
                }

                expressionType = ExpressionUtil.getExpressionType(expression);
                if(expressionType != null && expressionType.equals(ExpressionUtil.ExpressionEvaluatorType.SCRIPT)){
                    expressionLanguage = ExpressionUtil.getExpressionLanguage(expression);
                }
            } catch (SchemaException e) {
                //TODO - how can we show this error to user?
                LoggingUtils.logException(LOGGER, "Could not load expressions from mapping.", e, e.getStackTrace());
                expression = e.getMessage();
            }
        }
    }

    private void loadConditions(PrismContext context){
        if(mappingObject.getCondition() != null && mappingObject.getCondition().getExpressionEvaluator() != null
                && !mappingObject.getCondition().getExpressionEvaluator().isEmpty()){

            try {
                if(mappingObject.getCondition().getExpressionEvaluator().size() == 1){
                    condition = context.serializeAtomicValue(mappingObject.getCondition().getExpressionEvaluator().get(0), PrismContext.LANG_XML);
                } else{
                    StringBuilder sb = new StringBuilder();
                    for(JAXBElement<?> element: mappingObject.getCondition().getExpressionEvaluator()){
                        String subElement = context.serializeAtomicValue(element, PrismContext.LANG_XML);
                        sb.append(subElement).append("\n");
                    }
                    condition = sb.toString();

                }

                conditionType = ExpressionUtil.getExpressionType(condition);
                if(conditionType != null && conditionType.equals(ExpressionUtil.ExpressionEvaluatorType.SCRIPT)){
                    conditionLanguage = ExpressionUtil.getExpressionLanguage(expression);
                }
            } catch (SchemaException e) {
                //TODO - how can we show this error to user?
                LoggingUtils.logException(LOGGER, "Could not load expressions from mapping.", e, e.getStackTrace());
                condition = e.getMessage();
            }
        }
    }

    private JAXBElement<?> deserializeExpression(PrismContext prismContext, String xmlCode) throws SchemaException{
        return prismContext.parseAnyValueAsJAXBElement(xmlCode, PrismContext.LANG_XML);
    }

    public MappingType prepareDtoToSave(PrismContext prismContext) throws SchemaException{

        if(mappingObject == null){
            mappingObject = new MappingType();
        }

        if(target != null){
            MappingTargetDeclarationType mappingTarget = new MappingTargetDeclarationType();
            mappingTarget.setPath(new ItemPathType(target));
            mappingObject.setTarget(mappingTarget);
        }

        List<String> existingSources = new ArrayList<>();
        for(MappingSourceDeclarationType s: mappingObject.getSource()){
            if(s.getPath() != null && s.getPath().getItemPath() != null){
                existingSources.add(s.getPath().getItemPath().toString());
            }
        }

        List<MappingSourceDeclarationType> mappingSourceList = new ArrayList<>();
        for(String s: source){

            if(s == null || existingSources.contains(s)){
                continue;
            }

            MappingSourceDeclarationType mappingSource = new MappingSourceDeclarationType();
            mappingSource.setPath(new ItemPathType(s));
            mappingSourceList.add(mappingSource);
        }

        mappingObject.getSource().addAll(mappingSourceList);

        if(expression != null){
            if(mappingObject.getExpression() == null){
                mappingObject.setExpression(new ExpressionType());
            }

            mappingObject.getExpression().getExpressionEvaluator().add(deserializeExpression(prismContext, expression));
        }

        if(condition != null){
            if(mappingObject.getCondition() != null){
                mappingObject.setCondition(new ExpressionType());
            }

            mappingObject.getCondition().getExpressionEvaluator().add(deserializeExpression(prismContext, condition));
        }

        return mappingObject;
    }

    public void updateExpressionGeneratePolicy(){
        expression = ExpressionUtil.getExpressionString(expressionType, expressionPolicyRef);
    }

    public void updateConditionGeneratePolicy(){
        condition = ExpressionUtil.getExpressionString(expressionType, conditionPolicyRef);
    }

    public void updateExpressionLanguage(){
        expression = ExpressionUtil.getExpressionString(expressionType, expressionLanguage);
    }

    public void updateConditionLanguage(){
        condition = ExpressionUtil.getExpressionString(conditionType, conditionLanguage);
    }

    public void updateExpression(){
        expression = ExpressionUtil.getExpressionString(expressionType);
    }

    public void updateCondition(){
        condition = ExpressionUtil.getExpressionString(conditionType);
    }

    public MappingType getMappingObject() {
        return mappingObject;
    }

    public void setMappingObject(MappingType mappingObject) {
        this.mappingObject = mappingObject;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        this.expression = expression;
    }

    public String getCondition() {
        return condition;
    }

    public void setCondition(String condition) {
        this.condition = condition;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public List<String> getSource() {
        return source;
    }

    public void setSource(List<String> source) {
        this.source = source;
    }

    public ExpressionUtil.ExpressionEvaluatorType getExpressionType() {
        return expressionType;
    }

    public void setExpressionType(ExpressionUtil.ExpressionEvaluatorType expressionType) {
        this.expressionType = expressionType;
    }

    public ExpressionUtil.ExpressionEvaluatorType getConditionType() {
        return conditionType;
    }

    public void setConditionType(ExpressionUtil.ExpressionEvaluatorType conditionType) {
        this.conditionType = conditionType;
    }

    public ExpressionUtil.Language getExpressionLanguage() {
        return expressionLanguage;
    }

    public void setExpressionLanguage(ExpressionUtil.Language expressionLanguage) {
        this.expressionLanguage = expressionLanguage;
    }

    public ExpressionUtil.Language getConditionLanguage() {
        return conditionLanguage;
    }

    public void setConditionLanguage(ExpressionUtil.Language conditionLanguage) {
        this.conditionLanguage = conditionLanguage;
    }

    public ObjectReferenceType getExpressionPolicyRef() {
        return expressionPolicyRef;
    }

    public void setExpressionPolicyRef(ObjectReferenceType expressionPolicyRef) {
        this.expressionPolicyRef = expressionPolicyRef;
    }

    public ObjectReferenceType getConditionPolicyRef() {
        return conditionPolicyRef;
    }

    public void setConditionPolicyRef(ObjectReferenceType conditionPolicyRef) {
        this.conditionPolicyRef = conditionPolicyRef;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MappingTypeDto that = (MappingTypeDto) o;

        if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
        if (conditionLanguage != that.conditionLanguage) return false;
        if (conditionPolicyRef != null ? !conditionPolicyRef.equals(that.conditionPolicyRef) : that.conditionPolicyRef != null)
            return false;
        if (conditionType != that.conditionType) return false;
        if (expression != null ? !expression.equals(that.expression) : that.expression != null) return false;
        if (expressionLanguage != that.expressionLanguage) return false;
        if (expressionPolicyRef != null ? !expressionPolicyRef.equals(that.expressionPolicyRef) : that.expressionPolicyRef != null)
            return false;
        if (expressionType != that.expressionType) return false;
        if (mappingObject != null ? !mappingObject.equals(that.mappingObject) : that.mappingObject != null)
            return false;
        if (source != null ? !source.equals(that.source) : that.source != null) return false;
        if (target != null ? !target.equals(that.target) : that.target != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = mappingObject != null ? mappingObject.hashCode() : 0;
        result = 31 * result + (expression != null ? expression.hashCode() : 0);
        result = 31 * result + (condition != null ? condition.hashCode() : 0);
        result = 31 * result + (target != null ? target.hashCode() : 0);
        result = 31 * result + (source != null ? source.hashCode() : 0);
        result = 31 * result + (expressionType != null ? expressionType.hashCode() : 0);
        result = 31 * result + (conditionType != null ? conditionType.hashCode() : 0);
        result = 31 * result + (expressionLanguage != null ? expressionLanguage.hashCode() : 0);
        result = 31 * result + (conditionLanguage != null ? conditionLanguage.hashCode() : 0);
        result = 31 * result + (expressionPolicyRef != null ? expressionPolicyRef.hashCode() : 0);
        result = 31 * result + (conditionPolicyRef != null ? conditionPolicyRef.hashCode() : 0);
        return result;
    }
}
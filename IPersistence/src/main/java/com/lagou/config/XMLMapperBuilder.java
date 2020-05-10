package com.lagou.config;

import com.lagou.pojo.Configuration;
import com.lagou.pojo.MappedStatement;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;

import java.io.InputStream;
import java.util.List;

public class XMLMapperBuilder {
    private Configuration configuration;

    public XMLMapperBuilder(Configuration configuration) {
        this.configuration = configuration;
    }

    public void parse(InputStream inputStream) throws DocumentException {
        Document document = new SAXReader().read(inputStream);
        Element rootElement = document.getRootElement();
        String namespace = rootElement.attributeValue("namespace");
        List<Element> list = rootElement.selectNodes("//select");
        for (Element element : list) {
            setMappedStatement(element,namespace);
        }

        List<Element> updateList = rootElement.selectNodes("//update");
        for (Element element : updateList) {
            this.setMappedStatement(element,namespace);
        }

        List<Element> insertList = rootElement.selectNodes("//insert");
        for (Element element : insertList) {
            this.setMappedStatement(element,namespace);
        }

        List<Element> deleteList = rootElement.selectNodes("//delete");
        for (Element element : deleteList) {
            this.setMappedStatement(element,namespace);
        }
    }
    private void setMappedStatement(Element element,String namespace){
        String id = element.attributeValue("id");
        String resultType = element.attributeValue("resultType");
        String parameterType = element.attributeValue("parameterType");
        String sql = element.getTextTrim();
        MappedStatement mappedStatement = new MappedStatement();
        mappedStatement.setId(id);
        mappedStatement.setParameterType(parameterType);
        mappedStatement.setResultType(resultType);
        mappedStatement.setSql(sql);
        String key = namespace + "." + id;
        configuration.getMappedStatementMap().put(key,mappedStatement);
    }
}

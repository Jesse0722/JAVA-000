package io.github.jesse0722.springDemo.jesse0722.springDemo.beanAutowire;

import org.springframework.beans.factory.xml.NamespaceHandlerSupport;

/**
 * @author Lijiajun
 * @date 2020/11/16 17:28
 */
public class StudentNamespaceHandler extends NamespaceHandlerSupport {
    @Override
    public void init() {
        // 将 "student" 元素注册对应的 BeanDefinitionParser 实现
        registerBeanDefinitionParser("student", new StudentBeanDefinitionParser());
    }
}

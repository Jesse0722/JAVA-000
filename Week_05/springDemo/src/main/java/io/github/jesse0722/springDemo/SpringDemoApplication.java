package io.github.jesse0722.springDemo;

import io.github.jesse0722.springDemo.beanAutowire.BeansConfiguration;
import io.github.jesse0722.springDemo.beanAutowire.Student;
import io.github.jesse0722.springDemo.beanAutowire.User;
import io.github.jesse0722.springDemo.beanAutowire.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

/**
 * @author Lijiajun
 * @date 2020/11/16 12:06
 */

public class SpringDemoApplication {

    public static void main(String[] args) {
//        SpringDemoApplication springDemoApplication = new SpringDemoApplication();
//
//        String xmlPath = "user.xml";
//
//        ApplicationContext applicationContext = new ClassPathXmlApplicationContext(xmlPath);
//        System.out.println(applicationContext.getBean("user1"));
//        System.out.println(applicationContext.getBean("user2"));
//
//
//        ApplicationContext applicationContext1 = new AnnotationConfigApplicationContext(BeansConfiguration.class);
//        System.out.println(applicationContext1.getBean("user"));
//
//        ApplicationContext applicationContext2 = new ClassPathXmlApplicationContext(xmlPath);
//        UserService userService = (UserService) applicationContext2.getBean("userService");
//        System.out.println(userService.getUser());
        DefaultListableBeanFactory beanFactory = new DefaultListableBeanFactory();
        XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(beanFactory);
        reader.loadBeanDefinitions("META-INFO/students-context.xml");

        Student student = beanFactory.getBean(Student.class);
        System.out.println(student);

    }
}

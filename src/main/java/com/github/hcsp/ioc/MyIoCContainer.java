package com.github.hcsp.ioc;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MyIoCContainer {
    // 实现一个简单的IoC容器，使得：
    // 1. 从beans.properties里加载bean定义
    // 2. 自动扫描bean中的@Autowired注解并完成依赖注入
    // 先把所有的实体类放入到map中
    // 然后循环map 拿出每个bean实例 扫描bean实例中的字段Field 如果标注了@Autowired 则从bean中取出
    // 将指定对象变量上此 Field 对象表示的字段设置为指定的新值
    private static ConcurrentHashMap beans = new ConcurrentHashMap();

    public static void main(String[] args) {
        MyIoCContainer container = new MyIoCContainer();
        container.start();
        OrderService orderService = (OrderService) container.getBean("orderService");
        orderService.createOrder();
    }

    // 启动该容器
    public void start() {
        Properties properties = new Properties();
        try {
            properties.load(getClass().getResourceAsStream("/beans.properties"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        properties.forEach((beanName, beanClass) -> {
            try {
                Class klass = Class.forName((String) beanClass);
                Object instance = klass.getConstructor().newInstance();
                beans.put(beanName, instance);
            } catch (Exception e) {
                e.getMessage();
            }
        });
        beans.forEach((beanName, beanInstance) -> di(beanInstance, beans));
    }

    public void di(Object instance, Map beans) {
        List<Field> fields = Stream.of(instance.getClass().getDeclaredFields()).filter((field) ->
                field.getAnnotation(Autowired.class) != null).collect(Collectors.toList());
        fields.forEach(field -> {
            String fieldName = field.getName();
            Object fieldInstance = beans.get(fieldName);
            field.setAccessible(true);
            try {
//                void set(Object obj, Object value)
//                将指定对象变量上此 Field 对象表示的字段设置为指定的新值。
                field.set(instance, fieldInstance);
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        });
    }

    // 从容器中获取一个bean
    public Object getBean(String beanName) {
        return beans.get(beanName);
    }
}

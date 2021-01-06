//package io.kimmking.cache.redission;
//
//import org.redisson.Redisson;
//import org.redisson.api.RMap;
//import org.redisson.api.RedissonClient;
//import org.redisson.config.Config;
//
///**
// * @author Lijiajun
// * @date 2021/01/06 11:39 AM
// */
//public class RedssionClientDemo {
//
//    public static void redissionClient() {
//        Config config = new Config();
//        config.useSingleServer()
//                .setAddress("redis://192.168.50.244:6380").setPassword("123456");
//        RedissonClient client = Redisson.create(config);
//        RMap<String, String> map = client.getMap("map");
//        map.put("key1", "value1");
//        System.out.println(map.get("key1"));
//
//    }
//
//    public static void redssionSentinel() {
//        Config config = new Config();
//        config.useSentinelServers().addSentinelAddress("redis://192.168.50.244:26379")
//                .setCheckSentinelsList(false).setMasterName("mymaster").setPassword("123456");
//        RedissonClient client = Redisson.create(config);
//        RMap<String, String> map = client.getMap("map");
//        map.put("key1", "test");
//        System.out.println(map.toString());
//    }
//
//    public static void main(String[] args) {
//        redissionClient();
//        redssionSentinel();
//    }
//}

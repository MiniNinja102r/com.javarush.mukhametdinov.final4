package com.javarush;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.javarush.config.Config;
import com.javarush.dao.CityDAO;
import com.javarush.database.ConnectionData;
import com.javarush.database.HibernateSessionFactoryProvider;
import com.javarush.database.MysqlConnectionData;
import com.javarush.database.SessionFactoryProvider;
import com.javarush.entity.City;
import com.javarush.redis.CityCountry;
import com.javarush.service.DataTransferService;
import com.javarush.redis.RedisManager;
import com.javarush.service.DataSpeedTestService;
import com.javarush.service.LiquibaseMigrationRunner;
import com.sun.istack.NotNull;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@FieldDefaults(level = AccessLevel.PRIVATE)
public class Main {
    private static final String RESULT_FORMAT = "%s:\t%d ms";
    final SessionFactoryProvider sessionFactoryProvider;
    final RedisManager redisManager;
    final DataTransferService dataTransferService;
    final DataSpeedTestService speedTestService;

    public Main() {
        Config.load();

        final ConnectionData connectionData = new MysqlConnectionData();
        final LiquibaseMigrationRunner lmr = new LiquibaseMigrationRunner(connectionData);
        lmr.runMigrations();

        this.sessionFactoryProvider = new HibernateSessionFactoryProvider();
        sessionFactoryProvider.load();

        this.redisManager = new RedisManager();
        redisManager.load();

        final CityDAO cityDAO = new CityDAO(sessionFactoryProvider.getSessionFactory());
        final ObjectMapper mapper = new ObjectMapper();
        this.dataTransferService = new DataTransferService(
                redisManager, mapper, sessionFactoryProvider, cityDAO
        );
        this.speedTestService = new DataSpeedTestService(
                redisManager, mapper, sessionFactoryProvider, cityDAO
        );
    }

    public static void main(String[] args) {
        final Main main = new Main();

        Map<Integer, String> testResults = main.startTest();
        main.broadcastResults(testResults);

        main.shutdown();
    }

    private Map<Integer, String> startTest() {
        List<City> cities = dataTransferService.fetchData();
        List<CityCountry> preparedData = dataTransferService.transformData(cities);
        dataTransferService.pushToRedis(preparedData);

        sessionFactoryProvider.getSessionFactory().getCurrentSession().close();

        List<Integer> ids = List.of(3, 2545, 123, 4, 189, 89, 3458, 1189, 10, 102);
        Map<Integer, String> testResults = new LinkedHashMap<>();
        for (int i = 0; i < Config.generalConfig.checkQuantity(); i++) {
            testEach(i, ids, testResults);
        }
        return testResults;
    }

    private void testEach(int i, List<Integer> ids, @NotNull Map<Integer, String> testResults) {
        try {
            long startRedis = System.currentTimeMillis();
            speedTestService.testRedisData(ids);
            long stopRedis = System.currentTimeMillis();

            long startMysql = System.currentTimeMillis();
            speedTestService.testMysqlData(ids);
            long stopMysql = System.currentTimeMillis();

            String redisResult = String.format(RESULT_FORMAT, "Redis", (stopRedis - startRedis));
            String mysqlResult = String.format(RESULT_FORMAT, "MySQL", (startMysql - stopMysql));

            System.out.println(redisResult);
            System.out.println(mysqlResult);

            testResults.put(i + 1, String.format("%s| %s", redisResult, mysqlResult));
            Thread.sleep(Config.generalConfig.checkPauseInSec() * 1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private void broadcastResults(Map<Integer, String> results) {
        if (results == null || results.isEmpty()) {
            System.out.println("No result data found, check logs");
            return;
        }

        System.out.println("=".repeat(20));
        results.forEach((k, v) -> System.out.println(k + ": " + v));
        System.out.println("=".repeat(20));
    }

    private void shutdown() {
        sessionFactoryProvider.close();
        redisManager.shutdown();
    }
}

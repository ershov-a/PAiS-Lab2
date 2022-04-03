package PAIS.Lab2;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.Random;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {
    // Для записи результатов в файл
    PrintWriter out;
    // Общее количество операций push и pop
    int N = 200000;
    // Количество потоков
    static int threads = 200;
    Random random;

    public Main() {
        // Выходной файл. Каждая строка - время работы N потоков, N - номер строки
        try {
            out = new PrintWriter(new FileOutputStream("output.txt"), true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        out.println("threads,time");
        random = new Random();
    }

    public static void main(String[] args) {
        Main main = new Main();
        // Запуск бенчмарка с разным числом потоков 1..threads
        for (int i = 1; i <= threads; i++) {
            System.out.println("Testing " + i + " threads");
            main.testBench(i);
            System.out.println("Testing done");
        }
    }

    private void testBench(int threadCount) {
        // Счетчик времени работы
        double time = 0;
        // Каждый поток выполняет N/(количество потоков) операций
        var N = this.N / threadCount;
        // 10 одинаковых запусков для усреднения
        for (int k = 0; k < 10; k++) {
            EBStack<Integer> concurrentStack = new EBStack<>();

            // Заполнение стека элементами
            for (int i = 0; i < 1000000; i++) {
                concurrentStack.push(i);
            }

            var timer = new Timer();
            final CyclicBarrier barrier = new CyclicBarrier(threadCount, timer);
            ExecutorService threadPool = Executors.newFixedThreadPool(threadCount);

            for (int i = 0; i < threadCount; i++) {
                // Код, выполняемый каждым потоком
                threadPool.execute(() -> {
                    try {
                        // Ожидание пока все потоки не будут готовы к работе
                        barrier.await();

                        // Выполнение работы
                        try {
                            // Половина операций push
                            for (int j = 0; j < N * 0.5; j++)
                                concurrentStack.push(j + random.nextInt(100));
                            Thread.sleep(1);
                            // Половина операций pop
                            for (int j = 0; j < N * 0.5; j++)
                                concurrentStack.pop();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        // Ожидание пока все потоки не завершат работу
                        barrier.await();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            try {
                // Ожидание завершения работы пула потоков
                threadPool.shutdown();
                threadPool.awaitTermination(10000, TimeUnit.MILLISECONDS);
                // Подсчет времени выполнения
                time += timer.time;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // Вывод среднего времени работы по 10 запускам в файл
        out.println(threadCount + "," + time / 10.0);
    }

    // Таймер для подсчета времени выполнения потока
    private static class Timer implements Runnable {
        public long time;
        private long start;

        public void run() {
            if (start == 0) {
                start = System.currentTimeMillis();
            } else {
                time = (System.currentTimeMillis() - start);
                // System.out.println("Thread done in " + time + " ms");
            }

        }

    }

}


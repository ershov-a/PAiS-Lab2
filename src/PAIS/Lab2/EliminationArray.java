package PAIS.Lab2;

import java.util.Random;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

// Класс elimination массива
public class EliminationArray<T> {
    // Сам массив
    Exchanger<T>[] exchangers;
    // Таймаут обмена
    final long Timeout;
    // Единица измерения времени
    final TimeUnit timeUnit;
    Random random;

    @SuppressWarnings("unchecked")
    public EliminationArray(int capacity, long timeout, TimeUnit unit) {
        exchangers = new Exchanger[capacity];
        for (int i = 0; i < capacity; i++)
            exchangers[i] = new Exchanger<>();
        random = new Random();
        Timeout = timeout;
        timeUnit = unit;
    }

    public T visit(T x) throws TimeoutException, InterruptedException {
        // Генерация случайного индекса elimination массива
        int i = random.nextInt(exchangers.length);
        // Отправка элемента в другой поток и получение от него элемента (либо null)
        return exchangers[i].exchange(x, Timeout, timeUnit);
    }
}
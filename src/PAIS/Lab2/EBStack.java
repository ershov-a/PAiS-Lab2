package PAIS.Lab2;

import java.util.EmptyStackException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicReference;

public class EBStack<T> {
    // Размер elimination массива
    static final int eliminationCapacity = 50;
    // Таймаут для Exchange
    static final long timeout = 10;
    // Единица измерения времени
    static final TimeUnit timeUnit = TimeUnit.MILLISECONDS;
    // Вершина стека
    AtomicReference<Node<T>> top;
    // Elimination массив
    EliminationArray<T> eliminationArray;

    public EBStack() {
        top = new AtomicReference<>(null);
        eliminationArray = new EliminationArray<>(
                eliminationCapacity, timeout, timeUnit
        );
    }

    // Операция push для стека
    public void push(T x) {
        Node<T> n = new Node<>(x);
        // Цикл пока не выполнится push
        while (true) {
            // Если push выполнен (вершина не изменилась в другом потоке, то конец
            if (tryCASPush(n)) return;
            // Иначе работа с elimination массивом
            try {
                T y = eliminationArray.visit(x);
                // Другой поток выполнял pop
                // (visit() вернул null как значение, переданное от другого потока)
                // push выполнен, конец
                if (y == null) return;
            } catch (TimeoutException | InterruptedException ignored) {
            }
        }
    }


    public void pop() throws EmptyStackException {
        // Цикл пока не выполнится pop
        while (true) {
            Node<T> n = tryCASPop();
            // Если pop выполнен (вершина не изменилась в другом потоке, то конец
            if (n != null) return;
            // Иначе работа с elimination массивом
            try {
                T y = eliminationArray.visit(null);
                // Другой поток выполнял pop
                // (visit() вернул не null как значение, переданное от другого потока)
                // pop выполнен, конец
                if (y != null) return;
            } catch (TimeoutException | InterruptedException ignored) {
            }
        }
    }

    protected boolean tryCASPush(Node<T> n) {
        Node<T> m = top.get();
        // n теперь вершина
        n.next = m;
        // Обновление вершины с проверкой, что она не изменилась
        return top.compareAndSet(m, n);
    }

    protected Node<T> tryCASPop() throws EmptyStackException {
        Node<T> m = top.get();
        // Стек пуст
        if (m == null) throw new EmptyStackException();
        Node<T> n = m.next;
        // Обновление вершины с проверкой, что она не изменилась
        return top.compareAndSet(m, n) ? m : null;
    }


    // Элемент стека
    private static class Node<T> {
        // Значение
        public T value;
        // Следующий элемент
        public Node<T> next;

        public Node(T value) {
            this.value = value;
        }
    }
}

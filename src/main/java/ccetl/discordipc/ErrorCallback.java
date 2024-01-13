package ccetl.discordipc;

public interface ErrorCallback {
    void error(Throwable throwable);
    void error(int code, String message);
}
